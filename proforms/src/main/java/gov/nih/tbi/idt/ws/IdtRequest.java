package gov.nih.tbi.idt.ws;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class IdtRequest {
	private int draw;
	private int start;
	private int length;
	private int requestLength;
	private int pageNumber;
	private int pageLength;

	private String searchVal;
	private boolean searchIsRegex;
	private String primaryKey;
	
	private Integer orderColumn;
	private boolean ascending;

	private List<IdtColumnDescriptor> columnDescriptions;
	private List<IdtFilterDescription> filters;
	
	public static final String ORDER_ASCENDING = "asc";
	public static final String ORDER_DESCENDING = "desc";

	/**
	 * Loads the parameters that we need from the IDTables plugin to be able to perform the parameter mapping and
	 * querying needed here.
	 * 
	 * @param parameterMap Struts parameter map giving <parameter name, Object> format
	 * @throws InvalidColumnException If column configuration doesn't contain required information
	 */
	protected void loadParams(JsonObject parameterMap) throws InvalidColumnException {
		//@formatter:off
		/* Parameters list:
		 * @see https://datatables.net/manual/server-side
		 * NOTE: i refers to a column index.  Likely all columns will be referenced if any are
		 * 
		 * DATATABLES VALUES
		 * draw - 						integer -	Draw counter.  Orders responses back to datatables (must be cast to an integer)
		 * start - 						integer - 	Paging first record indicator. Start point in the current data set (0 - indexed)
		 * length - 					integer - 	Number of records that the table can display in the current draw
		 * search[value] - 				string 	- 	global search value
		 * search[regex] - 				boolean - 	true if the global filter should be treated as a regex.  otherwise false
		 * order[i][column] - 			integer - 	Column index to which ordering should be applied
		 * order[i][dir] - 				string 	- 	order direction.  "asc" | "desc"
		 * columns[i][data] - 			string 	- 	column's data source @see https://datatables.net/reference/option/columns.data
		 * columns[i][name] - 			string 	- 	column's name @see https://datatables.net/reference/option/columns.name
		 * columns[i][searchable] - 	boolean - 	flag to indicate if column is searchable (true) or not (false)
		 * columns[i][orderable] - 		boolean - 	flag to indicate if column is orderable (true) or not (false)
		 * columns[i][search][value] - 	string 	- 	search value to apply to a specific column
		 * columns[i][search][regex] - 	boolean - 	flag to indicate if search term for this column should be treated as a regex
		 * 
		 * IDT VALUES
		 * columns[i][parameter] -		string	-	property path of the java value to fill in this location. ex: "user.username"
		 * filter[i][name] -			string	-	name of the server-side filter to activate
		 * filter[i][value] - 			string	-	value to apply to the corresponding filter
		 * idtData[primaryKey] -		string	- 	property path of the row Identifier (DT_RowId)
		 * requestLength				int		-	length of results to send back (cached length)
		 * sLength						string  -	value: "All" if present.  identifies that the query should get ALL results
		 * pageLength					int		-	length of a single page of displayed data
		 */
		//@formatter:on
		
		this.draw = paramToInt(parameterMap, "draw", 1);
		this.start = paramToInt(parameterMap, "start", 0);
		this.length = paramToInt(parameterMap, "length", 15);
		this.pageLength = paramToInt(parameterMap, "pageLength", 15);
		
		if (parameterMap.has("sLength")) {
			this.length = 0;
		}
		
		if (length > 0) {
			pageNumber = Math.floorDiv(start, pageLength) + 1;
		} else {
			pageNumber = 1;
		}

		loadPrimaryKey(parameterMap);
		loadSearch(parameterMap);
		loadOrder(parameterMap);
		loadColumns(parameterMap);
		loadFilters(parameterMap);
	}
	
	private void loadPrimaryKey(JsonObject parameterMap) {
		searchVal = "";
		searchIsRegex = false;
		if (parameterMap.has("obj")) {
			JsonObject obj = parameterMap.getAsJsonObject("obj");
			if (obj.has("primaryKey")) {
				primaryKey = obj.get("primaryKey").getAsString();
			}
		}
	}
	
	private void loadSearch(JsonObject parameterMap) {
		searchVal = "";
		searchIsRegex = false;
		if (parameterMap.has("search")) {
			JsonObject search = parameterMap.getAsJsonObject("search");
			if (search.has("value")) {
				searchVal = search.get("value").getAsString();
			}
			if (search.has("regex")) {
				searchIsRegex = search.get("regex").getAsBoolean();
			}
		}
	}
	
	private void loadOrder(JsonObject parameterMap) {
		// note: for now, we are only supporting single-column ordering.  This will need to
		// be updated to allow for multi-column ordering (but it wouldn't be that hard)
		if (parameterMap.has("order")) {
			JsonArray orderArray = parameterMap.get("order").getAsJsonArray();
			// it is guaranteed that, if the array exists here, it has elements
			// NOTE: if we want multi-column ordering, we need to change this and the structure it stores into
			JsonObject order = orderArray.get(0).getAsJsonObject();
			if (order != null) {
				orderColumn = order.get("column").getAsInt();
				// the reason for the inversion here is because ASC is default so the ELSE should match that
				ascending = !order.get("dir").getAsString().equalsIgnoreCase(IdtRequest.ORDER_DESCENDING);
			}
		}
	}
	
	private void loadColumns(JsonObject parameterMap) throws InvalidColumnException {
		columnDescriptions = new ArrayList<IdtColumnDescriptor>();
		if (parameterMap.has("columns")) {
			JsonArray columns = parameterMap.getAsJsonArray("columns");
			int columnsSize = columns.size();
			for (int i = 0; i < columnsSize; i++) {
				// I would have preferred to put all this configuration in IdtColumnDescriptor
				// however, we need access to ascending, orderColumn, 
				JsonObject column = columns.get(i).getAsJsonObject();
				//columnDescriptions.add(new IdtColumnDescriptor(i, column));
				IdtColumnDescriptor columnDesc = new IdtColumnDescriptor();
				columnDesc.setIndex(i);
				columnDesc.setData(IdtRequest.paramToString(column, "data", null));
				if (columnDesc.getData() == null) {
					throw new InvalidColumnException("Required column configuration \"data\" is missing");
				}
				columnDesc.setPropertyPath(IdtRequest.paramToString(column, "parameter", null));
				if (columnDesc.getPropertyPath() == null) {
					throw new InvalidColumnException("Required column configuration \"parameter\" is missing");
				}
				
				columnDesc.setName(IdtRequest.paramToString(column, "name", ""));
				columnDesc.setSearchable(IdtRequest.paramToBool(column, "searchable", false));
				columnDesc.setOrderable(IdtRequest.paramToBool(column, "orderable", true));
				if (orderColumn != null && orderColumn == i) {
					columnDesc.setOrderDirection(ascending ? IdtRequest.ORDER_ASCENDING : IdtRequest.ORDER_DESCENDING);
				}
				// searchValue unused right now because we aren't yet doing per-column searching
				// searchRegex unused right now because we aren't yet doing per-column searching
				
				columnDescriptions.add(columnDesc);
			}
		}
	}
	
	private void loadFilters(JsonObject parameterMap) {
		filters = new ArrayList<IdtFilterDescription>();
		if (parameterMap.has("filter")) {
			JsonArray filterSet = parameterMap.getAsJsonArray("filter");
			int numFilters = filterSet.size();
			for (int i = 0; i < numFilters; i++) {
				JsonObject filter = filterSet.get(i).getAsJsonObject();
				IdtFilterDescription idtFilter = new IdtFilterDescription();
				idtFilter.setName(IdtRequest.paramToString(filter, "name", ""));
				idtFilter.setValue(IdtRequest.paramToString(filter, "value", ""));
				if (!idtFilter.getName().equals("")) {
					filters.add(idtFilter);
				}
			}
		}
	}
	
	public static String paramToString(JsonObject paramMap, String paramName, String def) {
		String output = def;
		if (paramMap.has(paramName)) {
			output = paramMap.get(paramName).getAsString();
		}
		return output;
	}
	
	public static int paramToInt(JsonObject paramMap, String paramName, int def) {
		int output = def;
		try {
			if (paramMap.has(paramName)) {
				output = Integer.valueOf(paramMap.get(paramName).getAsString());
			}
		}
		catch(NumberFormatException e) {
			// do nothing because output is already set to default
		}
		return output;
	}
	
	public static boolean paramToBool(JsonObject paramMap, String paramName, boolean def) {
		boolean output = def;
		try {
			if (paramMap.has(paramName)) {
				output = paramMap.get(paramName).getAsBoolean();
			}
		}
		catch(NumberFormatException e) {
			// do nothing because output is already set to default
		}
		return output;
	}
	
	public String[] splitKey(String key) {
		if (key.contains("[")) {
			String tempKey = key.replaceAll("\\]", "");
			String[] keySet = tempKey.split("\\[");
			return keySet;
		}
		else {
			return new String[] {key};
		}
	}
	
	public boolean isValidArrayTypeKey(String key) {
		String[] keySet = splitKey(key);
		return keySet.length == 3;
	}
	
	public boolean isValidObjectTypeKey(String key) {
		String[] keySet = splitKey(key);
		return keySet.length == 2;
	}
	
	public int getDraw() {
		return draw;
	}
	
	public void setDraw(int draw) {
		this.draw = draw;
	}
	
	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getSearchVal() {
		return searchVal;
	}

	public void setSearchVal(String searchVal) {
		this.searchVal = searchVal;
	}

	public boolean isSearchIsRegex() {
		return searchIsRegex;
	}

	public void setSearchIsRegex(boolean searchIsRegex) {
		this.searchIsRegex = searchIsRegex;
	}

	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKey) {
		this.primaryKey = primaryKey;
	}

	public List<IdtColumnDescriptor> getColumnDescriptions() {
		return columnDescriptions;
	}

	public void setColumnDescriptions(List<IdtColumnDescriptor> columnDescriptions) {
		this.columnDescriptions = columnDescriptions;
	}
	
	public IdtColumnDescriptor getColumnByData(String data) {
		for (IdtColumnDescriptor icd : columnDescriptions) {
			if (icd.getData().equals(data)) {
				return icd;
			}
		}
		return null;
	}

	public int getFinalIndex() {
		return start + length;
	}
	
	public int getRequestLength() {
		return requestLength;
	}

	public List<IdtFilterDescription> getFilters() {
		return filters;
	}

	public int getPageLength() {
		return pageLength;
	}

	public IdtColumnDescriptor getOrderColumn() {
		IdtColumnDescriptor output = null;
		if (orderColumn != null) {
			output = columnDescriptions.get(orderColumn);
		}
		return output;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}
}
