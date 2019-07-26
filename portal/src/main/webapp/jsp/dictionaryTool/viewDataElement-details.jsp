<%@include file="/common/taglibs.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<c:set var="hostname" value="${pageContext.request.serverName}"/>
      <c:choose>
         <c:when test="${fn:contains(hostname, 'cnrm' )}">
          <link href="<s:url value='/config/cnrm/style.css'/>" rel="stylesheet" type="text/css" media="all" />
          <link href="<s:url value='/config/cnrm/images/favicon.ico'/>" rel="icon" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'pdbp' )}">
            <link href="<s:url value='/config/pdbp/style.css'/>" rel="stylesheet" type="text/css" media="all" />
            <link href="<s:url value='/config/pdbp/images/favicon.ico'/>" rel="icon" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'fitbir' )}">
          <link href="<s:url value='/config/fitbir/style.css'/>" rel="stylesheet" type="text/css" media="all" />
          <link href="<s:url value='/config/fitbir/images/favicon.ico'/>" rel="icon" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'ninds' )}">
          <link href="<s:url value='/config/ninds/style.css'/>" rel="stylesheet" type="text/css" media="all" />
          <link href="<s:url value='/config/ninds/images/favicon.ico'/>" rel="icon" />
         </c:when>
          <c:when test="${fn:contains(hostname, 'cistar' )}">
          <link href="<s:url value='/config/cistar/style.css'/>" rel="stylesheet" type="text/css" media="all" />
          <link href="<s:url value='/config/cistar/images/favicon.ico'/>" rel="icon" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'eyegene' ) || fn:contains(hostname, 'nei' )}">
          <link href="<s:url value='/config/eyegene/style.css'/>" rel="stylesheet" type="text/css" media="all" />
          <link href="<s:url value='/config/eyegene/images/favicon.ico'/>" rel="icon" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'cdrns' )}">
          <link href="<s:url value='/config/cdrns/style.css'/>" rel="stylesheet" type="text/css" media="all" />
          <link href="<s:url value='/config/cdrns/images/favicon.ico'/>" rel="icon" />
         </c:when>
         <c:when test="${fn:contains(hostname, 'nti' )}">
        	<link href="<s:url value='/config/nti/style.css'/>" rel="stylesheet" type="text/css" media="all" />
        	<link href="<s:url value='/config/nti/images/favicon.ico'/>" rel="icon" />
         </c:when>
</c:choose>

<s:if test="queryArea == true">
<link rel="stylesheet" href="https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.css" />

<!-- new datatables styles -->
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/2.0/css/datatables-pdfmake.min.css" />
<!-- <link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/responsive/2.1.1/css/responsive.dataTables.min.css" />  -->
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/2.0/css/responsive.dataTables.min.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/2.0/css/datatablesWidget.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/2.0/css/idtSearchColumnPlugin.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/dataTables/2.0/css/idtSelectSetPlugin.css" />
<link type="text/css" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" />


<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.min.js" crossorigin="anonymous"></script>
<%-- <script src="https://code.jquery.com/jquery-migrate-1.4.1.min.js"></script> --%>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-migrate/1.4.1/jquery-migrate.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js"></script>
<%-- min file contains all the files located in /js/common-source-files/ --%>
<!-- Messaging JS -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/common.min.js"></script>
<script type="text/javascript" charset="utf-8" language="javascript" src="${pageContext.request.contextPath}/js/jquery.multiselect.min.js"></script>
<script type="text/javascript" charset="utf-8" language="javascript" src="${pageContext.request.contextPath}/formbuilder/js/lib/jquery.dataTables.js"></script>
<script type="text/javascript" charset="utf-8" language="javascript" src="${pageContext.request.contextPath}/js/dataTables/js/FixedHeader.min.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap/bootstrap-3.1.1.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bootstrap/bootstrap-multiselect.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/ibis/jquery.ibisMessaging-0.1.full.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/ibis/jquery.bricsDialog.js"></script>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/chosen/chosen.jquery.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery.mCustomScrollbar.concat.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/ibis/core_libs.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/ibis/core_classes.min.js"></script>

<!-- New Proforms DataTables -->
<!-- <script type="text/javascript" src="https://cdn.datatables.net/v/dt/jszip-3.1.3/pdfmake-0.1.27/dt-1.10.15/b-1.3.1/b-colvis-1.3.1/b-flash-1.3.1/b-html5-1.3.1/b-print-1.3.1/datatables.min.js"></script> -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/2.0/lib/datatables.min.js"></script>
<!-- <script type="text/javascript" src="https://cdn.datatables.net/select/1.2.2/js/dataTables.select.min.js"></script> -->
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/2.0/lib/dataTables.select.min.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/2.0/jqfactory-03.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/2.0/idtTableWidget.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/2.0/fnDtFilterPlugin.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/2.0/idtSelectSetPlugin.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/2.0/idtSearchColumnPlugin.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/dataTables/2.0/idtTableActions.js"></script>
<script type="text/javascript">$.fn.bootstrapBtn = $.fn.button.noConflict()</script>
</s:if>

<s:set var="mapElement" value="mapElement" /> 
<div class="lightbox-content-wrapper" style="width: 1200px;">
<h3>
  <s:property escapeHtml="false" value="currentDataElement.category.name" />:&nbsp;
  <s:property escapeHtml="false" value="currentDataElement.title" />
  
  <s:if test="showDataElementDetail">
  &nbsp;&nbsp;<a href="<s:property escapeHtml="false" value="modulesDDTURL" />dictionary/dataElementAction!view.action?dataElementName=<s:property escapeHtml="false" value="currentDataElement.name" />" target="_new" style="font-size:12px; float:right;">view more in the data dictionary</a>
  </s:if>
</h3>

<div class="clear-float line">

  <!-- First column of of data from the items.
           Items with long descriptions (and to get cropped on the page)
           See guidelines as an example.
           The less function must also be called for each cropped item in the js at
           the bottom of the page. -->
                   
    <div class="unit size3of5">
    <div class="mod">
    
      <div class="form-output">
        <div class="label" id="publication-title">Status:</div>
        <div class="readonly-text" id="publication">
          <s:property value="currentDataElement.status.name" />
        </div>
      </div>
      <div class="form-output">
        <div class="label">Variable Name:</div>
        <div class="readonly-text">
          <s:property escapeHtml="true" value="currentDataElement.name" />
        </div>
      </div> 
      <div class="form-output">
        <div class="label">Definition:</div>
        <div class="readonly-text">
          <s:property escapeHtml="true" value="currentDataElement.description" />
        </div>
      </div>
      <div class="form-output">
        <div class="label" id="guidelines-title">Guidelines & Instructions:</div>
        <div class="readonly-text" id="guidelines">
          <s:property escapeHtml="true" value="currentDataElement.guidelines" />
        </div>
      </div>
      <div class="form-output">
        <div class="label" id="references-title">References:</div>
        <div class="readonly-text" id="references">
          <s:property escapeHtml="true" value="currentDataElement.references" />
        </div>
      </div> 
      <div class="form-output">
        <div class="label" id="question-title">Preferred Question Text:</div>
        <div class="readonly-text" id="question">
          <s:property escapeHtml="true" value="currentDataElement.suggestedQuestion" />
        </div>
      </div>
      <div class="form-output">
        <div class="label" id="notes-title">Notes:</div>
        <div class="readonly-text" id="notes">
          <s:property escapeHtml="true" value="currentDataElement.notes" />
        </div>
      </div>
      <div class="form-output">
        <div class="label">Population:</div>
        <div class="readonly-text">
          <s:property value="currentDataElement.population.name" />
        </div>
      </div>
    </div>
  </div>
  
 <div class="unit size2of5 lastUnit">	
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
	         <s:if test="currentDataElement.sortedValueRangeList.size!=null && currentDataElement.sortedValueRangeList.size!=0">
	        	 <table class="display-data full-width" id="dEDetailPvTable"></table> 
	         </s:if>
	    </div>
	  <s:if test="currentDataElement.size!=null">
	    <div class="form-output">
             <div class="label">Maximum Character Quantity:</div>
             <div class="readonly-text">
                <s:property value="currentDataElement.size" />
             </div>
        </div> 
      </s:if> 
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
      <div class="form-output">
      </div>
      <div class="form-output">
        <div class="readonly-text underLabel">
          <a href="${pageContext.request.scheme}://${pageContext.request.serverName}:${pageContext.request.serverPort}/portal/dictionary/schemaMappingAction!viewSchemaMappingValues.action?dataElement=<s:property value="currentDataElement.name" />" target="_blank">Mapping to external dictionaries</a>
        </div>
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

<script id="test" type="text/javascript">
var runTables = function () {
    
    $("a.lightbox").fancybox();
    $("#fancybox-wrap").unbind('mousewheel.fb');
    
    $("#dEDetailPvTable").idtTable({
      autoWidth: false,
      dom : 'frtip',
      pageLength: 25,
      columns: [
        {
          data: "valueRange",
          title: "Permissible Value",
          name: "valueRange",
          width: "20%",
        },
        {
          data: "description",
          title: "Description",
          name: "description",
          width: "30%",
          render: IdtActions.ellipsis(250)
        },
        {
          data: "outputCode",
          title: "Code",
          name: "outputCode",
          width: "10%"
        }
      ],
      data: [
              <s:iterator value="currentDataElement.sortedValueRangeList" var="valueRange">
              {
                "valueRange": "<s:property value='valueRange' />",
                "description": "<s:property value='description' />",
                "outputCode": "<s:property value='outputCode' />"
              },
              </s:iterator>
      ]     
    });
  }

</script>


<script type="text/javascript">
var createStyleLink = function(link) {
  var style = document.createElement("link");
  style.setAttribute("href", link);
  style.setAttribute("rel", "stylesheet");
  style.setAttribute("type", "text/css");
  return style;
}
var _scripts = [
    'https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.min.js',
    'https://cdnjs.cloudflare.com/ajax/libs/jquery-migrate/1.4.1/jquery-migrate.js',
    'https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/jquery-ui.min.js',
    '/portal/js/common.min.js',
    '/portal/formbuilder/js/lib/jquery.dataTables.js',
    '/portal/js/dataTables/js/FixedHeader.min.js',
    '/portal/js/bootstrap/bootstrap-3.1.1.min.js',
    '/portal/js/bootstrap/bootstrap-multiselect.js',
    '/portal/js/chosen/chosen.jquery.js',
    '/portal/js/jquery.mCustomScrollbar.concat.min.js',
    '/portal/js/ibis/core_libs.min.js',
    '/portal/js/ibis/core_classes.min.js',
    '/portal/js/dataTables/2.0/lib/datatables.min.js',
    'https://cdn.datatables.net/select/1.2.2/js/dataTables.select.min.js',
    '/portal/js/dataTables/2.0/lib/dataTables.select.min.js',
    '/portal/js/dataTables/2.0/jqfactory-03.js',
    '/portal/js/dataTables/2.0/idtTableWidget.js',
    '/portal/js/dataTables/2.0/fnDtFilterPlugin.js',
    '/portal/js/dataTables/2.0/idtSelectSetPlugin.js',
    '/portal/js/dataTables/2.0/idtSearchColumnPlugin.js',
    '/portal/js/dataTables/2.0/idtTableActions.js',
    '/portal/js/dataTables/2.0/idtApi.js'
    
  ];

  function createScriptTag() {
    // gets the first script in the list
    var script = _scripts.shift();
    // all scripts were loaded
    if (!script) return;
    var js = document.createElement('script');
    js.type = 'text/javascript';
    js.src = script;
    js.onload = function(event) {
      // loads the next script
      createScriptTag();
    };
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(js, s);
  }
//For a page loaded in a new tab this property returns 1. when this page is loaded in a new tab we had to add manually script files and css files for datatable
if(history.length == 1) {
  //this is for CSS files
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("https://ajax.googleapis.com/ajax/libs/jqueryui/1.12.1/themes/smoothness/jquery-ui.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/styles/jquery/jquery.dataTables.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/styles/jquery/datatables.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/styles/bootstrap/bootstrap-3.1.1.min.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/styles/bootstrap/bootstrap-multiselect.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/styles/chosen/chosen.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/styles/jquery.mCustomScrollbar.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/styles/jquery/jquery.qtip.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/js/dataTables/2.0/css/datatables-pdfmake.min.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("https://cdn.datatables.net/responsive/2.1.1/css/responsive.dataTables.min.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/js/dataTables/2.0/css/responsive.dataTables.min.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/js/dataTables/2.0/css/datatablesWidget.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/js/dataTables/2.0/css/idtSearchColumnPlugin.css"));
  document.getElementsByTagName("head")[0].appendChild(createStyleLink("/portal/js/dataTables/2.0/css/idtSelectSetPlugin.css"));

  
  
  //this is for JS files
  createScriptTag();
  
  window.onload = function() {
    runTables();
  }
}else {
  runTables();
}
</script>