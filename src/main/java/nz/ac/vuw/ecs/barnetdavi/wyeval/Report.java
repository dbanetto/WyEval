package nz.ac.vuw.ecs.barnetdavi.wyeval;

import wybs.util.StdProject;
import wyc.builder.CompileTask;
import wyc.builder.FlowTypeChecker;
import wyc.builder.LoopInvariantGenerator;
import wyc.builder.invariants.Util;
import wyc.lang.Expr;
import wyc.lang.Stmt;
import wyc.lang.WhileyFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nz.ac.vuw.ecs.barnetdavi.wyeval.Util.findLoops;

public class Report {

    private static final String STARTING_BOUND = "starting bound";
    private static final String AGED_LOOP = "aged loop";
    private static final String ARRAY_INIT = "array init";
    private static final String ARRAY_COPY = "array copy";

    private final boolean isControl;

    public Report() {
        this.isControl = false;
    }

    public Report(boolean isControl) {
        this.isControl = isControl;
    }


    private static final String[] keys = new String[] {
            STARTING_BOUND,
            ARRAY_COPY,
            AGED_LOOP,
            ARRAY_INIT
    };

    public void report(WhileyFile file, boolean generateLoopInv, String name) {

        List<ReportData> reports = generateReport(file);

        // print data
        for (ReportData data : reports) {

            StringBuilder builder = new StringBuilder();
            builder.append(name).append(',');
            builder.append(data.grandTotal()).append(',');
            builder.append(data.sourceInvariants).append(',');
            builder.append(data.totalGeneratedInvariants()).append(',');

            for (String key : keys) {
                builder.append(data.generatedInvariants.getOrDefault(key, 0))
                        .append(',');
            }

            if (isControl) {
                builder.append("control");
            } else {
                builder.append(generateLoopInv ? "gen" : "min");
            }

            System.out.println(builder);
        }
    }

    private List<ReportData> generateReport(WhileyFile file) {
        List<Stmt.While> loops = findLoops(file);

        List<ReportData> reports = new ArrayList<>();

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
                key = STARTING_BOUND;
            } else if (key.contains("aged loop")) {
                key = AGED_LOOP;
            } else if (key.contains("elements are predictability")) {
                key = ARRAY_INIT;
            } else if (key.contains("length equal to another array")) {
                key = ARRAY_COPY;
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
