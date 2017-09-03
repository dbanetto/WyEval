package nz.ac.vuw.ecs.barnetdavi.wyeval;

import org.apache.commons.cli.*;
import wyc.io.WhileyFilePrinter;

import java.io.*;

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
        } else if (cmd.hasOption("minimize")) {
            handleMinimize(cmd);
        } else if (cmd.hasOption("report")) {
            handleReport(cmd);
        } else if (cmd.hasOption("check")) {
            handleCheck(cmd);
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

    private static void handleMinimize(CommandLine cmd) {
        String file = cmd.getOptionValue("minimize");

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
            minimize(file, cmd.hasOption("loopinv"), outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void handleReport(CommandLine cmd) {
        String file = cmd.getOptionValue("report");

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
            report(file, cmd.hasOption("loopinv"), outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void handleCheck(CommandLine cmd) {
        String file = cmd.getOptionValue("check");

        try {
            boolean success = Whiley.compile(file, true, false);
            if (!success) {
                System.exit(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption(Option.builder("report")
                .desc("Report the number and kinds of loop invariants in the file")
                .hasArg()
                .build());

        options.addOption(Option.builder("breakdown")
                .desc("Breakdown loop invariants of given file")
                .hasArg()
                .build());

        options.addOption(Option.builder("minimize")
                .desc("Minimizes the loop invariants of given file")
                .hasArg()
                .build());

        options.addOption(Option.builder("check")
                .desc("Checks if a given file will compile or not")
                .hasArg()
                .build());

        options.addOption(Option.builder("o")
                .desc("Output of the operation")
                .hasArg()
                .build());

        options.addOption(Option.builder("loopinv")
                .desc("Controls if compiled with loop invariant generation")
                .build());

        return options;
    }

    private static void breakdown(String path, OutputStream result) throws IOException {
        new WhileyFilePrinter(result).print(new Breakdown(Whiley.parse(path)).breakdown());
    }


    private static void minimize(String path, boolean generateLoopInv, OutputStream result) throws IOException {
            new WhileyFilePrinter(result).print(new Minimize(Whiley.parse(path), generateLoopInv).minimize());
    }

    private static void report(String path, boolean generateLoopInv, OutputStream result) throws IOException {
        new Report().report(Whiley.parse(path), generateLoopInv);
    }
}
