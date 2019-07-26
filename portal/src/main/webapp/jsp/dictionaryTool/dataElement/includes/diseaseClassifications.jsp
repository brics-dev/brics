<%@include file="/common/taglibs.jsp"%>

<s:iterator var="classificationElement" value="classificationOptions" status="status">
							<div class="form-field form-field-vert">
								<label class="required"><s:property value="#classificationElement.subgroup.subgroupName" /> <span
									class="required">* </span>:</label>
									
								<s:set var="classList" value="classificationList" />
	
								<s:if test="#classList.size==1">
									<s:set var="startValue" value="#classList[0].id" />
									<s:hidden name="valueRangeForm.classificationElementList[%{#classificationElement.subgroup.id}]"
										value="%{#startValue}" />
								</s:if>
								<s:else>
							
									<s:set var="startValue" value="#classificationElement.classification.id" />
								</s:else>
						
								<s:select id="valueRangeForm.classificationElementList[%{#classificationElement.subgroup.id}]"
									name="valueRangeForm.classificationElementList[%{#classificationElement.subgroup.id}]" list="#classList"
									listKey="id" listValue="name" value="#startValue" headerKey="" headerValue="- Select One -"
									disabled="isPublished || #classList.size==1 " />
							</div>
						</s:iterator>