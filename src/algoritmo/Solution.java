package algoritmo;

import impl.Point;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import interf.IPoint;

import java.awt.geom.Point2D;
import java.awt.Rectangle;
import java.awt.geom.Line2D;

public class Solution implements Comparable<Solution> {

    public IPoint start;
    public IPoint end;
    public List<Rectangle> obstacles;

    private int id;
    private List<IPoint> path;
    private double fitness;
    private double distance;
    private int collisions;
    private int generation;

    public Solution(IPoint start, IPoint end, List<Rectangle> obstacles) {
        this.id = 0;
        this.generation = 0;
        this.path = new ArrayList<>();

        this.start = start;
        this.end = end;
        this.obstacles = obstacles;

        initPath(0);
        collisionsAndDistance();
        calculateFitness();
    }

    public Solution(Solution sol) {
        this.id = sol.id;

        this.path = new ArrayList<>();
        for (IPoint p : sol.path)
            this.path.add(new Point(p.getX(), p.getY()));

        this.fitness = sol.fitness;
        this.distance = sol.distance;
        this.collisions = sol.collisions;
        this.generation = sol.generation;

        this.end = sol.end;
        this.start = sol.start;
        this.obstacles = sol.obstacles;
    }

    private void initPath(int minPoints) {
        Random rand = new Random();

        this.path.add(this.start);
        
        int size = Math.max(minPoints, rand.nextInt(AG.maxpoints - minPoints + 1));
        for (int j = 0; j < size; j++) {
            this.path.add(new Point(rand.nextInt(AG.maxWidth),
                    rand.nextInt(AG.maxHeight)));
        }

        this.path.add(this.end);
    }

    private void collisionsAndDistance() {
        int conta = 0;
        int distance = 0;

        // para cada segmento do caminho
        for (int i = 0; i < this.path.size() - 1; i++) {
            Point2D.Double p1 = new Point2D.Double(this.path.get(i).getX(),
                    this.path.get(i).getY());
            Point2D.Double p2 = new Point2D.Double(this.path.get(i + 1).getX(),
                    this.path.get(i + 1).getY());
            Line2D.Double line = new Line2D.Double(p1, p2);

            distance += Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2));

            // para cada obstáculo no caminho
            for (int j = 0; j < this.obstacles.size(); j++)
                if (this.obstacles.get(j).intersectsLine(line))
                    conta++;
        }

        this.collisions = conta;
        this.distance = distance;
    }

    public void calculateFitness() {
        double numberOfPoints = this.path.size();
        double distance = 1 / this.distance;
        double collisions = this.collisions;

        this.fitness = AG.pesoColisoes * Math.exp(-collisions)
                + AG.pesoDistancia * (distance)
                + AG.pesoNumeroPontos * (1 / numberOfPoints);
    }

    public void mutate() {
        Random rand = new Random();

        int randomOption = rand.nextInt(4);

        switch (randomOption) {
            case 0:
                mutateChangePoint_X();
                break;
            case 1:
                mutateChangePoint_Y();
                break;
            case 2:
                mutateChangePositionPoint();
                break;
            case 3:
                mutateAddNewPoint();
                break;
        }

        collisionsAndDistance(); // Recalcula a distância e colisões
        calculateFitness(); // Recalcula o fitness
    }

    /**
     * Função que escolhe um ponto aleatório e altera o valor de X.
     */
    private void mutateChangePoint_X() {
        Random rand = new Random();

        if (path.size() > 3) {

            int indexToMutate = rand.nextInt(path.size() - 2) + 1; // Evita o primeiro e último ponto

            int x = path.get(indexToMutate).getX();

            if (newCoordinatePoint(x) < AG.maxWidth)
                path.get(indexToMutate).setX(newCoordinatePoint(x));

        } else {
            randomOneOrTwoPoints();
        }
    }

    /**
     * Função que escolhe um ponto aleatório e altera o valor de Y.
     */
    private void mutateChangePoint_Y() {
        Random rand = new Random();

        if (path.size() > 3) {

            int indexToMutate = rand.nextInt(path.size() - 2) + 1; // Evita o primeiro e último ponto

            int y = path.get(indexToMutate).getY();

            // Se o novo ponto for maior que a altura máxima ou menor que a altura mínima
            if (newCoordinatePoint(y) < AG.maxHeight)
                path.get(indexToMutate).setY(newCoordinatePoint(y));

        } else {
            randomOneOrTwoPoints();
        }
    }

    /**
     * Função que gera um novo ponto aleatório.
     * 
     * @param coordinate Ponto a ser alterado.
     * @return Novo ponto.
     */
    public int newCoordinatePoint(int coordinate) {
        Random rand = new Random();
        int sign = rand.nextBoolean() ? 1 : -1; // Sorteia entre 1 e -1
        int randomNumber = rand.nextInt(AG.maxRangeNewCoordenate) + 1; // PathDrawingSample.MAX_RANGE_NEW_COORDENATE

        return coordinate + (sign * randomNumber);
    }

    /**
     * Função que escolhe dois pontos aleatórios e troca suas posições.
     */
    private void mutateChangePositionPoint() {
        Random rand = new Random();
        if (path.size() > 4) {
            int indexFrom = rand.nextInt(path.size() - 2) + 1; // Evita o primeiro e último ponto
            int indexTo = rand.nextInt(path.size() - 2) + 1; // Evita o primeiro e último ponto

            IPoint pointFrom = path.get(indexFrom);
            IPoint pointTo = path.get(indexTo);

            path.set(indexFrom, pointTo);
            path.set(indexTo, pointFrom);
        } else {
            randomOneOrTwoPoints();
        }
    }

    /**
     * Função que escolhe um ponto aleatório e o substitui por um novo ponto.
     */
    private void mutateAddNewPoint() {
        Random rand = new Random();

        if (path.size() > 3) {

            int indexToMutate = rand.nextInt(path.size() - 2) + 1; // Evita o primeiro e último ponto

            IPoint newPoint = new Point(rand.nextInt(AG.maxWidth),
                    rand.nextInt(AG.maxHeight));

            path.add(indexToMutate, newPoint);

        } else {
            randomOneOrTwoPoints();
        }
    }

    /**
     * Função que escolhe aleatoriamente entre adicionar um ponto ou dois pontos no
     * caminho.
     */
    private void randomOneOrTwoPoints() {
        Random rand = new Random();
        int randomOption = rand.nextInt(2);

        switch (randomOption) {
            case 0:
                addOnePoint();
                break;
            case 1:
                addTwoPoints();
                break;
        }
    }

    /**
     * Adiciona um ponto no caminho.
     * Mantém o primeiro e último ponto do caminho.
     */
    private void addOnePoint() {
        Random rand = new Random();

        IPoint firsPoint = path.get(0);
        IPoint lastPoint = path.get(path.size() - 1);

        IPoint newPoint = new Point(rand.nextInt(AG.maxWidth),
                rand.nextInt(AG.maxHeight));

        path.clear();
        path.add(firsPoint);
        path.add(newPoint);
        path.add(lastPoint);
    }

    /**
     * Adiciona dois pontos no caminho.
     * Mantém o ponto inicial e final.
     */
    private void addTwoPoints() {
        Random rand = new Random();

        IPoint firsPoint = path.get(0);
        IPoint lastPoint = path.get(path.size() - 1);

        IPoint newPoint1 = new Point(rand.nextInt(AG.maxWidth),
                rand.nextInt(AG.maxHeight));
        IPoint newPoint2 = new Point(rand.nextInt(AG.maxWidth),
                rand.nextInt(AG.maxHeight));

        path.clear();
        path.add(firsPoint);
        path.add(newPoint1);
        path.add(newPoint2);
        path.add(lastPoint);
    }

    public void crossover() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<IPoint> getPath() {
        return this.path;
    }

    public void setPath(List<IPoint> path) {
        this.path = path;
    }

    public double getFitness() {
        return this.fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getCollisions() {
        return this.collisions;
    }

    public void setCollisions(int collisions) {
        this.collisions = collisions;
    }

    public int getGeneration() {
        return this.generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.generation).append(";").append(id).append(";").append(this.path.toString()).append(";")
                .append(this.fitness).append(";").append(this.distance).append(";").append(this.path.size()).append(";")
                .append(this.collisions);

        return sb.toString();
    }

    @Override
    public int compareTo(Solution o) {
        if (this.fitness > o.fitness)
            return 1;
        if (this.fitness < o.fitness)
            return -1;

        return 0;
    }
}
