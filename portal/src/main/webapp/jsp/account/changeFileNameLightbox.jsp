<%@include file="/common/taglibs.jsp"%>

<label for="changeFileName" class="required">File Name<span class="required">* </span>:</label>

<s:textfield id="changeFileName" name="changeFileName" cssClass="float-left" maxlength="100" escapeHtml="true" escapeJavaScript="true" />

<span id="validateChangeFileName" style="display: none">
									<img class="icon-warning" alt="" src="/portal/images/brics/common/icon-warning.gif">
									<span class="required"><strong>Please Enter a new file name (255 char max) to change to</strong></span>
					 </span>