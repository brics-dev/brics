
<%@page import="gov.nih.tbi.commons.model.RequiredType"%>
<%@include file="/common/taglibs.jsp"%>
<c:set var="hostname" value="${pageContext.request.serverName}"/>
<c:set var="hostStyle" value="default-style"/>
	<c:choose>
         <c:when test="${fn:contains(hostname, 'cnrm' )}">
        	<c:set var="hostStyle" value="cnrm-style"/>
         </c:when>
         <c:when test="${fn:contains(hostname, 'pdbp' )}">
          <c:set var="hostStyle" value="pdbp-style"/>
         </c:when>
         <c:when test="${fn:contains(hostname, 'fitbir' )}">
         	<c:set var="hostStyle" value="fitbir-style"/>
         </c:when>
         <c:when test="${fn:contains(hostname, 'ninds' )}">
         	<c:set var="hostStyle" value="ninds-style"/>
         </c:when>
           <c:when test="${fn:contains(hostname, 'gsdr' )}">
         	<c:set var="hostStyle" value="gsdr-style"/>
         </c:when>
          <c:when test="${fn:contains(hostname, 'cistar' )}">
         	<c:set var="hostStyle" value="cistar-style"/>
         </c:when>
         <c:when test="${fn:contains(hostname, 'eyegene' ) || fn:contains(hostname, 'nei' )}">
         	<c:set var="hostStyle" value="eyegene-style"/>
         </c:when>
         <c:when test="${fn:contains(hostname, 'cdrns' )}">
         	<c:set var="hostStyle" value="cdrns-style"/>
         </c:when>
         <c:when test="${fn:contains(hostname, 'nti' )}">
         	<c:set var="hostStyle" value="nti-style"/>
         </c:when>
         <c:when test="${fn:contains(hostname, 'nia' )}">
         	<c:set var="hostStyle" value="nia-style"/>
         </c:when>
         <c:when test="${fn:contains(hostname, 'grdr' )}">
         	<c:set var="hostStyle" value="grdr-style"/>
         </c:when>                  
	</c:choose>


<s:set var="repeatableGroups" value="currentDataStructure.repeatableGroups" />
<s:set var="readOnly" value="true" />
<s:if test="%{repeatableGroups.size == 0}">
	<p>There are no data elements attached to this form structure.</p>
</s:if>
<s:else>
	<!-- 		Draw the first table outside the accordion -->

	<s:iterator var="group" value="#repeatableGroups">
		<c:if test="${group.name == 'Main'}">
			<div>
				<!-- 					Start Drawing an individual table -->
				<s:if test="#group.size == 0">
					<p>
						There are no data elements in the main group.
						
					</p>
				</s:if>
				<s:else>
					<table id="<c:out value="${group.id}" />" class="display-data full-width">
						<thead>
							<tr>
								<th width="20px">#</th>
								<th><s:text name="dataElementForm.title" /></th>
								<th>Short Description</th>
								<th><s:text name="dataElementForm.name" /></th>
								<th width="90px">Required?</th>
								<th width="45px">Type</th>
								<s:if test="!#readOnly">
									<th><a href="javascript: removeAll(${group.id})"><u>Remove All?</u></a></th>
								</s:if>
							</tr>
						</thead>
						<tbody>
							<s:iterator var="mapElement" value="#group.mapElements">
								<c:choose>
									<c:when test="${mapElement.position%2 == 1}">
										<tr class="odd" id="<s:property value="#mapElement.id" />">
									</c:when>
									<c:otherwise>
										<tr class="stripe" id="<c:out value="${mapElement.id}" />">
									</c:otherwise>
								</c:choose>
								<td><s:property value="#mapElement.position" /></td>
								<s:if test="!readOnly">
									<td><a class="lightbox" target="_blank"
										href="dataStructureElementAction!viewMapElement.ajax?mapElementId=${mapElement.id}&groupElementId=${group.id}">
											<c:out value="${currentDataStructure.dataElements[mapElement.structuralDataElement.nameAndVersion].title}"/>&nbsp;
											<c:if test="${mapElement.structuralDataElement.status.id == 0}">
												<p class="draft-class red-text">(Draft)</p>
											</c:if>
											<c:if test="${mapElement.structuralDataElement.status.id == 3}">
												<p class="red-text">(Deprecated)</p>
											</c:if>
											<c:if test="${mapElement.structuralDataElement.status.id == 4}">
												<p class="red-text">(Retired)</p>
											</c:if>
									</a></td>
								</s:if>
								<s:elseif test="currentMethod  == lightboxView">
									<td>
										<s:if test="publicArea == true && !queryArea">
										<a target="_blank" href="/${portalRoot}/publicData/dataElementAction!view.action?dataElementName=${mapElement.structuralDataElement.nameAndVersion}&style.key=<c:out value="${hostStyle}" />">
										
										</s:if>
										
										<s:if test="!publicArea && !queryArea">
										<a class="lightbox" target="_blank"
											href="/${portalRoot}/dictionary/dataElementAction!viewDetails.ajax?dataElementName=${mapElement.structuralDataElement.nameAndVersion}<s:if test="publicArea">&publicArea=true&dataStructureId=<s:property value="currentDataStructure.id" /></s:if>"
										>
										</s:if>
										<c:out value="${currentDataStructure.dataElements[mapElement.structuralDataElement.nameAndVersion].title}"/>&nbsp;
										<c:if test="${mapElement.structuralDataElement.status.id == 0}">
											<p class="draft-class red-text">(Draft)</p>
										</c:if>
										<c:if test="${mapElement.structuralDataElement.status.id == 3}">
											<p class="red-text">(Deprecated)</p>
										</c:if>
										<c:if test="${mapElement.structuralDataElement.status.id == 4}">
											<p class="red-text">(Retired)</p>
										</c:if>
										<s:if test="!queryArea">
											</a>
										</s:if> 
									</td>
								</s:elseif>
								<s:else>
									<td><a
										href="mapElementAction!view.action?mapElementId=${mapElement.id}&dataStructureId=${group.dataStructure.id}">
										<c:out value="${currentDataStructure.dataElements[mapElement.structuralDataElement.nameAndVersion].title}"/>&nbsp;
										<c:if test="${mapElement.structuralDataElement.status.id == 0}">
											<p class="draft-class red-text">(Draft)</p>
										</c:if>
										<c:if test="${mapElement.structuralDataElement.status.id == 3}">
											<p class="red-text">(Deprecated)</p>
										</c:if>
										<c:if test="${mapElement.structuralDataElement.status.id == 4}">
											<p class="red-text">(Retired)</p>
										</c:if>
									</a></td>
								</s:else>
								<td><c:out value="${currentDataStructure.dataElements[mapElement.structuralDataElement.nameAndVersion].shortDescription}" /></td>
								<td><s:property value="#mapElement.structuralDataElement.name" /></td>
								<td><s:property value="#mapElement.requiredType.value" /></td>
								<td><s:property value="#mapElement.structuralDataElement.category.shortName" /></td>
								</tr>
							</s:iterator>
						</tbody>
					</table>
				</s:else>
			
				
			</div>
		</c:if>
	</s:iterator>

	
	<s:if test="#repeatableGroups.size == 1">
		<p>
			There are no additional data element groups.
		</p>
	</s:if>
	<s:else>
		<p>Listed below are your additional element groups.</p>
		<div id="details-accordion">
			<s:iterator var="group" value="#repeatableGroups" status="groupStatus">
				<c:if test="${group.name != 'Main' }">
				<div id="group${group.id}">
					<h4>
						<a href="#"> <c:out value="${group.name}  " />
						<c:choose>
							<c:when test="${group.threshold != 0 }">
								<c:out value="(Appears ${group.type.value} ${group.threshold} " />Time<c:if test="${group.threshold != 1 }">s</c:if>)
							</c:when>
							<c:otherwise>
								(Repeat Infinitely)
							</c:otherwise>
						</c:choose>
						</a>
					</h4>
				
					<div>
						<!-- 					Start Drawing an individual table -->
						<s:if test="#group.size == 0">
							<br />
							<p>
								There are no data elements in this group.
								
							</p>
						</s:if>
						<s:else>
							<table id="<c:out value="${group.id}" />" class="display-data full-width">
								<thead>
									<tr>
										<th width="20px">#</th>
										<th><s:text name="dataElementForm.title" /></th>
										<th>Short Description</th>
										<th><s:text name="dataElementForm.name" /></th>
										<th width="90px">Required?</th>
										<th width="45px">Type</th>
										<s:if test="!#readOnly">
											<th><a href="javascript: removeAll(${group.id})">Remove All?</a></th>
										</s:if>
									</tr>
								</thead>
								<tbody>
									<s:iterator var="mapElement" value="#group.mapElements">
										<c:choose>
											<c:when test="${mapElement.position%2 == 1}">
												<tr class="odd" id="<c:out value="${mapElement.id}" />">
											</c:when>
											<c:otherwise>
												<tr class="stripe" id="<c:out value="${mapElement.id}" />">
											</c:otherwise>
										</c:choose>
																				<td><s:property value="#mapElement.position" /></td>
										<s:if test="!readOnly">
											<td><a class="lightbox" target="_blank"
												href="dataStructureElementAction!viewMapElement.ajax?mapElementId=${mapElement.id}&groupElementId=${group.id}">
													<c:out value="${currentDataStructure.dataElements[mapElement.structuralDataElement.nameAndVersion].title}"/>&nbsp;
													<c:if test="${mapElement.structuralDataElement.status.id == 0}">
														<p class="draft-class red-text">(Draft)</p>
													</c:if>
													<c:if test="${mapElement.structuralDataElement.status.id == 3}">
														<p class="red-text">(Deprecated)</p>
													</c:if>
													<c:if test="${mapElement.structuralDataElement.status.id == 4}">
														<p class="red-text">(Retired)</p>
													</c:if>
											</a></td>
										</s:if>
										<s:elseif test="currentMethod  == lightboxView">
											<td>
										<s:if test="publicArea == true && !queryArea">
										
										eha
										<a target="_blank" href="/${portalRoot}/publicData/dataElementAction!view.action?dataElementName=${mapElement.structuralDataElement.nameAndVersion}&style.key=<c:out value="${hostStyle}" />">
										
											</s:if>
										
										<s:if test="!publicArea && !queryArea">
										<a
											class="lightbox"
											target="_blank"
											href="/${portalRoot}/dictionary/dataElementAction!viewDetails.ajax?dataElementName=${mapElement.structuralDataElement.nameAndVersion}<s:if test="publicArea">&publicArea=true&dataStructureId=<s:property value="currentDataStructure.id" /></s:if>"
										>
										</s:if>
												<c:out value="${currentDataStructure.dataElements[mapElement.structuralDataElement.nameAndVersion].title}"/>&nbsp;
												<c:if test="${mapElement.structuralDataElement.status.id == 0}">
													<p class="draft-class red-text">(Draft)</p>
												</c:if>
												<c:if test="${mapElement.structuralDataElement.status.id == 3}">
													<p class="red-text">(Deprecated)</p>
												</c:if>
												<c:if test="${mapElement.structuralDataElement.status.id == 4}">
													<p class="red-text">(Retired)</p>
												</c:if>
										
										<s:if test="!queryArea">
											</a>
										</s:if> 
											</td>
										</s:elseif>
										<s:else>
											<td><a
												href="mapElementAction!view.action?mapElementId=${mapElement.id}&dataStructureId=${group.dataStructure.id}">
												<c:out value="${currentDataStructure.dataElements[mapElement.structuralDataElement.nameAndVersion].title}"/>&nbsp;
												<c:if test="${mapElement.structuralDataElement.status.id == 0}">
													<p class="draft-class red-text">(Draft)</p>
												</c:if>
												<c:if test="${mapElement.structuralDataElement.status.id == 3}">
													<p class="red-text">(Deprecated)</p>
												</c:if>
												<c:if test="${mapElement.structuralDataElement.status.id == 4}">
													<p class="red-text">(Retired)</p>
												</c:if>
												</a></td>
										</s:else>
										<td><c:out value="${currentDataStructure.dataElements[mapElement.structuralDataElement.nameAndVersion].shortDescription}"/></td>
										<td><s:property value="#mapElement.structuralDataElement.name" /></td>
										<td><s:if test="#readOnly">
												<s:property value="#mapElement.requiredType.value" />
											</s:if> <s:else>
												<div id="<s:property value='#mapElement.id' />,<s:property value='#group.id' />">
													<s:select cssStyle="width: 130px;" list="requiredTypes" listKey="id" listValue="value"
														value="#mapElement.requiredType.id" cssClass="textfield required-type" />
													<a id="<s:property value='#mapElement.id' />_<s:property value='#group.id' />" class="condition-edit">[EDIT]</a>
													<s:if test="#mapElement.condition != null">
														<a id="<s:property value='#mapElement.id' />-<s:property value='#group.id' />"
															href="javascript:deleteCondition(<s:property value='#mapElement.id' />, <s:property value='#group.id' />)"
															class="condition-edit">[REMOVE]</a>
													</s:if>
												</div>
											</s:else></td>
											
										<td><s:property value="#mapElement.structuralDataElement.category.shortName" /></td>
												
										<s:if test="!#readOnly">
											<td><a
												href="javascript: remove(<s:property value='#mapElement.id' />, <s:property value='#group.id' />) ">remove</a>
											</td>
										</s:if>
										</tr>
										</s:iterator>
								</tbody>
							</table>
						</s:else>
						
					</div>
					</div>
				</c:if>
			</s:iterator>
		</div>
	</s:else>
</s:else>

<s:if test="!#readOnly">

	<script type="text/javascript" src='<s:url value="/js/jquery.tablednd_0_5.js"/>'></script>
	<script type="text/javascript">

	function changeRequiredType(mapElementId, groupElementId, requiredTypeId) {

		$.post("dataStructureElementAction!changeRequiredType.ajax", {
			mapElementId: mapElementId,
			groupElementId: groupElementId,
			requiredTypeId: requiredTypeId
		});
	}
	
	function conditionalLightbox(mapElementId, groupId) {
		$.post("conditionalLogicAction!view.ajax", {
			mapElementId: mapElementId,
			groupId: groupId
		}, function(data) {
			$.fancybox(data);
		});
	}
	
	function deleteCondition(mapElementId, groupId) {
		$.post("conditionalLogicAction!delete.ajax", {
			mapElementId: mapElementId,
			groupId: groupId
		}, function(data) {
			if (data == "success") {
				window.location.replace("dataStructureAction!moveToElements.action");
			}
		});
	}
	
	$(document).ready(function() {
		$("a.lightbox").fancybox();
		
	    setup();
	    sortableAccordion();
	    
	    // Open a specific accordion if groupElementId is specified (id of repeatable Group)
	    if (getParameterByName('groupElementId') != "")
	    	{
	    		var groupToOpen = getParameterByName('groupElementId');
	    		$('#group' + groupToOpen + ' > h4').click();
	    	}

	    
	    //Bind submit functions to drop down menus
	    $(".required-type").change( function() {
			
			var p = $(this).parent();
			var ids = $(p).attr("id").split(",");
			var value = this.options[this.selectedIndex].value;
			
			changeRequiredType(ids[0], ids[1], value);
			
			var container = $(this).closest("div").attr("id");
			var mapElementId = ids[0];
			var groupId = ids[1];
			var edit = "#"+container.split(",").join("_");
			var deleteId = "#"+container.split(",").join("-");
			if($(this).val() == 3 || $(this).val() == 4) {
				$(edit).attr("href", "javascript:conditionalLightbox("+mapElementId+","+groupId+");");
				$(edit).fadeIn();
				conditionalLightbox(mapElementId, groupId);
			} else {
				$(edit).fadeOut();
				$(deleteId).fadeOut();
			}
		});
	    
	    
	    
	    
	  //Bind submit functions to drop down menus
	    $(".change-element-group").change( function() {

			var p = $(this).parent();
			var ids = $(p).attr("id").split(",");
			var value = this.options[this.selectedIndex].value;
			
			/* remove element from group it is currently in, then add it to the new group */
			//changeRequiredType(ids[0], ids[1], value);
			
			changeGroup(ids[0],ids[1], ids[2], value);
			
			
	});
	
	  
	});
	
	function sortableAccordion() {
		// Make this accodion sortable
		$("#details-accordion")
		    .sortable({
		    axis: "y",
		    handle: "h4",
		    stop: function (event, ui) {
		        // IE doesn't register the blur when sorting
		        // so trigger focusout handlers to remove .ui-state-focus
		        ui.item.children("h4").triggerHandler("focusout");
		        
		        // fire the moveGroup event
		        var groupId = $(ui.item).attr("id").split("group")[1];
		        moveGroup(groupId, ui.item.index() + 1);
		    },
		    start: function (event, ui) {
		    	$("#details-accordion").accordion( "option", "active", false );
		    }
		});
	}
	
	function setup() {
		
		
		// Initialise the table
	    $(".display-data").tableDnD(
	    			{
	    				onDrop: function(table, row) {
// 	    					$.tableDnD.serialize()

							// Find and store the open accordian
							var open =$("#details-accordion").accordion("option", "active");

//							Get tables id (corresponds to repeatableGroupId)
							var groupElementId = table.getAttribute("id");
							var rows = table.tBodies[0].rows;
							var endLocation;
							
							for (var i=0; i<rows.length;i++)
							{
								if (rows[i].id == row.id)
								{
									endLocation = i;
								}
							}
							
	    					
	    					$.post( "dataStructureElementAction!moveMapElement.ajax",
	    							{ mapElementId: row.id, rowId: endLocation, groupElementId: groupElementId },
	    							function (data)
	    							{
	    					            $("#elementsDiv").html(data);
	    					            $("#elementsDiv").find("script").each(function(i) {
	    					                eval($(this).text());
	    					            });
	    								$('#details-accordion').accordion({active: open, autoHeight: false,heightStyle: "content" , header: "h4"});
	    								sortableAccordion();
	    								$('a.lightbox').fancybox();
	    								setup();
	    							});

	    				}
	    			}
	    		);
		
			    $(".required-type").each(function(index) {
			    	var ids = $(this).closest("div").attr("id").split(",");
					var mapElementId = ids[0];
					var groupId = ids[1];
			    	var container = $(this).closest("div").attr("id");
					var edit = "#"+container.split(",").join("_");
					if($(this).val() != 3 && $(this).val() != 4) {
						$(edit).hide();
					} else if($(this).val() == 3 || $(this).val() == 4) {
						$(edit).attr("href", "javascript:conditionalLightbox("+mapElementId+","+groupId+")");
					}
				});
	}
	
	function showAddGroupLightbox() {
		$.post("dataStructureElementAction!showAddGroupLightbox.ajax",
			function(data) {
				document.getElementById("addGroup-lightbox").innerHTML = data;
			});
		}

	function viewElement(mapElementId, groupElementId) {
		$.post("dataStructureElementAction!viewMapElement.ajax", {
			mapElementId: mapElementId,
			groupElementId: groupElementId
		}, function(data) {
			document.getElementById("dataElement-lightbox").innerHTML = data;
			
		});
	}
	
	function remove(mapElementId, groupElementId) {
		if(!confirm("Are you sure you wish to remove this element from the group?"))
		{
			return;
		}
		
		var open = $( "#details-accordion" ).accordion( "option", "active" );
		
		$.post("dataStructureElementAction!removeMapElement.ajax", {
			mapElementId: mapElementId,
			groupElementId : groupElementId
		}, function(data) {
	        $("#elementsDiv").html(data);
	        $("#elementsDiv").find("script").each(function(i) {
	            eval($(this).text());
	        });
			$('#details-accordion').accordion({active: open, autoHeight: false,heightStyle: "content" , header: "h4"});
			sortableAccordion();
			$('.lightbox').fancybox();
		});
	}
	
	
	function changeGroup(mapElementId,dataElementId, currentGroupElementId, newGroupElementId) {
		var stop = 0;
		//let's check if the element is already in the new group
		$( "div[name*='"+newGroupElementId+"']" ).each(function(){
			
			var ids = $(this).attr("id").split(",");
			
			if(dataElementId == ids[1]) {
				
				alert("The Data Element can not be moved. This group contains a duplicate element.");
				stop = 1;
				
			}
		});
		
		if(stop) {
			return;
		}
		
		if(!confirm("Are you sure you wish to change this element's group?"))
		{
			return;
		}
		
		
		
		//var open = $( "#details-accordion" ).accordion( "option", "active" );
		//remove the element from the current group it is in
		$.post("dataStructureElementAction!removeMapElement.ajax", {
			mapElementId: mapElementId,
			groupElementId : currentGroupElementId
		}, function(data) {
			/*$("#elementsDiv").html(data);
	        $("#elementsDiv").find("script").each(function(i) {
	            eval($(this).text());
	        });
			$('#details-accordion').accordion({active: open, autoHeight: false, header: "h4"});
			sortableAccordion();
			$('.lightbox').fancybox();*/
			//console.log(data);
				// Find and store the open accordion
		 		var open =$("#details-accordion").accordion("option", "active");

		 		var address="dataStructureElementAction!addDataElements.action"
		 				+ "?dataElementIds="
		 				+ dataElementId
		 				+ "&groupElementId="
		 				+ newGroupElementId;
		 		window.location.href = address;
			
			/**/
		});
		
		
		
	}

	function removeAll(groupElementId) {
		
		var open =$("#details-accordion").accordion("option", "active");
		
		if(!confirm("Are you sure you wish to remove all the elements from this group?"))
		{
			return;
		}
		
		$.post("dataStructureElementAction!removeAllMapElements.ajax", {
			groupElementId : groupElementId
		}, function(data) {
	        $("#elementsDiv").html(data);
	        $("#elementsDiv").find("script").each(function(i) {
	            eval($(this).text());
	        });
			$('#details-accordion').accordion({active: open, autoHeight: false,heightStyle: "content" , header: "h4"});
			sortableAccordion();
			$('.lightbox').fancybox();
		});
	}
	
	function removeGroup(groupElementId) {
		
		if(!confirm("Are you sure you want to delete this group? Once deleted, this group cannot be recovered."))
		{
			return;
		}
		
		$.post("dataStructureElementAction!removeRepeatableGroup.ajax", {
			groupElementId : groupElementId
		}, function(data) {
	        $("#elementsDiv").html(data);
	        $("#elementsDiv").find("script").each(function(i) {
	            eval($(this).text());
	        });
			$('#details-accordion').accordion({ autoHeight: false,heightStyle: "content" , header: "h4"});
			sortableAccordion();
			$('.lightbox').fancybox();
		});
	}
	
	function moveGroup(group, position) {
		var open =$("#details-accordion").accordion("option", "active");
		
		$.post("dataStructureElementAction!moveRepeatableGroup.ajax", {
			groupElementId : group,
			position : position
		}, function(data) {
			// The UI is changed by the accordion. There is no need to drap everything again
	        //$("#elementsDiv").html(data);
	        //$("#elementsDiv").find("script").each(function(i) {
	        //    eval($(this).text());
	        //});
			//$('#details-accordion').accordion({active: (position - 1), autoHeight: false, header: "h4"});
			//sortableAccordion();
			//$('.lightbox').fancybox();
			
			// Open the the accordion that was just moved
			//$("#details-accordion").accordion("option", "active", position - 1);
			
		});
	}
	
	function submitRepeatableElementGroupForm()
	{
		$.ajax({
			type: "POST",
			cache: false,
			url: "addRepeatableElementGroupAction!addRepeatableElementGroup.ajax",
			data: $("form").serializeArray(),
			success: function(data) {
				
				if (data == "success")
				{
					window.location.replace("dataStructureAction!moveToElements.action");
				}
				else
				{
					$.fancybox(data);
				}
			}
		});
	}
	
	function submitRepeatableElementGroupFormEdits()
	{
		$.ajax({
			type: "POST",
			cache: false,
			url: "editRepeatableElementGroupAction!editRepeatableElementGroup.ajax",
			data: $("form").serializeArray(),
			success: function(data) {
				
				if (data == "success")
				{
					window.location.replace("dataStructureAction!moveToElements.action");
				}
				else
				{
					$.fancybox(data);
				}
			}
		});
	}
	
	
	function getParameterByName(name) {
	    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
	        results = regex.exec(location.search);
	    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
	}
	
</script>
</s:if>
<s:else>
	<script type="text/javascript">	
		$(document).ready(function() {
			$("a.lightbox").fancybox();
		});
	</script>
</s:else>
