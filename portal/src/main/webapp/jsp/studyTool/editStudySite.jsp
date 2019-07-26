<%@include file="/common/taglibs.jsp"%>
<%@page contentType="text/html; charset=UTF-8" %>

	<div class="form-field">
		<label for="siteName" class="required">Site Name <span class="required">* </span>:</label>
		<s:textfield id="siteName" name="siteEntry.siteName" cssClass="textfield required" maxlength="100" />
		
		<s:checkbox id="isPrimarySite" name="siteEntry.isPrimary" value="siteEntry.isPrimary" 
				cssStyle="float:left; margin-left:15px; margin-top:3px;" /> 
		<s:label for="isPrimarySite" cssStyle="width:70px; font-weight:normal; float:left;margin-top:1px;" value="Primary Site" />
		<s:fielderror fieldName="siteEntry.siteName" />
		<s:fielderror fieldName="sessionStudy.study.primarySite" />
	</div>
	
	<div class="form-field">
		<label for="address1">Address Line 1 :</label>
		<s:textfield id="address1" name="siteEntry.address.address1" cssClass="textfield" maxlength="255" />
		<s:fielderror fieldName="siteEntry.address.address1" />
	</div>
	
	<div class="form-field">
		<label for="address2">Address Line 2 :</label>
		<s:textfield id="address2" name="siteEntry.address.address2" cssClass="textfield" maxlength="255" />
		<s:fielderror fieldName="siteEntry.address.address2" />
	</div>
	
	<div class="form-field">
		<label for="country">Country :</label>
		<s:select id="countrySel" cssClass="country" list="countryList" listKey="id" listValue="name" name="siteEntry.address.country" 
			value="siteEntry.address.country.id" headerKey="" headerValue="- Select One -" />
	</div>
	
	<div id="stateDiv" class="form-field" style="display:none">
		<label for="state">State :</label>
		<s:select id="stateSel" width="50" list="stateList" listKey="id" listValue="code" name="siteEntry.address.state" 
			value="siteEntry.address.state.id" headerKey="" headerValue="- Select One -" />
		<s:fielderror fieldName="siteEntry.address.state" />
	</div>
	
	<div class="form-field">
		<label for="city">City :</label>
		<s:textfield id="city" name="siteEntry.address.city" cssClass="textfield" maxlength="255" />
		<s:fielderror fieldName="siteEntry.address.city" />
	</div>

	<div class="form-field">
		<label for="phoneNumber">Phone Number :</label>
		<s:textfield id="phoneNumber" name="siteEntry.phoneNumber" cssClass="textfield" maxlength="25" />
		<s:fielderror fieldName="siteEntry.phoneNumber" />
		<div class="special-instruction">Only numbers, parentheses, dashes, x, +, or spaces, e.g. +1-(202) 124-1234x567</div>
	</div>
	
	<div class="form-field inline-right-button">
		<div class="button" style="padding-right:300px">
			<input type="button" id="addSiteBtn" value="Add to Table" onclick="javascript:addStudySite()" />
		</div>
	</div>
	
	<div id="siteTableDiv" class="dataTableContainer dataTableJSON" style="padding-left:50px; padding-bottom:10px; width:800px">
		<h4>Site Table</h4>
		<div id="siteTableDiv" class="idtTableContainer" style="padding-left:50px; padding-top:20px; padding-bottom:10px; width:800px">
		<table id="siteTable" class="table table-striped table-bordered"></table>
	</div>
	<script type="text/javascript">
	$(document).ready(function() {
		$("#siteTable").idtTable({
			idtUrl: "<s:url value='/study/studyAction!getStudySiteSet.action' />",
			idtData: {
				primaryData: "id"
			},
			autoWidth: false,
			pages: 1,
			processing: false,
			serverSide: false,
			length: 15,
			columns: [
				{
					data: "siteName",
					title: "SITE NAME",
					name: "siteName",
					parameter: "siteName"
				},
				{
					data: "address",
					title: "ADDRESS",
					name: "address",
					parameter: "addressLine"
				},
				{
					data: "city",
					title: "CITY",
					name: "city",
					parameter: "address.city"
				},
				{
					data: "state",
					title: "STATE",
					name: "state",
					parameter: "state"
				},
				{
					data: "country",
					title: "COUNTRY",
					name: "country",
					parameter: "country"
				},
				{
					data: "phone",
					title: "PHONE NUMBER",
					name: "phone",
					parameter: "phoneNumber"
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
		if ($('#countrySel').val() == 1) {
			$("#stateDiv").show();
		}
		
		checkPrimarySite();
	});

	$('#countrySel').on('change', function() {
		if (this.value == 1) {   // USA
			$("#stateDiv").show();
		} else {
			$("#stateDiv").hide();
		}
	});

	function checkPrimarySite() {
		var hasPrimarySite = <s:property value="sessionStudy.study.hasPrimarySite" />;
		if (hasPrimarySite == true) {
			$('#isPrimarySite').prop("disabled", true);
		}
	}
	
	function addStudySite() {
		
		var addr1 = $('#address1').val();
		var addr2 = $('#address2').val();
		var city  = $('#city').val();
		
		var params = {
				'siteEntry.siteName': $('#siteName').val(),
				'siteEntry.isPrimary': $('#isPrimarySite').is(':checked'),
				'siteEntry.address.address1': (addr1.length > 50 ? addr1.substring(0,50):addr1),
				'siteEntry.address.address2': (addr2.length > 50 ? addr2.substring(0,50):addr2),
				'siteEntry.address.city': (city.length > 30 ? city.substring(0,30):city),
				'siteEntry.address.country.id': $('#countrySel').val(),
				'siteEntry.address.country.name': $('#countrySel :selected').text(),
				'siteEntry.address.state.id': $('#stateSel').val(),
				'siteEntry.address.state.code': $('#stateSel :selected').text(),
				'siteEntry.phoneNumber': $('#phoneNumber').val()
		};
		
		$.ajax({
			type: "POST",
			url: "studySiteValidationAction!addStudySite.ajax",
			data: params,
			"async": true,
			success: function(data) {
				$('#studySite').html(data);
				buildDataTables();
				checkPrimarySite();
			}
		});
	}
	
	function removeStudySite(siteJson) {
		$.ajax({
			type: "POST",
			url: "studyAction!removeStudySite.ajax",
			data: {"siteJson": JSON.stringify(siteJson)},
			"async": true,
			success: function(data) {
				$('#studySite').html(data);
				buildDataTables();
				checkPrimarySite();
			}
		});
	}
	
</script>	