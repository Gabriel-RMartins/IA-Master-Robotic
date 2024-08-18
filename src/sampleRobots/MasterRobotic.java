package sampleRobots;

import algoritmo.AG;
import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.RegressionModelPrediction;
import impl.Point;
import impl.UIConfiguration;
import interf.IPoint;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import maps.Grelha;
import robocode.*;
import robocode.Robot;
import utils.Utils;

public class MasterRobotic extends AdvancedRobot {

  private List<Rectangle> obstacles;
  public static UIConfiguration conf;
  private List<IPoint> points;
  private HashMap<String, Rectangle> inimigos; // utilizada par associar inimigos a retângulos e permitir remover
  private Grelha map;
  private double[][] location_probability;
  private static final int GRELHA_SIZE = 8;
  private static final int DISTANCE = 200;
  private double previousEnergy = 0;

  // variável que contém o ponto atual para o qual o robot se está a dirigir
  private int currentPoint = -1;

  EasyPredictModelWrapper model, model2;

  @Override
  public void run() {
    super.run();
    map = new Grelha(super.getX(), super.getY(), GRELHA_SIZE);
    obstacles = new ArrayList<>();
    inimigos = new HashMap<>();
    conf = new UIConfiguration(
        (int) getBattleFieldWidth(),
        (int) getBattleFieldHeight(),
        obstacles);
    previousEnergy = getEnergy();

    try {
      // load the model
      // TODO: be sure to change the path to the model!
      // you will need to crate the corresponding .data folder in the package of your
      // robot's class, and copy the model there
      model = new EasyPredictModelWrapper(
          MojoModel.load(
              this.getDataFile("ModeloDisparo.zip")
                  .getAbsolutePath()));

      model2 = new EasyPredictModelWrapper(
          MojoModel.load(
              this.getDataFile(
                  "ModeloMovimentacao.zip")
                  .getAbsolutePath()));

      while (true) {
        //this.setTurnGunRight(360);
        turnGunSlowlyRight(360);
  
        Random rand = new Random();
        setAllColors(
            new Color(rand.nextInt(3), rand.nextInt(3), rand.nextInt(3)));

        // se se está a dirigir para algum ponto
        if (currentPoint >= 0) {
          System.out.println("Going to point " + currentPoint);
          IPoint ponto = points.get(currentPoint);
          // se já está no ponto ou lá perto...
          if (Utils.getDistance(this, ponto.getX(), ponto.getY()) < 2) {
            currentPoint++;
            // se chegou ao fim do caminho
            if (currentPoint >= points.size()) {
              currentPoint = -1;             
            }
          }

          advancedRobotGoTo(this, ponto.getX(), ponto.getY());
        } else {
          if (getEnergy() < previousEnergy) {
            setData(previousEnergy - getEnergy());
          } 
          previousEnergy = getEnergy();
        }

        this.execute();
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void onMouseClicked(MouseEvent e) {
    super.onMouseClicked(e);

    conf.setStart(new Point((int) this.getX(), (int) this.getY()));
    conf.setEnd(new Point(e.getX(), e.getY()));

    /*
     * TODO: Implementar a chamada ao algoritmo genético!
     *
     */
    // System.out.println("Choo Choo!!!");

    AG ag = new AG(conf.getStart(), conf.getEnd(), conf.getObstacles(), conf);
    ag.execute();
    this.points = ag.getBestSolution().getPath();

    currentPoint = 0;
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
    obstacles
        .stream()
        .forEach(x -> g.drawRect(x.x, x.y, (int) x.getWidth(), (int) x.getHeight()));

    if (points != null) {
      for (int i = 1; i < points.size(); i++)
        drawThickLine(
            g,
            points.get(i - 1).getX(),
            points.get(i - 1).getY(),
            points.get(i).getX(),
            points.get(i).getY(),
            2,
            Color.green);
    }
  }

  @Override
  public void onScannedRobot(ScannedRobotEvent event) {
    super.onScannedRobot(event);

    // System.out.println("Enemy spotted: " + event.getName());

    Point2D.Double ponto = utils.Utils.getEnemyCoordinates(
        this,
        event.getBearing(),
        event.getDistance());
    ponto.x -= this.getWidth() * 2.5 / 2;
    ponto.y -= this.getHeight() * 2.5 / 2;

    Rectangle rect = new Rectangle(
        (int) ponto.x,
        (int) ponto.y,
        (int) (this.getWidth() * 2.5),
        (int) (this.getHeight() * 2.5));

    if (inimigos.containsKey(event.getName()))
      obstacles.remove(
          inimigos.get(event.getName())); // se já existe um retângulo deste inimigo // remover da lista de retângulos

    obstacles.add(rect);
    inimigos.put(event.getName(), rect);

    // System.out.println("Enemies at:");
    // obstacles.forEach(x -> System.out.println(x));

    Point2D.Double coordinates = utils.Utils.getEnemyCoordinates(
        this,
        event.getBearing(),
        event.getDistance());
    // System.out.println(
    // "Enemy " +
    // event.getName() +
    // " spotted at " +
    // coordinates.x +
    // "," +
    // coordinates.y +
    // "\n"
    // );
    double posicaoRelativaX = coordinates.x - getX();
    double posicaoRelativaY = coordinates.y - getY();
    RowData row = new RowData();
    row.put("Distancia", event.getDistance());
    row.put("Velocidade", event.getVelocity());
    row.put("Heading", event.getHeading());
    row.put("Bearing", event.getBearing());
    row.put("EnergiaInimigo", event.getEnergy());
    row.put("MinhaEnergia", getEnergy());
    row.put("PosicaoRelativaX", posicaoRelativaX);
    row.put("PosicaoRelativaY", posicaoRelativaY);

    try {
      BinomialModelPrediction p = model.predictBinomial(row);
      System.out.println("Will I hit? ->" + p.label);

      // if the model predicts I will hit...
      if (p.label.equals("hit"))
        this.fire(3);
    } catch (Exception e) {
      e.printStackTrace();
    }
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
  public static Point2D.Double getEnemyCoordinates(
      Robot robot,
      double bearing,
      double distance) {
    double angle = Math.toRadians((robot.getHeading() + bearing) % 360);

    return new Point2D.Double(
        (robot.getX() + Math.sin(angle) * distance),
        (robot.getY() + Math.cos(angle) * distance));
  }

  private void drawThickLine(
      Graphics g,
      int x1,
      int y1,
      int x2,
      int y2,
      int thickness,
      Color c) {
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

  /**
   * Dirige o robot (AdvancedRobot) para determinadas coordenadas
   *
   * @param robot o meu robot
   * @param x     coordenada x do alvo
   * @param y     coordenada y do alvo
   */
  public static void advancedRobotGoTo(
      AdvancedRobot robot,
      double x,
      double y) {
    x -= robot.getX();
    y -= robot.getY();

    double angleToTarget = Math.atan2(x, y);
    double targetAngle = robocode.util.Utils.normalRelativeAngle(
        angleToTarget - Math.toRadians(robot.getHeading()));
    double distance = Math.hypot(x, y);
    double turnAngle = Math.atan(Math.tan(targetAngle));
    robot.setTurnRight(Math.toDegrees(turnAngle));
    if (targetAngle == turnAngle)
      robot.setAhead(distance);
    else
      robot.setBack(
          distance);
    robot.execute();
  }

  private void setData(double damage) {
    map = new Grelha(super.getX(), super.getY(), GRELHA_SIZE);
    inimigos.forEach((k, v) -> {
      setEnemyGrelha(
          v.getX(),
          v.getY(),
          super.getBattleFieldWidth(),
          super.getBattleFieldHeight());
    });

    float near_enemy = 999f;
    int qtd_enemy = 0;
    for (Rectangle r : obstacles) {
      double distance = Math.sqrt(
          Math.pow(r.getX() - super.getX(), 2) +
              Math.pow(r.getY() - super.getY(), 2));
      if (distance < near_enemy)
        near_enemy = (float) distance;
      if (distance <= DISTANCE)
        qtd_enemy++;
    }
    map.setNearEnemy(near_enemy);
    map.setQtdEnemy(qtd_enemy);
    map.setDamage(damage);

    double[][] hasEnemy = map.getHasEnemy();
    RowData row = new RowData();
    for (int i = 0; i < hasEnemy.length; i++) {
      for (int j = 0; j < hasEnemy[i].length; j++) {
        row.put(i + "x" + j, map.getHasEnemy(i, j));
      }
    }

    try {
      location_probability = new double[GRELHA_SIZE][GRELHA_SIZE];
      double cx = 0.0;
      double cy = 0.0;

      row.put("robot_x", map.getRobotX());
      row.put("robot_y", map.getRobotY());
      row.put("damage", map.getDamage());
      row.put("near_enemy", map.getNearEnemy());
      row.put("qtd_enemy", map.getQtdEnemy());

      int x = (int) (map.getRobotX() / (int) (getBattleFieldWidth() / GRELHA_SIZE));
      int y = (int) (map.getRobotY() / (int) (getBattleFieldHeight() / GRELHA_SIZE));
      RegressionModelPrediction p = model2.predictRegression(row);
      location_probability[x][y] = p.value;

      // x - 1
      cx = getRandomXCordenate(x == 0 ? 0 : x - 1);
      cy = getRandomYCordenate(y);
      row.put("robot_x", cx);
      row.put("robot_y", cy);
      p = model2.predictRegression(row);
      location_probability[x == 0 ? 0 : x - 1][y] = p.value;

      // x - 1 / y - 1
      cx = getRandomXCordenate(x == 0 ? 0 : x - 1);
      cy = getRandomYCordenate(y == 0 ? 0 : y - 1);
      row.put("robot_x", cx);
      row.put("robot_y", cy);
      p = model2.predictRegression(row);
      location_probability[x == 0 ? 0 : x - 1][y == 0 ? 0 : y - 1] = p.value;

      // x - 1 / y + 1
      cx = getRandomXCordenate(x == 0 ? 0 : x - 1);
      cy = getRandomYCordenate(y == GRELHA_SIZE - 1 ? GRELHA_SIZE - 1 : y + 1);
      row.put("robot_x", cx);
      row.put("robot_y", cy);
      p = model2.predictRegression(row);
      location_probability[x == 0 ? 0 : x - 1][y == GRELHA_SIZE - 1
          ? GRELHA_SIZE - 1
          : y + 1] = p.value;

      // x + 1
      cx = getRandomXCordenate(x == GRELHA_SIZE - 1 ? GRELHA_SIZE - 1 : x + 1);
      cy = getRandomYCordenate(y);
      row.put("robot_x", cx);
      row.put("robot_y", cy);
      p = model2.predictRegression(row);
      location_probability[x == GRELHA_SIZE - 1 ? GRELHA_SIZE - 1 : x + 1][y] = p.value;

      // x + 1 / y - 1
      cx = getRandomXCordenate(x == GRELHA_SIZE - 1 ? GRELHA_SIZE - 1 : x + 1);
      cy = getRandomYCordenate(y == 0 ? 0 : y - 1);
      row.put("robot_x", cx);
      row.put("robot_y", cy);
      p = model2.predictRegression(row);
      location_probability[x == GRELHA_SIZE - 1 ? GRELHA_SIZE - 1 : x + 1][y == 0
          ? 0
          : y - 1] = p.value;

      // x + 1 / y + 1
      cx = getRandomXCordenate(x == GRELHA_SIZE - 1 ? GRELHA_SIZE - 1 : x + 1);
      cy = getRandomYCordenate(y == GRELHA_SIZE - 1 ? GRELHA_SIZE - 1 : y + 1);
      row.put("robot_x", cx);
      row.put("robot_y", cy);
      p = model2.predictRegression(row);
      location_probability[x == GRELHA_SIZE - 1 ? GRELHA_SIZE - 1 : x + 1][y == GRELHA_SIZE -
          1
              ? GRELHA_SIZE - 1
              : y + 1] = p.value;

      // y - 1
      cx = getRandomXCordenate(x);
      cy = getRandomYCordenate(y == 0 ? 0 : y - 1);
      row.put("robot_x", cx);
      row.put("robot_y", cy);
      p = model2.predictRegression(row);
      location_probability[x][y == 0 ? 0 : y - 1] = p.value;

      // y + 1
      cx = getRandomXCordenate(x);
      cy = getRandomYCordenate(y == GRELHA_SIZE - 1 ? GRELHA_SIZE - 1 : y + 1);
      row.put("robot_x", cx);
      row.put("robot_y", cy);
      p = model2.predictRegression(row);
      location_probability[x][y == GRELHA_SIZE - 1 ? GRELHA_SIZE - 1 : y + 1] = p.value;
      imprimirMatrizCSV(location_probability);

      double min = 999;
      for (int i = 0; i < location_probability.length; i++) {
        for (int j = 0; j < location_probability[i].length; j++) {
          if (location_probability[i][j] != 0.0) {
            if (location_probability[i][j] < min) {
              min = location_probability[i][j];
              cx = getRandomXCordenate(i);
              cy = getRandomYCordenate(j);
            }
          }
        }
      }

      System.out.println(" -----> Percurso  X: " + cx + " Y: " + cy);
      advancedRobotGoTo(this, cx, cy);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setEnemyGrelha(double x, double y, double width, double height) {
    int gridX = (int) (x / (int) (width / GRELHA_SIZE));
    int gridY = (int) (y / (int) (height / GRELHA_SIZE));
    // System.out.println("X: " + x + " Y: " + y + " DecimalX: " + gridX + "
    // DecimalY: " + gridY);
    // System.out.println("Grelha Width: " + (int)(width / GRELHA_SIZE) + " Grelha
    // Height: " + (int)(height / GRELHA_SIZE) + "\n************\n");
    map.setHasEnemy(gridX, gridY);
  }

  private double getRandomXCordenate(int x) {
    Random rand = new Random();
    double limit = getWidth() > getHeight() ? getWidth() : getHeight();
    double cx = x != 0
        ? rand.nextDouble(
            x * ((getBattleFieldWidth() - limit) / GRELHA_SIZE),
            (x + 1) * ((getBattleFieldWidth() - limit) / GRELHA_SIZE))
        : rand.nextDouble(
            0,
            (x + 1) * ((getBattleFieldWidth() - limit) / GRELHA_SIZE));

    return cx;
  }

  private double getRandomYCordenate(int y) {
    Random rand = new Random();
    double cy = y != 0
        ? rand.nextDouble(
            y * (getBattleFieldHeight() / GRELHA_SIZE),
            (y + 1) * (getBattleFieldHeight() / GRELHA_SIZE))
        : rand.nextDouble(0, (y + 1) * (getBattleFieldHeight() / GRELHA_SIZE));

    return cy;
  }

  private void turnGunSlowlyRight(double degrees) {
    double remainingDegrees = degrees;
    while (remainingDegrees > 0) {
        double turnAmount = Math.min(remainingDegrees, 15); // Ajusta este valor para controlar a velocidade
        setTurnGunRight(turnAmount);
        execute(); // Executa o movimento
        remainingDegrees -= turnAmount;
    }
}

  public void imprimirMatrizCSV(double[][] matriz) throws IOException {
    try (RobocodeFileOutputStream bw = new RobocodeFileOutputStream(this.getDataFile(
        "Resultados.csv")
        .getAbsolutePath())) {
      // Percorre as linhas da matriz
      for (double[] linha : matriz) {
        // Converte cada elemento da linha numa string
        StringBuilder linhaCSV = new StringBuilder();
        for (double valor : linha) {
          linhaCSV.append(valor).append(",");
        }

        // Remove a última vírgula
        linhaCSV.deleteCharAt(linhaCSV.length() - 1);

        // Escreve a linha CSV no ficheiro
        bw.write(linhaCSV.toString().getBytes());
        bw.write("\n".getBytes());
      }
    }
  }
}
