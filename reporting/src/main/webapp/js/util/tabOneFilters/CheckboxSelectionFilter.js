/**
 * 
 */
QT.Filters.CheckboxSelectionFilter = QT.Filter.extend({
	name : "CheckboxSelectionFilter",
	
	/**
	 * listOfVisible contains all selected on the other side.
	 * Have to check and see if this model is referenced there.
	 * 
	 * For example, model is a Study, listOfVisible contains many Forms
	 * TRUE if Form.getCollection().get(studyUri) is true.
	 * ALSO: since these are reversable, TRUE if Study.getCollection().get(formUri)
	 */
	check : function(model, fullObjMap, listOfVisible) {
		var asArray = _.keys(listOfVisible);
		var lengthOfVisible = asArray.length;
		for (var i = 0; i < lengthOfVisible; i++) {
			var singleVisible = asArray[i];
			if (model.getCollection().get(singleVisible)) {
				return true;
			}
		}
		return false;
	}
});
