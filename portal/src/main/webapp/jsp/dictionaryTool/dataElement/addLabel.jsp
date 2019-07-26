<%@include file="/common/taglibs.jsp"%>
<s:set var="labelNew" value="labelNew" />

<s:form id="newLabelForm" action="labelInterfacedataElementValidationAction" method="post" validate="false">
<s:token />
	
	<div class="form-field form-field-vert">
		<input type="hidden" value="UnusedValue" name="sessionLabels" />
		<s:fielderror fieldName="labelSearchKey" cssStyle="float-left" />
		<input type="text" name="PREVENT_SUBMISSION" value="Fix single input form bug" style="display: none" />
	</div>
</s:form>
