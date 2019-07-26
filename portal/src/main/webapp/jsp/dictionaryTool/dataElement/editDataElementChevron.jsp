<%@include file="/common/taglibs.jsp"%>

<s:set var="formType" value="formType" />
<s:set var="dataType" value="dataType" />
<s:set var="currentPage" value="currentPage" />
<s:set var="deletable" value="deletable" />
<s:set var="currentDataElement" value="currentDataElement" />
<s:bean name="gov.nih.tbi.dictionary.model.DataElementForm" var="dataElementForm" />

<script type="text/javascript">	

function remove(dataElementId) {
	
	$.post(	"dataElementAction!removeDataElement.ajax", 
			{ dataElementId: dataElementId }, 
			function (data) 
				{ 
					window.location = "searchDataElementAction!list.action";
				}
			);
}

//This function handles the tabs.
//Uses validate when necessary
function changeTab(tabId) {
	
	var theForm = document.getElementById("theForm");
	var currentPage = "${currentPage}";
	
	if( currentPage != "keywords" && currentPage != "attachments") {
		if(tabId == 1) {
			submitTheForm(theForm, "${currentPage}${dataType}ValidationAction!editDetails.action");
		} else if(tabId == 2) {
			submitTheForm(theForm, "${currentPage}${dataType}ValidationAction!editValueRange.action");
		} else if(tabId == 3) {
			submitTheForm(theForm, "${currentPage}${dataType}ValidationAction!editKeywords.action");
		}
	} else {
		selectAllCurrentKeywords();
		if(tabId == 1) {
			submitTheForm(theForm, "${dataType}Action!editDetails.action");
		} else if(tabId == 2) {
			submitTheForm(theForm, "${dataType}Action!editValueRange.action");
		} else if(tabId == 3) {
			submitTheForm(theForm, "${dataType}Action!editKeywords.action");
		}
	}
}

function submitTheForm(theForm, action)
{
       if (action)
       {
             theForm.action=action;
       }
       theForm.submit();
}

function saveForm(action)
{
	var theForm = document.getElementById('theForm');
    if (action)
    {
          theForm.action=action;
    }
    theForm.submit();
}

//calls clear session to clear the data in session upon cancel
function cancel() {
	var dataType = '<s:property value="dataType"/>';
	if(dataType=="mapElement") { 
		window.location = "dataStructureElementAction!moveToElements.action";
	} else if(dataType=="dataElement") {
		window.location = "searchDataElementAction!list.action";
	}		
}

function selectAllCurrentKeywords() {
	 var list = $('.currentKeywords')[0];
	 
	 //If the list contains no options, insert one with the value: 'empty'
	 if (list.length ==0)
	 {
		 $(list).append($("<option />", { value : "empty" }));
	 }
	 for (var i = 0; i < list.options.length; i++) 
	   {
// 	    alert(list.options[i].value)
	    list.options[i].selected = true;
	   }
}
</script>