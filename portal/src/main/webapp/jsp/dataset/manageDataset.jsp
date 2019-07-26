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
		
		<div class="clear-both clear-float">
			
		<s:form id="theForm"  method="post" validate="true"> 
				<h2 class="pageHeader">Dataset: ${currentDataset.name}</h2>
				
				<br />
				<h2>Administrative Functionality: ${datasetStatusSelect}</h2>
				<br />
				
				<div>
					<p ><strong>Current Status:</strong> ${currentDataset.datasetStatus.name} <br />
	                 You are about to perform the administrative function of permanently changing the status to <span class="required"> ${datasetSelectStatusName}</span>.</p>
	                 
	                <p>Please provide a detailed reason for performing the administrative action described above. You may also upload relevant documentation.</p>
                
                </div>
                
                <div class="form-field">
				<label for="reason" class="required">Reason for admin Status Change: <span class="required">* </span>
				</label>
				<s:textarea for="reason" cols="60" rows="4"
					cssClass="textfield required" name="statusReason" id="statusReason"
					escapeHtml="true" escapeJavaScript="true" />
				<s:fielderror fieldName="statusReason" />
				
				<s:if test="cantDelete">
							<span class="error-message">
								<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
								<span class="error-text">This dataset can not be deleted until it's access records have been removed.</span>
							</span>
				</s:if>
	
				</div>
				<jsp:include page="bulkDatasetDocumentation.jsp"></jsp:include>
				
				 <div class="float-right">
				    <s:if test="cantDelete" >
					    <div class="button margin-left disabled">
						  <input disabled="disabled" type="submit" value="${datasetStatusSelect}" title="Provide Required Field(s)" onclick="javascript:submitForm('datasetStatusValidationAction!changeStatus.action')"/>
					    </div>	
				    </s:if>
				    <s:else>
				    	<div class="button margin-left disabled" id="buttonDiv">
					       <input name="datasetStatusSelect" id="datasetStatusSelect" disabled="disabled" type="submit" value="${datasetStatusSelect}" title="Provide Required Field(s)" onclick="javascript:submitForm('datasetStatusValidationAction!changeStatus.action')"/>
				       </div>
				    </s:else>
				 		
				    <div class="button margin-left">
					  <input name="cancel" id="cancel" type="submit" value="Cancel" onclick="javascript:submitForm('datasetAction!view.action?prefixedId=${currentDataset.prefixedId}')"/>
				    </div>
				    		 
		        </div>
		
			</s:form>		
						
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

			<h3>Study Overview</h3>

			<jsp:include page="../studyTool/viewStudyInterface.jsp" />
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
			//window.location = "/portal/studyAdmin/datasetAction!delete.action?prefixedId=${currentDataset.prefixedId}";
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
	
	//check the required fields are provided or not
	if($("#statusReason").length != 0){
        $("#statusReason").keyup(function(){
            if($(this).val() == "" || $(this).val().length>4000) {        
                $("#buttonDiv").addClass("disabled");
                $("#datasetStatusSelect").addClass("disabled");         
                $("#datasetStatusSelect").prop("disabled",true);   
                $('#datasetStatusSelect').attr('title', 'Provide Required Field(s)');
            } else {
                $("#buttonDiv").removeClass("disabled");
                $("#datasetStatusSelect").removeClass("disabled");            
                $("#datasetStatusSelect").prop("disabled",false);    
                $("#datasetStatusSelect").removeAttr( 'title');
            }
        });
    };
    
    
</script>