package gov.nih.tbi.pojo;

import gov.nih.tbi.filter.PermissibleValueFilter;
import gov.nih.tbi.filter.ChangeInDiagnosisFilter;
import gov.nih.tbi.filter.RangedNumericFilter;
import gov.nih.tbi.filter.DateFilter;
import gov.nih.tbi.filter.DelimitedMultiSelectFilter;
import gov.nih.tbi.filter.Filter;
import gov.nih.tbi.filter.FreeFormFilter;

public enum FilterType {
	PERMISSIBLE_VALUE(PermissibleValueFilter.class), CHANGE_IN_DIAGNOSIS(ChangeInDiagnosisFilter.class),
	RANGED_NUMERIC(RangedNumericFilter.class), DATE(DateFilter.class),
	DELIMITED_MULTI_SELECT(DelimitedMultiSelectFilter.class), FREE_FORM(FreeFormFilter.class);

	private Class<? extends Filter> filterClass;

	public Class<? extends Filter> getFilterClass() {
		return filterClass;
	}


	public void setFilterClass(Class<? extends Filter> filterClass) {
		this.filterClass = filterClass;
	}

	FilterType(Class<? extends Filter> filterClass) {
		this.filterClass = filterClass;
	}
}
