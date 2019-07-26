//Common js that would be applied across data collection module

$(document).ready(function() {	
	
	//Resize Text Area
	//$("textarea").height( $("textarea")[0].scrollHeight );
	//To put auto focus on first input field to address some of the keyboard navigation issue so it reduces the no of user tabs
	$(":input:visible:enabled:not([readonly]):first").focus();
	
	//Scroll fixes on key navigation
	 $(".questionTR").find("input, textarea, select").keydown(function(event) {	  
		 /*$('input, textarea, select').keydown(function(event) {	  */      
	        if (event.which == 9){
			 if(event.shiftKey){
				 //Get previous questionTR in backward direction
				 var prevInput = $(this).prevAll("input:visible").first();
				 //will get selected element or determines if the you are in last option of select type input
				 if($(this).is(':checked') || prevInput.length == 0) {
			     //if($(this).is(':checked')) {	
					 var prevTr = $(this).parents(".questionTR").first().prevAll(".questionTR").first();
					 if(prevTr.length != 0 ){
						 var windowViewPortHeight = $(window).height();
						 var trPosition = prevTr.offset().top;
						 var trPositionRelativeToViewport = prevTr.offset().top - $(document).scrollTop();
						 if(trPositionRelativeToViewport < 0) {
							 $(window).scrollTop(trPosition);
						 }
					 }
				  }
			 }else{
				 //Get next questionTR in forward direction 
				 var nextInput = $(this).nextAll("input:visible").first();
				 //will get selected element or determines if the you are in last option of select type input
				 //alert("yy"+$(this).is(':checked')+"tt"+nextInput.length+"id"+$(this).target.value);
				 if($(this).is(':checked') || nextInput.length == 0) {
					 var nextTr = $(this).parents(".questionTR").first().nextAll(".questionTR").first();
					// alert("nn");
					 if(nextTr.length != 0){
						 var windowViewPortHeight = $(window).height();
						 var  trPosition = nextTr.offset().top;
						 var trPositionRelativeToViewport = nextTr.offset().top - $(document).scrollTop();
						 var nextTrHeight = nextTr.height();
						 if((trPositionRelativeToViewport+nextTrHeight) > windowViewPortHeight){							
							 $(window).scrollTop(trPosition);
						 }
					 }
				 }
			 }
	        }

	    });
	 
	 // sets up a timeout loop instead of an each loop to run asynchronously
	 var $allTds = $(".questionTR td");
	 var tdsLength = $allTds.length;
	 var j = 0;
	 setTimeout(function copyBackgroundLoop() {
	     try {
	         // perform your work here
	    	 var $td = $allTds.eq(j);
	    	 $td.find("span").each(function() {
	 			var childColor = $(this).css("background-color");
	 			if (childColor != "transparent") {
	 				$td.css("background-color", childColor);
	 			}
	 		});
	     }
	     catch(e) {
	         // handle any exception
	     }
	      
	     j++;
	     if (j < tdsLength) {
	         setTimeout(copyBackgroundLoop, 0); // timeout loop
	     }
	     else {
	         // any finalizing code
	     }
	 });
});





function goToFileQuestion(type,id) {
	if(type=='11') {
		goToValidationErrorFlag();
		$('[ref="'+ id +'"]').get(0).focus();

	}else {
		goToValidationErrorFlag();
		document.getElementById (id).focus();
	}
}
