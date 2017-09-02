package nz.ac.vuw.ecs.barnetdavi.wyeval;

import wybs.lang.Attribute;
import wybs.lang.Build;
import wybs.util.StdProject;
import wyc.builder.CompileTask;
import wyc.builder.FlowTypeChecker;
import wyc.builder.LoopInvariantGenerator;
import wyc.builder.invariants.Util;
import wyc.lang.Expr;
import wyc.lang.Stmt;
import wyc.lang.WhileyFile;

import static nz.ac.vuw.ecs.barnetdavi.wyeval.Util.findLoops;
import java.util.*;

public class Report {

    public void report(WhileyFile file, boolean generateLoopInv) {

        List<ReportData> data = generateReport(file, generateLoopInv);

        // print data
        System.out.println(data);
    }

    private List<ReportData> generateReport(WhileyFile file, boolean generateLoopInv) {
        List<Stmt.While> loops = findLoops(file);

        List<ReportData> reports = new ArrayList<>();

        if (generateLoopInv) {
            // first run the type system over the AST (required for generation)
            new FlowTypeChecker(new CompileTask(new StdProject())).propagate(file);
            // then generate the loop invariants
            new LoopInvariantGenerator(file).generate();
        }

        for (Stmt.While  loop : loops) {
            ReportData entry = new ReportData();

            for (Expr inv : loop.invariants) {
                Util.GeneratedAttribute generated = inv.attribute(Util.GeneratedAttribute.class);

                if (generated != null) {
                    entry.inc(generated);
                } else {
                    entry.sourceInvariants += 1;
                }
            }

            reports.add(entry);
        }

        return reports;
    }

    private static class ReportData {
        int sourceInvariants = 0;
        Map<String, Integer> generatedInvariants = new HashMap<>();

        public void inc(Util.GeneratedAttribute attr) {

            String key = attr.toString().toLowerCase();

            // normalize key
            if (key.contains("starting boundary")) {
                key = "starting bound";
            } else if (key.contains("aged loop")) {
                key = "aged loop";
            } else if (key.contains("elements are predictability")) {
                key = "array init";
            } else if (key.contains("length equal to another array")) {
                key = "array copy";
            }

            // update entry
            int value = generatedInvariants.getOrDefault(key, 0);
            generatedInvariants.put(key, value + 1);
        }

        public int totalGeneratedInvariants() {
            int sum = 0;
            for (int value : generatedInvariants.values()) {
                sum += value;
            }
            return sum;
        }

        public int grandTotal() {
            return sourceInvariants + this.totalGeneratedInvariants();
        }

        @Override
        public String toString() {
            return "ReportData{" +
                    "sourceInvariants=" + sourceInvariants +
                    ", generatedInvariants=" + generatedInvariants +
                    '}';
        }
    }

}
