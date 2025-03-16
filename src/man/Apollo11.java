package man;
import robocode.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static robocode.util.Utils.normalRelativeAngleDegrees;

/** Apollo11 Tank written by [her] and [I].

 * Credit for the core of the Wave Surfing algorithm: Patrick "Voidious" Cupka at https://robowiki.net/wiki/Wave_Surfing_Tutorial on 11/03/2025, and Vincent Maliko from https://github.com/malikov/robocode/blob/master/WaveSurfer.java on 11/03/2025
  Credit for the GuessFactor Targeting algorithm; https://www.youtube.com/watch?v=-aEHOm5toRc and https://www.cse.chalmers.se/~bergert/robowiki-mirror/RoboWiki/robowiki.net/wiki/GuessFactor_Targeting_(traditional).html
 */

public class Apollo11 extends AdvancedRobot {

    // Battlefield dimensions for wall smoothing
    private static final int WALL_SPACE = 100; // minimum pixels we keep from the wall
    private static final int MIN_WALL_SPACE = 40; // minimum pixels we keep from the wall

    private static final int GUESS_FACTOR_RANGE = 47;
    public static double[] surfStats = new double[GUESS_FACTOR_RANGE];

    public static int[] dadosLoc = new int[GUESS_FACTOR_RANGE]; // histograma
    public static int contador = 0; //conta quantos tiros damos
    public ArrayList<Onda> tiros;

    public Point2D.Double myLocation;
    public Point2D.Double enemyLocation;

    public ArrayList<Wave> waves;     // List of active waves
    public ArrayList<Integer> surfDirections;   // Directions of past movements

    public ArrayList<Double> waveAbsBearings;   // Absolute bearings of point of fire from past scans

    public static double enemyEnergy = 100.0;   // Enemy's last known energy level


    public void onScannedRobot(ScannedRobotEvent e) {

        perfectRadar(e);

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
                perfectRadar(e);
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

    public void perfectRadar(ScannedRobotEvent e) {
        double absoluteBearing = getHeading() + e.getBearing(); // Direction of the enemy relative to this point
        double radarTurn = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading()); // Radar rotation

        // Rotate slightly ahead of the enemy to maintain lock and auto-scan
        if(radarTurn < 0){
            radarTurn = radarTurn - 0.02;
        }else{
            radarTurn = radarTurn + 0.02;
        }

//        setTurnRadarRight(radarTurn * 2);
        setTurnGunRight(radarTurn * 2);
    }




}

