package gov.nih.tbi.pojo;

import java.io.Serializable;
import java.util.List;

public class DeSelectSearch implements Serializable {
	
	private static final long serialVersionUID = 6668252922260267354L;
	
	String searchPhrase = "";
	String wholeWordSearch = "";
	List<String> elementTypes = null;
	List<String> diseases = null;
	List<String> populations = null;
	List<String> searchLocations = null;
	String pageOffset = "0";
	int sortColumnIndex = 0;
	String sortDirection = "asc";
	int countPerPage = 10;
	int sEcho = 1;

	public List<String> getSearchLocations() {
		return searchLocations;
	}

	public void setSearchLocations(List<String> searchLocations) {
		this.searchLocations = searchLocations;
	}

	public String getSearchPhrase() {
		return searchPhrase;
	}

	public void setSearchPhrase(String searchPhrase) {
		this.searchPhrase = searchPhrase;
	}

	public String getWholeWordSearch() {
		return wholeWordSearch;
	}

	public void setWholeWordSearch(String wholeWordSearch) {
		this.wholeWordSearch = wholeWordSearch;
	}

	public List<String> getElementTypes() {
		return elementTypes;
	}

	public void setElementTypes(List<String> elementTypes) {
		this.elementTypes = elementTypes;
	}

	public List<String> getDiseases() {
		return diseases;
	}

	public void setDiseases(List<String> diseases) {
		this.diseases = diseases;
	}

	public List<String> getPopulations() {
		return populations;
	}

	public void setPopulations(List<String> populations) {
		this.populations = populations;
	}

	public int getSortColumnIndex() {
		return sortColumnIndex;
	}

	public void setSortColumnIndex(int sortColumnIndex) {
		this.sortColumnIndex = sortColumnIndex;
	}

	public String getSortDirection() {
		return sortDirection.toUpperCase();
	}

	public void setSortDirection(String sortDirection) {
		this.sortDirection = sortDirection;
	}

	public int getCountPerPage() {
		return countPerPage;
	}

	public void setCountPerPage(int countPerPage) {
		this.countPerPage = countPerPage;
	}

	public int getsEcho() {
		return sEcho;
	}

	public void setsEcho(int sEcho) {
		this.sEcho = sEcho;
	}

	public String getPageOffset() {
		return pageOffset;
	}

	public void setPageOffset(String pageOffset) {
		this.pageOffset = pageOffset;
	}
}
