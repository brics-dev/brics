<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.Image"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs"%>
<%@ page import="gov.nih.nichd.ctdb.form.common.FormResultControl"%>
<%@ page import="gov.nih.nichd.ctdb.security.domain.User"%>
<%@ page import="java.util.Locale"%>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="viewforms" />

<%
	User user = (User) session.getAttribute(CtdbConstants.USER_SESSION_KEY);
	Locale l = request.getLocale();
%>
<html>
<s:set var="pageTitle" scope="request">
	<s:text name="form.forms.manageFormsDisplay" />
</s:set>

<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/designs/style_guide/jquery.expandSections.css" />
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/v/dt/jszip-3.1.3/pdfmake-0.1.27/dt-1.10.15/b-1.3.1/b-colvis-1.3.1/b-flash-1.3.1/b-html5-1.3.1/b-print-1.3.1/datatables.min.css" />

 <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.min.css">
 <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/responsive/2.1.1/css/responsive.dataTables.min.css" />

<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript" src="<s:property value="#webRoot"/>/common/calendar.js"></script>
<script src="<s:property value='#webRoot'/>/common/js/template.js" type="text/javascript"></script>
 
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/designs/datatablesWidget/css/datatablesWidget.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/designs/datatablesWidget/css/idtSearchColumnPlugin.css" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/designs/datatablesWidget/css/idtSelectSetPlugin.css" />
<link href="http://maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css" rel="stylesheet" />

<script type="text/javascript" src="https://cdn.datatables.net/v/dt/jszip-3.1.3/pdfmake-0.1.27/dt-1.10.15/b-1.3.1/b-colvis-1.3.1/b-flash-1.3.1/b-html5-1.3.1/b-print-1.3.1/datatables.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/select/1.2.2/js/dataTables.select.min.js"></script>
<script src="<s:property value="#webRoot"/>/common/designs/style_guide/jquery.expandSections.full.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/jqfactory-03.js" type="text/javascript"></script>

<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/idtTableWidget.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/formbuilder/js/core/util/TemplateManager.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/fnDtFilterPlugin.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/idtSelectSetPlugin.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/idtSearchColumnPlugin.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/idtTableActions.js" type="text/javascript"></script>
<script src="<s:property value="#webRoot"/>/common/designs/datatablesWidget/idtApi.js" type="text/javascript"></script>

<script type="text/javascript" >
	<%-- This function toggles between showing the pateint forms div and the non-patient forms div --%>
/* 	function formsToggleVisibility() {
		var selected = $("#formType option:selected").val();

		if (selected == "0") {
			// Patient forms needs to be visible
			$("#patientFormsContent").show();
			$("#nonPatientFormsContent").hide();
			$("#allFormsContent").hide();
		}
		else if (selected == "1") {
			// Non-patient forms needs to be visible
			$("#nonPatientFormsContent").show();
			$("#patientFormsContent").hide();
			$("#allFormsContent").hide();
		}
		else {
			// All forms needs to be visible
			$("#allFormsContent").show();
			$("#patientFormsContent").hide();
			$("#nonPatientFormsContent").hide();
		}
	} */

	
	function initialize(formTypeToDisplay) {
		var $formType = $("#formType");
		
		if (formTypeToDisplay == "Subject") {
			$formType.val(0);
		}
		else {
			$formType.val(1);
		}
		
		// removed for PS-1784
		//formsToggleVisibility();
	}
	
	/**
	 * Handles the view form buttons processing for all of the form tables on the page.
	 *
	 * @param $table - jQuery reference to the form table that the view form process shall act apon.
	 */
	function performViewForm($table) {
		var selected = IDT.getSelectedOptions($table);
		
		// for view, there should only be one, so just grab the first of the array.
		// that handles any case
		if (selected.length > 0) {
			selected = selected[0];
			var WindowArgs = "toolbar=1,menubar=1,location=no,directories=no,status=1,top=10,left=10,scrollbars=yes,resizable=yes,width=600,height=400";
			var url = "<s:property value='#webRoot'/>/form/viewFormDetail.action?source=popup&id=" + selected;
			openPopup(url, "", WindowArgs);
		}
		else {
			$.ibisMessaging("dialog", "warning", "You must select a form to perform this action.");
		}
	}
	
	/**
	 * Handles the activate buttons processing for all of the form tables on the page.
	 *
	 * @param $table - jQuery reference to the form table that the activate process shall act apon.
	 * @parma $button - jQuery reference to the edit button that initiated the activate process. Assumed to be disabled at this point.
	 */
	function activate($table, $button) {
		var selectedIds = IDT.getSelectedOptions($table);
		
		if ( selectedIds.length == 1 ) {
			var formId = selectedIds[0];
			var numQuestions = $table.find("input[name='numQuestions_" + formId + "']").val();
			var statsQuestions = $table.find("input[name='statsQuestions_" + formId + "']").val();
			
			if ( (numQuestions == "0") || (statsQuestions != "1") ) {
				$button.prop("disabled", false);
				$.ibisMessaging("dialog", "warning", "Only forms that are inactive and have at least 1 question can be activated.");
			}
			else {
				redirectWithReferrer("activateForm.action?formId=" + formId);
			}
		}
		else {
			$button.prop("disabled", false);
		}
	}
	
	
	/**
	 * Handles the view audit buttons processing for all of the form tables on the page.
	 *
	 * @param $table - jQuery reference to the form table that the view audit process shall act apon.
	 */
	function launchViewAudit($table) {
		var selectedIds = IDT.getSelectedOptions($table);
		
		if ( selectedIds.length == 1 ) {
			var WindowArgs = "toolbar=0,location=0,directories=0,,menubar=0,status=1,scrollbars=1,resizable=1,top=10,left=10,width=700,height=400";
			var url = "<s:property value='#webRoot'/>/form/formAudit.action?id=" + selectedIds[0];
			
			top.newWin = openPopup2(url, "", WindowArgs); //fix for session information lost on IE
			//top.newWin.focus();
		}
		else {
			$.ibisMessaging("dialog", "warning", "You must select only one form to perform this action.");
		}
	}
	
	function openPopup2(url, name, specs) {
		var wnd = null;
		
		if (typeof name == "undefined" || name == "") {
			name = "newwindow";
		}
		
	/*	if ((BrowserDetect.browser == "Explorer")) {*/
		if(navigator.msMaxTouchPoints !== void 0 ) {
			if ($("#redirectLink").length < 1) {
				// if we don't currently have a redirect link on the page, create one
				$("body").append('<a href="javascript:void(0)" id="redirectLink" target="_self" style="display:none">click</a>');
			}
			// change the properties of the link and click it
			wnd = window.open("", name, specs);
			var link = document.getElementById("redirectLink");
			link.target = name;
			link.href = url;
			link.click();
			 //window.parent.focus();
		}
		else {
			wnd = window.open(url, name, specs);
			//window.parent.focus();
		}
		return wnd;
	}

	
	/**
	 * Handles the save as new form buttons processing for all of the form tables on the page.
	 *
	 * @param $table - jQuery reference to the form table that the save as process shall act apon.
	 * @parma $button - jQuery reference to the edit button that initiated the save as process. Assumed to be disabled at this point.
	 */
	function performSaveAs($table, $button) {
		var selectedIds = IDT.getSelectedOptions($table);
		
		if ( selectedIds.length == 1 ) {
			var url = "<s:property value="#webRoot"/>/form/showSaveAsForm.action?id=" + selectedIds[0];
			redirectWithReferrer(url);
		}
		else {
			$button.prop("disabled", false);
			$.ibisMessaging("dialog", "warning", "You must select a form to perform this action.");
		}
	}
	
	/**
	 * Handles the form edit process for any of the form table's edit buttons.
	 *
	 * @param $table - jQuery reference to the form table that the edit process shall act apon.
	 * @parma $button - jQuery reference to the edit button that initiated the edit process. Assumed to be disabled at this point. 
	 */
	function performEditForm($table, $button) {
		var selectedIds = IDT.getSelectedOptions($table);
		
		if ( selectedIds.length == 1 ) {
			var id = selectedIds[0];
			var url = "<s:property value="#webRoot"/>/form/showEditForm_newFormBuilder.action?id=" + id;
			var status = getCellContentAsText($table, id, 4);
			var dialogBtns = [
				{
					id : "saveNewBtn",
					text : "Save as New",
					
					click : _.debounce(function() {
						var uri = "<s:property value="#webRoot"/>/form/showSaveAsForm.action?id=" + id;
						
						$(this).siblings().find("#saveNewBtn").prop("disabled", true);
						redirectWithReferrer(uri);
					}, 1000, true)
				},
				
				{
					id : "editFormBtn",
					text : "Edit Form",
					
					click : _.debounce(function() {
						var uri = "<s:property value="#webRoot"/>/form/showEditForm_newFormBuilder.action?id=" + id;
						
						$(this).siblings().find("#editFormBtn").prop("disabled", true);
						redirectWithReferrer(uri);
					}, 1000, true)
				}
			];
			
			if (status == '<%=CtdbConstants.FORM_STATUS_INPROGRESS_SHORTNAME%>') {
				$button.prop("disabled", false);
				
				<%if (user.isSysAdmin()) {%>
					$("#editInProgressFormDecisionInfo").dialog({
						modal : true,
						width : 600,
						buttons : dialogBtns
					});
				<%} else {%>
					$.ibisMessaging("dialog", "warning", 'You can not edit a form that is "In Progress."');
				<%}%>
			}
			else {
				var administered = getCellContentAsText($table, id, 5);
				<%if (user.isSysAdmin()) {%>
					if (administered == "Yes") {
						$button.prop("disabled", false);
						
						// Open decision box
						$("#editAdministeredFormDecisionInfo").dialog({
							modal : true,
							width : 600,
							buttons : dialogBtns
						});
					}
					else {
						redirectWithReferrer(url);
					}
				<%} else {%>
					if (administered == "Yes") {
						$button.prop("disabled", false);
						$.ibisMessaging("dialog", "warning", 
							"You can not edit this form because there are data collections for this form.", {width: 600, modal: true});
					}
					else {
						redirectWithReferrer(url);
					}
				<%}%>
			}
		}
		else {
			$button.prop("disabled", false);
			$.ibisMessaging("dialog", "warning", "You must select a form to perform this action.", {width: 600, modal: true});
		}
	}
	
	function getCellContentAsText($table, selectedVal, position) {
		var tableModel = IDT.getTableModel($table);
		var cellText = "";
		
		// Loop though all of the table rows visable or not.
		tableModel.rows.forEach(function (row) {
			var $checkBx = row.getInput();
			var dataText = row.getCellDataHtml(position);
			
			// Check if at the right row.
			if ( ($checkBx.val() == selectedVal) || ($checkBx.attr("id") == selectedVal) ) {
				cellText = dataText;
				return false;
			}
		});
		
		return cellText;
	}
	
	<%-- This function performs Delete when the Delete button is clicked --%>
	/**
	 * This function performs a delete with the following steps:
	 * Determine which table we are operating on and which selected items
	 * Check selected options and see if form is administered
	 * Administered forms cannot be deleted
	 * notify users which forms can be deleted and which cannot
	 * 
	 */
	function performDelete($table, $button) {
		var selected = IDT.getSelectedOptions($table);

		if (selected.length > 0) {
			var adminTitle = '<%=rs.getValue("form.administered.display", l)%>';
			var nameTitle = '<%=rs.getValue("form.name.display", l)%>';
			var adminArray = IDT.getCellContent($table, adminTitle);
			var nameArray = IDT.getCellContent($table, nameTitle);
			
			// at this point, selected and adminArray have common-indexed values associated with each other
			// such that selected[0] = id_1 and adminArray = is_admin(id_1)
			var formIdsToRemove = "";
			var removeString = "";
			var cantRemoveString = "";
			
			for (var i = 0; i < adminArray.length; i++) {
				if (adminArray[i] != "Yes") {
					if (formIdsToRemove != "") {
						formIdsToRemove += "," + selected[i];
					}
					else {
						formIdsToRemove += selected[i];
					}
					
					removeString += "<li>" + nameArray[i] + "</li>";
				}
				else {
					cantRemoveString += "<li>" + nameArray[i] +"</li>";
				}
			}
			
			var url = "<s:property value="#webRoot"/>/form/deleteForm.action?id=" + formIdsToRemove;

			
			if (formIdsToRemove == "") {
				$button.prop("disabled", false);
				$.ibisMessaging("dialog", "info", 
						'<span style="text-align: left; padding:5px"><p>The following forms can not be deleted because they are administered: </p> <br\><span style="font-weight:normal"><ul>'+cantRemoveString+'</ul></span> </span>',
						{width: "600px", modal: true}
					);
			}
			else {
				if (cantRemoveString != "") {
					var dlgId = $.ibisMessaging(
						"dialog", 
						"info", 
						'<span style="text-align: left; padding:5px"><p>The following form(s) will be deleted:</p><br\><span style="font-weight:normal"><ul>' + removeString + '</ul></span> <br\><p>The following form(s) can not be deleted because they are administered: </p><br\><span style="font-weight:normal"><ul>' + cantRemoveString + '</ul></span> <br\><p>Do you want to proceed?</p></span>',
						{
							buttons: [{
								id: "yesBtnA",
								text: "Yes", 
								click: _.debounce(function() {
									$(this).siblings().find("#yesBtnA").prop("disabled", true);
									redirectWithReferrer(url);
								}, 1000, true)
							},
							{
								text: "No",
								click: function() {
									$.ibisMessaging("close",{id: dlgId});
								}
							}],
							modal: true,
							width: "600px"
						});
				}
				else {
			        var dlgId = $.ibisMessaging(
						"dialog", 
						"info", 
						'<span style="text-align: left; padding:5px"><p>The following form(s) will be deleted:</p><br\><span style="font-weight:normal"><ul>' + removeString + '</ul></span> <br\><p>Do you want to proceed?</p></span>',
						{
							buttons: [{
								id: "yesBtnB",
								text: "Yes", 
								click: _.debounce(function() {
									$(this).siblings().find("#yesBtnB").prop("disabled", true);
									redirectWithReferrer(url);
								}, 1000, true)
							},
							{
								text: "No",
								click: function() {
									$.ibisMessaging("close",{id: dlgId});
								}
							}],
							modal: true,
							width: "600px"
						});
				}
				
				$button.prop("disabled", false);
			}
		}
		else {
			$button.prop("disabled", false);
			$.ibisMessaging("dialog", "warning", "You must select a form to perform this action.", {width: 600, modal: true});
		}
	}

	function randomize() {
		var $table = $(".dataTableContainer").filter(":visible").find("table");
		var selectedIds = IDT.getSelectedOptions($table);
		
		if ( selectedIds.length == 0 ) {
			var formId = selectedIds[0];
			var numQuestions = $table.find("input[name='numQuestions_" + formId + "']").val();
			var statsQuestions = $table.find("input[name='statsQuestions_" + formId + "']").val();
			
			if ( (numQuestions == "0") || (statsQuestions == "1") ) {
				$.ibisMessaging("dialog", "warning", "Only forms that are active and have at least 1 question can be randomized.");
			}
			else {
				var url = "designRandomization.do?formId=" + formId;
				redirectWithReferrer(url);
			}
		}
	}
	
	$(document).ready(function() {
		initialize('<%=request.getAttribute("formTypeToDisplay")%>');
		
		$(".editFormButton").click(_.debounce(function(event) {
			var $editBtn = $(this);
			var $table = $(".dataTableContainer").filter(":visible").find("table");
			
			$editBtn.prop("disabled", true);
			performEditForm($table, $editBtn);
		}, 1000, true));
		
		$(".viewFormsButton").click(_.debounce(function(event) {
			var $table = $(".dataTableContainer").filter(":visible").find("table");
			
			performViewForm($table);
		}, 1000, true));
		
		$(".saveAsButton").click(_.debounce(function(event) {
			var $saveAsBtn = $(this);
			var $table = $(".dataTableContainer").filter(":visible").find("table");
			
			$saveAsBtn.prop("disabled", true);
			performSaveAs($table, $saveAsBtn);
		}, 1000, true));
		
		$(".viewAuditButton").click(_.debounce(function(event) {
			var $table = $(".dataTableContainer").filter(":visible").find("table");
			
			launchViewAudit($table);
		}, 1000, true));
		
		$(".activateButton").click(_.debounce(function(event) {
			var $activateBtn = $(this);
			var $table = $(".dataTableContainer").filter(":visible").find("table");
			
			$activateBtn.prop("disabled", true);
			activate($table, $activateBtn);
		}, 1000, true));
		
		$(".exportFormButton").click(_.debounce(function(event) {
			var $table = $(".dataTableContainer").filter(":visible").find("table");
			var selected_Form_Id = IDT.getSelectedOptions($table);
			redirectWithReferrer('<s:property value="#webRoot"/>/form/exportForm.action?id=' + selected_Form_Id[0]);
		}, 1000, true));
		
		$(".deleteFormButton").click(_.debounce(function(event) {
			var $deleteBtn = $(this);
			var $table = $(".dataTableContainer").filter(":visible").find("table");
			
			$deleteBtn.prop("disabled", true);
			performDelete($table, $deleteBtn);
		}, 1000, true));
	});
</script>

<%-- <script type="text/javascript">
	EventBus.on("init:table", function(tableModel) {
		var dt = tableModel.get("datatable");
		var $el = tableModel.get("$el").parents(".dataTableContainer");
		var $searchInput = $el.find('.fg-toolbar .dataTables_filter');
		$searchInput.hide();
		var columns = {columns: []};
		dt.api().columns().every(function() {
			var $header = $(this.header());
			if ($header.find(".idtNav").length == 0) {
				columns.columns.push({
					index: this.index(),
					text: $header.text()
				});
			}
		});
		$searchInput.after(Handlebars.compile($("#newSearch").html())(columns));
		// adds a new search method to the search stack, runs it, then pops the method off
		$(".idt-searchInput").on('keyup', function() {
			var searchTerm = this.value.toLowerCase();
			$.fn.dataTable.ext.search.push(function(settings, data, dataIndex) {
				// get all selected checkboxes
				for (var i=0, len=data.length; i < len; i++) {
					if ($el.find('.idt-selectColumnCheckbox[value="'+i+'"]:checked').length > 0) {
						if (~data[i].toLowerCase().indexOf(searchTerm)) return true;
					}
				}
				return false;
			});
			var id = IDT.getId(tableModel.get("$el"));
			IDT.dataTables[id].fnDraw();
			$.fn.dataTable.ext.search.pop();
		});
	});
</script> --%>

<div><s:text name="form.forms.manageForms.instruction" /></div>
<h3><s:text name="form.forms.manageForms.myFormsDisplay" /></h3>


<p>
	<s:text name="form.forms.manageForms.myFormsDisplay.text" />
<%-- 	<select id="formType" onChange="formsToggleVisibility()" class="floatRight"
			title="Select to view Subject Forms, Non-Subject Forms,and All Forms">
		<option value="0">
			<s:text name="form.forms.subjectForms" />
		</option>
		<option value="1">
			<s:text name="form.forms.nonsubjectForms" />
		</option>
		<option value="2">
			<s:text name="form.forms.allForms" />
		</option>
	</select> --%>
</p>
<br/>
<div class="dataTableContainer dataTableJSON" id="patientFormsContent" style="display: block">
	<ul>
		<li>
			<security:hasProtocolPrivilege privilege="addeditforms">
				<input type="button" id="btnEditP" class="editFormButton" value="<s:text name='button.Edit' />" 
					title="<s:text name='tooltip.edit' />" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="viewforms">
				<input type="button" id="btnViewP" class="viewFormsButton" value="<s:text name='button.View' />" 
					title="<s:text name='tooltip.view.form' />" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="addeditforms">
				<input type="button" id="btnSaveAsP" class="saveAsButton" value="<s:text name='form.addform.saveasform' />" 
					title="<s:text name='tooltip.form.addform.saveasform' />" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="viewaudittrails">
				<input type="button" id="btnViewAuditP" class="viewAuditButton" value="<s:text name='button.ViewAudit' />" 
					title="<s:text name='tooltip.viewAudit.form' />" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="addeditforms">
				<input type="button" id="btnActivateP" class="activateButton" value="<s:text name='button.Activate' />" 
					title="<s:text name='tooltip.activate' />" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="importexportforms">
				<input type="button" id="exportFormButton1" class="exportFormButton" value="Export" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="addeditforms">
				<input type="button" id="btnDeleteP" class="enabledOnMany deleteFormButton" value="<s:text name='button.Delete' />" 
					title="<s:text name='tooltip.delete' />" />
			</security:hasProtocolPrivilege>
		</li>
	</ul>

	<idt:jsontable name="protocolforms" scope="request" decorator="gov.nih.nichd.ctdb.form.tag.FormHomeDecorator">
		<idt:setProperty name="basic.msg.empty_list"
			value="There are no Subject Forms for this protocol found at this time." />
		<idt:column property="formIdCheckbox" title="" />
		<idt:column property="nameVersion" title='<%=rs.getValue("form.name.display", l)%>' />
		<idt:column property="numQuestions" title='<%=rs.getValue("response.collect.label.numberofquestions", l)%>' />
		<%-- <idt:column property="associatedFormGroups" title='<%=rs.getValue("form.group.display", l)%>' /> --%>
		<idt:column property="status.shortName" title='<%=rs.getValue("form.forms.formInformation.status", l)%>' />
		<idt:column property="administered" title='<%=rs.getValue("form.administered.display", l)%>' />
		<idt:column property="updateddatetime" title='<%=rs.getValue("form.public.search.date.display", l)%>' />
	</idt:jsontable>

</div>
<div id="data_table_second" class="idtTableContainer">
	<div id="test2"></div>
	<ul class="filter"></ul>
   	<div id="dialog"></div>
    <div id="test1"></div>
	<table id="tableTest" class="table table-striped table-bordered" cellspacing="0" width="100%" />
</div>

<script type="text/javascript">

        // Default plugin options

$(document).ready(function() {
	  $("#tableTest").idtTable({
          idtUrl: 'http://fitbir-portal-local.cit.nih.gov:8084/ibis/form/formList.action',
          idtData: {
              primaryKey: "name"
          },
          pages: 1,
          "processing": false,
          "serverSide": false,
          length: 10,
          "columns": [
            {
                "data": '<%=rs.getValue("form.name.display", l)%>',
                "title":'<%=rs.getValue("form.name.display", l)%>',
                "name": '<%=rs.getValue("form.name.display", l)%>',
                "parameter" : 'name',
                "searchable": true,
                "orderable": true,
                "render": IdtActions.ellipsis(10)
            },
            {
                "data": '<%=rs.getValue("response.collect.label.numberofquestions", l)%>',
                "title": '<%=rs.getValue("response.collect.label.numberofquestions", l)%>',
                "name": '<%=rs.getValue("response.collect.label.numberofquestions", l)%>',
                "parameter" : 'numQuestions',
                "searchable": true,
                "orderable": true
            },
            {
                "data": '<%=rs.getValue("form.forms.formInformation.status", l)%>',
                "title": '<%=rs.getValue("form.forms.formInformation.status", l)%>',
                "name": '<%=rs.getValue("form.forms.formInformation.status", l)%>',
                "parameter" : 'status.shortName',
                "searchable": true,
                "orderable": true
            },
            {
                "data": '<%=rs.getValue("form.administered.display", l)%>',
                "title": '<%=rs.getValue("form.administered.display", l)%>',
                "name": '<%=rs.getValue("form.administered.display", l)%>',
                "parameter" : 'administered',
                "searchable": true,
                "orderable": true
            },
            {
                "data": '<%=rs.getValue("form.public.search.date.display", l)%>',
                "title": '<%=rs.getValue("form.public.search.date.display", l)%>',
                "name": '<%=rs.getValue("form.public.search.date.display", l)%>',
				"parameter" : 'updatedDate',
				"searchable" : true,
				"orderable" : true,
				"render": IdtActions.formatDate()
			} ],
			dom : 'Bfrtip',
			fixedHeader : true,
			select : 'multi',
			bFilter: true,
            filters: [{
                type: 'select',
                name: 'status',
                defaultValue: 'in progress',
 /*                options: [{
                        value: 'marshall',
                        label: 'name',

                    },
                    {
                        value: 'regional director',
                        label: 'regional director'
                    },
                    {
                        value: 'seach',
                        label: 'search'
                    }
                ], */
/*                 bRegex: true,
 */                options: ['active', 'in progress'],
                // containerId: 'test',
                columnIndex: 3,
                // test: function(oSettings, aData, iDataIndex, filterData) {
                //     if(aData[1] == 'Marshall'){
                //         console.log('test it', aData[1]);
                //         return true;
                //     }
                // }
            }
        ],
        buttons: [
        	{
	        	text: 'Row selected data',
	            action: function(e, dt, node, config) {
	            	alert(
	                	'Row data: ' +
	                    JSON.stringify(dt.row({ selected: true }).data())
	                );
	            },
	            className: 'test',
	            enableControl: {
	            	count: 4,
	                invert:true
	            },
	            enabled: true
	
	        },
	        {
		        extend: 'delete',
		        enableControl: {
			        count: 2,
			        invert:false
		        },
	/*      url: 'http://localhost:8089/post'
	 */     },
	        {
		        extend: 'addRow',
		        values: [{'false': ''},true, true, true, true, {'false': "2014-06-10 13:02"}],
		/*      url: 'http://localhost:8089/post'
		 */ 
	        },               
	        {
		        extend: 'print',
		        exportSelected: true,
		        action: IdtActions.exportAction()
	        },
	        {
		        extend: 'csv',
		        action: IdtActions.exportAction()
	        },
			{
		        extend: 'pdf',
		        exportSelected: true,
		        action: IdtActions.exportAction()
	        },
	        {
		        extend: 'excel',
		        exportSelected: true,
		        action: IdtActions.exportAction()
	        }
        ]
  });
});
</script>
<%-- <div id="testtest">
<%=request.getAttribute("formJson")%>
</div> --%>


<%-- <div class="dataTableContainer" id="nonPatientFormsContent" style="display:none">
	<ul>
		<li>
			<security:hasProtocolPrivilege privilege="addeditforms">
				<input type="button" id="btnEditN" class="editFormButton" value="<s:text name='button.Edit' />" 
					title="<s:text name='tooltip.edit' />" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="viewforms">
				<input type="button" id="btnViewN" class="viewFormsButton" value="<s:text name='button.View' />" 
					title="<s:text name='tooltip.view.form' />" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="addeditforms">
				<input type="button" id="btnSaveAsN" class="saveAsButton" value="<s:text name='form.addform.saveasform' />"
					title="<s:text name='tooltip.form.addform.saveasform' />" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="viewaudittrails">
				<input type="button" id="btnViewAuditN" class="viewAuditButton" value="<s:text name='button.ViewAudit' />" 
					title="<s:text name='tooltip.viewAudit.form' />" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="addeditforms">
				<input type="button" id="btnActivateN" class="activateButton" value="<s:text name='button.Activate' />" 
					title="<s:text name='tooltip.activate' />" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="importexportforms">
				<input type="button" id="exportFormButton2" class="exportFormButton" value="Export" />
			</security:hasProtocolPrivilege>
		</li>
		<li>
			<security:hasProtocolPrivilege privilege="addeditforms">
				<input type="button" id="btnDeleteN" class="enabledOnMany deleteFormButton" value="<s:text name='button.Delete' />" 
					title="<s:text name='tooltip.delete' />" />
			</security:hasProtocolPrivilege>
		</li>
	</ul>

	<idt:jsontable name="NonPatientForms" scope="request" decorator="gov.nih.nichd.ctdb.form.tag.FormHomeDecorator">
		<idt:setProperty name="basic.msg.empty_list"
			value="There are no Non-Subject Forms for this protocol found at this time." />
		<idt:column property="formIdCheckbox" title="" />
		<idt:column property="nameVersion" title='<%=rs.getValue("form.name.display", l)%>' />
		<idt:column property="numQuestions" title='<%=rs.getValue("response.collect.label.numberofquestions", l)%>' />
		<idt:column property="associatedFormGroups" title='<%=rs.getValue("form.group.display", l)%>' />
		<idt:column property="status.shortName" title='<%=rs.getValue("form.forms.formInformation.status", l)%>' />
		<idt:column property="administered" title='<%=rs.getValue("form.administered.display", l)%>' />
		<idt:column property="updateddatetime" title='<%=rs.getValue("form.public.search.date.display", l)%>' />
	</idt:jsontable>
</div> --%>

<%-- <div id="editAdministeredFormDecisionInfo" style="display: none" >
	<p>
		This form is administered. <br/>
		Editing this form may affect current data collections. <br/>
		You can either save this form as a new form or you can edit this form. <br/>
	</p>
</div>

<div id="editInProgressFormDecisionInfo" style="display: none" >
	<p>
		This form is in progress. <br/>
		Editing this form may affect current data collections. <br/>
		You can either save this form as a new form or you can edit this form. <br/>
	</p>
</div>

<jsp:include page="/common/footer_struts2.jsp" /> --%>

</html>

