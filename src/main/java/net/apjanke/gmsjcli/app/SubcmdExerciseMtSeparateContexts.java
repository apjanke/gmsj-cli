package net.apjanke.gmsjcli.app;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

/**
 * Exercise multi-threaded client queries on separate GeoApiContexts.
 *
 * NOTE: This class exercises a BAD programming pattern. You should not make your own code do this!
 *
 * NOTE: Running this class will chew up all your Google Maps API queries. Don't use it unless you
 * want to spend a lot of queries on testing!
 *
 * Usage:
 * <pre>
 *     gmsj-cli exercise-mt-separate-contexts [-n &lt;num-instances>] [-p &lt;points-per-instance] [-D|--delayBetweenLaunch &lt;delayBetweenLaunch>] [-d|--dirty] [-W|--no-wait]
 *
 *     Arguments:
 *       * -n, --num-instances &lt;nInstances
 *         Number of GeoApiContext instances to create in separate worker threads
 *         Default: 100
 *       * -p, --points-per-instance &lt;nPoints>
 *         Number of points to query in each GeoApiContext instance
 *         Default: 1000
 *       * -D, --delay-between-launch
 *         Delay between launching each worker thread, in milliseconds
 *         Default: 100
 *       * -b, --delay-between-queries
 *         Delay between individual queries in the worker threads.
 *         Default: 15
 *       * -d, --dirty
 *         Dirty close: do not call shutdown() on GeoApiContext instances
 *       * -W, --no-waith
 *         Do not wait for user input at end of process
 * </pre>
 */
public class SubcmdExerciseMtSeparateContexts extends GenericApiSubcmd {
    private static final Logger log = LoggerFactory.getLogger(SubcmdExerciseMtSeparateContexts.class);

    private final Outputter out = new Outputter();

    @Override
    public int run(String[] args) {
        super.run(args);
        getReady();

        MyOptions options = new MyOptions();
        Options cmdLineOptions = new Options();
        cmdLineOptions.addOption("n", "num-instances", true,
                "number of instances to create in separate threads");
        cmdLineOptions.addOption("p", "points-per-instance", true,
                "queries per worker");
        cmdLineOptions.addOption("D", "delay-between-launch", true,
                "delay between launching worker threads, in milliseconds");
        cmdLineOptions.addOption("b", "delay-between-queries", true,
                "delay between queries in each instance");
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
            options.nWorkers = Integer.parseInt(nInstancesArg);
        }
        if (cmdLine.hasOption("D")) {
            String delayArg = cmdLine.getOptionValue("D");
            options.delayBetweenLaunch = Integer.parseInt(delayArg);
        }
        if (cmdLine.hasOption("b")) {
            String delayArg = cmdLine.getOptionValue("b");
            options.delayBetweenQueries = Integer.parseInt(delayArg);
        }
        if (cmdLine.hasOption("p")) {
            String nQueriesArg = cmdLine.getOptionValue("p");
            options.nQueriesPerWorker = Integer.parseInt(nQueriesArg);
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

        exerciseMtSeparateContexts(options);

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

    private void exerciseMtSeparateContexts(MyOptions options) {
        for (int iWorker = 1; iWorker <= options.nWorkers; iWorker++) {
            Thread thread = new Thread(new Worker(options, iWorker), "gmsj-cli MtSc Worker-"+iWorker);
            thread.start();
            if (options.delayBetweenLaunch > 0) {
                try {
                    Thread.sleep(options.delayBetweenLaunch);
                } catch (InterruptedException e) {
                    System.err.println("Sleeping between workers was interrupted! Continuing with next worker...");
                }
            }
        }
    }

    private class Worker implements Runnable {

        final MyOptions options;
        final int instanceNumber;

        Worker(MyOptions options, int instanceNumber) {
            this.options = options;
            this.instanceNumber = instanceNumber;
        }

        @Override
        public void run() {
            // Create a GeoApiContext
            log.debug("Launching worker #{}", instanceNumber);
            out.println("[launch #" + instanceNumber +"]");
            GeoApiContext geoApiContext = new GeoApiContext.Builder()
                    .apiKey(apiKey)
                    .build();
            // Run queries on it
            runFuzzQueries(geoApiContext, options.nQueriesPerWorker);
            // Shut it down (or not)
            if (!options.dirty) {
                geoApiContext.shutdown();
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
                //out.print(".");
                //out.flush();
                // And do nothing with the results; we're just trying to make threaded workers do work.
                try {
                    Thread.sleep(options.delayBetweenQueries);
                } catch (InterruptedException e) {
                    // quash
                }
            }
        }
    }

    private class Outputter {
        synchronized void println(String str) {
            System.out.println(str);
        }
        synchronized void print(String str) {
            System.out.print(str);
        }
        synchronized void flush() {
            System.out.flush();
        }
    }

    private static class MyOptions {
        int nWorkers = 100;
        int nQueriesPerWorker = 100;
        int delayBetweenLaunch = 1000;
        int delayBetweenQueries = 15;
        boolean dirty = false;
        boolean wait = true;
    }
}
