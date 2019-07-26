<%@include file="/common/taglibs.jsp"%>


<div id="dataElementListContainer" class="dataTableContainer dataTableJSON" style="float: center;">
	<idt:jsontable name="dataElementList" id="dataElements" decorator="gov.nih.tbi.taglib.datatableDecorators.DataElementListDecorator">
		<idt:column title="Title" property="titleViewLink" />
		<idt:column title="Variable Name" property="variableName" />
		<idt:column title="Type" property="type" />
		<idt:column title="Modified Date" property="modifiedDate" styleClass="nowrap" />
		<idt:column title="Status" property="status" />
	</idt:jsontable>
</div>

