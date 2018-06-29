package net.apjanke.gmsjcli.app;

/**
 * Generic class for a subcommand that has to use the Google Maps API, but not more specific than that.
 * @author janke
 */
public class GenericApiCmd extends Subcommand {
  protected String apiKey;
  
  public void getReady() {
        // General API stuff
        apiKey = System.getenv("GOOGLE_MAPS_API_KEY");
        if (apiKey == null) {
            System.err.println("No API key found.");
            System.err.println("The GOOGLE_MAPS_API_KEY environment variable must be set.");
            System.exit(1);
        }
  }
  
}
