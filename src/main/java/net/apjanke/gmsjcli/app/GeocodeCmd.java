package net.apjanke.gmsjcli.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;

import java.io.IOException;

public class GeocodeCmd extends Subcommand {
    @Override
    public int run(String[] args) {
        super.run(args);

        // Parse inputs

        // Haha, hack
        if (args.length == 0) {
            darnit("Geocoding argument is required.");
        }
        if (args.length > 1) {
            darnit("Too many inputs.");
        }
        String geocodeInput = args[0];
        System.out.println("Geocoding: " + geocodeInput);

        // General API stuff
        String apiKey = System.getenv("GOOGLE_MAPS_API_KEY");
        if (apiKey == null) {
          System.err.println("No API key found.");
          System.err.println("The GOOGLE_MAPS_API_KEY environment variable must be set.");
          System.exit(1);
        }
                
        // Run geocoding request
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
        System.out.println("Results length: " + results.length);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (int i = 0; i < results.length; i++) {
            System.out.println("Result " + i + ":");
            System.out.println(gson.toJson(results[i]));
        }

        // Okay, things went as expected
        return 0;
    }
}
