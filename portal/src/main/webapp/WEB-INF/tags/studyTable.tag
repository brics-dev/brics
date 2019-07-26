<%@ tag body-content="scriptless"%>
<%@ attribute name="studyList" type="java.util.Collection" required="true"%>

<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>

<table class="display-data full-width">
	<tr>
		<!-- 	An array of the table headers. The first value in each pair is the name of the header and the second value is the string that will be used to sort the hibernate results -->
		<s:if test="!inAdmin">
			<s:set var="columns"
				value="{{'study title', 'title'}, {'study id', 'prefixedId'}, {'PI', 'principalInvestigator'}, {'Data Types', ''}, {'Study Permission', ''}}" />
		</s:if>
		<s:else>
			<s:set var="columns"
				value="{{'study title', 'title'}, {'study id', 'prefixedId'}, {'PI', 'principalInvestigator'}, {'Owner', ''}, {'Status', 'studyStatus'}, {'Request Date', 'dateCreated'}}" />
		</s:else>
		<s:iterator var="name" value="#columns">
			<th>
				<div class="no-wrap">
					<s:if test="#name[1] != ''">
						<a href="javascript:studySetSort('<s:property value='#name[1]' />')"> <s:property value="#name[0]" />
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
	<s:iterator var="study" value="studyList" status="status">
		<c:choose>
			<c:when test="${status.count%2 == 0}">
				<tr class="stripe">
			</c:when>
			<c:otherwise>
				<tr>
			</c:otherwise>
		</c:choose>
		<s:if test="inAdmin">
			<td><a
				href="/portal/studyAdmin/studyAction!view.action?studyId=<s:property value='#study.prefixedId' />"><s:property
						value="#study.title" /></a></td>
		</s:if>
		<s:else>
			<td><a href="/portal/study/studyAction!view.action?studyId=<s:property value='#study.prefixedId' />"><s:property
						value="#study.title" /></a></td>
		</s:else>
		<td><s:property value="#study.prefixedId" /></td>
		<td><s:property value="#study.principalInvestigator" /></td>
		<s:if test="!inAdmin">
			<td align="center"><s:if test="#study.isGenomic">
					<img src="<s:url value="/images/brics/study/icon_genomics.png" />" />
				</s:if> <s:else>
					<img src="<s:url value="/images/brics/study/icon_genomics_disabled.png" />" />
				</s:else> <s:if test="#study.isClinical">
					<img src="<s:url value="/images/brics/study/icon_clinical_assesment.png" />" />
				</s:if> <s:else>
					<img src="<s:url value="/images/brics/study/icon_clinical_assesment_disabled.png" />" />
				</s:else> <s:if test="#study.isImaging">
					<img src="<s:url value="/images/brics/study/icon_imaging.png" />" />
				</s:if> <s:else>
					<img src="<s:url value="/images/brics/study/icon_imaging_disabled.png" />" />
				</s:else></td>
			<td><s:property value="permissionList[#status.index].permission.name" /></td>

		</s:if>
		<s:else>
			<td><s:property value="permissionList[#status.index].account.user.fullName" /></td>
			<td><s:if test="#study.studyStatus.id == 2">
					<span class="red-text"> <s:property value="#study.studyStatus.name" />
					</span>
				</s:if> <s:else>
					<s:property value="#study.studyStatus.name" />
				</s:else></td>
			<td><s:if test="#study.studyStatus.id == 2">
					<ndar:dateTag value="${study.dateCreated}" />
				</s:if></td>
		</s:else>

		</tr>
		<!-- Warning due to <tr> tag being created inside a choose tag -->
	</s:iterator>
</table>
