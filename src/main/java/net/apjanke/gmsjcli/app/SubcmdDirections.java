package net.apjanke.gmsjcli.app;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Display a route from one location to another.
 *
 * Usage:
 * <pre>
 *     java net.apjanke.gmsjcli.app.MainApp directions [-t &lt;travel-mode>] [-T &lt;transit-mode>] [-a] [-M &lt;traffic-model>]
 * </pre>
 */
public class SubcmdDirections extends GenericApiSubcmd {
    @Override
    public int run(String[] args) {
        super.run(args);
        getReady();

        // Parse command line inputs - subcommand-specific
        TravelMode travelMode = null;
        TransitMode transitMode = null;
        TrafficModel trafficModel = null;
        Unit unit = null;
        boolean showAlternatives = false;
        TransitRoutingPreference transitRoutingPreference = null;
        Options cmdLineOptions = new Options();
        cmdLineOptions.addOption("t", "travel-mode", true, "travel mode");
        cmdLineOptions.addOption("T", "transit-mode", true, "transit mode");
        cmdLineOptions.addOption("a", "alternatives", false, "display alternative routes");
        cmdLineOptions.addOption("M", "traffic-model", true, "traffic model");
        cmdLineOptions.addOption("u", "units", true, "measurement unit system to use for display");
        cmdLineOptions.addOption("p", "routing-preference", true, "routing preference (walking vs transfers)");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(cmdLineOptions, args);
        } catch (ParseException e) {
            System.err.println("Error: Could not parse command line: " + e.getMessage());
            System.exit(1);
        }
        if (cmdLine.hasOption("t")) {
            travelMode = parseTravelModeArg(cmdLine.getOptionValue("t"));
        }
        if (cmdLine.hasOption("T")) {
            transitMode = parseTransitModeArg(cmdLine.getOptionValue("T"));
        }
        if (cmdLine.hasOption("a")) {
            showAlternatives = true;
        }
        if (cmdLine.hasOption("M")) {
            trafficModel = parseTrafficModelArg(cmdLine.getOptionValue("M"));
        }
        if (cmdLine.hasOption("u")) {
            unit = parseUnit(cmdLine.getOptionValue("u"));
        }
        if (cmdLine.hasOption("p")) {
            transitRoutingPreference = parseTransitRoutingPreference(cmdLine.getOptionValue("p"));
        }
        List<String> leftovers = cmdLine.getArgList();
        if (leftovers.size() != 2) {
            darnit("Must supply exactly two arguments for route endpoints.");
        }

        String originArg = leftovers.get(0);
        String destinationArg = leftovers.get(1);

        // Run Directions API request
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
        DirectionsApiRequest req = DirectionsApi.newRequest(geoApiContext);
        req.origin(originArg);
        req.destination(destinationArg);
        if (travelMode != null) {
            req.mode(travelMode);
        }
        if (transitMode != null) {
            req.transitMode(transitMode);
        }
        if (showAlternatives) {
            req.alternatives(true);
        }
        if (trafficModel != null) {
            req.trafficModel(trafficModel);
        }
        if (unit != null) {
            req.units(unit);
        }
        if (transitRoutingPreference != null) {
            req.transitRoutingPreference(transitRoutingPreference);
        }
        DirectionsResult result = getDirectionsSafe(req);

        // Display output
        DirectionsDisplayer displayer = new DirectionsDisplayer();
        displayer.displayOutput(result);

        // Okay, things went as expected
        return 0;
    }

    private static TravelMode parseTravelModeArg(String arg) {
        switch (arg) {
            case "driving":
                return TravelMode.DRIVING;
            case "walking":
                return TravelMode.WALKING;
            case "bicycling":
                return TravelMode.BICYCLING;
            case "transit":
                return TravelMode.TRANSIT;
            case "unknown":
                return TravelMode.UNKNOWN;
            default:
                throw new IllegalArgumentException("Invalid TravelMode argument: " + arg);
        }
    }

    private static TransitMode parseTransitModeArg(String arg) {
        switch (arg) {
            case "bus":
                return TransitMode.BUS;
            case "subway":
                return TransitMode.SUBWAY;
            case "train":
                return TransitMode.TRAIN;
            case "tram":
                return TransitMode.TRAM;
            case "rail":
                return TransitMode.RAIL;
            default:
                throw new IllegalArgumentException("Invalid TransitMode argument: " + arg);
        }
    }

    private static TrafficModel parseTrafficModelArg(String arg) {
        switch (arg) {
            case "best-guess":
                return TrafficModel.BEST_GUESS;
            case "optimistic":
                return TrafficModel.OPTIMISTIC;
            case "pessimistic":
                return TrafficModel.PESSIMISTIC;
            default:
                throw new IllegalArgumentException("Invalid TrafficModel argument: " + arg);
        }
    }

    private static Unit parseUnit(String arg) {
        switch (arg) {
            case "metric":
                return Unit.METRIC;
            case "imperial":
                return Unit.IMPERIAL;
            default:
                throw new IllegalArgumentException("Invalid Unit argument: " + arg);
        }
    }

    private static TransitRoutingPreference parseTransitRoutingPreference(String arg) {
        switch (arg) {
            case "less-walking":
                return TransitRoutingPreference.LESS_WALKING;
            case "fewer-transfers":
                return TransitRoutingPreference.FEWER_TRANSFERS;
            default:
                throw new IllegalArgumentException("Invalid TransitRoutingPreference argument: " + arg);
        }
    }

    private static DirectionsResult getDirectionsSafe(GeoApiContext geoApiContext, String origin, String destination) {
        DirectionsApiRequest req = DirectionsApi.getDirections(geoApiContext, origin, destination);
        req.origin(origin);
        req.destination(destination);
        return getDirectionsSafe(req);
    }

    private static DirectionsResult getDirectionsSafe(DirectionsApiRequest request) {
        try {
            return request.await();
        } catch (Exception e) {
            System.err.format("Error: %s during Directions query: %s", e.getClass().getName(), e.getMessage());
            e.printStackTrace(System.err);
            System.exit(1);
        }
        throw new RuntimeException("BUG: Can't get here! This means there's a bug in gmsj-cli.");
    }

    private static class DirectionsDisplayer {
        private void displayOutput(DirectionsResult result) {
            PrintStream out = System.out;
            GeocodedWaypoint[] waypoints = result.geocodedWaypoints;
            DirectionsRoute[] routes = result.routes;
            for (int i = 0; i < waypoints.length; i++) {
                out.format("Waypoint %d: %s (placeId=%s)\n", i, waypoints[i], waypoints[i].placeId);
                AddressType[] addressTypes = waypoints[i].types;
                out.format("  AddressTypes: %s\n", Arrays.toString(addressTypes));
            }
            if (routes.length == 0) {
                out.println("No routes found.");
            }
            for (int i = 0; i < routes.length; i++) {
                DirectionsRoute route = routes[i];
                out.format("Route %d: %s\n", i, route);
                out.format("  Summary: %s\n", route.summary);
                out.format("  Legs (%d):\n", route.legs.length);
                for (int iLeg = 0; iLeg < route.legs.length; iLeg++) {
                    DirectionsLeg leg = route.legs[iLeg];
                    out.format("    Leg %d: %s -> %s\n", iLeg, leg.startAddress, leg.endAddress);
                    out.format("      Departure %s, Arrival %s, Distance %s\n",
                            leg.departureTime, leg.arrivalTime, leg.distance);
                    out.format("      Steps: %d\n", leg.steps.length);
                    for (int iStep = 0; iStep < leg.steps.length; iStep++) {
                        DirectionsStep step = leg.steps[iStep];
                        out.format("        Step %d: %s -> %s: %s (%s, %s)\n",
                                iStep, step.startLocation, step.endLocation, step.htmlInstructions, step.distance, step.duration);
                        out.format("                 %s\n", step.toString());
                    }
                }
            }
        }
    }
}
