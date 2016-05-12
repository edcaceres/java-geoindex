package com.blanclink.geoindex;

public class BasicPoint implements IPoint {

    private final String id;
    private final Double lat;
    private final Double lon;

    public BasicPoint(String id, Double lat, Double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }

    public String getId() {
        return id;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicPoint point = (BasicPoint) o;

        if (!id.equals(point.id)) return false;
        if (!lat.equals(point.lat)) return false;
        return lon.equals(point.lon);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + lat.hashCode();
        result = 31 * result + lon.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return id;
    }
}