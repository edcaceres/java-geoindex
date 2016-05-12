package com.blanclink.geoindex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.blanclink.geoindex.Cell.cellOf;

public class GeoIndex {

    private final double resolution;
    private final Map<Cell, Object> index;
    private final Supplier<Object> newEntry;

    public GeoIndex(double resolution, Map<Cell, Object> index, Supplier<Object> newEntry) {
        this.resolution = resolution;
        this.index = index;
        this.newEntry = newEntry;
    }

    public GeoIndex(double resolution, Supplier<Object> newEntry) {
        this.resolution = resolution;
        this.index = new HashMap<>();
        this.newEntry = newEntry;
    }

    public GeoIndex clone() {
        GeoIndex clone = new GeoIndex(this.resolution, this.index, this.newEntry);
        clone.index.putAll(this.index);
        return clone;
    }

    public Object addEntryAt(IPoint point) {
        Cell square = cellOf(point, this.resolution);

        if (!this.index.containsKey(square)) {
            Object entry = this.newEntry.get();
            this.index.put(square, entry);
            return entry;
        }

        return this.index.get(square);
    }

    public Object getEntryAt(IPoint point) {
        Cell square = cellOf(point, this.resolution);

        return this.index.containsKey(square) ?
                this.index.get(square) :
                this.newEntry.get();
    }

    public List<Object> range(IPoint topLeft, BasicPoint bottomRight) {
        Cell topLeftIndex = cellOf(topLeft, this.resolution);
        Cell bottomRightIndex = cellOf(bottomRight, this.resolution);

        return get(bottomRightIndex.getX(), topLeftIndex.getX(), topLeftIndex.getY(), bottomRightIndex.getY());
    }

    public List<Object> get(int minX, int maxX, int minY, int maxY) {
        ArrayList<Object> entries = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                Cell square = new Cell(x, y);
                if (index.containsKey(square)) {
                    entries.add(index.get(square));
                }
            }
        }

        return entries;
    }

    private List<Cell> getCells(int minX, int maxX, int minY, int maxY) {
        ArrayList<Cell> indices = new ArrayList<>();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                indices.add(new Cell(x, y));
            }
        }

        return indices;
    }

    public double getResolution() {
        return resolution;
    }

    public Map<Cell, Object> getIndex() {
        return index;
    }

    public Supplier getNewEntry() {
        return newEntry;
    }

}
