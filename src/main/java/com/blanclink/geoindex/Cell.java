package com.blanclink.geoindex;

public class Cell {

    private static final double MIN_LON = -180.0;
    private static final double MIN_LAT = -90.0;
    public static final double LAT_DEGREE_LENGTH = 111000;
    public static final double LON_DEGREE_LENGTH = 85000;

    private final int x;
    private final int y;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Cell cellOf(IPoint point, double resolution) {

        int x = (int) ((-MIN_LAT + point.getLat()) * LAT_DEGREE_LENGTH / resolution);
        int y = (int) ((-MIN_LON + point.getLon()) * LON_DEGREE_LENGTH / resolution);

        return new Cell(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cell cell = (Cell) o;

        if (x != cell.x) return false;
        return y == cell.y;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
