<%@ tag body-content="scriptless"%>
<%@ attribute name="datasetSet" type="java.util.Collection" required="true"%>
<%@ attribute name="mode" required="true"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>

<table class="display-data full-width">
	<thead>
		<tr>
			<c:if test="${mode == 'edit'}">
				<th style="width: 17px"></th>
			</c:if>
			<th style="width: 20%">Dataset ID</th>
			<th style="width: 32%">Name</th>
			<th style="width: 16%">Submission Date</th>
			<th style="width: 20%">Type</th>
			<th style="width: 10%">Status</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="dataset" items="${datasetSet}" varStatus="rowCounter">
			<c:choose>
          		<c:when test="${rowCounter.count % 2 == 0}">
           			<tr class="stripe">
          		</c:when>
	          	<c:otherwise>
	            	<tr>
	          	</c:otherwise>
        	</c:choose>
				<c:if test="${mode == 'edit'}">
					<td>
						<input id="datasetRadio${dataset.id}" <c:if test="${dataset.datasetRequestStatus == null && dataset.datasetStatus.id != 0 && dataset.datasetStatus.id != 1}">disabled=""</c:if> class="datasetCheckbox" data-requested="${dataset.datasetRequestStatus != null}" data-status="${dataset.datasetRequestStatus!=null?dataset.datasetRequestStatus.name:dataset.datasetStatus.name}" type="checkbox" name="selectedDatasetIds" value="${dataset.id}" />
					</td>
				</c:if>
				<td><label for="datasetRadio${dataset.id}" style="text-align: left; cursor: pointer; display: block; padding-top: 4px; float: none; font-weight: normal;">${dataset.prefixedId}</label></td>
				<td><a href="javascript:viewDataset('${dataset.prefixedId}')">${dataset.name}</a></td>
				<td><ndar:dateTag value="${dataset.submitDate}" /></td>
				<td>${dataset.fileTypeString}</td>
				<td><c:choose>
						<c:when test="${dataset.datasetRequestStatus != null}">
							${dataset.datasetRequestStatus.name}&nbsp;<i>(Pending)</i>
						</c:when>
						<c:otherwise>
							${dataset.datasetStatus.name}
						</c:otherwise>
					</c:choose></td>
			</tr>
		</c:forEach>
	</tbody>
</table>

<script type="text/javascript">	
	function viewDataset(datasetId) {
		$.fancybox.showActivity();
		$.post(	"datasetAction!viewLightbox.ajax", 
			{ datasetId:datasetId }, 
			function (data) {
				$.fancybox(data);
				$("#fancybox-wrap").unbind('mousewheel.fb');
			}
		);
	}
	
	$('.datasetCheckbox').click(function() {
		updateStatusOptions($(this));
	});
</script>