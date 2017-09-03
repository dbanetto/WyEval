package nz.ac.vuw.ecs.barnetdavi.wyeval;

import wyc.commands.Compile;
import wyc.lang.WhileyFile;
import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * Interface to compile and parse whiley files
 */
public class Whiley {

    public static WhileyFile parse(String path) throws IOException {

        File whileyfile = new File(path);
        File whileydir = new File(whileyfile.getCanonicalFile().getParent());

        Content.Registry registry = new wyc.Activator.Registry();
        Compile cmd = new Compile(registry, Logger.NULL, System.out, System.err);
        cmd.setWhileydir(whileydir);
        cmd.setWyaldir(whileydir);
        cmd.setWyildir(whileydir); // fixes getModifiedSourceFiles from crashing


        // Gets the entries to be formatted
        List<Path.Entry<WhileyFile>> entries = cmd.getModifiedSourceFiles();
        for (Path.Entry<WhileyFile> entry : entries) {
            if (entry.location().equals(whileyfile.getAbsolutePath())) {
                return WhileyFile.ContentType.read(entry, null);
            }
        }

        throw new RuntimeException("Could not find " + whileyfile);
    }

    public static boolean compile(String path, boolean verify, boolean generateLoopInv, boolean verbose) throws IOException {
       return compile(new File(path), verify, generateLoopInv, verbose);
    }

    public static boolean compile(File whileyfile, boolean verify, boolean generateLoopInv, boolean verbose) throws IOException {
        File whileydir = new File(whileyfile.getCanonicalFile().getParent());

        Content.Registry registry = new wyc.Activator.Registry();

        PrintStream stdout = System.out;
        PrintStream stderr = System.err;

        PrintStream out = stdout;
        PrintStream err = stderr;

        if (!verbose) {
            out = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException { }
            });

            err = out;
        }

        System.setOut(out);
        System.setErr(err);

        Compile cmd = new Compile(registry, Logger.NULL, out, err);
        cmd.setWhileydir(whileydir);
        cmd.setWyaldir(whileydir);
        cmd.setWyildir(whileydir);

        cmd.setVerify(verify);
        cmd.setGenerateLoopInvariant(generateLoopInv);

        try {
            Compile.Result result = cmd.execute(whileyfile.getAbsolutePath());

            return result == Compile.Result.SUCCESS;
        } catch (Exception e) {
            System.exit(-1);
            throw e;
        } finally {
            System.setOut(stdout);
            System.setErr(stderr);
        }
    }
}
