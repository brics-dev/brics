<%@page import="gov.nih.tbi.commons.model.StatusType"%>
<%@include file="/common/taglibs.jsp"%>

<div class="lightbox-size" style="overflow: hidden;">
	<div class="border-wrapper">
		<div id="main-content">
		<s:if test="editMode">
			<h2>Edit Data Element Group</h2>
		</s:if>
		<s:else>
			<h2>Add Data Element Group</h2>
		</s:else>
			<s:form validate="true" name="repeatableGroupForm" cssClass="validate" action="addRepeatableElementGroupAction">
				<s:token />
				<div class="formElements">

					<p>Data element groups allow you to group a set of data elements together, essentially creating sections
						within a form structure.</p>
					<p class="required">
						Fields marked with a <span class="required">* </span>are required.
					</p>
					<input type="hidden" name="sessionRepeatableGroupNames" value="${sessionRepeatableGroupNames}" />

					<div class="form-field">
						<label for="name" class="required">Group Name <span class="required">* </span>:
						</label>
						<s:if test="editMode">
							<s:textfield name="repeatableGroupForm.name" value="%{repeatableGroup.name}" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
						</s:if>
						<s:else>
							<s:textfield name="repeatableGroupForm.name" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
						</s:else>
						<s:fielderror fieldName="repeatableGroupForm.name" />
					</div>

					<div class="form-field">
						<label for="type" class="required">Number of Times Repeated <span class="required">* </span>:
						</label>
						<s:if test="editMode">
							<s:select id="type" list="repeatableTypes" listKey="value" listValue="value" value="%{repeatableGroup.type.value}"  name="repeatableGroupForm.type" cssClass="textfield" ></s:select>
						</s:if>
						<s:else>
							<s:select id="type" list="repeatableTypes" listKey="value" listValue="value" value="%{repeatableGroup.type.value}" name="repeatableGroupForm.type" cssClass="textfield" />
						</s:else>
						<div class="special-instruction">Bounds for repeating. it can be an exact, a maximum or a minimum.</div>
					</div>

					<div class="form-field">
						<label for="threshold" class="required">Threshold<span class="required">* </span>:
						</label>
						<s:if test="editMode">
							<s:textfield name="repeatableGroupForm.threshold" value="%{repeatableGroup.threshold}" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
						</s:if>
						<s:else>
						<s:textfield name="repeatableGroupForm.threshold" cssClass="textfield required" escapeHtml="true" escapeJavaScript="true" />
						</s:else>
						<s:fielderror fieldName="repeatableGroupForm.threshold" />
						<div class="special-instruction">Enter 0 (zero) to be able to repeat indefinitely</div>
					</div>

					<%-- 	<ndar:dataStructureSave action="dataStructureValidationAction" method="moveToElements" /> --%>
						<s:if test="editMode">
							<div id="divEdit" style="display: none;">
								<s:textfield name="editMode" value="true" escapeHtml="true" escapeJavaScript="true"/>
								<s:textfield name="groupElementId" value="%{repeatableGroup.id}" escapeHtml="true" escapeJavaScript="true"/>
							</div>
						</s:if>
						
					<div class="button">
					<s:if test="editMode">
								<input type="button" value="Save Edits" id="submitGroupButton"
							onclick="javascript: submitRepeatableElementGroupFormEdits();" />
						</s:if>
						<s:else>
							<input type="button" value="Save Group" id="submitGroupButton"
							onclick="javascript: submitRepeatableElementGroupForm();" />
						</s:else>
					</div>
				</div>

			</s:form>
			</form>
		</div>
	</div>
</div>