package net.apjanke.gmsjcli;

import net.apjanke.gmsjcli.app.SubcmdGeoFuzz;
import net.apjanke.gmsjcli.app.SubcmdGeocode;
import net.apjanke.gmsjcli.app.Subcommand;

import java.util.Arrays;

class MainApp {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Arguments are required.");
            System.exit(1);
        }

        String subcommand = args[0];
        if (subcommand.charAt(0) == '-') {
            System.err.println("Error: First argument must be a subcommand, not an option.");
            System.exit(1);
        }
        String[] remainingArgs = new String[args.length - 1];
        System.arraycopy(args, 1, remainingArgs, 0, args.length - 1);

        String[] validSubCmds = { "geocode", "geofuzz" };
        Subcommand cmd = null;
        if ("geocode".equals(subcommand)) {
            cmd = new SubcmdGeocode();
        } else if ("geofuzz".equals(subcommand)) {
            cmd = new SubcmdGeoFuzz();
        } else {
            System.err.println("Error: Unrecognized subcommand: " + subcommand);
            System.err.println("Error: Valid subcommands are: " + Arrays.toString(validSubCmds));
            System.exit(1);
        }
        int status = cmd.run(remainingArgs);
        System.exit(status);
    }
}
