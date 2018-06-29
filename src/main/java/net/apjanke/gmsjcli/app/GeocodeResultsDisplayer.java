package net.apjanke.gmsjcli.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.Geometry;

import java.io.PrintStream;

class GeocodeResultsDisplayer {

    /**
     * Displays geocoding results on System.out in the specified format.
     *
     * If there is an error, it displays an error message to System.err and
     * exits.
     * @param format Format to display results in
     * @param results Results to display
     */
    void displayOutput(GeocodeOutputFormat format, GeocodingResult[] results) {
        switch (format) {
            case REGULAR:
                displayOutputRegular(results);
                break;
            case CONCISE:
                displayOutputConcise(results);
                break;
            case GSON:
                displayOutputGson(results);
                break;
            case TERSE:
                displayOutputTerse(results);
                break;
            case SILENT:
                // NOP
                break;
            default:
                System.err.println("Internal error: Invalid outputFormat: " + format);
                System.exit(1);
        }
    }

    private void displayOutputTerse(GeocodingResult[] results) {
        PrintStream out = System.out;
        if (results.length > 0) {
            GeocodingResult result = results[0];
            out.format("Results %2d long: %s (%s)\n", results.length,
                    result.formattedAddress, result.geometry.location);
        }
    }

    private void displayOutputConcise(GeocodingResult[] results) {
        PrintStream out = System.out;
        out.println("Results length: " + results.length);
        for (int i = 0; i < results.length; i++) {
            GeocodingResult result = results[i];
            out.format("[%d]: %s\n", i, result.formattedAddress);
        }
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

    private void displayOutputRegular(GeocodingResult[] results) {
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
                String display;
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
        StringBuilder sb = new StringBuilder(things[0].toString());
        for (int i = 1; i < things.length; i++) {
            sb.append(" / ").append(things[i].toString());
        }
        return sb.toString();
    }

}
