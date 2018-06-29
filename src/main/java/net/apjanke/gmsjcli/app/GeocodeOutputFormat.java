package net.apjanke.gmsjcli.app;

/**
 * Variants of outputs for Geocoding results
 */
public enum GeocodeOutputFormat {
    /** A long-ish, human-readable output format. */
    REGULAR,
    /** A concise, 1 line per element, format. */
    CONCISE,
    /** An even more concise, 1 line per entire result, format. */
    TERSE,
    /** A GSON-driven JSON dump of the raw Java objects. */
    GSON,
    /** No output. */
    SILENT
}
