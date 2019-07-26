<%@ tag body-content="scriptless"%>
<%@ attribute name="repeatableGroupList" type="java.util.Collection" required="true"%>

<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>
<c:if test="${fn:length(repeatableGroupList) == 0}">
	<br />
	<p>There are no data elements in this group.</p>
</c:if>
<c:if test="${fn:length(repeatableGroupList) != 0}">
	<div id="details-accordion">
		<c:forEach var="group" items="${repeatableGroupList}" varStatus="status">
			<h3>
				<a href="#"> <c:out value="${group.name}  " /> <c:if test="${group.threshold != 0 }">
						<c:out value="(Appears ${group.type.value} ${group.threshold} Times)" />
					</c:if>
				</a>
			</h3>
			<div>
				<ndar:attachedDataElementList elementList="${group.mapElements}" elementGroupId="${group.id}"
					tableName="${group.id}" />
				<br />
				<div class="action-button">
					<a href="dataStructureElementAction!searchElements.action?groupElementId=${group.id}">Add Elements</a>
				</div>
			</div>


		</c:forEach>

	</div>
</c:if>