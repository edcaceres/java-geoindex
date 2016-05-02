package com.blanclink.geoindex;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class GeoIndexTest extends BaseTest {

    private static GeoIndex index;
    private static int counter = 0;

    @Test
    public void testGeoIndex() throws Exception {
        index = new GeoIndex(100, () -> {
            counter++;
            return new TestEntry(counter, 0);
        });

        for (Point location : locations) {
            Object o = index.addEntryAt(location);
            ((TestEntry) o).add();
        }

        List<Object> entries = index.range(oxford, embankment);

        int count = 0;
        for (Object entry : entries) {
            count += ((TestEntry) entry).getCount();
        }
        Assert.assertEquals("Invalid number of stations", 6, count);
    }

    protected class TestEntry {

        private int entries;
        private int count;

        public TestEntry(int entries, int count) {
            this.entries = entries;
            this.count = count;
        }

        public int getEntries() {
            return entries;
        }

        public int getCount() {
            return count;
        }

        public void add() {
            this.count++;
        }

        public void remove() {
            this.count--;
        }

        @Override
        public String toString() {
            return "TestEntry{" +
                    "entries=" + entries +
                    ", count=" + count +
                    '}';
        }
    }
}