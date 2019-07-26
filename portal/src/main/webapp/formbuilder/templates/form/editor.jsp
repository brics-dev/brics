<%@include file="/common/taglibs.jsp"%>
<script id="editFormTemplate" type="text/x-handlebars-template">
		<form id="dialog_editForm" class="formcreator_dialog">
			<div class="editorErrorContainer"></div>
			<ul>
				<li><a href="#basics">eForm Basics</a></li>
				<li><a href="#advanced">Advanced Settings</a></li>
				<li><a href="#format">eForm Formatting</a></li>
				<li><a href="#section">Section Formatting</a></li>
				<li><a href="#permissions">Permissions</a></li>
			</ul>
			
				<div id="basics" class="tabcontainer">
					<div id="errorContainer" style="display: none"></div>
					<div class="row clearfix">
						<div class="col-md-2">
							<label for="dataStructureName">Form Structure</label>
						</div>
						<div class="col-md-6">
							<div name="dataStructureName">{{dataStructureName}}</div>
						</div>
						<div class="col-md-2">
							<label for="form-name">eForm Status  :</label>
						</div>
						<div class="col-md-2">
							<div>{{status}}</div>
						</div>
					</div>	
					<div class="row clearfix">
						<div class="col-md-2">
							<label for="form-name">eForm Name<span class="requiredStar">*</span></label>
						</div>
						<div class="col-md-2">
							<input type="text" name="name" id="form-name" value="{{name}}">
						</div>
					</div>	
                    <div class="row clearfix">
						<div class="col-md-2">
							<label for="shortName">eForm Short Name<span class="requiredStar">*</span></label>
						</div>
						<div class="col-md-2">
							<input type="text" name="shortName" id="form-shortName" value="{{shortName}}">
						</div>
					</div>
					<div class="row clearfix">
						
						<div class="col-md-2">
							<label for="form-description">eForm Description</label>
						</div>
						<div class="col-md-2">
							<textarea name="description" id="form-description">{{description}}</textarea>
						</div>
						
					</div>
				</div>
				
				<div class="tabcontainer" id="advanced">
                     <div class="row  clearfix">
						<div class="col-md-2">
							<label for="form-copyrightedForm">Allow Multiple Instances</label>
							<br/>
							
						</div>
						<div class="col-md-2">
							<input type="checkbox" class='advanced_chk' name="allowMultipleCollectionInstances" id="form-allowMultipleCollectionInstances" title="Allows multiple collection instances for the same combination of Subject, Form name, Visit type and Visit date" />
						</div>
					</div>
					<div class="row clearfix" id="advanced_cat_msg">
						<div class="col-md-5">
							<div class='ibisMessaging-message ibisMessaging-primary ibisMessaging-error'>
								This function will be disabled when a computer adaptive test (CAT) form structure is selected.
							</div>
						</div>
						<div class="col-md-2"></div>
					</div>	
				</div>
			
				<div class="tabcontainer" id="format">
					<div class="row clearfix">
						<div class="col-md-2">
							<label for="form-header">eForm Header</label>
						</div>
						<div class="col-md-2">
							<textarea name="formHeader" id="form-header"></textarea>
						</div>
						<div class="col-md-2">
							<label for="form-footer">eForm Footer</label>
						</div>
						<div class="col-md-2">
							<textarea name="formFooter" id="form-footer"></textarea>
						</div>
						<div class="col-md-3">
							<label for="form-borders">Borders Around Entire eForm</label>
						</div>
						<div class="col-md-1">
							<input type="checkbox" name="formborder" id="form-borders" />
						</div>
					</div>
					<div class="row clearfix">
						<div class="col-md-2">
							<label for="form-font">eForm Name Font</label>
						</div>
						<div class="col-md-2">
							<select name="formfont" id="form-font">
								<option value="arial">Arial</option>
								<option value="helvetica">Helvetica</option>
								<option value="courier">Courier</option>
								<option value="times">Times</option>
								<option value="avant guard">Avant Guard</option>
								<option value="lucida sans">Lucida Sans</option>
							</select>
						</div>
						<div class="col-md-2">
							<label for="form-fontSize">eForm Name Font Size</label>
						</div>
						<div class="col-md-2">
							<select name="fontSize" id="form-fontSize">
								<option value="8">8</option>
								<option value="9">9</option>
								<option value="10">10</option>
								<option value="11">11</option>
								<option value="12">12</option>
								<option value="13">13</option>
								<option value="14">14</option>
								<option value="15">15</option>
								<option value="16">16</option>
								<option value="17">17</option>
								<option value="18">18</option>
							</select>
						</div>
						<div class="col-md-2">
							<label for="form-formcolor">eForm Name Color</label>
						</div>
						<div class="col-md-2">
							<select name="formcolor" id="form-formcolor">
							<option value="red">Red</option>
							<option value="green">Green</option>
							<option value="yellow">Yellow</option>
							<option value="blue">Blue</option>
							<option value="purple">Purple</option>
							<option value="black">Black</option>
						</select>
						</div>
					</div>
					<div class="row clearfix">
						<div class="col-md-2">
							<label for="form-cellpadding">Element Padding</label>
						</div>
						<div class="col-md-2">
							<select name="cellpadding" id="form-cellpadding">
								<option value="1">1</option>
								<option value="2">2</option>
								<option value="3">3</option>
								<option value="4">4</option>
								<option value="5">5</option>
								<option value="6">6</option>
								<option value="7">7</option>
								<option value="8">8</option>
								<option value="9">9</option>
								<option value="10">10</option>
								<option value="11">11</option>
								<option value="12">12</option>
								<option value="13">13</option>
								<option value="14">14</option>
								<option value="15">15</option>
								<option value="16">16</option>
								<option value="17">17</option>
								<option value="18">18</option>
								<option value="19">19</option>
								<option value="20">20</option>
							</select>
						</div>
					</div>
				</div>
				
				<div class="tabcontainer" id="section">
					<div class="row  clearfix">
						<div class="col-md-3">
							<label for="form-sectionborder">Borders Around Each Section</label>
						</div>
						<div class="col-md-1">
							<input type="checkbox" name="sectionborder" id="form-sectionborder" />
						</div>
						<div class="col-md-2">
							<label for="form-sectionfont">Section Name Font</label>
						</div>
						<div class="col-md-2">
							<select name="sectionfont" id="form-sectionfont">
								<option value="arial">Arial</option>
								<option value="helvetica">Helvetica</option>
								<option value="courier">Courier</option>
								<option value="times">Times New Roman</option>
								<option value="avant guard">Avant Guard</option>
								<option value="lucida sans">Lucida Sans</option>
							</select>
						</div>
						<div class="col-md-2">
							<label for="form-sectioncolor">Section Name Color</label>
						</div>
						<div class="col-md-2">
							<select name="sectioncolor" id="form-sectioncolor">
								<option value="black">Black</option>
								<option value="red">Red</option>
								<option value="green">Green</option>
								<option value="yellow">Yellow</option>
								<option value="blue">Blue</option>
								<option value="purple">Purple</option>
							</select>
						</div>
					</div>
				</div>
				<s:if test="(isDictionaryAdmin || hasAdminPermission) || (formMode == 'create')">
					<div id="permissions" class="tabcontainer">
							<jsp:include page='/formbuilder/templates/form/eFormPermisson.jsp' />
					</div>
				</s:if>
		</form>
	</script>
<!-- 	<script type="text/javascript">
	$('document').ready(function() 
			{ 
				getPermissions();
			}
	);
	
	function getPermissions()
	{
		var params = {entityIdParam: <s:property value='currentEform.id' />, entityTypeParam:"EFORM"};
		$.ajax({
			cache : false,
			url : "eFormPermissionAction!load.ajax",
			data : params,
			success : function (data) {
				$("#permissionDivId").html(data);
			}
		});
	}
</script> -->
	