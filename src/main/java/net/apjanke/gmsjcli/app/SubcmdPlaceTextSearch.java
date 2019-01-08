package net.apjanke.gmsjcli.app;

import com.google.maps.*;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import org.apache.commons.cli.*;

import java.io.IOException;


/**
 * Do a Text Search for a place.
 *
 * Usage:
 * <pre>
 *         gmsj-cli placetextsearch [-g|--region &lt;region>] [-l|--location &lt;location>]
 *           [-r|--radius &lt;radius>] [-n|--language &lt;language>] [-o|--open-now]
 *           [-t|--type &lt;type>] &lt;query>
 * </pre>
 */
public class SubcmdPlaceTextSearch extends GenericApiSubcmd {

    @Override
    public int run(String[] args) {
        super.run(args);
        getReady();

        // Parse command line inputs - subcommand-specific
        Options cmdLineOptions = new Options();
        cmdLineOptions.addOption("g", "region", true, "region");
        cmdLineOptions.addOption("l", "location", true, "location");
        cmdLineOptions.addOption("r", "radius", true, "radius");
        cmdLineOptions.addOption("n", "language", true, "language");
        cmdLineOptions.addOption("o", "open-now", false, "open now");
        cmdLineOptions.addOption("t", "type", true, "type");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(cmdLineOptions, args);
        } catch (ParseException e) {
            System.err.println("Error: Could not parse command line: " + e.getMessage());
            System.exit(1);
        }
        String region = null;
        String location = null;
        Integer radius = null;
        String language = null;
        Boolean openNow = null;
        String type = null;
        String query = null;
        if (cmdLine.hasOption('g')) {
            region = cmdLine.getOptionValue('g');
        }
        if (cmdLine.hasOption('l')) {
            location = cmdLine.getOptionValue('l');
        }
        if (cmdLine.hasOption('r')) {
            radius = Integer.parseInt(cmdLine.getOptionValue('r'));
        }
        if (cmdLine.hasOption('n')) {
            language = cmdLine.getOptionValue('n');
        }
        if (cmdLine.hasOption('o')) {
            openNow = true;
        }
        if (cmdLine.hasOption('t')) {
            type = cmdLine.getOptionValue('t');
        }
        if (cmdLine.getArgList().size() == 0) {
            darnit("Query argument is required");
        }
        if (cmdLine.getArgList().size() > 1) {
            darnit(String.format("Too many arguments (max=1, got=%d)",
                    cmdLine.getArgList().size()));
        }
        query = cmdLine.getArgList().get(0);

        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();

        TextSearchRequest req = new TextSearchRequest(geoApiContext)
                .query(query);
        if (region != null) {
            req.region(region);
        }
        if (location != null) {
            // TODO: Parse location
            //req.location()
        }
        if (radius != null) {
            req.radius(radius);
        }
        if (language != null) {
            req.language(language);
        }
        if (openNow != null) {
            req.openNow(openNow);
        }
        if (type != null) {
            req.type(PlaceType.valueOf(type));
        }

        PlacesSearchResponse response = null;
        try {
            response = req.await();
            System.out.println(response.toString());
            printPlacesSearchResponse(response);
        } catch (ApiException | IOException e) {
            System.err.println("Error during request: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            // abort
            System.exit(1);
        }

        return 0;
    }

    private static void printPlacesSearchResponse(PlacesSearchResponse response) {
        PlacesSearchResult[] results = response.results;
        for (PlacesSearchResult r: results) {
            System.out.println(r.toString());
        }
    }
}
