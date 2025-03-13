package man;


import robocode.*;
import robocode.Robot;

import java.awt.*;
import java.awt.geom.Point2D;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class MeuDeus extends AdvancedRobot {


    //double alturaDaBatalha = getBattleFieldHeight();
    //double larguraDaBatalha = getBattleFieldWidth();
    double direcaoInimigoInicial = 0;

    //movimentação básica
    public void run() {
        setColors(Color.gray, Color.black, Color.white, Color.red, Color.yellow);


        while (true) {
            ahead(100);

            turnGunRight(360);

            back(100);
        }
    }

    //o robô viu outro robô e agora?
    public void onScannedRobot(ScannedRobotEvent e) {
        //Fabs: Considera usar isto:
        // Adjust radar to keep tracking the enemy
        // setTurnRadarRight(robocode.util.Utils.normalRelativeAngleDegrees(enemyAbsBearing - getRadarHeadingRadians()) * 2);
        circularTargeting(e);
    }

    //Fabs: Por convenção, métodos como este não devem começar por letra maiúscula. O mesmo acontece para as variáveis. (reservado para Objectos/Classes)
    //Fabs: Isto parece não terminar. Penso que pode haver um erro de lógica.
    //Fabs: Infinite Loop???
    public void circularTargeting ( ScannedRobotEvent e){

        //CRIAR A VARIAVEL INICIAL DIRECAOINIMIGOINICIAL NAO ESQUECER!!!!
        // MINHAS INFORMAÇÕES
        double atualX = getX();
        double atualY = getY();

        //  VARIAVEIS RELATIVAS A= INIMIGO
        double distanciaInimigo = e.getDistance(); //Distancia do inimigo
        double direcaoEuInimigo = e.getHeadingRadians() + e.getBearingRadians();
        // direção em relação ao meu robo
        double direcaoInimigo = e.getHeadingRadians(); //direção do inimigo em relação ao mapa
        double velocidadeInimigo = e.getVelocity(); // velocidade do inimigo
        double direcaoAnguloInimigo = direcaoInimigo;


        // LOCALIZACAO ATUAL DO INIMIGO
        double InimigoX = atualX + Math.sin(direcaoEuInimigo) * distanciaInimigo;
        //localizacao atual do inimigo em x
        double InimigoY = atualY + Math.cos(direcaoEuInimigo) * distanciaInimigo;
        //localizacao atual do inimigo em x

        // VARIAVEIS TEMPORAIS
        double tempoAtual = 0;
        double tempoFuturo = 0;
        // essa variavel mudará ao decorrer do tempo
        // é iniciada a 0, para indicar que o inimigo ainda está parado

        // LOCALIZAÇÃO FUTURA DO INIMIGO ASSUMINDO QUE ELE SE MOVERA CIRCULARMENTE
        double FuturoInimigoX = InimigoX;
        // essa variavel será mudará ao decorrer do tempo
        // atualmente estamos assumindo que o tempo é 0
        // logo o inimigo ainda não se moveu
        double FuturoInimigoY = InimigoY;
        // essa variavel será mudadá ao decorrer do tempo
        // atualmente estamos assumindo que o tempo é 0
        // logo o inimigo ainda não se moveu


        // VARIAVEIS RELATIVAS A RELACAO EU-INIMIGO
        double AnguloInimigo = getHeading() + e.getBearing(); // angulo do inimigo em relacao a mim
        double ajustarAnguloArma = normalRelativeAngleDegrees(AnguloInimigo - getGunHeading());
        // ajusta o angulo da arma levando em conta
        // o angulo que o inimigo esta e o angulo atual da arma


        // VARIAVEIS RELATIVAS AO PODER DE FOGO NOSSO ROBÔ
        double MyBulletPower = Math.min(3 - ajustarAnguloArma, getEnergy() - 0.1);
        // poder de fogo do nosso robô
        double MyBulletSpeed = 20 - (3 * MyBulletPower); // velocidade das minhas balas
        // RESOLVER OS PROBLEMAS RELACIONADOS A MIRA DA ARMA E DA BALA


        //SIMULAÇÃO DO MOVIMENTO
        while (tempoAtual * MyBulletSpeed < Point2D.distance(atualX, atualY, FuturoInimigoX, FuturoInimigoY)) {
            FuturoInimigoX += velocidadeInimigo * Math.sin(direcaoInimigo); // atualiza "teoricamente" o x do inimigo
            FuturoInimigoY += velocidadeInimigo * Math.cos(direcaoInimigo); // atualiza "teoricamente" o y do inimigo
            direcaoAnguloInimigo += direcaoInimigo - direcaoInimigoInicial;
            // atualiza a direção do inimigo se o inimigo
            direcaoInimigoInicial = direcaoInimigo;
            //posição atual se torna a posição inicial para o proximo movimento
            direcaoInimigo = e.getHeadingRadians();
            // atualiza para a nova direção do inimigo
            tempoAtual++; // atualiza o tempo que passou
        }

        if (Math.abs(ajustarAnguloArma) <= 3) {

            turnGunRight(ajustarAnguloArma); //ajusto a arma

            // checar se a arma pode atirar pq se não puder,
            // podemos perder a direção do inimigo
            if (getGunHeat() == 0) {
                //Fabs: Considera usar setFire(). Como atirar custa un turno, usar setFire permite que outras ações poção ser feitas no mesmo turno (ex movimentar).
                fire(MyBulletPower);
            } else {
                //Fabs: setTurnGunRight().
                turnGunRight(ajustarAnguloArma);
                // se a arma nao esta preparada para atirar, apenas ajustar

            }


            if (ajustarAnguloArma == 0) {
                scan(); //voltar para o inicio do onScannedRobot
            }


        }


//    public void CircularTargeting ( ScannedRobotEvent e){
//
//        //CRIAR A VARIAVEL INICIAL DIRECAOINIMIGOINICIAL NAO ESQUECER!!!!
//        // MINHAS INFORMAÇÕES
//        double atualX = getX();
//        double atualY = getY();
//
//        //  VARIAVEIS RELATIVAS A= INIMIGO
//        double distanciaInimigo = e.getDistance(); //Distancia do inimigo
//        double direcaoEuInimigo = e.getHeadingRadians() + e.getBearingRadians();
//        // direção em relação ao meu robo
//        double direcaoInimigo = e.getHeadingRadians(); //direção do inimigo em relação ao mapa
//        double velocidadeInimigo = e.getVelocity(); // velocidade do inimigo
//        double direcaoAnguloInimigo = direcaoInimigo;
//
//
//        // LOCALIZACAO ATUAL DO INIMIGO
//        double InimigoX = atualX + Math.sin(direcaoEuInimigo)*distanciaInimigo;
//        //localizacao atual do inimigo em x
//        double InimigoY = atualY + Math.cos(direcaoEuInimigo)*distanciaInimigo;
//        //localizacao atual do inimigo em x
//
//        // VARIAVEIS TEMPORAIS
//        double tempoAtual = 0;
//        double tempoFuturo = 0;
//        // essa variavel mudará ao decorrer do tempo
//        // é iniciada a 0, para indicar que o inimigo ainda está parado
//
//        // LOCALIZAÇÃO FUTURA DO INIMIGO ASSUMINDO QUE ELE SE MOVERA CIRCULARMENTE
//        double FuturoInimigoX = InimigoX;
//        // essa variavel será mudará ao decorrer do tempo
//        // atualmente estamos assumindo que o tempo é 0
//        // logo o inimigo ainda não se moveu
//        double FuturoInimigoY = InimigoY;
//        // essa variavel será mudadá ao decorrer do tempo
//        // atualmente estamos assumindo que o tempo é 0
//        // logo o inimigo ainda não se moveu
//
//
//        // VARIAVEIS RELATIVAS A RELACAO EU-INIMIGO
//        double AnguloInimigo = getHeading() + e.getBearing(); // angulo do inimigo em relacao a mim
//        double ajustarAnguloArma = normalRelativeAngleDegrees(AnguloInimigo - getGunHeading());
//        // ajusta o angulo da arma levando em conta
//        // o angulo que o inimigo esta e o angulo atual da arma
//
//
//        // VARIAVEIS RELATIVAS AO PODER DE FOGO NOSSO ROBÔ
//        double MyBulletPower = Math.min(3 - ajustarAnguloArma, getEnergy() - 0.1);
//        // poder de fogo do nosso robô
//        double MyBulletSpeed = 20 - (3* MyBulletPower); // velocidade das minhas balas
//        // RESOLVER OS PROBLEMAS RELACIONADOS A MIRA DA ARMA E DA BALA
//
//
//        //SIMULAÇÃO DO MOVIMENTO
//
//        while (tempoAtual*MyBulletSpeed < Point2D.distance(atualX, atualY, FuturoInimigoX, FuturoInimigoY))
//        {
//            FuturoInimigoX += velocidadeInimigo*Math.sin(direcaoInimigo); // atualiza "teoricamente" o x do inimigo
//            FuturoInimigoY += velocidadeInimigo*Math.cos(direcaoInimigo); // atualiza "teoricamente" o y do inimigo
//            direcaoAnguloInimigo += direcaoInimigo - direcaoInimigoInicial;
//            // atualiza a direção do inimigo se o inimigo
//            direcaoInimigoInicial = direcaoInimigo;
//            //posição atual se torna a posição inicial para o proximo movimento
//            direcaoInimigo = e.getHeadingRadians();
//            // atualiza para a nova direção do inimigo
//            tempoAtual++; // atualiza o tempo que passou
//        }
//
//        if (Math.abs(ajustarAnguloArma) <= 3) {
//
//            turnGunRight(ajustarAnguloArma); //ajusto a arma
//
//            // checar se a arma pode atirar pq se não puder,
//            // podemos perder a direção do inimigo
//            if (getGunHeat() == 0) {
//                fire(MyBulletPower);
//            }
//            else {
//                turnGunRight(ajustarAnguloArma);
//                // se a arma nao esta preparada para atirar, apenas ajustar
//
//            }
//
//
//
//            if (ajustarAnguloArma == 0)
//            {
//                scan(); //voltar para o inicio do onScannedRobot
//            }
//
//
//
//
//
//
//

        // 1) Obter informações do inimigo:
        //
        //Sua posição atual (enemyX, enemyY). X
        //Sua direção de movimento enemyHeading. X
        //Sua velocidade enemyVelocity. X
        //Seu deslocamento angular (quanto ele gira a cada tick).

        // 2) Simular o movimento do inimigo no futuro:
        //
        //Assumimos que ele continuará girando e movendo-se com a mesma velocidade e aceleração.
        //Calculamos sua nova posição em cada tick.

        // 3) Determinar onde o tiro e o inimigo se encontrarão:
        //
        //A bala tem uma velocidade fixa (dependendo do firePower).
        //Precisamos prever quantos ticks a bala levará para alcançar o inimigo.
        //Iteramos para estimar onde o inimigo estará naquele momento.

        // 4) Apontar a arma para essa posição futura e disparar.


//        }
//    }

    }
}