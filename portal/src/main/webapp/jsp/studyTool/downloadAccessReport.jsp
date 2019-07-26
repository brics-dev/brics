<%@page import="gov.nih.tbi.commons.model.StatusType"%>
<%@include file="/common/taglibs.jsp"%>
<%-- <%@include file="/common/script-includes.jsp"%> --%>

<script type="text/javascript" >
	var DAYS_ACCESS_REPORT = 7;
	$('document').ready(function(){ 
		
		var $table = $("#studyListTableTable");
		var selectedRows = $('#studyListTableTable').idtApi("getSelectedOptions");

		$('#selectedStudies').val(selectedRows.join(","));
		//console.log("#selectedStudies.val(): "+$('#selectedStudies').val());
		if (selectedRows.length > 0) {
			if(!$('#selectedStudyNames').children().length > 0){
				$('#selectedStudyNames').empty().append("<ul></ul>");
			}

			for(var i = 0; i<selectedRows.length; i++){
				var data = $('#studyListTableTable').idtApi("getApiRow",'#'+selectedRows[i]).data();
				/*remove hyperlink of the name*/
				var regex = /(<([^>]+)>)/ig;
				var studyName = data.studyAdminLink.replace(regex, "");
				//console.log("data: "+studyName);
				$("#selectedStudyNames ul").append('<li>'+studyName+'</li>');					
			};
		}
		
		$("#startAccessReportDate,#endAccessReportDate").datepicker({
			//showOn: "button",
 			buttonImage: "../images/calendar.gif",
			dateFormat: "yy-mm-dd",
			buttonImageOnly: true ,
			buttonText: "Select Date", 
			changeMonth: true,
			changeYear: true,
			duration: "fast",
			gotoCurrent: true,
			hideIfNoPrevNext: true,
			showOn: "both"
		    });
		var currentDate= new Date();
		$("#endAccessReportDate").datepicker("setDate", currentDate);
		currentDate.setDate(currentDate.getDate()- DAYS_ACCESS_REPORT);
		$("#startAccessReportDate").datepicker("setDate", currentDate);
   	});
</script>

<div class="lightbox-size">
	<div class="border-wrapper">
		<div id="main-content">
 			<h2>Enter Range for Report:</h2>
 			<s:form validate="true"  cssClass="validate" name="accessRecordForm" id="accessRecordForm" action="accessRecordDownloadAction" >
  				<s:hidden name="selectedStudies" id="selectedStudies"/>
  				<div class="line" >
  				<div class="unit size1of2" style="width: 42%;">
 					<div  class="form-field""> 
						<label for="startAccessReportDate" >From:</label>
						<s:textfield name="startAccessReportDate" id="startAccessReportDate" />
						<s:fielderror fieldName="startAccessReportDate" />
						
					</div>
					<div class="form-field">
						<label for="endAccessReportDate">To:</label>
						<s:textfield name="endAccessReportDate" id="endAccessReportDate"/>
						<s:fielderror fieldName="endAccessReportDate" />
					</div>
				</div>
				<div class="unit size1of2 lastUnit">
					<div class="form-field">
						<div class="label"><strong>Selected Studies:</strong></div>
						<div id="selectedStudyNames"></div>
					</div>

				</div>
			</div>
			<div class="button">
				<input type="button" id="donwloadReportBtn"  value="Create Report" onclick="javascript: submitFormAjax();" />
			</div>
			</s:form>
		</div>
	</div>
</div> 
