<%@include file="/common/taglibs.jsp"%>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
        <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
            <c:set var="hostname" value="${pageContext.request.serverName}" />
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
                    <link href="<s:url value='/config/nti/nti.css'/>" rel="stylesheet" type="text/css" media="all" />
                 </c:when>
            </c:choose>
            <title><s:property escapeHtml="true" value="de.title" /></title>

            <s:set var="newDataElementList" value="newDataElementList" />
            <s:set var="disabledList" value="disabledList" />
            <s:set var="alreadyAttachedDataElementList" value="alreadyAttachedDataElementList" />
            <s:set var="existingDataElementList" value="existingDataElementList" />

            <body class="lightbox" style="background:none;">



                <!-- begin .border-wrapper -->
                <div class="border-wrapper">
                    <div class="clear-float"></div>
                    <!--begin #center-content -->
                    <div id="main-content" style="min-height:300px; margin-top:15px;">


                        <h2>Data Element Mapping</h2>

                        <s:if test="hasActionErrors()">
                            <div class="form-error">
                                <s:actionerror />
                            </div>
                        </s:if>
                        <s:if test="hasActionMessages()">
                            <div class="success-message">
                                <s:actionmessage />
                            </div>
                        </s:if>

                        <div>
                            <p>This table contains the mapping between the system Data Element Permissible Value and external entity schema IDs and Permissible Values
                            </p>
                            <p>
                                Data element Variable Name: <s:property escapeHtml="false" value="de.name" />
                            </p>
                        </div>

                        <div id="mappingsContainer" class="idtTableContainer" style="width: 125%; margin-left: auto; margin-right: auto;">
							<table id="mappingsTable" class="table table-striped table-bordered" width="100%"></table>
						</div>
                    </div>


                </div>

<script type="text/javascript">
	setNavigation({
	    "bodyClass": "primary",
	    "navigationLinkID": "dataDictionaryModuleLink",
	    "subnavigationLinkID": "defineDataToolsLink",
	    "tertiaryLinkID": "importDataElementSchemaLink"
	});
	
	function submitTheForm(theForm) {
	
	
	    var theForm = document.forms['uploadForm'];
	    theForm.action = 'schemaMappingAction!adminUploadSchemaMapping.action';
	    theForm.submit();
	}
	$('document').ready( function() {
		var deShortName = '<s:property escapeHtml="false" value="de.name" />';
		$("#mappingsTable").idtTable({
			idtUrl: "schemaMappingAction!getSchemaPvList.action",
			filterData: {
				deShortName : deShortName
			},
			columns: [
				{
					title: "DE Variable Name",
					data: "dataElementName",
					name: "dataElementName",
					parameter: "dataElement.name"
				},
				{
					title: "DE Permissible Value",
					data: "valueRange",
					name: "valueRange",
					parameter: "valueRange"
				},
				{
					title: "Permissible Value Description",
					data: "valueRangeDescription",
					name: "valueRangeDescription",
					parameter: "valueRangeDescription"
				},
				{
					title: "External Schema System Name",
					data: "schemaName",
					name: "schemaName",
					parameter: "schema.name"
				},
				{
					title: "Schema System DE Variable Name",
					data: "schemaDataElementName",
					name: "schemaDataElementName",
					parameter: "schemaDataElementName"
				},
				{
					title: "Schema System DE ID",
					data: "schemaDeId",
					name: "schemaDeId",
					parameter: "schemaDeId"
				},
				{
					title: "Schema System DE PVs",
					data: "permissibleValue",
					name: "permissibleValue",
					parameter: "permissibleValue"
				},
				{
					title: "Schema System DE PV Codes",
					data: "schemaPvId",
					name: "schemaPvId",
					parameter: "schemaPvId"
				}								
			]
		});
	})
</script>