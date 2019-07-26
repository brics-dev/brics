<div id="documentTableAndButtons">
<%@include file="/common/taglibs.jsp"%>
<%@ attribute name="supportDocList" type="java.util.Collection" required="true"%> 
<div class="ui-clearfix">
	 <s:if test="%{isDocumentLimitNR}"> 
		<div id="addDocBtnDiv" class="button button-right btn-downarrow">
				<input id="addDocBtn" type="button" value="Add Documentation" />
				<div id="selectAddDocDiv" class="btn-downarrow-options">
					<p>
						<a class="lightbox"
							href="${actionName}!addDocDialog.ajax?addDocSelect=file">File</a>
					</p>
				</div>
		</div>
	 </s:if> 
	 <s:else> 
		<div id="addDocBtnDiv" class="button button-right disabled btn-downarrow " title="The maximum amount of documents allowed to be uploaded is ${documentationLimit}.">
				<input id="addDocBtn" disabled="true" type="button" value="Add Documentation"/>
				<div id="selectAddDocDiv" class="hidden btn-downarrow-options">
					<p>
						<a class="lightbox"
							href="${actionName}!addDocDialog.ajax?addDocSelect=file">File</a>
					</p>
				</div>
		</div>
	</s:else> 
</div>
<br />

<s:hidden id="documentationLimit" name="documentationLimit" />
<s:hidden id="actionName" name="actionName" />

<div id="documentationTable" class="dataTableContainer dataTableJSON">
	<ul>
		<li><input type="button" value="Edit"
			onclick="editDocumentation()" /></li>
		<li><input type="button" class="enabledOnMany" value="Delete"
			onclick="deleteDocumentation()" /></li>
	</ul>
	<idt:jsontable name="supportDocList" id="supportDocs" decorator="gov.nih.tbi.taglib.datatableDecorators.DatasetSupportDocListDecorator">
		<idt:setProperty name="basic.msg.empty_list" value="There are no subjects at this time." />
		<idt:column title="" property="selectCheckbox" styleClass="checkbox-column" />
		<idt:column title="Name" property="docNameLink" />
		<idt:column title="Description" property="descriptionEllipsis" styleClass="breakword" />
	</idt:jsontable>
	<div class="ibisMessaging-dialogContainer"></div>
</div>
<br />
<script type="text/javascript" src="/portal/js/uploadDocumentations.js"></script>
<script type="text/javascript">


	$("#addDocBtn").click( function(e) {
		$("#selectAddDocDiv").toggle();
	});
		
</script>
</div>