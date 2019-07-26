<!-- Adjustments to the progress spinner in the dialog box. -->
<style>
	.loadingSpinner {
		position: relative;
		margin-left: 110px;
		top: auto;
		left: auto;
	}
</style>

<!-- Create DOI JavaScript to run the UI. -->
<script type="text/javascript">
	// Check if "underscore-min.js" is loaded.
	if ( typeof _ !== "function" ) {
		document.write('<script type="text/javascript" src="/portal/formbuilder/js/lib/underscore-min.js"><\/script>');
	}
</script>
<script type="text/javascript">
	var doiCreateUrl = "";
	var isDialogMsgAllowed = false;
	
	/**
	 * Allows the parent page to initize the dialog UI values.
	 *
	 * @param actionUrl - The URL used to call the action on the server to create the DOI for
	 * 					  current object.
	 * @param objRefName - The name of the referenced object that a DOI will be created for
	 *					   (i.e. study or meta study).
	 * @param isMsgAllowed - An optional parameter which is used to signal whether or not to
	 *						 display error messages from the server in the UI.
	 * @throws An error if the action URL and object reference name parameters are missing.
	 */
	function initCreateDoiDialog(actionUrl, objRefName, isMsgAllowed) {
		// Validate the action URL and object reference name were passed in.
		if ( (typeof actionUrl == "undefined") || (typeof objRefName == "undefined") ) {
			throw "The \"actionUrl\" and \"objRefName\" parameters are required.";
		}
		
		// Assign the show message flag if able.
		if ( typeof isMsgAllowed == "boolean" ) {
			isDialogMsgAllowed = isMsgAllowed;
		}
		
		// Set the object references in the dialog UI, and store the action URL.
		doiCreateUrl = actionUrl;
		$("#doiDialogUi").find(".doiObjRefName").text(objRefName);
	}

	/**
	 * Parses the generic Tomcat error page for the error message.
	 * 
	 * @param $errorPage - A jQuery object of the Tomcat error page.
	 */
	function getMsgFromErrResponse($errorPage) {
		var msg = "";
		
		// Find the message bold tag.
		$errorPage.find("b").each(function() {
			var $boldTag = $(this);
			
			if ($boldTag.text().trim() == "message") {
				msg = $boldTag.next().text().trim();
				return false;
			}
		});
		
		return msg;
	}
	
	$("document").ready(function() {
		var $doiDialogDiv = $("#doiDialogUi");
		
		// Init the DOI dialog box UI
		$doiDialogDiv.dialog({
			autoOpen: false,
			modal: true,
			resizable: false,
			width: 500,
			
			close: function(event, ui) {
				var $dialogDiv = $(this);
				
				// Reset all display divs to their default visiblity.
				$dialogDiv.find("#doiAssignDisplay").show();
				$dialogDiv.find("#doiProcessingDisplay").hide();
				
				//// Reset the result display visiblity.
				var $resultDiv = $dialogDiv.find("#doiCreateResultDisplay");
				
				$resultDiv.find("#doiCreateResult").text("Complete");
				$resultDiv.find("#doiCreateSuccessMsg").show();
				$resultDiv.find("#doiCreateFailMsg").hide();
				$resultDiv.find("#doiServerMsgs").empty();
				$resultDiv.hide();
				
				// Reset dialog width.
				$dialogDiv.dialog("option", "width", 500);
				
				// Re-center the dialog.
				$dialogDiv.dialog("option", "position", {my: "center", at: "center", of: window});
			}
		});
		
		// Bind click handler to the #doiCreateBtn button in the dialog UI.
		$doiDialogDiv.find("#doiCreateBtn").click(_.debounce(function(event) {
			var $dialogDiv = $(this).parents("#doiDialogUi");
			
			// Hide the assign DOI UI and show the processing message.
			$dialogDiv.find("#doiAssignDisplay").hide();
			$dialogDiv.find("#doiProcessingDisplay").show();
			$dialogDiv.dialog("option", "width", 311);
			
			// Ask server to create a DOI for this study.
			$.getJSON(doiCreateUrl, function(data) {
				var jsonData = JSON.parse(data);
				
				// Trigger DOI create event to allow the parent page to respond to the successful creation of the DOI.
				EventBus.trigger("create:doi", jsonData, $dialogDiv);
				
				// Show the successful completion interface.
				$dialogDiv.find("#doiProcessingDisplay").hide();
				$dialogDiv.find("#doiCreateResultDisplay").show();
				$dialogDiv.dialog("option", "width", 300);
			})
			.fail(function(jqXHR, textStatus, errorThrown) {
				var cause = getMsgFromErrResponse($(jqXHR.responseText));
				
				// Output the error message to the dialog box, if allowed.
				if ( isDialogMsgAllowed ) {
					$.ibisMessaging("primary", "error", cause, {container: "#doiServerMsgs"});
				}
				
				console.error("Couldn't create a DOI. Cause: " + cause);
				
				// Trigger DOI error event to allow the parent page to respond to the DOI creation error.
				EventBus.trigger("error:doi", cause, $dialogDiv);
				
				// show error interface.
				var $resultDiv = $dialogDiv.find("#doiCreateResultDisplay");
				
				$dialogDiv.find("#doiProcessingDisplay").hide();
				$resultDiv.show();
				$resultDiv.find("#doiCreateResult").text("Error");
				$resultDiv.find("#doiCreateSuccessMsg").hide();
				$resultDiv.find("#doiCreateFailMsg").show();
			});
		}, 3000, true));
	});
</script>

<!-- The UI of the create DOI dialog -->
<div id="doiDialogUi" style="display: none;">
	<div id="doiAssignDisplay">
		<h3>Study DOI Assignment</h3>
		<br/>
		<p>
			This action will assign a DOI to the <span class="doiObjRefName"></span>. Once a DOI is assigned,
			the action cannot be reversed. Please ensure that the <span class="doiObjRefName"></span> is ready 
			for DOI assignment before proceeding. Click the "Cancel" button to exit this operation or the 
			"Assign DOI" button to continue the DOI assignment.
		</p>
		<br/>
		<div id="doiAssignBtns" class="button float-right">
			<input type="button" id="doiCreateBtn" value="Assign DOI" 
					title="Click to assign a DOI to this object." style="display: inline;"/>
			<input type="button" id="doiCancelAssignBtn" value="Cancel" 
					title="Click to cancel the DOI assignment action." style="display: inline;"
					onclick="$(this).parents('#doiDialogUi').dialog('close');"/>
		</div>
	</div>
	<div id="doiProcessingDisplay" style="display: none;">
		<h3>Processing DOI Assignment Request</h3>
		<br/>
		<div id="doiCreateProgress">
			<i id="doiDialogSpinner" class="loadingSpinner fa fa-spinner fa-pulse fa-5x fa-fw"></i>
		</div>
		<br/>
		<p>Please wait while the system processes the request.</p>
	</div>
	<div id="doiCreateResultDisplay" style="display: none;">
		<h3>DOI Assignment Processing <span id="doiCreateResult">Complete</span></h3>
		<br/>
		<p id="doiCreateSuccessMsg">The system has assigned a DOI to the <span class="doiObjRefName"></span>.</p>
		<p id="doiCreateFailMsg" style="display: none;">
			The system encountered an error while assigning a DOI to the 
			<span class="doiObjRefName"></span>. Please try again later.
		</p>
		<div id="doiServerMsgs"></div>
		<br/>
		<div id="doiCreateResultBtns" class="button float-right">
			<input type="button" id="doiDialogCloseBtn" value="OK"
					title="Click to close this dialog box."
					onclick="$(this).parents('#doiDialogUi').dialog('close');"/>
		</div>
	</div>
</div>