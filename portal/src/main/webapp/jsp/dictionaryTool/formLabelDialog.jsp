<%@include file="/common/taglibs.jsp"%>

	<div class="form-field">
		<label for="currentLabel">Label :</label>
		<s:textfield id="currentLabel" name="currentLabel" escapeHtml="true" escapeJavaScript="true" maxlength="255" />
		<s:fielderror fieldName="currentLabel" />
		<s:hidden id="labelId" name="labelId"/>
	</div>
