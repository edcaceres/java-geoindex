package com.blanclink.geoindex;

import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PointIndexTest extends BaseTest {

    private Predicate<BasicPoint> all = point -> true;

    @Test
    public void testRange() throws Exception {
        PointIndex<BasicPoint> index = new PointIndex<>(1000);

        locations.forEach(index::add);

        List<BasicPoint> within = index.range(oxford, embankment);
        List<BasicPoint> expected = Arrays.asList(picadilly, charring, coventGarden, embankment, leicester, oxford);

        assertEquals(sort(expected), sort(within));
    }

    @Test
    public void testKNearest() {
        PointIndex<BasicPoint> index = new PointIndex<>(500);

        locations.forEach(index::add);

        assertEquals(Arrays.asList(charring, embankment, leicester), index.kNearest(charring, 3, 1000, all));
        assertEquals(Arrays.asList(charring, embankment, leicester, coventGarden, picadilly), index.kNearest(charring, 5, 20000, all));

        Predicate<BasicPoint> noPicadilly = point -> !point.getId().contains("Piccadilly");

        assertEquals(Arrays.asList(charring, embankment, leicester, coventGarden, westminster), index.kNearest(charring, 5, 20000, noPicadilly));
        assertEquals(Arrays.asList(charring, embankment, leicester, coventGarden, picadilly), index.kNearest(charring, 5, 20000, all));
        assertEquals(9, index.kNearest(charring, 100, 1000, all).size());
    }

    @Test
    public void testExpiringIndex() throws InterruptedException {
        PointIndex<BasicPoint> index = new PointIndex<>(1000, Duration.ofMillis(5000));

        index.add(picadilly);

        Thread.sleep(1000);
        index.add(charring);

        Thread.sleep(1000);
        index.add(embankment);

        Thread.sleep(1000);
        index.add(coventGarden);

        Thread.sleep(1000);
        index.add(leicester);

        assertEquals(sort(index.range(oxford, embankment)),
                sort(Arrays.asList(picadilly, charring, embankment, coventGarden, leicester)));
        assertEquals(index.kNearest(charring, 3, 5000, all), Arrays.asList(charring, embankment, leicester));

        assertNotNull(index.get(picadilly.getId()));
        assertNotNull(index.get(charring.getId()));

        Thread.sleep(2000);

        assertEquals(null, index.get(picadilly.getId()));
        assertEquals(null, index.get(charring.getId()));

        assertNotNull(index.get(embankment.getId()));
        assertNotNull(index.get(coventGarden.getId()));
        assertNotNull(index.get(leicester.getId()));

        assertEquals(sort(index.range(oxford, embankment)), sort(Arrays.asList(embankment, coventGarden, leicester)));
        assertEquals(sort(index.kNearest(charring, 3, 5000, all)), sort(Arrays.asList(embankment, leicester, coventGarden)));
    }

    private List<? extends IPoint> sort(List<? extends IPoint> pointList) {
        pointList.sort((IPoint o1, IPoint o2) -> o1.getId().compareTo(o2.getId()));
        return pointList;
    }

}