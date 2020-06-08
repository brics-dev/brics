/**
 * 
 */
QT.FilterExpressionUtil = {
		
	parentSplitRegex : /\)[\|\&\!\s]/g,
	whitespaceRegex: /\s/g,
	orRegex: /\|\|/g,
	andRegex: /\&\&/g,
	filterNameRegex: /[^\!\&\|\(\)]+/g,
	
	orOutput: '\|',
	andOutput: '\&',
	filterNameOutput: '_',
	
	logicBeforeMap : {
		"&": "&&", 
		"|": "||", 
		"!": "!",
		"|!": "||!"},
		
		
	/**
	 * Removes all whitespace
	 * Removes all repeated logical characters (IE: && becomes &)
	 * Turn all filter names into a single underscore
	 */
	preprocess : function(filterExpression) {
		filterExpression = filterExpression
				.replace(this.whitespaceRegex,'')
				.replace(this.orRegex, this.orOutput)
				.replace(this.andRegex, this.andOutput)
				.replace(this.filterNameRegex, this.filterNameOutput);
		return filterExpression;
	},
	
	/**
	 * Splits the given expression into parent-filter sections.
	 * Does this by assuming every parent filter ends in a parenthesis
	 * and, in the middle of the string, that would be followed by
	 * something OTHER than a parenthesis.
	 * 
	 * NOTE: retains all characters in one of the output array elements
	 * @return array of elements representing the parent filters
	 */
	splitIntoParents : function(filterExpression) {
		var parentParts = [];
		var previousIndex = 0;
		var expressionLength = filterExpression.length;
		var match;
		while ((match = this.parentSplitRegex.exec(filterExpression)) != null) {
			var matchingIndex = match.index + 1;
			var extracted = filterExpression.substring(previousIndex, matchingIndex);
			previousIndex = matchingIndex;
			parentParts.push(extracted);
		}
		// grab the last one since it wouldn't be included in the while
		parentParts.push(filterExpression.substring(previousIndex, expressionLength));
		return parentParts;
	},
	
	/**
	 * Splits a parent filter after groupingBefore, groupingAfter, and logicBefore
	 * are removed (for the parent) into child logical statements.  Should always have
	 * the same length as number of filters in the expression.
	 * NOTE: removes the child filter elements leaving only their logicBefore values
	 * 
	 * @example !_|_&_ would result in ['!', '|', '&']
	 * @example _|_ would result in ['', '|']
	 */
	splitIntoChildren : function(filterExpression) {
		var output = filterExpression.split(this.filterNameOutput);
		output.pop();
		return output;
	},
	
	/**
	 * Determines if the given filterExpression starts with a groupingBefore of 1, 2, or 3
	 * NOTE: parents are the only ones who can get groupingBefore
	 */
	determineGroupingBefore : function(filterExpression) {
		// the - 1 is because we're turning an array of split bodies into a length of splitters
		return filterExpression.split("(").length - 1;
	},
	
	/**
	 * Determines if the given filterExpression ends with a groupingAfter of 1, 2, or 3
	 * NOTE: parents are the only ones who can get groupingAfter
	 */
	determineGroupingAfter : function(filterExpression) {
		// the - 1 is because we're turning an array of split bodies into a length of splitters
		return filterExpression.split(")").length - 1;
	},
	
	/**
	 * Determines what the logicBefore is at the beginning of the expression.  Turns that into
	 * the two-character version if applicable.  Returns empty string (a valid logicBefore) if
	 * the first character is not a valid logicBefore character.
	 */
	determineLogicBefore : function(filterExpression) {
		var logicBefore = this.logicBeforeMap[filterExpression.charAt(0)];
		// handle the special case of "or not"
		if (logicBefore == "||") {
			if (filterExpression.length > 1 && this.logicBeforeMap[filterExpression.charAt(1)] == "!") {
				return "||!";
			}
		}
		else if (logicBefore == "&&") {
			if (filterExpression.length > 1 && this.logicBeforeMap[filterExpression.charAt(1)] == "!") {
				return "&&!";
			}
		}
		return logicBefore || "";
	},
	
	/**
	 * Removes any number of open parentheses at the beginning of the given
	 * filter expression string
	 */
	removeGroupingBefore : function(filterExpression) {
		return filterExpression.replace(/^\(+/g, '');
	},
	
	/**
	 * Removes any number of closing parentheses at the end of the given
	 * filter expression string
	 */
	removeGroupingAfter : function(filterExpression) {
		return filterExpression.replace(/\)+$/g, '');
	},
	
	/**
	 * Removes any logic character at the beginning of the given filter expression
	 */
	removeLogicBefore : function(filterExpression) {
		
		return filterExpression.replace(/^[\|\&\!]/, '');
	}
};