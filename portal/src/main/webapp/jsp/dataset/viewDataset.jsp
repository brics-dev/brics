<%@include file="/common/taglibs.jsp"%>
<s:if test="%{isRequest}"><title>Dataset Request: ${currentDataset.name}</title></s:if>
<s:else><title>Dataset: ${currentDataset.name}</title></s:else>

<!-- begin .border-wrapper -->
<div class="border-wrapper wide">
	<jsp:include page="../navigation/dataRepositoryNavigation.jsp" />
	<h1 class="float-left">Data Repository</h1>
	<div style="clear:both"></div>

	<!--begin #center-content -->
	<div id="main-content">
		<s:if test="inAdmin">
			<div id="breadcrumb">
				<a href="/portal/studyAdmin/datasetAction!list.action">Manage Datasets</a>&nbsp;>&nbsp;Dataset:&nbsp;${currentDataset.name}
			</div>
		</s:if>
		<s:set var="rc" value="accessRecordCount" />
		<!-- <s:set var="cantDelete" value="#rc > 0 && inAdmin" /> -->
		<s:set var="cantApprove" value="#rc > 0 && currentDataset.datasetRequestStatus.id == 3" />
		<div class="clear-both clear-float">
			<h2><s:if test="%{isRequest}">Dataset Request: ${currentDataset.name}</s:if>
				<s:else>Dataset: ${currentDataset.name}</s:else></h2>
				
			<s:if test="inAdmin && !isNoChangeStatusOptions && !isRequest">
				<h3 id="adminFunLabel" class="clear-both collapsable">
					<span id="adminFunPlusMinus"></span>&nbsp;Administrative Functionality
				</h3>
				
				<div id="adminFun">
				
				<s:if test="uploading || errorLoading">
				
				<s:form id="theForm" cssClass="validate no-margin" method="post" validate="true">
					
					<p>Permanently delete a dataset from the system. Deleting a dataset cannot be undone.</p>
	                    <div class="form-field">
	                        <label for="comment" class="required">Admin Status Change Reason: <span class="required">* </span>
	                        </label>
	                        <s:textarea label="comment" cols="60" rows="4" cssClass="textfield required" name="deletionComment" id="deletionComment" escapeHtml="true" escapeJavaScript="true"/>
						    <s:fielderror fieldName="deletionComment" />
	
						</div>
	                    <div class="button right-margin disabled" id="deleteButtonDiv">
	                        <input disabled="disabled" type="button" onclick="javascript:confirmDelete()" value="Delete" id="deleteDatasetButton" />
	                    </div>
						<s:if test="#cantDelete">
							<span class="error-message">
								<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
								<span class="error-text">This dataset can not be deleted until it's access records have been removed.</span>
							</span>
						</s:if>
				
				<s:if test="errorLoading">
						<br /><br /><br />
						<p>The data loader has failed to load the data. You can attempt to reload the data into the dataset.</p>

						<div class="button">
							<input type="button" onclick="javascript:reloadData()" value="Reload Data" />
						</div>
				</s:if>
				
				</s:form>
				</s:if>
				
				<s:else>
					<form action="datasetAction!manageDataset.action">
					<p>Change the status of the dataset:</p>
		
						<div>
							<select id="datasetStatusSelect" name="datasetStatusSelect" class="medium"></select> 
							
							<div class="button" id="buttonDiv" >
								<input id="accept" type="submit" value="Continue" class="button margin-left" /> 
							</div>
						</div>
					
					</form>
					<s:hidden id="currentDatasetStatusView" name="currentDatasetStatusView" />
					
					<br /><br /><br /><br /><br /><br />
				</s:else>						
				</div>
							
			</s:if>	
				
			<s:if test="inAdmin && isRequest">
				<s:form id="theForm" cssClass="validate no-margin" method="post" validate="true">
				<s:token />
					<div class="form-output">
						<div class="label">Status:</div>
						<div class="readonly-text no-padding">
							Request to
							<s:property value="currentDataset.datasetRequestStatus.verb" />
						</div>
					</div>

					<div class="form-field">
						<label for="reason" class="required">Approval/Rejection Reason: <span class="required">* </span>
						</label>
						<s:textarea label="reason" cols="60" rows="4" cssClass="textfield required" name="statusReason" id="statusReason" escapeHtml="true" escapeJavaScript="true" />
						<div class="special-instruction">An email will be sent to the user with the above message included in the
							body.</div>
						<s:fielderror fieldName="statusReason" />

						<s:if test="#cantApprove">
							<span class="error-message">
								<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
								<span class="error-text">This dataset can not be deleted until it's access records have been removed.</span>
							</span>
						</s:if>
					</div>
					
					<div>
						<p>
							This dataset is currently set to
							${currentDataset.datasetStatus.name}. Approving the request will
							change the status to <span class="required">${currentDataset.datasetRequestStatus.name}</span>.
							Rejecting the request will return the dataset to
							${currentDataset.datasetStatus.name}.
						</p>
						
					</div>

					

					<div class="form-field">
						<label for="comment" class="required">Admin Status Change
							Reason: <span class="required">* </span>
						</label>
						<s:textarea label="comment" cols="60" rows="4"
							cssClass="textfield required" name="statusChangeComment"
							id="statusChangeComment" escapeHtml="true"
							escapeJavaScript="true" />

					</div>
					
					<div>
					<p>Please provide a reason for making the change to the
							dataset to be stored in the Admin Status Change History.You may
							also upload any relevant documentation (not required).</p>
					</div>
					
					<jsp:include page="bulkDatasetDocumentation.jsp"></jsp:include>

					<div class="float-right" >
						<div id="buttonDiv" class="button right-margin disabled <s:if test="#cantApprove">disabled</s:if>">
							<input <s:if test="#cantApprove" >disabled="disabled"</s:if> id="approve" title="Provide Required Field(s)" disabled="disabled"  type="button" onclick="javascript:submitForm('datasetStatusValidationAction!approve.action')"
								value="Approve Request" />
						</div>
						<div class="button disabled" id="buttonDivTwo">
							<input id="reject" disabled="disabled" type="button" title="Provide Required Field(s)" onclick="javascript:submitForm('datasetStatusValidationAction!reject.action')"
								value="Reject Request" />
						</div>
					</div>
				</s:form>
			</s:if>
						
			<h3 class="clear-both">Dataset Overview:</h3>
			<div class="form-output">
				<div class="label">Status:</div>
				<div class="readonly-text">
					<s:property value="currentDataset.datasetStatus.name" />
				</div>
			</div>
			<s:if test="inAdmin" >
				<div class="form-output">
					<div class="label">Access Records:</div>
					<div class="readonly-text">
						<s:property value="#rc" />
					</div>
				</div>

				<s:if test="isCurrentDatasetDeleted" >
				    <div class="form-output">
                        <div class="label">Deleted by:</div>
                        <div class="readonly-text">
                            <s:property value="currentDatasetLatestEvent.user.fullName" />
                        </div>
                    </div>
                    <div class="form-output">
                        <div class="label">Admin Status Change Reason: </div>
                        <div class="readonly-text">
                            <s:property value="currentDatasetLatestEvent.comment" />
                        </div>
                    </div>
                    <div class="form-output">
                    	<div class="label">Deletion Time:</div>
                    	<div class="readonly-text">
                    		<ndar:dateTag value="${currentDatasetLatestEvent.createTime}" format="long"/>
                    	</div>
                    </div>
				</s:if>
			</s:if>

			<jsp:include page="viewDatasetBasic.jsp" />
			
			<c:if test="${fn:length(eventLogList) != 0}">
			
				<h3>Dataset Administrative Status Change History</h3>						
				<ndar:eventTable eventLogList="${eventLogList}" /> 
				
			</c:if>

			<s:if test="currentDataset.study != null">
				<h3>Study Overview</h3>
				<jsp:include page="../studyTool/viewStudyInterface.jsp" />
			</s:if> 
		</div>
	</div>
</div>

<script type="text/javascript">
	
	
	<s:if test="inAdmin" >
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"contributeDataToolsLink", "tertiaryLinkID":"datasetListLink"});
	</s:if>
	<s:else>
		setNavigation({"bodyClass":"primary", "navigationLinkID":"dataRepositoryModuleLink", "subnavigationLinkID":"workspaceLink"});
	</s:else>
	
	$('document').ready(function() 
		{ 
			<s:if test="%{inAdmin}">
				adminFunInit();
				$("#adminFun").hide();
				$("#adminFunLabel").addClass("no-border");
				$("#adminFunPlusMinus").text("+");



			</s:if>

            <s:if test="%{canDelete}">

            if($("#deletionComment").length != 0){
                $("#deletionComment").keyup(function(){
                    if($(this).val() == "") {
                        $("#deleteDatasetButton").prop("disabled", true);
                        $("#deleteButtonDiv").addClass("disabled");

                    } else {
                        $("#deleteDatasetButton").prop("disabled", false);
                        $("#deleteButtonDiv").removeClass("disabled");
                    }
                });
            }

            </s:if>
            
            if($("#currentDatasetStatusView").val()=="Private"){
            	$('#datasetStatusSelect').empty().prepend('<option selected value="Share">Share</option><option selected value="Archive">Archive</option><option selected value="Delete">Delete</option>');
    			$("#datasetStatusSelect option:first-child").attr("selected","selected");
            }
            else if($("#currentDatasetStatusView").val()=="Shared"){
            	$('#datasetStatusSelect').empty().append('<option selected value="Archive">Archive</option>');
            }
		}
	);
	
	function reloadData() {
		$.post(	"datasetAction!reloadData.ajax", 
			{ }, 
			function (data) {
			}
		);
	}
	
	function viewClinicalTrial(clinicalTrialId) {
		$.post(	"studyAction!viewClinicalTrial.ajax", 
			{ clinicalTrialId:clinicalTrialId }, 
			function (data) {
				$.fancybox(data);
			}
		);
	}
	
	function confirmDelete() {
		var where_to = confirm("Are you sure you want to delete this dataset?  Once deleted, a dataset can not be recovered.");
		if (where_to == true) {
			submitForm('datasetDeletionValidationAction!delete.action?prefixedId=${currentDataset.prefixedId}');
		}
	}
	
	function adminFunInit() {
		$("#adminFunLabel").click(function(){
			$("#adminFun").slideToggle("fast");
			$("#adminFunLabel").toggleClass("no-border");
			if($("#adminFunPlusMinus").text()=="+") {
				$("#adminFunPlusMinus").text("- ");
			} else {
				$("#adminFunPlusMinus").text("+");
			}
		});
	}
	function ellipsisExpandCollapse(element) {
		var $this = $(element);
		$this.parent().toggle();
		if ($this.text() == "...") {
			$this.parent().next().toggle();
		}
		else {
			$this.parent().prev().toggle();
		}
	}
	if($("#statusChangeComment,statusReason").length != 0){
        $("#statusChangeComment,statusReason").keyup(function(){
            if($(this).val() == "" || $(this).val().length>4000) {        
                $("#buttonDiv").addClass("disabled");
                $("#buttonDivTwo").addClass("disabled");
                $("#approve").addClass("disabled");         
                $("#approve").prop("disabled",true);
                $('#approve').attr('title', 'Provide Required Field(s)');
                
                $('#reject').addClass("disabled");         
                $('#reject').prop("disabled",true);
                $('#reject').attr('title', 'Provide Required Field(s)');
                
            } else {
                $('#buttonDiv').removeClass("disabled");
                $('#buttonDivTwo').removeClass("disabled");
                $("#approve").removeClass("disabled");            
                $("#approve").prop("disabled",false); 
                $("#approve").removeAttr( 'title' );
                
                $("#reject").removeClass("disabled");            
                $("#reject").prop("disabled",false); 
                $("#reject").removeAttr( 'title' );
                
            }
        });
    };
</script>