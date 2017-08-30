import wyc.lang.Expr;
import wyc.lang.Stmt;
import wyc.lang.WhileyFile;

import java.util.*;

public class Breakdown {
    private final WhileyFile file;

    public Breakdown(WhileyFile file) {
       this.file = file;
    }

    public WhileyFile breakdown() {

        // search through find all while loops
        for (WhileyFile.FunctionOrMethodOrProperty functionOrMethodOrProperty :
                file.declarations(WhileyFile.FunctionOrMethodOrProperty.class)) {


            // go through the things
            breakdown(functionOrMethodOrProperty.statements);
        }

        // then break up &&'s into separate invariant lines

        return this.file;
    }


    private void breakdown(List<Stmt> stmts) {
        for (Stmt stmt: stmts) {
            breakdown(stmt);
        }
    }
    private void breakdown(Stmt stmt) {
        if (stmt instanceof Stmt.While) {
            Stmt.While whileStmt = (Stmt.While) stmt;

            handleBreakdown(whileStmt);

            breakdown(whileStmt.body);
        } else if (stmt instanceof Stmt.Switch) {
            Stmt.Switch switchStmt = (Stmt.Switch) stmt;

            for (Stmt.Case swCase : switchStmt.cases) {
                breakdown(swCase.stmts);
            }
        } else if (stmt instanceof Stmt.IfElse) {
            Stmt.IfElse ifElseStmt = (Stmt.IfElse) stmt;

            breakdown(ifElseStmt.trueBranch);
            if (ifElseStmt.falseBranch != null) {
                breakdown(ifElseStmt.falseBranch);
            }
        }
    }

    private void handleBreakdown(Stmt.While whileStmt) {
        List<Expr> exprs = new ArrayList<>(whileStmt.invariants);

        whileStmt.invariants.clear();

        List<Expr> brokendown = new ArrayList<>();
        for (Expr expr : exprs) {
            brokendown.addAll(breakExpr(expr));
        }

        whileStmt.invariants.addAll(brokendown);
    }

    private List<Expr> breakExpr(Expr expr) {
       List<Expr> exprs = new ArrayList<>();
       if (expr instanceof Expr.BinOp) {
           Expr.BinOp binOp = (Expr.BinOp) expr;

           if (binOp.op == Expr.BOp.AND) {

               exprs.addAll(breakExpr(binOp.lhs));
               exprs.addAll(breakExpr(binOp.rhs));
               return exprs;
           }
       }

       exprs.add(expr);
       return exprs;
    }
}
