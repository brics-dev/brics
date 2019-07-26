<%@include file="/common/taglibs.jsp"%>

<s:if test="currentDatasetList!=0">
<ndar:datasetTable mode="view" datasetSet="${currentDatasetList}" /> 
</s:if>