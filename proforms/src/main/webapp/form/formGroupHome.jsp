<jsp:include page="/common/doctype.jsp" />
<%@ page import="gov.nih.nichd.ctdb.common.rs,java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<security:check privileges="viewforms"/>
<%
	String mode = (String)request.getAttribute("mode");
	Locale l = request.getLocale();
%>
<html>

<s:set var="pageTitle" scope="request">
	<s:text name="form.forms.formGroupsDisplay"/>
</s:set>

<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript">
	// Initializes the form table with any previously selected froms.
	function tableInitListener(dataTable) {
		var $formsTable = $("#formsToAttachDiv table");
		
		// Check if the forms table is being initialized, and the page is in edit mode.
		if ( ($("#showAddEdit").val() == "true") && _.isEqual(dataTable, IDT.getTableModel($formsTable)) ) {
			var formIds = $.parseJSON($("#formIdJsonArray").val());
			
			for ( var i = 0; i < formIds.length; i++ ) {
				var id = formIds[i].toString();
				IDT.addSelectedOptionValue($formsTable, id);
			}
			
			// Expand the add edit form group section.
			$("#addEditFormGroupDivHeader").click();
		}
	}
	
	$(document).ready(function() {
		// Bind to any IBIS DataTable events
		EventBus.on("init:table", tableInitListener, this);
		
		// Page event listeners.
		$("#btnCancel").click(_.debounce(function(event) {
			var url = "<s:property value='#webRoot'/>/form/formGroupHome.action";
			
			redirectWithReferrer(url);
		}, 1000, true));
		
		$("#btnEdit").click(_.debounce(function(event) {
			$(this).prop("disabled", true);
			
			var selectedId = IDT.getSelectedOptions($("#formGroupsDiv table"));
			var $form = $("#formGroupForm");
			
			// Set the form to get the selected form group
			$form.find("#formGroupId").val(selectedId[0]);
			$form.submit();
		}, 1000, true));
		
		$("#resetBtn").click(_.debounce(function(event) {
			var $table = $("#formsToAttachDiv table");
			var formIds = $.parseJSON($("#formIdJsonArray").val());
			
			// Clear the form table selections, and re-populate with original values if able.
			IDT.clearSelected($table);
			
			for ( var i = 0; i < formIds.length; i++ ) {
				var id = formIds[i].toString();
				IDT.addSelectedOptionValue($table, id);
			}
			
			// Reset the rest of the form data.
			$("#formGroupForm").get(0).reset();
		}, 1000, true));
		
		$("#btnDelete").click(function(event) {
			var msg = '<s:text name="form.formgroup.alert.remove"/>';
			
			$.ibisMessaging("dialog", "info", msg, {
				modal : true,
				buttons : [{
					id : "yesBtn",
					text : "<s:text name='button.yes'/>",
					
					click : _.debounce(function() {
						// The "Yes" button
						$(this).siblings().find("#yesBtn").prop("disabled", true);
						
						var params = {
							id : IDT.getSelectedOptions($("#formGroupsDiv table"))
						};
						
						var $confirmBx = $(this);
						
						// Perform the GET request to delete the selected form group(s).
						$.get("<s:property value='#webRoot'/>/form/deleteFormGroup.action", params, function(data, textStatus, jqXHR) {
							redirectWithReferrer("<s:property value='#webRoot'/>/form/formGroupHome.action");
						})
						.fail(function(jqXHR, textStatus, errorThrown) {
							$("#messageContainer").empty();
							
							switch ( jqXHR.status ) {
								case 500:
									$.ibisMessaging("primary", "error", "Could not delete some or all of the selected form group(s).");
									break;
								case 403:
									redirectWithReferrer("<s:property value='#webRoot'/>/pickStudy.action?id=0");
									break;
								default:
									$.ibisMessaging("primary", "error", "Could not delete some or all of the selected form group(s).");
									break;
							}
							
							$confirmBx.siblings().find("#yesBtn").prop("disabled", false);
							$confirmBx.dialog("close");
						});
					}, 1000, true)
				},
				
				{
					text : "<s:text name='button.no'/>",
					
					click : function() {
						$(this).dialog("close");
					}
				}]
			});
		});
		
		$("#formGroupForm").submit(function(event) {
			var selectedForms = IDT.getSelectedOptions($("#formsToAttachDiv table"));
			
			$("#formIdJsonArray").val(JSON.stringify(selectedForms));
		});
	});

</script>

<security:hasProtocolPrivilege privilege="addeditforms">

	<div><s:text name="form.forms.formGroups.instruction"/></div>	
				
	<h3 class="toggleable collapsed" id="addEditFormGroupDivHeader"><s:text name="form.forms.formGroups.addNewGroupDisplay"/></h3>
	<div id="addEditFormGroupDiv" >
	    <s:form theme="simple" method="post" id="formGroupForm">
	     	<s:hidden name="id" id="formGroupId" />
	     	<s:hidden name="showAddEdit" id="showAddEdit" />
	     	<s:hidden name="selectedFormJson" id="formIdJsonArray" />
	
			<div><s:text name="form.forms.formGroups.addSubInstruction"/></div>
			<label class="requiredInput"></label> 
			<i><s:text name="form.forms.formGroups.requiredSymbol"/></i>
			<br/><br/>
			  
			<div class="formrow_1">
				<label for="name" class="requiredInput"><s:text name="form.formgroup.name.display"/></label>
				<s:textfield id="fgName" name="name" maxlength="255" size="35"/>
			</div>
			
			<div class="formrow_1">
				<label for="description"><s:text name="app.label.lcase.description"/></label>
				<s:textarea id="fgDesc" name="description" rows="5" cols="35"/>
			</div>
			
			<div id="formsDiv" class="formrow_1">
				<label><s:text name="form.forms.formGroups.associateForms.text"/></label>
			 	<div class="dataTableContainer formrowinput" id="formsToAttachDiv">
					<idt:jsontable name="protocolforms" scope="request" decorator="gov.nih.nichd.ctdb.form.tag.FormGroupHomeDecorator">
						<idt:setProperty name="basic.msg.empty_list" value="There are no Patient Forms for this protocol found at this time."/>
				        <idt:column property="formIdsToAttach" title="" />
				        <idt:column property="id" title="Form ID" />
				        <idt:column property="name" title="Form Name" />
				        <idt:column property="status.shortName" title="Status" />
					</idt:jsontable>
				</div>		
			</div>
			
			<div class="formrow_1">
				<input type="button" id="btnCancel" value="<s:text name='button.Cancel'/>" title="<s:text name='tooltip.cancel'/>" />
				<input type="button" id="resetBtn" value="<s:text name='button.Reset' />" title="Click to clear fields"/>
			    <s:submit action="saveFormGroup" key="button.Save" title="%{getText('tooltip.save')}"/>
			</div>
	    </s:form>
	</div>
	
</security:hasProtocolPrivilege>

<h3><s:text name="form.forms.formGroups.myGroupsDisplay"/></h3>
<p><s:text name="form.forms.formGroups.myGroups.text"/></p>

<div class="dataTableContainer" id="formGroupsDiv">
	<ul>
		<li> 
			<security:hasProtocolPrivilege privilege="addeditforms">
				<input type="button" id="btnEdit" value="<s:text name='button.Edit'/>" title="<s:text name='tooltip.edit'/>" />
			</security:hasProtocolPrivilege>
		</li>
		<li> 
			<security:hasProtocolPrivilege privilege="addeditforms">
				<input type="button" id="btnDelete" class="enabledOnMany" value="<s:text name='button.Delete'/>" title="<s:text name='tooltip.delete'/>" />
			</security:hasProtocolPrivilege>
		</li>
	</ul>

	<idt:jsontable name="formGroups" scope="request" decorator="gov.nih.nichd.ctdb.form.tag.FormGroupHomeDecorator" >
    	<idt:setProperty name="basic.msg.empty_list" value="There are no form groups to display at this time."/>
    	<security:hasProtocolPrivilege privilege="addeditforms">
    		<idt:column property="formGroupIdCheckbox" title="" />
    	</security:hasProtocolPrivilege>
    	<idt:column property="name" title='<%=rs.getValue("form.formgroup.name.display",l)%>' />
    	<idt:column property="description" title='<%=rs.getValue("form.formgroup.description.display",l)%>'/>
    	<idt:column property="associatedForms" title='<%=rs.getValue("form.formgroup.associatedForms",l)%>'  />
	</idt:jsontable>
</div>


<jsp:include page="/common/footer_struts2.jsp" />
</html>