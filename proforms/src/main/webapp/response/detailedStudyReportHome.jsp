<jsp:include page="/common/doctype.jsp" />

<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale" %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>

<%@ page buffer="100kb"%>

<%
	Locale l = request.getLocale();
%>

<html>
<s:set var="pageTitle" scope="request">
	<s:text name="report.detailedStudyReport" />
</s:set>
	
<jsp:include page="/common/header_struts2.jsp" />
<s:set var="clientName" value="#systemPreferences.get('template.global.appName')" />


<p><s:text name="report.detailedStudyReport.instruction" /></p>
<br>

<script type="text/javascript">
//reformat the Date to YYYYMMDD
function getValue(value) {
   return (value < 10) ? "0" + value : value;
};
function getDate () {
   var newDate = new Date();

   var sMonth = getValue(newDate.getMonth() + 1);
   var sDay = getValue(newDate.getDate());
   var sYear = newDate.getFullYear();

   return sYear + sMonth + sDay;
}


function ellipsis(data, type, row, full, numCollections) {
    var moreText = "more";
    var lessText = "less";
    var ellipsestext = '...';
    var showChars = 50;
    var oTableId = full.settings.nTable.id;
    // $(full.settings.aanFeatures.t[0]).off("click", '.morelink');
    $('#' + oTableId).on("click", '.morelink', function(e) {
        e.stopImmediatePropagation();
        if ($(this).hasClass("less")) {
            $(this).removeClass("less");
            $(this).html(moreText);
           
            
        } else {
            $(this).addClass("less");
            $(this).html(lessText);
            //$(".morecontent span").css("display", "inline-block");
        }
        $(this).parent().prev().toggle();
        $(this).prev().toggle();
        console.log($(this).parent().prev());
        return false;
    });
    if (type !== 'display') {
        return data + '<br>';
    }
    if (typeof data !== 'number' && typeof data !== 'string') {
        return data + '<br>';
    }
    data = data.toString(); // cast numbers
    if (data.length <= showChars) {
        return data + '<br>';
    }
    var shortened = data.substr(0, showChars);
    var h = data.substr(showChars, data.length - showChars);
	return '<div class="showMoreContainer">' + shortened + '<span class="moreellipses">' + ellipsestext + '&nbsp;' + '(' + numCollections +')</span><span class="morecontent"><span>' + h + '</span>&nbsp;&nbsp;<a href="" class="morelink">' + moreText + '</a></span></div>';
	 
};

var now = getDate();

$(document).ready(function() {
	
		$("#studyReportsTable").idtTable({
			idtUrl: "<s:property value='#webRoot'/>/response/getDetailedStudyReportList.action",
			dom: 'Bfrtip',
			autoWidth:false,
	        columns: [
	        	{
	                name: 'studyName',
	                title: '<%=rs.getValue("study.add.name.display",l)%>',
	                parameter: 'studyName',
	                data: 'studyName',
	                width: '20%',
	                render: IdtActions.ellipsis(40)
	            },
	            {
	                name: 'studyPI',
	                title: '<%=rs.getValue("report.studyReport.pi",l)%>',
	                parameter: 'studyPI',
	                data: 'studyPI',
	                width: '10%',
	                render: IdtActions.ellipsis(20)
	            },

	            {
	                name: 'studyStartDate',
	                title: 'Start Date',
	                parameter: 'studyStartDate',
	                data: 'studyStartDate',
	                width: '9%'
	            },
	            
	            {
	                name: 'studyEndDate',
	                title: 'End Date',
	                parameter: 'studyEndDate',
	                data: 'studyEndDate',
	                width: '9%'
	            },
	            {
	                name: 'studySubjectCount',
	                title: '<%=rs.getValue("report.numOfSubjectsEnrolled",l)%>',
	                parameter: 'studySubjectCount',
	                data: 'studySubjectCount'
	            },
	            {
	                name: 'studyEformCount',
	                title: '<%=rs.getValue("report.numOfEforms",l)%>',
	                parameter: 'studyEformCount',
	                data: 'studyEformCount',
	                width: '7.4%'
	            },
	            {
	                name: 'protocolEformsAndAdminFormCount',
	                title: '<%=rs.getValue("report.eformsAssocAndNumCollections",l)%>',
	                parameter: 'protocolEformsAndAdminFormCount',
	                data: 'protocolEformsAndAdminFormCount',
	                sortable: false,
	                render: function (data, type, row, full) {//console.log("type: "+type+" | full: "+JSON.stringify(full));
	                	var arr = data.split("<br>");
	                	var text = "";
	                	var result = "";
	                	var full = full;
	                	var type = type;
	                	for(var i =0;i<arr.length;i++) {
	                		var fullString = arr[i];
	                		var collections = arr[i].split("(");
	                		collections = collections[collections.length - 1];
	                		collections = collections.slice(0, - 1);
	                		var text = ellipsis(fullString, type, row, full, collections);
	                		result += text;
	                	}
	                
	                return result;
	                	
			          }
	            }  
            

	        ],
	        buttons: [
				{
       				text: "Download",
       				className: "detailedProtocolDownloadBtn",
       				action: function(e, dt, node, config) {
       					var exportURL = '<s:property value="#webRoot"/>'+ '/response/studyReportExport.action';
       					redirectWithReferrer(exportURL);
       				}
       			}
			]
		})	
	
	
		
});
</script>

<div id="studyReportsContainer" class="idtTableContainer brics">
	<table id="studyReportsTable" class="table table-striped table-bordered" width="100%">
	</table>
</div>	

<jsp:include page="/common/footer_struts2.jsp" />
</html>