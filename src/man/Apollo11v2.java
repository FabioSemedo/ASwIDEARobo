package man;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.HitByBulletEvent;
import robocode.CustomEvent;
import robocode.Condition;
import robocode.BulletHitEvent;
import robocode.Bullet;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static robocode.util.Utils.normalRelativeAngle;
import static robocode.util.Utils.normalRelativeAngleDegrees;

/* Apollo11 Tank written by  Maria A. and Fábio S.
 *
 * The movement of this bot was adapted from the wave surfing algorithm, due credit to:
 *   Patrick "Voidious" Cupka, Wave Surfing Tutorial, at https://robowiki.net/wiki/Wave_Surfing_Tutorial on 11/03/2025,
 *   Vincent Maliko, code of the Wave Surfing Tutorial by "Voidious", at https://github.com/malikov/robocode/blob/master/WaveSurfer.java on 11/03/2025,
 * Additional help:
 *   "Kawigi" ,GuessFactor Targeting Tutorial , at https://robowiki.net/wiki/GuessFactor_Targeting_Tutorial
 *   ChatGPT , code and learning assistant, https://chatgpt.com/.
 */

public class Apollo11v2 extends AdvancedRobot {
    //space for GuessFactor targeting indexes
    private static final int GUESS_FACTOR_RANGE = 47;
    public static double[] surfStats =new double[GUESS_FACTOR_RANGE];

    // Battlefield dimensions for wall smoothing
    public static final int WALL_STICK = 50; // minimum pixels we keep from the wall
    public static final int FIELD_CENTRE_RANGE = 50; // minimum pixels we keep from the wall
    public boolean movingToCentre;

    public static int[] dadosLoc = new int[GUESS_FACTOR_RANGE]; // histograma
    public static int contador = 0; //conta quantos tiros damos
    public ArrayList<Onda> tiros;

    public Point2D.Double myLocation;
    public Point2D.Double enemyLocation;

    public ArrayList<Wave> waves;     // List of active waves
    public ArrayList<Integer> surfDirections;   // Directions of past movements

    public static double enemyEnergy = 100.0;   // Enemy's last known energy level
    

    // MOVEMENT CODE - (Start) - WaveSurfing
    // ======================================================================


    public void run() {
        setBodyColor(Color.black);
        setGunColor(Color.red);
        setRadarColor(Color.black);
        setBulletColor(Color.red);

        myLocation = new Point2D.Double(getX(), getY());
        waves = new ArrayList<Wave>();
        surfDirections = new ArrayList<Integer>();
        movingToCentre = false;

        //Too close to a wall
        addCustomEvent(new Condition("walled") {
            public boolean test() {
                return !movingToCentre && isNearWall(getX(), getY(), WALL_STICK);
            }
        });

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true){

            turnRadarRight(Double.POSITIVE_INFINITY); // Keep scan if we lose the enemy
        }
    }//end run()

    //Escape walls if we spawn there
    public void onCustomEvent(CustomEvent e) {
        if (e.getCondition().getName().equals("walled")) {
            double range = FIELD_CENTRE_RANGE;
            double waveAngle;
            Point2D.Double centre;
            double distance;
            double angleToCentre;

            movingToCentre=true;

            //Check for out of bounds
            if( (getBattleFieldHeight() < FIELD_CENTRE_RANGE*2)
                    || (getBattleFieldWidth() < FIELD_CENTRE_RANGE*2)
            ){
                range = Math.min(getBattleFieldHeight(), getBattleFieldWidth())*0.05;
            }

            double x = getBattleFieldWidth()/2 + Math.random()*range*2 - range;
            double y = getBattleFieldHeight()/2 + Math.random()*range*2 - range;

            centre = new Point2D.Double(x,y);

            distance = myLocation.distance(centre);
            angleToCentre = absoluteBearing(myLocation, centre);

            // Create wavy movement by adjusting turn angle dynamically
            for (int i = 0; i < distance; i += 20) {
                waveAngle = Math.sin(i / 50.0) * 30;
                setTurnRight(angleToCentre + waveAngle); // Slight left/right adjustments
                setAhead(20); // Move in small steps

            }

            movingToCentre = false;
        }//end of walled custom event
    }

    //Checks if enemy fired and updates Wave ArrayList based on energy drops.
    public void waveHandler(ScannedRobotEvent e) {
        double enemyAbsBearing = getHeading() + e.getBearing();
        double bulletPower;

/*       ArrayList<Integer> _surfDirections = new ArrayList<Integer>();

        newWave = (bulletPower < 3.01 && bulletPower > 0.09) && (_surfDirections.size() > 2);

        if(newWave){

        }
*/

        // Detect enemy fire
        bulletPower = Math.min(3, enemyEnergy - e.getEnergy());
        if (bulletPower >= 0.1 && bulletPower <= 3) {
            int direction = Wave.calcDirection(getVelocity(), getHeading(), enemyAbsBearing);
            double bulletSpeed = 20 - (3 * bulletPower);

            waves.add(new Wave(getTime() -1 , enemyLocation, enemyAbsBearing, bulletSpeed, direction));
        }

        // Move perpendicular to enemy while avoiding walls
        double absoluteBearing = getHeading() + e.getBearing();
        double bearingFromGun = robocode.util.Utils.normalRelativeAngleDegrees(absoluteBearing - getGunHeading());

        // Update last known enemy energy level for the next turn
        enemyEnergy = e.getEnergy();
        // Update last known enemy location for the next turn
        enemyLocation = project(myLocation, enemyAbsBearing, e.getDistance());
        // Update
        myLocation.setLocation(getX(),getY());

        updateWaves();
        //goWaveSurf();
    }

    public void updateWaves() {
        for (int i = 0; i < waves.size(); i++) {
            Wave enemyWave = waves.get(i);

            // Remove waves that have passed (with tolerance)
            if (enemyWave.distanceTraveled(getTime()) > myLocation.distance(enemyWave.startPoint) + 50) {
                waves.remove(i);
                i--;
            }
        }
    }

    public void goWaveSurf(){
        Wave nextWave = getClosestSurfableWave();
        if (nextWave == null) return;

        // Check danger for both directions
        double dangerLeft = checkDanger(nextWave, -1);
        double dangerRight = checkDanger(nextWave, 1);

        // Choose the safer direction
        double goAngleRd = absoluteBearing(nextWave.startPoint, myLocation);
        if (dangerLeft < dangerRight) {
            goAngleRd = wallSmoothing(myLocation, goAngleRd - (Math.PI / 2), -1);
        } else {
            goAngleRd = wallSmoothing(myLocation, goAngleRd + (Math.PI / 2), 1);
        }

        // Move in the chosen direction
        setBackAsFront(this, goAngleRd);
    }

    //Returns the wave that will arrive at our location the soonest
    public Wave getClosestSurfableWave(){
        //Start at the max possible time (longest path and slowest bullet)
        double shortestTime; // The closest arrival time of a wave to our location
//        shortestTime <-- Math.pow(getBattleFieldHeight(), 2) + Math.pow(getBattleFieldWidth(),2) ;
//        shortestTime <-- Math.sqrt(shortestTime); //Pythagoras for diagonal of the battlefield
//        shortestTime <-- shortestTime/(11); //Divided by the speed of bullet of power 3
        shortestTime = Math.sqrt(Math.pow(getBattleFieldHeight(),2) + Math.pow(getBattleFieldWidth(),2))/11;

        Wave wave = null; // incoming wave
        double arrivalTime;

        for( Wave ew : waves ){
            //In how many ticks this wave will reach us
            arrivalTime = myLocation.distance(ew.startPoint)/ew.bulletSpeed - (getTime() - ew.startTime);

            // >1 implies we have more than 1 tick to react
            if(arrivalTime > 1 && arrivalTime < shortestTime){
                wave = ew;
                shortestTime = arrivalTime;
            }
        }

        return wave;
    }

    //We were shot.
    public void waveHit(double blSpeed){
        Wave hitWave = null;
        Point2D.Double thisLocation = new Point2D.Double(getX(), getY()); //Not dependent on global variables

        for(Wave w : waves){
            // check if the speed and timing match some wave (with a little tolerance)
            if( (Math.abs(w.distanceTraveled(getTime()) - w.startPoint.distance(thisLocation)) <= 10) //Compare distances myLocation-to-waveStartPoint and waveDistanceTraveled
                    && (Math.abs(w.bulletSpeed - blSpeed) <= 0.1) // compare speed of the bullet that hit us to the waveBulletSpeed
            ){
                hitWave = w;
            }
        }

        // If we found a matching wave, update the stats.
        if (hitWave != null) {

            // Map the guess factor from [-1, 1] to a bin index (our bins are the elements of the surfStats defined as Guess_Factor_Range)
            // Increment the stat for this guess factor.
            surfStats[getFactorIndex(hitWave, thisLocation)]++;

            // Remove the wave so it isn't used again.
            waves.remove(hitWave);
        }
    }

    //Method based on wallSmoothing from the Surfing Tutorial
    // angle - try to find an angle that will keep us away from a wall
    // orientation - (1 or -1), informs if angle turns right (1) or angle turns left (-1)
    //Causes an infinite loop on runtime
    public double wallSmoothing(Point2D.Double currectLocation, double angleRd, int orientation) {
        //decide if we need to turn to avoid walls and by how much

        while (isNearWall(project(currectLocation, angleRd, WALL_STICK), WALL_STICK)) {
            angleRd = angleRd + orientation*0.05;
        }
        return angleRd;
    }

    private boolean isNearWall(double x, double y, double minSpace) {
        return ((x < minSpace) || (x > (getBattleFieldWidth() - minSpace)) ||
                (y < minSpace) || (y > getBattleFieldHeight() - minSpace) );
    }
    private boolean isNearWall(Point2D.Double point, double minSpace) {
        return isNearWall(point.x, point.y, minSpace);
    }

    //Tells us how often we are shot based on the past
    public double checkDanger(Wave wave, int direction) {
        return surfStats[ getFactorIndex(wave, estimateFuturePosition(wave, direction)) ];
    }

    //Tells us what bearing (normalised to the guess factor range [-1, 1]) a point in a wave corresponds to.
    //This index is in [0 , GUESS FACTOR RANGE-1]
    public static int getFactorIndex(Wave ew, Point2D.Double targetLocation) {
        double offsetAngle = (absoluteBearing(ew.startPoint, targetLocation) - ew.directAngle);
        double factor = ew.direction * normalRelativeAngleDegrees(offsetAngle) / (Math.sin(8.0 / (ew.bulletSpeed)));
        int index = (int) (0.5 * (GUESS_FACTOR_RANGE - 1) * (1.0 + factor));

        if (index < 0) {
            return 0;
        }else if(index > GUESS_FACTOR_RANGE - 1) {
            return GUESS_FACTOR_RANGE - 1;
        }

        return  index;
    }

    //Estimates the future position of the bot based on game physics.
    public Point2D.Double estimateFuturePosition(Wave wave, int direction) {
        Point2D.Double futurePosition = new Point2D.Double(getX(), getY());

        //For the sake of the math, radians were used.
        double speed = getVelocity();
        double heading = getHeadingRadians();
        double turnLimit;
        double moveAngle;
        double movementDirection;
        double waveDistance;
        double perpendicularAngle;

        int timeStep = 0;
        boolean reached = false;

        while(!reached && timeStep < 500){
            waveDistance = wave.startPoint.distance(futurePosition);
            perpendicularAngle = Math.PI / 2 * Math.min(1, waveDistance / (20 * wave.bulletSpeed));

            // Calculate movement angle considering walls
            moveAngle = wallSmoothing(futurePosition, absoluteBearing(wave.startPoint, futurePosition) + (direction * perpendicularAngle), direction);
            moveAngle = moveAngle - heading; //Adjust for current heading
            movementDirection = 1;

            // Correct for downward movement
            if (Math.cos(moveAngle) < 0) {
                moveAngle = moveAngle + Math.PI;
                movementDirection = -1;
            }

            moveAngle = normalRelativeAngle(moveAngle);
            // Limit turning rate per tick according to the Surfing tutorial
            turnLimit = Math.PI / 720.0 * (40.0 - 3.0 * Math.abs(speed));

            if(moveAngle < -turnLimit) moveAngle = -turnLimit;
            if(moveAngle > turnLimit) moveAngle = turnLimit;
            if(-0.01 < moveAngle && moveAngle < 0.01) moveAngle = 0.01; //struggles with 0


            heading = normalRelativeAngle(heading + moveAngle);

            // Adjust speed based on direction
            if(speed * movementDirection < 0){
                speed = speed * (1 + 2*movementDirection); //deceleration = 2
            }else{
                speed = speed * (1 + movementDirection); //acceleration = 1
            }

            //ensure speed is within the correct bounds
            if(speed < -8) speed = -8; //max reverse speed
            if(speed > 8) speed = 8; //max forward speed

            // Compute the next position
            futurePosition = project(futurePosition, heading, speed);
            timeStep++;

            // Check if the wave reaches the predicted position
            if (futurePosition.distance(wave.startPoint) < wave.bulletSpeed * (1 + timeStep + getTime() - wave.startTime) ) {
                reached = true;
            }
        }//end while

        return futurePosition;
    }

    //Decide if it is better to move forward or backwards to reach a given angle
    //Method from Surfing Tutorial
    public static void setBackAsFront(AdvancedRobot robot, double goAngleRd) {
        double angle = normalRelativeAngle(goAngleRd - robot.getHeadingRadians());
        if (Math.abs(angle) <= (Math.PI /2)) {
            //North
            robot.setAhead(100);

            //NorthWest
            if (angle < 0) robot.setTurnLeft(-1*angle);
                //NorthEast
            else robot.setTurnRight(angle);

        } else {
            //South
            robot.setBack(100);
            //SouthWest
            if (angle < 0) robot.setTurnRight(Math.PI + angle);
                //SouthEast
            else robot.setTurnLeft(Math.PI - angle);

        }
    }

    //Note that trigonometry in Robocode has an offset of 90º anticlockwise
    //This results in us having x:sin and y:cos instead of x:cos and y:sin

    //Method taken from the Robowiki Wave Surfing Tutorial
    /**
     * @param sourceLocation is the origin point
     * @param angle (radians) is the direction of translation
     * @param length distance from origin
     *
     * @return A point in the direction and at a distance of from the origin
     * */
    public static Point2D.Double project(Point2D.Double sourceLocation, double angle, double length) {
        return new Point2D.Double(sourceLocation.x + Math.sin(angle) * length, sourceLocation.y + Math.cos(angle) * length);
    }
    public static Point2D.Double project(double x, double y, double angle, double length) {
        return new Point2D.Double(x + Math.sin(angle) * length, y + Math.cos(angle) * length);
    }

    //Returns the bearing (radians) of a point relative to a given origin point
    public static double directAngleDg(Point2D.Double origin, Point2D.Double destination){
        return Math.toDegrees(Math.atan2(destination.x - origin.x, destination.y - origin.y));
    }
    //Returns the bearing (radians) of a point relative to a given origin point
    public static double directAngle(Point2D.Double origin, Point2D.Double destination){
        return (Math.atan2(destination.x - origin.x, destination.y - origin.y));
    }

    /**@return the direction, in  radians,  of a target point relative to a starting point*/
    public static double absoluteBearing(Point2D.Double source, Point2D.Double target) {
        return Math.atan2(target.x - source.x, target.y - source.y);
    }
    public static double absoluteBearing(double sourceX, double sourceY, double targetX, double targetY) {
        return Math.atan2(targetX - sourceX, targetY - sourceY);
    }

    // ======================================================================
    // MOVEMENT SECTION -(END)

    // ======================================================================
    // AIMING SECTION - Preserve Apollo11's Existing Implementation (BEGIN)
    // ======================================================================

    public void onScannedRobot(ScannedRobotEvent e) {
        // Radar lock
        perfectRadar(e.getBearing());

        // Movement
        waveHandler(e);
        goWaveSurf();

        //Targeting
        double distanciaInimigo = e.getDistance(); //Distancia do inimigo
        double direcaoInimigo = getHeadingRadians() + e.getBearingRadians(); // direcao que o inimigo esta
        double AnguloInimigo = getHeading() + e.getBearing(); // angulo do inimigo em relacao a mim


        double InimigoX = getX() + Math.sin(direcaoInimigo) * distanciaInimigo;
        //localizacao atual do inimigo em x
        double InimigoY = getY() + Math.cos(direcaoInimigo) * distanciaInimigo;
        //localizacao atual do inimigo em x

        double MyBulletPower = Math.min(3, getEnergy() - 0.1);
        // força da bala
        double MyBulletSpeed = 20 - (3 * MyBulletPower); // velocidade das minhas balas

        //direcao da fuga
        int caminho = 1; //direita
            if (e.getVelocity() != 0) {

            if (Math.sin((e.getHeadingRadians() - (getHeadingRadians() + e.getHeadingRadians())) * e.getVelocity()) >= 0) {
                caminho = -1; //esquerda
            }
        }

        double ajustarAnguloArma; //quanto teremos que ajustar a arma para atirar

            if (contador == 0) {
            ajustarAnguloArma = normalRelativeAngleDegrees(AnguloInimigo - getGunHeading());
            // ajusta o angulo da arma, quando ainda nao temos historico
        } else {
            ajustarAnguloArma = previsaoAngulo(e, tiros.get(maisProvavel()).anguloMaximo(MyBulletSpeed));
            //utiliza o historico para tentar prever aonde o robô inimigo estará
        }

        // se a arma precisar de um ajuste muito grande,
        // é melhor não atirar nessa rodada para não desperdiçar o tiro
            if (ajustarAnguloArma < Math.atan2(9, e.getDistance())) {

            setTurnGunRightRadians(ajustarAnguloArma);
            //caso precisar de ajuste, mas não muito grande

            //se a arma estiver com getGunHeat() == 0, ela poderá atirar
            //essa condição é necessária, pois se nao estiver ela tentará atirar e falhará
            // e podemos perder o inimigo
            if (getGunHeat() == 0) {
                setFire(MyBulletPower); //atira
                Onda disparo = new Onda(getTime(), getX(), getY(), InimigoX,
                        InimigoY, MyBulletSpeed, direcaoInimigo, dadosLoc, caminho);
                //cria uma onda correspondente a bala
                tiros.add(disparo);
                //guardo o disparo em um ArrayList, para contabiliza-lo

            } else {
                setTurnGunRightRadians(ajustarAnguloArma);
                perfectRadar(e.getBearing());
                //se a arma não está pronta para atirar,
                //apenas ajustar para o proximo turno
            }

            if (ajustarAnguloArma == 0) {

                scan();
                //continua escaneando o inimigo, para não o perdermos

            }
        }


    }

    public void onBulletHit(BulletHitEvent e) {
        //Quando nossa bala atinge o inimigo

        Bullet bala = e.getBullet();
        double balaVelocidade = bala.getVelocity(); // velocidade da bala

        //olhar todos os tiros que fizemos
        // a procura do tiro que corresponda ao que atingiu o alvo
        for (Onda disparo : tiros) {

            // vê se as balas tem velocidade semelhantes
            if (Math.abs(disparo.bulletSpeed - balaVelocidade) < 0.001) {
                // vê se a bala esteve proximo do inimigo
                if (disparo.proximo(bala.getX(), bala.getY())) {
                    disparo.updates(disparo.indice()); //atualiza o histograma
                    tiros.remove(disparo); // tira o disparo do Array
                    break;
                }
            }

        }

        contador++;
        //esse contador serve para dizer se
        // já temos historico gravado no histograma
    }

    //fuga mais frequente do robô
    public int maisProvavel ()
    {
        int melhor = (GUESS_FACTOR_RANGE - 1) / 2;
        // centro do histograma, onde o robô ficaria se estivesse parado
        for (int i = 0; i < dadosLoc.length; i++) {
            // dizer qual é o valor maximo no histograma
            if (dadosLoc[i] > dadosLoc[melhor]) {

                melhor = i;
            }

        }
        return  melhor;
    }

    // essa função nos devolve o angulo maximo
    // que o inimigo pode usar para escapar do nosso tiro
    public double anguloMaximo(double MyBulletSpeed){

        return Math.asin(8.0/MyBulletSpeed);
        // aqui queremos saber qual é o angulo que possui
        // o seno da (velocidade maxima que o robo pode atingir)/(velocidade da bala atirada)
        // sendo a velocidade a velocidade da bala atirada = 8

    }

    //retorna o angulo que devemos nos mover para atirar
    public double previsaoAngulo(ScannedRobotEvent e, double bulletSpeed) {
        int melhor = maisProvavel(); // indica para onde o inimigo normalmente foge
        double direcaoInimigo =  getHeadingRadians() + e.getBearingRadians();
        double desnormalizar = (double) (melhor/(GUESS_FACTOR_RANGE - 1))*2 - 1;
        // agora faz o processo inverso de normalizar
        // coloca o angulo de volta dentro do intervalo [-1, 1]

        double ajuste = desnormalizar * anguloMaximo(bulletSpeed);
        //como devemos ajustar a arma
        double locExata = direcaoInimigo + ajuste;
        //localização para onde tem que apontar a arma
        return normalRelativeAngleDegrees(direcaoInimigo - getGunHeadingRadians() + locExata);
        //angulo que devemos mover para atirar

    }

    // ======================================================================
    // AIMING SECTION - END)


    //Continuous enemy scan
    public void perfectRadar(double enemyBearing) {
            double absoluteBearing = getHeading() + enemyBearing; // Direction of the enemy relative to this point
            double radarTurn = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading()); // Radar rotation

            // Rotate slightly ahead of the enemy to maintain lock and auto-scan
            if(radarTurn < 0){
                radarTurn = radarTurn - 0.02;
            }else{
                radarTurn = radarTurn + 0.02;
            }

            //Making the radar over-shoot tends to keep it from losing the enemy
            setTurnRadarRight(radarTurn * 2);
        }
}