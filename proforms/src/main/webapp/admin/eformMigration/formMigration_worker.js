importScripts("../../common/js/encoding-min.js");

function log(type, message, data) {
	self.postMessage({
		task : data.task,
		result : "message",
		formDivId : data.formDivId,
		wkIdx : data.wkIdx,
		formObj : data.formObj,
		msgType : type.toUpperCase(),
		msg : message
	});
}

function sendErrorMsg(xhr, data) {
	var uint8array = new TextEncoder().encode(xhr.responseText);
	
	log("error", "Failed to create an eForm for the " +  data.formObj.name + " form in the data dictionary.", data);
	
	// Send error back to the main page.
	self.postMessage({
		task : data.task,
		result : "error",
		formDivId : data.formDivId,
		wkIdx : data.wkIdx,
		formObj : data.formObj,
		respStat : xhr.status,
		respStatTxt : xhr.statusText,
		encResponse : uint8array
	}, [uint8array.buffer]);
}

function convertFormToEform(data) {
	var xhr = new XMLHttpRequest();
	
	// Respond to any HTTP success responses.
	xhr.addEventListener("load", function(e) {
		// Check for success code.
		if ( xhr.status < 400 ) {
			// Remove the extra '"', '\"', and '\\' characters from the response string. Thanks Struts 2 JSON plug in.
			var jsonStr = xhr.responseText.substring(1, xhr.responseText.length - 1).replace(/\\"/g, '"').replace(/\\{2,}/g, '\\');
			var uint8array = new TextEncoder().encode(jsonStr);
			
			log("info", "Form JSON object successfully created for the \"" + data.formObj.name + "\" form.", data);
			
			// Send the eForm JSON back to the main page.
			self.postMessage({
				task : data.task,
				result : "success",
				formDivId : data.formDivId,
				wkIdx : data.wkIdx,
				formObj : data.formObj,
				encJsonData : uint8array
			}, [uint8array.buffer]);
		}
		else {
			sendErrorMsg(xhr, data);
		}
	}, false);
	
	// Listen for any HTTP error responses.
	xhr.addEventListener("error", function(e) {
		sendErrorMsg(xhr, data);
	}, false);
	
	log("info", "Converting the \"" + data.formObj.name + "\" form to eForm JSON.", data);
	
	// Initialize request for the eForm conversion action.
	xhr.open("GET", "eFormConversion.action?formId=" + data.formObj.formid);
	xhr.setRequestHeader("Accept", "text/plain; q=0.5, text/json, application/json");
	
	// Send the request.
	xhr.send();
}

function createEform(data) {
	var xhr = new XMLHttpRequest();
	
	// Respond to any HTTP success responses.
	xhr.addEventListener("load", function(e) {
		// Check for success code.
		if ( xhr.status < 400 ) {
			var uint8array = new TextEncoder().encode(xhr.responseText);
			
			log("info", "eForm successfully created for the \"" + data.formObj.name + "\" form in the data dictionary.", data);
			
			// Send the eForm XML back to the main page.
			self.postMessage({
				task : data.task,
				result : "success",
				formDivId : data.formDivId,
				wkIdx : data.wkIdx,
				formObj : data.formObj,
				encXmlData : uint8array
			}, [uint8array.buffer]);
		}
		else {
			sendErrorMsg(xhr, data);
		}
	}, false);
	
	// Listen for any HTTP error responses.
	xhr.addEventListener("error", function(e) {
		sendErrorMsg(xhr, data);
	}, false);
	
	log("info", "Creating eForm in dictionary for the \"" + data.formObj.name + "\" form.", data);
	
	// Initialize request for the eForm conversion action.
	xhr.open("POST", "createEForm.action");
	xhr.setRequestHeader("Accept", "text/plain; q=0.5, text/xml, application/xml");
	xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
	
	// Send the request.
	xhr.send(data.encJsonData.buffer);
}

function linkEformInProforms(data) {
	var xhr = new XMLHttpRequest();
	
	// Respond to any HTTP success responses.
	xhr.addEventListener("load", function(e) {
		// Check for a success status.
		if ( xhr.status < 400 ) {
			log("info", "eForm successfully linked to the \"" + data.formObj.name + "\" form in ProFoRMS.", data);
			
			// Send the eForm XML back to the main page.
			self.postMessage({
				task : data.task,
				result : "success",
				formDivId : data.formDivId,
				wkIdx : data.wkIdx,
				formObj : data.formObj,
				eFormShortName : data.eFormShortName
			});
		}
		else {
			sendErrorMsg(xhr, data);
		}
	}, false);
	
	log("info", "Linking eForm (" + data.eFormShortName + ") in ProFoRMS.", data);
	
	// Initialize request for the eForm conversion action.
	xhr.open("POST", "linkEFormToForm.action?formId=" + data.formObj.formid);
	xhr.setRequestHeader("Content-Type", "application/xml; charset=UTF-8");
	
	// Send the request.
	xhr.send(data.encXmlData.buffer);
}

/**
 * Listens for messages from the controller. Once sent, the migration
 * of the specified form will start.
 */
self.addEventListener("message", function(e) {
	var msgData = e.data;
	
	switch ( msgData.task ) {
		case "form-to-json":
			convertFormToEform(msgData);
			break;
		case "create-eform":
			createEform(msgData);
			break;
		case "link-eform":
			linkEformInProforms(msgData);
			break;
		default:
			log("error", "Unknown task. Terminating the worker.", msgData);
			self.close();
			break;
	}
}, false);
