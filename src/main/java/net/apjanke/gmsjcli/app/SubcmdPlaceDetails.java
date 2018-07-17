package net.apjanke.gmsjcli.app;

import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlaceDetails;
import org.apache.commons.cli.*;

import java.io.IOException;

/**
 * Look up place details.
 *
 * Usage:
 * <pre>
 *     gmsj-cli placedetails &lt;placeId>
 * </pre>
 *
 * Examples:
 * <pre>
 *     gmsj-cli placedetails ChIJN1t_tDeuEmsRUsoyG83frY4
 * </pre>
 */
public class SubcmdPlaceDetails extends GenericApiSubcmd {

    @Override
    public int run(String[] args) {
        super.run(args);
        getReady();

        // Parse command line inputs - subcommand-specific
        Options cmdLineOptions = new Options();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(cmdLineOptions, args);
        } catch (ParseException e) {
            System.err.println("Error: Could not parse command line: " + e.getMessage());
            System.exit(1);
        }
        if (cmdLine.getArgList().size() == 0) {
            darnit("Must supply an address input.");
        }
        if (cmdLine.getArgList().size() > 1) {
            darnit(String.format("Too many arguments (max=1, got=%d)",
                    cmdLine.getArgList().size()));
        }
        String placeId = cmdLine.getArgList().get(0);

        // Run Places API query
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
        PlaceDetailsRequest req = new PlaceDetailsRequest(geoApiContext)
            .placeId(placeId);
        PlaceDetails details = null;
        try {
            details = req.await();
            System.out.println(details.toString());
        } catch (ApiException | IOException e) {
            System.err.println("Error during request: " + e.getMessage());
            e.printStackTrace();
            return 1;
        } catch (InterruptedException e) {
            // ignore
        }

        return 0;
    }
}
