<%@ tag body-content="scriptless"%>
<%@ attribute name="readOnly" type="java.lang.Boolean" required="true"%>
<%@ attribute name="admin" type="java.lang.Boolean" required="true"%>

<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>

<h4>Classification</h4>


<%-- Read only for viewing data  --%>
<c:if test="${readOnly}">
	<div id="classificationInterface" class="form-output">
		<s:iterator var="classificationElement" value="currentDataElement.classificationElementList" status="status">
	
			<div class="form-output">
				<div class="label">
				
				<s:if test="%{#classificationElement.subgroup.subgroupName != null && #classificationElement.subgroup.subgroupName != ''}">
				<%-- this should be sub group --%>
					<s:property value="#classificationElement.subgroup.subgroupName" />
				</s:if>
				<s:else>
				<s:property value="#classificationElement.disease.name" />
				</s:else>
					:
				</div>
				<div class="readonly-text">
					<s:property value="#classificationElement.classification.name" />
				</div>
			</div>
		</s:iterator>
	</div>
</c:if>
<%-- Editable fields --%>
<c:if test="${!readOnly}">


	<div id="classificationInterface">
		<%-- This field fires the custom validator for classification --%>
		<s:hidden name="dataElementForm.classification" value="" />
		<s:fielderror fieldName="dataElementForm.classification" />

		<s:iterator var="classificationElement" value="dataElementForm.classificationElementList" status="status">
			<div class="form-field">
				<label class="required"><s:property value="#classificationElement.subgroup.subgroupName" /> <span
					class="required">* </span>:</label>
				<c:if test="${admin}">
					<s:set var="classList" value="classificationList" />
				</c:if>
				<c:if test="${!admin}">
					<s:set var="classList" value="userClassificationList" />
				</c:if>

				<s:if test="#classList.size==1">
					<s:set var="startValue" value="#classList[0].id" />
					<s:hidden name="dataElementForm.classificationElementList[%{#classificationElement.subgroup.id}]"
						value="%{#startValue}" />
				</s:if>
				<s:else>
					<s:set var="startValue" value="#classificationElement.classification.id" />
				</s:else>

				<s:select id="dataElementForm.classificationElementList[%{#classificationElement.subgroup.id}]"
					name="dataElementForm.classificationElementList[%{#classificationElement.subgroup.id}]" list="#classList"
					listKey="id" listValue="name" value="#startValue" headerKey="" headerValue="- Select One -"
					disabled="isPublished || #classList.size==1" />
			</div>
		</s:iterator>
	</div>

</c:if>