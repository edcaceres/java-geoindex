package com.blanclink.geoindex;

import java.util.*;
import java.util.function.Predicate;

import static java.lang.Math.*;

public class PointIndex {

    private static final double EARTH_RADIUS = 6371000;
    private final GeoIndex index;
    private final Map<String, Point> currentPosition;
    private static final Map<Integer, Double> lonDegreeDistance = new HashMap<>();

    private PointIndex(GeoIndex index, Map<String, Point> currentPosition) {
        this.index = index;
        this.currentPosition = currentPosition;
    }

    public PointIndex(double resolution) {
        this.index = new GeoIndex(resolution, HashMap::new);
        this.currentPosition = new HashMap<>();
    }

    public PointIndex clone() {
        PointIndex clone = new PointIndex(this.index, this.currentPosition);
        clone.currentPosition.putAll(this.currentPosition);
        return clone;
    }

    public Point get(String id) {
        if(currentPosition.containsKey(id)) {
            Map<String, Object> entry = (Map<String, Object>) index.getEntryAt(currentPosition.get(id));
            if(entry.containsKey(id)) {
                return (Point) entry.get(id);
            }
        }
        return null;
    }

    public Map<String, Point> getAll() {
        HashMap<String, Point> newPoints = new HashMap<>();
        for (String key : currentPosition.keySet()) {
            newPoints.put(key, currentPosition.get(key));
        }
        return newPoints;
    }

    public void add(Point point) {
        remove(point.getId());
        Map<String, Object> entry = (Map<String, Object>) index.addEntryAt(point);
        entry.put(point.getId(), point);
        currentPosition.put(point.getId(), point);
    }

    public void remove(String id) {
        if (currentPosition.containsKey(id)) {
            Point prevPoint = currentPosition.get(id);
            Map<String, Object> entry = (Map<String, Object>) index.getEntryAt(prevPoint);
            entry.remove(prevPoint.getId());
            currentPosition.remove(prevPoint.getId());
        }
    }

    private List<Point> getPoints(List<Object> entries, Predicate<Point> accept) {
        List<Point> result = new ArrayList<>();
        result = getPointsAppend(result, entries, accept);
        return result;
    }

    private List<Point> getPointsAppend(List<Point> s, List<Object> entries, Predicate<Point> accept) {

        entries.stream()
                .flatMap(entry -> ((Map<String, Object>) entry).values().stream())
                .map(o -> (Point) o)
                .filter(accept)
                .forEach(s::add);

        return s;
    }

    private Double approximateSquareDistance(Point p1, Point p2) {
        double avgLat = (p1.getLat() + p2.getLat()) / 2.0;

        double latLen = abs(p1.getLat() - p2.getLat()) * Cell.LAT_DEGREE_LENGTH;
        double lonLen = abs(p1.getLon()-p2.getLon()) * get(lonDegreeDistance, avgLat);

        return latLen*latLen + lonLen*lonLen;
    }

    private double get(Map<Integer, Double> lonDegreeDistance, double lat) {
        int latIndex = (int) (lat * 10);
        double latRounded = latIndex / 10;

        if (lonDegreeDistance.containsKey(latIndex)) {
            return lonDegreeDistance.get(latIndex);
        } else {
            double dist = distance(new Point("", latRounded, 0.0), new Point("", latRounded, 1.0));
            lonDegreeDistance.put(latIndex, dist);
            return dist;
        }
    }

    private double distance(Point p1, Point p2) {

        double dLat = toRadians(p2.getLat() - p1.getLat());
        double dLng = toRadians(p2.getLon() - p1.getLon());
        double sindLat = sin(dLat / 2);
        double sindLng = sin(dLng / 2);
        double a = pow(sindLat, 2) + pow(sindLng, 2) * cos(toRadians(p1.getLat())) * cos(toRadians(p2.getLat()));
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public List<Point> kNearest(Point point, int k, double maxDistance, Predicate<Point> accept) {
        Map<String, Object> pointEntry = (Map<String, Object>) index.getEntryAt(point);
        List<Point> nearbyPoints = getPoints(Collections.singletonList(pointEntry), accept);

        int totalCount = 0;
        Cell idx = Cell.cellOf(point, index.getResolution());
        double coarseMaxDistance = max(maxDistance * 2.0, index.getResolution() * 2.0 + 0.01);

        for (int d = 1; d * index.getResolution() <= coarseMaxDistance; d++) {
            int oldCount = nearbyPoints.size();

            nearbyPoints = getPointsAppend(nearbyPoints, index.get(idx.getX()-d, idx.getX()+d, idx.getY()+d, idx.getY()+d), accept);
            nearbyPoints = getPointsAppend(nearbyPoints, index.get(idx.getX()-d, idx.getX()+d, idx.getY()-d, idx.getY()-d), accept);
            nearbyPoints = getPointsAppend(nearbyPoints, index.get(idx.getX()-d, idx.getX()-d, idx.getY()-d+1, idx.getY()+d-1), accept);
            nearbyPoints = getPointsAppend(nearbyPoints, index.get(idx.getX()+d, idx.getX()+d, idx.getY()-d+1, idx.getY()+d-1), accept);

            totalCount += nearbyPoints.size() - oldCount;

            if (totalCount > k) break;
        }

        k = min(k, nearbyPoints.size());

        Collections.sort(nearbyPoints, (o1, o2) -> approximateSquareDistance(o1, point)
                .compareTo(approximateSquareDistance(o2, point)));

        for (int i = 0; i < nearbyPoints.size(); i++) {
            if (distance(point, nearbyPoints.get(i)) > maxDistance || i == k) {
                k = i;
                break;
            }
        }

        return nearbyPoints.subList(0, k);
    }

    public List<Point> range(Point topLeft, Point bottomRight) {
        List<Object> entries = index.range(topLeft, bottomRight);
        Predicate<Point> accept = point ->
                between(point.getLat(), bottomRight.getLat(), topLeft.getLat()) &&
                        between(point.getLon(), topLeft.getLon(), bottomRight.getLon());

        return  getPoints(entries, accept);
    }

    private boolean between(Double value, Double min, Double max) {
        return value >= min && value <= max;
    }

}