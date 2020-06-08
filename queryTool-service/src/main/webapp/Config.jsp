<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<jsp:useBean id="bean" class="gov.nih.tbi.constants.ApplicationConstants"></jsp:useBean>
<script type="text/javascript">
var System = {
	user : {
		username : "administrator",
		isAdmin : true
	},
	environment : "default",
	urls : {
		base : "${applicationConstants.modulesAccountURL}",
		dictionary : "${applicationConstants.modulesDDTURL}",
		logout : "logout",
		login : "${applicationConstants.logInURL}",

		publicSite : "${applicationConstants.modulesPublicURL}",
		TemplatePublicURL : "${applicationConstants.templatePublicURL}",
		proforms :  "${applicationConstants.modulesPFURL}",
		guid : "${applicationConstants.modulesGTURL}",
		dataDict :  "${applicationConstants.modulesDDTURL}",
		dataRepo :  "${applicationConstants.modulesSTURL}",
		query : "${applicationConstants.modulesQTURL}",
		meta : "${applicationConstants.modulesMSURL}",
		account : "${applicationConstants.modulesAccountURL}",
		reporting : "${applicationConstants.modulesReportingURL}",
		
		accountsByRole : "${applicationConstants.accountsByRoleURL}",
		savedQueryUnique : "${applicationConstants.savedQueryNameUniqueWebServiceURL}",
		savedQueryInternalSave : "${applicationConstants.internalSavedQuerySaveWebServiceURL}",
		getSavedQuerySingle : "${applicationConstants.savedQueryGetWebServiceURL}",
		savedQueryRemove : "${applicationConstants.savedQueryRemoveSavedQueryWebServiceURL}",
		getEntitiesByTypeObjId : "${applicationConstants.listSavedQueryPermissionsWebServiceURL}",
		unregisterEntitiesByIdList : "${applicationConstants.unregisterEntityMapByIdsWebServiceURL}",
		rBoxProcess : "${applicationConstants.rboxProcessURL}"
	},
	maintenance : {
		enabled : false,
		message : ""
	},
	nextNegId : -1,
	
	getUniqueNegId : function() {
		// Check for int overflow event.
		if (this.nextNegId >= 0) {
			this.nextNegId = -1;
		}
		
		return this.nextNegId--;
	}
};

var Config = {
	containerDiv : $("body"),
	getMaxDialogHeight : function() {
		var maxHeight;
		
		if ( typeof window.innerHeight != "undefined" ) {
			maxHeight = window.innerHeight * .9;
		}
		else {
			maxHeight = false;
		}
		
		return maxHeight;
	},
	
	/**
	 * class names for elements on the page so that we don't have to hard
	 * code those
	 */
	identifiers : {

	},
	
	language : {
		selectCriteriaEmpty : "You must select a form to query before this tab can be populated"
	},
	
	/*
	 * Amount to subtract from the window height for the main content height
	 */
	windowHeightOffset : 0,
	scrollContainerBottomOffset : 15,
	
	tabConfig : {
		StudiesTab : {
			name : "StudiesTab",
			selectionList : {
				object : "Form",
				list : "SelectionForms",
				pageList : "forms",
				objectRelationToTiles : "studies",
				currentlyFilteringText : "Currently Filtering Studies By:",
				filterText : "Filter studies by:",
				filterTypeText : "Forms",
				textSearchUrl : "service/query/forms"
			},
			tiles : {
				object : "Study",
				list : "SelectionStudies",
				pageList : "studies",
				objectRelationToSelection : "forms",
				container : ".resultPaneSelectStudies",
				subElementList : "forms",
				subElementListText : "forms",
				filterTypeText : "Studies",
				textSearchUrl : "service/query/studies"
			}
		},
		
		FormsTab : {
			name : "FormsTab",
			selectionList : {
				object : "Study",
				list : "SelectionStudies",
				pageList : "studies",
				objectRelationToTiles : "forms",
				currentlyFilteringText : "Currently Filtering Forms By:",
				filterText : "Filter forms by:",
				filterTypeText : "Studies",
				textSearchUrl : "service/query/studies"
			},
			tiles : {
				object : "Form",
				list : "SelectionForms",
				pageList : "forms",
				objectRelationToSelection : "studies",
				container : ".resultPaneSelectForms",
				subElementList : "studies",
				subElementListText : "studies",
				filterTypeText : "Forms",
				textSearchUrl : "service/query/forms"
			}
		},
		
		DataElementsTab : {
			name : "DataElementsTab",
			selectionList : {
				object : "DataElement",
				list : "SelectionDataElements",
				pageList : "dataElements",
				objectRelationToTiles : "forms",
				currentlyFilteringText : "",
				filterText : "",
				filterTypeText : "Data Elements",
				textSearchUrl : ""
			},
			tiles : {
				object : "Form",
				list : "SelectionForms",
				pageList : "forms",
				objectRelationToSelection : "dataElements",
				container : ".resultPaneSelectDataElements",
				subElementList : "studies",
				subElementListText : "studies",
				textSearchUrl : "service/query/forms",
				filterTypeText : "Forms"
			}
		},
		
		DefinedQueriesTab : {
			name : "DefinedQueriesTab",
			selectionList : {
				object : "DefinedQuery",
				list : "SelectionDefinedQueries",
				pageList : "definedQueries",
				objectRelationToSelection : "studies",
				currentlyFilteringText : "",
				filterText : "",
				filterTypeText : "Defined Queries",
				textSearchUrl : ""
			},
			tiles : {
				object : "Study",
				list : "SelectionStudies",
				pageList : "studies",
				objectRelationToSelection : "definedQueries",
				container : ".resultPaneSelectDefinedQueries",
				subElementList : "forms",
				subElementListText : "forms",
				filterTypeText : "Studies",
				textSearchUrl : "service/query/studies"
			}
		}
	},
	
	/**
	 * Used to map from the server's description.  Just provides a nice set of constants
	 * that can be accessed quickly.
	 * These are related to the DE's input instructructions
	 */
	filterTypeMapping : {
		freeForm : "Free-Form Entry",
		singleSelect : "Single Pre-Defined Value Selected",
		radioSelect : "Radio Values",
		multiSelect : "Multiple Pre-Defined Values Selected",
		guid : "GUID",
		changeInDiagnosis : "Change in Diagnosis",
		date : "Date or Date & Time",
		alpha : "Alphanumeric",
		numeric : "Numeric Values",
		triplanar: "Tri-Planar",
		thumbnail: "Thumbnail",
		file: "File",
		biosample: "Biosample",
		dataset: "Dataset",
		multiRange: "Multi Range"
		// TODO: add more here
	},
	
	//TODO: we need to create an accurate mapping from the DE's input instructions, dataType, and Permissibale values
	//count to the JAVA types, right now FreeForm from above can have FREE_FORM or SINGLE_SELECT type. 
	filterTypeJava: {
		0: "FREE_FORM",
		1: "SINGLE_SELECT", 
		2: "MULTI_SELECT", 
		3: "DATE",
		4: "RANGED_NUMERIC",
	    5: "DELIMITED_MULTI_SELECT", 
	    6: "CHANGE_IN_DIAGNOSIS",
	    7: "SHOW_BLANKS",
	    8: "DATASET",
	    9: "MULTI_RANGE"

	},
	
	filterTypes : {
		freeFormType : 0,
		numericRangeType : 1,
		dateRangeType : 2,
		permissibleValuesType : 3,
		numericUnbounded : 4,
		radioFormType : 5,
		multiRangeType: 6
	},
	
	permissionTypes : ["Read", "Write", "Admin", "Owner"],
	multiRaceDEs: ["RaceUSACat"],
	
	mdsUpdrsOptions : 
		{ message: "The MDS-UPDRS part III section has been rescored for a subset of Penn State subjects - records are highlighted in RED. For additional details visit the Data Repository or the PDBP public website and view the study: Multimodal MRI markers of nigrostriatal pathology in Parkinson's disease (U01).",
			formNames: ["PDBP MDS-UPDRS","MDS_UPDRS","PDBP MDS-UPDRS-X","MDSUPDRS_PennStateRescored","PDBP MDS-UPDRS_Penn State Rescored"],
			formNamesForRedText: ["PDBP MDS-UPDRS-X","MDS_UPDRS_X","MDSUPDRS_PennStateRescored","PDBP MDS-UPDRS_Penn State Rescored"]
	},
};

$(document).ready(function() {
	// Determine the environment string.
	//TODO: This needs to be defined by style-key in properties
	var env = "default";
	var url = "${applicationConstants.styleKey}"; //window.location.href;
	
	if ( url.search("localhost") == -1 ) {
		// Check for FITBIR.
		if ( url.search("pdbp") >= 0 ) {
			env = "pdbp";
		}
		// Check for FITBIR.
		else if ( url.search("fitbir") >= 0 ) {
			env = "fitbir";
		}
		// Check for EyeGene.
		else if ( url.search("eyegene") >= 0 || url.search("nei") >= 0 ) {
			env = "eyegene";
		}
		// Check for cdRNS.
		else if ( url.search("cdrns") >= 0 ) {
			env = "cdrns";
		}
		// Check for CiStar.
		else if ( url.search("cistar") >= 0 ) {
			env = "cistar";
		}
		// Check for CNRM.
		else if ( url.search("cnrm") >= 0 ) {
			env = "cnrm";
		}
		// Check for NTI.
		else if ( url.search("nti") >= 0 ) {
			env = "nti";
		}
		// Is NINDS.
		else {
			env = "ninds"
		}
	}

	// Set the environment string in the System object.
	System.environment = env;
	System.downloadThreshold = parseInt("${applicationConstants.downloadThreshold}",10) || 0;
});
</script>