<%@include file="/common/taglibs.jsp"%>

<div id="eformListContainer" class="dataTableContainer dataTableJSON">
	<idt:jsontable name="basicEformList" id="eForms" decorator="gov.nih.tbi.taglib.datatableDecorators.EformListDecorator">
		<idt:setProperty name="basic.msg.empty_list"
			value="You have no visible eForms at this time." />
		<idt:column title="eForm Title" property="titleLink" />
		<idt:column title="Short Name" property="shortName" />
		<idt:column title="eForm Status" property="status" styleClass="nowrap" />
		<idt:column title="Modified Date" property="modifiedDate" styleClass="nowrap" />
		<idt:column title="Form Structure Title" property="formStructureTitle" />
	</idt:jsontable>
</div>