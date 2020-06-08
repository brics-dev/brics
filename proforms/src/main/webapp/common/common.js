/***********************************
    Global JavaScript Functions
************************************/

/*********************************************
 * popupCalendar(dateField)
 *
 * Opens a new window for the Calendar
 * and the date you choose will be displayed
 * in the dateField.
 *********************************************/
 function popupCalendar(webroot, dateField)
 {
    w = openPopup(webroot + "/common/popUpCalendar.html","calendar","width=200,height=230");
    w.moveTo(window.outerWidth/2-100,window.outerHeight/2-100);
    var d = w.document;
    d.newVar = dateField;
    d.close();
 }

/****************************
 * popupWindow(url)
 *
 * Opens a new window with the
 * provided URL.
 ****************************/
function popupWindow(url)
{
    var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
    openPopup(url, "CtdbLink", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=600,height=400");
}

function popupWideWindow(url)
{
    var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
    openPopup(url, "CtdbLink", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=900,height=400");
}
/****************************
 * popupWindowWithMenu(url)
 *
 * Opens a new window with the
 * provided URL.
 ****************************/
function popupWindowWithMenu(url)
{
    var WindowArgs = "menubar=yes," + "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
    openPopup(url, "CtdbLink", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=600,height=400");
}

function popupWindowWithMenuLarge(url) {
     var WindowArgs = "menubar=yes," + "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
     var tehWin = openPopup(url, "CtdbLink", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=800,height=600");
     return tehWin;
}

function popupPdaViewWindow (url) {
    var WindowArgs = "menubar=no," + "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
    var tehWin = openPopup(url, "CtdbLink", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=no,width=300,height=350");
}

function popupLabelWindow(url)
{
    var WindowArgs = "toolbar=no," + "location=no," + "directories=no," + "status=yes,";
    openPopup(url, "CTK_Label_Printer", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=470,height=270");
}

/****************************
 * popupWindowWithMenuToobar(url)
 *
 * Opens a new window with the
 * provided URL.
 ****************************/
function popupWindowWithMenuToolbar(url)
{
    var WindowArgs = "menubar=yes," + "toolbar=yes," + "location=no," + "directories=no," + "status=yes,";
    openPopup(url, "CtdbLink", WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=600,height=400");
}

/****************************
 * popupWindowWithMenuToobar(url)
 *
 * Opens a new window with the
 * provided URL.
 ****************************/
function popupWindowWithMenuLocationToolbar(url,title)
{
    var WindowArgs = "menubar=yes," + "toolbar=yes," + "location=yes," + "directories=no," + "status=yes,";
    openPopup(url, title, WindowArgs+"top=10,left=10,scrollbars=yes,resizable=yes,width=600,height=400");
}

var toggle = "ALL";

/****************************
 * confirmDelete(url, item)
 *
 * Prompts user for a confirmation
 * before attempting to delete
 ****************************/
function confirmDelete(url, item)
{
    ok = confirm("Are you sure you want to remove this " + item + "?");
    if (ok == true)
    {
        window.location = url;
    }
}

/****************************
 * confirmAction(url, action)
 *
 * Prompts user for a confirmation
 * before attempting to delete
 ****************************/
function confirmAction(url, action)
{
    ok = confirm(action);
    if (ok == true)
    {
        window.location = url;
    }
}

/****************************
 * confirmAdmin(url, item, sysadmin)
 *
 * Prompts user for a confirmation
 * before attempting to add or remove
 * admin privileges to a user
 ****************************/
function confirmAdmin(url, item, sysadmin)
{
    if(sysadmin == 'false')
    {
        ok = confirm("Are you sure you want to remove System Administrator Privileges from " + item + "?");
        if (ok == true)
        {
            window.location = url;
        }
    }
    else
    {
        ok = confirm("Are you sure you want to add System Administrator Privileges to " + item + "?");
        if (ok == true)
        {
            window.location = url;
        }
    }
}

/****************************
 * selectAll(thisForm)
 *
 * Selects all checkboxes of
 * the passed in form.
 ****************************/
function selectAll(thisForm)
{
    for(var i=0; i < thisForm.elements.length; i++)
    {
        var el = thisForm.elements[i];
        if(toggle == 'ALL')
            el.checked = true;
        else
            el.checked = false;
    }

    toggle = toggle == 'ALL' ? "NONE" : "ALL";
}

/****************************
 * selectAll(thisForm)
 *
 * Selects all options of
 * the passed in form select object.
 ****************************/
function selectAllOptions(obj)
{
    for(var i = 0; i < obj.options.length; i++)
    {
        obj.options[i].selected = true;
    }
}

/****************************
 * unselect all(select)
 *
 * Selects all options of
 * the passed in form select object.
 ****************************/
function unselectAll(obj)
{
    for(var i = 0; i < obj.options.length; i++)
    {
        obj.options[i].selected = false;
    }
}

/****************************
select default
 *
 * literally selects 'default'
 * the passed in form select object.
 ****************************/
function selectDefault(obj)
{
    for(var i = 0; i < obj.options.length; i++)
    {
       if (obj.options[i].text =='default') {
        obj.options[i].selected = true;
        }
    }
}

/****************************
 * sortItems(obj)
 *
 * Sorts items in a select
 * box.
 ****************************/
function sortItems(obj)
{   var temp_opts = new Array();
    var temp = new Object();
    for (var i=0; i<obj.options.length; i++)
        temp_opts[i] = obj.options[i];

    for (var x=0; x<temp_opts.length-1; x++)
    {   for (var y = (x+1); y < temp_opts.length; y++)
        {   if(temp_opts[x].text > temp_opts[y].text)
            {   temp = temp_opts[x].text;
                temp_opts[x].text = temp_opts[y].text;
                temp_opts[y].text = temp;
                temp = temp_opts[x].value;
                temp_opts[x].value = temp_opts[y].value;
                temp_opts[y].value = temp;
            }
        }
    }

    for (var i=0; i<obj.options.length; i++)
    {   obj.options[i].value = temp_opts[i].value;
        obj.options[i].text = temp_opts[i].text;
    }
} // end function sortItems()

function scoreValidation(obj)
{
	var a=0;
	var b=0;

    for (var i=0; i<obj.options.length; i++)
    {
    	var codeValue = new Array();
        codeValue = obj.options[i].value.split('|');
        alert("codeValue= " + codeValue);
        if (codeValue.length > 2 && codeValue[2].trim()!="") {
        	b=1;
        }
        else{a=1;}
    }

    if(a==b){alert("Please enter the score for all of answers, or none of them.");return false;}
    else{return true;}
}


/*******************************************
 * transferItem(source, destination, sort)
 *
 * Transfers items from one select
 * box to a different text box. With
 * an optional sort flag.
 *******************************************/
function transferItem(source, destination, sort)
{   var i = 0;
    var newitem;
    var retval = false;

    while (i < source.options.length)
    {   if (source.options[i].selected)
        {
	        var value=source.options[i].value;
	        var duplicate = false;
			for (j=0; j < destination.options.length; j++)
			{
				if (destination.options[j].value == value)
				{ // its a duplicate, dont add it
					duplicate = true;
				}
			}
			if ( ! duplicate)
			{
	        	newitem = new Option(source.options[i].text, source.options[i].value, 0, 0);
            	destination.options[destination.options.length] = newitem;
            	retval = true;
            	source.options[i] = null;
            }
            else
            {
	            source.options[i] = null;
	            i = i+1;
            }
        }
        else
            i = i + 1;
    }

    // Sort items
    if(sort == "YES")
        sortItems(destination);
    else
        sortItems(source);

    return retval;
} // enf function transferItem()


function clearSelect( selObj) {
    i = selObj.options.length;

    while (i >= 0) {
        selObj.options[i] = null;
        i--;
    }
}

/*******************************************
 * transferItem(source, sort)
 *
 * Removes an item from a select box
 * With an optional sort flag.
 *******************************************/
function removeItem(source, sort)
{   var i = 0;
    var newitem;
    var retval = false;
    while (i < source.options.length)
    {   
    	if (source.options[i].selected)
        {
            source.options[i] = null;
        }
        else{
            i = i + 1;
        }
    }

    // Sort items
    if(sort == "YES")
        sortItems(source);

    return retval;
} // enf function transferItem()

function removeItemMacOpt(source, sort)
{   var i = 0;
    var newitem;
    var retval = false;

    while (i < source.options.length)
    {   if (source.options[i].selected)
        {
            source.options[i] = null;
        }
        else
            i = i + 1;
    }

    // Sort items
    if(sort == "YES"){
        sortItems(source);
    }
} // enf function transferItem()

/*******************************************
 * transferTextItemToSelect(source1, source2, destination, sort)
 *
 * Transfers items from one or two input
 * boxes to a different select box. With
 * an optional sort flag.
 *******************************************/
function transferTextItemToSelect(source1, source2, destination, sort)
{   
	var i = 0;
    var tmpitem;
    var newitem;
    var retval = false;
    var value1=trimStr(source1.value);

    if(value1 == "")
    {
        //alert("Please enter a value to move.");
    	alert("Please enter a Option to move.");
        return false;
    }

    var value2 = trimStr(source2.value + "");
  
    if (value2 == null || value2=="")
    {
        tmpitem = value1;
    }
    else
    {
        tmpitem = value1 + " | " + value2;
    }
    
    var duplicate = false;
    for (j=0; j < destination.options.length; j++)
    {	
        if (destination.options[j].value == tmpitem) // a | 1 == a | 1
        { // its a duplicate, dont add it
            duplicate = true;
        }
        if(destination.options[j].value.split('|')[0]==tmpitem.split('|')[0]){ // a  == a
        	// its a duplicate, dont add it
            duplicate = true;
        }
    }

    if (!duplicate)
    {
        newitem = new Option(tmpitem, tmpitem, 0, 0);
        destination.options[destination.options.length] = newitem;
        retval = true;
        source1.value = "";
        source2.value = "";
    }
    else
    {
        source1.value = "";
        source2.value = "";
        i = i+1;
    }

    // Sort items
    if(sort == "YES")
        sortItems(destination);

    return retval;
} // enf function transferItem()

/*******************************************
 * transferTextItemToSelect(source1, source2, source3, destination, sort)
 * source3 should be a number.
 * Transfers items from one or two input
 * boxes to a different select box. With
 * an optional sort flag.
 *******************************************/
function transferTextItemToSelect(source1, source2, source3, destination, sort)
{
	var i = 0;
    var tmpitem;
    var newitem;
    var retval = false;
    var value1;
    if(source1.value=='undefined'){
    	value1='undefined'; // keep the "undefined" word 
    } else {
    	value1=trimStr(source1.value);
    }
	
    if(value1 == "")
    {
    	alert("Please enter a Option to move.");
        return false;
    }

    var value2;
    if(source2.value=='undefined'){
    	value2='undefined'; // keep the "undefined" word 
    } else {
    	value2=trimStr(source2.value);
    }
    
    if(source3.value == 'undefined')
    {
        alert ("Not numeric.  Score is blank or not numeric or cause division by 0.");
        source3.value="";
        return false;
    }
    var value3 = trimStr(source3.value + ""); 
    if( !isFinite(value3))
    {
        alert ("Not numeric.  Score is blank or not numeric or cause division by 0.");
        source3.value="";
        return false;
    }

    if (value2 == null || value2=="")
    {
        tmpitem = value1;
	    if (!(value3 == null && value3 == ""))
	    {
	      tmpitem = value1 + " | " + " " + " | " + value3;
	    }
    }
    else
    {
        tmpitem = value1 + " | " + value2;
        if (!(value3 == null && value3 == ""))
        {
          tmpitem = value1 + " | " + value2 + " | " + value3;
        }
    }
    
    var duplicate = false;
    for (j=0; j < destination.options.length; j++)
    {
        if (destination.options[j].value == tmpitem)
        { // its a duplicate, dont add it
            duplicate = true;
        }
    }

    if (!duplicate)
    {
      
    	newitem = new Option(tmpitem, tmpitem, 0, 0); //TODO:
    	destination.options[destination.options.length] = newitem;
        retval = true;
        source1.value = "";
        source2.value = "";
        source3.value = "";
    }
    else
    {
        source1.value = "";
        source2.value = "";
        source3.value = "";
        i = i+1;
    }

    // Sort items
    if(sort == "YES")
        sortItems(destination);

    return retval;
} // enf function transferItem()

/*******************************************
 * transferTextItemToSelect(source1, source2, source3, destination, sort)
 * source3 should be a number.
 * Transfers items from one or two input
 * boxes to a different select box. With
 * an optional sort flag.
 *******************************************/
function transferTextItemToSelectX(source1, source2, source3, destination, sort)
{
	var i = 0;
    var tmpitem;
    var newitem;
    var retval = false;
    var value1;
	
    if (source1.value == 'undefined') {
    	value1 = ""; 
    } else {
    	value1 = trimStr(source1.value);
    }
	
    if(value1 == "")
    {
    	alert("Please enter a Option to move.");
        return false;
    }
    
    var value2;
    
    if (source2.value == 'undefined') {
    	 alert ("Not numeric.  Score is blank or not numeric or cause division by 0.");
         source2.value="";
		 
         return false;
    } else {
    	value2 = trimStr(source2.value);
    	
		if( !isFinite(value2))
		{
			alert ("Not numeric.  Score is blank or not numeric or cause division by 0.");
			source2.value="";
			
			return false;
		}
    }
    
    var value3;
    if (source3.value == 'undefined'){
    	value3 = "";
    } else {
    	value3 = trimStr(source3.value);
    }
    
    if (value2 == null) {
    	value2 = "";
    }
    
    if (value3 == null) {
    	value3 = "";
    }
    
    tmpitem = value1 + " | " + value2 + " | " + value3;
    
    var duplicate = false;
    for (j=0; j < destination.options.length; j++)
    {
        if (destination.options[j].value == tmpitem)
        { // its a duplicate, dont add it
            duplicate = true;
        }
    }

    if (!duplicate)
    {
    	newitem = new Option(tmpitem, tmpitem, 0, 0); //TODO:
        destination.options[destination.options.length] = newitem;
        retval = true;
        source1.value = "";
        source2.value = "";
        source3.value = "";
    }
    else
    {
        source1.value = "";
        source2.value = "";
        source3.value = "";
        i = i+1;
    }

    // Sort items
    if(sort == "YES")
        sortItems(destination);

    return retval;
} // enf function transferItem()

/*******************************************
 * swapItm(src, dest)
 *
 * Swaps items up and down in a select box.
 *******************************************/
function swapItem(src, dest)
{
	var selectedOpt = $(src).find("option:selected");
	
	// Make sure that only one option is selected from the select box
	if ( selectedOpt.length > 1 )
	{
		alert("Please selection only one option.");
		return;
	}
	else if (selectedOpt.length == 0 ) // Check if no selected option is found
	{
		alert("Please select one of the available options.");
		return;
	}
	
	if( dest == "UP" )
	{
		selectedOpt.prev().before(selectedOpt);
	}
	else
	{
		selectedOpt.next().after(selectedOpt);
	}
} // end function swapItem()

//***************** Image Swapping functions for Navigation
// generated js
function MM_swapImgRestore() { //v3.0
  var i,x,a = document.MM_sr; for(i=0; a &&i < a.length && (x=a[i]) && x.oSrc; i++) x.src = x.oSrc;
}

function MM_preloadImages() { //v3.0
  var d = document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_findObj(n, d) { //v4.01
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && d.getElementById) x=d.getElementById(n); return x;
}

function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}
//***************** END image swapping functions for navigation

// Function trims white space from strings
// param str: string to be trimmed
function trimStr(str)
{   var resultStr = "";
    resultStr = trimLeft(str);
    resultStr = trimRight(resultStr);
    return resultStr;
} // end function trimStr()

//trimStr(str) helper functions
function trimLeft(str)
{
    var resultStr = "";
    var i = len = 0;

    if (str+"" == "undefined" || str == null)
        return null;

    str += "";

    if (str.length == 0)
        resultStr = "";
    else
    {
        len = str.length;

        while ((i <= len) && (str.charAt(i) == " "))
            i++;
        resultStr = str.substring(i, len);
     }

     return resultStr;
} // end trimLeft

function trimRight(str)
{
    var resultStr = "";
    var i = 0;

    if (str+"" == "undefined" || str == null)
        return null;

    str += "";

    if (str.length == 0)
        resultStr = "";
    else
    {
          i = str.length - 1;
          while ((i >= 0) && (str.charAt(i) == " "))
             i--;

          resultStr = str.substring(0, i + 1);
    }

    return resultStr;
} // end trimRight



function jumpto(x) {

    window.location.href = window.location.href.replace(/\#\d*/, '') + "#" + x;

}

function calculate(inputbox, calculation, atype, decimalPrecision, conditionalForCalc,isCount) {

	
	var typestr = new String(atype);
    var dt = new String("datetime");
    var isDateTime = (typestr.toString() == dt.toString()) ;
    var deName = $('#'+inputbox.id).attr('deName');
   

    if (isDateTime) {
        alert("Calculation will be done at Save Progress or Lock if this is Date/Date-Time Calculated Question.");
        return;
    }

    var str = /\[S_([0-9]+)_Q_([0-9]+)\]/;

    count = 0;
    // only calcs that succeed will set a value, blank it now to trap all errors
    // but not for cdNRS PROMISRawScore
    if(deName !='PROMISRawScore'){
    	inputbox.value="";
    }
    $('#'+inputbox.id).attr("readonly", true).css('background-color', 'rgb(221, 221, 221)');
    while(ar = str.exec(calculation)) {
    	var box_data;
		var isSingleSelect = false;

		if(isCount == 'true') {
			if($("#S_"+ar[1]+"_Q_" + ar[2]).is(':radio') || $("#S_"+ar[1]+"_Q_" + ar[2]).is(':checkbox')) {
				//radio and checkbox questions
				box_data = trimStr($('input[name="S_'+ar[1]+'_Q_'+ar[2]+'"]:checked').val());

			}else {
				//need to handle case in which they are editing a collection in which they already added a file
				var elem = $("a[assoc_questionid='" + "S_" +ar[1]+"_Q_" + ar[2] + "'");
				if(elem.length && elem.attr("attachmentid") != '-2147483648') {
					box_data = 'file'; 
				}else {
					//all other cases
					box_data = trimStr(document.getElementById("S_"+ar[1]+"_Q_" + ar[2]).value);
				}	
			}
			if (box_data != null && box_data.length > 0) {
				box_data = '1';
			}else {
				box_data = '0';
			}
		}else {
			box_data = trimStr(document.getElementById("S_"+ar[1]+"_Q_" + ar[2]).value);

			//select or radio
			if (box_data != null && box_data.length > 0
					&& document.getElementById("S_"+ar[1]+"_Q_" + ar[2] +'_scoreBox')!=null
			)
			{	 
				isSingleSelect = true;
				box_data = trimStr($('input[name="S_'+ar[1]+'_Q_'+ar[2]+'"]:checked').val());
				if(box_data==null){
					box_data = trimStr($('#S_'+ar[1]+'_Q_'+ar[2]+' :selected').val());// e.g."blue"
				}

				// e.g. value="red|1|blue|2|green|3|"
				scoreBoxValue = trimStr(document.getElementById("S_"+ar[1]+"_Q_" + ar[2] +'_scoreBox').value);
				var scoreBoxValues = new Array();
				scoreBoxValues = scoreBoxValue.split('|');
				if (scoreBoxValues!=null && scoreBoxValues.length>0) 
				{ 
					for( var k=0; k<scoreBoxValues.length; k++)
					{
						if(box_data==scoreBoxValues[k].trim()) // e.g.
						// "blue"
						{
							box_data=scoreBoxValues[k+1]; // e.g. "2"

							// other,please specify has a score of
							// Intger.min. if user selects other please
							// specify and enters something, dont do
							// calculation
							if(box_data == "-2147483648") {
								return;
							}
							break;
						}
					}
				}				
			}

			if (conditionalForCalc=='true') {
				// 555 represents the permissible value code for single
				// select to indicate Do Not Calculate
				if (box_data == null || box_data == "" || (isSingleSelect && box_data == 555)) {
					if (deName =='PROMISRawScore') {
						$("input[dename='PROMISRawScore']").attr("readonly", false).css('background-color', 'white');
						var userEntry = $('#inputFalg_' + inputbox.id).val();

						if (userEntry == 'false') {
							inputbox.value = "";
						}
						set_tScore_SE();
					} else{
						inputbox.value="";
					}
					return;
				}
			}
		}


		if (box_data != null && box_data.length > 0) {
			var re = new RegExp("\\[S_"+ar[1]+"_Q_" + ar[2] + "\\]", "g");
			calculation = calculation.replace(re, box_data);
			count++;
		} else {
			var re = new RegExp("\\[S_"+ar[1]+"_Q_" + ar[2] + "\\]", "g");
			calculation = calculation.replace(re, 0 );
		}
        
    }

    if (count == 0) {
    	// We don't need alert here as per suggestion by Ben to address keyboard
		// navigation
    	inputbox.value = "";
        return;
    }

    calculation = calculation.replace(/sqrt\(\s*([\d\.]+)\s*\)/g, "Math.sqrt($1)");
    calculation = calculation.replace(/%/g, "*\(1/100\)*");

    var divbyzero = /\s\/\s0+\s+/g;
    if (teststr = divbyzero.exec(calculation + " ")) {
            alert("Submit answer can not be calculated because of division by zero.");
            return;
    }

    var result = eval(calculation);



    // round to x digits
    if (!isNaN(result) && (isFinite(result) || result == 0)) {

    	var digits = 10;
    	if(decimalPrecision != "-1") {
    		digits = decimalPrecision;
    		result = result.toFixed(digits);
    	}else {
    		result = (Math.round(result*Math.pow(10,digits))) / Math.pow(10,digits);
    	}


        inputbox.value=result;
        $('#inputFalg_'+inputbox.id).val('false');
    }
    set_tScore_SE();

    // if edit answer , add some reason
    try {

    	var $inputbox = $(inputbox);
    	var inputboxid = $inputbox.attr("id");
    	var inputboxlength = inputboxid.length;
        var qId = inputboxid.split("_")[1];
		
        jsResponses.get(qId).setChangeReason("CALCULATION_CHANGED");
    } catch (err) {
        console.error(err);
    }
}

function debug(x) {
    document.getElementById("debug").innerHTML = document.getElementById("debug").innerHTML + "<BR>" + x;
}

/****************************
 * calculate(inputbox, children, ctype)
 * This matches the business logic for calcuation implemented in the back-end code
 * in InputHandler.java
 *
 * Does calcuations and updates value of inputbox
 ****************************/
function calculateold(inputbox, children, ctype)
{
    if( children.length < 2 )
    {
        return;
    }

    // if first child does not have an answer put in and the calculation type is
    // difference or division, no answer is provided
    if( (ctype == 'Difference' || ctype == 'Division') &&
        (trimStr(document.getElementById(children[0]).value).length == 0) )
    {
        alert("This calculated question with calculation type " + ctype + " has no answer yet.  The first question used for calculation is blank.");
        inputbox.value="";
        return;
    }
    var denominator = 0;
    for( var i = 1; i < children.length; i++)
    {
        if (trimStr(document.getElementById(children[i]).value).length > 0)
        {
        	denominator++;
        }
    }

    if ((ctype == 'Division') && (denominator == 0))
    {
        alert("This calculated question with calculation type " + ctype + " has no answer yet.  The denominator used for calculation is blank.");
        inputbox.value="";
        return;
    }

    var result = 0;
    var count = 0; // count == 0 means result is not set

    var invalidCalculatedType = false;
    var isDateTime = false;
    var pat1 = /^(\d{1,2})(\/)(\d{1,2})(\/)(\d{2,4})$/;
    var pat2 = /^(\d{1,2})(\-)(\d{1,2})(\-)(\d{2,4})$/;
    var pat3 = /^(\d{1,2})(\-)(\d{1,2})(\-)(\d{2,4})(\-)(\d{1,2})(\-)(\d{1,2})(\-)(\d{1,2})$/;

    for( var i = 0; i < children.length; i++)
    {
        var values = getParameterValues(document.getElementById(children[i]), children[i]);
		
        // values is an array of strings after trimming
        for( var k = 0; k < values.length; k++)
        {
            isDateTime = pat1.test(values[k]) || pat2.test(values[k]) || pat3.test(values[k]);
            if( count == 0 )
            {
                // result not set yet
                result = Number(values[k]);
            }
            else
            {
                if( (ctype == 'Sum') || (ctype == 'Average') )
                {
                    result += Number(values[k]);
                }
                else if( ctype == 'Difference' )
                {
                    result -= Number(values[k]);
                }
                else if( ctype == 'Multiplication' )
                {
                    result *= Number(values[k]);
                }
                else if( ctype == 'Division' )
                {
                    result /= Number(values[k]);
                }
                else
                {
                    invalidCalculatedType = true;
                    break;
                }
            }
            count++;
        }
    }

    if( invalidCalculatedType )
    {
        alert("System Error: Invalid calculation type. ");
        inputbox.value="";
        return;
    }

    if( (count != 0) && (ctype == 'Average') )
    {
        result /= count;
    }

    if( count == 0 )
    {
        // all blank
        alert ("No value.  Answers used for calculation are blank.");
        inputbox.value="";
        return;
    }
    if (isNaN(result) && isDateTime)
    {
        if (inputbox.value.length <= 0)
        {
            alert("Calculation will be done at Save Progress or Lock if this is Date/Date-Time Calculated Question.");
            inputbox.value = "";
        }
        return;
    }
    else if( !isFinite(result) )
    {
        alert ("Not numeric.  Answers used for calculation are blank or not numeric or cause division by 0.");
        inputbox.value="";
        return;
    }

    inputbox.value=result;
    return;
}

/****************************
 * Returns an string array containing values of input boxes.
 * Similar to the HTTPServletRequest.getParameterValues().
 * The inputed string is trimmed first.  For multi select and checkboxes, the answer is not
 * trimmed, assuming options are non blank.
 *
 * @param object The object returned from getElementById.
 * @param strName The name of the objects for use with checkboxes.
 ****************************/
function getParameterValues(object, strName)
{
   var values = new Array();

   if(object.type == "select-multiple")
   {
       for(var i = 0; i < object.options.length; i++)
       {
           if(object.options[i].selected)
           {
               values[values.length] = object.options[i].value;
           }
       }
   }
   else if(object.type == "checkbox")
   {
       var objects = document.getElementsByName(strName);
       for( var i = 0; i < objects.length; i++)
       {
           if(objects[i].checked)
           {
               values[values.length] = objects[i].value;
           }
       }
   }
   else
   {
      // single answer
      var value = trimStr(object.value);
      if( value.length > 0 )
      {
          values[values.length] = value;
      }
   }
   return values;
}
/****************************
 * applyskipruleisblank(inputbox, children)
 *
 * apply skiprule of operator type of is blank on the children questions
 * when setting blank to answers to parent question during editing response
 * answers on the inputbox.  Only works on checkbox with skip rule of disable.
 ****************************/
function applyskipruleisblank(inputbox, children)
{
	for( var i = 0; i < children.length; i++)
	{
		var child = document.getElementById(children[i]);
		var reason = "reason_" + children[i].substr(3, children[i].length - 3);

		var reasonArea = document.getElementById(reason);
        if( child != null && reasonArea != null)
        {
        	if (inputbox.checked)
        	{
            	child.checked = true;
            	reasonArea.value="Skip Rule applies."
            }
        	else if (!inputbox.checked)
        	{
            	child.checked = false;
            	reasonArea.value="";
            }
        }
	}
}

/**
 * This method was added to clear skip rules when user double clickes the radio type of question
 * @param soption
 * @param children
 */
function  clearSkipRuleForDoubleClick(soption, children){
	 undoskip(soption, children);
}


/****************************
 * applyskiprule(inputbox, children, stype, soption, svalue)
 *
 * applyskiprule on the children questions based on the skip options (soption)
 * and skip values (svalue) check on the inputbox
 ****************************/
function applyskiprule(inputbox, children, stype, soption, svalue)
{
	// sValue may be delimited by |.  if it is, we need to split this
	var svalues = null;
	if(svalue.indexOf("|")>-1){
		svalues=svalue.split("|");
	}
	
	var inputbox_value = "";
    var hasMultipleValue = false;
   
    // get value
    var inputbox_name = inputbox.name;
    if( inputbox.type == 'radio' || inputbox.type == 'checkbox' )
    {
        inputboxes = document.getElementsByName(inputbox_name);
        var numSelected = 0;
        for( var i = 0; i < inputboxes.length; i++)
        {
            if( inputboxes[i].checked )
            {
                inputbox_value = inputboxes[i].value;
                numSelected ++;
            }
        }
        if( numSelected > 1 )
        {
            hasMultipleValue = true;
        }
    }
    else if (inputbox.type == 'select')
    {
        for(var i=0; i < inputbox.options.length; i++)
        {
            if( inputbox.options[i].selected )
            {
                inputbox_value = inputbox.options[i].value;
            }
        }
    }
    else if( inputbox.type=='select-multiple')
    {
        var numSelected = 0;
        for(var i=0; i < inputbox.options.length; i++)
        {
            if( inputbox.options[i].selected )
            {
                inputbox_value = inputbox.options[i].value;
                numSelected ++;
            }
        }
        if( numSelected > 1 )
        {
            hasMultipleValue = true;
        }
    }
    else
    {
        inputbox_value = trimStr(inputbox.value);
    }

    // apply rule
    if( stype == 'Equals' )
    {
    	if(svalues != null) {
    		for(var i=0;i<svalues.length;i++) {
    			//if( !hasMultipleValue && inputbox_value == trimStr(svalues[i]) )
    			if(inputbox_value == trimStr(svalues[i]) )
    	        {
    	            doskip(soption, children);
    	            break;
    	        }
    	        else
    	        {
    	            undoskip(soption, children);
    	        }
    		}
    	}else {
    		//if( !hasMultipleValue && inputbox_value == svalue )
    		if( inputbox_value == svalue )
            {
                doskip(soption, children);
            }
            else
            {
                undoskip(soption, children);
            }
    	}
    }
    else if(stype == 'Contains'){
    	var flag=false;
    	if(inputbox.type == 'select-multiple') {
    		smOuterloop:
    		for(var i=0; i < inputbox.options.length; i++)
	        {
	            if( inputbox.options[i].selected )
	            {
	            	if(svalues != null) {
	            		for(var j=0;j<svalues.length;j++) {
	            			if(inputbox.options[i].value.indexOf(trimStr(svalues[j]))>-1){
			                	flag=true;
			                	break smOuterloop;
			                }
	            		}
	            	}else {
	            		if(inputbox.options[i].value.indexOf(svalue)>-1){
		                	flag=true;
		                	break smOuterloop;
		                }
	            	}
	            	
	            }
	        }
    	} else if(inputbox.type == 'checkbox') {  
    		var flag=false;
            inputboxes = document.getElementsByName(inputbox_name);
            var numSelected = 0;
           
            cbOuterloop:
            for( var i = 0; i < inputboxes.length; i++)
            {
                if( inputboxes[i].checked )
                {
                	if(svalues != null) {
                		for(var j=0;j<svalues.length;j++) {
                			if(inputboxes[i].value.indexOf(trimStr(svalues[j]))>-1){
        	                	flag=true;
        	                	break cbOuterloop;
                        	}
                		}
                	}else {
                		if(inputboxes[i].value.indexOf(svalue)>-1){
    	                	flag=true;
    	                	break cbOuterloop;
                    	}
                	}
                	
                }
            }
    	} else{
    		// select,textbox,radio
    		if(svalues != null) {
    			for(var i=0;i<svalues.length;i++) {
    				if(  inputbox_value.indexOf(trimStr(svalues[i]))>-1 )
                    {
                    	flag=true;
                    	break;
                    }
    				
    			}
    		} else {
    			if(  inputbox_value.indexOf(svalue)>-1 )
                {
                	flag=true;
                }
    		}
    	}
        
        if( flag )
        {
            doskip(soption, children);
        }
        else
        {
            undoskip(soption, children);
        } 	
    }
    else if( stype == 'Has Any Value' || stype == 'Is Blank' )
    {
        var flag = (stype == 'Has Any Value');
        if(inputbox_value.length == 0)
        {
            flag = !flag;
        }

        if( flag )
        {
            doskip(soption, children);
        }
        else
        {
            undoskip(soption, children);
        }
    }
    else if(stype == 'Less than'){
    	
    	 var flag=false;
         if(inputbox_value && inputbox_value < svalue)
         {
             flag = true;
         }

         if( flag )
         {
             doskip(soption, children);
         }
         else
         {
             undoskip(soption, children);
         }
    }
    else if(stype == 'Less than equal to'){
    	
   	 var flag=false;
        if(inputbox_value && inputbox_value <= svalue)
        {
            flag = true;
        }

        if( flag )
        {
            doskip(soption, children);
        }
        else
        {
            undoskip(soption, children);
        }
   }
   else if(stype == 'Greater than'){
    	
      	 var flag=false;
           if(inputbox_value && inputbox_value > svalue)
           {
               flag = true;
           }

           if( flag )
           {
               doskip(soption, children);
           }
           else
           {
               undoskip(soption, children);
           }
      }
    else if(stype == 'Greater than equal to'){
    	
     	 var flag=false;
          if(inputbox_value && inputbox_value >= svalue)
          {
              flag = true;
          }

          if( flag )
          {
              doskip(soption, children);
          }
          else
          {
              undoskip(soption, children);
          }
     }
}

/****************************
 * doskip(soption, children)
 *
 * clears value and disables the children if necessary
 ****************************/
function doskip(soption, children)
{
    if( soption == 'Disable' )


    {
        for( var i = 0; i < children.length; i++)
        {

            var questionNumber = children[i];
            var child = document.getElementById(children[i]);
            if (child == null) {
                child = document.getElementById ('imageMap_'+children[i]);
            }
            if( child != null && (child.type == 'radio' || child.type == 'checkbox') )
            {
                childrenTmp = document.getElementsByName(children[i]);
                for(var j=0; j < childrenTmp.length; j++)
                {
                    var defaultValue = document.getElementById(questionNumber + '_default').value;

					childrenTmp[j].checked = false;
                    childrenTmp[j].disabled = true;
                }
            }
            if( child != null )
            {
                if( child.type == 'text' )
                {
                    child.value = '';
                }

                if (child.type == 'textarea') {
                    child.value = '';
                }

                else if (child.type == 'select' || child.type == 'select-one' || child.type == 'select-multiple')
                {
                var defaultValue = document.getElementById(questionNumber + '_default').value;
                    for(var j = 0; j < child.options.length; j++)
                    {
                        child.options[j].selected = false;
                    }
                }
				
                // may need to clear other types
                child.disabled = true;
            }
        }
    }
}

/****************************
 * undoskip(soption, children)
 *
 * enables children objects
 ****************************/
function undoskip(soption, children)
{
    if( soption == 'Disable' )
    {
        for( var i = 0; i < children.length; i++)
        {
            var child = document.getElementById(children[i]);
			
             if (child == null) {
                child = document.getElementById ('imageMap_'+children[i]);
            }
			
            if( child != null )
            {
                if (child.type == 'radio' || child.type == 'checkbox')
                {
                    childrenTmp = document.getElementsByName(children[i]);
                    for(var j = 0; j < childrenTmp.length; j++)
                    {
                        childrenTmp[j].disabled = false;
                    }
                }
                child.disabled = false;
            }
        }
    }
}

function applyAllSkipRules()
{
	applyTheSkipRules ("INPUT");
	applyTheSkipRules ("SELECT");
	applyTheSkipRules ("TEXTAREA");
}

function applyTheSkipRules (tagType)
{
	var elements = document.getElementsByTagName (tagType);

	for (var i = 0; i < elements.length; i++)
	{
		if (elements[i].onchange && elements[i].name.indexOf('Q_') >-1)
		{
			elements[i].onchange();
		}
	}
}

function applyAllSkipRulesEditPage()
{

	applyTheSkipRulesEditPage ("INPUT");
	applyTheSkipRulesEditPage ("SELECT");
	applyTheSkipRulesEditPage ("TEXTAREA");

}

function applyTheSkipRulesEditPage (tagType)
{
	// Edit page uses different id and name tag as the data entry page
	// For example, if the question id is 12, the final edit page uses
	// id="12" instead of id="Q_12" as on the data entry page.
	// So fire all onchange and do not check for Q_.
	var elements = document.getElementsByTagName (tagType);

	for (var i = 0; i < elements.length; i++)
	{
		if (elements[i].onchange )
		{
			elements[i].onchange();
		}
	}
}

function applyAllCalculations ()
{
	applyTheCalculations ('INPUT');
	applyTheCalculations ('SELECT');
	applyTheCalculations ('TEXTAREA');
}

function applyTheCalculations(tagType)
{
	var elements = document.getElementsByTagName (tagType);
	for (var i = 0; i < elements.length; i++)
	{
		if (elements[i].onclick && elements[i].name.indexOf('Q_') > -1)
		{
			elements[i].onclick();
		}
	}
}

function activedisableskiprule(inputbox, children, stype, soption, svalue)
{
    var inputbox_value = "";
    var hasMultipleValue = false;

    // get value
    var inputbox_name = inputbox.name;
    if( inputbox.type == 'radio' || inputbox.type == 'checkbox' )
    {
        inputboxes = document.getElementsByName(inputbox_name);
        var numSelected = 0;
        for( var i = 0; i < inputboxes.length; i++)
        {
            if( inputboxes[i].checked )
            {
                inputbox_value = inputboxes[i].value;
                numSelected ++;
            }
        }
        if( numSelected > 1 )
        {
            hasMultipleValue = true;
        }
    }
    else if (inputbox.type == 'select')
    {
        for(var i=0; i < inputbox.options.length; i++)
        {
            if( inputbox.options[i].selected )
            {
                inputbox_value = inputbox.options[i].value;
            }
        }
    }
    else if( inputbox.type=='select-multiple')
    {
        var numSelected = 0;
        for(var i=0; i < inputbox.options.length; i++)
        {
            if( inputbox.options[i].selected )
            {
                inputbox_value = inputbox.options[i].value;
                numSelected ++;
            }
        }
        if( numSelected > 1 )
        {
            hasMultipleValue = true;
        }
    }
    else
    {
        inputbox_value = trimStr(inputbox.value);
    }

    // apply rule
    if( stype == 'Equals' && soption == 'Disable')
    {
        if( !hasMultipleValue)
        {
            if (inputbox_value == svalue)
            {
                activeRequiredSkipQuestion(children);
            }
            else
            {
                disableRequiredSkipQuestion(children);
            }
        }
    }
}

function disableRequiredSkipQuestion(children)
{
    for( var i = 0; i < children.length; i++)
    {
        var questionNumber = children[i];
        document.getElementById("bk_" + questionNumber).checked = false;
        document.getElementById("bk_" + questionNumber).disabled = true;
    }
}

function activeRequiredSkipQuestion(children)
{
    for( var i = 0; i < children.length; i++)
    {
        var questionNumber = children[i];
        document.getElementById("bk_" + questionNumber).disabled = false;
    }
}

function unescapeErrors() {
    var errors = document.getElementsByTagName ('LI');
    for (j = 0; j < errors.length; j++) {
        var error = errors[j].innerHTML;
        var lt = error.indexOf ("&lt;");
		
        if (lt > -1) {
			var incorrectLtFound = true;
			var ii = 0;
			while (incorrectLtFound && ii < 15) {
				ii ++;
				str = error.substring (lt+4, lt+10);
				if (str == "a href") {
					incorrectLtFound = false;
				} else {
					lt += 4;
					tmpStr = error.substring (lt, error.length);
					lt += tmpStr.indexOf ("&lt;");
					str = error.substring (lt+4, lt+10);
				}
			}
			
			var strEnd = error.substring (lt + 4, error.length);
			var newStr = error.substring (0, lt);
			
			newStr += "<";
			newStr += strEnd.substring (0, strEnd.indexOf ("&gt;"));
			newStr += ">";
			newStr += strEnd.substring (strEnd.indexOf ("&gt;") + 4, strEnd.lastIndexOf ("&lt;"));
			newStr += "<";
			newStr += strEnd.substring (strEnd.lastIndexOf ("&lt;") +4, strEnd.lastIndexOf ("&gt;"));
			newStr += ">";

			newStr += strEnd.substring (strEnd.lastIndexOf ("&gt;") +4, strEnd.length);

			errors[j].innerHTML = newStr;
        }
    }

}
function oncancel()
{
window.close();
}

function checkparent()
{
	var openerHref = window.opener.location.href;

	if( openerHref != g_openerHrefOriginal )
	{
		window.close();
	}
	else if( window.opener.g_number != g_numberOriginal )
	{
		window.close();
	}
	else
	{
		window.setTimeout("checkparent()",1000);
	}
}

/*************************************************************
**  IMAGE MAP QUESTION METHODS
***/
function enterCoordinates (id, row, col, option) {
    selId = 'imageMap_' + id;
    targetSelect = document.getElementById (selId);
    if (targetSelect.disabled == true) {
        return;
    }
    //remove blank default value if any
    if (targetSelect.options.length > 0 && (targetSelect.options[0].text == "" || targetSelect.options[0].text == " ")) {
        targetSelect.options[0] = null;
    }
    var duplicate = false;
    for (i = 0; i < targetSelect.options.length; i++) {
        if (targetSelect.options[i].text == option) {
            duplicate = true;
        }
    }
    if (! duplicate) {
         o = new Option (option, option, 0, 0);
         targetSelect.options[targetSelect.options.length] = o;
    }
}

function updateGridLocation (sid,qid) {
    id = 'imageMapImage_S_'+sid+'_Q_'+qid;
    img = document.getElementById(id);
    id = 'imageMapGrid_S_'+sid+'_Q_'+qid;
    grid = document.getElementById(id);
    spacerId = "imageMapRef_S_"+sid+'_Q_'+qid;
    spacer = document.getElementById (spacerId);
    img.style.left = spacer.offsetLeft;
    grid.style.left = spacer.offsetLeft;


}

function doNothing() {   }

function toggleOverflow (elemId) {
    if (document.getElementById(elemId).style.overflow == 'auto') {
        document.getElementById(elemId).style.overflow = 'visible';
    }
    else {
        document.getElementById (elemId).style.overflow = 'auto';
    }
}

function toggleVisibility (elemId, clickedImgId) {
    try {
        setControlPosition ();  // THIS IS DEFINED IN TABS.JS
    } catch (err) {
    }
    if (document.getElementById(elemId).style.display == 'none') {
        document.getElementById(elemId).style.display = 'block';
        document.getElementById(clickedImgId).src = "../images/ctdbCollapse.gif";
        
    } else {
        document.getElementById(elemId).style.display = 'none';
       document.getElementById(clickedImgId).src = "../images/ctdbExpand.gif";
       
    }

}

function toggleTreeSelector (elemId, backId) {

    if (document.getElementById(elemId).style.display == 'none') {
        document.getElementById(elemId).style.display = 'block';
        document.getElementById(backId).height = document.getElementById(elemId).offsetHeight;
        document.getElementById(backId).style.width = document.getElementById(elemId).offsetWidth;
        document.getElementById(backId).style.top = document.getElementById(elemId).offsetTop;
        document.getElementById(backId).style.left = document.getElementById(elemId).offsetLeft;

        document.getElementById(backId).style.display = 'block';
    } else {
        document.getElementById(elemId).style.display = 'none';
        document.getElementById(backId).style.display = 'none';
    }


}

function growTextarea(elemId) {
    var width = document.getElementById(elemId).style.width;
    width = parseInt(width.substring (0, width.length -2));
    
	var height = document.getElementById(elemId).style.height;
    height = parseInt(height.substring (0, height.length -2));
	
    document.getElementById(elemId).style.width =  (width + 30) +"px";
    document.getElementById(elemId).style.height = (height + 15) + "px";
}

function shrinkTextarea(elemId) {
    var width = document.getElementById(elemId).style.width;
    width = parseInt(width.substring (0, width.length -2));
	
    var height = document.getElementById(elemId).style.height;
    height = parseInt(height.substring (0, height.length -2));
	
    document.getElementById(elemId).style.width =  (width - 30) +"px";
    document.getElementById(elemId).style.height = (height - 15) + "px";
}

/**
 * Toggles the radio button selection
 * @param element the element to toggle
 */
function toggleRadio(element)
{
    theSrc = element;
    var index = 0;
    radioGp = document.getElementsByName(theSrc.name);

    for (i = 0; i < radioGp.length; i++)
    {
        if (theSrc == radioGp[i])
        {
            index = i;
        }
    }
    var timeOutCmd = "uncheckRadio ('" + theSrc.name + "', '" + index + "');";

    if (theSrc.checked)
    {
        window.setTimeout(timeOutCmd, 150);
    }
}

/**
 * Unchecks a radio button
 * @param name the name of the element
 * @param index the index of the element
 */
function uncheckRadio(name, index)
{
    radioGp = document.getElementsByName(name);
    radioGp[index].checked = false;
    applyTheSkipRules ("INPUT");
}

/**
 * Function which adds toggle capabilities to radio buttons.
 */
function addEventToRadio()
{
    var allinputs = document.getElementsByTagName("input");
    for (var i = 0; i < allinputs.length; i++)
    {
        var element = allinputs[i];
        if (element.type == "radio")
        {
            //Add toggle capability to radio buttons only
            element.onmousedown = callToggleRadio;
        }
    }
}

/**
 * Wrapper function calling toggleRadio for the current element
 */
function callToggleRadio()
{
	// in IBIS, we don't want this to happen
}

/**
 * Adds an onload event to the window while preserving any existing onload functionality.
 * @param func the function to load
 */
function addLoadEvent(func)
{
    var oldonload = window.onload;
    if (typeof window.onload != 'function')
    {
        window.onload = func;
    }
    else
    {
        window.onload = function()
        {
            oldonload();
            func();
        }
    }
}

//Load event
addLoadEvent(addEventToRadio);

/**
 * Toggles the visibility of a field
 */
function showhidefield(source)
{
    if (document.getElementsByName(source)[0].checked)
    {
    	if (document.getElementById("hiddenarea").tagName == "TR") {
    		document.getElementById("hiddenarea").style.display = "table-row";
    	}
    	else {
    		document.getElementById("hiddenarea").style.display = "block";
    	}
        document.getElementsByName("subjectNumberStart")[0].value = "*****";
    }
    else
    {
        document.getElementById("hiddenarea").style.display = "none";
        document.getElementsByName("subjectNumberStart")[0].value = "";
        document.getElementsByName("subjectNumberPrefix")[0].value = "";
        document.getElementsByName("subjectNumberSuffix")[0].value = "";
    }
}

/**
 * Toggles the visibility of a field
 */
function showhidefieldSubjectNum(source)
{
    if (source == "autoIncrementSubjectShow")
    {
    	if (document.getElementById("hiddenarea").tagName == "TR") {
    		document.getElementById("hiddenarea").style.display = "table-row";
    	}
    	else {
    		document.getElementById("hiddenarea").style.display = "block";
    	}
        document.getElementsByName("subjectNumberStart")[0].value = "*****";
    }
    else if (source == "autoIncrementSubjectHide")
    {
        document.getElementById("hiddenarea").style.display = "none";
        document.getElementsByName("subjectNumberStart")[0].value = "";
        document.getElementsByName("subjectNumberPrefix")[0].value = "";
        document.getElementsByName("subjectNumberSuffix")[0].value = "";
    }
}

function URLDecode(psEncodeString) {
  // Create a regular expression to search all +s in the string
  var lsRegExp = /\+/g;
  // Return the decoded string
  return unescape(String(psEncodeString).replace(lsRegExp, " "));
}

//==============Ching-Heng
var assessmentId = "";
var currentCatOid;
var batteryAsmtIds = "";
var batteryAsmtItems = "";

function initiateCATs(){
	var formOID = $('#OID').val();
	var measurementType = $('#measurementType').val();
	getAsmtOID(formOID,"");
}

function getAsmtOID(formOID, postedData){
	if(assessmentId.length == 0){
		$.ajax({
		    url: baseUrl + "/ws/public/promis/getAsmtOID/" + formOID,
		    type: "GET",
		    dataType: "json",
			
		    success: function(data) {
//		    	console.log("data: "+JSON.stringify(data));
		    	assessmentId = data.OID;
		    	if(assessmentId != null){
		    		renderQuestion(assessmentId, postedData);
		    	} else { /*Battery form, there are multiple assessment ids for one battery form*/
		    		batteryAsmtIds = data.batteryAssessments;
		    		batteryAsmtItems = data.batteryAsmtItems;
		    		
		    		/*get the assessmentID for the first question*/
	    			var jsonResp = batteryAsmtIds[0];
	    			assessmentId = jsonResp.OID; 
	    			renderQuestion(assessmentId, postedData);
		    	}
		    },	
		    error: function(jqXHR, textStatus, errorThrown) {
		        document.write(jqXHR.responseText + ':' + textStatus + ':' + errorThrown);
		    }
		});
	}else{
		renderQuestion(assessmentId, postedData);
	}
}

function renderQuestion(assessmentId, postedData){
	currentID = assessmentId; // always renew this ID by getAssessmentID??
	$.ajax({
		url: baseUrl + "/ws/public/promis/renderQuestion/" + currentID,
		type: "POST",
		data: postedData,
		dataType: "json",
		
		success: function(data) {			
			if(data.DateFinished !=''){
				scoring(currentID);
			}else if(data.Items[0].Elements.length < 2){ // it's not a real question
				renderQuestion(assessmentId, "ItemResponseOID=''&Response=''");
			}else{	
				for(var i=0; i < data.Items.length; i++){					
					var catOid = data.Items[i].ID;
					catOid = catOid.trim();
					$("[id='" + catOid + "']").parent().closest('tr').show();
					var secId = $("[id='" + catOid + "']").attr('secid');
					$('#sectionContainer_'+secId).show();
					currentCatOid = catOid;
				}
			}
		},
	    error: function(jqXHR, textStatus, errorThrown) {
	    	alert('renderQuestion: ' + jqXHR.responseText + ':' + textStatus + ':' + errorThrown);
	    }
	});
}

function getNext(obj){
	var nothingChecked = true;
	var formOID = $('#OID').val();
	var responseID;
	var responseValue;
	$(obj).each(function(){
		if($(this).is(':checked')){
			responseID = $(this).attr('responseID');
			responseValue = $(this).attr('responseValue');
			if(responseValue == '0'){
				responseValue = '00000000-0000-0000-0000-000000000000';
			}
			nothingChecked = false;
		}		
	});
	if(nothingChecked){
		alert('Please select a value for the question displayed.');
	}else{
		$("[id='" + currentCatOid + "']").parent().closest('tr').hide();		
		var secId = $("[id='" + currentCatOid + "']").attr('secid');
		$('#sectionContainer_'+secId).hide();
		
		/*Battery Form*/
		var nextAsmtId = "";
		for(var i = 0; i < batteryAsmtItems.length; i++){
			var batteryAsmtItem = batteryAsmtItems[i];
			if(batteryAsmtItem.OID == assessmentId) {
				var isBreak = false;
				for(var j=0; j < batteryAsmtItem.Items.length; j++){
					var catOid = batteryAsmtItem.Items[j].ID;
					if(catOid == currentCatOid 
							&& j == (batteryAsmtItem.Items.length - 1) //last question of each assessment form 
							&& i < (batteryAsmtItems.length -1)) {     //but not last question of whole battery form
						nextAsmtId = batteryAsmtItems[i+1].OID;
						isBreak = true;
						break;
					}
				}
				if(isBreak){
					break;
				}
			}
		}
		var data = "";
		if(nextAsmtId.length > 0){ /*Battery Form*/
			assessmentId = nextAsmtId;
		} else {
		    data = "ItemResponseOID=" + responseID + "&Response=" + responseValue;
		}
		renderQuestion(assessmentId, data);
	}
}

function scoring(assessmentId){
	var formOID = $('#OID').val();
	$.ajax({
		url:  baseUrl + "/ws/public/promis/scoring/" + assessmentId,
		type: "GET",
		dataType: "json",
		
		success: function(data) { 
			var total_tScore = formatFloat(data.Theta*10+50,1);
			var total_se = formatFloat(data.StdError*10,1);
			for(var i=0; i < data.Items.length; i++){
				var pqID = data.Items[i].ID;
				pqID = pqID.trim();
				var tscore = formatFloat(data.Items[i].Theta*10+50,1);
				var se= formatFloat(data.Items[i].StdError*10,1);
				
				var qPosition = data.Items[i].Position;
				var secId = $('#'+pqID).attr('secid');				
				$("#sectionContainer_"+secId+" :input").each(function(){
					if($(this).attr('catOid') == 'catTSCORE'){
						$(this).attr('value',tscore);
					}else if($(this).attr('catOid') == 'catStandardError'){
						$(this).attr('value',se);
					}else if($(this).attr('catOid') == 'catQuestionPosition'){
						$(this).attr('value',qPosition);
					}else{}
				});
			}			
			$("input[catOid='catFinalTSCORE']").attr('value',total_tScore);
			$("input[catOid='catFinalStandardError']").attr('value',total_se);
			$('#thanks').show();
		},		
		error: function(jqXHR, textStatus, errorThrown){
			alert('displayScore:' + jqXHR.responseText + ':' + textStatus + ':' + errorThrown);
		}
	});
}

function hideEmptyEntries(){
	$('.sectionContainerTable').each(function(){
		if($(this).attr('name') != 'Main' && $(this).attr('name') != 'Final Results' && $(this).attr('name') != 'Form Administration'){
			var secName = $(this).attr('name');
			$("[name='"+secName+"'] input").each(function(){
				if($(this).attr("type")=='text'){
					if($(this).val() == ''){
						$(this).parent().closest('.sectionContainerTable').closest('.rowTable').hide();			
					}
				}
				if($(this).attr("type")=='radio'){
					if(!$("input[name='"+$(this).attr('id')+"']").is(':checked')){
						$(this).parent().closest('.sectionContainerTable').closest('.rowTable').hide();			
					}
				}
				
			});
		}
	});
}

function formatFloat(num, pos){
  var size = Math.pow(10, pos);
  return Math.round(num * size) / size;
}

function EditConfirmationDialog(table, dialogType, msgText, yesBtnText, noBtnText, action, isFormSubmission, width, title, selectedRows) {
	var titleText = title || null;
	//var documentationLimit = $("#documentationLimit").val();
	var dt = table.idtApi('getTableApi');
	var dlgId = $.ibisMessaging(
		"dialog", 
		dialogType, 
		msgText,
		{
			id: 'test',
			container: 'body',
			buttons: [{
				id: "yesBtnA",
				text: yesBtnText, 
				click: _.debounce(function() {
					$(this).siblings().find("#yesBtnA").prop("disabled", true);
					var selectedRow = table.idtApi('getSelectedOptions');
						$.ajax({
							type: "POST",
							cache: false,
							url: action,
							success: function(data) {
								selectedRow.length = 0;
								dt.rows('.selected').remove().draw(false);
								
								$.ibisMessaging("close", {type: 'dialog'});
								//console.log(dlgId);
							},
							error: function(jqXHR, textStatus, errorThrown) {
								console.log(errorThrown)
							}
						});
				}, 1000, true)
			},
			{
				text: noBtnText,
				click: function() {
					$.ibisMessaging("close", {id: dlgId});					
				}
			}],
			modal: true,
			width: width,
			title: titleText
		}
	);
}


function DeleteDialog(table, dialogType, msgText, yesBtnText, noBtnText, action, isFormSubmission, width, title) {
	var titleText = title || null;
	var dlgId = $.ibisMessaging(
		"dialog", 
		dialogType, 
		msgText,
		{
			id: 'deleteRows',
			container: 'body',
			buttons: [{
				id: "yesBtnA",
				text: yesBtnText, 
				click: _.debounce(function() {
					$(this).siblings().find("#yesBtnA").prop("disabled", true);
					var selectedRows = table.idtApi('getSelectedOptions');
					
					table.idtApi('getTableApi').rows('.selected').remove().draw(false);
					selectedRows.length = 0;
						
					redirectWithReferrer(action);
					$.ibisMessaging("close", {type: 'dialog'});

			
				}, 1000, true)
			},
			{
				text: noBtnText,
				click: function() {
					$.ibisMessaging("close", {id: dlgId});					
				}
			}],
			modal: true,
			width: width,
			title: titleText
		}
	);
}

function getValue(value) {
	   return (value < 10) ? "0" + value : value;
};

function getDate () {
	   var newDate = new Date();
	   var sMonth = getValue(newDate.getMonth() + 1);
	   var sDay = getValue(newDate.getDate());
	   var sYear = newDate.getFullYear();
	   return sYear + sMonth + sDay;
}
//=====================




