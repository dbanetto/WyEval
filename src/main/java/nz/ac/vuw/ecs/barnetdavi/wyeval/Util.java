package nz.ac.vuw.ecs.barnetdavi.wyeval;

import wyc.lang.Stmt;
import wyc.lang.WhileyFile;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static List<Stmt.While> findLoops(WhileyFile file) {
        List<Stmt.While> loops = new ArrayList<>();
        // search through find all while loops
        for (WhileyFile.FunctionOrMethodOrProperty functionOrMethodOrProperty :
                file.declarations(WhileyFile.FunctionOrMethodOrProperty.class)) {
            loops.addAll(Util.findLoops(functionOrMethodOrProperty.statements));
        }

        return loops;
    }

    public static  List<Stmt.While> findLoops(List<Stmt> stmts) {
        List<Stmt.While> loops = new ArrayList<>();

        findLoops(stmts, loops);

        return loops;
    }


    private static void findLoops(List<Stmt> stmts, List<Stmt.While> loops) {
        for (Stmt stmt: stmts) {
            findLoops(stmt, loops);
        }
    }

    private static void findLoops(Stmt stmt, List<Stmt.While> loops) {
        if (stmt instanceof Stmt.While) {
            Stmt.While whileStmt = (Stmt.While) stmt;

            loops.add(whileStmt);

            findLoops(whileStmt.body, loops);
        } else if (stmt instanceof Stmt.Switch) {
            Stmt.Switch switchStmt = (Stmt.Switch) stmt;

            for (Stmt.Case swCase : switchStmt.cases) {
                findLoops(swCase.stmts, loops);
            }
        } else if (stmt instanceof Stmt.IfElse) {
            Stmt.IfElse ifElseStmt = (Stmt.IfElse) stmt;

            findLoops(ifElseStmt.trueBranch, loops);
            if (ifElseStmt.falseBranch != null) {
                findLoops(ifElseStmt.falseBranch, loops);
            }
        }
    }

}
