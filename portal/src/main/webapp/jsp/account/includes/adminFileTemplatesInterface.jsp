<%@include file="/common/taglibs.jsp"%>

<s:if test="!isRequest">
	<h3>Electronic Signatures</h3>
	
	<div id="electronicSignaturesContainer" class="idtTableContainer">
		<div id="dialog"></div>
		<table id="electronicSignaturesTable" class="table table-striped table-bordered" width="100%"></table>
	</div>
	<script>
		$(document).ready(function() {
			$('#electronicSignaturesTable').idtTable({
				"columns": [
					{
						"data": "fileName",
						"title": "FILE NAME",
						"name": "File Name",
						"width": "45%"
					},
					{
						"data": "completeDate",
						"title": "COMPLETE DATE/TIME",
						"name": "Completion Date/Time",
						"width": "50%",
						"render": IdtActions.formatDate()
					}
				],
				autoWidth: false,
				dom: "rtip",
				data: [
					<s:iterator value="currentAccount.electronicSignatures" var="signature">
					{
						"fileName": "<a href='fileDownloadAction!download.action?fileId=<s:property value='signatureFile.id' />'><s:property value='signatureFile.name' /></a>",
						"completeDate": "<s:date name='signatureDate' format='yyyy-MM-dd HH:mm' />"
					},
					</s:iterator>
				]
			})
		})
	</script>
</s:if>

<h3>Administrative File Templates</h3>

<div id="fileTemplatesContainer" class="idtTableContainer">
	<div id="dialog"></div>
	<table id="fileTemplatesTable" class="table table-striped table-bordered" width="100%"></table>
</div>
	
<script>
	$(document).ready(function() {
		$('#fileTemplatesTable').idtTable({
			"columns": [
				{
					"data": "fileTemplate",
					"title": "FILE TEMPLATE",
					"name": "File Template",
					"width": "45%"

				},
				{
					"data": "privilege",
					"title": "REQUIRED FOR PRIVILEGE",
					"name": "Required For Privilege",
					"width": "50%"
				}
			],
			autoWidth: false,
			<%-- Note the form below is not used by PDBP, only for FITBIR --%>
			data: [
				<s:if test="userFileExists(6) eq true">
				{
					"fileTemplate": "<a href='fileDownloadAction!download.action?fileId=6'>Data Submission Request</a>",
					"privilege": "Data Dictionary, Data Repository, GUID, ProFoRMS, Yearly Account Renewal"							
				},
				</s:if>
				<s:if test="userFileExists(4) eq true">
				{
					"fileTemplate": "<a href='fileDownloadAction!download.action?fileId=4'>Data Access Request</a>",
					"privilege": "Query"							
				},
				</s:if>
				<s:if test="userFileExists(9) eq true">
				{
					"fileTemplate": "<a href='fileDownloadAction!download.action?fileId=9'>Biographical Sketch</a>",
					"privilege": "Yearly Account Renewal"							
				},
				</s:if>
				<s:if test="userFileExists(12) eq true">
				{
					"fileTemplate": "<a href='fileDownloadAction!download.action?fileId=12'>Genomics DUC</a>",
					"privilege": "Access to all Genomics studies (Please note that this is optional and not needed to access the PDBP DMR)"							
				}
				</s:if>				
			]
		})
	})
</script>
