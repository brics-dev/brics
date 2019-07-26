<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>
<%@ page import="gov.nih.nichd.ctdb.protocol.domain.Protocol"%>
<%@ page import="gov.nih.nichd.ctdb.common.CtdbConstants,gov.nih.nichd.ctdb.response.common.ResponseConstants"%>
<%@ page import="gov.nih.nichd.ctdb.response.domain.DataEntryHeader"%>

<%-- CHECK PRIVILEGES --%>
<security:check privileges="doublekeyresolution,dataentryoversight"/>
<html>
<s:set var="pageTitle" scope="request">
	<s:text name="response.viewedit.history.title" />
</s:set>
<jsp:include page="/common/popUpHeader_struts2.jsp" />
<%
	Protocol protocol = (Protocol) session.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
	DataEntryHeader deh = (DataEntryHeader) session.getAttribute(ResponseConstants.DATAENTRYHEADER_SESSION_KEY);
	boolean enableEsignature = protocol.isEnableEsignature();
%>
<body>
<%--The below line is needed to export datatable in pdf, since we did not include header.jsp int this jsp file(as logo or banner is not displayed view audit page) --%>
 <a id="logo" class="float-left" href='${systemPreferences.get('brics.modules.workspace.url')}' style="display:none;"></a>

    <div id="wrap">
	    <div class="container960">
	   		 <div class="formbutton">
	    		<input type="button" value="Close" id="bntCloseAudit" onClick="window.close()" title ="Click to close" class="no-print"/> 
				<input type="button" value="Print" id="bntPrintAudit" onClick="window.print()" title ="Click to Print" class="no-print"/>
				<input type="button" value="Export to CSV" id="exportCSV" title = "Export to CSV" class="no-print"/>	 
			</div>
	    	<h3 align="left"><s:text name="response.viewedit.history.title"/></h3>
		    <table border=0 width="100%">
				<jsp:include page="/response/dataEntryHeader.jsp" />
		        <tr>
		            <td><img src="<s:property value="#imageRoot"/>/spacer.gif" width="1" height="15" alt="" border="0"/></td>
		        </tr>
		        <tr>
		            <td class="subPageTitle"><h3><s:text name="response.viewaudit.formsummarystatus"/></h3></td>
		        </tr>
		        <tr>
		            <td>
		            	<div id="dataEntry1ListContainer" class="idtTableContainer brics" style="display:block">
							<table id="dataEntry1ListTable" class="table table-striped table-bordered" width="100%">
							</table>
						</div>
		                		                
		            </td>
		        </tr>
		        <tr>
		            <td><img src="<s:property value="#imageRoot"/>/spacer.gif" width="1" height="15" alt="" border="0"/></td>
		        </tr>
		        <tr>
		            <td class="subPageTitle"><h3><s:text name="response.viewaudit.dataentry1"/></h3></td>
		        </tr>
		        <tr>
		            <td>
						<div id="dataEntryListContainer" class="idtTableContainer brics" style="display:block">
							<table id="dataEntryListTable" class="table table-striped table-bordered" width="100%">
							</table>
						</div>	             
		            </td>
		        </tr>
		        <tr>
		            <td><img src="<s:property value="#imageRoot"/>/spacer.gif" width="1" height="15" alt="" border="0"/></td>
		        </tr>
		        <tr>
		            <td class="subPageTitle"><h3><s:text name="response.viewaudit.editanswer"/></h3></td>
		        </tr>
		        <tr>
		            <td>
						<div id="responseListContainer" class="idtTableContainer brics" style="display:block">
							<table id="responseListTable" class="table table-striped table-bordered" width="100%">
							</table>
						</div>		           
		            </td>
		        </tr>
		        <tr>
		            <td><img src="<s:property value="#imageRoot"/>/spacer.gif" width="1" height="15" alt="" border="0"/></td>
		        </tr>
		        <tr>
		            <td class="subPageTitle"><h3><s:text name="response.viewaudit.sentEmails" /></h3></td>
		        </tr>
		        <tr>
		            <td>
						<div id="sentEmailsContainer" class="idtTableContainer brics" style="display:block">
							<table id="sentEmailsTable" class="table table-striped table-bordered" width="100%">
							</table>
						</div>		               
		            </td>
		        </tr>
		    </table>   
		    
	   		 <div class="formbutton">
				<input type="button" value="Close" id="bntCloseAudit" onClick="window.close()" title ="Click to close" class="no-print"/> 
				<input type="button" value="Print" id="bntPrintAudit" onClick="window.print()" title ="Click to Print" class="no-print"/>
				<input type="button" value="Export to CSV" id="exportCSV" title = "Export to CSV" class="no-print"/>	 
			</div>
		</div>
	</div>
</body>
<script type="text/javascript">
//Get the url param value
function getURLParameter(name) {
       return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [null, ''])[1].replace(/\+/g, '%20')) || null;
}
var aformId = getURLParameter('id');
var ename = '<%=deh.getFormDisplay()%>';
$(document).ready(function() {
	$('#dataEntryListTable').idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getInitialDataEntryList.action",
		filterData: {
			id: aformId
		},
	    "language": {
	        "emptyTable": "There are no data entries to display at this time."
	    },
		columns: [
			{
				title:"Name",
				data: "username",
				name:"username",
				parameter: "username"
			},
			{
				title:"Date/Time",
				data: "editDate",
				name:"editDate",
				parameter: "editDate",
				render: IdtActions.formatDateWithSeconds()
			},
			{
				title:"Section Name",
				data: "sectionName",
				name:"sectionName",
				parameter: "sectionName"
			},
			{
				title:"Data Element Name",
				data: "dataElementName",
				name:"dataElementName",
				parameter: "dataElementName"
			},
			{
				title:"Question Text",
				data: "questionText",
				name:"questionText",
				parameter: "questionText"
			},
			{
				title:"Answers Before",
				data: "prevAnswer",
				name:"prevAnswer",
				parameter: "prevAnswer",
				render: IdtActions.ellipsis(1000)				
			},
			{
				title:"Answers After",
				data: "editAnswer",
				name:"editAnswer",
				parameter: "editAnswer",
				render: IdtActions.ellipsis(1000)				
			}
		],
		dom : 'Bfrtip',
	    buttons: [
			{
				extend: "collection",
				title: ename+'__Original Entries',
				className: "no-print",
				buttons: [				
					{
						extend: 'csv',
						text: 'csv',
						className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
						extension: '.csv',
						name: 'csv',
						exportOptions: {
							columns: ':visible',
							orthogonal: 'export'
						},
						enabled: true,
						action: IdtActions.exportAction()
						
					},
					{
						extend: 'pdf',
						text: 'pdf',
						className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn buttons-pdf',
						extension: '.pdf',
						name: 'pdf',
						exportOptions: {
							columns: ':visible',
							orthogonal: 'export'
						},
						enabled: true,
						orientation: 'landscape',
						action: IdtActions.exportAction(),
			            customize: IdtActions.pdfCustomizer()						
					}			
					
				]
			}
		]
	});	
	
	$('#dataEntry1ListTable').idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getDataEntryList.action",
		filterData: {
			id: aformId
		},
	    "language": {
	        "emptyTable": "There are no data entries to display at this time."
	    },
		columns: [
			{
				title:"Name",
				data: "username",
				name:"username",
				parameter: "dataEnteredByName"
			},
			{
				title:"Date/Time",
				data: "createdDate",
				name:"createdDate",
				parameter: "createdDate",
				render: IdtActions.formatDateWithSeconds()
			},
			{
				title:"Action",
				data: "status",
				name:"status",
				parameter: "status"
			},
			{
				title:"# of Questions Completed",
				data: "questionCompleted",
				name:"questionCompleted",
				parameter: "questionCompleted"
			}
			<% if (enableEsignature) { %>
			,{
				title:"e-Signature",
				data: "origEntryESignature",
				name:"origEntryESignature",
				parameter: "origEntryESignature"
			}
			<% } %>
		],
		dom : 'Bfrtip',
	    buttons: [
			{
				extend: "collection",
				title: ename+'__Form Summary Status',
				className: "no-print",
				buttons: [				
					{
						extend: 'csv',
						text: 'csv',
						className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
						extension: '.csv',
						name: 'csv',
						exportOptions: {
							columns: ':visible',
							orthogonal: 'export'
						},
						enabled: true,
						action: IdtActions.exportAction()
						
					},
					{
						extend: 'pdf',
						text: 'pdf',
						className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn buttons-pdf',
						extension: '.pdf',
						name: 'pdf',
						exportOptions: {
							columns: ':visible',
							orthogonal: 'export'
						},
						enabled: true,
						orientation: 'landscape',
						action: IdtActions.exportAction(),
			            customize: IdtActions.pdfCustomizer()						
					}			
					
				]
			}
		]
	});	
	
	//lockedListTable
	
	$('#finalLockListTable').idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getFinalLockedList.action",
		filterData: {
			id: aformId
		},
	    "language": {
	        "emptyTable": "There is no information for final lock."
	    },
		columns: [
			{
				title:"Name",
				data: "username",
				name:"username",
				parameter: "finalLockByUsername"
			},
			{
				title:"Date/Time",
				data: "finalLockDate",
				name:"finalLockDate",
				parameter: "finalLockDate",
				render: IdtActions.formatDateWithSeconds()
			}
			<% if (enableEsignature) { %>
			,{
				title:"e-Signature",
				data: "lockESignature",
				name:"lockESignature",
				parameter: "lockESignature"
			}
			<% } %>
		]
	});
	
	//ArchivesListTable
	$('#responseListTable').idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getEditArchivesList.action",
		filterData: {
			id: aformId
		},
	    "language": {
	        "emptyTable": "There are no edited answers to display at this time."
	    },
		columns: [
			{
				title:"Name",
				data: "username",
				name:"username",
				parameter: "username"
			},
			{
				title:"Date/Time",
				data: "editDate",
				name:"editDate",
				parameter: "editDate",
				render: IdtActions.formatDateWithSeconds()
			},
			{
				title:"Section Name",
				data: "sectionName",
				name:"sectionName",
				parameter: "sectionName"
			},
			{
				title:"Data Element Name",
				data: "dataElementName",
				name:"dataElementName",
				parameter: "dataElementName"
			},
			{
				title:"Question Text",
				data: "questionText",
				name:"questionText",
				parameter: "questionText"
			},
			{
				title:"Answers Before",
				data: "prevAnswer",
				name:"prevAnswer",
				parameter: "prevAnswer",
				render: IdtActions.ellipsis(1000)				
			},
			{
				title:"Answers After",
				data: "editAnswer",
				name:"editAnswer",
				parameter: "editAnswer",
				render: IdtActions.ellipsis(1000)				
			},
			{
				title:"Reason for Change",
				data: "reasonForEdit",
				name:"reasonForEdit",
				parameter: "reasonForEdit"
			}						
			<% if (enableEsignature) { %>
			,{
				title:"e-Signature",
				data: "editESignature",
				name:"editESignature",
				parameter: "editESignature"
			}
			<% } %>
		],
	    dom : 'Bfrtip',
	    buttons: [
			{
				extend: "collection",
				title: ename+'__Answers edited after complete or lock',
				className: "no-print",
				buttons: [				
					{
						extend: 'csv',
						text: 'csv',
						className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
						extension: '.csv',
						name: 'csv',
						exportOptions: {
							columns: ':visible',
							orthogonal: 'export'
						},
						enabled: true,
						action: IdtActions.exportAction()
						
					},
					{
						extend: 'pdf',
						text: 'pdf',
						className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn buttons-pdf',
						extension: '.pdf',
						name: 'pdf',
						exportOptions: {
							columns: ':visible',
							orthogonal: 'export'
						},
						enabled: true,
						orientation: 'landscape',
						action: IdtActions.exportAction(),
			            customize: IdtActions.pdfCustomizer()						
					}			
					
				]
			}
		]
	});
	
	//ArchivesListTable
	$('#sentEmailsTable').idtTable({
		idtUrl: "<s:property value='#webRoot'/>/response/getSentEmailsList.action",
		filterData: {
			id: aformId
		},
	    "language": {
	        "emptyTable": "No emails have been sent."
	    },
		columns: [
			{
				title:"Date&nbsp;Sent",
				data: "dateSent",
				name:"dateSent",
				parameter: "dateSent",
				render: IdtActions.formatDate()
			},
			{
				title:"Sent&nbsp;To",
				data: "toEmailAddress",
				name:"toEmailAddress",
				parameter: "toEmailAddress"
			},
			{
				title:"Carbon&nbsp;Copy",
				data: "ccEmailAddress",
				name:"ccEmailAddress",
				parameter: "ccEmailAddress"
			},
			{
				title:"Email Subject",
				data: "subject",
				name:"subject",
				parameter: "subject"
			},
			{
				title:"Triggered Answer",
				data: "triggeredAnswer",
				name:"triggeredAnswer",
				parameter: "triggeredAnswer"
			}						
		],
	    dom : 'Bfrtip',
	    buttons: [
			{
				extend: "collection",
				title: ename+'__Sent Emails',
				className: "no-print",
				buttons: [				
					{
						extend: 'csv',
						text: 'csv',
						className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn  buttons-csv',
						extension: '.csv',
						name: 'csv',
						exportOptions: {
							columns: ':visible',
							orthogonal: 'export'
						},
						enabled: true,
						action: IdtActions.exportAction()
						
					},
					{
						extend: 'pdf',
						text: 'pdf',
						className: 'btn btn-xs btn-primary p-5 m-0 width-35 assets-export-btn buttons-pdf',
						extension: '.pdf',
						name: 'pdf',
						exportOptions: {
							columns: ':visible',
							orthogonal: 'export'
						},
						enabled: true,
						orientation: 'landscape',
						action: IdtActions.exportAction(),
			            customize: IdtActions.pdfCustomizer()						
					}			
					
				]
			}
		]
	});	
	
	$("#exportCSV").click(function(){
		
 		var tables = ["dataEntryListTable","dataEntry1ListTable","responseListTable","sentEmailsTable"]; //table ids
 		var tableNames = ["Original Entries","Form Summary Status","Answers edited after complete/lock","Sent Emails"]; //table names
 		var finalCSVString ='Data Collection Audit Log \r\n';
 		var count=0;
 		
 		//iterate details above the tables like eform name, protocol name etc and append it to string
 		$('#dataEntryHeader td').each(function() {
 			finalCSVString+=$(this).text().replace(/\r?\n|\r/g, "");
 			if((count+1)%2 === 0){
 				finalCSVString+='\r\n'; //add new line
 			}else{
 				finalCSVString+=' ';
 			}
 			count++;
 		});
 		finalCSVString+='\r\n';
 		
 		//Iterate all the tables in View Audit page
 		for(var i=0; i<tables.length; i++){
	  		var titles = [];
			var data = [];
		
			/*
			 * Get the table headers, this will be CSV headers
			 * The count of headers will be CSV string separator
			 */
			 var tableId = '#'+tables[i];
			  $(tableId+' th').each(function() {
			    titles.push($(this).text());
			  });
		
			  /*
			   * Get the actual data, this will contain all the data, in 1 array
			   */
			  $(tableId+' td').each(function() {
			    data.push($(this).text());
			  });
			  
			  /*
			   * Convert our data to CSV string
			   */
			  var CSVString = prepCSVRow(titles, titles.length, '');
			  CSVString = prepCSVRow(data, titles.length, CSVString);
			  finalCSVString = finalCSVString + tableNames[i] + '\r\n' + CSVString + '\r\n';
 		}
 		downloadCSV(finalCSVString);
	  
	});

	function downloadCSV(finalCSVString){
		/*
		   * Make CSV downloadable
		   */
		  var downloadLink = document.createElement("a");
		  var blob = new Blob(["\ufeff", finalCSVString]);
		  var url = URL.createObjectURL(blob);
		  downloadLink.href = url;
		  downloadLink.download = "AuditLog.csv";

		  /*
		   * Actually download CSV
		   */
		  document.body.appendChild(downloadLink);
		  downloadLink.click();
		  document.body.removeChild(downloadLink);
	}
	   /*
	* Convert data array to CSV string
	* @param arr {Array} - the actual data
	* @param columnCount {Number} - the amount to split the data into columns
	* @param initial {String} - initial string to append to CSV string
	* return {String} - ready CSV string
	*/
	function prepCSVRow(arr, columnCount, initial) {
	  var row = ''; // this will hold data
	  var delimeter = ','; // data slice separator, in excel it's `;`, in usual CSv it's `,`
	  var newLine = '\r\n'; // newline separator for CSV row

	  /*
	   * Convert [1,2,3,4] into [[1,2], [3,4]] while count is 2
	   * @param _arr {Array} - the actual array to split
	   * @param _count {Number} - the amount to split
	   * return {Array} - splitted array
	   */
	  function splitArray(_arr, _count) {
	    var splitted = [];
	    var result = [];
	    _arr.forEach(function(item, idx) {
	      if ((idx + 1) % _count === 0) {
	        splitted.push(item);
	        result.push(splitted);
	        splitted = [];
	      } else {
	        splitted.push(item);
	      }
	    });
	    return result;
	  }
	  var plainArr = splitArray(arr, columnCount);
	  
	  if(plainArr.length == 0){
		  row = arr[0] + newLine;
	  } else{
	  
	  // it converts `['a', 'b', 'c']` to `a,b,c` string
	  plainArr.forEach(function(arrItem) {
	    arrItem.forEach(function(item, idx) {
	    	if(item.indexOf(',') > -1){
	    		itemArr = item.split(',');
	    		item = itemArr[1]+" "+itemArr[0];
	    	}
	      row += item + ((idx + 1) === arrItem.length ? '' : delimeter);
	    });
	    row += newLine;
	  });
	  }
	  return initial + row;
	}
})
</script>

</html>