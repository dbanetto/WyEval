import wyc.lang.Expr;
import wyc.lang.Stmt;
import wyc.lang.WhileyFile;

import java.util.*;

/**
 * Breaks down the loop invariants by separating conjunctive conditions
 * into multiple where clauses
 */
public class Breakdown {

    private final WhileyFile file;

    public Breakdown(WhileyFile file) {
       this.file = file;
    }

    public WhileyFile breakdown() {

        for (Stmt.While whileStmt : Util.findLoops(file)) {
            handleBreakdown(whileStmt);
        }

        return this.file;
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
