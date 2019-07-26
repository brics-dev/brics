(function($) {

	var uploadedFile;
	var fileObj;
	// output information
	function Output(msg) {
		console.log(msg);
	}
	// file drag hover
	function FileDragHover(e) {
		
		e.stopPropagation();
		e.preventDefault();
		e.target.className = (e.type == "dragover" ? "hover" : "");
	}

	// file selection
	function FileSelectHandler(e) {
		
		
		fileObj = e;
		
		
		jQuery.event.props.push('dataTransfer');
		// cancel event and hover styling
		FileDragHover(e);
 		
		// fetch FileList object
		var files = e.target.files || e.originalEvent.dataTransfer.files;
		
		uploadedFile = files;
		
		
		// process all File objects
		for (var i = 0, f; f = files[i]; i++) {
			ParseFile(f); //Show information about file
		}

	}
	
	// output file information
	function ParseFile(file) {

		// display text
		if (file.type.indexOf("text") == 0) {
			var reader = new FileReader();
			reader.onload = function(e) {
				Output(
					"<p><strong>" + file.name + ":</strong></p><pre>" +
					e.target.result.replace(/</g, "&lt;").replace(/>/g, "&gt;") +
					"</pre>"
				);
			}
			reader.readAsText(file);
		}

	}

		
	function FileSelectUpload(e) {
		
		jQuery.event.props.push('dataTransfer');
		// cancel event and hover styling
		FileDragHover(e);
// fetch FileList object
		//var files = fileObj.target.files || fileObj.originalEvent.dataTransfer.files;
		var files = uploadedFile;
		// process all File objects
		for (var i = 0, f; f = files[i]; i++ ) {
			
			UploadFile(f);
		}
	

	}

	


	// upload JPEG files
	function UploadFile(file) {

		var xhr = new XMLHttpRequest();
	
		if (xhr.upload && file.size <= $("#MAX_FILE_SIZE").val()) {

			///hmmm this damn line i don't even know if upload is complete
			xhr.upload.addEventListener("load", function(e) { console.log("upload complete"); }, false);

			// file received/failed
			xhr.onreadystatechange = function(e) {
			
				
				if (xhr.readyState > 2) {
					
					
					//debug
					console.log(xhr.responseText);
	
					var new_response = xhr.responseText.substring(xhr.responseText.length);
                    var result =  new_response ;
                    xhr.previous_text = xhr.responseText;
					
					
					
					
					if (xhr.readyState == 4) {
	
						if(xhr.status == 200) {
							//upload passed we can manipulate objects on the page here if needed/wanted
							
							
						}
					}
				}
			};
	
	
			
			
			// start upload
			xhr.open("POST", $("#cardBgUploadFrm").attr('action'), true);
			
			xhr.setRequestHeader("X-FILENAME", file.name);	
			
			xhr.setRequestHeader("X-FILETYPE", file.type);
		
			xhr.setRequestHeader("X-FILETMPNAME", $("#userID").val() + 'tempName');
			xhr.setRequestHeader("X-FILEERROR", file.error);
			xhr.setRequestHeader("X-FILESIZE", file.size);
			
			
			
			xhr.send(file);
			
			//$().closeSubFeed('cardBGUploadWrapper');
			
			

		}

	}
	
	
	
	

	// initialize
	function Init(options) {

		var bgFile = $("#bgFile");
		//var dropArea = $("#dropArea");
		var saveImage = $("#saveImage");
		
		
		
		
		// file select
		bgFile.bind("change", FileSelectHandler);

		// is XHR2 available?
		var xhr = new XMLHttpRequest();
		
		if (xhr.upload) {

			// file drop
			/*dropArea.bind("dragover", FileDragHover);
			dropArea.bind("dragleave", FileDragHover);
			dropArea.bind("drop", FileSelectHandler);
			dropArea.css("display","block");*/
			saveImage.bind("click",FileSelectUpload);
			
			
		}

	}

	// call initialization file
	if (window.File && window.FileList && window.FileReader) {
		
		Init();
	}


}(jQuery));