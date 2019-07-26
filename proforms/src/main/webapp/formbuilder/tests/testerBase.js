/**
 * 
 */
function resetForm() {
	// remove all questions and sections recursively
	FormBuilder.form.sections.forEach(function(section) {
		section.questions.forEach(function(question) {
			EventBus.trigger("delete:question",question);
		});
		EventBus.trigger("delete:section",section);
	});
}