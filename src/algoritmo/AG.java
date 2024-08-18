package algoritmo;

import java.util.*;

import java.awt.Rectangle;
import interf.IPoint;
import interf.IUIConfiguration;

public class AG {

    public static String caminhoArquivo = "Resultados.csv";

    // Variaveis Globais
    public static int populationSize = 100; // Tamanho da população
    public static int generationSize = 100; // Número de gerações
    public static int maxpoints = 5; // Número máximo de pontos intermédios
    public static int mapId = 10; // ID do mapa a usar [ ver Maps.java ]

    // Fitness
    public static double pesoColisoes = 1;// PESO_COLISOES = 1;
    public static double pesoDistancia = 0.5;  // PESO_DISTANCIA = 0.5;
    public static double pesoNumeroPontos = 0.3; // PESO_NUMERO_PONTOS = 0.3;

    // Mutação
    public static int popHereditary = 50; // Quantidade de individuos a passar para a próxima geração
    public static int popMutation = 50; // Quantidade de individuos a sofrer mutação
    public static int maxRangeNewCoordenate = 5;

    public ArrayList<Solution> population;

    /**
     * Melhor solução encontrada após a execução do algoritmo.
     */
    public Solution bestSolution;

    /**
     * Ponto de início do caminho. (Ponto onde se encontra o robô)
     */
    private IPoint start;

    /**
     * Ponto de fim do caminho. (Ponto onde o robô deve chegar)
     */
    private IPoint end;

    /**
     * Lista de obstáculos que o robô deve evitar.
     */
    private List<Rectangle> obstacles;

    /**
     * IU Configuration
     */
    public IUIConfiguration conf;

    /**
     * Altura máxima da janela.
     */
    public static int maxHeight;

    /**
     * Largura máxima da janela.
     */
    public static int maxWidth;

    public AG(IPoint start, IPoint end, List<Rectangle> obstacles, IUIConfiguration conf) {
        this.population = new ArrayList<>();

        this.start = start;
        this.end = end;
        this.obstacles = obstacles;
        this.conf = conf;

        this.maxHeight = conf.getHeight();
        this.maxWidth = conf.getWidth();

        this.bestSolution = new Solution(start, end, obstacles);

        initPopulation();
    }

    private void initPopulation() {
        for (int i = 0; i < populationSize; i++) {
            population.add(new Solution(this.start, this.end, this.obstacles));
        }
    }

    public void execute() {
        int generation = 0;

        while (generation < generationSize) {
            int idTotal = 0;

            population.sort(Collections.reverseOrder()); // Ordena a população

            if (population.get(0).getFitness() > this.bestSolution.getFitness()) {
                this.bestSolution = population.get(0);

                System.out.println(
                        "Atualizando: " + generation + " - " + this.bestSolution.getFitness() + " - "
                                + this.bestSolution.getPath().toString());
            }

            ArrayList<Solution> newSolution = new ArrayList<>();

            for (int i = 0; i < popHereditary; i++) {
                newSolution.add(population.get(i));
            }

            for (int i = 0; i < popMutation; i++) {
                Solution s = new Solution(population.get(i));
                s.mutate();
                newSolution.add(s);
            }

            for (Solution s : population) {
                s.setGeneration(generation);
                s.setId(idTotal++);
            }

            generation++;
            population = newSolution;
        }

        System.out.println("\nFim da execução!");
        System.out.println("BEST:" + this.bestSolution.getFitness() + " - " + this.bestSolution.getPath().toString());

    }

    public ArrayList<Solution> getPopulation() {
        return population;
    }

    public Solution getBestSolution() {
        return this.bestSolution;
    }

}
