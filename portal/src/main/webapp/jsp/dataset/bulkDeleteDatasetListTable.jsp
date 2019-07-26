<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@include file="/common/taglibs.jsp"%>

<div id="datasetList" class="dataTableContainer dataTableJSON">
	<idt:jsontable name="datasetList" id="dataset" decorator="gov.nih.tbi.taglib.datatableDecorators.DatasetBulkListDecorator">
		<idt:setProperty name="basic.msg.empty_list" value="There are no dataset lists at this time." />
		<idt:column title="NAME" property="nameLink" />
		<idt:column title="STUDY" property="studyName" />
		<idt:column title="SUBMITTER" property="submitter" />
		<idt:column title="DATE SUBMITTED" property="submitDate" styleClass="nowrap" decorator="gov.nih.tbi.taglib.datatableDecorators.DateColumnDecorator" />
		<idt:column title="STATUS" property="bulkDatasetStatus" />
		<idt:column title="ACCESS RECORD?" property="accessRecord" />
	</idt:jsontable>
</div>