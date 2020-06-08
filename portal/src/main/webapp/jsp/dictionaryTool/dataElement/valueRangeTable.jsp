<%@include file="/common/taglibs.jsp"%>
<hr class="underline">

<div class="alphanumericRange" style="float:left;">
	<div class="form-field form-field-vert">
		<label for="permissible" class="required">Permissible Value <span class="required">* </span>:
		</label>
		<s:textfield id="permissibleField" name="permissibleValue" maxlength="200" cssClass="textfield required" />
		
		
		<div class="special-instruction">Enter a single value that does not contain a semi-colon.</div>
	</div>
	
	<div class="form-field form-field-vert">
		<label for="permissible">Description of Value <span class="required">* </span>:
		</label>
		<s:textarea id="descriptionField" name="valueDescription" class="textfield"/>
		<s:fielderror fieldName="valueDescription" />
	</div>
	
	<div class="form-field form-field-vert">
		<label for="outputCodeField">Output Code: </label>
		<s:textfield id="outputCodeField" name="outputCode" maxlength="10" cssClass="textfield" />
		<s:fielderror fieldName="outputCode" />
	</div>
	
</div>
<div  style="float:left; margin-left: 20px;">
	<br>
	
<s:fielderror fieldName="permissibleValue" />
	<table class="display-data">
		<thead>
			<tr>
				<th>Permissible Value</th>
				<th class="alphanumericRange">Description</th>
				<th>Output Code</th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<s:iterator var="valueRange" value="valueRangeList">
				<tr class="odd">
					<td><s:property value="#valueRange.valueRange" /></td>
					<td class="alphanumericRange"><s:property value="#valueRange.description" /></td>
					<td><s:property value="#valueRange.outputCode" /></td>
					<td><a class="alphanumericRange" href="javascript:void(0);"
						onClick="removePermissibleValueRange('<s:property  value="#valueRange.valueRange" escapeHtml="true" escapeJavaScript="true"/>',
							'<s:property value="#valueRange.description" escapeHtml="true" escapeJavaScript="true"/>',
							'<s:property value="#valueRange.outputCode" />')">Remove</a></td>
				</tr>
			</s:iterator>
		</tbody>
	</table>
</div>
<div style="clear:both;"></div><br>
<div class="button">
	<input type="button" value="Add New Value" onClick="createValueRange()" />
</div>
<br />
<br />
<br />
<br />
<div style="clear:both;"></div>

