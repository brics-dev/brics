<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@include file="/common/taglibs.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<s:set var="currentDataElement" value="currentDataElement" />
<div class="lightbox-content-wrapper" style="min-width: 800px">
<h3>
	<s:property escapeHtml="false" value="currentDataElement.category.name" />:&nbsp;
	<s:property escapeHtml="false" value="currentDataElement.title" />
</h3>

<div class="clear-float line">

	<!-- First column of of data from the items.
					 Items with long descriptions (and to get cropped on the page)
					 See guidelines as an example.
					 The less function must also be called for each cropped item in the js at
					 the bottom of the page. -->
					 				 
		<div class="unit size1of3">
		<div class="mod">
			<h4>General Details</h4>
			<div class="form-output">
				<s:label cssClass="label" key="label.dataElement.title" />
				<div class="readonly-text">
					<s:property escapeHtml="false" value="currentDataElement.title" />
				</div>
			</div>
			<div class="form-output">
				<s:label cssClass="label" key="label.dataElement.name" />
				<div class="readonly-text">
					<s:property value="currentDataElement.name" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Short Description:</div>
				<div class="readonly-text">
					<s:property escapeHtml="false" value="currentDataElement.shortDescription" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Definition:</div>
				<div class="readonly-text">
					<s:property escapeHtml="false" value="currentDataElement.description" />
				</div>
			</div>
			
		<%-- <div class="form-output">
					<div class="readonly-text">
						<s:iterator var="externalId" value="currentDataElement.externalIdSet" status="externalIdStatus">
						<s:if test="%{#externalId.value==''}">
							<div class="label"><s:property value="#externalId.type.value" />:</div>		
								<div class="readonly-text limitLength" id="loinc">
								<s:property value="#externalId.value" />
							</div>			
						</s:if>	
						</s:iterator>
					</div>
				</div> --%>
			
			
<!-- 			<div class="form-output"> -->
				
<!-- 				<div class="readonly-text limitLength" id="loinc"> -->
<%-- 					<s:property value="mapElement.loinc.value" /> --%>
<!-- 				</div> -->
<!-- 			</div> -->
	
<!-- 			<div class="form-output"> -->
<!-- 				<div class="label">caDSR ID:</div> -->
<!-- 				<div class="readonly-text limitLength" id="cadsr"> -->
<%-- 					<s:property value="mapElement.cadsr.value" /> --%>
<!-- 				</div> -->
<!-- 			</div> -->
	
<!-- 			<div class="form-output"> -->
<!-- 				<div class="label">SNOMED ID:</div> -->
<!-- 				<div class="readonly-text limitLength" id="snomed"> -->
<%-- 					<s:property value="mapElement.snomed.value" /> --%>
<!-- 				</div> -->
<!-- 			</div> -->
<!-- 			<div class="form-output"> -->
<!-- 				<div class="label">CDISC ID:</div> -->
<!-- 				<div class="readonly-text limitLength" id="cdisc"> -->
<%-- 					<s:property value="mapElement.cdisc.value" /> --%>
<!-- 				</div> -->
<!-- 			</div> -->
<!-- 			<div class="form-output"> -->
<!-- 				<div class="label">NINDS ID:</div> -->
<!-- 				<div class="readonly-text limitLength" id="ninds"> -->
<%-- 					<s:property value="mapElement.ninds.value" /> --%>
<!-- 				</div> -->
<!-- 			</div> -->
			
			<div class="form-output">
				<div class="label" id="publication-title">Publication Status:</div>
				<div class="readonly-text" id="publication">
					<s:property value="currentDataElement.status.name" />
				</div>
			</div>
			<h4>Data Definition</h4>	
			<div class="form-output">
				<div class="label">Data Type:</div>
				<div class="readonly-text">
					<s:property value="currentDataElement.type.value" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Input Restrictions:</div>
				<div class="readonly-text">
					<s:property value="currentDataElement.restrictions.value" />
				</div>
			</div>
			<div class="form-output">
				<div class="label">Maximum Character Quanitity:</div>
				<div class="readonly-text">
					<s:property value="currentDataElement.size" />
				</div>
			</div>
			<s:if test="currentDataElement.minimumValue!=null">
				<div class="form-output">
					<div class="label">Minimum Value:</div>
					<div class="readonly-text">
						<s:property value="currentDataElement.minimumValue" />
					</div>
				</div>
			</s:if>
			<s:if test="currentDataElement.maximumValue!=null">
				<div class="form-output">
					<div class="label">Maximum Value:</div>
					<div class="readonly-text">
						<s:property value="currentDataElement.maximumValue" />
					</div>
				</div>
			</s:if>
			
			<s:if test="currentDataElement.measuringUnit!=null">
				<div class="form-output">
					<div class="label">Unit of Measure:</div>
					<div class="readonly-text">
						<s:property value="currentDataElement.measuringUnit" />
					</div>
				</div>
			</s:if>	
			<%--
			<s:if test="currentDataElement.size!=null">
				<div class="form-output">
					<div class="label">Element Size:</div>
					<div class="readonly-text">
						<s:property value="currentDataElement.size" />
					</div>
				</div>
			</s:if>--%>
			<div class="form-output">
				<div class="label" id="notes-title">Notes:</div>
				<div class="readonly-text" id="notes">
					<s:property escapeHtml="false" value="currentDataElement.notes" />
				</div>
			</div>
			
			<div class="form-output">
				<div class="label">Created By:</div>
				<div class="readonly-text limitLength" id="createdBy">
					<s:property escapeHtml="false" value="currentDataElement.createdBy" />
				</div>
			</div>

			<div class="form-output">
				<div class="label" id="historical-notes-title">Historical Notes:</div>
				<div class="readonly-text" id="historical-notes">
					<s:property escapeHtml="false" value="currentDataElement.historicalNotes" />
				</div>
			</div>
			<div class="form-output">
				<div class="label" id="guidelines-title">Guidelines & Instructions:</div>
				<div class="readonly-text" id="guidelines">
					<s:property escapeHtml="false" value="currentDataElement.guidelines" />
				</div>
			</div>
			<div class="form-output">
				<div class="label" id="question-title">Preferred Question Text:</div>
				<div class="readonly-text" id="question">
					<s:property escapeHtml="false" value="currentDataElement.suggestedQuestion" />
				</div>
			</div>
			<div class="form-output">
				<div class="label" id="references-title">References:</div>
				<div class="readonly-text" id="references">
					<s:property escapeHtml="false" value="currentDataElement.references" />
				</div>
			</div>
	
			
		</div>
	</div>


	<!-- Column 2 of Category Groups -->
		<div class="unit size1of3">
		
		<div class="mod">
			<h4>Categorization</h4>
			<div class="form-output">
			<div class="form-output">
				<div class="label">Population:</div>
				<div class="readonly-text">
					<s:property value="currentDataElement.population.name" />
				</div>
			</div>
			<s:if test="currentKeywords!=null">
				<div class="form-output">
					<div class="label">Keywords:</div>
					<div class="readonly-text">
						<s:iterator var="keywordFromList" value="currentKeywords" status="keywordStatus">
							<c:out value="${keywordFromList.keyword}" />
							<s:if test="!#keywordStatus.last">,</s:if>
						</s:iterator>
					</div>
				</div>
			</s:if>
			<s:else>
				<div class="form-output">
					<div class="label">Keywords:</div>
					<div class="readonly-text">
						<s:iterator var="keywordFromList" value="currentDataElement.keywords" status="keywordStatus">
							<c:out value="${keywordFromList.keyword}" />
							<s:if test="!#keywordStatus.last">,</s:if>
						</s:iterator>
					</div>
				</div>
			</s:else>
			
<!-- 		 <table border="1" class="display-data"> -->
<!--             <tr><th width="25%">Disease</th><th width="25%">Domain</th><th width="25%">Sub-Domain</th><th>Classification</th> </tr> -->
<!--             	 <s:iterator var="diseaseElement" value="currentDataElement.structuralDataElement.diseaseList" status="diseaseStatus"> -->
<!--             	<tr> -->
<%--             	<td><strong><c:out value="${diseaseElement.disease.name}" /></strong></td> --%>
<!--             	<td colspan="2"> -->
            	
<!--             	<table cellspacing="0" cellpadding="0" width="100%" border="1"> -->
            	
<!--             	<s:iterator var="domainPair" value="#diseaseElement.domainList" status="domainStatus"> -->
<!--             	<tr><td width="50%" style="background-color:transparent !important;"> -->
<%-- 						<c:out value="${domainPair.domain.name}" /></td> --%>
<%-- 						<td width="50%" style="background-color:transparent !important;"><c:out value="${domainPair.subdomain.name}" /></td></tr> --%>
						
<!-- 					</s:iterator> -->
					
<!-- 					</table> -->
					
					
<!-- 					</td> -->
<!-- 					<td>
					
<%-- <%-- Read only for viewing data  --%> --%>
<!-- <s:iterator var="classificationElement" value="mapElement.structuralDataElement.classificationElementList" status="status"> -->
<!-- 				<div class="form-output"> -->
<!-- 					<div class="label"> -->
<!-- 						<s:property value="#classificationElement.subgroup.subgroupName" /> -->
<!-- 						: -->
<!-- 					</div> -->
<!-- 					<div class="readonly-text"> -->
<!-- 						<s:property value="#classificationElement.classification.name" /> -->
<!-- 					</div> -->
<!-- 				</div> -->
<!-- 			</s:iterator> -->
	
					
					
<!-- 					</td> --> -->
<!--             	</tr></s:iterator></table>  -->
			
				<h4>Classification</h4>
			<s:iterator var="classificationElement" value="currentDataElement.classificationElementList" status="status">
				<div class="form-output">
					<div class="label">
						<s:property value="#classificationElement.subgroup.subgroupName" />
						:
					</div>
					<div class="readonly-text">
						<s:property value="#classificationElement.classification.name" />
					</div>
				</div>
			</s:iterator>
				
			</div>
			
			
				</div>
	</div>
	
	
		<div class="unit size1of3 lastUnit">
		
			<h4>Pre-Defined Values</h4>
			
			<div class="scrollDiv">
				<!-- A table that displays the Acceptable Value ranges -->
				
				<table class="display-data full-width">
					<thead>
						<tr>
							<th>Permissible Value</th>
							<th class="alphanumericRange">Description</th>
							<th>Output Code</th>
						</tr>
					</thead>
					<tbody>
						<s:iterator value="currentDataElement.valueRangeList" var="valueRange">
							<tr>
								<td><s:property escapeHtml="false" value="valueRange" /></td>
								<td><s:property escapeHtml="false" value="description" /></td>
								<td><s:property escapeHtml="false" value="outputCode" /></td>
							</tr>
						</s:iterator>
					</tbody>
				</table>
				
				<s:if test="currentDataElement.measuringUnit!=null">
					<div class="form-output">
						<div class="label">Unit of Measure:</div>
						<div class="readonly-text">
							<s:property value="currentDataElement.measuringUnit" />
						</div>
					</div>
				</s:if>
			</div>
			
			<div class="form-output">
				<div class="readonly-text underLabel"><a href="schemaMappingAction!viewSchemaMappingValues.action?dataElement=<s:property value="currentDataElement.name" />">External Schema Permissible Value Mapping</a></div>
			</div>
		</div>
			

</div>
<s:if test="dataStructureId != null">
	<br />
	<div class="action-button align-center">
		<a href="javascript: newDSLightbox( ${dataStructureId} );">Back</a>
	</div>
</s:if>

</div>
<script type="text/javascript">
	$('document').ready(function() {
		$("a.lightbox").fancybox();
	});
</script>