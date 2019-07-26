package gov.nih.tbi.idt.ws;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class IdtInterface {
	private static final Logger logger = Logger.getLogger(IdtInterface.class);

	private ArrayList list;
	private IdtDecorator decoratorStack = null;
	
	protected IdtRequest request;
	
	private int totalRecordCount;		// total records before any filtering
	private int filteredRecordCount;	// total records after any filtering
	
	public IdtInterface() {
		request = new IdtRequest();
	}
	
	public IdtInterface(ArrayList<?> lst) {
		this();
		list = lst;
	}
	
	/**
	 * Decorates the primary list 
	 * 
	 * @param IdtDecorator dec.  New class to add to the decorator stack
	 */
	public void decorate(IdtDecorator decorator) {
		decorator.init(list);
		if (decoratorStack != null) {
			decorator.addDecorated(decoratorStack);
		}
		decoratorStack = decorator;
	}
	
	/**
	 * Looks up a property on a given object.  Checks the decorator stack first, then
	 * the object itself.  This is used for per-row parameter lookup while compiling data.
	 * 
	 * @param obj object to look in after checking decorators
	 * @param property string parameter name (can be dot-notation path)
	 * @return Object result of the lookup.  toString() will likely be called on this result
	 */
	public Object lookup(Object obj, String property) {
		Object output = null;
		
		if (property != null) {
			// first try the decorators
			try {
				if (decoratorStack != null) {
					output = PropertyUtils.getProperty(decoratorStack, property);
				}
			} catch (IllegalAccessException e) {
				logger.error("access error during lookup for decorator on property " + property + "and  object " + obj.getClass().getName());
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				logger.error("error invoking lookup for decorator on property " + property + "and  object " + obj.getClass().getName());
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// ignore and fall through to the object property lookup below
			}
			
			// if not found, try on the object itself
			if (output == null) {
				try {
					output = PropertyUtils.getProperty(obj, property);
				} catch (IllegalAccessException e) {
					logger.error("access error during lookup on property " + property + "and  object " + obj.getClass().getName());
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					logger.error("error invoking lookup on property " + property + "and  object " + obj.getClass().getName());
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// ignore and fall through to the handler below
				}
			}
		}
		
		if (output == null) {
			output = "";
		}
		
		return output;
	}
	
	public String toJson() {
		JsonObject data = compileData();
		return data.toString();
	}
	
	public JsonObject toJsonObject() {
		return compileData();
	}
	
	public String output() {
		// must be implemented by a concrete stream handler in a subclass if needed
		// see Struts2IdtInterface for an example
		return toJson();
	}
	
	private JsonPrimitive rowLookup(Object row, String property, int index) {
		if (decoratorStack != null) {
			decoratorStack.initRow(row, index);
		}
		return new JsonPrimitive(lookup(row, property).toString());
	}
	
	/**
	 * Compiles the data from mainList and decorators with the column descriptors
	 * to return the proper data back to the datatable.
	 * 
	 * Return Parameters:
	 * draw
	 * recordsTotal
	 * recordsFiltered
	 * data : {
	 * 		<data object properties defined by columnDescriptions>
	 * }
	 * 
	 * @return JsonObject result object (see method description)
	 */
	protected JsonObject compileData() {
		JsonObject output = new JsonObject();
		output.add("draw", new JsonPrimitive(request.getDraw()));
		output.add("recordsTotal", new JsonPrimitive(totalRecordCount));
		output.add("recordsFiltered", new JsonPrimitive(filteredRecordCount));
		
		JsonArray data = new JsonArray();
		int listSize = list.size();
		// make sure we have rows and columns to work with.  Otherwise, nothing will make sense
		if (listSize > 0 && totalRecordCount > 0 && request.getColumnDescriptions().size() > 0) {
			for (int i = 0; i < listSize; i++) {
				Object row = list.get(i);
				
				JsonObject rowOutput = new JsonObject();
				rowOutput.add("DT_RowId", rowLookup(row, request.getPrimaryKey(), i));
				
				Iterator<IdtColumnDescriptor> columnIterator = request.getColumnDescriptions().iterator();
				while (columnIterator.hasNext()) {
					IdtColumnDescriptor column = columnIterator.next();
					// reminder: rowLookup looks in the main list AND any decorators
					rowOutput.add(column.getData(), rowLookup(row,column.getPropertyPath(), i));
				}
				
				data.add(rowOutput);
			}
		}
		
		output.add("data", data);
		return output;
	}
	
	protected void addArrayTypeElement(JsonObject finalOut, String key, String value) {
		String[] keySet = request.splitKey(key);
		String keyName = keySet[0];
		int keyIndex = Integer.parseInt(keySet[1]);
		String keySubName = keySet[2];

		JsonElement outputElement = finalOut.get(keyName);
		if (outputElement != null && outputElement.isJsonArray()) {
			JsonArray element = finalOut.get(keyName).getAsJsonArray();
			// does the array already contain the index we're working with?
			if (element.size() > keyIndex) {
				JsonObject subElement = element.get(keyIndex).getAsJsonObject();
				subElement.add(keySubName, new JsonPrimitive(value));
				// modifying in place so no need to re-save
			} else {
				// the element has not yet been created in the array
				JsonObject subElement = new JsonObject();
				subElement.add(keySubName, new JsonPrimitive(value));
				element.add(subElement);
				finalOut.add(keyName, element);
			}
		} else {
			// element not already in output or it's not an array, so write as an array
			// in case it was originally not an array... well we overwrite it
			JsonArray newSet = new JsonArray();
			JsonObject newElement = new JsonObject();
			newElement.add(keySubName, new JsonPrimitive(value));
			newSet.add(newElement);
			finalOut.add(keyName, newSet);
		}
	}

	protected void addObjectTypeElement(JsonObject finalOut, String key, String value) {
		String[] keySet = request.splitKey(key);
		String keyName = keySet[0];
		String paramName = keySet[1];

		JsonElement outputElement = finalOut.get(keyName);
		if (outputElement == null) {
			outputElement = new JsonObject();
			finalOut.add(keyName, outputElement);
		}
		JsonObject outputObj = (JsonObject) outputElement;
		outputObj.add(paramName, new JsonPrimitive(value));
	}

	protected void addKeyValueElement(JsonObject finalOut, String key, String value) {
		finalOut.add(key, new JsonPrimitive(value));
	}


	public int getTotalRecordCount() {
		return totalRecordCount;
	}

	public void setTotalRecordCount(int totalRecordCount) {
		this.totalRecordCount = totalRecordCount;
	}

	public int getFilteredRecordCount() {
		return filteredRecordCount;
	}

	public void setFilteredRecordCount(int filteredRecordCount) {
		this.filteredRecordCount = filteredRecordCount;
	}

	public List<?> getList() {
		return list;
	}

	public void setList(ArrayList<?> list) {
		this.list = list;
	}

	public IdtRequest getRequest() {
		return request;
	}
}
