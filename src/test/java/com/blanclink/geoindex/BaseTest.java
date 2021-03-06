package com.blanclink.geoindex;

import org.junit.Before;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class BaseTest {

    protected static final BasicPoint waterloo     = new BasicPoint("Waterloo", 51.502973, -0.114723);
    protected static final BasicPoint kingsCross   = new BasicPoint("Kings Cross", 51.529999, -0.124481);
    protected static final BasicPoint leicester    = new BasicPoint("Leicester Square", 51.511291, -0.128242);
    protected static final BasicPoint coventGarden = new BasicPoint("Covent Garden", 51.51276, -0.124507);
    protected static final BasicPoint totenham     = new BasicPoint("Tottenham Court Road", 51.516206, -0.13087);
    protected static final BasicPoint picadilly    = new BasicPoint("Piccadilly Circus", 51.50986, -0.1337);
    protected static final BasicPoint charring     = new BasicPoint("Charing Cross", 51.508359, -0.124803);
    protected static final BasicPoint embankment   = new BasicPoint("Embankment", 51.507312, -0.122367);
    protected static final BasicPoint oxford       = new BasicPoint("Oxford Circus", 51.51511, -0.1417);
    protected static final BasicPoint westminster  = new BasicPoint("Westminster", 51.501402, -0.125002);
    protected static final BasicPoint regentsPark  = new BasicPoint("Regents Park", 51.52347, -0.1468);
    protected static final BasicPoint londonBridge = new BasicPoint("London Bridge", 51.504674, -0.086006);
    protected static final BasicPoint brentCross   = new BasicPoint("Brent Cross", 51.576599, -0.213336);
    protected static final BasicPoint lewisham     = new BasicPoint("Lewisham", 51.46532, -0.0134);
    protected static final BasicPoint swanley      = new BasicPoint("Swanley", 51.392994, 0.168716);
    protected static final BasicPoint watford      = new BasicPoint("Watford", 51.65747, -0.41726);
    protected static final BasicPoint aylesbury    = new BasicPoint("Aylesbury", 51.808615, -0.772219);
    protected static final BasicPoint aylesford    = new BasicPoint("Aylesford", 51.28597, 0.507689);

    protected static final BasicPoint reykjavik = new BasicPoint("Reykjavik", 64.15, -21.95);
    protected static final BasicPoint ankara    = new BasicPoint("Ankara", 39.93, 32.86);

    protected static List<BasicPoint> locations = new ArrayList<>();

    @Before
    public void setUpClass() throws IOException {
        String line;

        BufferedReader br = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("tube.csv")));
        while ((line = br.readLine()) != null) {
            String[] columns = line.split(",");
            locations.add(parseLine(columns));
        }
    }

    private BasicPoint parseLine(String[] strings) {

        NumberFormat nf = NumberFormat.getInstance(Locale.UK);

        double lat, lon;
        try {
            lat = nf.parse(strings[1]).doubleValue();
            lon = nf.parse(strings[2]).doubleValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new BasicPoint(strings[0], lat, lon);
    }

}