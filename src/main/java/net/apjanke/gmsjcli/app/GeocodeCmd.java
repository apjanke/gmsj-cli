package net.apjanke.gmsjcli.app;

import com.google.maps.GeoApiContext;
import com.google.maps.model.GeocodingResult;
import org.apache.commons.cli.*;

/**
 * Geocode a given address.
 *
 * Usage:
 * <pre>
 *     java net.apjanke.gmsjcli.app.MainApp geocode [-f|--format &lt;format>] &lt;address>
 *
 *     Arguments:
 *       * -f, --format &lt;format>
 *         Valid arguments:
 *           regular, concise, gson, terse, silent; default = regular
 *       * <address>
 *           Any valid Google Maps search string
 * </pre>
 *
 * The gmsj-cli command wraps this up for easy invocation.
 *
 * Examples:
 * <pre>
 *     gmsj-cli geocode "Bar Great Harry, Brooklyn, NY"
 *     gmsj-cli geocode -f concise "Lahore, Pakistan"
 *     gmsj-cli geocode "424a 3rd Ave, Brooklyn, NY 11215"
 * </pre>
 */
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
