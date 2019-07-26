<jsp:include page="/common/doctype.jsp" />
<%@ page import="java.util.Locale"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs" %>
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="manageAttachmentCategories" />
<s:if test="%{type == @gov.nih.nichd.ctdb.common.CtdbConstants@ATTACHMENT_TYPEID_STUDY}">
	<s:set var="key" value="patient.study.title" />
</s:if>
<s:elseif test="%{type == @gov.nih.nichd.ctdb.common.CtdbConstants@ATTACHMENT_TYPEID_PATIENT}">
	<s:set var="key" value="subject.title.display" />
</s:elseif>
<s:elseif test="%{type == @gov.nih.nichd.ctdb.common.CtdbConstants@ATTACHMENT_TYPEID_FORM}">
	<s:set var="key" value="form.forms.formInfo.FormDisplay" />
</s:elseif>
<s:elseif test="%{type == @gov.nih.nichd.ctdb.common.CtdbConstants@ATTACHMENT_TYPEID_SAMPLE}">
	<s:set var="key" value="form.sample.display" />
</s:elseif>

<%
	Locale l = request.getLocale();
%>

<html>
<jsp:include page="/common/popUpHeader_struts2.jsp" />
	
<script type="text/javascript">

$(document).ready(function() {
 	var otb = IDT.dataTables[IDT.getId($("#attCatContainer table"))];
 	
	if ($("div#attCatContainer ul li").size() == 0) {
		otb.fnSetColumnVis(0, false);
	}
	
	// Show add/edit attachment category section if there are any displayable errors
	if ( $("#addEditCategories").is(":hidden") && ($("#messageContainer .ibisMessaging-error").length > 0) ) {
		$("#addEditSectionTitle").click();
	}
	
	$("input#btnSaveCategory").click(function(event) {
		IDT.clearSelected($("#attCatContainer table"));
		
		if ($.trim($("input#name").val()).length == 0) {
			$("#messageContainer").empty();
			$.ibisMessaging("primary", "error", "Name is required.");
			
			return false;
		}
	});
	
	$("input#btnCancelCategory").click(function(event) {
		$("input[name='name']").val("");
		$("textarea[name='description']").val("");
		$("#attchCatId").val("-1");
		IDT.clearSelected($("#attCatContainer table"));
        $("#addEditSectionTitle").click();
        
		return false;
	});
	
	$("#btnResetCategory").click(function(event) {
		var $tableContainer = $("#attCatContainer");
		var $table = $tableContainer.find("table");
		
		IDT.clearSelected($table);
		
		// IDT work arounds
		// TODO: Remove when new IDT is used
		$table.find("tr").removeClass("row_selected");
		$tableContainer.find("input:button, input:submit").prop("disabled", true);
	});
	
	$("input#btnEditCategory").click(function() {
		var selected = IDT.getSelectedOptions($("#attCatContainer table")).length;
		
		if (selected != 1) {
			alert("Warning: Only one attachment category can be selected for editing!");
			return false;
		}

		var selectedAttCatId = IDT.getSelectedOptions($("#attCatContainer table"))[0];
		$("#attchCatId").val(selectedAttCatId);
 		$("#attachmentCategoryForm").submit();
	});

	$("#btnDeleteCategory").click(function(event) {
		var confirmOutput = confirm("<s:text name='attachmentcategory.alert.remove' />");
		
		if (!confirmOutput) {
			return false;
		}
	});
});

</script>

<body>
<jsp:include page="/common/messages_struts2.jsp" />
<div id="wrap" style="text-align: left;">
	<s:form theme="simple" method="post" id="attachmentCategoryForm">
		<s:hidden name="protocolId" />
		<s:hidden name="id" id="attchCatId" />
		<s:hidden name="type" />
            
		<security:hasProtocolPrivilege privilege="manageattachments">
		<div id="divAttInputs">
			<p><s:text name="attachmentcategory.instruction.display"/></p>
			<s:if test="%{id > 0}">
				<h3 id="addEditSectionTitle" class="toggleable"><s:text name="attachmentcategory.editTitle.display"/></h3>
			</s:if>
			<s:else>
				<h3 id="addEditSectionTitle" class="toggleable collapsed"><s:text name="attachmentcategory.addNewTitle.display"/></h3>
			</s:else>
	  		
	  		<div id="addEditCategories"> 
	  			<div class="formrow_1" > 
	  				<s:text name="attachmentcategory.add.instruction"/><br/>
	  	    		<label class="requiredInput"></label> 
	        		<i><s:text name="attachmentcategory.requiredSymbol.display"/></i><br/><br/>
	     		</div>	
	            <div class="formrow_2">
	            	<label for="name" class="requiredInput"><s:text name="attachmentcategory.nameLabel.display"/></label>
	            	<a></a>
					<s:textfield name="name" id="name" size="50" maxlength="50" cssStyle="width:318px;"/>
				</div>
				<div class="clearboth"></div>
				<div class="clearboth"></div>
				<div class="clearboth"></div>
				<div class="formrow_2">
					<label for="description"><s:text name="attachmentcategory.descriptionLabel.display"/></label>  
	      			<a></a>	
	      			<s:textarea name="description" cols="25" rows="4" cssStyle="width:318px;"/>
				</div>
				<div class="formrow_1">
		 			<input type="button" id="btnCancelCategory" value='<s:text name="button.Cancel"/>' title="Click to cancel(changes will not be saved)."/> 
		 			<input type="reset" id="btnResetCategory" value='<s:text name="button.Reset"/>' title="Click to clear fields" />
					<s:submit action="saveAttachmentCategory" id="btnSaveCategory" key="button.Save" title="Click to save changes"/>
		 		</div>
		 	</div>
		</div>
		</security:hasProtocolPrivilege>
	
		<div id="divDisplayCategory">
		    <h3><s:text name="attachmentcategory.subtitleMy.display"/></h3> 
		    <div><s:text name="attachmentcategory.subinstructionMy.display"/></div>
		    <div id="attCatContainer" class="dataTableContainer">
				<ul>
					<security:hasProtocolPrivilege privilege="manageattachments">
						<li> 
							<s:submit id="btnEditCategory" action="attachmentCategory" key="button.Edit" title="Click to make changes"/> 
						</li>
					</security:hasProtocolPrivilege>
					<security:hasProtocolPrivilege privilege="manageattachments">
						<li> 
							<s:submit id="btnDeleteCategory" action="deleteAttachmentCategory" cssClass="enabledOnMany" key="button.Delete" title="Click to delete" /> 
						</li>
					</security:hasProtocolPrivilege>
				</ul>
				<display:table name="_attachmentCategories" scope="request" decorator="gov.nih.nichd.ctdb.attachments.tag.AttachmentCategoryHomeDecorator" >
					<display:column nowrap="true" property="checkbox" title="" />
					<display:column property="name" title='<%= rs.getValue("attachmentcategory.attachmentName", l) %>' />
					<display:column property="description" title='<%= rs.getValue("attachmentcategory.attachmentDescription", l) %>' />
				</display:table>	 
		    </div>
		</div>
	</s:form>
</div>
</body>  

</html>
