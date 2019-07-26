<jsp:include page="/common/doctype.jsp" />
<%@ taglib uri="/struts-tags" prefix="s" %>
<%@ taglib uri="/WEB-INF/display.tld" prefix="display" %>
<%@ taglib uri="/WEB-INF/security.tld" prefix="security" %>


<%-- CHECK PRIVILEGES --%>
<security:check privileges="sysadmin"/>

<html>
<%-- Include Header --%>
<s:set var="pageTitle" scope="request">
	Form to eForm Migration
</s:set>
<jsp:include page="/common/header_struts2.jsp" />

<script type="text/javascript" src="/common/js/encoding-min.js"></script>

<script type="text/javascript">
	var formsToMigrate = null;
	var maxProcesses = 5;
	var workers = [];
	
	/**
	 * Handles messages form the web worker for the eForm linkage task, and performs actions depending on the "result" 
	 * property of the message data object.
	 *
	 * @param data {object} - The message data object from the web worker.
	 */
	function eFormLinkEvtHandler(data) {
		var $formDiv = $("#" + data.formDivId);
		
		switch ( data.result ) {
			case "success":
				// Add entry to the completed forms list.
				logCompletedForm(data);
				
				// Update JSON conversion status.
				$formDiv.find(".migrateStatus .linkEformToForm").text(function(index, origText) { 
					return origText + "done.";
				});
				
				// Terminate and clear out the worker.
				workers[data.wkIdx].terminate();
				workers[data.wkIdx] = null;
				
				// Kickoff another migration process, or show the migration completion success message.
				if ( formsToMigrate.length > 0 ) {
					kickOffFormMigration();
				}
				else {
					$("#cancelAllBtn").prop("disabled", true);
					updateFormCount();
				}
				break;
			case "error":
				var msg = "Couldn't link the \"" + data.eFormShortName + "\" eForm to the \"" + data.formObj.name + "\" form.";
				
				// Update JSON conversion status.
				$formDiv.find(".migrateStatus .linkEformToForm").text(function(index, origText) {
					return origText + "failed!";
				});
				
				workerHttpErrorHandler(data, msg);
				break;
			case "message":
				console.log(data.msgType + " :: " + data.msg);
				break;
			default:
				// Update JSON conversion status.
				$formDiv.find(".migrateStatus .linkEformToForm").text(function(index, origText) {
					return origText + "failed!";
				});
				
				var msg = "Invalid message result while trying to link a new eForm. Skipping to next form.";
				handleGeneralWorkerError(data, msg);
				break;
		}
	}
	
	/**
	 * Handles messages form the web worker for the eForm creation task, and performs actions depending on the "result" 
	 * property of the message data object.
	 *
	 * @param data {object} - The message data object from the web worker.
	 */
	function eFormCreateEvtHandler(data) {
		var $formDiv = $("#" + data.formDivId);
		
		switch ( data.result ) {
			case "success":
				var xmlStr = new TextDecoder("utf-8").decode(data.encXmlData);
				
				// Check if any XML was sent back.
				if ( xmlStr.length > 0 ) {
					// Update eForm creation status to done.
					$formDiv.find(".migrateStatus .eFormCreateStat").text(function(index, origText) {
						return origText + "done.";
					});
					
					// Add status for linking eForm to ProFoRMS form.
					statusHtml = '<br/><span class="linkEformToForm">Linking eForm to ProFoRMS Form...</span>';
					$formDiv.find(".migrateStatus").append(statusHtml);
					
					// Call worker to do the eForm link task.
					data.eFormShortName = $(xmlStr).find("eformShortName").text();
					data.task = "link-eform";
					workers[data.wkIdx].postMessage(data, [data.encXmlData.buffer]);
				}
				else {
					// Update JSON conversion status.
					$formDiv.find(".migrateStatus .eFormCreateStat").text(function(index, origText) {
						return origText + "failed!";
					});
					
					var msg = "Invalid eform XML returned while trying to create a new eForm. Skipping to next form.";
					handleGeneralWorkerError(data, msg);
				}
				break;
			case "error":
				var msg = "Couldn't save the eForm version of \"" + data.formObj.name + "\" in the Dictionary.";
				
				// Update JSON conversion status.
				$formDiv.find(".migrateStatus .eFormCreateStat").text(function(index, origText) {
					return origText + "failed!";
				});
				
				workerHttpErrorHandler(data, msg);
				break;
			case "message":
				console.log(data.msgType + " :: " + data.msg);
				break;
			default:
				// Update JSON conversion status.
				$formDiv.find(".migrateStatus .eFormCreateStat").text(function(index, origText) {
					return origText + "failed!";
				});
				
				var msg = "Invalid message result while trying to create a new eForm. Skipping to next form.";
				handleGeneralWorkerError(data, msg);
				break;
		}
	}
	
	/**
	 * Handles messages form the web worker for the form to JSON conversion task, and performs actions depending on the "result" 
	 * property of the message data object.
	 *
	 * @param data {object} - The message data from the web worker.
	 */
	function formToEformEvtHandler(data) {
		var $formDiv = $("#" + data.formDivId);
		
		switch ( data.result ) {
			case "success":
				var jsonStr = new TextDecoder("utf-8").decode(data.encJsonData);
				
				// Check for successful eForm JSON conversion.
				if ( jsonStr.length > 0 ) {
					// Update JSON conversion status.
					$formDiv.find(".eformJsonOut").text(jsonStr);
					$formDiv.find(".migrateStatus .jsonObjStat").text(function(index, origText) {
						return origText + "done.";
					});
					
					// Add status for the creating the eForm in Dictionary.
					var statusHtml = '<br/><span class="eFormCreateStat">Creating eForm in Dictionary...</span>';
					$formDiv.find(".migrateStatus").append(statusHtml);
					
					// Start the eForm create task.
					var worker = workers[data.wkIdx];
					
					data.task = "create-eform";
					worker.postMessage(data, [data.encJsonData.buffer]);
				}
				else {
					// Update JSON conversion status.
					$formDiv.find(".migrateStatus .jsonObjStat").text(function(index, origText) {
						return origText + "failed!";
					});
					
					var msg = "Invalid JSON string returned while converting a form to an eForm JSON format. Skipping to next form.";
					handleGeneralWorkerError(data, msg);
				}
				
				break;
			case "error":
				var msg = "Couldn't convert the \"" + data.formObj.name + "\" form to an eForm JSON object.";
				
				// Reset UI elements.
				$formDiv.find(".eformJsonOut").empty();
				
				// Update JSON conversion status.
				$formDiv.find(".migrateStatus .jsonObjStat").text(function(index, origText) {
					return origText + "failed!";
				});
				
				workerHttpErrorHandler(data, msg);
				break;
			case "message":
				console.log(data.msgType + " :: " + data.msg);
				break;
			default:
				// Update JSON conversion status.
				$formDiv.find(".migrateStatus .jsonObjStat").text(function(index, origText) {
					return origText + "failed!";
				});
				
				var msg = "Invalid message result while converting a form to an eForm JSON format. Skipping to next form.";
				handleGeneralWorkerError(data, msg);
				break;
		}
	}
	
	/**
	 * Handles the output of general errors that may occur in the migration.
	 *
	 * @param data {object} - Data object passed from the worker.
	 * @param msg {string} - Custom error message.
	 */
	function handleGeneralWorkerError(data, msg) {
		// Log the error.
		console.error(msg);
		$.ibisMessaging("flash", "error", msg);
		logFailedForm(data.formObj, msg);
		
		// Clear worker data and start a new migration process.
		workers[data.wkIdx].terminate();
		workers[data.wkIdx] = null;
		kickOffFormMigration();
	}
	
	/**
	 * Provides basic logging of errors from the web workers. Will also produce a error dialog message, 
	 * and will not release the current process until the dialog box is closed.
	 *
	 * @param data {object} - Data object passed from the worker.
	 * @param msg {string} - Custom error message.
	 */
	function workerHttpErrorHandler(data, msg) {
		var responseText = new TextDecoder("utf-8").decode(data.encResponse);
		var serverError = $(responseText).find("#messages").text().trim();
		
		// Log the server error.
		console.error(msg + " " + data.respStat + " : " + data.respStatTxt + "; Reason: " + serverError);
		logFailedForm(data.formObj, msg + " Reason: " + serverError);
		
		// Show error in the UI.
		$.ibisMessaging("flash", "error", msg);
		
		// Clear worker data and start a new migration process.
		workers[data.wkIdx].terminate();
		workers[data.wkIdx] = null;
		kickOffFormMigration();
	}
	
	/**
	 * Message listener for the form migration web worker. Used to dispatch message data to verious handlers by
	 * worker task.
	 *
	 * @param event {object} - The web worker message event object, which holds the message data.
	 */
	function migrationWorkerListener(event) {
		var data = event.data;
		
		switch ( data.task ) {
			case "form-to-json":
				formToEformEvtHandler(data);
				break;
			case "create-eform":
				eFormCreateEvtHandler(data);
				break;
			case "link-eform":
				eFormLinkEvtHandler(data);
				break;
			default:
				var msg = "Invalid task sent by the worker. Skipping to next form.";
				console.error(msg);
				$.ibisMessaging("flash", "error", msg);
				logFailedForm(data.formObj, msg);
				
				// Clear worker data and start a new migration process.
				workers[data.wkIdx].terminate();
				workers[data.wkIdx] = null;
				kickOffFormMigration();
				break;
		}
	}
	
	 /**
	  * Starts the migration process for any remaining forms to be converted. If the maximum number
	  * of migration processes is reached or if there are no forms to migrate, no operation will occur.
	  */
	function kickOffFormMigration() {
		// Get form to migrate, if able.
		if ( formsToMigrate.length > 0 ) {
			// Start a migration processes, if able.
			for ( var i = 0; i < maxProcesses; i++ ) {
				if ( (typeof workers[i] == "undefined") || (workers[i] === null) ) {
					var form = formsToMigrate.shift();
					var $currFormDiv = $("#migrateProcess_" + (i + 1));
					
					// Set form info.
					setInitFormInfo($currFormDiv, form);
					
					// Create form to eForm conversion worker.
					var worker = new Worker("eformMigration/formMigration_worker.js");
					
					// Assign a message listener.
					worker.addEventListener("message", migrationWorkerListener, false);
					
					// Run the form to eForm conversion worker.
					worker.postMessage({
						task : "form-to-json",
						wkIdx : i,
						formObj : form,
						formDivId : $currFormDiv.attr("id")
					});
					
					// Register worker.
					workers[i] = worker;
				}
			}
		}
		
		// Update form count.
		updateFormCount();
	}
	 
	function updateFormCount() {
		var count = formsToMigrate.length;
		
		// Include the forms that are in the process of being migrated into the count.
		for ( var i = 0; i < maxProcesses; i++ ) {
			if ( (typeof workers[i] != "undefined") && (workers[i] !== null) ) {
				count++;
			}
		}
		
		// Apply the new count on the page.
		$("#numMigrate").text(count);
	}
	
	function setInitFormInfo($formDiv, form) {
		$formDiv.find(".formId").text(form.formid);
		$formDiv.find(".formVersion").text(form.version);
		$formDiv.find(".formName").text(form.name);
		$formDiv.find(".formDesc").text(form.description);
		$formDiv.find(".isFormLegacy").text(form.islegacy);
		$formDiv.find(".linkedStudy").text(form.protocolnumber);
		$formDiv.find(".formOwner").text(form.username);
		
		// Clear any old status messages.
		$formDiv.find(".migrateStatus").empty();
		
		// Add status for eForm JSON creation.
		var statusHtml = '<span class="jsonObjStat">Converting form to eForm JSON...</span>';
		$formDiv.find(".migrateStatus").append(statusHtml);
	}
	
	function clearFormDiv($formDiv) {
		// Clear current form info.
		$formDiv.find(".formId").text("");
		$formDiv.find(".formVersion").text("");
		$formDiv.find(".formName").text("");
		$formDiv.find(".formDesc").text("");
		$formDiv.find(".isFormLegacy").text("");
		$formDiv.find(".linkedStudy").text("");
		$formDiv.find(".formOwner").text("");
		
		// Clear status messages.
		$formDiv.find(".migrateStatus").empty();
		
		// Clear JSON string output.
		$formDiv.find(".eformJsonOut").empty();
	}
	
	function logFailedForm(form, errorMsg) {
		var $failedForms = $("#failedForms");
		
		// Add entry to the failed forms list.
		$failedForms.append("<li>Form ID: " + form.formid + ", Form Name: " + form.name +
				"<br/>" + errorMsg + "</li>");
		
		// Update failure count.
		var count = $failedForms.children().length;
		
		$("#totalFailed").text(count);
	}
	
	function logCompletedForm(data) {
		var $completedForms = $("#completedForms");
		
		// Add entry to the completed forms list.
		$completedForms.append("<li>Form ID: " + data.formObj.formid + ", Form Name: " + 
				data.formObj.name + ", eForm Short Name: " + data.eFormShortName + "</li>");
		
		// Update completed count.
		var count = $completedForms.children().length;
		
		$("#totalCompleted").text(count);
	}

	$(document).ready(function() {
		// Disable auto log out.
		clearLogoutTimeout();
		
		formsToMigrate = JSON.parse($("#formMigrateArray").val());
		
		// Verify that the formsToMigrate array is set to at least an empty array.
		if ( (typeof formsToMigrate == "undefined") || (formsToMigrate == null) ) {
			formsToMigrate = [];
		}
		
		// Set the number of forms to migrate.
		var numForms = formsToMigrate.length;
		
		$("#numMigrate").text(numForms);
		$("#numTotal").text(numForms);
		
		if ( numForms == 0 ) {
			$("#startMigrationBtn").prop("disabled", true);
		}
		
		// Make the needed process trackers visible.
		for ( var i = 1; i <= maxProcesses; i++ ) {
			$("#migrateProcess_" + i).show();
		}
		
		// Start Migration button click listener.
		$("#startMigrationBtn").click(function() {
			$(this).prop("disabled", true);
			$("#migrationInfo").show();
			
			// Kick-off the form migration processes.
			kickOffFormMigration();
			
			$("#cancelAllBtn").prop("disabled", false);
		});
		
		// Cancel All Migration Processes button click listener.
		$("#cancelAllBtn").click(function() {
			$(this).prop("disabled", true);
			
			if ( formsToMigrate.length > 0 ) {
				$("#startMigrationBtn").prop("disabled", false);
				
				// Iterate through the worker array and end all workers.
				for ( var i = 0; i < workers.length; i++ ) {
					if ( (typeof workers[i] != "undefined") && (workers[i] !== null) ) {
						workers[i].terminate();
						workers[i] = null;
					}
				}
				
				// Update form count.
				updateFormCount();
				
				// Clear the form info fields.
				$("div[id^='migrateProcess']").each(function() {
					clearFormDiv($(this));
				});
				
				// Re-activate start migration button.
				$("#startMigrationBtn").prop("disabled", false);
			}
		});
		
		// Stop Process buttons click listener.
		$(".stopProc-Btn").click(function() {
			var $formDiv = $(this).parents("div[id^='migrateProcess']:first");
			var procIdx = Number($formDiv.attr("id").split("_")[1]);
			
			// Verify that procIdx is a number.
			if ( !isNaN(procIdx) ) {
				procIdx--;
			}
			else {
				return false;
			}
			
			// Stop the current migration process, if it is still running.
			if ( (typeof workers[procIdx] != "undefined") && (workers[procIdx] !== null) ) {
				workers[procIdx].terminate();
				workers[procIdx] = null;
				clearFormDiv($formDiv);
				updateFormCount();
				
				// Re-activate start migration button.
				$("#startMigrationBtn").prop("disabled", false);
			}
		});
	});
</script>

<s:form>
	<s:hidden name="formMigrateJsonArray" id="formMigrateArray"/>
</s:form>

<h3><span id="numMigrate"></span> Forms of <span id="numTotal"></span> Are Available to Migrate</h3>
<div>
	<input type="button" id="startMigrationBtn" value="Begin Form Migration"/>
	<input type="button" id="cancelAllBtn" value="Cancel All Migration Processes" disabled="disabled"/>
</div>
<br/>

<div id="migrationInfo" style="display: none;">
	<h3 class="toggleable collapsed">Completed Forms (<span id="totalCompleted">0</span>):</h3>
	<div>
		<ul id="completedForms"></ul>
	</div>
	<h3 class="toggleable collapsed">Failed Forms (<span id="totalFailed">0</span>):</h3>
	<div>
		<ul id="failedForms"></ul>
	</div>
	<br/>
	
	<!-- ################################################################################### -->
	<!-- ######################### Migration Progress Tacker 1 ############################# -->
	<!-- ################################################################################### -->
	
	<div id="migrateProcess_1" style="display: none;">
		<h2>Migration Process for the &quot;<span class="formName"></span>&quot; Form</h2>
		<h3 class="toggleable">Migrating the Following Form:</h3>
		<div class="currentForm">
			<div class="formrow_2">
				<label>Form ID:</label>
				<span class="formId"></span>
			</div>
			<div class="formrow_2">
				<label>Is Legacy?:</label>
				<span class="isFormLegacy"></span>
			</div>
			<div class="formrow_2">
				<label>Form Version:</label>
				<span class="formVersion"></span>
			</div>
			<div class="formrow_2">
				<label>Linked Study:</label>
				<span class="linkedStudy"></span>
			</div>
			<div class="formrow_2">
				<label>Form Name:</label>
				<span class="formName"></span>
			</div>
			<div class="formrow_2">
				<label>Owner:</label>
				<span class="formOwner"></span>
			</div>
			<div class="formrow_2">
				<label>Form Description:</label>
				<span class="formDesc"></span>
			</div>
			<div class="formrow_1">
				<input type="button" class="stopProc-Btn" value="Stop Migration"/>
			</div>
		</div>
		
		<h3 class="toggleable">&quot;<span class="formName"></span>&quot; Form Migration Status:</h3>
		<div class="migrateStatus"></div>
		
		<h3 class="toggleable collapsed">eForm JSON:</h3>
		<div>
			<span class="eformJsonOut"></span>
		</div>
	</div>
	<br/>
	
	<!-- ################################################################################### -->
	<!-- ######################### Migration Progress Tacker 2 ############################# -->
	<!-- ################################################################################### -->
	
	<div id="migrateProcess_2" style="display: none;">
		<h2>Migration Process for the &quot;<span class="formName"></span>&quot; Form</h2>
		<h3 class="toggleable">Migrating the Following Form:</h3>
		<div class="currentForm">
			<div class="formrow_2">
				<label>Form ID:</label>
				<span class="formId"></span>
			</div>
			<div class="formrow_2">
				<label>Is Legacy?:</label>
				<span class="isFormLegacy"></span>
			</div>
			<div class="formrow_2">
				<label>Form Version:</label>
				<span class="formVersion"></span>
			</div>
			<div class="formrow_2">
				<label>Linked Study:</label>
				<span class="linkedStudy"></span>
			</div>
			<div class="formrow_2">
				<label>Form Name:</label>
				<span class="formName"></span>
			</div>
			<div class="formrow_2">
				<label>Owner:</label>
				<span class="formOwner"></span>
			</div>
			<div class="formrow_2">
				<label>Form Description:</label>
				<span class="formDesc"></span>
			</div>
			<div class="formrow_1">
				<input type="button" class="stopProc-Btn" value="Stop Migration"/>
			</div>
		</div>
		
		<h3 class="toggleable">&quot;<span class="formName"></span>&quot; Form Migration Status:</h3>
		<div class="migrateStatus"></div>
		
		<h3 class="toggleable collapsed">eForm JSON:</h3>
		<div>
			<span class="eformJsonOut"></span>
		</div>
	</div>
	<br/>
	
	<!-- ################################################################################### -->
	<!-- ######################### Migration Progress Tacker 3 ############################# -->
	<!-- ################################################################################### -->
	
	<div id="migrateProcess_3" style="display: none;">
		<h2>Migration Process for the &quot;<span class="formName"></span>&quot; Form</h2>
		<h3 class="toggleable">Migrating the Following Form:</h3>
		<div class="currentForm">
			<div class="formrow_2">
				<label>Form ID:</label>
				<span class="formId"></span>
			</div>
			<div class="formrow_2">
				<label>Is Legacy?:</label>
				<span class="isFormLegacy"></span>
			</div>
			<div class="formrow_2">
				<label>Form Version:</label>
				<span class="formVersion"></span>
			</div>
			<div class="formrow_2">
				<label>Linked Study:</label>
				<span class="linkedStudy"></span>
			</div>
			<div class="formrow_2">
				<label>Form Name:</label>
				<span class="formName"></span>
			</div>
			<div class="formrow_2">
				<label>Owner:</label>
				<span class="formOwner"></span>
			</div>
			<div class="formrow_2">
				<label>Form Description:</label>
				<span class="formDesc"></span>
			</div>
			<div class="formrow_1">
				<input type="button" class="stopProc-Btn" value="Stop Migration"/>
			</div>
		</div>
		
		<h3 class="toggleable">&quot;<span class="formName"></span>&quot; Form Migration Status:</h3>
		<div class="migrateStatus"></div>
		
		<h3 class="toggleable collapsed">eForm JSON:</h3>
		<div>
			<span class="eformJsonOut"></span>
		</div>
	</div>
	
	<!-- ################################################################################### -->
	<!-- ######################### Migration Progress Tacker 4 ############################# -->
	<!-- ################################################################################### -->
	
	<div id="migrateProcess_4" style="display: none;">
		<h2>Migration Process for the &quot;<span class="formName"></span>&quot; Form</h2>
		<h3 class="toggleable">Migrating the Following Form:</h3>
		<div class="currentForm">
			<div class="formrow_2">
				<label>Form ID:</label>
				<span class="formId"></span>
			</div>
			<div class="formrow_2">
				<label>Is Legacy?:</label>
				<span class="isFormLegacy"></span>
			</div>
			<div class="formrow_2">
				<label>Form Version:</label>
				<span class="formVersion"></span>
			</div>
			<div class="formrow_2">
				<label>Linked Study:</label>
				<span class="linkedStudy"></span>
			</div>
			<div class="formrow_2">
				<label>Form Name:</label>
				<span class="formName"></span>
			</div>
			<div class="formrow_2">
				<label>Owner:</label>
				<span class="formOwner"></span>
			</div>
			<div class="formrow_2">
				<label>Form Description:</label>
				<span class="formDesc"></span>
			</div>
			<div class="formrow_1">
				<input type="button" class="stopProc-Btn" value="Stop Migration"/>
			</div>
		</div>
		
		<h3 class="toggleable">&quot;<span class="formName"></span>&quot; Form Migration Status:</h3>
		<div class="migrateStatus"></div>
		
		<h3 class="toggleable collapsed">eForm JSON:</h3>
		<div>
			<span class="eformJsonOut"></span>
		</div>
	</div>
	
	<!-- ################################################################################### -->
	<!-- ######################### Migration Progress Tacker 5 ############################# -->
	<!-- ################################################################################### -->
	
	<div id="migrateProcess_5" style="display: none;">
		<h2>Migration Process for the &quot;<span class="formName"></span>&quot; Form</h2>
		<h3 class="toggleable">Migrating the Following Form:</h3>
		<div class="currentForm">
			<div class="formrow_2">
				<label>Form ID:</label>
				<span class="formId"></span>
			</div>
			<div class="formrow_2">
				<label>Is Legacy?:</label>
				<span class="isFormLegacy"></span>
			</div>
			<div class="formrow_2">
				<label>Form Version:</label>
				<span class="formVersion"></span>
			</div>
			<div class="formrow_2">
				<label>Linked Study:</label>
				<span class="linkedStudy"></span>
			</div>
			<div class="formrow_2">
				<label>Form Name:</label>
				<span class="formName"></span>
			</div>
			<div class="formrow_2">
				<label>Owner:</label>
				<span class="formOwner"></span>
			</div>
			<div class="formrow_2">
				<label>Form Description:</label>
				<span class="formDesc"></span>
			</div>
			<div class="formrow_1">
				<input type="button" class="stopProc-Btn" value="Stop Migration"/>
			</div>
		</div>
		
		<h3 class="toggleable">&quot;<span class="formName"></span>&quot; Form Migration Status:</h3>
		<div class="migrateStatus"></div>
		
		<h3 class="toggleable collapsed">eForm JSON:</h3>
		<div>
			<span class="eformJsonOut"></span>
		</div>
	</div>
</div>
	
<%-- Include Footer --%>
<jsp:include page="/common/footer_struts2.jsp" />
</html>