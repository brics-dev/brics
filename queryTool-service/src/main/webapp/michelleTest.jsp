<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>

<script type="text/javascript">

var filtersTest = [
	{
		elementUri : "",
		groupUri : "",
		permissibleValues : ["one", "two"],
		freeFormValue : "a free form value",
		maximum : 1,
		minimum : 2,
		dateMin : "12/12/90",
		dateMax : "3/20/94",
		blank: false
	},
	{
		elementUri : "",
		groupUri : "",
		permissibleValues : ["three", "four"],
		freeFormValue : "another free form value",
		maximum : 3,
		minimum : 4,
		dateMin : "1/2/04",
		dateMax : "10/3/12",
		blank: true
	}
                   
];

$(document).ready(function() {
	//var requestUrl = "http://pdbp-portal-local.cit.nih.gov:8080/query/service/dataCart/data_cart/form/add";
	//var requestUrl = "http://pdbp-portal-local.cit.nih.gov:8080/query/service/dataCart/runQuery";
	var requestUrl = "http://pdbp-portal-local.cit.nih.gov:8080/query/service/dataCart/dataWithPagination";
	//var requestUrl = "http://pdbp-portal-local.cit.nih.gov:8080/query/service/dataCart/selectedFormDetails";
	//var requestUrl = "http://pdbp-portal-local.cit.nih.gov:8080/query/service/dataCart/applyFilters";

	$.ajaxSettings.traditional = true;
	$.ajax({
		type: "GET",
		//type: "POST",
		url: requestUrl,
		data : {
			formUris: ["http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/FamilyHistory_v1.0", 
			           "http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/BehavioralHistory_v1.0"],
			
			//formUri: "http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/ImagingMR_v1.0",
			formUri: "http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/FamilyHistory_v1.0",
			//formUri: "http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/BehavioralHistory_v1.0",
			studyUri: "http://ninds.nih.gov/repository/fitbir/1.0/Study/Non-invasive%20Markers%20of%20Neurodegeneration%20in%20Movement%20Disorders",
		
			offset: 10,
			limit: 10,
			sortColName: "FamilyHistory_v1.0,Required Fields,AgeYrs",
			sortOrder: "ASC"  
		},
	//	data: {filters: JSON.stringify(filtersTest)},
		success : function(data, textStatus, jqXHR) {
			$("body").html("Successful receipt: " + textStatus + " " + data);
		},
		error : function(data) {
			$("body").html("there was an error\n\n" + data);
		}
	});
});

</script>


</head>
<body>
michelle test
</body>
</html>