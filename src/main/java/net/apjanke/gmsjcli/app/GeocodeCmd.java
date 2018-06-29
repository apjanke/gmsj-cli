package net.apjanke.gmsjcli.app;

import com.google.maps.GeoApiContext;
import com.google.maps.model.GeocodingResult;
import org.apache.commons.cli.*;

public class GeocodeCmd extends GenericApiCmd {
    @Override
    public int run(String[] args) {
        super.run(args);
        getReady();
        
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
            outputFormat = parseFormatCmdLineOption(cmdLine, "f");
        }
        if (cmdLine.getArgList().size() == 0) {
            darnit("Must supply a geocoding address input.");
        }
        String geocodeInput = cmdLine.getArgList().get(0);

        // Run geocoding API request
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
        GeocodingResult[] results = geocodeSafe(geoApiContext, geocodeInput);

        // Display output
        GeocodeResultsDisplayer displayer = new GeocodeResultsDisplayer();
        displayer.displayOutput(outputFormat, results);

        // Okay, things went as expected
        return 0;
    }

}
