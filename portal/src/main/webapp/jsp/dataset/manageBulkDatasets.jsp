<%@include file="/common/taglibs.jsp"%>

<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1>Data Repository</h1>
	<div style="clear:both"></div>

	<!--begin #center-content -->
	<div id="main-content">
			<h2>Manage Datasets</h2>
			<h3>Status: <s:if test="%{isBulkRequest}">Requested ${requestedDatasetStatus.verb}</s:if>
				<s:else>${currentDatasetStatus.name}</s:else></h3>
			<h4>Datasets: </h4>	
        <s:if test="%{isDatasetRequestDelete}"> 
       		 <jsp:include page="bulkDeleteDatasetListTable.jsp"></jsp:include>
        </s:if>
        <s:else>
             <jsp:include page="bulkDatasetListTable.jsp"></jsp:include>
        </s:else>
        
        <s:form id="formId" onsubmit="getAction()" method="post" validate="true"> 
			<s:if test="%{isBulkRequest}">
	
				<p>
					The selected dataset(s) is(are) currently set to ${currentDatasetStatus.name}.
					Approving the request will change the status to <span
						class="required"> ${requestedDatasetStatus.name} </span>. Canceling will
					return the dataset(s) to ${currentDatasetStatus.name}.Please provide a
					detailed approval or rejection reason. This reason will be sent to
					the dataset submitter in an email.
				</p>
				
				<s:if test="%{isDatasetRequestDelete}">
					<p>
					<strong>Note:</strong>If you are attempting to delete dataset(s) with access records, 
					the deletion will not be completed for those datasets.
					To delete these datasets, please remove associated access records by contacting the system administrator.
					</p>
			    </s:if>
	
				<div class="form-field">
					<label for="reason" class="required">Approval/Rejection
						Reason: <span class="required">* </span>
					</label>
					<s:textarea label="reason" cols="60" rows="4"
						cssClass="textfield required" name="statusReason" escapeHtml="true"
						escapeJavaScript="true" id="statusReason" />				     
				 </div>
	
	            <div>
					<p>Please provide a reason for making the change to the
						datasets(s) to be stored in the Admin Status Change History. The
						reason will be associated with each dataset shown above. You may
						also upload any relevant documentation (not required).</p>
				 </div>
	
			</s:if>
			
			<s:else>
				<p>The selected dataset (s) is(are) currently set to ${currentDatasetStatus.name}. Approving the request
					will change the status to <span class="required"> ${newStatus.name}</span>. Provide a detailed reason as to why
					the status of the dataset is being changed. </p>
					
					<s:if test="%{isDatasetRequestDelete}">
						<p>
						<strong>Note:</strong>If you are attempting to delete dataset(s) with access records, 
						the deletion will not be completed for those datasets.
						To delete these datasets, please remove associated access records by contacting the system administrator.
						</p>
			        </s:if>
			</s:else>
			<div class="form-field">
				<label for="comment" class="required">Admin Status Change
					Reason: <span class="required">* </span>
				</label>
				<s:textarea label="comment" cols="60" rows="4"
					cssClass="textfield required" name="statusChangeComment" id="statusChangeComment"
					escapeHtml="true" escapeJavaScript="true" />
	
			</div>
			
			<jsp:include page="bulkDatasetDocumentation.jsp"></jsp:include>
			
	         <div class="float-right">
				 	<div class="button margin-left disabled" id="buttonDiv">
					  <input name="datasetStatusSelect" disabled = "disabled" id="datasetStatusSelect" type="submit" value="${datasetStatusSelect}" title="Provide Required Field(s)" />
				    </div>				 
				 <a class= "form-link" href="datasetAction!list.action">Cancel</a>
		     </div>	 	
      </s:form> 

	  </div>
</div>
<script type="text/javascript">
	setNavigation({
		"bodyClass" : "primary",
		"navigationLinkID" : "dataRepositoryModuleLink",
		"subnavigationLinkID" : "contributeDataToolsLink",
		"tertiaryLinkID" : "datasetListLink"
	});

	function getAction(){
		
		var selectedOption = $('#datasetStatusSelect').val();
		
	    if(selectedOption=="Share" || selectedOption=="Archive" || selectedOption=="Delete")
	    	$('#formId').attr('action', 'bulkDatasetValidationAction!bulkDatasetLists.action');
	    else if(selectedOption=="Approve Request")
	    	$('#formId').attr('action', 'bulkDatasetValidationAction!approveBulkDatasetStatus.action');
	    else
	    	$('#formId').attr('action', 'bulkDatasetValidationAction!rejectBulkDatasetStatus.action');
	    	
	}	
 	//check the required fields are provided or not
	if($("#statusChangeComment,statusReason").length != 0){
        $("#statusChangeComment,statusReason").keyup(function(){
            if($(this).val() == "" || $(this).val().length>4000) {        
                $("#buttonDiv").addClass("disabled");
                $("#datasetStatusSelect").addClass("disabled");         
                $("#datasetStatusSelect").prop("disabled",true);
                $('#datasetStatusSelect').attr('title', 'Provide Required Field(s)');
                
            } else {
                $("#buttonDiv").removeClass("disabled");
                $("#datasetStatusSelect").removeClass("disabled");            
                $("#datasetStatusSelect").prop("disabled",false); 
                $("#datasetStatusSelect").removeAttr( 'title' );
                
            }
        });
    };
    
    
 	
</script>

