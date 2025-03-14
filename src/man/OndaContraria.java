
package man;

import robocode.AdvancedRobot;

import java.awt.geom.Point2D;

import static robocode.util.Utils.normalRelativeAngleDegrees;


public class OndaContraria extends AdvancedRobot {

    double x; //minha localizacao por x
    double y; //minha localizacao por y

    double minhadirecao; //direcao que o meu robo esta olhando
    long fireTime; //momento que o tiro foi lançado
    double bulletVelocity; // velocidade da bala na hora do disparo

    //double distanceTraveled;
    int direction; // se ele esta indo para esquerda (-1) ou direita (+1) do nosso robô
    int [] dados;

    public OndaContraria(double meuX, double meuY, long tempo, double velocidade, double mydirection,  int direcao, int [] informacoes) {

        x = meuX; //minha localizacao por x
        y = meuY; //minha localizacao por y
        fireTime = tempo; //momento que o tiro foi lançado
        bulletVelocity = velocidade; // velocidade da bala na hora do disparo
        //distanceTraveled = distancia;
        direction = direcao; // se ele esta indo para esquerda (-1) ou direita (+1) do nosso robô
        minhadirecao = mydirection; //direção que o meu robo esta olhando
        dados = informacoes; // vetor com os dados
    }


    // queremos criar uma onda que relate o lugar mais provavel que o inimigo pode estar ao atirarmos uma bala
    // utilizaremos o algoritmo guessfactory para tentar fazer a melhor previsão
    // de certa forma fazendo o contrario do que fazemos para fugir do inimigo usando wavesurfing

    // essa função nos devolve o angulo maximo que o inimigo pode usar para escapar do nosso tiro
    public double anguloMaximo(){

        return Math.asin(8.0/bulletVelocity);
        // aqui queremos saber qual é o angulo que possui
        // o seno da (velocidade maxima que o robo pode atingir)/(velocidade da bala atirada)
        // sendo a velocidade a velocidade da bala atirada
        // 8

    }



    // para ocorrer uma coleta de dados adequada precisamos
    // contabilizar as jogadas mais provaveis do inimigo
    // para isso manteremos um registro onde terá para onde o inimigo escapou
    // por exemplo se quando atiramos, o inimigo normalmente escapa para a esquerda,
    // é melhor atirar diretamente na esquerda e impedir que ele escape
    // para manter esse registro precisamos identificar se o robô inimigo foi atingido por nossa bala
    // o registro teoricamente mantem dados em relação a como o inimigo "escapou", mas na realidade mantem
    // a "localização" aproximada de onde ele estava quando a bala o atingiu
    public void histograma (double inimigoX, double inimigoY, double tempo)
    {

        // essa funçao diz se a bala ela atingiu o alvo
        // Point2D.distance(x,y, inimigoX, inimigoY) dá a distancia entre o atirador (nosso robô) e o alvo (inimigo)
        // (tempo-fireTime) * bulletVelocity calcula se o tempo que decorreu
        // tornou possivel a bala ter percorrido essa distancia com sua velocidade
        if (Point2D.distance(x,y, inimigoX, inimigoY) <= ((tempo-fireTime) * bulletVelocity ))
        {

            // Caso a bala tenha de fato atingido o alvo, precisamos guardar esta informação

            double direcaoInimigo = Math.atan2(inimigoX - x, inimigoY - y);
            // direção que queremos atirar

            double ajuste = normalRelativeAngleDegrees(direcaoInimigo - minhadirecao);
            // essa variavel é para saber o quanto devemos mover a arma para acertar o inimigo

            double intevalofuga = ajuste/anguloMaximo();
            //temos o intervalo de fuga do inimigo, mas esse intervalo é muito grande
            // precisamos compreender o intervalo entre -1 e 1
            // exemplo: -1 -0.8 -0.6 -0.4 -0.2 0.0 0.2 0.4 0.6 0.8 1
            // se o inimigo ficar parado ele ficará em 0.0, mas se ele for o maximo possivel (dado as limitações da fisica do jogo)
            // para a esquerda ele ira estar em -1

            double normalizar = Math.max(-1, Math.min(1, intevalofuga))* direction;
            // garante que o intervalo de fuga seja entre -1 e 1

            int indice = (int) Math.round(((dados.length - 1)/2.0) * (normalizar+1)); // dividir o array

            // (dados.length - 1)/2, serve para dividir o array de tal forma que no centro seja o 0.0,
            // a esquerda os valores negativos e a direita os positivos
            // normalizar+1 serve para transformar o intervalo [-1, 1] em [0, 2], pois nao existe indice negativo
            // o histograma gravará as informações de forma que [5,2,0,2,1,8,1,1,3,0]
            // significa que o robô normalmente fica parado e não foge (o maximo fica no centro)
            // e seu segundo metodo de fuga mais usado é ir o maximo possivel para a esquerda

            dados[indice]++;
            //contar a frequencia

        }
        // caso não tenha atingido iremos retornar falso


    }
}
