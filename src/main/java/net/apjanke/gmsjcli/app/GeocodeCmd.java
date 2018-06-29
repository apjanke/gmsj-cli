package net.apjanke.gmsjcli.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintStream;

public class GeocodeCmd extends GenericApiCmd {
    @Override
    public int run(String[] args) {
        super.run(args);
        getReady();
        
        // Parse command line inputs - general
        sanityCheckCmdLineArgs(args);
        String geocodeInput = args[0];

        // Parse command line inputs - subcommand-specific
        GeocodeOutputFormat outputFormat = GeocodeOutputFormat.CONCISE;
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
                outputFormat = GeocodeOutputFormat.CONCISE;
            } else if ("gson".equals(outputFormatArg)) {
                outputFormat = GeocodeOutputFormat.GSON;
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
        switch (outputFormat) {
            case CONCISE:
                displayOutputConcise(results);
                break;
            case GSON:
                displayOutputGson(results);
                break;
            case SILENT:
                // NOP
                break;
            default:
                darnit("Internal error: Invalid outputFormat: " + outputFormat);
        }

        // Okay, things went as expected
        return 0;
    }

    private void displayOutputGson(GeocodingResult[] results) {
        PrintStream out = System.out;
        out.println("Results length: " + results.length);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (int i = 0; i < results.length; i++) {
            out.println("Result " + i + ":");
            out.println(gson.toJson(results[i]));
        }
    }

    private void displayOutputConcise(GeocodingResult[] results) {
        PrintStream out = System.out;
        out.println("Results length: " + results.length);
        for (int i = 0; i < results.length; i++) {
            GeocodingResult rslt = results[i];
            out.println("Result " + i + ":");
            out.println("Formatted Address: " + rslt.formattedAddress);
            out.println("Types: " + slashJoin(rslt.types));
            if (rslt.partialMatch) {
                out.println("PARTIAL MATCH");
            }
            out.println("PlaceId: " + rslt.placeId);
            Geometry geom = rslt.geometry;
            out.format("Geom: (%.6f, %.6f) %s: [(%.3f,%.3f), (%.3f,%.3f)]\n",
                    geom.location.lat, geom.location.lng, geom.locationType,
                    geom.viewport.northeast.lat, geom.viewport.northeast.lng,
                    geom.viewport.southwest.lat, geom.viewport.southwest.lng);
            out.println("Address:");
            for (int iAddr = 1; iAddr < rslt.addressComponents.length; iAddr++) {
                AddressComponent ac  = rslt.addressComponents[iAddr];
                String display = null;
                if (ac.shortName.equals(ac.longName)) {
                    display = ac.shortName;
                } else {
                    display = String.format("%s (\"%s\")", ac.shortName, ac.longName);
                }
                out.format("  %s [%s]\n", display, slashJoin(ac.types));
            }
        }
    }

   private static String slashJoin(Object[] things) {
        if (things.length == 0) {
            return "";
        }
        String str = things[0].toString();
        for (int i = 1; i < things.length; i++) {
            str = str + " / " + things[i].toString();
        }
        return str;
   }

    private void sanityCheckCmdLineArgs(String[] args) {
        // Haha, hack
        if (args.length == 0) {
            darnit("Geocoding argument is required.");
        }
    }
}
