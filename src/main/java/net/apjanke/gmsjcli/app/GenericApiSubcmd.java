package net.apjanke.gmsjcli.app;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import org.apache.commons.cli.CommandLine;

/**
 * Generic class for a subcommand that has to use the Google Maps API, but not more specific than that.
 * @author janke
 */
class GenericApiSubcmd extends Subcommand {
  protected String apiKey;
  
  void getReady() {
        // General API stuff
        apiKey = System.getenv("GOOGLE_MAPS_API_KEY");
        if (apiKey == null) {
            System.err.println("Error: No API key found.");
            System.err.println("Error: The GOOGLE_MAPS_API_KEY environment variable must be set.");
            System.exit(1);
        }
  }

  GeocodeOutputFormat parseFormatCmdLineOption(CommandLine cmdLine, String shortOptionName) {
      GeocodeOutputFormat outputFormat = null;
      if (cmdLine.hasOption(shortOptionName)) {
          String outputFormatArg = cmdLine.getOptionValue('f');
          if ("concise".equals(outputFormatArg)) {
              outputFormat = GeocodeOutputFormat.CONCISE;
          } else if ("regular".equals(outputFormatArg)) {
              outputFormat = GeocodeOutputFormat.REGULAR;
          } else if ("gson".equals(outputFormatArg)) {
              outputFormat = GeocodeOutputFormat.GSON;
          } else if ("terse".equals(outputFormatArg)) {
              outputFormat = GeocodeOutputFormat.TERSE;
          } else {
              darnit("Invalid output format: " + outputFormatArg);
          }
      }
      return outputFormat;
  }

    /**
     * Does a geocoding, panicking and exiting the program if there is an exception.
     * @param geoApiContext GeoApiContext to code against
     * @param geocodeInput String input to geocode
     * @return Results of geocoding the given input
     */
    GeocodingResult[] geocodeSafe(GeoApiContext geoApiContext, String geocodeInput) {
      try {
          return GeocodingApi.geocode(geoApiContext,
                  geocodeInput).await();
      } catch (Exception e) {
          System.err.format("Error: %s during geocoding: %s", e.getClass().getName(), e.getMessage());
          e.printStackTrace(System.err);
          System.exit(1);
      }
      throw new RuntimeException("BUG: Can't get here! Should have returned a geocode() result or exited.");
  }
  
}
