<%@taglib prefix="s" uri="/struts-tags"%>
<%@taglib prefix="ndar" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<div style="float:left; position:relative; margin-left:17px;">
		<div id="hamburger">
        	<div></div>
        	<div></div>
        	<div></div>
        	<b>Menu</b>
		</div>
		<div id="hamburgerMenu" style="display:none; position:absolute; top:46px; left:0px; z-index:100;">
<div id="left-sidebar">
	<ul class="subnavigation">
		<s:if test='modulesDDTURL != ""'>
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
				<li id='dataDictionaryToolLink'><s:a href="%{modulesDDTURL}dictionary/listDataStructureAction!list.action">Data Dictionary Tool</s:a>
					<ul class="tertiary-links" style="display: none">
						<li style="font-weight: bold; font-size: 110%;">
							<s:a href="%{modulesDDTURL}dictionary/listDataStructureAction!list.action">Form Structures</s:a>
						</li>
						<li><ul class="tertiary-links-inner">
							<li id='listDataStructureLink' name="nav_search_form_structures" class="long-text">
								<s:a href="%{modulesDDTURL}dictionary/listDataStructureAction!list.action">Search</s:a>
							</li>
							<li id='dataStructureLink' name="nav_create_form_structures" class="long-text">
								<s:a href="%{modulesDDTURL}dictionary/dataStructureAction!create.action">Create</s:a>
							</li>
						</ul></li>
						<li style="font-weight: bold; font-size: 110%;">
							<s:a href="%{modulesDDTURL}dictionary/searchDataElementAction!list.action">Data Elements</s:a>
						</li>
						<li><ul class="tertiary-links-inner">
							<li id='searchDataElementLink' name="nav_search_data_elements" class="long-text">
								<s:a href="%{modulesDDTURL}dictionary/searchDataElementAction!list.action">Search</s:a>
							</li>
							<li id='dataElementLink' name="nav_create_date_elements" class="long-text">
								<s:a href="%{modulesDDTURL}dictionary/dataElementAction!create.action">Create</s:a>
							</li>
							<li id='userImportDataElementLink' name="nav_import_data_elements" class="long-text">
								<s:a href="%{modulesDDTURL}dictionary/importDataElementAction!userImport.action">Import</s:a>
							</li>
						</ul></li>
						<!-- Only users with eform permission can view these options  -->
 						<sec:authorize access="hasAnyRole('ROLE_DICTIONARY_EFORM', 'ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN')">
 						<li style="font-weight: bold; font-size: 110%;">
 							<s:a href="%{modulesDDTURL}dictionary/eFormSearchAction!list.action">eForms</s:a>
 						</li>
 						<li><ul class="tertiary-links-inner">
							<li id='searchEformLink' name="nav_search_eforms" class="long-text">
								<s:a href="%{modulesDDTURL}dictionary/eFormSearchAction!list.action">Search</s:a>
							</li>
							<li id='createEformLink' name="nav_create_eforms" class="long-text">
								<s:a href="%{modulesDDTURL}dictionary/eFormAction!createEform.action">Create</s:a>
							</li>
						</ul></li>
						</sec:authorize>
					</ul></li>
			</sec:authorize>
			<sec:authorize access="!hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN', 'ROLE_DICTIONARY')">
				<li id='dataDictionaryToolLink'>
					<div class="missingPermission">Data Dictionary Tool</div>
				</li>
			</sec:authorize>
		</s:if>
		
		<s:if test='modulesDDTURL != ""'>
			<sec:authorize access="hasAnyRole('ROLE_ADMIN', 'ROLE_DICTIONARY_ADMIN')">
				<li id="defineDataToolsLink" class="long-text"><s:a href="%{modulesDDTURL}dictionaryAdmin/searchDataElementAction!list.action">Data Dictionary Administration</s:a>
					<ul class="tertiary-links" style="display: none">
						<li style="font-weight: bold; font-size: 110%;">
							<s:a href="%{modulesDDTURL}dictionaryAdmin/listDataStructureAction!list.action">Form Structures</s:a>
						</li>
						<li><ul class="tertiary-links-inner">
							<li id='manageDataStructuresLink' name="nav_manage_form_structures" class="long-text">
								<s:a href="%{modulesDDTURL}dictionaryAdmin/listDataStructureAction!list.action">Manage</s:a>
							</li>
							<li id='importFormStructureLink' name="nav_import_form_structures" class="long-text">
								<s:a href="%{modulesDDTURL}dictionaryAdmin/importDataElementAction!adminFormStructureImport.action">Import</s:a>
							</li>						
						</ul></li>
						<li style="font-weight: bold; font-size: 110%;">
							<s:a href="%{modulesDDTURL}dictionaryAdmin/searchDataElementAction!list.action">Data Elements</s:a>
						</li>
						<li><ul class="tertiary-links-inner">
							<li id='manageDataElementsLink' name="nav_manage_data_elements" class="long-text">
								<s:a href="%{modulesDDTURL}dictionaryAdmin/searchDataElementAction!list.action">Manage</s:a>
							</li>
							<li id='importDataElementSchemaLink' name="nav_import_data_elements_schema" class="long-text">
								<s:a href="%{modulesDDTURL}dictionaryAdmin/schemaMappingAction!adminDataElementSchemaImport.action">Import Schema</s:a>
							</li>
							<!-- <li id='bulkDeleteDataElementsLink' name="nav_bulk_delete_data_elements" class="long-text">
								<s:a href="%{modulesDDTURL}dictionaryAdmin/deleteDataElementAction!delete.action">Delete</s:a>
							</li> -->
						</ul></li>
						<li style="font-weight: bold; font-size: 110%;">
							<s:a href="%{modulesDDTURL}dictionaryAdmin/eFormSearchAction!list.action">eForms</s:a>
						</li>
 						<li><ul class="tertiary-links-inner">
 							<li id='manageEformLink' name="nav_manage_eforms" class="long-text">
 								<s:a href="%{modulesDDTURL}dictionaryAdmin/eFormSearchAction!list.action">Manage</s:a>
 							</li>
 							<li id='importEformLink' name="nav_import_eforms" class="long-text">
 								<s:a href="%{modulesDDTURL}dictionaryAdmin/eformImportAction!importEformView.action">Import</s:a>
 							</li>
 						</ul></li>
						<!-- added by Ching-Heng -->
 						<li style="font-weight: bold; font-size: 110%;">
							<s:a href="%{modulesDDTURL}dictionaryAdmin/promisDataStructureAction!adminListNonexistentPromisForms.action">HealthMeasures Forms</s:a>
						</li>
 						<li><ul class="tertiary-links-inner"> 						
							<li id='createPromisFormStructureLink' name="nav_create_promisForm_structures" class="long-text">
								<s:a href="%{modulesDDTURL}dictionaryAdmin/promisDataStructureAction!adminListNonexistentPromisForms.action">Export</s:a>
							</li>
							<li id='promisCsvToXMLLink' name="nav_convert_promis_csv2xml" class="long-text">
								<s:a href="%{modulesDDTURL}dictionaryAdmin/promisCsvToXMLAction!importCSV.action">Convert Form Structure</s:a>
							</li>
						</ul></li>		
							
					</ul></li>
			</sec:authorize>
		</s:if>
	</ul>
</div>


</div>
	</div>