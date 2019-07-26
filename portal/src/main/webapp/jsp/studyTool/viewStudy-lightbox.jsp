<%@include file="/common/taglibs.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<c:set var="hostname" value="${pageContext.request.serverName}"/>
<c:choose>
   <c:when test="${fn:contains(hostname, 'cnrm' )}">
  	<link href="<s:url value='/config/cnrm/style.css'/>" rel="stylesheet" type="text/css" media="all" />
   </c:when>
   <c:when test="${fn:contains(hostname, 'pdbp' )}">
     	<link href="<s:url value='/config/pdbp/style.css'/>" rel="stylesheet" type="text/css" media="all" />
   </c:when>
   <c:when test="${fn:contains(hostname, 'fitbir' )}">
   	<link href="<s:url value='/config/fitbir/style.css'/>" rel="stylesheet" type="text/css" media="all" />
   </c:when>
   <c:when test="${fn:contains(hostname, 'ninds' )}">
   	<link href="<s:url value='/config/ninds/style.css'/>" rel="stylesheet" type="text/css" media="all" />
   </c:when>
    <c:when test="${fn:contains(hostname, 'cistar' )}">
   	<link href="<s:url value='/config/cistar/style.css'/>" rel="stylesheet" type="text/css" media="all" />
   </c:when>
   <c:when test="${fn:contains(hostname, 'eyegene' ) || fn:contains(hostname, 'nei' )}">
   	<link href="<s:url value='/config/eyegene/style.css'/>" rel="stylesheet" type="text/css" media="all" />
   </c:when>
   <c:when test="${fn:contains(hostname, 'cdrns' )}">
   	<link href="<s:url value='/config/cdrns/style.css'/>" rel="stylesheet" type="text/css" media="all" />
   </c:when>
   	<c:when test="${fn:contains(hostname, 'nti' )}">
        <link href="<s:url value='/config/nti/style.css'/>" rel="stylesheet" type="text/css" media="all" />
 </c:when>
</c:choose>
<style type="text/css">
.lightbox-content-wrapper {
	background: white;
}

.detailFont {
    font-size: 14px !important;
}

.labelWidth {
    width: 250px !important;
} 
 
.lightbox-content-wrapper .readonly-text {
    overflow: hidden;
    word-wrap: break-word;
}
.readonly-text {
    color: #798085;
}

.lightbox-content-wrapper label, .lightbox-content-wrapper .label {
    display: inline;
    float: left;
    font-weight: bold;
    margin: 0 5px 0 0;
    text-align: right;
    width: 160px;
}
.lightbox-content-wrapper .readonly-text {
	text-align:left;
}

.lightbox-content-wrapper .form-output {
	clear:both;
}

</style>

<div class="lightbox-content-wrapper">

	
	<h3 style='font-size:20px'>
		Study: <s:property value="currentStudy.title" />
	</h3>
	
	<div class="form-output detailFont">
		<div class="label labelWidth">Study ID:</div>
		<div class="readonly-text">
			<s:property value="currentStudy.prefixedId" />
		</div>
	</div>
	
	
	<div class="clear-float">
		<div class="form-output detailFont">
			<div class="label labelWidth">Abstract:</div>
			<div class="readonly-text">
				<s:property value="currentStudy.abstractText" />
			</div>
		</div>

		<c:if test="${not empty currentStudy.clinicalTrialSet}">
			<div class="form-output detailFont">
				<div class="label labelWidth">Clinical Trial ID Number(s):</div>
				<div class="readonly-text">
					<c:forEach var="clinicalId" items="${currentStudy.clinicalTrialSet}">
						${clinicalId.clinicalTrialId}&nbsp;&nbsp;
					</c:forEach>
				</div>
			</div>
		</c:if>
		
		<c:if test="${not empty currentStudy.grantSet}">
			<div class="form-output detailFont">
				<div class="label labelWidth">Grant/Project ID Number(s):</div>
				<div class="readonly-text">
					<c:forEach var="grant" items="${currentStudy.grantSet}">
						<%-- <a href="#">${grantId.grantId}</a>&nbsp;  Hiding Grant link until we get impact II working--%>
						<c:out value="${grant.grantId}" />&nbsp;&nbsp;
					</c:forEach>
				</div>
			</div>
		</c:if>
		
		<div class="form-output detailFont">
			<div class="label labelWidth">Owner:</div>
			<div class="readonly-text">
				<s:property value="studyOwner.fullName" />
			</div>
		</div>
		
		<div class="form-output detailFont">
			<div class="label labelWidth">Owner E-Mail:</div>
			<div class="readonly-text">
				<s:property value="studyOwner.email" />
			</div>
		</div>
		
		<div class="form-output detailFont">
			<div class="label labelWidth">Principal Investigator:</div>
			<div class="readonly-text">
				<s:property value="currentStudy.principalInvestigator" />
			</div>
		</div>
		
	</div>
</div>