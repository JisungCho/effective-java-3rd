package ch02.item01;

public final class Point {

    private static final Point ORIGIN = new Point(0, 0);

    private final double x;
    private final double y;

    private Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    public static Point origin() {
        return ORIGIN;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}