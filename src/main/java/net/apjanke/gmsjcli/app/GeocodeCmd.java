package net.apjanke.gmsjcli.app;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import org.apache.commons.cli.*;

public class GeocodeCmd extends GenericApiCmd {
    @Override
    public int run(String[] args) {
        super.run(args);
        getReady();
        
        // Parse command line inputs - general
        sanityCheckCmdLineArgs(args);
        String geocodeInput = args[0];

        // Parse command line inputs - subcommand-specific
        GeocodeOutputFormat outputFormat = GeocodeOutputFormat.REGULAR;
        Options options = new Options();
        options.addOption("f", "format", true, "format for output");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println("Error: Could not parse command line: " + e.getMessage());
            System.exit(1);
        }
        if (cmdLine.hasOption("f")) {
            String outputFormatArg = cmdLine.getOptionValue('f');
            if ("concise".equals(outputFormatArg)) {
                outputFormat = GeocodeOutputFormat.REGULAR;
            } else if ("gson".equals(outputFormatArg)) {
                outputFormat = GeocodeOutputFormat.GSON;
            } else if ("terse".equals(outputFormatArg)) {
                outputFormat = GeocodeOutputFormat.TERSE;
            } else {
                darnit("Invalid output format: " + outputFormatArg);
            }
        }

        // Run geocoding API request
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

        // Display output
        GeocodeResultsDisplayer displayer = new GeocodeResultsDisplayer();
        displayer.displayOutput(outputFormat, results);

        // Okay, things went as expected
        return 0;
    }

    private void sanityCheckCmdLineArgs(String[] args) {
        // Haha, hack
        if (args.length == 0) {
            darnit("Geocoding argument is required.");
        }
    }
}
