package interf;

/**
 *
 * Represents a point in the path
 */
public interface IPoint
{

    /**
     * Sets the X value of this point in the path
     * @param x the X value of this point
     */
    void setX(int x);

    /**
     * Sets the Y value of this point in the path
     * @param y the Y value of this point
     */
    void setY(int y);

    /**
     * Returns the X value of this point in the path
     * @return the X value of this point
     */
    int getX();

    /**
     * Returns the Y value of this point in the path
     * @return the Y value of this point
     */
    int getY();

    /**
     * Returns a string representation of the Point
     * @return
     */
    String toString();
}
