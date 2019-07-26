<%@include file="/common/taglibs.jsp"%>

<div id="grantInfo" style="padding-top:20px;">
	<h4 style="padding-left:50px">Grant ID Table</h4>
	<div class="form-field">
		<label for="grantId">Grant/Project ID :</label>
		<s:textfield id="grantId" name="grantEntry.grantId" cssClass="textfield" maxlength="25" />
		<div class="special-instruction">Example: DR080205 or 1ZIABC010277-13</div>
		<s:fielderror fieldName="grantEntry.grantId" />
	</div>
	
	<div id="grantOptional" style="display:none">
		<div class="form-field">
			<label for="grantName" class="required">Grant/Project Name <span class="required">* </span>:</label>
			<s:textfield id="grantName" name="grantEntry.grantName" cssClass="textfield" maxlength="255" />
			<s:fielderror fieldName="grantEntry.grantName" />
		</div>
		
		<div class="form-field">
			<label for="grantFunders" class="required">Grant/Project Funders Name <span class="required">* </span>:</label>
			<s:textfield id="grantFunders" name="grantEntry.grantFunders" cssClass="textfield" maxlength="255" />
			<s:fielderror fieldName="grantEntry.grantFunders" />
			
			<div class="inline-right-button button" style="padding-right:300px">
				<input type="button" id="grantButton" value="Add to Table" onclick="javascript:addGrant()" />
			</div>
		</div>
	</div>
	<div id="grantTableDiv" class="idtTableContainer" style="padding-left:50px; padding-bottom:10px; width:800px">
		<table id="grantTable" class="table table-striped table-bordered"></table>
	</div>
	<script type="text/javascript">
	$(document).ready(function() {
		$("#grantTable").idtTable({
			idtUrl: "<s:url value='/study/studyAction!getGrantSet.action' />",
			autoWidth: false,
			columns: [
				{
					data: "grantId",
					title: "GRANT ID",
					name: "grantId",
					parameter: "grantId"
				},
				{
					data: "actions",
					title: "ACTIONS",
					name: "actions",
					parameter: "removeLink"
				}
			]
		});
	});
	</script>
</div>

<script type="text/javascript">

	$('document').ready( function() { 
		var grantId = $('#grantId').val();
		if (grantId) {
			$('#grantOptional').show();
		}
	});
	
	$('#grantId').on('input', function(e) {
		var grantId = $('#grantId').val();
		if (grantId) {
			$('#grantOptional').show();
		}
	});
   
	function viewGrantInfo(grantId) {
		
	    var f = $("<form target='_blank' method='POST' style='display:none;'></form>").attr({
	        action: "<s:property value='@gov.nih.tbi.PortalConstants@FEDERAL_REPORTER_SEARCH_URL' />"
	    }).appendTo(document.body);

	    $('<input type="hidden" />').attr({
            name: "projectNumbers", value: grantId
        }).appendTo(f);
	    
	    $('<input type="hidden" />').attr({
            name: "projectNumbersRaw", value: grantId
        }).appendTo(f);
	    
	    $('<input type="hidden" />').attr({
            name: "searchMode", value: "Smart"
        }).appendTo(f);
	    
	    f.submit();
	    f.remove();
	}
	
	function addGrant() {
		var params = {
			"grantEntry.grantId": $('#grantId').val(),
			"grantEntry.grantName": $('#grantName').val(),
			"grantEntry.grantFunders": $('#grantFunders').val()
		};

		$.ajax({
			type: "POST",
			url: "grantValidationAction!addGrant.ajax",
			data: params,
			"async": true,
			success: function(data) {
				$('#grantInfo').html(data);
				buildDataTables();
			}
		});
	}
	
	function removeGrant(grantJson) {
		$.ajax({
			type: "POST",
			url: "studyAction!removeGrant.ajax",
			data: "grantJson=" + JSON.stringify(grantJson),
			"async": true,
			success: function(data) {
				$('#grantInfo').html(data);
				buildDataTables();
			}
		});
	}
	
</script>

