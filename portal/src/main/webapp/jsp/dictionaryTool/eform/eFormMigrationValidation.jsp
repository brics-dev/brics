<%@include file="/common/taglibs.jsp"%>
<title>Migrated eForm Validation</title>

<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<jsp:include page="../../navigation/dataDictionaryNavigation.jsp" />
	<h1 class="float-left">Migrated eForm Validation</h1>
	<div style="clear:both"></div>
	
	<!--begin #center-content -->
	<div id="main-content" style="padding:10px 15px 0 0;" >
		<s:if test="isAdmin">
			<div id="fileUploadDiv" >
				<h2>Upload ProFoRMS Form CSV</h2>
				<form id="csvUploadForm" action="/portal/ws/ddt/dictionary/tools/eforms/validation/start" 
						method="post" enctype="multipart/form-data" target="upload_target" accept-charset="utf-8" >
					<div class="form-field">
						<label for="fileUploadField" class="required">ProFoRMS CSV File <span class="required">* </span>:</label>
						<input type="file" id="fileUploadField" name="csvFile" accept=".csv" />
					</div>
					<div class="form-field">
						<label for="filterDupsChkbx">Filter Out Duplicates:</label>
						<input type="checkbox" id="filterDupsChkbx" name="noDups" value="true" checked="checked" />
					</div>
					<div class="form-field">
						<input type="submit" id="fileSubmitBtn" value="Submit File" />
					</div>
				</form>
				
				<iframe id="upload_target" name="upload_target" src="#" style="display: none;" ></iframe>                 
			</div>
			<div id="validationStatusDiv" style="display: none;">
				<form id="csvGenForm" action="/portal/ws/ddt/dictionary/tools/eforms/validation/genCsvFile" 
						method="post" accept-charset="utf-8" >
					<input type="hidden" id="issueArray" name="issueArray" value="[]"/>
					<div class="form-field inline-right-button">
						<input type="submit" id="genCSVFileBtn" value="Convert to CSV"/>
					</div>
				</form>
				<div id="validationStatusTableDiv"></div>
			</div>
		</s:if>
		<s:else>
			<br/>
			<br/>
			<p>You do not have access to this page.</p>
		</s:else>
	</div>
</div>
<div class="ibisMessaging-dialogContainer"></div>

<s:if test="isAdmin">
	<script id="validationIssuesTemplate" type="text/x-handlebars-template" >
		<p>{{message}}</p>
		<div class="dataTableContainer">
			<table id="issueTable" class="dataTable">
				<tr class="tableRowHeader">
					<th>eForm Short Name</th>
					<th>Section Name</th>
					<th>Question Name</th>
					<th>Issue Description</th>
				</tr>
				{{#each issueArray}}
					<tr>
						<td>{{eFormName}}</td>
						<td>{{sectionName}}</td>
						<td>{{questionName}}</td>
						<td>{{issueDesc}}</td>
					</tr>
				{{/each}}
			</table>
		</div>
	</script>
	
	<script type="text/javascript" >
		var issueDisplayTemplate = Handlebars.compile($("#validationIssuesTemplate").html());
		var refreshIntervalId = -1;
		var refreshInterval = 300000;
		var issues = null;
	
		$("document").ready(function() {
			// Set an interval function to keep the session from timing out.
			refreshIntervalId = setInterval(extendSession, refreshInterval);
			
			// Disable the default session extension logic.
			$("body").off("click");
			
			$("#fileSubmitBtn").click(_.debounce(function() {
				$(this).prop("disabled", true);
			}, 3000, true));
			
			$("#upload_target").load(function() {
				var jsonString = $(this).contents().find("body").text();
				
				// Display the validation output from the server.
				try {
					var respData = JSON.parse(jsonString);
					var htmlText = issueDisplayTemplate(respData);
					
					$("#validationStatusTableDiv").html(htmlText);
					$("#validationStatusDiv").show();
					$("#fileSubmitBtn").prop("disabled", false);
					issues = respData.issueArray;
				}
				catch (error) {
					console.error("There was an error with the CSV upload: " + error);
					$.ibisMessaging("dialog", "error", "There was an error while processing the CSV.");
					$("#fileSubmitBtn").prop("disabled", false);
					$("#upload_target").show();
				}
			});
			
			$("#genCSVFileBtn").click(_.debounce(function() {
				$("#issueArray").val(JSON.stringify(issues));
			}, 3000, true));
		});
	</script>
</s:if>