<%@include file="/common/taglibs.jsp"%>
<div id="statusChange" style="display:none">	
			<div id="statusChangeInner"></div>
			<div class="ui-clearfix">
		
	 <s:if test="%{isDocumentLimitNR}"> 
		<div id="addDocBtnDiv" class="button button-right btn-downarrow">
				<input id="addDocBtn" type="button" value="Add Documentation" />
				<div id="selectAddDocDiv" class="btn-downarrow-options">
					<p>
						<a class="bricsDialog"
							href="${actionName}!addDocDialog.ajax?addDocSelect=file">File</a>
					</p>
				</div>
		</div>
	 </s:if> 
	 <s:else> 
		<div id="addDocBtnDiv" class="button button-right disabled btn-downarrow " title="The maximum amount of documents allowed to be uploaded is ${documentationLimit}.">
				<input id="addDocBtn"  type="button" value="Add Documentation" disabled/>
				<div id="selectAddDocDiv" class="hidden btn-downarrow-options">
					<p>
						<a class="bricsDialog"
							href="${actionName}!addDocDialog.ajax?addDocSelect=file">File</a>
					</p>
				</div>
		</div>
	</s:else> 
</div>
<br />
<input type="hidden" name="openDialog" id="openDialog" value="true"/>		    
<jsp:include page="supportDocList.jsp"></jsp:include>
<br />
<s:hidden id="documentationLimit" name="documentationLimit" />
<s:hidden id="actionName" name="actionName" />
</div>

<div id="dataElementsDialogue" style="display:none">
    <br> <br>    
	<div id="dataElementListContainer" class="idtTableContainer" style="float: center;">
		<table id="dataElementListTable" class="table table-striped table-bordered" width="100%"></table>
	</div>
</div>

<script type="text/javascript" src="/portal/js/uploadDocumentations.js"></script>
<script type="text/javascript">
   $(document).ready(function() {
	  $("a.bricsDialog").bricsDialog();
   });

	$("#addDocBtn").click( function(e) {
		$("#selectAddDocDiv").toggle();
		 
	});
		
</script>
