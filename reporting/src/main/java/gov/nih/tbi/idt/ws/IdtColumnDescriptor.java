package gov.nih.tbi.idt.ws;

public class IdtColumnDescriptor {
	private int index;
	private String data;
	private String propertyPath;
	private String name;
	private boolean searchable;
	private boolean orderable;
	// null if not sorting on this column, "asc" if ascending, "desc" if descending
	private String orderDirection;
	private String searchValue;
	private boolean searchRegex;
	
	public IdtColumnDescriptor() {
		index = 0;
		data = null;
		propertyPath = null;
		name = null;
		searchable = false;
		orderable = true;
		orderDirection = null;
		searchValue = null;
		searchRegex = false;
	}	
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getPropertyPath() {
		return propertyPath;
	}
	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isSearchable() {
		return searchable;
	}
	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}
	public boolean isOrderable() {
		return orderable;
	}
	public void setOrderable(boolean orderable) {
		this.orderable = orderable;
	}
	public String getOrderDirection() {
		return orderDirection;
	}
	public void setOrderDirection(String orderDirection) {
		this.orderDirection = orderDirection;
	}
	public String getSearchValue() {
		return searchValue;
	}
	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}
	public boolean isSearchRegex() {
		return searchRegex;
	}
	public void setSearchRegex(boolean searchRegex) {
		this.searchRegex = searchRegex;
	}
}
