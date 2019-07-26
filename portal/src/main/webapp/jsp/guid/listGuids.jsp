<title>View All GUIDs</title>
<%@ include file="/common/taglibs.jsp"%>

<div class="clear-float">
	<h1 class="float-left">GUID (Global Unique Identifier)</h1>
</div>

<div class="border-wrapper">
	<jsp:include page="../navigation/guidNavigation.jsp" />
	<div id="main-content">
		<h2>View All GUIDs</h2>
		<p>All GUIDs that have been generated within the system are listed below. GUIDs can be listed in the table more than once as 
		multiple users may receive the same GUID for the same subject. Results are shown in a tabular format to include the following:</p>
		<ul>
			<li><strong>GUID</strong> - Unique identifier associated with a research subject.</li>
			<li><strong>PseudoGUID</strong> - An identifier that is created as a GUID alternative when creation of a GUID is not possible.</li>
			<li><strong>Registered by Organization</strong> - Name of the organization that registered/asked for the GUID</li>
			<li><strong>Registered By</strong> - Name of the user account that registered the GUID</li>
			<li><strong>Date Registered</strong> - Date the GUID was registered</li>
			<li><strong>Linked To</strong> - Indicates the linked GUID or PseudoGUID if a PseudoGUID was converted to a GUID.  If a PseudoGUID has not yet been converted, this field will have a value of "Not yet converted".  This field will be blank if the row is already for a GUID.
		</ul>
		<div class="button margin-left" style="display: none;">
			<input id="pGuid" type="button" class="button" value="GENERATE PSEUDO GUID"  />
		</div>	
	
		<div id="dialog"  style="display: none" >
  			<p>Generate Pseudo GUIDs</p>
  			<div>
  				<div>Enter Number of Pseudo GUIDs:</div>
  				<div>
  					<input type="number" min="1" max="1000"  name="noOfpseduGuid"  id="noOfpseduGuid" />
  				</div>
  			</div>
  			<div>
  				<div>Requested By:</div>
  				<div>
  				<s:select  list="userNameList" name="selectedUser" >
  					
  					</s:select>
  				</div>
  			</div>	
  			<input type="submit" class="button margin-left" value="Generate" id="gGuid"/>
  			<input type="button" class="button margin-left" value="Cancel" id="closeDialog"/>
		</div>
		<div id="promptDialog" style="display: none" >
		
			<p> You are about to generate X Pseduo GUID(s) in the system. The Pseudo GUID(s) can not be generated again.
			Are you sure you want to generate the specified number of Pseudo GUID?
			</p>
		</div>
		
		<script type="text/javascript" src="/portal/js/search/guidSearch.js"></script>
		<jsp:include page="../guid/guidTable.jsp"></jsp:include>
	</div>
</div>

<script type="text/javascript">
    	setNavigation({"bodyClass":"primary",  "navigationLinkID":"guidModuleLink", "subnavigationLinkID":"guidToolLink", "tertiaryLinkID":"listGuidsLink"});
    	
    	$('document').ready(function() {
    		$('#closeDialog').click(function(){
    			$('#dialog').dialog("close");
    		});
    		$('#pGuid').click(function() {
    			$( "#dialog" ).dialog({
    				modal: true,
    				resizable: true,
    				width: 400,
    				height:200
    				
    			});
    		});
    		$('#gGuid').click(function() {
    			$( "#promptDialog" ).dialog({
    				modal: true,
    				resizable: true,
    				width: 400,
    				height:200,
    				buttons: [{
    					text: "Yes,Continue",
    					click: function() {
    						$('#promptDialog').dialog("close");
    						$('#dialog').dialog("close");
    						$.post( "guidAdminAction!list.ajax", { selectedUser: $("#selectedUser").val(), noOfpseduGuid: $("#noOfpseduGuid").val()} );
    					}
    				},
    				{
    					text: "No,Cancel",
    					click: function() {
    						$('#promptDialog').dialog("close");
    						$('#dialog').dialog("close");
    					}
    				}]
    				
    			});
    		});
    	});
    	
    </script>
    
