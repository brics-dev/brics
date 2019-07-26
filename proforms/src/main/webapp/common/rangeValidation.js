/** Define the javascript object
*/
function RangeValidatorObj () {
    var id;
    var name;
    var required;
    var operator;
    var range1;
    var range2;
}
//This JS range validation is moved to server side by yogi on 5/10/2013
function confirmLockValidateRange (markAsCompletedCheckBoxStatus) {

    var msg = checkRanges(markAsCompletedCheckBoxStatus);
    if (msg != "") {
        // errors prompt for confirm
        document.getElementById('valuesOutsideRange').value='true';
        //alert(msg);
        
        var target=document.getElementById('dcTitle');
        var newdiv;
        if(document.getElementById('validationErrors')){
        	newdiv=document.getElementById('validationErrors');
        }else{
        	newdiv = document.createElement('div');
	        newdiv.setAttribute('id','validationErrors');
        }
        newdiv.innerHTML =msg;
        $('#dcTitle').before(newdiv);
        return false;
    } else {
        return true;

    }
}

/**
 * checkRanges
 * This method relies upon the existance of an array , possibly empty,
 * of RangeValidatorObj Objects.  This array is generated in the
 * forwarding action class and printed onto the including page of
 * this file.
 * each object is used to check for compliance with the pre defined
 * normal range of values
 * each descrepancy is noted in string form
 * RETURNS : String of error messages , possibly empty string
 */
function checkRanges (markAsCompletedCheckBoxStatus) {
// assume that an array named theObjs was printed via jsp
    var errorMsg = "";
   // if(markAsCompletedCheckBoxStatus){
	    var rangeViolatorIds = "";
	    for (i = 0; i < theObjs.length; i++) {
	        anObj = theObjs[i];
	        elementId="S_" +  anObj.section + "_Q_"+anObj.id;
	        theElem = document.getElementById(elementId);
	       /* if (anObj.required == 'false' && theElem.value == "") {*/
	        	 if ( theElem.value == "") {
	            // if its not required, and no value entered, ignore it
	            // validation will catch then range can be checked
	            continue;
	        }
	        if ((aMsg = violatesRange(anObj, theElem)) != "") {
	            rangeViolatorIds += anObj.id + "abc";
	            errorMsg += aMsg;
	        }
	
	    }
	    if (errorMsg != "") {
	        document.getElementById('rangeViolators').value = rangeViolatorIds;
	        /*errorMsg = "Data entered falls outside defined normal ranges for the following "+
	                    "questions \n\n" + errorMsg;
	        errorMsg += "\n\nPlease correct the value";*/
	        errorMsg = "<span style='color: red'>Data entered falls outside defined normal ranges for the following questions</span><ul> "+errorMsg;
	        errorMsg += "</ul>Please correct the value";
	
	
	    }
   // }
    return errorMsg;
}

function violatesRange (validatorObject, element ) {
    var msg = "";
     // value is not "", needs check
        if (validatorObject.operator == 1) {
            // is equal To
            msg += checkEqualTo(validatorObject, element.value);
        } else if (validatorObject.operator == 2) {
            // less than
            msg += checkLessThan (validatorObject, element.value);
        } else if (validatorObject.operator == 3) {
            msg += checkGreaterThan (validatorObject, element.value);
        } else {  // operator is 4
            msg += checkBetween (validatorObject, element.value);
        }
    return msg;

}

function checkEqualTo (anObj, value) {
	if(isNaN(value)){
		return "<li class='badMessage'>Question " + anObj.text + " is not numeric <a href=\"Javascript:document.getElementById ('S_"+anObj.section+"_Q_" +anObj.id+ "').focus();\">Go To</a></il>";
	}else if (parseFloat (anObj.range1) != parseFloat (value)) {
        /*return "Question " + anObj.text + " does not equal "+anObj.range1 +"\n";*/
    	return "<li class='badMessage'>Question " + anObj.text + " does not equal "+anObj.range1 +".  <a href=\"Javascript:document.getElementById ('S_"+anObj.section+"_Q_" +anObj.id+ "').focus();\">Go To</a></il>";
    } else {
        return "";
    }
}

function checkLessThan (anObj, value) {
    if(isNaN(value)){
    	return "<li class='badMessage'>Question " + anObj.text + " is not numeric <a href=\"Javascript:document.getElementById ('S_"+anObj.section+"_Q_" +anObj.id+ "').focus();\">Go To</a></il>";
    }else if (parseFloat (anObj.range1) <= parseFloat (value) || value=='') {
        return "<li class='badMessage'>Question "+anObj.text + " is not less than "+anObj.range1 +".  <a href=\"Javascript:document.getElementById ('S_"+anObj.section+"_Q_" +anObj.id+ "').focus();\">Go To</a></il>";
    } else {
        return "";
    }
}

function checkGreaterThan (anObj, value) {
	if(isNaN(value)){
		return "<li class='badMessage'>Question " + anObj.text + " is not numeric <a href=\"Javascript:document.getElementById ('S_"+anObj.section+"_Q_" +anObj.id+ "').focus();\">Go To</a></il>";
	}else if (parseFloat (anObj.range1) >= parseFloat (value) || value=='') {
        return "<li class='badMessage'>Question "+ anObj.text + " is not greater than "+anObj.range1+".  <a href=\"Javascript:document.getElementById ('S_"+anObj.section+"_Q_" +anObj.id+ "').focus();\">Go To</a></il>";
    } else {
        return "";
    }
}

function checkBetween (anObj, value) {
    if(isNaN(value)){
    	return "<li class='badMessage'>Question " + anObj.text + " is not numeric <a href=\"Javascript:document.getElementById ('S_"+anObj.section+"_Q_" +anObj.id+ "').focus();\">Go To</a></il>";
    }else if (parseFloat (anObj.range1) <= parseFloat(value)
        && parseFloat(anObj.range2) >= parseFloat(value)) {
        return "";
    } else {
        return "<li class='badMessage'>Question "+anObj.text +" is not between " + anObj.range1 + " and " + anObj.range2 +".  <a href=\"Javascript:document.getElementById ('S_"+anObj.section+"_Q_" +anObj.id+ "').focus();\">Go To</a></il>";
    }
}
