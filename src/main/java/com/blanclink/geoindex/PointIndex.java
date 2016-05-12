package com.blanclink.geoindex;

import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import static java.lang.Math.*;

public class PointIndex {

    private static final double EARTH_RADIUS = 6371000;
    private final GeoIndex index;
    private final Map<String, IPoint> currentPosition;
    private static final Map<Integer, Double> lonDegreeDistance = new HashMap<>();

    private PointIndex(GeoIndex index, Map<String, IPoint> currentPosition) {
        this.currentPosition = currentPosition;
        this.index = index;
    }

    public PointIndex(double resolution) {
        this.currentPosition = new HashMap<>();
        this.index = new GeoIndex(resolution, HashMap::new);
    }

    public PointIndex(double resolution, Duration expiration) {
        this.currentPosition = new HashMap<>();
        this.index = new GeoIndex(resolution, () -> {
            ExpiringMap<String, IPoint> eMap = new ExpiringMap<>(expiration);
            eMap.setOnExpire((s, p) -> currentPosition.remove(p.getId()));
            return eMap;
        });
    }

    public PointIndex clone() {
        PointIndex clone = new PointIndex(this.index, this.currentPosition);
        clone.currentPosition.putAll(this.currentPosition);
        return clone;
    }

    public IPoint get(String id) {
        if(currentPosition.containsKey(id)) {
            Map<String, Object> entry = (Map<String, Object>) index.getEntryAt(currentPosition.get(id));
            if(entry.containsKey(id)) {
                return (IPoint) entry.get(id);
            }
        }
        return null;
    }

    public Map<String, IPoint> getAll() {
        HashMap<String, IPoint> newPoints = new HashMap<>();
        for (String key : currentPosition.keySet()) {
            newPoints.put(key, currentPosition.get(key));
        }
        return newPoints;
    }

    public void add(IPoint point) {
        remove(point.getId());
        Map<String, Object> entry = (Map<String, Object>) index.addEntryAt(point);
        entry.put(point.getId(), point);
        currentPosition.put(point.getId(), point);
    }

    public void remove(String id) {
        if (currentPosition.containsKey(id)) {
            IPoint prevPoint = currentPosition.get(id);
            Map<String, Object> entry = (Map<String, Object>) index.getEntryAt(prevPoint);
            entry.remove(prevPoint.getId());
            currentPosition.remove(prevPoint.getId());
        }
    }

    private List<IPoint> getPoints(List<Object> entries, Predicate<IPoint> accept) {
        List<IPoint> result = new ArrayList<>();
        result = getPointsAppend(result, entries, accept);
        return result;
    }

    private List<IPoint> getPointsAppend(List<IPoint> s, List<Object> entries, Predicate<IPoint> accept) {

        entries.stream()
                .flatMap(entry -> ((Map<String, Object>) entry).values().stream())
                .map(o -> (IPoint) o)
                .filter(accept)
                .forEach(s::add);

        return s;
    }

    private Double approximateSquareDistance(IPoint p1, IPoint p2) {
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
            double dist = distance(new BasicPoint("", latRounded, 0.0), new BasicPoint("", latRounded, 1.0));
            lonDegreeDistance.put(latIndex, dist);
            return dist;
        }
    }

    private double distance(IPoint p1, IPoint p2) {

        double dLat = toRadians(p2.getLat() - p1.getLat());
        double dLng = toRadians(p2.getLon() - p1.getLon());
        double sindLat = sin(dLat / 2);
        double sindLng = sin(dLng / 2);
        double a = pow(sindLat, 2) + pow(sindLng, 2) * cos(toRadians(p1.getLat())) * cos(toRadians(p2.getLat()));
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public List<IPoint> kNearest(IPoint point, int k, double maxDistance, Predicate<IPoint> accept) {
        Map<String, Object> pointEntry = (Map<String, Object>) index.getEntryAt(point);
        List<IPoint> nearbyPoints = getPoints(Collections.singletonList(pointEntry), accept);

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

    public List<IPoint> range(IPoint topLeft, IPoint bottomRight) {
        List<Object> entries = index.range(topLeft, bottomRight);
        Predicate<IPoint> accept = point ->
                between(point.getLat(), bottomRight.getLat(), topLeft.getLat()) &&
                        between(point.getLon(), topLeft.getLon(), bottomRight.getLon());

        return  getPoints(entries, accept);
    }

    private boolean between(Double value, Double min, Double max) {
        return value >= min && value <= max;
    }

}