package man;

import robocode.AdvancedRobot;

import static robocode.util.Utils.normalRelativeAngleDegrees;

public class Onda {
    long fireTime; //Momento do tiro
    double myX; //Nossas coordenadas
    double myY; //Nossas coordenadas
    double enemyX; //Coordenadas do inimigo
    double enemyY; //Coordenadas do inimigo
    double bulletSpeed; //Velocidade da bala
    double direcaoInicial; //direcao que nosso esta olhando
    int [] histograma;
    int EsqDir;
    Onda(long startTime, double x, double y, double inimigoX, double inimigoY, double MybulletSpeed, double minhadirecao, int [] dados, int caminho)
    {
        fireTime = startTime; //Momento do tiro
        myX = x; //Nossas coordenadas
        myY = y; //Nossas coordenadas
        enemyX = inimigoX; //Coordenadas do inimigo
        enemyY = inimigoY; //Coordenadas do inimigo
        bulletSpeed = MybulletSpeed; //Velocidade da bala
        direcaoInicial = minhadirecao; //direcao que nosso esta olhando
        histograma = dados;
        EsqDir = caminho;
}




    public boolean proximo(double balaX, double balaY)
    {
        return((Math.abs(myX - balaX) < 10)&&(Math.abs(myY - balaY) < 10));

        }




    // essa função nos devolve o angulo maximo que o inimigo pode usar para escapar do nosso tiro
    public double anguloMaximo(double MyBulletSpeed){

        return Math.asin(8.0/MyBulletSpeed);
        // aqui queremos saber qual é o angulo que possui
        // o seno da (velocidade maxima que o robo pode atingir)/(velocidade da bala atirada)
        // sendo a velocidade a velocidade da bala atirada
        // 8

    }



    // essa função serve para converter o metodo de "fuga" que o inimigo ussou
    // em um indice para guardar a informação no histograma

    public int indice() {

        double novaDirecao = Math.atan2(enemyX - myX, enemyY - myY);
        // direção que queremos atirar

        double ajuste = normalRelativeAngleDegrees(novaDirecao - direcaoInicial);
        // essa variavel é para saber o quanto devemos mover a arma para acertar o inimigo

        double intevalofuga = ajuste / anguloMaximo(bulletSpeed);
        //temos o intervalo de fuga do inimigo, mas esse intervalo é muito grande
        // precisamos compreender o intervalo entre -1 e 1
        // exemplo: -1 -0.8 -0.6 -0.4 -0.2 0.0 0.2 0.4 0.6 0.8 1
        // se o inimigo ficar parado ele ficará em 0.0, mas se ele for o maximo possivel (dado as limitações da fisica do jogo)
        // para a esquerda ele ira estar em -1


        double normalizar = Math.max(-1, Math.min(1, intevalofuga))* EsqDir;
        // garante que o intervalo de fuga seja entre -1 e 1

        return (int) Math.round(((histograma.length - 1)/2.0) * (normalizar+1)); // dividir o array

        // (dados.length - 1)/2, serve para dividir o array de tal forma que no centro seja o 0.0,
        // a esquerda os valores negativos e a direita os positivos
        // normalizar+1 serve para transformar o intervalo [-1, 1] em [0, 2], pois nao existe indice negativo
        // o histograma gravará as informações de forma que [5,2,0,2,1,8,1,1,3,0]
        // significa que o robô normalmente fica parado e não foge (o maximo fica no centro)
        // e seu segundo metodo de fuga mais usado é ir o maximo possivel para a esquerda



    }

    //atualiza o histograma
    public void updates (int id)
    {
        histograma[id]++;
    }







}
