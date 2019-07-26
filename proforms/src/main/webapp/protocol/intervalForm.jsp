
<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.StrutsConstants" %>
<%@ page import="gov.nih.nichd.ctdb.form.common.FormConstants" %>
<%@ page import="gov.nih.nichd.ctdb.common.LookupSessionKeys" %>
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.tbi.dictionary.model.hibernate.DataElement"%>
<%@ page import="java.util.Map"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.*"%>

<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="addeditvisittypes" />

<html>



<% Locale l = request.getLocale();
Map<String, DataElement> deCache;
ArrayList<String> deNamesCache = new ArrayList<String>();
if (session.getAttribute(CtdbConstants.PRE_POP_DE_CACHE_KEY) != null) {
	deCache = (Map) session.getAttribute(CtdbConstants.PRE_POP_DE_CACHE_KEY);
	Set<String> keys = deCache.keySet();
	Iterator iter = keys.iterator();
	while(iter.hasNext()) {
		String key = (String)iter.next();
		DataElement de = deCache.get(key);
		String deName = de.getName();
		deNamesCache.add(deName);
	}
}




%>

<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	<s:text name="protocol.visitType.create.title.display" />
</s:set>
<jsp:include page="/common/header_struts2.jsp" />

<jsp:include page="/protocol/templates/prePopDeCheckBoxes.jsp" />

<script type="text/javascript">
	var webRoot = "<s:property value='#webRoot'/>";
	var prePopDeChkBxTemplate = Handlebars.compile($("#prePopDeCheckBoxTemplate").html());
	var selectedForms = new HashTable();
	var canInitSelection = false;
	var canToggleSelection = true;
	var initSelected = false;
	var prePopDeTable = new HashTable();
	// added by Ching-Heng
	var anyPromisForm = [];
	var hasSigned = false;
	
	/**
	 * Form object constructor.
	 */
	function Form(shortName, name, isManitory, orderVal, selfReport, iscat) {
		this.shortName = shortName;
		this.name = name;
		this.isManitory = isManitory;
		this.orderVal = orderVal;
		this.isSelfReport = selfReport;
		this.iscat = iscat; //added by Ching-Heng
	}
	
	/** 
	 * Determine if a set of radio buttons is disabled or not. If the radio buttons are not disabled, disable them
	 * and reset the default state. Otherwise enable the radio buttons.
	 * 
	 * @param $radioBtns - JQuery reference to the radio buttons to be altered.
	 */
	function toggleRadioBtns($radioBtns) {
		
		// If the associated radio buttons are disabled enable them, and if they are enabled disable them.
		if ( !$radioBtns.is(":disabled") ) {
			// Reset the radio buttons back to the default values
			$radioBtns.each(function(indx, element) {
				if ( $(this).val() == "true" ) {
					$(this).prop("checked", false);
				}
				else {
					$(this).prop("checked", true);
				}
			});
			
			$radioBtns.prop("disabled", true);
		}
		else {
			$radioBtns.prop("disabled", false);
		}
	}
	
	/**
	 * Listener for the select and deselect row and checkbox events. If the assoicated checkbox can be found
	 * on the page, its selection will be either be added or removed (a toggle action) from the hash map 
	 * and form order box.
	 * 
	 * @param saveVal - A string containing the value of the checkbox on the affected row.
	 * @param this - Set to the default setting.
	 */
	function selectionChangeListener(saveVal) {
		if ( canToggleSelection ) {			
			var shortName = saveVal;
			var $checkBox = $("input:checkbox[name='formShortNameCheckbox_" + shortName + "']");
			
			// Check for the existance of the checkbox on the page before toggling.
			if ( $checkBox.length != 0 ) {
				var $radioBtns = $("input:radio[name='isMandatory_" + shortName + "']");
				var $selfReportRadio = $("input:radio[name='isSelfReport_" + shortName + "']");
				toggleRadioBtns($radioBtns);
				toggleRadioBtns($selfReportRadio);
				
				// If the check box is part of the IDT selection, add it to the hash map and form order box.
				if ( IDT.isSelectedOption($checkBox, saveVal) ) {
					var formName = $checkBox.parent().next().next().text();
					var manditory = $radioBtns.filter(":checked").val();
					var selfReport = $selfReportRadio.filter(":checked").val();
					//added by Ching-Heng
					var iscat = $checkBox.attr('iscat');					
					if(iscat == 'true'){
						if(!hasSigned){
							showPromisAgeement($checkBox, formName);
						}
						anyPromisForm.push(iscat);
					}					
					//=======================
					var cForm = new Form(shortName, formName, manditory, 0, selfReport, iscat);
					var $formOrderBx = $("#formOrderBox");
					
					selectedForms.setItem(shortName, cForm);
					
					// Add entry in the form order box.
					if ( $formOrderBx.children("#orderForm_" + shortName).length == 0) {
						$formOrderBx.append('<option id ="orderForm_' + shortName + '" value="' + shortName + '">' + formName + '</option>');
					}
					
					// Show the pre-population data element checkboxes.
					showPrePopCheckboxes($("#fieldAutopopulationDiv"));
				}
				else {
					// Remove the form from the hash map and form order box.
					selectedForms.removeItem(shortName);
					$("#orderForm_" + shortName).remove();
					anyPromisForm.pop();
					// Clear the pre-population checkboxes, if needed.
					if ( selectedForms.isEmpty() ) {
						clearPrepopDECheckbox();
					}
				}				
				//show promis msg
				if(anyPromisForm.length > 0){					
					$('#promisformAddingMsg').show();		
				}else{
					$('#promisformAddingMsg').hide();
				}
			}else{
				anyPromisForm = [];
				$('#promisformAddingMsg').hide();
			}
		}
	}
	
	/**
	 * Callback for the table clear or de-select all event. The hash map and order box will be 
	 * cleared, and the table will re-sync.
	 *
	 * @param dataTable - The DataTable model.
	 * @param this - Set to the table DOM element of the forms table.
	 */
	function deSelectAllListener(dataTable) {
		var $table = $(this);
		
		selectedForms.clear();
		clearPrepopDECheckbox();
		$("#formOrderBox").empty();
		clearPrepopDECheckbox();
		// added by Ching-Heng
		anyPromisForm = [];
		$('#promisformAddingMsg').hide();
		// Re-init the form selection if needed
		if ( canInitSelection ) {
			initSelectedForms();
			initPrePopDeCheckBoxes(JSON.parse($("#prepopDEStrList").val()));
			canInitSelection = false;
		}
		
		reFreshTable($table);
	}
	
	/**
	 * Callback for the select all and selected filtered events.
	 *
	 * @param dataTable - The DataTable model.
	 * @param this - Set to the table DOM element of the forms table.
	 */
	function selectAllListener(dataTable) {
		var $formOrderBx = $("#formOrderBox");
		 
		var table = $("#formDisplayTableRef table").DataTable();
		
		// Loop through the selected rows and add any new selections to the hash map and form order box.
		table.rows( { page: 'all', search: 'applied'} ).data().each(function(value, index ) {
			var checkbox = $(value[0]); 
			var shortName = checkbox.val();
			var name = value[2];
			var iscat = checkbox.attr('iscat');
			if(iscat == 'true'){
				anyPromisForm.push(iscat);
				if(!hasSigned){
					showPromisAgeementForAll(dataTable);
				}
			}			
			// Check if the form needs to be added to the hash map and form order box.
			if ( !selectedForms.hasItem(shortName) ) {
				
				selectedForms.setItem(shortName, new Form(shortName, name, "false", 0, "false", iscat)); // added by Ching-Heng
				$formOrderBx.append('<option id ="orderForm_' + shortName + '" value="' + shortName + '">' + name + '</option>');			
			}
		});
		
		//show promis msg
		if(anyPromisForm.length > 0){
			$('#promisformAddingMsg').show();			
		}else{
			$('#promisformAddingMsg').hide();
		}
		
		// Reset the IBIS Data Table
		reFreshTable($(this));
	}
	
	/**
	 * Called when any of the form table radio buttons are clicked. Updates the value stored as part of the
	 * form selection hash map.
	 * 
	 * @param shortName - The ID for the form associated with the radio button.
	 * @param value - The value of the selected radio button.
	 */
	function processRadioBtn(shortName, value) {
		var form = selectedForms.getItem(shortName);
		
		form.isManitory = value;
		selectedForms.setItem(shortName, form);
	}
	
	/**
	 * Called when any of the form table self-reporting radio buttons are clicked. 
	 * Updates the value stored as part of the form selection hash map.
	 * 
	 * @param shortName - The ID for the form associated with the radio button.
	 * @param value - The value of the selected radio button.
	 */
	function processSelfReportRadioBtn(shortName, value) {
		var form = selectedForms.getItem(shortName);
		
		form.isSelfReport = value;
		selectedForms.setItem(shortName, form);
	}
	
	/** 
	 * Reads all of the selected forms from the hash table and populates the visable elements of the form
	 * table with the selections stored in the hash table.
	 * 
	 * @param $table - JQuery reference to the form table.
	 */
	function populateCheckBoxes($table) {
		var idList = selectedForms.keys();
		var currForm = null;
		
		// Loop through the form array and seperate out the form ID 
		for ( var i = 0; i < idList.length; i++ ) {
			currForm = selectedForms.getItem(idList[i]);
			var $checkbox = $("input:checkbox[name='formShortNameCheckbox_" + currForm.shortName + "']");
			
			// If the checkbox exists, ensure that the displayed data is correct
			if ( $checkbox.length > 0 ) {
				//added by Ching-Heng
				var iscat = $checkbox.attr('iscat');
				if(iscat == 'true'){
					anyPromisForm.push(iscat);
				}
				
				// Add the associated checkbox as a selected option if not already selected. When adding
				// new check box to the table selection, signal the selection listener to ignore any changes.
				if ( !IDT.isSelectedOption($checkbox, currForm.shortName.toString()) ) {
					canToggleSelection = false;
					IDT.addSelectedOptionValue($table, currForm.shortName.toString());
					canToggleSelection = true;
				}
				
				var $radioBtns = $("input:radio[name='isMandatory_" + currForm.shortName + "']");
				$radioBtns = $radioBtns.add($("input:radio[name='isSelfReport_" + currForm.shortName + "']"));
				
				// Enable the radio button if it is disabled
				if ( $radioBtns.is(":disabled") ) {;
					$radioBtns.prop("disabled", false);
				}
				
				// Set the associated radio button option
				$radioBtns.each(function(idx, element) {
					var $radioBtn = $(this);
					
					if (($radioBtn.attr("name").indexOf("isMandatory") > -1 && currForm.isManitory == "true")
							|| ($radioBtn.attr("name").indexOf("isSelfReport") > -1 && currForm.isSelfReport == "true")){
						if ( $radioBtn.val() == "true" ) {
							$radioBtn.prop("checked", true);
						}
						else {
							$radioBtn.prop("checked", false);
						}
					} 
					else {
						if ( $radioBtn.val() == "true" ) {
							$radioBtn.prop("checked", false);
						}
						else {
							$radioBtn.prop("checked", true);
						}
					}
				});
			}
		}
		// added by Ching-Heng
		if(anyPromisForm.length > 0){
			$('#promisformAddingMsg').show();			
		}else{
			$('#promisformAddingMsg').hide();
		}
	}
	
	/**
	 * Callback for the table draw event. Will just call reFreshTable().
	 * 
	 * @param dataTable - The DataTable model.
	 * @param this - Set to the table DOM element of the forms table. 
	 */
	function tableDrawListener(dataTable) {
		var $formTable = $(this);
		
		if ( initSelected ) {
			initSelectedTableValues($formTable);
			initSelected = false;
		}
		
		reFreshTable($formTable);
	}
	
	/**
	 * Used to populate the form table with the selected forms and values on each of the table's pages.
	 *
	 * @param $table - JQuery reference to the target table to sync.
	 */
	function reFreshTable($table) {
		var $radioBtns = $table.find("input:radio:enabled");
		
		// Ensure that the radio buttons are in the correct state and values.
		if ( !selectedForms.isEmpty() ) {
			populateCheckBoxes($table);
			
			// Look for any orphaned radio buttons that need to be reset back to the default state.
			$radioBtns.each(function(idx, element) {
				var $radioBtn = $(this);
				var $checkBox = $radioBtn.parents("td:first").siblings().find("input:checkbox");
				
				if ( !IDT.isSelectedOption($checkBox, $checkBox.val()) ) {
					// Set the radio button to its default state
					if ( $radioBtn.val() == "true" ) {
						$radioBtn.prop("checked", false);
					}
					else {
						$radioBtn.prop("checked", true);
					}
					
					$radioBtn.prop("disabled", true);
				}
			});
		}
		else {
			// Reset all enabled radio buttons to their default states.
			$radioBtns.each(function(idx, element) {
				var $radioBtn = $(this);
				
				if ( $radioBtn.val() == "true" ) {
					$radioBtn.prop("checked", false);
				}
				else {
					$radioBtn.prop("checked", true);
				}
				
				$radioBtn.prop("disabled", true);
			});
		}
	}
	
	/**
	 * Initializes the selectedForms HashTable and the form order select box.
	 */
	function initSelectedForms() {
		var formArray = $.parseJSON($("#formStrList").val());

		// Exit function if the form array is empty
		if ( formArray.length == 0 ) {
			return;
		}
		
		// Populate the selected form and form order select box
		var $formOrderBx = $("#formOrderBox");
		
		for ( var i = 0; i < formArray.length; i++ ) {
			selectedForms.setItem(formArray[i].shortName, formArray[i]);
	
			$formOrderBx.append('<option id ="orderForm_' + formArray[i].shortName + '" value="' + formArray[i].shortName + '">' + formArray[i].shortName + '</option>');
		}
	}
	
	/**
	 * function to add selected checkbox on other pages
	 */
	function initSelectedTableValues($table) {
		var selectedValues = selectedForms.values();
		
		// Disable the row selection listener when adding selected values to the table.
		canToggleSelection = false;
		
		for ( var i = 0; i < selectedValues.length; i++ ) {
			IDT.addSelectedOptionValue($table, selectedValues[i].shortName);
		}
		
		// Re-enable the row selection listener.
		canToggleSelection = true;
	}
	
	/**
	 * Handler for the pre-population checkbox change event.
	 *
	 * @param event {DOM Event} - The Event object of the tiggered event.
	 * @param this {DOM Element} - Set to the checkbox DOM object, who is the target of this event.
	 */
	function prePopCheckBxChangeHandler(event) {
		var $chkbox = $(this);
		var prePopDe = prePopDeTable.getItem($chkbox.val());
		
		// Update the selected status.
		prePopDe.isSelected = $chkbox.is(":checked");
	}
	
	/**
	 * Renders the pre-population data element checkboxes based on the passed in array of pre-population objects. This
	 * function or any parent functions should be called after the eForm selection changes are processed to ensure that
	 * the display functionality is executed correctly.
	 *
	 * @param prePopDeArray {JS Array} - An array of pre-population data element objects to build the checkboxes with.
	 */
	function renderPrePopDeCheckBoxes(prePopDeArray) {
		var markUp = prePopDeChkBxTemplate({"prePopDeArray" : prePopDeArray});
		var $prePopDiv = $("#fieldAutopopulationDiv");
		
		// Turn off and "change" handlers on existing checkboxes, render the new checkboxes, then bind the new 
		// checkboxes to the pre-pop checkbox change handler.
		$prePopDiv.find(".prePopDeChkBox").off("change");
		$prePopDiv.find("#prePopChkBoxDiv").html(markUp);
		$prePopDiv.find(".prePopDeChkBox").on("change", prePopCheckBxChangeHandler);
		
		// Show the pre-pop checkboxes.
		if ( !selectedForms.isEmpty() ) {
			showPrePopCheckboxes($prePopDiv);
		}
		else {
			$prePopDiv.find("#prepopulationDiv").hide();
			$prePopDiv.find("#noSelectedEform").show();
			$prePopDiv.find("#noPrepopDEs").hide();
		}
	}
	
	/**
	 * Initializes the pre-population data element hashtable, and renders the associated checkboxes on the page.
	 *
	 * @param prePopDeArray {JS Array} - An array of pre-population data element objects, which will be used to 
	 * 			populate the pre-pop DE hashtable and checkboxes.
	 */
	function initPrePopDeCheckBoxes(prePopDeArray) {
		// Add the pre-pop data elements to the pre-pop DE table.
		prePopDeTable.clear();
		
		for ( var i = 0; i < prePopDeArray.length; i++ ) {
			prePopDeTable.setItem(prePopDeArray[i].shortName, prePopDeArray[i]);
		}
		
		// Render the checkboxes.
		renderPrePopDeCheckBoxes(prePopDeArray);
	}
	
	/**
	 * Makes sure that the pre-population checkboxes are displayed. If the checkboxes are
	 * already shown, then no operation will take place.
	 *
	 * @param $prePopDiv {jQuery Object} - Reference to the div that contains all of the 
	 *		pre-population checkbox UI elements.
	 */
	function showPrePopCheckboxes($prePopDiv) {
		// Only show the checkboxes if they are already hidden.
		if ( $prePopDiv.find("#prePopChkBoxDiv").is(":hidden") ) {
			$prePopDiv.find("#prepopulationDiv").show();
			$prePopDiv.find("#noSelectedEform").hide();
			$prePopDiv.find("#noPrepopDEs").hide();
		}
	}
	
	/**
	 * Clears the non-disabled pre-population checkboxes and makes sure that the checkboxes are
	 * hidden if there are no eForms selected.
	 */
	function clearPrepopDECheckbox() {
		var $prePopDiv = $("#fieldAutopopulationDiv");
		
		// Clear out the non-disabled pre-pop checkbox selections.
		$prePopDiv.find(".prePopDeChkBox").not(":disabled").prop("checked", false);
		
		// Check if the pre-pop checkboxes will need to be hidden.
		if ( selectedForms.isEmpty() ) {
			$prePopDiv.find("#prepopulationDiv").hide();
			$prePopDiv.find("#noSelectedEform").show();
			$prePopDiv.find("#noPrepopDEs").hide();
		}
	}
	
	
	
	function validatePrepops() {
		
		
		var unmatchedPrepops = [];
		
		var index = 0;
		$('.prePopDeChkBox:checkbox:checked').each(function () {
		       var sThisVal = (this.checked ? $(this).val() : "");
		       var found = false;
		       <% 
		       	for(int i=0;i<deNamesCache.size();i++) {
			    	 String deName = deNamesCache.get(i);
			    	 %>
			    		var de = "<%= deName %>";
			    		if(sThisVal == de) {
			    			found = true;
			    		}
			     <%}%>
			if(!found) {
				unmatchedPrepops[index] = sThisVal;
    			index++;
			}    
		  });
		
		
		if(unmatchedPrepops.length > 0) {
			var msg = "";
			for(var i=0;i<unmatchedPrepops.length;i++) {
				msg = msg + unmatchedPrepops[i];
				if(i != unmatchedPrepops.length - 1 ) {
					msg = msg + ',';
				}
			}
			//show warning
			if (confirm("The following fields do not have corresponding data elements in dictionary: " + msg + "\n Do you wish to still create a Visit Type?")) {
    			return true;
 			} else {
 				return false;
  			}
			
			
		}
			
		
		
		
	}
	
	$(document).ready(function() {
		// Bind to some Data Table events
		var tableDom = $("#formDisplayTableRef table").get(0);
		EventBus.on("select:all", selectAllListener, tableDom);
		EventBus.on("deselect:all", deSelectAllListener, tableDom);
		EventBus.on("select:set", selectAllListener, tableDom);
		EventBus.on("draw:table", tableDrawListener, tableDom);
		EventBus.on("select:row", selectionChangeListener);
		EventBus.on("deselect:row", selectionChangeListener);
		
		// Check if the page should be in edit mode
		if ( $("#visitTypeId").val() > 0 ) {
			initSelectedForms();
			initSelected = true;
			$("#addVisitTypeBtn").hide();
			$("#updateVisitTypeBtn").show();
		}
		else if ( $("#formStrList").val() !== "[]" ) {
			initSelectedForms();
		}
		
		// Check if any pre-population checkboxes will need to be displayed.
		var prePopDes = JSON.parse($("#prepopDEStrList").val());
		
		if ( (prePopDes.length == 0) && selectedForms.isEmpty() ) {
			var $prePopDiv = $("#fieldAutopopulationDiv");
		}
		
		// Check if any pre-population checkboxes will need to be displayed.
		var prePopDes = JSON.parse($("#prepopDEStrList").val());
		
		if ( prePopDes.length > 0 ) {
			initPrePopDeCheckBoxes(prePopDes);
		}
		
		// ++++++ Event Listners ++++++
		
		// A listener for all text fields when a change occurs
		$("input:text, textarea").change(function(event) {
			var $elem = $(this);
			
			// Remove all leading and trailing white spaces
			$elem.val($.trim($elem.val()));
		});
		
		// Listener for the Up button for re-ordering a form
		$("#shiftUpBtn").click(function(event) {
			var $selectedOpt = $("#formOrderBox option:selected");
			$selectedOpt.prev().before($selectedOpt);
		});
		
		// Listener for the Down button for re-ordering a form
		$("#shiftDownBtn").click(function(event) {
			var $selectedOpt = $("#formOrderBox option:selected");
			$selectedOpt.next().after($selectedOpt);
		});
		
		// Listener for the web form reset button
		$("#resetVisitTypeBtn").click(function(event) {
			// Call the form's reset function.
			$("#addEditIntervalForm").get(0).reset();
			
			// If in edit mode, set signal to re-init the form selection.
			if ( $("#visitTypeId").val() > 0 ) {
				canInitSelection = true;
			}
			
			// Clear all selected items in the table
			IDT.clearSelected($("#formDisplayTableRef table"));
		});
		
		// Listener for the web form cancel button
		$("#cancleAddEditBtn").click(function(event) {
			var url = webRoot + "/protocol/visitTypeHome.action";
			redirectWithReferrer(url);
		});
		
		// Submit listner 
		$("#addEditIntervalForm").submit(_.debounce(function(event) {
			// Disable the submit button.
			_.defer(function() {
				$(".submitButton").prop("disabled", true);
			});
			
			// Set the order of the selected forms 
			$("#formOrderBox option").each(function (index) {
				selectedForms.getItem($(this).val()).orderVal = index + 1;
			});
			
			// Deselect any selected options in the ordering select box to avoid any extra data that may get submitted
			$("#formOrderBox option:selected").prop("selected", false);
			
			// Convert the selectedForms HashTable to JSON
			if ( !selectedForms.isEmpty() ) {
				$("#formStrList").val(JSON.stringify(selectedForms.values()));
				$("#prepopDEStrList").val(JSON.stringify(prePopDeTable.values()));
			}
			else {
				$("#formStrList").val("[]");
			}
			var intervalClinicalPntRows = $('#intervalCPDTTable').idtApi("getTableApi").rows().data();
			var intervalClinicalPntArr = [];
			for (var i = 0; i < intervalClinicalPntRows.length; i++) {
				var intClinicalPntJson = getSelectedIntCPJson(intervalClinicalPntRows[i]);
				intervalClinicalPntArr.push(intClinicalPntJson);
			}
			$("#intervalClinicalPntStrList").val(JSON.stringify(intervalClinicalPntArr));
		}, 3000, true));
		
		$(".selfReportSpinner").spinner({
			min: 0,
			max: 366
		});
		
		$(".selfReportSpinner").on("change", function() {
			var $this = $(this);
			var val = $this.val();
			if (isNaN(val)) {
				$this.val(15);
				$.ibisMessaging("dialog", "error", "The form availability fields must be numeric");
			}
			else if (val > 366) {
				$this.val(366);
			}
			else if (val < 0) {
				$this.val(0);
			}
		});
	}); //end document.ready
	
	function ellipsisExpandCollapse(element) {
		var $this = $(element);
		$this.parent().toggle();
		if ($this.text() == "...") {
			$this.parent().next().toggle();
		}
		else {
			$this.parent().prev().toggle();
		}
	}
	
	// added by Ching-Heng
	function showPromisAgeement(currentCheckBox, shortName){
		$("#agreement").dialog({
			  modal: true,
			  width: 1055,
			  maxHeight: 600,
			  title: 'PROMIS Shared Content Agreement',			  
              create: function (e, ui) {
                  var pane = $(this).dialog("widget").find(".ui-dialog-buttonpane")
                  $("<input type='checkbox' name='iAgree' /><span style='color:red'>&nbsp;I agree with the PROMIS Shared Content Agreement</span>").prependTo(pane)
              },
			  open: function(){
				  $(this).scrollTop(0);
				  $("input:checkbox[name='iAgree']").prop( "checked", false );
			  },
			  close: function() {
				  if(!hasSigned){	
					currentCheckBox.parent().closest('.row_selected').click();
					anyPromisForm.pop();
					//show promis msg
					if(anyPromisForm.length > 0){					
						$('#promisformAddingMsg').show();		
					}else{
						hasSigned = false;
						$('#promisformAddingMsg').hide();
					}
				  }
              },
			  buttons: [
			    {
			      text: "I agree",
			      click: function() {
			    	if($("input:checkbox[name='iAgree']:checked").length == 0){				    		 
			    		alert('Please check the terms of use before you agree');
			    	}else{
			    		hasSigned = true;
				        $( this ).dialog( "close" );
			    	}						
			      }
			    },
			    {
			      text: "I do not agree",
			      click: function() {
			    	currentCheckBox.parent().closest('.row_selected').click();
					anyPromisForm.pop();
					//show promis msg
					if(anyPromisForm.length > 0){					
						$('#promisformAddingMsg').show();		
					}else{
						hasSigned = false;
						$('#promisformAddingMsg').hide();
					}
			        $( this ).dialog( "close" );
			      }
			    }
			  ]
		});
	}

	function showPromisAgeementForAll(dataTable){
		$("#agreement").dialog({
			  modal: true,
			  width: 1055,
			  maxHeight: 600,
			  title: 'PROMIS Shared Content Agreement',
			  create: function (e, ui) {
                  var pane = $(this).dialog("widget").find(".ui-dialog-buttonpane")
                  $("<input type='checkbox' name='iAgree' /><span style='color:red'>&nbsp;I agree with the PROMIS Shared Content Agreement</span>").prependTo(pane)
              },
              open: function(){
				  $(this).scrollTop(0);
			  },
			  buttons: [
				    {
				      text: "I agree",
				      click: function() {
				    	if($("input:checkbox[name='iAgree']:checked").length == 0){				    		 
				    		alert('Please check the terms of use before you agree');
				    	}else{
				    		hasSigned = true;
					        $( this ).dialog( "close" );
				    	}						
				      }
				    },
				    {
				      text: "I do not agree",
				      click: function() {
				    	IDT.clearSelected($("#formDisplayTableRef table"));
				        $( this ).dialog( "close" );
				      }
				    }
			  ]
		});
	}
</script>

<p><s:text name="protocol.intervalForm.instruction"/></p>
		
<%-- ---------------------------------------------------Interval Details---------------------------------------------------- --%>
<h3 class="toggleable">
	<s:text name="protocol.intervalForm.create.title"/>
</h3>	
<div id="intervalInfoDiv">
	<s:form theme="simple" method="post" enctype="multipart/form-data" id="addEditIntervalForm">
		<s:hidden name="id" id="visitTypeId" />
		<s:hidden name="studyId" id="studyId" />
		<s:hidden name="selectedForms" id="formStrList" />
		<s:hidden name="changeMode" id="changeMode" />
		<s:hidden name="prepopDataElements" id="prepopDEStrList" />
		<s:hidden name="intervalClinicalPoints" id="intervalClinicalPntStrList" />

		<label class="requiredInput"></label> 
        <i><s:text name="protocol.intervalForm.create.requiredSymbol"/></i>
        <br/><br/>
		
		<div class="formrow_1">
			<label for="visitTypeName" class="requiredInput">
				<s:text name="protocol.intervalForm.create.intervalName"/>
			</label>
			<s:textfield name="name" maxlength="50" size="45" id="visitTypeName"/>
		</div>
		
		<div class="formrow_1">
			<label for="visitTypeType"><s:text name="protocol.intervalForm.create.intervalType"/></label>
			<s:select cssClass="intervalType" name="intervalType" id="visitTypeType" 
				list="intervalTypeList" listKey="id" listValue="shortName" />
		</div>
		
		<div class="formrow_1">
			<label for="visitTypeCategory"><s:text name="protocol.intervalForm.create.category"/></label>
			<s:textfield name="category" maxlength="50" size="45" id="visitTypeCategory"/>
		</div>
		
		<div class="formrow_1">
			<label for="visitTypeDescr" class="requiredInput">
				<s:text name="protocol.intervalForm.create.description"/>
			</label>
			<s:textarea name="description" rows="4" cols="45" id="visitTypeDescr" />
		</div>
		<%-- ---------------------------------------------------Field Location Points starts---------------------------------------------------- --%>
		<s:set var="displayClinicalPoint" value="#systemPreferences.get('display.protocol.clinicalPoint')" />
		<s:if test="#displayClinicalPoint">
		<div class="formrow_1">
			<label for="visitTypeClinicalPoints" class="requiredInput">
				<s:text name="protocol.intervalForm.create.clinicalPoints"/>
			</label>
			<div id="intervalClinicalPoint" style="float:left; ">
				<jsp:include page="addIntervalClinicalPoints.jsp" />
			</div>
		</div>
		</s:if>
		<%-- ---------------------------------------------------Field Location Points ends---------------------------------------------------- --%>
		<security:hasProtocolPrivilege privilege="patientselfreporting">
		<div class="formrow_1">
			<label for="visitTypeSelfReportStart" class="requiredInput">
				Self Reporting eForms
			</label>
			<div class="noResize">
				Available 
				<s:textfield name="visitTypeSelfReportStart" maxlength="3" size="3" id="visitTypeSelfReportStart" class="selfReportSpinner" />
				days before the scheduled visit until 
				<s:textfield name="visitTypeSelfReportEnd" maxlength="3" size="3" id="visitTypeSelfReportEnd" class="selfReportSpinner" />
				days after the scheduled visit.
			</div>
		</div>
		</security:hasProtocolPrivilege>
<%-- ---------------------------------------------------Interval Details---------------------------------------------------- --%>
<%-- ---------------------------------------------------Form Details-------------------------------------------------------- --%>
		<div class="formrow_1">
			<label><s:text name="protocol.intervalForm.associateEForm.title"/></label>
			<div class="dataTableContainer formrowinput" id="formDisplayTableRef">
				<idt:jsontable name="<%= FormConstants.PROTOCOLEFORMS %>" scope="request" decorator="gov.nih.nichd.ctdb.form.tag.ProtocolFormDecorator">
			    	<idt:setProperty name="basic.msg.empty_list" value="There are no Public Forms found at this time."/>
			    	<idt:column property="shortNameCheckBox" title="" />
			        <idt:column property="name" title='<%=rs.getValue("form.name.display", l)%>' />
			        <idt:column property="shortName" title='eForm Short Name' />
			        <idt:column property="description" title='<%=rs.getValue("form.forms.formInformation.description", l)%>'/>
			        <idt:column property="isMandatory" title="Required?" nowrap="true" />
			        <security:hasProtocolPrivilege privilege="patientselfreporting">
			       		<idt:column property="isSelfReport" title="Self Reporting?" nowrap="true" />
			        </security:hasProtocolPrivilege>
			    </idt:jsontable>
			</div>			
		</div>
<%-- ---------------------------------------------------Form Details---------------------------------------------------- --%>
		<!-- added by Ching-Heng -->	
		<div id='promisformAddingMsg' style = 'display:none;background-color:#ffff66;border-style: solid; border-color: coral;'>
			<b>NOTICE:</b> The instrument you are adding to your protocol is either an Adaptive Instrument - computer adaptive test (CAT), Auto-Scoring test, or PROMIS Short-Form (SF). Please reference definitions and survey behavior details below:
			<br/>
			<b>Adaptive Instrument - </b> computer adaptive test (CAT) means that its questions are generated dynamically using previous answers. Adaptive instrument eForms behave a little differently in BRICS than traditional eForms. You will not be able to modify any fields on the instrument at any time. This adaptive instrument can only be taken in survey form. If the data entry form is viewed for this instrument, all fields will be displayed as read-only.
			<br/>
			<b>Auto-Scoring Instrument - </b> means that it behaves differently than traditional instruments/eForms. Once the instrument has been downloaded, you will not be able to modify any fields on the instrument at any time. This auto-scoring instrument can only be taken in survey form. If the data entry form is viewed for this instrument, all fields will be displayed as read-only.
			<br/>
			<b>Short-Form (SF) - </b> standard survey format.
		</div>
<%-- ---------------------------------------------------Form Ordering---------------------------------------------------- --%>
		<br/>
		<h3 id="orderInfo" class="toggleable collapsed">
			<s:text name="protocol.intervalForm.formDisplayOrder.title" />
		</h3>
		<div id="orderInfoDiv">
			<p><s:text name="protocol.intervalForm.formDisplayOrder.instruction" /></p>
			<br/><br/>
			<div class="formrow_1">
				<select id="formOrderBox" size="10">
				</select>
				<div style="position: relative; top: 48px; margin-left: 460px;">
				    <input type="button" id="shiftUpBtn" value="Up" title="Shifts the selected form up one."/>
				    <br/><br/>
				    <input type="button" id="shiftDownBtn" value="Down" title="Shifts the selected form down one."/>
				</div>
			</div>
		</div>
<%-- ---------------------------------------------------Form Ordering---------------------------------------------------- --%>

<%-- ---------------------------------------------------Field Auto-population---------------------------------------------------- --%>
		<br/>
		<h3 id="fieldAutopopulation" class="toggleable collapsed">
			<s:text name="protocol.intervalForm.fieldAutopopulationList.title" />
		</h3>
		
		<div id="fieldAutopopulationDiv">
			<div id="noSelectedEform">No eForm is selected. Please select at least one eForm.</div>
			<div id="noPrepopDEs" style="display: none;">There are no auto population fields in the selected eForm(s).</div> 
			<div class="formrow_1" id="prepopulationDiv" style="display: none;">
				<p><s:text name="protocol.intervalForm.fieldAutopopulationList.instruction" /></p>
				<br/>
				<div id="prePopChkBoxDiv"></div>
			</div>
		</div>
<%-- ---------------------------------------------------Field Auto-population---------------------------------------------------- --%>

<%-- ---------------------------------------------------Interval Form Buttons------------------------------------------- --%>
		<div class="formrow_1">	
			<input type="button" id="cancleAddEditBtn" value="<s:text name='button.Cancel'/>" title="Click to cancel (changes will not be saved)." />
			<input type="button" id="resetVisitTypeBtn" value="<s:text name='button.Reset'/>" title="Click to reset all fields to their original values" />
			<s:submit action="saveVisitType" id="addVisitTypeBtn" key="button.intervalForm.addVisitType" 
				cssClass="submitButton" title="Click to add a visit type to a selected study" />
			<s:submit action="saveVisitType" id="updateVisitTypeBtn" key="button.intervalForm.updateVisitType" 
				cssClass="hidden submitButton" title="Click to update the visit type" />
		</div>
<%-- ---------------------------------------------------Interval Form Buttons------------------------------------------- --%>
	</s:form>
</div>

<div id="agreement" style="display:none;height:350px;overflow:auto;">
	<jsp:include page="/protocol/promisSharedContentAgreement.jsp" />
	<h3></h3>
</div>


<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />

</html>
