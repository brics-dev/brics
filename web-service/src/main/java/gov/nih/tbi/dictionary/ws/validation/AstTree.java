package gov.nih.tbi.dictionary.ws.validation;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class AstTree {

	//Store full names structure shortname.elementName
	private HashSet<String> columnRefs;
	private HashSet<String> rowRefs;
	private Node root;

	public AstTree(String shortName, Vector<String> tokens) throws ParseException{

		columnRefs = new HashSet<String>();
		rowRefs = new HashSet<String>();
	
		for(int i = 0; i < tokens.size(); i++){
			String s = tokens.get(i);
			if (ValidationUtil.isColRef(s)){
				if (s.contains(ValidationConstants.VALUE_REFERENCE_DIVIDER)){
					String[] splits = s.split( "\\" + ValidationConstants.VALUE_REFERENCE_DIVIDER);
					if(splits.length != 2){
						throw new ParseException("Column references should be of the format $short_name.element_name or $element_name", i);
					}
					columnRefs.add(s.substring(1).toLowerCase());
				}else{
					s = (shortName + ValidationConstants.VALUE_REFERENCE_DIVIDER + s.substring(1)).toLowerCase();
					columnRefs.add(s);
					tokens.set(i, ValidationConstants.VALUE_COLUMN_REFERENCE + s);
				}
			}else if (ValidationUtil.isRowRef(s)){
				if (s.contains(ValidationConstants.VALUE_REFERENCE_DIVIDER)){
					throw new ParseException("Row references should be of the format #element_name", i);
				}else{
					s = (shortName + ValidationConstants.VALUE_REFERENCE_DIVIDER + s.substring(1)).toLowerCase();
					rowRefs.add(s);
					tokens.set(i, ValidationConstants.VALUE_ROW_REFERENCE + s);
				}
			}
		}
		
		root = buildTree(tokens);
	}
	
	private AstTree(Node root){
		this.root = root;
	}

	public HashSet<String> getColumnRefs() {
		return columnRefs;
	}
	
	public boolean isRootNull(){
		return (root == null);
	}
	
	public void setConstraintTypes(ConditionalValidator validator)throws RuntimeException{
		if(root != null){
			root.setType(validator);
		}
	}

	public boolean evaluate(ConditionalValidator validator) throws RuntimeException{
		if(root != null){
			return root.eval(validator);
		}
		return true;
	}
	
	public AstTree rowValuesSubsitution(HashMap<String, String>rowValues) throws RuntimeException{
		if(root != null){
			return new AstTree(root.valSub(rowValues));
		}
		return null;
	}

	private Node buildTree(Vector<String> tokens) throws ParseException{
		return buildTree(tokens, false, 0);
	}


	private Node buildTree (Vector<String> tokens, boolean negate, int loc) throws ParseException {
		if(tokens.isEmpty()){
			return null;
		}
		
		Vector<String> left = new Vector<String>();
		int parenCounter = 0;

		if(tokens.get(0).equalsIgnoreCase(ValidationConstants.CONSTRAINT_NEGATION)){ 
			if(!tokens.get(1).equalsIgnoreCase("(")){
				throw new ParseException("You can only negate constraints, '(' expected and missing", loc);				
			}
			negate = true;
			tokens.remove(0);
			loc++;
		}

		if(tokens.get(0).equalsIgnoreCase("(")){
			for(int i = 0; i < tokens.size(); i++) {

				left.addElement(tokens.get(i));

				if(tokens.get(i).equalsIgnoreCase("(")) {
					parenCounter++;
				}else if(tokens.get(i).equalsIgnoreCase(")")) {
					parenCounter--;
				}

				if (parenCounter < 0){
					throw new ParseException("Unmatched closing perenthesis found", loc + i);
				}else if (parenCounter == 0){
					//remove the ()
					left.remove(0);
					left.remove(left.size() - 1);
					i++;
					if (i == tokens.size()){
						return buildTree(left, negate, loc + 1);
					}else if (tokens.elementAt(i).equalsIgnoreCase(ValidationConstants.CONSTRAINT_AND) || 
							tokens.elementAt(i).equalsIgnoreCase(ValidationConstants.CONSTRAINT_OR)){
						LogicalOp node = new LogicalOp(tokens.elementAt(i), false, loc + i);
						node.left = buildTree(left, negate, loc + 1);
						i++;
						node.right = buildTree(getRight(tokens,i), false, loc + i);
						return node;
					}else{
						throw new ParseException("Constrains must be separated by logical operators - && ||", loc + i);
					}
				}
			}			
			throw new ParseException("Unmatched opening perenthesis found", loc);
		}

		for(int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).equalsIgnoreCase("(") || tokens.get(i).equalsIgnoreCase(")")
					|| tokens.elementAt(i).equalsIgnoreCase(ValidationConstants.CONSTRAINT_NEGATION)){
				throw new ParseException(tokens.get(i) + " is only valid around a constraint expression", loc + i);
			}else if (tokens.elementAt(i).equalsIgnoreCase(ValidationConstants.CONSTRAINT_AND) || 
					tokens.elementAt(i).equalsIgnoreCase(ValidationConstants.CONSTRAINT_OR)){
				LogicalOp node = new LogicalOp(tokens.elementAt(i), negate, loc + i);
				node.left = buildTree(left, false, loc);
				i++;
				node.right = buildTree(getRight(tokens,i), false, loc + i);
				return node;
			}else
				left.addElement(tokens.get(i));
		}


		if(!ValidationUtil.isRowRef(tokens.get(0))){
			throw new ParseException("Contraints must begin with a row reference - #element_name", loc);
		}

		String columnRef = tokens.remove(0);
		loc++;

		if (!ValidationUtil.isOperator(tokens.get(0))){
			throw new ParseException("Contraints missing operator - <, >, =, !=, <=, >=, ~, !~", loc);
		}

		String operator = tokens.remove(0);
		loc++;
		
		if (tokens.isEmpty()){
			throw new ParseException("Constraint missing value", loc);
		}
		
		boolean isRangeOp = ValidationUtil.isRangeOperator(operator);
		
		if (!isRangeOp && (tokens.size() > 1)){
				throw new ParseException("Multiple values are only valid for range operators - ~, !~", loc);
		}
		
		Vector<String> values = new Vector<String>();
		
		for(int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if(ValidationUtil.isColRef(token) && !isRangeOp){
				throw new ParseException("Column references, $element_name, are only valid values for range operators - ~, !~",
						loc + i);
			}
			if(token.endsWith(ValidationConstants.VALUE_RANGE_BOTTOM_BOUND) && !isRangeOp){
				throw new ParseException("Value ranges can only be specified for contstraints with range operators - ~, !~", 
						loc + token.indexOf(ValidationConstants.VALUE_RANGE_DELIMITER));
			}
			if (token.contains(ValidationConstants.VALUE_RANGE_DELIMITER)){
				if(!isRangeOp){
					throw new ParseException("Value ranges can only be specified for contstraints with range operators - ~, !~", 
							loc + token.indexOf(ValidationConstants.VALUE_RANGE_DELIMITER));
				}
				String [] minMax = token.split(ValidationConstants.VALUE_RANGE_DELIMITER);
				if (minMax.length != 2){
					throw new ParseException("Incorrect value range syntax", loc);
				}
			}
			if (!token.equalsIgnoreCase(ValidationConstants.VALUE_DELIMITER)){
				values.add(tokens.get(i));
			}
		}

		return new Constraint(operator, columnRef, values, negate, loc);	
	}

	private Vector<String> getRight(Vector<String> tokens, int start){
		Vector<String> right = new Vector<String>();
		for(int i=start; i<tokens.size();i++) {
			right.addElement(tokens.get(i));
		}
		return right;	
	}
	
	public void printTree(){
		ArrayList<Node> next = new ArrayList<Node>();
		next.add(root);
		printTree(next);
	}
	
	private void printTree(ArrayList<Node> level){
		String row = "";
		ArrayList<Node> next = new ArrayList<Node>();
		for (Node n : level){
			if (n != null){
				row = row  + n.printNode() + "\t";
				if (n instanceof LogicalOp){
					LogicalOp op = (LogicalOp) n;
					next.add(op.left);
					next.add(op.right);
				}
			}
		}
		if (!next.isEmpty()){
			printTree(next);
		}
	}
	
	public String toString(){
		return String.format("%s", root);
	}

	private abstract class Node{
		String operator;
		boolean negate;
		
		abstract void setType(ConditionalValidator validator) throws RuntimeException;

		abstract boolean eval(ConditionalValidator validator) throws RuntimeException;
		
		abstract Node valSub(HashMap<String, String>rowValues);
		
		abstract public String printNode();
	}

	//Inner Node
	private class LogicalOp extends Node{
		int loc;
		Node left;
		Node right;

		protected LogicalOp(String operator, boolean negate, int loc){
			this.loc = loc;
			this.operator = operator;
			this.negate = negate;
		}
		
		void setType(ConditionalValidator validator) throws RuntimeException{
			left.setType(validator);
			right.setType(validator);
		}

		boolean eval(ConditionalValidator validator) throws RuntimeException{
			if (validator instanceof TypeValidator){
				return left.eval(validator)&& right.eval(validator);
			}else{
				if (operator.equalsIgnoreCase(ValidationConstants.CONSTRAINT_AND)){
					if (negate){
						return !(left.eval(validator) && right.eval(validator));
					}
					return left.eval(validator) && right.eval(validator); 
				}else if (operator.equalsIgnoreCase(ValidationConstants.CONSTRAINT_OR)){
					if (negate){
						return !(left.eval(validator) || right.eval(validator)); 
					}
					return left.eval(validator) || right.eval(validator); 
				}else{
					//should not be possible 
					return false;
				}
			}
		}

		Node valSub(HashMap<String, String> rowValues) throws RuntimeException{
			LogicalOp node = new LogicalOp(this.operator, this.negate, this.loc);
			node.left = left.valSub(rowValues);
			node.right = right.valSub(rowValues);
			return node;
		}
		
		public String printNode(){
			String output = "";
			if (negate){
				output =  "(!)";
			}
			output = output + operator;
			return output;
		}
		
		public String toString(){
			String output = "";
			if (negate){
				output = output + "!(";
			}
			output = output + right.toString();
			output = output + " " + operator + " ";
			output = output + left.toString();
			if (negate){
				output = output + ")";
			}
			return output;
		}
		
		
	}

	//Leaf Node 
	private class Constraint extends Node{
		int loc;
		String rowRef;
		Vector<String> caseValues;
		String type;

		protected Constraint(String operator, String rowRef, Vector<String> caseValues, boolean negate, int loc){
			this.loc = loc;
			this.operator = operator;
			this.rowRef = rowRef;
			this.caseValues = caseValues;
			this.negate = negate;
		}
		
		void setType(ConditionalValidator validator) throws RuntimeException{
			try{
				type = validator.getConstraintType(rowRef);
			}catch(RuntimeException e){
				throw new RuntimeException("type could not be determined for " + rowRef + " because " + e.getMessage()); 
			}
		}

		boolean eval(ConditionalValidator validator) throws RuntimeException{
			if (negate && !(validator instanceof TypeValidator)){
				return !(validator.validateConstraint(operator, rowRef, caseValues, type, null));
			}
			return validator.validateConstraint(operator, rowRef, caseValues, type, null);
		}

		Node valSub(HashMap<String, String> rowValues) throws RuntimeException{
			String refSub;
			Vector<String> caseSub = new Vector<String>();
			
			//rowRef
			String element = rowRef.split("\\" + ValidationConstants.VALUE_REFERENCE_DIVIDER)[1];
			if (rowValues.containsKey(element)&& !(rowValues.get(element)).isEmpty()){
				refSub = rowValues.get(element);
			}else{
				throw new RuntimeException("value subsitution could not occur because " + element + " - No such data element found.");
			}
			
			//values
			for(String value : caseValues){
				if (ValidationUtil.isRowRef(value)){
					element = value.split("\\" + ValidationConstants.VALUE_REFERENCE_DIVIDER)[1];
					if (rowValues.containsKey(element) && !(rowValues.get(element)).isEmpty()){
						caseSub.add(rowValues.get(element));
					}else{
						throw new RuntimeException("Value subsitution could not occur because " + element + " - No such data element found.");
					}
				}else{
					caseSub.add(value);
				}
			}
			
			Constraint node = new Constraint(this.operator, refSub, caseSub, this.negate, this.loc);
			node.type = this.type;
			return node;
		}
		
		public String printNode(){
			return toString();
		}
		
		public String toString(){
			return rowRef + " " + operator + " " + caseValues.toString();
		}
	}
}
