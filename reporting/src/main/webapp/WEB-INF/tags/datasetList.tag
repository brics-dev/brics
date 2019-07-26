<%@ tag body-content="scriptless"%>
<%@ attribute name="datasetList" type="java.util.Collection" required="true"%>

<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>

<table class="display-data full-width">
	<tr>
		<s:set var="columns"
			value="{{'Name', 'name'}, {'Study', 'stdy.title'}, {'Submitter', 'sub.lastName'}, {'Date Submitted', 'submitDate'}, {'Status', 'datasetStatus'}}" />
		<s:iterator var="name" value="#columns">
			<th>
				<div class="no-wrap">
					<s:if test="#name[1] != ''">
						<a href="javascript:datasetSetSort('<s:property value='#name[1]' />')"> <s:property value="#name[0]" />
						</a>
					</s:if>
					<s:else>
						<s:property value="#name[0]" />
					</s:else>
					<s:if test="#name[1] == sort">
						<s:if test="ascending">
							<img src='<s:url value="/images/brics/common/icon-down.png"/>'>
						</s:if>
						<s:else>
							<img src='<s:url value="/images/brics/common/icon-up.png"/>'>
						</s:else>
					</s:if>
				</div>
			</th>
		</s:iterator>
	</tr>
	<s:iterator var="dataset" value="datasetList" status="status">
		<c:choose>
			<c:when test="${status.count%2 == 0}">
				<tr class="stripe">
			</c:when>
			<c:otherwise>
				<tr>
			</c:otherwise>
		</c:choose>
		<td><s:if test="inAdmin">
				<a
					href="/portal/studyAdmin/datasetAction!view.action?datasetId=<s:property value='#dataset.prefixedId' />"><s:property
						value="#dataset.name" /></a>
			</s:if> <s:else>
				<a href="/portal/study/datasetAction!view.action?datasetId=<s:property value='#dataset.prefixedId' />"><s:property
						value="#dataset.name" /></a>
			</s:else></td>
		<td><s:if test="inAdmin">
				<a
					href="/portal/studyAdmin/studyAction!view.action?source=datasetList&studyId=<s:property value='#dataset.study.prefixedId' />"><s:property
						value="#dataset.study.title" /></a>
			</s:if> <s:else>
				<a href="/portal/study/studyAction!view.action?studyId=<s:property value='#dataset.study.prefixedId' />"><s:property
						value="#dataset.study.title" /></a>
			</s:else></td>

		<td><s:property value="#dataset.submitter.fullName" /></td>
		<td><ndar:dateTag value="${dataset.submitDate}" /></td>
		<td>
			<s:if test="#dataset.datasetRequestStatus != null">
				<span class="red-text"> <s:property value="#dataset.datasetRequestStatus.name" />&nbsp;<i>(Pending)</i>
				</span>
			</s:if> 
			<s:else>
				<s:property value="#dataset.datasetStatus.name" />
			</s:else>
		</td>

		</tr>
		<!-- Warning due to <tr> tag being created inside a choose tag -->
	</s:iterator>
</table>
