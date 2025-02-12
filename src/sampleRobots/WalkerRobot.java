package sampleRobots;

import impl.Point;
import impl.UIConfiguration;
import interf.IPoint;
import robocode.Robot;
import robocode.*;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import algoritmo.AG;
import java.awt.Color;
import java.awt.Graphics;

public class WalkerRobot extends AdvancedRobot {
    /*
     * lista de obstáculos, preenchida ao fazer scan
     */
    private List<Rectangle> obstacles;
    public static UIConfiguration conf;
    private List<IPoint> points;
    private HashMap<String, Rectangle> inimigos; // utilizada par associar inimigos a retângulos e permitir remover
                                                 // retângulos de inimigos já desatualizados

    @Override
    public void run() {
        super.run();

        obstacles = new ArrayList<>();
        inimigos = new HashMap<>();
        conf = new UIConfiguration((int) getBattleFieldWidth(), (int) getBattleFieldHeight(), obstacles);

        while (true) {
            this.turnRadarRight(360);

            // this.ahead(100);
            // this.turnLeft(100);

            // this.execute();
        }
    }

    @Override
    public void onMouseClicked(MouseEvent e) {
        super.onMouseClicked(e);

        Random rand = new Random();

        // conf.setStart(new Point((int) this.getX(), (int) this.getY()));
        // conf.setEnd(new Point(e.getX(), e.getY()));
        // conf.setObstacles(obstacles);

        /*
         * TODO: Implementar a chamada ao algoritmo genético!
         *
         */

        System.out.println("Choo Choo!!!");
        IPoint start = new Point((int) this.getX(), (int) this.getY());
        System.out.println(this.getX() + " " + this.getY());
        IPoint end = new Point(e.getX(), e.getY());
        AG ag = new AG(start, end, obstacles, conf);
        ag.execute();

        List<IPoint> points = ag.getBestSolution().getPath();

        for (int i = 0; i < points.size(); i++)
            robotGoTo(this, points.get(i).getX(), points.get(i).getY());

        /*
         * System.out.println("Choo Choo!!!");
         * points = new ArrayList<>();
         * System.out.println("Choo Choo!!!");
         * points = new ArrayList<>();
         * points.add(new Point((int) this.getX(), (int) this.getY()));
         * int size = rand.nextInt(5); //cria um caminho aleatório com no máximo 5 nós
         * intermédios (excetuando início e fim)
         * for (int i=0;i<size;i++)
         * points.add(new
         * Point(rand.nextInt(conf.getWidth()),rand.nextInt(conf.getHeight())));
         * points.add(new Point(e.getX(), e.getY()));
         * 
         * for (int i=0;i<points.size();i++)
         * robotGoTo(this, points.get(i).getX(), points.get(i).getY());
         */
    }

    /**
     * ******** TODO: Necessário selecionar a opção Paint na consola do Robot
     * *******
     * 
     * @param g
     */
    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);

        g.setColor(Color.RED);
        obstacles.stream().forEach(x -> g.drawRect(x.x, x.y, (int) x.getWidth(), (int) x.getHeight()));

        if (points != null) {
            for (int i = 1; i < points.size(); i++)
                drawThickLine(g, points.get(i - 1).getX(), points.get(i - 1).getY(), points.get(i).getX(),
                        points.get(i).getY(), 2, Color.green);
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        System.out.println("Enemy spotted: " + event.getName());

        Point2D.Double ponto = getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        ponto.x -= this.getWidth() * 2.5 / 2;
        ponto.y -= this.getHeight() * 2.5 / 2;

        Rectangle rect = new Rectangle((int) ponto.x, (int) ponto.y, (int) (this.getWidth() * 2.5),
                (int) (this.getHeight() * 2.5));

        if (inimigos.containsKey(event.getName())) // se já existe um retângulo deste inimigo
            obstacles.remove(inimigos.get(event.getName()));// remover da lista de retângulos

        obstacles.add(rect);
        inimigos.put(event.getName(), rect);

        // System.out.println("Enemies at:");
        // obstacles.forEach(x -> System.out.println(x));
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        super.onRobotDeath(event);

        Rectangle rect = inimigos.get(event.getName());
        obstacles.remove(rect);
        inimigos.remove(event.getName());

    }

    /**
     * Devolve as coordenadas de um alvo
     *
     * @param robot    o meu robot
     * @param bearing  ângulo para o alvo, em graus
     * @param distance distância ao alvo
     * @return coordenadas do alvo
     */
    public static Point2D.Double getEnemyCoordinates(Robot robot, double bearing, double distance) {
        double angle = Math.toRadians((robot.getHeading() + bearing) % 360);

        return new Point2D.Double((robot.getX() + Math.sin(angle) * distance),
                (robot.getY() + Math.cos(angle) * distance));
    }

    /**
     * Dirige o robot (RobotSimples) para determinadas coordenadas
     *
     * @param robot o meu robot
     * @param x     coordenada x do alvo
     * @param y     coordenada y do alvo
     */
    public static void robotGoTo(Robot robot, double x, double y) {
        x -= robot.getX();
        y -= robot.getY();

        double angleToTarget = Math.atan2(x, y);
        double targetAngle = robocode.util.Utils
                .normalRelativeAngle(angleToTarget - Math.toRadians(robot.getHeading()));
        double distance = Math.hypot(x, y);
        double turnAngle = Math.atan(Math.tan(targetAngle));
        robot.turnRight(Math.toDegrees(turnAngle));
        if (targetAngle == turnAngle)
            robot.ahead(distance);
        else
            robot.back(distance);
    }

    private void drawThickLine(Graphics g, int x1, int y1, int x2, int y2, int thickness, Color c) {

        g.setColor(c);
        int dX = x2 - x1;
        int dY = y2 - y1;

        double lineLength = Math.sqrt(dX * dX + dY * dY);

        double scale = (double) (thickness) / (2 * lineLength);

        double ddx = -scale * (double) dY;
        double ddy = scale * (double) dX;
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;
        int dx = (int) ddx;
        int dy = (int) ddy;

        int xPoints[] = new int[4];
        int yPoints[] = new int[4];

        xPoints[0] = x1 + dx;
        yPoints[0] = y1 + dy;
        xPoints[1] = x1 - dx;
        yPoints[1] = y1 - dy;
        xPoints[2] = x2 - dx;
        yPoints[2] = y2 - dy;
        xPoints[3] = x2 + dx;
        yPoints[3] = y2 + dy;

        g.fillPolygon(xPoints, yPoints, 4);
    }
}
