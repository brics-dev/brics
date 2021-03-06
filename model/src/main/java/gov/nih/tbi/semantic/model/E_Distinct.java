package gov.nih.tbi.semantic.model;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class E_Distinct extends ExprFunction1 {

	private static final String symbol = "DISTINCT";

	public E_Distinct(Expr expr) {
		super(expr, symbol);
	}

	@Override
	public NodeValue eval(NodeValue v) {
		return v;
	}

	@Override
	public Expr copy(Expr expr) {
		return new E_Distinct(expr);
	}
}
