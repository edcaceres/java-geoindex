package com.blanclink.geoindex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.blanclink.geoindex.Cell.cellOf;

public class GeoIndex<T> {

    private final double resolution;
    private final Map<Cell, T> index;
    private final Supplier<T> newEntry;

    public GeoIndex(double resolution, Map<Cell, T> index, Supplier<T> newEntry) {
        this.resolution = resolution;
        this.index = index;
        this.newEntry = newEntry;
    }

    public GeoIndex(double resolution, Supplier<T> newEntry) {
        this.resolution = resolution;
        this.index = new HashMap<>();
        this.newEntry = newEntry;
    }

    public GeoIndex clone() {
        GeoIndex clone = new GeoIndex<>(this.resolution, this.index, this.newEntry);
        clone.index.putAll(this.index);
        return clone;
    }

    public T addEntryAt(IPoint point) {
        Cell square = cellOf(point, this.resolution);

        if (!this.index.containsKey(square)) {
            T entry = this.newEntry.get();
            this.index.put(square, entry);
            return entry;
        }

        return this.index.get(square);
    }

    public T getEntryAt(IPoint point) {
        Cell square = cellOf(point, this.resolution);

        return this.index.containsKey(square) ?
                this.index.get(square) :
                this.newEntry.get();
    }

    public List<T> range(IPoint topLeft, IPoint bottomRight) {
        Cell topLeftIndex = cellOf(topLeft, this.resolution);
        Cell bottomRightIndex = cellOf(bottomRight, this.resolution);

        return get(bottomRightIndex.getX(), topLeftIndex.getX(), topLeftIndex.getY(), bottomRightIndex.getY());
    }

    public List<T> get(int minX, int maxX, int minY, int maxY) {
        ArrayList<T> entries = new ArrayList<>();

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

    public Map<Cell, T> getIndex() {
        return index;
    }

    public Supplier getNewEntry() {
        return newEntry;
    }

}
