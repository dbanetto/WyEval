import wyc.commands.Compile;
import wyc.io.WhileyFilePrinter;
import wyc.lang.WhileyFile;
import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;
import org.apache.commons.cli.*;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        CommandLine cmd = null;
        try {
             cmd = new DefaultParser().parse(getOptions(), args);
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(1);
        }

        if (cmd.hasOption("breakdown")) {
            handleBreakdown(cmd);
        } else {
            new HelpFormatter().printHelp("eval", getOptions());
        }

    }

    private static void handleBreakdown(CommandLine cmd) {
        String file = cmd.getOptionValue("breakdown");

        OutputStream outputStream = System.out;

        if (cmd.hasOption("o")) {
            File outputFile = new File(cmd.getOptionValue("o"));
            try {
                outputStream = new FileOutputStream(outputFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(2);
            }
        }

        try {
            breakdown(file, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption(Option.builder("breakdown")
                .desc("Breakdown loop invariants of given file")
                .hasArg()
                .build());
        options.addOption(Option.builder("o")
                .desc("Output of the operation")
                .hasArg()
                .build());

        options.addOption(Option.builder("minimize")
                .desc("Minimizes the loop invariants of given file")
                .hasArg()
                .build());

        options.addOption(Option.builder("loopinv")
                .desc("Controls if compiled with loop invariant generation")
                .build());

        return options;
    }

    private static void breakdown(String path, OutputStream result) throws IOException {
        File whileyfile = new File(path);
        File whileydir = new File(whileyfile.getCanonicalFile().getParent());

        Content.Registry registry = new wyc.Activator.Registry();
        Compile cmd = new Compile(registry, Logger.NULL, System.out, System.err);
        cmd.setWhileydir(whileydir);
        cmd.setWyaldir(whileydir);
        cmd.setWyildir(whileydir); // fixes getModifiedSourceFiles from crashing


        // Gets the entries to be formatted
        List<Path.Entry<WhileyFile>> entries = cmd.getModifiedSourceFiles();

        List<WhileyFile> whileyFiles = new ArrayList<>(entries.size());
        for (Path.Entry<WhileyFile> entry : entries) {
            WhileyFile file = WhileyFile.ContentType.read(entry, null);

            // Breakdown
            WhileyFile broke = new Breakdown(file).breakdown();

            new WhileyFilePrinter(result).print(broke);
        }
    }
}
