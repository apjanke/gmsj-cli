/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.apjanke.gmsjcli.app;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import static net.apjanke.gmsjcli.app.Subcommand.darnit;

import com.google.maps.model.LatLng;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.Random;

/**
 * "Fuzz-test" around a given point in space, checking for warnings or other odd results.
 *
 * Usage:
 * <pre>
 *     java net.apjanke.gmsjcli.app.MainApp geofuzz [-f|--format &lt;format>] [-r &lt;radius>] <input>
 *
 *     Arguments:
 *       * -f, --format &lt;format>
 *         Valid arguments:
 *           concise
 *           gson
 *       * -r, --radius &lt;radius>
 *         Search radius size in degrees lat/long
 *       * -n, --npoints &lt;npoints>
 *         Number of points to generate during search
 *       * -S, --rand-seed &lt;seed-value>
 *         Initial value for random seed (for reproducible results)
 *       * <input>
 *           Any valid Google Maps search string
 *
 * </pre>
 *
 * Examples:
 * <pre>
 *     java net.apjanke.gmsjcli.app.MainApp geofuzz -n 3000 -r 0.3 "World Trade Center"
 * </pre>
 *
 * This command is wrapped by the 'bin/gmsj-cli geofuzz' subcommand in the main driver script.
 * Use that.
 */
public class GeoFuzzCmd extends GenericApiCmd {

    private static Random rnd = new Random();

    @Override
    public int run(String[] args) {
        super.run(args);
        getReady();
        
        // Parse command line inputs - general
        sanityCheckCmdLineArgs(args);
        String geocodeInput = args[0];

        // Parse command line inputs - subcommand-specific
        // Note: Ugh, this is horrible -apj
        GeoFuzzOptions fuzzOptions = new GeoFuzzOptions();
        GeocodeOutputFormat outputFormat = GeocodeOutputFormat.SILENT;
        Options cmdLineOptions = new Options();
        cmdLineOptions.addOption("f", "format", true, "format for output");
        cmdLineOptions.addOption("r", "radius", true, "radius size, in degrees lat/lng");
        cmdLineOptions.addOption("n", "npoints", true, "number of points to search through");
        cmdLineOptions.addOption("S", "rand-seed", true, "initial random seed value");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(cmdLineOptions, args);
        } catch (ParseException e) {
            darnit("Error: Could not parse command line: " + e.getMessage());
        }
        if (cmdLine.hasOption("f")) {
            String outputFormatArg = cmdLine.getOptionValue('f');
            if ("concise".equals(outputFormatArg)) {
                outputFormat = GeocodeOutputFormat.CONCISE;
            } else if ("gson".equals(outputFormatArg)) {
                outputFormat = GeocodeOutputFormat.GSON;
            } else {
                darnit("Invalid output format: " + outputFormatArg);
            }
        }
        if (cmdLine.hasOption("r")) {
            String radiusArg = cmdLine.getOptionValue('r');
            try {
                Double radius = Double.parseDouble(radiusArg);
                fuzzOptions.radius = radius;
            } catch (NumberFormatException nfe) {
                darnit("Invalid radius value: " + radiusArg);
            }
        }
        if (cmdLine.hasOption("n")) {
            String nPointsArg = cmdLine.getOptionValue("n");
            int nPoints = Integer.parseInt(nPointsArg);
            fuzzOptions.nPoints = nPoints;
        }
        if (cmdLine.hasOption("S")) {
            String randSeedArg = cmdLine.getOptionValue("S");
            long randSeed = Long.parseLong(randSeedArg);
            fuzzOptions.setSeed = true;
            fuzzOptions.randSeed = randSeed;
        }

        // We only support SILENT format right now, because actual geocoding results are
        // irrelevant.
        switch (outputFormat) {
            case SILENT:
                // NOP
                break;
            default:
                darnit("Unsupported output format: '" + outputFormat + "'. Only SILENT is currently supported.");
                break;
        }

        geoFuzz(fuzzOptions, geocodeInput, outputFormat);
        
        return 0;
    }

    private void geoFuzz(GeoFuzzOptions options, String geocodeInput, GeocodeOutputFormat format) {

        String msg = String.format("Searching %d points around '%s', with radius %.6f degrees",
                options.nPoints, geocodeInput, options.radius);
        if (options.setSeed) {
            msg = msg + String.format(" (rand seed %d)", options.randSeed);
        }
        System.out.println(msg);

        // Find the base location for fuzzing
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
        GeocodingResult[] results = null;
        try {
            results = GeocodingApi.geocode(geoApiContext,
                    geocodeInput).await();
        } catch (Exception e) {
            System.err.format("%s during geocoding.", e.getClass().getName());
            e.printStackTrace(System.err);
            System.exit(1);
        }

        if (results.length < 1) {
            System.err.format("No geocoding results found for '%s'", geocodeInput);
            System.exit(1);
        }

        GeocodingResult startPointRslt = results[0];
        LatLng startPoint = startPointRslt.geometry.location;

        // Fuzz around that point
        for (int iPoint = 0; iPoint < options.nPoints; iPoint++) {
            double fuzzLat = startPoint.lat + ((rnd.nextDouble() - 0.5) * options.radius);
            double fuzzLng = startPoint.lng + ((rnd.nextDouble() - 0.5) * options.radius);
            LatLng fuzzPoint = new LatLng(fuzzLat, fuzzLng);
            //System.out.println("Geocoding point 1: " + fuzzPoint);
            GeocodingResult[] fuzzResults;
            try {
                fuzzResults = GeocodingApi.reverseGeocode(geoApiContext, fuzzPoint)
                        .await();
            } catch (ApiException e) {
                darnit("ApiException during geocoding: " + e.getMessage());
            } catch (InterruptedException e) {
                darnit("Query interrupted: " + e.getMessage());
            } catch (IOException e) {
                darnit("IOException during geocoding: " + e.getMessage());
            }
            // And do nothing with the results for now; the useful info is the warnings
            // kicked out during the geocoding process
        }


    }

    private static class GeoFuzzOptions {
        int nPoints = 50;
        double radius = 0.1;
        long randSeed = 0;
        boolean setSeed = false;
    }

    private void sanityCheckCmdLineArgs(String[] args) {
        // Haha, hack
        if (args.length == 0) {
            darnit("Geocoding argument is required.");
        }
    }
  
}
