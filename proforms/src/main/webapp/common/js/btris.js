/**
 * 
 */

$(function() {

	$("input#mrn, input#lastName, input#firstName").keyup(function(){
		if(($.trim($("input#mrn").val()).length ==0) || 
				($.trim($("input#lastName").val()).length ==0) ||
				($.trim($("input#firstName").val()).length ==0)){
			
			$("input#btngetFromBTRIS").prop("disabled", true);
		} else {
			$("input#btngetFromBTRIS").prop("disabled", false);		
		}
		
	});
	
	$("input#mrnG, input#lastNameG, input#firstNameG").keyup(function(){
		if(($.trim($("input#mrnG").val()).length ==0) || 
				($.trim($("input#lastNameG").val()).length ==0) ||
				($.trim($("input#firstNameG").val()).length ==0)){
			
			$("input#btngetFromBTRISG").prop("disabled", true);
		} else {
			$("input#btngetFromBTRISG").prop("disabled", false);		
		}
		
	});
	
	
	$("input#btngetFromBTRIS").click(function(){

		var data = {
				mrn: $("input#mrn").val(),
				firstName: $("input#firstName").val(),
				lastName: $("input#lastName").val()
		}
	
		$.ajax({
			type: "POST",
			url: baseUrl+"/patient/getSubjectFromBTRIS!getSubjectFromBtris.action",
			datatype: "json",
			data: data,
			
			success: function(btrisData){
				var subjectFromBtris = JSON.parse(btrisData);
				$("#birthCity").val(subjectFromBtris.birthCity);
				$("#dob").val(subjectFromBtris.dob);
				$("#middleName").val(subjectFromBtris.middleName);
				$("#sex option").each(function() {
					if($.trim($(this).text()) == subjectFromBtris.sex){
						$(this).prop('selected',true);
					}
				});
				$("#editForm_patientForm_address1").val(subjectFromBtris.homeAddress1);
				$("#editForm_patientForm_address2").val(subjectFromBtris.homeAddress2);
				$("#editForm_patientForm_city").val(subjectFromBtris.city);
				$("#editForm_patientForm_zip").val(subjectFromBtris.zip);
				$("#editForm_patientForm_state").val(subjectFromBtris.state);
				$("#editForm_patientForm_country").val(subjectFromBtris.country);
				$("#birthCountry").val(subjectFromBtris.birthCountry);
				$("#editForm_patientForm_homePhone").val(subjectFromBtris.homePhone);
			},
			error: function(e){
				console.log('Error: '+e.error.errorMessage);
				$.ibisMessaging("primary", "error", 'Subject not found in BTRIS');
			}
		});
		
	});

	
	$("input#btngetFromBTRISG").click(function(){

		var data = {
				mrnG: $("input#mrnG").val(),
				firstNameG: $("input#firstNameG").val(),
				lastNameG: $("input#lastNameG").val()
		}
	
		$.ajax({
			type: "POST",
			url: baseUrl+"/patient/getGuidInfoFromBTRIS!getGuidInfoFromBtris.action",
			datatype: "json",
			data: data,
			
			success: function(btrisData){
				var subjectFromBtris = JSON.parse(btrisData);

				$("#guidClient-input-fn1").val(subjectFromBtris.guidClientInputFn1);
				$("#guidClient-input-fn2").val(subjectFromBtris.guidClientInputFn2);

				$("#guidClient-input-SUBJECTHASMIDDLENAME1").val(subjectFromBtris.guidClientInputSUBJECTHASMIDDLENAME1);
				$("#guidClient-input-SUBJECTHASMIDDLENAME2").val(subjectFromBtris.guidClientInputSUBJECTHASMIDDLENAME1);
				
				$("#guidClient-input-mn1").val(subjectFromBtris.guidClientInputMn1);
				$("#guidClient-input-mn2").val(subjectFromBtris.guidClientInputMn2);
				
				$("#guidClient-input-ln1").val(subjectFromBtris.guidClientInputLn1);
				$("#guidClient-input-ln2").val(subjectFromBtris.guidClientInputLn2);

				$("#guidClient-input-dob1").val(subjectFromBtris.guidClientInputDob1);
				$("#guidClient-input-dob2").val(subjectFromBtris.guidClientInputDob2);
				
				$("#guidClient-input-mob1").val(subjectFromBtris.guidClientInputMob1);
				$("#guidClient-input-mob2").val(subjectFromBtris.guidClientInputMob2);
				
				$("#guidClient-input-yob1").val(subjectFromBtris.guidClientInputYob1);
				$("#guidClient-input-yob2").val(subjectFromBtris.guidClientInputYob2);		
				
				$("#guidClient-input-cob1").val(subjectFromBtris.guidClientInputCob1);
				$("#guidClient-input-cob2").val(subjectFromBtris.guidClientInputCob2);
				
				$("#guidClient-input-cnob1").val(subjectFromBtris.guidClientInputCnob1);
				$("#guidClient-input-cnob2").val(subjectFromBtris.guidClientInputCnob2);

				$("#guidClient-input-sex1").val(subjectFromBtris.guidClientInputSex1);
				$("#guidClient-input-sex2").val(subjectFromBtris.guidClientInputSex2);

			},
			error: function(e){
				console.log('Error: '+e.error.errorMessage);
				$.ibisMessaging("primary", "error", 'GUID Info not found in BTRIS');
			}
		});
		
	});
	
	jQuery(document).ready(function(){
	 jQuery('#guidClient-eula-acceptButton').on('click', function(event) {        
	     jQuery('#guidBtrisDataSearchDiv').toggle('show');
	 });
	});

});