<%@taglib prefix="s" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div class="lightbox-content-wrapper">
	<s:if test="addDocSelect == 'url'">
		<h3>Add URL</h3>
	</s:if>
	<s:else>
		<h3>Add File</h3>
	</s:else>
		
	<s:form id="addDocForm" name="addDocForm" class="validate" method="post" enctype="multipart/form-data" 
			onsubmit="uploadDocument(); return false;" style="width: 98%;">
		<s:token />
		<s:if test="hasFieldErrors()">
			<div style="width:980px">&nbsp;<!-- fixes fancybox size because fancybox is terrible --></div>
			<input id="hasFieldErrors" value="true" type="hidden"/>
		</s:if>	
		<div id="messageContainer">
		
		</div>
		<s:if test="hasActionErrors()">
			<div class="errors">
				<s:actionerror />
			</div>
		</s:if>
		
		<s:hidden id="validationActionName" name="validationActionName" />
		<s:hidden id="actionName" name="actionName" />
		<s:hidden id="isEditingDoc"  name="isEditingDoc" />
		<s:hidden id="addDocSelect"  name="addDocSelect" />
						
		<s:if test="isEditingDoc">
			<input id="isEditingDoc" value="true" type="hidden"/>
		</s:if>
		
		<s:if test="addDocSelect == 'url'">
			<div class="form-field">
				<label for="url" class="required">URL <span class="required">* </span>
				</label>
				<table>
					<tr><td>
						<s:textfield id="url" name="url" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true"/>
						<s:fielderror fieldName="url" />
					</td></tr>
					<tr><td style="padding-top: 3px">
						<a id="testUrlLink" href="javascript:void(0)" onclick="testUrl()">Test URL</a>
					</td></tr>
				</table>
			</div>
		</s:if>
		
		<s:if test="addDocSelect == 'file'">
			<div class="form-field">
				<label for="file" class="required">File<span class="required">* </span>
				</label>
				<s:file id="file" name="uploadSupportDoc" />
				<s:hidden id="uploadFileName" name="uploadFileName" />
				<table align="right">
					<tr><td><s:fielderror fieldName="uploadFileName" /></td></tr>
					<tr><td><s:fielderror fieldName="uploadSupportDoc" /></td></tr>
				</table>
			</div>
		</s:if>
			
		<div class="form-field">
			<label for="supportingDocType" class="required">Type<span class="required">* </span>
			</label>
			<s:select id="supportingDocType" list="supportingDocTypes" listKey="id" listValue="name" 
					name="supportingDocType" value="supportingDocType.id" />
			<s:fielderror fieldName="supportingDocType" />
		</div>
		
		<div id="titleDiv">
			<div class="form-field">
				<label for="supportingDocTitle" class="required">Title<span class="required">* </span>
				</label>
				<s:textfield id="supportingDocTitle" escapeHtml="true" name="supportingDocTitle" cssClass="textfield" escapeJavaScript="true" maxlength="500" />
				<s:fielderror fieldName="supportingDocTitle" />
			</div>
		</div>
		
		<div id="pubDiv"> 
	
		<div class="form-field">
			<label for="pubMedId">PubMed ID </label>
			<s:textfield id="pubMedId" escapeHtml="true" name="pubEntry.pubmedId" escapeJavaScript="true" />
			<a href="javascript:void(0);" id="pubMedWS">Submit</a>
			<s:fielderror fieldName="pubEntry.pubMedId" />
		</div>
		
		<div class="form-field">
			The entries below can be automatically generated using the PubMed ID
		</div>
		
		<div class="form-field">
			<label for="docTitle">Title <span class="required">* </span></label>
			<s:textarea id="docTitle" name="pubEntry.title" cols="30" rows="3" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.title" />
		</div>

		<div class="form-field">
			<label for="pubDate" class="required">Publication Date <span class="required">* </span></label>
			<s:textfield id="pubDate" escapeHtml="true" name="pubEntry.dateString" cssClass="textfield" escapeJavaScript="true" placeholder="ex: 2016-12-05" />
			<s:fielderror fieldName="pubEntry.publicationDate" />
		</div>
		
			<div class="form-field">
		<h3 class="underlined">First Author</h3>
		</div>
		
		<div class="form-field">
			<label for="faFirstName" class="required">First Name <span class="required">* </span></label>
			<s:textfield id="faFirstName" escapeHtml="true" name="pubEntry.firstAuthor.firstName" cssClass="textfield" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.firstAuthor.firstName" />
		</div>
		

		
		<div class="form-field">
			<label for="faMiddleName">Middle Name </label>
			<s:textfield id="faMiddleName" escapeHtml="true" name="pubEntry.firstAuthor.mi" cssClass="textfield" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.firstAuthor.mi" />
		</div>
	
		<div class="form-field">
			<label for="faLastName" class="required">Last Name <span class="required">* </span></label>
			<s:textfield id="faLastName" escapeHtml="true" name="pubEntry.firstAuthor.lastName" cssClass="textfield" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.firstAuthor.lastName" />
		</div>
		
		<div class="form-field">
			<label for="faEmail">Author Email </label>
			<s:textfield id="faEmail" escapeHtml="true" name="pubEntry.firstAuthor.email" cssClass="textfield" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.firstAuthor.email" />
		</div>
	
		<div class="form-field">
			<label for="faOrg">Author Organization </label>
			<s:textfield id="faOrg" escapeHtml="true" name="pubEntry.firstAuthor.orgName" cssClass="textfield" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.firstAuthor.orgName" />
		</div>
		
		<div class="form-field">
			<h3 class="underlined">Last Author</h3>
		</div>
	
		<div class="form-field">
			<label for="laFirstName">First Name </label>
			<s:textfield id="laFirstName" escapeHtml="true" name="pubEntry.lastAuthor.firstName" cssClass="textfield" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.lastAuthor.firstName" />
		</div>
		
		<div class="form-field">
			<label for="laMiddleName">Middle Name </label>
			<s:textfield id="laMiddleName" escapeHtml="true" name="pubEntry.lastAuthor.mi" cssClass="textfield" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.lastAuthor.mi" />
		</div>
		
		<div class="form-field">
			<label for="laLastName">Last Name </label>
			<s:textfield id="laLastName" escapeHtml="true" name="pubEntry.lastAuthor.lastName" cssClass="textfield" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.lastAuthor.lastName" />
		</div>
	
		<div class="form-field">
			<label for="laEmail">Author Email </label>
			<s:textfield id="laEmail" escapeHtml="true" name="pubEntry.lastAuthor.email" cssClass="textfield" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.lastAuthor.email" />
		</div>
		
		<div class="form-field">
			<label for="laOrg">Author Organization </label>
			<s:textfield id="laOrg" escapeHtml="true" name="pubEntry.lastAuthor.orgName" cssClass="textfield" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.lastAuthor.orgName" />
		</div>
		<div class="form-field">
			<label for="abstract" class="required">Abstract<span class="required">* </span>
			</label>
			<s:textarea id="abstract" name="pubEntry.description" cols="30" rows="5" cssClass="textfield required"
					escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="pubEntry.description" />
		</div>
		 </div>
		 <div id="softwareDiv">
		
			<div class="form-field">
				<label for="version">Version <span class="required">* </span></label>
				<s:textfield id="version" escapeHtml="true" name="version" cssClass="textfield" escapeJavaScript="true" maxlength="10" />
				<s:fielderror fieldName="version" />
			</div>
		 
		 </div>
		
		
		<div class="form-field" id="descDiv">
			<br />
			<br />
			<label for="description" class="required">Description<span class="required">* </span>
			</label>
			<s:textarea id="supportingDocDescription" name="supportingDocDescription" cols="30" rows="5" cssClass="textfield required"
					escapeHtml="true" escapeJavaScript="true" />
			<s:fielderror fieldName="supportingDocDescription" />
		</div>
		
		<div class="form-field">
			<div class="button">
				<input id="saveFile" type="button" class="submit" value="Save" onclick="uploadDocument()" />
			</div>
			<a class="form-link" href="#" onclick="$.fancybox.close();">Cancel</a>
		</div>
	</s:form>
</div>

<script type="text/javascript">

$('#pubMedWS').click(function(event) {
	//alert("https://www.ncbi.nlm.nih.gov/pmc/utils/oa/oa.fcgi?id=PMC13901");
	
    $.ajax({
        type: "get",
        url: "metaStudyDocAction!pubMedWS.ajax",
        data : {
              "pubMedId" : $("#pubMedId").val()
        },
        success: function(response) {
        	 
        	var obj = JSON.parse(response, function (key, value) {
        		//alert("key"+key+"val"+value);
        		if(key == "title"){
        	    	$("#docTitle").val(value);
        	  	}
        		
        		if(key=="publicationDate"){
        			$("#pubDate").val(value);
        		}
        		
        		if(key=="faFirstName"){
        			$("#faFirstName").val(value);
        		}
        		
				if(key=="faMiddleName"){
					$("#faMiddleName").val(value);
			       }
				
				if(key=="faLastName"){
					$("#faLastName").val(value);
				}
				
				if(key=="laFirstName"){
        			$("#laFirstName").val(value);
        		}
        		
				if(key=="laMiddleName"){
					$("#laMiddleName").val(value);
			       }
				
				if(key=="laLastName"){
					$("#laLastName").val(value);
				}

				if(key=="abstract"){
					$("#abstract").val(value);
				}
  
        	    if (key == "error") {
        	    	 $.ibisMessaging("close", {type:"primary"}); 
        	    	$.ibisMessaging("primary", "error", value,{container: "#messageContainer"});
        	    }
        	    
        	    if(key == "format"){
        	    	 $.ibisMessaging("close", {type:"primary"}); 
        	    	$.ibisMessaging("primary", "error", value,{container: "#messageContainer"});
        	    }
        	 
        	   });
                    
        },
        error: function(e) {
              //most likely empty list
        }
	 });
});

    function testUrl() {
    	var url = $("#url").val();
    	
    	if (!url || url.trim().length == 0) {
    		alert("Warning: URL field is empty!");
    		return;
    	}
    	
    	if (!url.match(/^(f|ht)tps?:\/\//i)) {
            url = 'http://' + url;
        }
    	
    	window.open(url, "_blank");
    	window.focus();
    }
    
    $('select').on('change', function()
    		{
    	var type = $("#supportingDocType").find('option:selected').text();	   
    	if(type==='Publication'){
			$("#pubDiv").show();
			$("#titleDiv").hide();
			$("#descDiv").hide();
		}else if(type==='Software'){
			$("#softwareDiv").show();
			$("#titleDiv").show();
			$("#descDiv").show();
			$("#pubDiv").hide();
		}else{
			$("#pubDiv").hide();
			$("#softwareDiv").hide();
			$("#titleDiv").show();
			$("#descDiv").show();
		}
    });

    function setAddFileDialogMode(mode) {
    	// reset to default
    	$("#pubDiv").hide();
    	$("#softwareDiv").hide();
    	$("#titleDiv").show();
    	$("#descDiv").show();
    	
    	// changes from default for types
    	if(mode==='Publication'){
    		$("#pubDiv").show();
    		$("#titleDiv").hide();
    		$("#descDiv").hide();
    	} else if(mode==='Software'){
    		$("#softwareDiv").show();
    		$("#titleDiv").show();
    		$("#descDiv").show();
    	} 
    }
	
	$(document).ready(function() {
				
		var hasFieldErrors = $("input#hasFieldErrors").val();
		var isEditingDoc = $("input#isEditingDoc").val();
		var fileExistInSession = false;
		
		if(hasFieldErrors == "true"){
			var fileFromSession = '${sessionUploadFile.uploadFile}';
			if(fileFromSession != null){ 
				fileExistInSession="true";
				$("uploadSupportDoc").val(fileFromSession);
			}
		}
		if(isEditingDoc == "true" || fileExistInSession == "true"){
			var input = $("input[name=uploadSupportDoc]");
			var fileName = $("#uploadFileName").val();
		
			convertFileUpload(input, fileName);	
		}
		
		$("#supportingDocDescription").bind("keyup", function() { 
			checkTextareaMaxLength(this.id, 1000); 
		});
		
		
		var type = $("#supportingDocType").find('option:selected').text();
    	if(type==='Publication'){
			$("#pubDiv").show();
			$("#descDiv").hide();
		} else if(type==='Software'){
			$("#softwareDiv").show();
			$("#descDiv").show();
			$("#pubDiv").hide();
		} else{
			$("#pubDiv").hide();
			$("#softwareDiv").hide();
			$("#descDiv").show();
		}
		
    	setAddFileDialogMode(type);

	});
    
</script>
