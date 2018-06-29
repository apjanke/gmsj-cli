package net.apjanke.gmsjcli.app;

/**
 * Variants of outputs for Geocoding results
 */
public enum GeocodeOutputFormat {
    /** A concise, human-readable output format. */
    CONCISE,
    /** A GSON-driven JSON dump of the raw Java objects. */
    GSON,
    /** No output. */
    SILENT
}
