package gov.nih.tbi.service.model;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.service.cache.InstancedDataCache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Represents the Data Cart object saved in server session. NOTE: Keep it lightweight!!!
 * 
 * @author jim3
 */

@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "session")
public class DataCart implements Serializable {

	private static final long serialVersionUID = -2593432429724605910L;

	private List<String> selectedFormUris = new ArrayList<String>();

	private Map<String, FormResult> formsInCart = new HashMap<String, FormResult>();

	private boolean metasearchVisible = true;

	private int rowsToDisplay = QueryToolConstants.DEFAULT_ROWS_TO_DISPLAY;

	private List<String> activeCriteriaTabs = new ArrayList<String>();

	private InstancedDataTable instancedDataTable;

	private CodeMapping codeMapping; // Maybe this can be removed, the only time we need it again is when expanding
									 // RG

	private InstancedDataCache instancedDataCache = new InstancedDataCache();

	public List<String> getSelectedFormUris() {
		return selectedFormUris;
	}

	public void setSelectedFormUris(List<String> selectedFormUris) {
		if (this.selectedFormUris == null) {
			this.selectedFormUris = new ArrayList<>();
		}

		List<String> synchronizedList = Collections.synchronizedList(this.selectedFormUris);

		synchronized (synchronizedList) {
			synchronizedList.clear();
			if (selectedFormUris != null) {
				synchronizedList.addAll(selectedFormUris);
			}
		}
	}

	public Map<String, FormResult> getFormsInCart() {
		return formsInCart;
	}

	public void setFormsInCart(Map<String, FormResult> formsInCart) {
		if (formsInCart != null) {
			this.formsInCart = formsInCart;
		} else {
			this.formsInCart.clear();
		}
	}

	public boolean isMetasearchVisible() {
		return metasearchVisible;
	}

	public void setMetasearchVisible(boolean metasearchVisible) {
		this.metasearchVisible = metasearchVisible;
	}

	public int getRowsToDisplay() {
		return rowsToDisplay;
	}

	public void setRowsToDisplay(int rowsToDisplay) {
		this.rowsToDisplay = rowsToDisplay;
	}

	public List<String> getActiveCriteriaTabs() {
		return activeCriteriaTabs;
	}

	public void setActiveCriteriaTabs(List<String> activeCriteriaTabs) {
		if (activeCriteriaTabs != null) {
			this.activeCriteriaTabs = activeCriteriaTabs;
		} else {
			this.activeCriteriaTabs.clear();
		}
	}

	public InstancedDataTable getInstancedDataTable() {
		return instancedDataTable;
	}

	public void setInstancedDataTable(InstancedDataTable instancedDataTable) {
		this.instancedDataTable = instancedDataTable;
	}

	public CodeMapping getCodeMapping() {
		return codeMapping;
	}

	public void setCodeMapping(CodeMapping codeMapping) {
		this.codeMapping = codeMapping;
	}

	public InstancedDataCache getInstancedDataCache() {
		return instancedDataCache;
	}

	public void setInstancedDataCache(InstancedDataCache instancedDataCache) {
		this.instancedDataCache = instancedDataCache;
	}

	public void setDisplayOption(String displayOption) {
		if (instancedDataTable != null) {
			instancedDataTable.setDisplayOption(displayOption);
		}
	}

	public synchronized void clearSelectedForms() {
		instancedDataCache.clear();

		// Clear any data table repeatable groups expand trackers and update the row
		// count.
		if (instancedDataTable != null) {
			instancedDataTable.clear();
		}
	}

	public FormResult getFormFromCart(String formUri) {
		return formsInCart.get(formUri);
	}

	public List<FormResult> getSelectedForms() {

		List<FormResult> selectedForms = new ArrayList<FormResult>();

		for (String formUri : selectedFormUris) {
			FormResult form = formsInCart.get(formUri);
			if (form != null) {
				selectedForms.add(form);
			}
		}

		return selectedForms;
	}

	public void reset() {
		selectedFormUris.clear();
		removeAllFilters();
		formsInCart.clear();
		activeCriteriaTabs.clear();
		instancedDataCache.clear();

		if (codeMapping != null && codeMapping.getDeValueRangeMap() != null) {
			codeMapping.getDeValueRangeMap().clear();
			codeMapping = null;
		}

		if (instancedDataTable != null) {
			instancedDataTable.clear();
		}
	}

	public void removeAllFilters() {
		Map<String, FormResult> allForms = getFormsInCart();
		for (Map.Entry<String, FormResult> entry : allForms.entrySet()) {
			FormResult form = entry.getValue();
			form.getFilters().clear();
		}
	}

}
