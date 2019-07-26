<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s"%>
<%@ taglib uri="/WEB-INF/datatables.tld" prefix="idt" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security"%>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants"%>
<%@ page import="gov.nih.nichd.ctdb.common.rs, java.util.Locale,gov.nih.nichd.ctdb.protocol.domain.Interval"%>
<%@ page import="java.util.List,gov.nih.nichd.ctdb.response.form.FormVisitTypeStatusSubjectMatrix"%>
<%@ page import="org.json.JSONArray,org.json.JSONObject"%>
<%@ page buffer="100kb"%>
<%
	Locale l = request.getLocale();
	JSONArray jsonArrayOfVisitType  = (JSONArray)session.getAttribute("jsonArrayOfVisitType");
	JSONArray jsonArrayOfGuid  = (JSONArray)session.getAttribute("jsonArrayOfGuid");
	JSONArray jsonArrayOfSubjectMatrix  = (JSONArray)session.getAttribute("jsonArrayOfSubjectMatrix");
	String targetGuid = (String) session.getAttribute("selectedGuidInAction");
	//JSONObject jsonObjOfSubjectMatrix  = (JSONObject)session.getAttribute("jsonObjOfSubjectMatrix");
	Protocol curProtocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	int SUBJECT_DISPLAY_TYPE = curProtocol.getPatientDisplayType();
	request.setAttribute("SUBJECT_DISPLAY_TYPE", SUBJECT_DISPLAY_TYPE);
	String SUBJECT_DISPLAY_LABEL = curProtocol.getPatientDisplayLabel();
	request.setAttribute("SUBJECT_DISPLAY_LABEL", SUBJECT_DISPLAY_LABEL);
%>
<s:set var="pageTitle" scope="request">
	<s:text name="subject.matrix.dashboard" />
</s:set>	
<jsp:include page="/common/header_struts2.jsp" />
<link rel="stylesheet" type="text/css" href="<s:property value="#webRoot"/>/common/css/dataCollection.css">
  <style>
  .ui-autocomplete {
    max-height: 100px;
    overflow-y: auto;
    /* prevent horizontal scrollbar */
    overflow-x: hidden;
  }

  * html .ui-autocomplete {
    height: 200px;
    width : 200px
  }
  
  .ui-menu .ui-menu-item a{
  text-align:left
  }
  
  #subjectFormVisitStatusTable_wrapper {
  	overflow: visible;
  }
  </style>
    <script src="<s:property value="#webRoot"/>/common/js/ibisCommon.js" type="text/javascript"></script>
	<script type="text/javascript">
		var target = "<%=targetGuid%>";

		$(document).ready(function() {
	  		var data = <%=jsonArrayOfGuid%>;

	  		$( "#selectedGuid" ).autocomplete({
	  			source : data,
	  			minLength: 0,
	  			scroll: true
	  		}).focus(function (){
					$(this).autocomplete("search", "");
 		  	});
	  		
	  		if(target != null && target != ""){
	  			$("#selectedGuid").val(target);
	  		}

  			$("img#dropDownImg").click(function(){
				$("#selectedGuid").autocomplete("search", "");
  			});
	  		
		});
		  
   
 </script>
	<p><s:text name="report.subject.matrix.instruction"/></p>	
	<div>
		<div class="floatLeft" id="subjectMatrixSelectSubject">
			<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_ID) {%>
	 				<label for="selectedGuid" class="subjectMatrixSelectSubjectLabel"><strong><s:text name="patient.label.SubjectID"/></strong> </label>
	 			<%}%>
	 			<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_GUID) {%>
	 				<label for="selectedGuid" class="subjectMatrixSelectSubjectLabel"><strong><s:text name="patient.guid.display"/></strong> </label>
	 			<%}%>
	 			<%if (SUBJECT_DISPLAY_TYPE  == CtdbConstants.PATIENT_DISPLAY_MRN) {%>
	 				<label for="selectedGuid" class="subjectMatrixSelectSubjectLabel"><strong><s:text name="patient.scheduleVisit.mrn.display"/></strong> </label>
	 			<%}%>

				<s:form action="subjectMatrixDashboard" method="post" id="rFrom">							
					<input id="selectedGuid"  list="#session.guidList"  name="selectedGuidInAction"    type="text" placeholder="Select a  <%=SUBJECT_DISPLAY_LABEL%>"/>
					<img alt="dropDownImg" id="dropDownImg" src="<s:property value="#webRoot"/>/images/dropdownArrow.png">											
					 <s:submit id="submitButtonId"/> 
				</s:form>
		</div>
		<div class="floatRight" >
						<div  class="greenCircle floatLeft statusCir"></div><div class="floatLeft height30px" >Locked</div>
						<div class="redCircle floatLeft statusCir"></div><div  class="floatLeft height30px" >In Progress</div>
						<div class="yellowCircle floatLeft  statusCir"></div><div class="floatLeft height30px" >Completed</div>			
						<div class="whiteCircle floatLeft statusCir"></div><div class="floatLeft height30px" >Not Started</div>
						<div style="clear: left;">-   =  Not Administered( or form not in visit type)</div>
						<div class="marginBottom10px" ><span class="paintRed" >*</span> Letter R inside circle means required form for that visit type </div>
						
		</div>
		<div style="clear: both;"></div>
		</div>
		
	<div id="subjectFormVisitStatusContainer" class="idtTableContainer brics">
		<table id="subjectFormVisitStatusTable" class="table table-striped table-bordered" width="100%">
		</table>
	</div>
		
<script id="test" type="text/javascript">
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
	var now = getDate();
    $("#subjectFormVisitStatusTable").idtTable({
      autoWidth: false,
      dom : 'Bfrtip',
      columns: <%=jsonArrayOfVisitType%>,
      data: <%=jsonArrayOfSubjectMatrix%>,
      buttons: [
			{
				extend: "collection",
				title: 'Form_Status_By_GUID_'+now,
				buttons: [
					{
		                extend: 'excel',
		                className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-excel',
		                extension: '.xlsx',
						exportOptions: {
							orthogonal: 'export'
						},
						enabled: true,
						action: IdtActions.exportAction()
						
					}				
				]
			}			
		]
		
    });
    

</script>	
			
<jsp:include page="/common/footer_struts2.jsp" />