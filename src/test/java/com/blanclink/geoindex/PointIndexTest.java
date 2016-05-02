package com.blanclink.geoindex;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class PointIndexTest extends BaseTest {

    @Test
    public void testRange() throws Exception {
        PointIndex index = new PointIndex(1000);

        locations.forEach(index::add);

        List<Point> within = index.range(oxford, embankment);
        List<Point> expected = Arrays.asList(picadilly, charring, coventGarden, embankment, leicester, oxford);

        assertEquals(sort(expected), sort(within));
    }

    @Test
    public void testKNearest() {
        PointIndex index = new PointIndex(500);

        locations.forEach(index::add);

        Predicate<Point> all = point -> true;

        assertEquals(Arrays.asList(charring, embankment, leicester), index.kNearest(charring, 3, 1000, all));
        assertEquals(Arrays.asList(charring, embankment, leicester, coventGarden, picadilly), index.kNearest(charring, 5, 20000, all));

        Predicate<Point> noPicadilly = point -> !point.getId().contains("Piccadilly");

        assertEquals(Arrays.asList(charring, embankment, leicester, coventGarden, westminster), index.kNearest(charring, 5, 20000, noPicadilly));
        assertEquals(Arrays.asList(charring, embankment, leicester, coventGarden, picadilly), index.kNearest(charring, 5, 20000, all));
        assertEquals(9, index.kNearest(charring, 100, 1000, all).size());
    }

    private List<Point> sort(List<Point> pointList) {
        pointList.sort((Point o1, Point o2) -> o1.getId().compareTo(o2.getId()));
        return pointList;
    }

}