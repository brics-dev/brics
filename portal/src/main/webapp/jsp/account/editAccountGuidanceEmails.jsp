<%@include file="/common/taglibs.jsp"%>

<div>
			<s:form id="accountGuidanceEmailsForm" name="accountGuidanceEmailsForm">
				<div class="form-field">
					<label for="checkboxTextLightBox" class="required">Checkbox Label<span class="required">* </span>:</label>
					<input type="text" maxlength="100" name="checkboxTextLightBox" id="checkboxTextLightBox" value="${currentAccountMessageTemplate.checkboxText}"/>
					<span id="validateCheckboxMsgLightBox" style="display: none">
						<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif" style="float: none" >
						<span class="required"><strong>Checkbox Label is a  required field</strong></span>
					</span>
				</div>
				<br>
				<div class="form-field">
					<label for="messageLightBox">Text Sent to User:</label>
					<textarea name="messageLightBox" maxlength="2000" id="messageLightBox" cols="40" rows="5"><c:out value="${currentAccountMessageTemplate.message}" /></textarea>
				</div>
				<br>
				<div class="form-field">
					<div style="width: 425px">
						<label for="defaultCheckedLightBox">Default Checked?</label>
						<input type="radio" name="defaultCheckedLightBox" id="defaultCheckedLightBoxTY" ${currentAccountMessageTemplate.defaultChecked ? 'checked' :''} />Yes&nbsp;
						<input type="radio"  name="defaultCheckedLightBox" id="defaultCheckedLightBoxTN" ${currentAccountMessageTemplate.defaultChecked ? '' :'checked'} />No
					</div>
				</div>
			</s:form>
</div>
