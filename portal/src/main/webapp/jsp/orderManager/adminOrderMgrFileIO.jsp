<%@include file="/common/taglibs.jsp"%>
	
	<div class="lightbox-content-wrapper" >	
		<s:form id="theForm" name="theForm" class="validate" action="adminOrderAction!upload.ajax" method="post"
				enctype="multipart/form-data">
				<s:token />
				<h3>File Upload</h3>
				<div class="form-field textfield-other">
					<label for="new_file_upload">File:</label>
					<s:file name="upload" id="upload" cssClass="textfield float-left" />
				</div>
				
		</s:form>
		<div class="form-field">
					<div class="button">
						<input id="submitFile" type="button" class="submit" value="Submit"></input>
					</div>
				</div>
	</div>
	

<script type="text/javascript">
	function addDocument(){
		var formData = $("#theForm").serialize();
		//document.getElementById('theForm').submit();
		$.ajax("fileOrder!upload.ajax", {
			"type": 	"POST",
			"async": 	false,
			"data": formData,
		});
// 		theForm.action = 'fileOrder!upload.action';
// 		theForm.submit();
	}
	
	
	$(document).ready(function()
			{
			    $("#submitFile").click(function()
			    {
			       if($('#upload').val()==='')
			       {
			        	alert("Please select a file.");
			    	    return false;
			       }
			    	
			    	$('#theForm').append('<input type="hidden" id="orderTitle" name="orderTitle" value="'+$('#orderTitle').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="orderBean.abstractText" name="orderBean.abstractText" value="'+$('#abstract').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="orderBean.experimentalDesignPowerAnalysis" name="orderBean.experimentalDesignPowerAnalysis" value="'+$('#experimentalDesign').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="orderComment" name="orderComment" value="'+$('[name="orderComment"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="orderBean.shipToName" name="orderBean.shipToName" value="'+$('[name="orderBean\\.shipToName"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="orderBean.shipToInstitution" name="orderBean.shipToInstitution" value="'+$('[name="orderBean\\.shipToInstitution"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="orderBean.phone" name="orderBean.phone" value="'+$('[name="orderBean\\.phone"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="orderBean.affiliation" name="orderBean.affiliation" value="'+$('[name="orderBean\\.affiliation"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="orderBean.affiliationPhone" name="orderBean.affiliationPhone" value="'+$('[name="orderBean\\.affiliationPhone"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="orderBean.affiliationEmail" name="orderBean.affiliationEmail" value="'+$('[name="orderBean\\.affiliationEmail"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="orderBean.affiliationSpecialInstructions" name="orderBean.affiliationSpecialInstructions" value="'+$('[name="orderBean\\.affiliationSpecialInstructions"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="addressBean.address1" name="addressBean.address1" value="'+$('[name="addressBean\\.address1"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="addressBean.address2" name="addressBean.address2" value="'+$('[name="addressBean\\.address2"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="addressBean.city" name="addressBean.city" value="'+$('[name="addressBean\\.city"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="addressBean.oldState" name="addressBean.oldState" value="'+$('[name="addressBean\\.oldState"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="addressBean.zipCode" name="addressBean.zipCode" value="'+$('[name="addressBean\\.zipCode"]').val()+'" />');
			    	$('#theForm').append('<input type="hidden" id="formSubmitStatus" name="formSubmitStatus" value="'+$('#formSubmitStatus').val()+'" />');
			    	
			    	$('.itemNum').each(function(i, obj) {
			    	    $('#theForm').append('<input type="hidden" name="'+$(this).attr('name')+'" value="'+$(this).val()+'" />');
			    	});
		        		
			    
			    	
			    	
			    	$('#theForm').submit();
			 
			    });
			   
			 
			   
			});

	
	
</script>