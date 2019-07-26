<%@include file="/common/taglibs.jsp"%>
<jsp:include page="/common/script-includes.jsp" />
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



<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryViewEngine.js'></script>
<script type="text/javascript" src='/portal/js/metastudy/SavedQuery.js'></script>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryDe.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryDes.js'></script>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryForm.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryForms.js'></script>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryRg.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryRgs.js'></script>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryStudy.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryStudies.js'></script>

<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryDeView.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryRgView.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryFormView.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryStudyView.js'></script>
<script type="text/javascript"
	src='/portal/js/metastudy/SavedQueryView.js'></script>

<script type="text/config" id="savedQueryData"><s:property value="savedQueryText" /></script>

<div class="viewQueryContainer"></div>

<script id="container" type="text/x-handlebars-template">
<div class="viewQuery_studyList">
	<div class="viewQuery_header viewQuery_studiesHeader">
		<div class="viewQuery_headerText">Studies</div>
	</div>
	<div class="viewQuery_studiesContainer">
	<!-- for all studies -->
	</div>
</div>

<div class="viewQuery_rightSide">
	<div class="viewQuery_header viewQuery_queryHeader">
		<span class="viewQuery_detailsPlusMinus viewQuery_minus"> - </span>
		<span class="viewQuery_detailsTitle">Saved Query Details:</span> 
		<span class="viewQuery_queryName" name="name">{{name}}</span>
	</div>
	<div class="viewQuery_queryDetails">
		<div class="viewQuery_label">Name</div>
		<div class="viewQuery_value" name="name">{{name}}</div>
		<div class="clearfix"></div>

		<div class="viewQuery_label">Description</div>
		<div class="viewQuery_value" name="description">{{description}}</div>
		<div class="clearfix"></div>

		<div class="viewQuery_label">Last Updated</div>
		<div class="viewQuery_value" name="lastUpdated">{{lastUpdated}}</div>
		<div class="clearfix"></div>

		<div class="viewQuery_label">Permissions</div>
		<div class="viewQuery_value">
			<table class="display-data">
				<thead>
					<tr>
						<th>User/Permission Group</th>
						<th>Permission</th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="savedQueryPermission" items="${savedQueryPermissions}">
						<tr>
							<td>${savedQueryPermission.account.user.fullName}</td>
							<td>${savedQueryPermission.permission.name}</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>

		</div>
		<div class="clearfix"></div>
	</div>
	
	<div class="viewQuery_header viewQuery_formsHeader">Forms <span class="viewQuery_formCount"></span></div>
	<div class="viewQuery_formList">
		Select a Study in the left menu to view its Forms.
		<!-- for all forms -->
		<!-- formItem template -->
	</div>
</div>
</script>

<script id="formItem" type="text/x-handlebars-template">
<div class="viewQuery_formHeader">
	<span class="viewQuery_plusMinus viewQuery_plus"> + </span>
	<span class="viewQuery_formName" name="name">{{name}}</span>
	<span class="viewQuery_formFiltered"></span>
</div>
<div class="viewQuery_formData">
				
	<!-- For all groups in form -->
	<!-- rgItem template -->
</div>
</script>

<script id="rgItem" type="text/x-handlebars-template">
<div class="viewQuery_rgName" name="name">{{name}}</div>
<div class="viewQuery_deListContainer">
	<!-- For all data elements in RG -->
	<!--  deItem template -->
</div>
</script>

<script id="deItem" type="text/x-handlebars-template">
	<div class="viewQuery_filterIcon"></div>
	<div class="viewQuery_filterText">
		<div class="viewQuery_deTitle" name="name">{{name}}</div>
		<div class="viewQuery_filterValue"></div>
	</div>
	<div class="clearfix"></div>
</script>

<script id="study" type="text/x-handlebars-template">
<a class="viewQuery_studyItemLink">
	<span class="viewQuery_studyName" name="title">{{title}}</span> 
	(<span class="viewQuery_studyFormCount" name="formCount">{{formCount}}</span>)
	<span class="viewQuery_studyFiltered"></span>
</a>
</script>

<script>

$( document ).ready(function() {
    console.log( "ready!" );

// 80% of window height
var height = $(window).height() * 0.9;

$('body').css('background', '#fff');

var style = $('<style>.ui-dialog { z-index: 1200 !important; }</style>');
$('html > head').append(style);

SQViewProcessor.render();
});
</script>
