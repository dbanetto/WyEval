package nz.ac.vuw.ecs.barnetdavi.wyeval;

import wyc.commands.Compile;
import wyc.lang.WhileyFile;
import wycc.util.Logger;
import wyfs.lang.Content;
import wyfs.lang.Path;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Interface to compile and parse whiley files
 */
public class Whiley {

    public static List<WhileyFile> parse(String path) throws IOException {

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
            if (entry.location().equals(whileyfile.getAbsolutePath())) {
                whileyFiles.add(WhileyFile.ContentType.read(entry, null));
            }
        }

        return whileyFiles;
    }

    public static boolean compile(String path, boolean verify, boolean generateLoopInv) throws IOException {
       return compile(new File(path), verify, generateLoopInv);
    }

    public static boolean compile(File whileyfile, boolean verify, boolean generateLoopInv) throws IOException {
        File whileydir = new File(whileyfile.getCanonicalFile().getParent());

        Content.Registry registry = new wyc.Activator.Registry();
        Compile cmd = new Compile(registry, Logger.NULL, System.out, System.err);
        cmd.setWhileydir(whileydir);
        cmd.setWyaldir(whileydir);
        cmd.setWyildir(whileydir);

        cmd.setVerify(verify);
        cmd.setGenerateLoopInvariant(generateLoopInv);

        Compile.Result result = cmd.execute(whileyfile.getAbsolutePath());

        return result == Compile.Result.SUCCESS;
    }
}
