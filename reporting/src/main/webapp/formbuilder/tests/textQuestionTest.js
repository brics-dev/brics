/**
 * 
 */
module("textQuestion");
test("Question Renders To Page", function() {
	// click the "add text question" link
	$("#addSectionButton").click();
	
	$(".ui-dialog-titlebar-close").click();
	
	$("#addTextboxQuestion").click();
	
	// is there a text input on the page?
	ok($('#formContainer input[type="text"]').length > 0);
});