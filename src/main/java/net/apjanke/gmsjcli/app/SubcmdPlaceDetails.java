package net.apjanke.gmsjcli.app;

import com.google.maps.FindPlaceFromTextRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.FindPlaceFromText;
import com.google.maps.model.PlaceDetails;
import org.apache.commons.cli.*;

import java.io.IOException;

/**
 * Look up place details.
 *
 * Usage:
 * <pre>
 *     gmsj-cli placedetails [-i &lt;placeId>] [-p &lt;phone>] [&lt;address>]
 * </pre>
 *
 * Only one of &lt;placeId>, &lt;phone>, or &lt;address> may be specified.
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
        cmdLineOptions.addOption("p", "phone", true, "phone number");
        cmdLineOptions.addOption("i", "placeid", true, "placeId");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(cmdLineOptions, args);
        } catch (ParseException e) {
            System.err.println("Error: Could not parse command line: " + e.getMessage());
            System.exit(1);
        }
        String placeId = null;
        String phoneNumber = null;
        String address = null;
        if (cmdLine.hasOption("p")) {
            phoneNumber = cmdLine.getOptionValue('p');
        }
        if (cmdLine.hasOption("i")) {
            placeId = cmdLine.getOptionValue('i');
        }
        if (cmdLine.getArgList().size() > 0) {
            address = cmdLine.getArgList().get(0);
        }
        if (cmdLine.getArgList().size() > 1) {
            darnit(String.format("Too many arguments (max=1, got=%d)",
                    cmdLine.getArgList().size()));
        }

        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();

        if (placeId == null) {
            if (phoneNumber != null) {
                placeId = lookupPlace(geoApiContext, phoneNumber,
                        FindPlaceFromTextRequest.InputType.PHONE_NUMBER);
            } else if (address != null) {
                placeId = lookupPlace(geoApiContext, address,
                        FindPlaceFromTextRequest.InputType.TEXT_QUERY);
            } else {
                darnit("One of <address>, <phone>, or <placeId> must be specified.");
            }
        }

        // Run Places API query
        PlaceDetailsRequest req = new PlaceDetailsRequest(geoApiContext)
            .placeId(placeId);
        PlaceDetails details = null;
        try {
            details = req.await();
            System.out.println(details.toString());
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

    private static String lookupPlace(GeoApiContext geoApiContext, String queryText,
                                      FindPlaceFromTextRequest.InputType inputType) {
        FindPlaceFromTextRequest req = PlacesApi.findPlaceFromText(geoApiContext, queryText, inputType);
        try {
            FindPlaceFromText rslt = req.await();
            if (rslt.candidates.length == 0) {
                darnit("Error: No matches for search.");
            }
            return rslt.candidates[0].placeId;
        } catch (ApiException | IOException e) {
            System.err.println("Error during request: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            // abort
            System.exit(1);
        }
        return null;
    }
}
