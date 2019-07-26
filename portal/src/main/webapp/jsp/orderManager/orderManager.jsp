<%@include file="/common/taglibs.jsp"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="decorator"
	uri="http://www.opensymphony.com/sitemesh/decorator"%>
<%@taglib prefix="page" uri="http://www.opensymphony.com/sitemesh/page"%>
<%@taglib prefix="s" uri="/struts-tags"%>


<title>Brics Biosample Orders - Biosample Orders</title>
<div class="border-wrapper">
	<p>
		<a href='<s:url action="openQueue" />'>Your Queue (<s:property
				value="queueSize" />)
		</a>&nbsp;&nbsp;<a href='<s:url action="viewOrders" />'>Your Orders (<s:property
				value="orderSize" />)
		</a>
		<s:if test="isOrderManagerAdmin">&nbsp;&nbsp;<a
				href='<s:url action="adminOrders" />'>Admin Orders (<s:property
					value="adminOrderSize" />)
			</a>
		</s:if>
	</p>
			<h1>Biosample Orders</h1>
	<div style="padding-top:0px !important; margin-top:0px !important;"
	 id="content-wrapper">
	
		<s:if test="errorMessage neq null">

			<div id="errorMessage" class="errors">
				<h2>
					<s:property value="errorMessage" />
				</h2>
			</div>
		</s:if>
		<form id="form1" name="form1"
			action="saveOrderValidation!saveOrder.action" method="post"
			enctype="multipart/form-data">
			<s:token />
			
			<s:if test="isOrderManagerAdmin">
			<div id="orderOwner"  style="padding-top:0px !important; margin-top:0px !important;" class="unitLeft" style="width: 350px;"> 
					<p>
						<b>Owner:</b> <s:property value="orderOwner.displayName" />
					<br>
						<b>Owner Organization:</b> <s:property
								value="orderOwner.affiliatedInstitution" />
					</p>

 				</div> 
			</s:if>

				
<!-- 			<div class="form-output readonly-text no-padding"> -->
			
				<p>
					<b>Order #:</b> <s:property value="currentOrder.id" />
				</p>
<!-- 			</div> -->
			<p class="required">
					Fields marked with a <span class="required">* </span>are required.
				</p>
			<div class="form-field full-length">
				<p>
					<label for="orderTitle" class="required" style="width:75px;"><b>Order
							Title: <span class="required">* </span>
					</b></label>
					<!--  the order title should probably be captured in a simple string field and then set as the order  bean field because
			the first time this page is displayed there will be no title in the order bean so the title will never be set -->
					<s:if test="isOrderEditable">
						<s:textfield name="orderTitle" cssClass="textfield"
							id="orderTitle" label="Order Title" theme="simple" />
						<s:fielderror fieldName="orderTitle" cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="orderTitle" />
					</s:else>
				</p>
			</div>
			
			<c:forEach items="${currentOrder.requestedItems}" var="item">
				<input 
					type="hidden" 
					id="hidden_<c:out value="${item.id}"></c:out>"
					name="itemsFromQueue(<c:out value="${item.id}"></c:out>).numberOfAliquots" 
					value="<c:out value="${item.numberOfAliquots}"></c:out>" />
			</c:forEach>
			
			<div id="activeOrderTableContainer" class="dataTableContainer idtTableContainer">
				<div id="dialog"></div>
				<table id="activeOrderTable" class="table table-striped table-bordered"  width="100%"></table>
			</div>
			
			
			<br>
			<div id="uploadForm">
			<%-- 	<s:if test="isOrderEditable"> --%>
					<label for="uploadDocumentation" class="required no-float left"><h2>
							Add Supporting Documentation <span class="required">* </span>
						</h2></label>
					<br>
					<p> A copy of the NINDS Human Genetics Repository MTA should be signed by the requesting PI and their institutional business official and uploaded with each order. 
						<br><b>Download</b>&nbsp;&nbsp;<a href="fileDownloadAction!download.action?fileId=21">NINDS Human Genetics Repository Material
							Transfer Agreement for Biospecimens</a> 
						<br><b>Download</b>&nbsp;&nbsp;<a href="fileDownloadAction!download.action?fileId=20">Request to Access Parkinson's Disease Related-Biospecimens(X01)</a> <span class="required">*
						</span>
					</p>
					<!--  Include File Upload Page -->
					
					<div class="action-button">
						<a class="lightbox" target="_blank" href="orderAction!view.ajax"
							onClick="javascript: submitBeforeDocumentation('orderAction!view.ajax');">
							Upload Supporting Documentation</a>
					</div>									
					<div style="clear:both;"></div>
					<h4>Required Files:</h4>
					<p> Based on the item(s) being ordered, the following are the required files for Repositories being accessed</p>

										<table class="display-data">
										<thead>
												<tr>
													<th>File Type</th>
												</tr>
											</thead>
											<tbody>
												<s:iterator  value="uploadFileType">
												<tr class="odd"> 
													<td><s:property/></td>
													</tr>
												</s:iterator >
											</tbody>
										</table>

					<%-- 					<s:if test="filesAttached.size gte 5"> --%>
<!-- 					<p id="limitMessage">The upload limit of 5 documents has been reached, please remove a file if needed.</p> -->
<%-- 				</s:if> --%>
				<%-- </s:if> --%>
				<div class="form-field">
				<s:fielderror
								fieldName="uploadedFilesMap"
								cssClass="left error-message" />
					<br>
					<h4>Attached Files:</h4>

					<%-- 		<s:if test="filesAttached.size gt 0" > --%>
					<table class="display-data">
						<thead>
							<tr>
								<th>Filename</th>
								<th>File Type</th>
								<s:if test="isOrderEditable">
								<th></th>
								</s:if>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="file" items="${filesAttached}">
								<tr class="odd">
									<%-- 									<s:if test="isOrderEditable"> --%>
<%-- 									<td><c:out value="${file.name}" /></td> --%>
									<%-- 									</s:if> --%>
									<%-- 									<s:else> --%>
									<td><a
										href="javascript:void(0);" onClick="$().downloadDocument(${file.id})">${file.name}</a></td>
																			<td>${file.description}</td>
									<%-- 									</s:else> --%>
									<s:if test="isOrderEditable">
										<td><a href="javascript:void(0);"
											onClick="$().removeUploadDocument(this,'<c:out value="${file.name}" />')">Remove</a></td>
									</s:if>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>


			</div>
			<div id="form2">


				<div class="form-field">
					<h2>Current Comments</h2>
					<div id="addNotation">

						<div id="notesList">
							<c:forEach items="${currentOrder.commentList}" var="note">
								<span> <span class="noteHeader"><c:choose>
											<c:when test="${note.user != null }">
												<b><c:out
														value="${note.user.firstName} ${note.user.lastName}" /></b>
											</c:when>
											<c:otherwise>
												<b>System</b>
											</c:otherwise>
										</c:choose> created on - <fmt:formatDate dateStyle="MEDIUM" pattern="yyyy-MM-dd"
											value="${note.date}" />&nbsp;</span> <c:out value="${note.message}" /></span>
							</c:forEach>
						</div>
						<s:if test="isOrderEditable">
							<label for="note" class="no-float left"><h2>Add
									Comment</h2></label>
							<p>(limit 4000 char)</p>
							<div id="addNoteTextArea">
								<s:textarea id="note" label="Add a note" theme="simple"
									name="orderComment" rows="8" cssClass="textfield"
									cssStyle="width:650px;" />
								<s:fielderror fieldName="orderComment"
									cssClass="left error-message" />
							</div>
						</s:if>
					</div>
				</div>
				<hr>

				<h2>Shipping Address</h2>
				<div class="form-field full-length">

					<label for="orderBean.shipToName" class="required">Ship To
						Name: 
						<s:if test="isOrderEditable">
						<span class="required">* </span>
						</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield name="orderBean.shipToName" label="Ship to Name"
							cssClass="textfield" />
						<s:fielderror fieldName="orderBean.shipToName"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="orderBean.shipToName" />
					</s:else>

				</div>
				<div class="form-field full-length">

					<label for="orderBean.shipToInstitution" class="required">Ship
						To Institution: 
						<s:if test="isOrderEditable">
						<span class="required">* </span>
						</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield name="orderBean.shipToInstitution"
							label="Ship to Institution" cssClass="textfield" />
						<s:fielderror fieldName="orderBean.shipToInstitution"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="orderBean.shipToInstitution" />
					</s:else>
				</div>
				<div class="form-field full-length">
					<label for="orderBean.phone" class="required">
						Ship To Phone: 
						<s:if test="isOrderEditable">
							<span class="required">* </span>
						</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield name="orderBean.phone" label="Ship to Phone"
							cssClass="textfield" />
						<s:fielderror fieldName="orderBean.phone"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="orderBean.phone" />
					</s:else>
				</div>
				<div class="form-field full-length">
					<label for="orderBean.affiliation" class="required">
						Affiliation: 
						<s:if test="isOrderEditable">
							<span class="required">* </span>
						</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield name="orderBean.affiliation" label="Affiliation"
							cssClass="textfield" />
						<s:fielderror fieldName="orderBean.affiliation"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="orderBean.affiliation" />
					</s:else>
				</div>
				<div class="form-field full-length">
					<label for="orderBean.affiliationPhone" class="required">
						Affiliation Phone: 
							<s:if test="isOrderEditable">
								<span class="required">* </span>
							</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield name="orderBean.affiliationPhone" label="Affiliation Phone"
							cssClass="textfield" />
						<s:fielderror fieldName="orderBean.affiliationPhone"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="orderBean.affiliationPhone" />
					</s:else>
				</div>
				<div class="form-field full-length">
					<label for="orderBean.affiliationEmail" class="required">
						Affiliation Email: 
						<s:if test="isOrderEditable">
						<span class="required">* </span>
						</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield name="orderBean.affiliationEmail" label="Affiliation Email"
							cssClass="textfield" />
						<s:fielderror fieldName="orderBean.affiliationEmail"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="orderBean.affiliationEmail" />
					</s:else>
				</div>
				<div class="form-field full-length">
					<label for="orderBean.affiliationSpecialInstructions" class="required">
						Affiliation Special Instructions: 
						<s:if test="isOrderEditable">
						<span class="required">* </span>
						</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield name="orderBean.affiliationSpecialInstructions" label="Affiliation Special Instructions"
							cssClass="textfield" />
						<s:fielderror fieldName="orderBean.affiliationSpecialInstructions"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="orderBean.affiliationSpecialInstructions" />
					</s:else>
				</div>
				<div class="form-field full-length">

					<label for="addressBean.address1" class="required">Address
						1: 
						<s:if test="isOrderEditable">
						<span class="required">* </span>
						</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield name="addressBean.address1" label="Address 1"
							cssClass="textfield" />
						<s:fielderror fieldName="addressBean.address1"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="addressBean.address1" />
					</s:else>

				</div>
				<div class="form-field full-length">

					<label for="addressBean.address2" class="required">Address
						2:</label>
					<s:if test="isOrderEditable">
						<s:textfield name="addressBean.address2" label="Address 2"
							theme="simple" cssClass="textfield" />
						<s:fielderror fieldName="addressBean.address2"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="addressBean.address2" />
					</s:else>
				</div>
				<div class="form-field full-length">

					<label for="addressBean.city" class="required">City: 
					<s:if test="isOrderEditable">
						<span class="required">* </span>
					</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield name="addressBean.city" label="City" theme="simple"
							cssClass="textfield" />
						<s:fielderror fieldName="addressBean.city"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="addressBean.city" />
					</s:else>
				</div>
				<div class="form-field full-length">

					<label for="state" class="required">State: 
					<s:if test="isOrderEditable">
						<span class="required">* </span>
					</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield id="state" name="addressBean.oldState" label="State" theme="simple" cssClass="textfield" />
						<s:fielderror fieldName="addressBean.oldState" cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="addressBean.oldState" />
					</s:else>
				</div>
				<div class="form-field full-length">

					<label for="zipCode" class="required">Zip: 
					<s:if test="isOrderEditable">
						<span class="required">* </span>
					</s:if>
					</label>
					<s:if test="isOrderEditable">
						<s:textfield id="addressBean.zipCode" name="addressBean.zipCode"
							label="Zip Code" theme="simple" cssClass="textfield" />
						<s:fielderror fieldName="addressBean.zipCode"
							cssClass="left error-message" />
					</s:if>
					<s:else>
						<s:property value="addressBean.zipCode" />
					</s:else>
				</div>
				<s:if test="isOrderEditable">


					<div id="boxBtns" class="button inline-right-button">
						<a href='<s:url action="openQueue" />'>Cancel</a>&nbsp;&nbsp;
						<button name="formSubmitStatusButton1" value="SAVE"
							onclick="saveAndExit()">Save and Exit</button>
						<button name="formSubmitStatusButton2" value="SUBMIT" onclick="saveAndSubmit()">Save and Submit</button>

					</div>
				</s:if>



				<hr>
		</div></form>

	</div>

</div>
<div></div>


<script type="text/javascript">

var editChangeComments = [];



/**
 * Copies the value from the text input field in the table to the hidden input
 * that keeps a single-page version of the data available so it can be submitted
 * along with the rest of the form.
 *
 * @attr input - DOM Element input on the page initiating the input
 * @attr id - ID of the hidden input to update
 */
function updateSampleQty(input, id) {
	var value = $(input).val();
	var hiddenInput = $("#" + id);
	hiddenInput.val(value);
}

saveAndSubmit = function(){
	var editChangeCommentsJSONString = JSON.stringify(editChangeComments);
	$('#form1').append('<input type="hidden" id="editChangeComments" name="editChangeComments" />');
	$('#form1').append('<input type="hidden" id="formSubmitStatus" name="formSubmitStatus" value="SUBMIT" />');
	$("#editChangeComments").val(editChangeCommentsJSONString);
	$("#form1").attr("action", "saveOrderValidation!saveOrder.action"); 
	 $('#form1').submit();
}

saveAndExit = function(){
	var editChangeCommentsJSONString = JSON.stringify(editChangeComments);
	$('#form1').append('<input type="hidden" id="editChangeComments" name="editChangeComments" />');
	$('#form1').append('<input type="hidden" id="formSubmitStatus" name="formSubmitStatus" value="SAVE" />');
	$("#editChangeComments").val(editChangeCommentsJSONString);
	$("#form1").attr("action", "saveDraftOrderValidation!saveOrder.action");
	 $('#form1').submit();
}

function submitBeforeDocumentation(action)
{
	$.ajax({
		type: "POST",
		cache: false,
		url: action,
		data: $("form").serializeArray(),
	});
}


function launchReasonForChangeDialog(isRemoveButton) {
	
	$( '#editExistingOrderReason' ).dialog({
		 open: function(event,ui) {
			 $("#rChange").val("");
		 },
		modal:true,
		buttons: [
		            {
		              text: "Ok",
		              click: function() {
		            	  //validate there is text
		            	  if($("#rChange").val().trim() != "" && $("#rChange").val().length < 1000) {
		            		  var comment = $("#rChange").val();
		            		  editChangeComments.push(comment);
		            		  
		            		  if(isRemoveButton) {
		            		  	handleRemoveClick();
		            		  }
			            	  $( this ).dialog( "close" );
		            	  }  
		              }
		            },
		            {
		            	 text: "Cancel",
			              click: function() {
			                $( this ).dialog( "close" );
			              }
		            }
		          ]
	});
	
	
}



function handleRemoveClick() {
	var selectedRemove = $("#activeOrderTable").idtApi('getSelectedOptions');
	
	var dlgId = $.ibisMessaging(
			"dialog", 
			"warning", 
			"Are you sure you want to remove the selected item(s) from your Order?",
			{
				id: 'deleteRows',
				container: 'body',
				buttons: [{
					id: "yesBtnA",
					text: "OK", 
					click: _.debounce(function() {
						$(this).siblings().find("#yesBtnA").prop("disabled", true);
					    $().removeItems(selectedRemove);
						$.ibisMessaging("close", {type: 'dialog'});				
					}, 1000, true)
				},
				{
					text: "Cancel",
					click: function() {
						$.ibisMessaging("close", {id: dlgId});					
					}
				}],
				modal: true,
				width: "400px",
				title: "Confirm Deletion"
			}
		);
}

jQuery.fn.extend({
	removeItems: function (selectedRemove) {
		var itemIds = [];
		for (var i = 0; i < selectedRemove.length; i++) {
			itemIds.push(selectedRemove[i]);
		}
		//alert("itemIds: "+JSON.stringify(itemIds));
		var table = $("#activeOrderTable").idtApi('getTableApi');
		$.post("orderManager!removeBiospecimenItemFromOrder.action", { itemToRemove : JSON.stringify(itemIds) },
			function(data) {
				table.rows('.selected').remove().draw(false);
				selectedRemove.length = 0;
				
		}); 
	},
	editNote:function() {
    	
    	$( "#recentNote" ).remove();
    	$('#notesList').scrollTop(0);
    	$( "#noteTemp" ).value = '';
    	$("#addNoteTextArea").show(500);
        
    },
    
//      token:$('[name="token"]').val() 
    removeUploadDocument: function(el,uploadFileName) {
    
    	var $m = $(el);
    	if(uploadFileName.length > 0) {
    		$.post(	"orderManager!removeFile.action", 
    			{ uploadFileName:uploadFileName,
 "async": 	false }, 
    				function(data) {  
    				
    				
    					$m.closest('tr').hide('slow', 
    								function(){  
    									$m.closest('tr').remove();
    								}
    					)
    				}
    		);
    		
//check to see if i need to make the upload link visible and hide message
    		
    		var rowCount = $('#documentTable tr').length;
    		
    		if(rowCount < 7 && !$("#uploadFileLink").is(":visible")) {
    			$("#limitMessage").hide("slow", function() {$("#uploadFileLink").show("slow");});
    		}
    	}
    },

    downloadDocument: function (fileId) { 
 	   
 	   
 	
 	   if(typeof fileId !== "undefined"){ 
        
        window.location.href = 'fileDownloadAction!download.action?fileId='+fileId;
       

 	   } else {
 		  alert('Please save order before attempting to download file.');
 		  return false;
 	   }
     
    }
});

/**********************************************/
$( document ).ready(function() {
       
	
	
		$( "#addNote" ).click(function( event ) {
        
        	event.preventDefault();
        	if( $( "#note" ).val() != '') {
        		$( "#noteTemp" ).value = $( "#note" ).val();
        		$("#notesList").prepend('<span id="recentNote" class="newNote">'+$( "#note" ).val()+'<div style="text-align:right; margin:5px;"><a href="javascript:void(0);" id="editNote" onClick="$().editNote();">Edit</a></div></span>');
        		$("#addNoteTextArea").hide(500);
        		$('#notesList').scrollTop(0);
        	}
       
            
        });
        
        
        
        $( "#clearNote" ).click(function( event ) {
        	event.preventDefault();
        	if( $( "#note" ).val() != '') {
        	 $( "#note" ).val('');
        	}
       
            
        });

    	var oTable = $('#queueTable').dataTable( {
    	    "bFilter": false,
    	    "sScrollX": "100%",
    	    "bInfo": false,
    	    "bPaginate": false
    	} );


    	var orderStatus = "<s:property value='currentOrder.orderStatus.value' />";
		$("#activeOrderTable").idtTable({
            idtUrl: "<s:url value='/ordermanager/orderManager!getOidBiosampleOrderTableList.ajax' />",
			idtData: {
				primaryKey: 'id'
			},
			dom: 'Bfrtip',
			select: "multi",
	        columns: [
	            {
	                name: 'coriellId',
	                title: 'SAMPLE ID',
	                parameter: 'coriellId',
	                data: 'coriellId'
	            },
	            {
	                name: 'bioRepositoryName',
	                title: 'REPOSITORY',
	                parameter: 'bioRepositoryName',
	                data: 'bioRepositoryName'
	            },
	            {
	                name: 'sampCollType',
	                title: 'SAMPLE TYPE',
	                parameter: 'sampCollType',
	                data: 'sampCollType'
	            },	      
	            {
	                name: 'guid',
	                title: 'Guid',
	                parameter: 'guid',
	                data: 'guid'
	            },
	            {
	                name: 'visitTypePDBP',
	                title: 'VISIT TYPE',
	                parameter: 'visitTypePDBP',
	                data: 'visitTypePDBP'
	            },
	            {
	            	name: 'inventory',
	            	title: 'INV',
	            	parameter: 'inventory',
	            	data: 'inventory'
	            },
	            {
	            	name: 'inventoryDate',
	            	title: 'INV DATE',
	            	parameter: 'inventoryDate',
	            	data: 'inventoryDate'
	            },
	            {
	            	name: 'adminQuantityInputField',
	            	title: 'QTY',
	            	parameter: 'adminQuantityInputField',
	            	data: 'adminQuantityInputField'
	            },
	            {
	            	name: 'unitNumber',
	            	title: 'UNIT #',
	            	parameter: 'unitNumber',
	            	data: 'unitNumber'
	            },
	            {
	            	name: 'unitMeasurement',
	            	title: 'UOM',
	            	parameter: 'unitMeasurement',
	            	data: 'unitMeasurement'
	            },
	            {
	            	name: 'diagnosis',
	            	title: 'DIAGNOSIS',
	            	parameter: 'diagnosis',
	            	data: 'diagnosis'
	            },
	            {
	            	name: 'caseControl',
	            	title: 'CASE/CONTROL',
	            	parameter: 'caseControl',
	            	data: 'caseControl'
	            },
	            {
	            	name: 'ageYrs',
	            	title: 'AGE',
	            	parameter: 'ageYrs',
	            	data: 'ageYrs'
	            }
	        ],
			buttons: [
				{
					text: 'Remove',
					className: 'idt-removeButton',
					enabled: false,
			 	 	action: function(e, dt, node, config) {
				 		
				 		if (orderStatus =='Created') {			  	  			
				 			handleRemoveClick();
				 		} else {
				 			launchReasonForChangeDialog(true);
				 		}
				   	} 
			 	}
			]
			
		});
});




//Sets the navigation menus on the page
setNavigation({
	"bodyClass" : "primary",
	"navigationLinkID" : "userManagementModuleLink",
	"subnavigationLinkID" : "userManagementToolLink",
	"tertiaryLinkID" : "changePasswordLink"
});

	

	
</script>







<div style="display:none" id="editExistingOrderReason">

	<div>
		<label>Reason for Change</label>
		<textarea rows="2" cols="35" id="rChange"></textarea>
	</div>
	


</div>