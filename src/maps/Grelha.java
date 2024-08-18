package maps;

public class Grelha {

  private double[][] hasEnemy;
  private double robot_x;
  private double robot_y;
  private double damage;
  private double near_enemy;
  private double qtd_enemy;
  private double timespamp;

  public Grelha(double robot_x, double robot_y, int size) {
    this.robot_x = robot_x;
    this.robot_y = robot_y;
    this.hasEnemy = new double[size][size];
    this.damage = 0;
    this.near_enemy = 0;
    this.qtd_enemy = 0;
    this.timespamp = 0;
  }

  public void setHasEnemy(int x, int y) {
    this.hasEnemy[x][y] = 1;
  }

  public double[][] getHasEnemy() {
    return this.hasEnemy;
  }

  public Number getHasEnemy(int x, int y) {
    return this.hasEnemy[x][y];
  }

  public void setDamage(double damage) {
    this.damage = damage;
  }

  public double getDamage() {
    return this.damage;
  }

  public void setNearEnemy(float near_enemy) {
    this.near_enemy = near_enemy;
  }

  public double getNearEnemy() {
    return this.near_enemy;
  }

  public void setQtdEnemy(int qtd_enemy) {
    this.qtd_enemy = qtd_enemy;
  }

  public double getQtdEnemy() {
    return this.qtd_enemy;
  }

  public double getRobotX() {
    return this.robot_x;
  }

  public double getRobotY() {
    return this.robot_y;
  }

  public void setRobotX(double robot_x) {
    this.robot_x = robot_x;
  }

  public void setRobotY(double robot_y) {
    this.robot_y = robot_y;
  }

  public double getTimespamp() {
    return this.timespamp;
  }

  public void setTimespamp(int timespamp) {
    this.timespamp = timespamp;
  }
}
