package net.apjanke.gmsjcli.app;

public class Subcommand {
    public int run(String[] args) {
        return 0;
    }

    public static void darnit(String message) {
        System.err.println("Error: " + message);
        System.exit(1);
    }
}
