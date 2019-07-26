<%@include file="/common/taglibs.jsp"%>

	<h3 style="padding-left:10px;">Clinical Trial Information</h3>
	<hr class="underline" style="margin:10px 0;">
	<h4 style="padding-left:30px">FDA Clinical Trial Table</h4>
	
	<div class="form-field">
		<label for="fdaInd">FDA IND/IDE :</label>
		<s:textfield id="fdaInd" name="sponsorInfoEntry.fdaInd" cssClass="textfield required" maxlength="55" />
		<s:fielderror fieldName="sponsorInfoEntry.fdaInd" />
	</div>

	<div class="form-field">
		<label for="sponsor">Sponsor :</label>
		<s:textfield id="sponsor" name="sponsorInfoEntry.sponsor" cssClass="textfield" maxlength="255" />
		<s:fielderror fieldName="sponsorInfoEntry.sponsor" cssStyle="float:left" />
		
		<div class="inline-right-button button" style="padding-right:300px">
			<input type="button" id="addSponsorButton" value="Add to Table" onclick="javascript:addSponsorInfo()" />
		</div>
	</div>
	
	<div id="sponsorInfoTblDiv" class="idtTableContainer" style="padding-left:50px; padding-bottom:10px; width:800px">
		<table id="sponsorInfoTable" class="table table-striped table-bordered"></table>
	</div>
	<script type="text/javascript">
	$(document).ready(function() {
		$("#sponsorInfoTable").idtTable({
			idtUrl: "<s:url value='/study/studyAction!getSponsorInfoSet.action' />",
			idtData: {
				primaryKey: "id"
			},
			autoWidth: false,
			pages: 1,
			processing: false,
			serverSide: false,
			length: 15,
			select: false,
			columns: [
				{
					data: "fdaInd",
					title: "FDA IND/IDE",
					name: "fdaInd",
					parameter: "fdaInd"
				},
				{
					data: "sponsor",
					title: "SPONSOR",
					name: "sponsor",
					parameter: "sponsor"
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
