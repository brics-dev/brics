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
		<s:if test="%{orgName == 'NINR'}">
			$('#fileTemplatesTable').idtTable({
				"columns": [
					{
						"data": "program",
						"title": "PROGRAM",
						"name": "program",
						"width": "45%"
	
					},
					{
						"data": "fileTemplates",
						"title": "FILE TEMPLATES",
						"name": "fileTemplates",
						"width": "50%"
					}
				],
				autoWidth: false,
				<%-- Note the form below is not used by PDBP, only for FITBIR --%>
				data: [
					{
						"program": '<s:a href="%{modulesPublicURL}extramural" target="_blank">Extramural Data Policies</s:a>',
						"fileTemplates": "Data Sharing Policy, Data Submission Request, Data Access Request, Biographical Sketch",
					},
					{
						"program": '<s:a href="%{modulesPublicURL}intramural" target="_blank">Intramural Data Policies</s:a>',
						"fileTemplates": "Data Sharing Policy, Data Submission Request, Data Access Request, Biographical Sketch",
					}
				]
			});	
		</s:if>
		<s:else>
		$('#fileTemplatesTable').idtTable({
			"columns": [
				{
					"data": "fileTemplate",
					"title": "FILE TEMPLATE",
					"name": "File Template",
					"width": "15%"
				},
				{
					"data": "privilege",
					"title": "PRIVILEGES ASSOCIATED",
					"name": "Privileges Associated",
					"width": "45%"
				},
				{
					"data": "required",
					"title": "REQUIRED FOR ANNUAL RENEWAL",
					"name": "Required for Annual Renewal",
					"width": "35%"
				}
			],
			autoWidth: false,
			dom: "rt",
			<%-- Note the form below is not used by PDBP, only for FITBIR --%>
			data: [
				<s:if test="userFileExists(9) eq true">
				{
					"fileTemplate": "<a href='fileDownloadAction!download.action?fileId=9'>Biographical Sketch</a>",
					"privilege": "Required for Data Access Users (Data Dictionary, Data Repository, Query Tool, Meta Study)",
					"required": "Yes"
				},
				</s:if>
				<s:if test="userFileExists(4) eq true">
				{
					"fileTemplate": "<a href='fileDownloadAction!download.action?fileId=4'>Data Access Request</a>",
					"privilege": "Required for Data Access Users (Data Dictionary, Data Repository, Query Tool, Meta Study)",
					"required": "Yes"
				},
				</s:if>
				<s:if test="userFileExists(6) eq true">
				{
					"fileTemplate": "<a href='fileDownloadAction!download.action?fileId=6'>Data Submission Request</a>",
					"privilege": "Required for Data Submission Users (Data Dictionary, Data Repository, GUID, ProFoRMS)",							
					"required": "Submission Privileges are given until end of study's period of performance. Please contact the Operations team if privilege extension required."
				},
				</s:if>
				<s:if test="(userFileExists(12) eq true) && (instanceType.name == 'PDBP')">
				{
					"fileTemplate": "<a href='fileDownloadAction!download.action?fileId=12'>Genomics DUC</a>",
					"privilege": "Access to all Genetics, Genomics, and Imaging study data (Please note that this is optional and not needed to access the PDBP DMR. Access to this data requires annual renewal).",							
					"required": "Yes"
				}
				</s:if>				
			]
		})
		</s:else>
	})
</script>
