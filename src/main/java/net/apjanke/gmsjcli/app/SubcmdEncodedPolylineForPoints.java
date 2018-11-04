package net.apjanke.gmsjcli.app;

import com.google.maps.GeoApiContext;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.apache.commons.cli.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Look up points and encode the path between them as an EncodedPolyline.
 *
 * Usage:
 * <pre>
 *     gmsj-cli encoded-poly-path [-l|--latlng] &lt;point1> [&lt;point2> ...]
 *
 *     Arguments:
 *       * -l, --latlng
 *         Interpret the arguments as exact Lat/Lng points, instead of locations
 *         to geocode.
 * </pre>
 *
 */
public class SubcmdEncodedPolylineForPoints extends GenericApiSubcmd {

    @Override
    public int run(String[] args) {
        super.run(args);
        getReady();

        // Parse command line inputs - subcommand-specific
        Options cmdLineOptions = new Options();
        cmdLineOptions.addOption("l", "latlng", false, "interpret points as exact lat/long coordinates");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(cmdLineOptions, args);
        } catch (ParseException e) {
            System.err.println("Error: Could not parse command line: " + e.getMessage());
            System.exit(1);
        }
        List<String> pointArgs = new ArrayList<>(cmdLine.getArgList());

        List<LatLng> points = new ArrayList<>();
        PrintStream out = System.out;

        if (cmdLine.hasOption("l")) {
            // Parse each point as a lat/long coordinate
            for (int i = 0; i < pointArgs.size(); i++) {
                LatLng location = parseLatLng(pointArgs.get(i));
                points.add(location);
                out.println("Point " + i + ":");
                out.format("LatnLng: (%.6f, %.6f)\n", location.lat, location.lng);
            }
        } else {
            // Geocode arguments to turn them in to LatLng points
            GeoApiContext geoApiContext = new GeoApiContext.Builder().apiKey(apiKey).build();
            for (int i = 0; i < pointArgs.size(); i++) {
                String arg = pointArgs.get(i);
                GeocodingResult[] results = geocodeSafe(geoApiContext, arg);
                if (results == null || results.length == 0) {
                  throw new RuntimeException("No geocoding results for: " + arg);
                }
                // Just assume first result is best/most precise
                GeocodingResult result = results[0];
                LatLng location = result.geometry.location;
                points.add(location);
                out.println("Result " + i + ":");
                out.println("Formatted Address: " + result.formattedAddress);
                out.format("LatnLng: (%.6f, %.6f)\n", location.lat, location.lng);
            }
        }

        // Convert the geocoded points to an EncodedPolyline
        EncodedPolyline encodedPolyline = new EncodedPolyline(points);

        // Display encoded polyline
        out.println("Encoded polyline:");
        out.println(encodedPolyline.getEncodedPath());

        // Okay, things went as expected
        return 0;
    }

    private static LatLng parseLatLng(String str) {
        String parts[] = str.split(", *");
        if (parts.length != 2) {
            throw new RuntimeException("Error parsing lat/lng point: " + str);
        }
        Double lat = Double.parseDouble(parts[0]);
        Double lng = Double.parseDouble(parts[1]);
        return new LatLng(lat, lng);
    }
}
