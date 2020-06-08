 <!DOCTYPE html>
<html>
<head>
<title>Page Title</title>
<script src="https://code.jquery.com/jquery-1.7.2.min.js" 
		integrity="sha256-R7aNzoy2gFrVs+pNJ6+SokH04ppcEqJ0yFLkNGoFALQ=" crossorigin="anonymous"></script>
<script type="text/javascript" src="js/util/Util.js"></script>
<style>
.headingDiv {
	margin: 200px auto 0px auto;
	font-size: 40px;
	text-align: center;
}

.loadingBarDiv {
	margin: 40px auto 0px auto;
	font-size: 20px;
	text-align: center;
}

.loadingTextDiv {
	margin: 30px auto 0px auto;
	font-size: 20px;
	width:200px;
	color: #666666;
	padding-left:15px;
}

.didYouKnowDiv {
	margin: 100px auto 0px auto;
    width: 600px; 
    overflow: auto;
	font-size: 20px;
	color: #b3b3b3;
	text-align: center;
	border-style: solid;
    border-width: 1px;
	padding: 10px 10px;
}
</style>
</head>
<body>
<div class="headingDiv">Query Tool</div>


<div class="loadingBarDiv">
<img src="images/slow_load.gif" />
</div>

<div class="loadingTextDiv">
loading permissions...
<br>
caching studies...
<br>
caching forms...
</div>


<div class="didYouKnowDiv">


</div>


</body>
<script type="text/javascript">
var didYouKnowTexts = [
"you can easily export your query results in csv format to your hard drive by clicking on the \"Download to Queue\" button?",
"you can do a logical join on 5 forms?",
"you can download all the data in your data cart by clicking on the \"Download Data Cart to Queue\"?",
"meta Study is integrated into Query Tool.  You can send your data and query to a Meta Study by clicking on the \"Send to Meta Study\" button?",
"you can save a query by clicking on the \"Save New Query\" button?",
"you can search a form by the data elements contained within the form in the \"Data Elements\" tab?",
"you can add or remove columns from the datatable in the \"Select Criteria\" tab or by using the menu in each column?",
"the query tool works by doing a \"outer\" join, which means all results are returned for the first form, and all results will be displayed for subsequent forms?",
"in the \"Refine Selected Data\" section, you can use the \"Select Criteria\" tab to view all the groups and data elements represented in the \"Datatable view\". You can use that view to select and deselect what you want to be presented or downloaded in the datatable?",
"you can view data element details by selecting the hamburger menu next to each column heading in the datatable view?",
"you can sort data by data element in the data table by clicking on the up/down arrows?",
"if you have multiple forms in your cart, you can look at the data found in one form by just clicking on it?",
"in the \"Studies\" tab, you can expand each study to view the forms collected in that study?",
"in the \"Forms\" tab, you can expand each form to view all the studies that are using that form?",
"you can hide Studies and forms not available by clicking on the button that says \"Hide not available\" in the \"Filter All Data\" section?",
"you can display different data standards from other organizations (CDISC, LOINC, SNOMED, etc.) on the datatable?",
"the default view in the Datatable View shows 20 entries. You can view up to 100 entries of a Query Result at a time by changing the value in the drop down menu?"
];
var randomNumber = Math.floor(Math.random()*didYouKnowTexts.length);
var randomText = didYouKnowTexts[randomNumber];
$(".didYouKnowDiv").html("Did you know " + randomText);

</script>

<script type="text/javascript">
// this particular format waits until everything on the page is loaded, including the loading gif
$(window).on("load", function() {
	// looks for a saved query parameter and, if it exists, passes it on to initialize
	var urlObj = urlObject(window.location.href);
	var sqId = "";
	if (typeof urlObj.parameters.savedQueryId !== "undefined") {
		sqId = "?savedQueryId=" + urlObj.parameters.savedQueryId;
	}

	window.location.replace("service/initialize" + sqId);
});
</script>
</html> 