package nz.ac.vuw.ecs.barnetdavi.wyeval;

import org.paukov.combinatorics.CombinatoricsVector;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;
import wyc.io.WhileyFilePrinter;
import wyc.lang.Expr;
import wyc.lang.Stmt;
import wyc.lang.WhileyFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
  * Minimizes
 */
public class Minimize {

    private final WhileyFile file;
    private final boolean generateLoopInv;

    public Minimize(WhileyFile file, boolean generateLoopInv ) {
       this.file = file;
       this.generateLoopInv = generateLoopInv;
    }

    public WhileyFile minimize() {

        List<Stmt.While> loops = Util.findLoops(file);

        List<LoopInvPair> pairs = pairLoopInvariant(loops);
        List<LoopInvPair> best = null;

        round: for (int round = 0; round <= pairs.size(); round++ ) {
            CombinatoricsVector<LoopInvPair> pairsCombo = new CombinatoricsVector<>(pairs);
            Generator<LoopInvPair> combos = Factory.createSimpleCombinationGenerator(pairsCombo, round);

            System.out.println("Testing combinations of  " + round);

            for (ICombinatoricsVector<LoopInvPair> selected : combos) {

                List<LoopInvPair> candidate = selected.getVector();

                setInvariants(loops, candidate);

                // test program
                try {
                    if (compile()) {
                        // first combination to work is the minimum set of invariants required
                        best = candidate;
                        break round;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        if (best != null) {
            setInvariants(loops, best);
        } else {
            throw new RuntimeException("Did not find a valid combination of invariants");
        }

        return this.file;
    }

    private boolean compile() throws IOException {

        // dump whiley file to disk
        File temp = File.createTempFile("_minimising_", ".whiley");
        FileOutputStream fileSteam = new FileOutputStream(temp);
        new WhileyFilePrinter(fileSteam).print(file);
        fileSteam.flush();
        fileSteam.close();

        // compile with given flags
        boolean result;

        try {
            result = Whiley.compile(temp, true, generateLoopInv);
        } catch (IOException ex) {
            throw  ex;
        } finally {
            // clean up
            new File(temp.getParent(), temp.getName().replace(".whiley", ".wyal")).delete();
            new File(temp.getParent(), temp.getName().replace(".whiley", ".wyil")).delete();
            temp.delete();
        }

        return result;
    }

    private void setInvariants(List<Stmt.While> loops, Iterable<LoopInvPair> invs) {
        // reset all invariants
        for (Stmt.While loop : loops) {
            loop.invariants.clear();
        }

        // apply all invariants to loops
        for (LoopInvPair pair : invs) {
            pair.loop.invariants.add(pair.invariant);
        }
    }

    /**
     * Pairs up loops with their invariants
     * @param loops
     * @return A list of LoopInvPairs that are a mapping from a loop to an invariant
     */
    private List<LoopInvPair> pairLoopInvariant(List<Stmt.While> loops) {
        List<LoopInvPair> pairs = new ArrayList<>();

        for (Stmt.While loop : loops) {
            for (Expr inv : loop.invariants) {
                pairs.add(new LoopInvPair(loop, inv));
            }
        }

        return pairs;
    }

    private static class LoopInvPair {
        final Stmt.While loop;
        final Expr invariant;

        LoopInvPair(Stmt.While loop, Expr invariant) {
            this.loop = loop;
            this.invariant = invariant;
        }
    }
}
