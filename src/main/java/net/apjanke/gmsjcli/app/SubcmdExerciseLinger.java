package net.apjanke.gmsjcli.app;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;

import com.google.maps.model.LatLng;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;


/**
 * Exercise repeated use of GeoApiContext objects.
 *
 * Usage:
 * <pre>
 *     gmsj-cli exercise-linger [-n &lt;num-instances>] [-p &lt;points-per-instance] [-d|--dirty] [-W|--no-wait]
 *
 *     Arguments:
 *       * -n, --num-instances &lt;nInstances
 *         Number of GeoApiContext instances to create and exercise
 *         Default: 100
 *       * -p, --points-per-instance &lt;nPoints>
 *         Number of points to query in each GeoApiContext instance
 *         Default: 100
 *       * -d, --dirty
 *         Dirty close: do not call shutdown() on GeoApiContext instances
 *       * -W, --no-waith
 *         Do not wait for user input at end of process
 *
 * </pre>
 *
 * This is for testing the thread-persistence issue reported in
 * https://github.com/googlemaps/google-maps-services-java/issues/261.
 *
 * This subcommand creates multiple instances of GeoApiContext, and peforms multiple
 * queries on them. The instances and queries are done serially. This is to test whether
 * and how worker threads linger after the end of the use of GeoApiContext objects.
 */
public class SubcmdExerciseLinger extends GenericApiSubcmd {

    PrintStream out = System.out;

    @Override
    public int run(String[] args) {
        super.run(args);
        getReady();

        MyOptions options = new MyOptions();
        Options cmdLineOptions = new Options();
        cmdLineOptions.addOption("p", "points-per-instance", true,
                "number of points to query per GeoApiContext instance");
        cmdLineOptions.addOption("n", "num-instances", true,
                "number of GeoApiContext instances to create");
        cmdLineOptions.addOption("d", "dirty", false,
                "do \"dirty\" closes, without calling shutdown() on GeoApiContext");
        cmdLineOptions.addOption("W", "no-wait", false,
                "do not wait for user input at end of execution");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(cmdLineOptions, args);
        } catch (ParseException e) {
            darnit("Error: Could not parse command line: " + e.getMessage());
        }
        if (cmdLine.hasOption("n")) {
            String nInstancesArg = cmdLine.getOptionValue("n");
            options.nInstances = Integer.parseInt(nInstancesArg);
        }
        if (cmdLine.hasOption("p")) {
            String nPointsPerInstanceArg = cmdLine.getOptionValue("p");
            options.nPointsPerInstance = Integer.parseInt(nPointsPerInstanceArg);
        }
        if (cmdLine.hasOption("d")) {
            options.dirty = true;
        }
        if (cmdLine.hasOption("W")) {
            options.wait = false;
        }
        if (cmdLine.getArgList().size() > 0) {
            darnit("Unrecognized arguments on command line.");
        }

        exerciseLinger(options);

        if (options.wait) {
            try {
                System.out.print("Processing complete. Press enter to finish...");
                int ignoredInput = System.in.read();
            } catch (IOException ioe) {
                // quash
            }
        }

        return 0;
    }

    private void exerciseLinger(MyOptions options) {
        for (int iInstance = 0; iInstance < options.nInstances; iInstance++) {
            out.println();
            out.println("Exercising GeoApiContext instance #" + (iInstance + 1));
            // Create a GeoApiContext
            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();
            // Run queries on it
            runFuzzQueries(geoApiContext, options.nPointsPerInstance);
            // Shut it down (or not)
            if (!options.dirty) {
                geoApiContext.shutdown();
            }
        }
    }

    private void runFuzzQueries(GeoApiContext geoApiContext, int nPoints) {
        // Fuzz around that point
        Random rnd = new Random();
        rnd.setSeed(42);
        double radius = 0.1;

        // Start all queries in New York. It's the center of the universe, anyway.
        GeocodingResult[] initResults = geocodeSafe(geoApiContext, "New York, NY");
        GeocodingResult startResult = initResults[0];
        LatLng startPoint = startResult.geometry.location;

        for (int iPoint = 0; iPoint < nPoints; iPoint++) {
            double fuzzLat = startPoint.lat + ((rnd.nextDouble() - 0.5) * radius);
            double fuzzLng = startPoint.lng + ((rnd.nextDouble() - 0.5) * radius);
            LatLng fuzzPoint = new LatLng(fuzzLat, fuzzLng);
            //System.out.println("Geocoding point 1: " + fuzzPoint);
            GeocodingResult[] fuzzResults;
            GeocodeResultsDisplayer displayer = new GeocodeResultsDisplayer();
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
            out.print(".");
            out.flush();
            // And do nothing with the results; we're just trying to make threaded workers do work.
        }
    }

    private static class MyOptions {
        int nPointsPerInstance = 100;
        int nInstances = 100;
        boolean dirty = false;
        boolean wait = true;
    }
}


