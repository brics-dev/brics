package gov.nih.tbi.commons.util;

import gov.nih.tbi.ModelConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_Exists;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_NotExists;
import com.hp.hpl.jena.sparql.expr.E_OneOf;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_StrLowerCase;
import com.hp.hpl.jena.sparql.expr.E_StrUpperCase;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDT;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueString;
import com.hp.hpl.jena.sparql.modify.request.QuadDataAcc;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;

public class SparqlConstructionUtil {

	private static final Map<String, String> SPARQL_ESCAPE_SEARCH_REPLACEMENTS = new HashMap<String, String>();
	private static final String STRING_SEARCH_START = "^";
	private static final String STRING_SEARCH_END = "$";


	static {
		SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\t", "\\t");
		SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\n", "\\n");
		SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\r", "\\r");
		SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\b", "\\b");
		SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\f", "\\f");
		SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\"", "\\\"");
		SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("'", "\\'");
		SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("\\", "\\\\");
		SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put("(", "\\(");
		SPARQL_ESCAPE_SEARCH_REPLACEMENTS.put(")", "\\)");
	}


	public static String regexEscape(String str) {
		StringBuffer bufOutput = new StringBuffer(str);
		for (int i = 0; i < bufOutput.length(); i++) {
			String replacement = SPARQL_ESCAPE_SEARCH_REPLACEMENTS.get("" + bufOutput.charAt(i));
			if (replacement != null) {
				bufOutput.deleteCharAt(i);
				bufOutput.insert(i, replacement);

				i += (replacement.length() - 1);
			}
		}
		return bufOutput.toString();
	}

	/**
	 * Returns the RDF literal sans any appended type
	 * 
	 * @param s
	 * @return
	 */
	public static String trimRdfType(String s) {

		String[] parts = s.split("\\^\\^");
		return parts[0];
	}

	/**
	 * Convenience method to build a optional block using one triple
	 * 
	 * @param subject
	 * @param property
	 * @param object
	 * @return
	 */
	public static ElementOptional buildSingleOptionalPattern(Node subject, Node property, Node object) {

		ElementTriplesBlock optionalBlock = new ElementTriplesBlock();
		optionalBlock.addTriple(Triple.create(subject, property, object));
		ElementOptional elementOptional = new ElementOptional(optionalBlock);
		return elementOptional;
	}

	/**
	 * Convenience method to build a optional block using one triple
	 * 
	 * @param subject
	 * @param property
	 * @param object
	 * @return
	 */
	public static ElementOptional buildGroupOptionalPattern(Collection<Triple> tripleSet)// Node subject, Node property,
																							// Node
	// object)
	{

		ElementTriplesBlock optionalBlock = new ElementTriplesBlock();
		for (Triple t : tripleSet) {
			optionalBlock.addTriple(t);
		}
		ElementOptional elementOptional = new ElementOptional(optionalBlock);
		return elementOptional;
	}

	public static Expr multiRegexFilter(String variable, Set<String> searchKey) {

		LinkedList<String> searchKeyList = new LinkedList<String>();
		searchKeyList.addAll(searchKey);

		return multiRegexFilterHelper(new ExprVar(variable), searchKeyList);
	}

	public static Expr multiRegexFilterHelper(ExprVar variable, LinkedList<String> searchKey) {

		if (searchKey.size() == 0) {
			return null;
		} else if (searchKey.size() == 1) // base case
		{
			return new E_Regex(new E_Str(variable), regexEscape(searchKey.pop()), "i");
		} else
		// search key has more than one element
		{
			return new E_LogicalOr(
					new E_Regex(new E_Str(variable), regexEscape(searchKey.pop()), "i"),
					multiRegexFilterHelper(variable, searchKey));
		}
	}

	public static ElementFilter regexFilter(Node variable, String searchKey) {

		Expr expression = new E_Regex(new E_Str(new ExprVar(variable)),
				regexEscape(STRING_SEARCH_START + searchKey + STRING_SEARCH_END), "i");
		ElementFilter filter = new ElementFilter(expression);

		return filter;
	}

	public static ElementFilter greaterThanDate(ExprVar variable, String dateString) {

		Expr conditional = new E_GreaterThan(variable,
				new NodeValueDT(dateString, NodeFactory.createLiteral(dateString, XSDDatatype.XSDdateTime)));
		return new ElementFilter(conditional);
	}

	public static ElementFilter isOneOfIgnoreCase(String variable, Collection<String> uris) {

		return isOneOfIgnoreCase(variable, ModelConstants.EMPTY_STRING, uris);
	}

	public static ElementFilter isOneOfIgnoreCase(String variable, String namespace, Collection<String> uris) {

		ExprList urisExpression = new ExprList();

		for (String uri : uris) {
			urisExpression.add(new E_StrUpperCase(new NodeValueString(namespace + uri)));
		}

		Expr filterExpression = new E_OneOf(new E_StrUpperCase(new E_Str(new ExprVar(variable))), urisExpression);

		ElementFilter filter = new ElementFilter(filterExpression);

		return filter;
	}

	/**
	 * Builds a sub-query with one VALUES clause with one variable binding to a list of values Looks something like, {
	 * VALUES (variable) { "value1", "value2", "value3", ..."valueN" } } This used as an alternative for FILTER IN when
	 * filtering against a large list of items.
	 * 
	 * @param variable
	 * @param values
	 * @return
	 */
	public static <T> ElementSubQuery buildValuesSubQuery(Var variable, Collection<T> values) {

		Query subQuery = QueryFactory.make();
		subQuery.setDistinct(true);

		List<Var> variables = new ArrayList<Var>();
		variables.add(variable);
		subQuery.setValuesDataBlock(variables, buildValuesBinding(variable, values));

		return new ElementSubQuery(subQuery);
	}

	public static <T> ElementSubQuery buildUriValuesSubQuery(Var variable, Collection<T> values) {

		Query subQuery = QueryFactory.make();
		subQuery.setDistinct(true);

		List<Var> variables = new ArrayList<Var>();
		variables.add(variable);
		subQuery.setValuesDataBlock(variables, buildUriValuesBinding(variable, values));

		return new ElementSubQuery(subQuery);
	}

	/**
	 * Creates a list of bindings for a single variable
	 * 
	 * @param variable
	 * @param values
	 * @return
	 */
	public static <T> List<Binding> buildValuesBinding(Var variable, Collection<T> values) {

		List<Binding> bindings = new ArrayList<Binding>();

		for (T value : values) {
			BindingHashMap bindingHashMap = new BindingHashMap();
			bindingHashMap.add(variable, NodeFactory.createLiteral(value.toString()));
			bindings.add(bindingHashMap);
		}

		return bindings;
	}

	/**
	 * Creates a list of bindings for a single variable
	 * 
	 * @param variable
	 * @param values
	 * @return
	 */
	public static <T> List<Binding> buildUriValuesBinding(Var variable, Collection<T> values) {

		List<Binding> bindings = new ArrayList<Binding>();

		for (T value : values) {
			BindingHashMap bindingHashMap = new BindingHashMap();
			bindingHashMap.add(variable, NodeFactory.createURI(value.toString()));
			bindings.add(bindingHashMap);
		}

		return bindings;
	}

	/**
	 * Creates an IN filter for a variable against a list of values Looks like, FILTER( ?variable IN "value1", "value2",
	 * ..."valueN" )
	 * 
	 * @param variable
	 * @param uris
	 * @return
	 */
	public static ElementFilter isOneOfUri(String variable, Collection<String> uris) {

		return isOneOfUri(variable, ModelConstants.EMPTY_STRING, uris);
	}

	/**
	 * Construct the one of filter using a list of strings and the variable name to filter
	 * 
	 * @param variable
	 * @param uris
	 * @return
	 */
	public static ElementFilter isOneOfUri(String variable, String namespace, Collection<String> uris) {

		ExprList urisExpression = new ExprList();

		for (String uri : uris) {
			urisExpression.add(new NodeValueString(uri));
		}

		Expr filterExpression = new E_OneOf(new E_Str(new ExprVar(variable)), urisExpression);

		ElementFilter filter = new ElementFilter(filterExpression);

		return filter;
	}

	public static E_Equals buildEqualsExpression(Var variable, String value) {

		if (variable == null || value == null) {
			return null;
		}

		return new E_Equals(new E_Str(new ExprVar(variable)), new NodeValueString(value));
	}


	public static E_NotEquals buildNotEqualsExpression(Var variable, String value) {

		if (variable == null || value == null) {
			return null;
		}

		return new E_NotEquals(new E_Str(new ExprVar(variable)), new NodeValueString(value));
	}

	/**
	 * Add the list of triples into the provided QuadDataAcc object
	 * 
	 * @param quads
	 * @param triples
	 * @return
	 */
	public static QuadDataAcc addTriples(QuadDataAcc quads, List<Triple> triples) {

		// if quads is null, initiate new QuadDataAcc
		if (quads == null || triples == null || triples.isEmpty()) {
			quads = new QuadDataAcc();
		}

		for (Triple triple : triples) {
			quads.addTriple(triple);
		}

		return quads;
	}

	/**
	 * Returns an filter exists expression. (e.g. FILTER EXISTS { ?s ?p ?o })
	 * 
	 * @param block
	 * @param exists
	 * @return
	 */
	public static ElementFilter buildExistsExpression(ElementGroup group, boolean exists) {
		Expr expression = null;

		if (exists) {
			expression = new E_Exists(group);
		} else {
			expression = new E_NotExists(group);
		}

		return new ElementFilter(expression);
	}

	/**
	 * Returns an filter exists expression. (e.g. FILTER EXISTS { ?s ?p ?o })
	 * 
	 * @param block
	 * @param exists
	 * @return
	 */
	public static ElementFilter buildExistsExpression(Triple triple, boolean exists) {
		ElementGroup group = new ElementGroup();
		ElementTriplesBlock block = new ElementTriplesBlock();
		block.addTriple(triple);
		group.addElement(block);
		return buildExistsExpression(group, exists);
	}
	
	public static E_Equals buildEqualsExpressionCaseInsensitive(Var variable, String value) {

		if (variable == null || value == null) {
			return null;
		}

		return new E_Equals(new E_StrLowerCase(new E_Str(new ExprVar(variable))), new NodeValueString(value.toLowerCase()));
	}
}
