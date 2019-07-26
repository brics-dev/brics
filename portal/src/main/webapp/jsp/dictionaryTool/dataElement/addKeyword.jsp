<%@include file="/common/taglibs.jsp"%>
<s:set var="keywordNew" value="keywordNew" />

<s:form id="newKeywordForm" action="keywordInterfacedataElementValidationAction" method="post" validate="false">
<s:token />
	
	<div class="form-field form-field-vert">
		<input type="hidden" value="UnusedValue" name="sessionKeywords" />
		<s:fielderror fieldName="keywordSearchKey" cssStyle="float-left" />
		<input type="text" name="PREVENT_SUBMISSION" value="Fix single input form bug" style="display: none" />
	</div>
</s:form>
