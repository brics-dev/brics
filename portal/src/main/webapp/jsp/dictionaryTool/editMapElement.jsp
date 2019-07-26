<%@include file="/common/taglibs.jsp"%>
<s:set var="currentMapElement" value="currentMapElement" />
<s:set var="currentDataElement" value="currentDataElement" />
<s:set var="dataType" value="dataType" />
<s:set var="dsId" value="dsId" />
<s:set var="formType" value="formType" />
<s:bean name="gov.nih.tbi.dictionary.model.DataElementForm" var="dataElementForm" />
<title>CRF-Specific Details</title>


<div class="clear-float">
	<h1 class="float-left">Data Dictionary</h1>
</div>
<!-- begin .border-wrapper -->
<div class="border-wrapper">
	<!-- begin #left-sidebar -->
	<jsp:include page="../navigation/dataDictionaryNavigation.jsp" />
	<!--begin #center-content -->
	<div id="main-content" style="margin-top:15px;">
		<jsp:include page="dataElement/editDataElementChevron.jsp" />

		<h2>CRF-Specific Details</h2>
		<p>All form elements MUST go inside the &lt;form&gt; tag in order for the CSS styling to be applied to form
			elements.</p>
		<p>
			In order for the client side validation to work, the form MUST have a class of "validate". <a
				href="/templates/forms-layout.htm">There is another option available for the forms layout.</a> Click the Help link
			on the above to trigger the lightbox for the help content.
		</p>
		<p>
			<strong>Please enter the following information. Fields marked with an asterisk (*) are required.</strong>
		</p>
		<div class="clear-float">
			<s:form id="theForm" action="mapElementAction" method="post">
			<s:token />
				<div class="form-field">
					<label for="mapElementForm.position">Position:</label>
					<s:textfield name="mapElementForm.position" maxlength="100" cssClass="textfield" escapeHtml="true" escapeJavaScript="true" />
					<br />
				</div>

				<div class="form-field">
					<label for="mapElementForm.section">Repeated Element Group:</label>
					<s:textfield name="mapElementForm.section" maxlength="100" label="Section" cssClass="textfield" escapeHtml="true" escapeJavaScript="true" />
					<br />
				</div>

				<div class="form-field">
					<label class="requiredId">Required?:</label>
					<s:select id="requiredId" list="requiredTypes" listValue="value" name="mapElementForm.requiredType"
						value="currentMapElement.requiredType" cssClass="textfield" />
					<br />
				</div>

				<br />
				<div id="aliasDiv"></div>

				<div class="button">
					<input type="button" value="Save & Finish" onClick="saveForm('mapElementAction!submit.action')" />
				</div>
				<a class="form-link" href="javascript:cancel()">Cancel</a>
			</s:form>
		</div>
	</div>
</div>

<script type="text/javascript">
	setNavigation({"bodyClass":"primary", "navigationLinkID":"", "subnavigationLinkID":"dataDictionaryToolLink", "tertiaryLinkID":"listDataStructureLink"});
	var formType = '<s:property value="formType"/>';

	$('document').ready(function() 
			{ 
				displayAlias();
			}
	);
	
	function displayAlias() {
		$.post(	"aliasAction!display.ajax", 
			{ formType:formType }, 
				function (data) { 
					document.getElementById("aliasDiv").innerHTML = data;
				}
		);
	}

	function createAlias() {
		var name = $("#nameField").val();
		
		if( name.length > 0 ) {
			$.post(	"aliasAction!create.ajax", 
					{ name:name }, 
						function (data) { 
							document.getElementById("aliasDiv").innerHTML = data;
						}
				);
		}
	}
	
	function removeAlias( name ) {
		
		$.post(	"aliasAction!remove.ajax", 
				{ name:name }, 
					function (data) { 
						document.getElementById("aliasDiv").innerHTML = data;
					}
			);
	}
</script>