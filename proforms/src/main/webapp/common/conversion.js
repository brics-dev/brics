/**
 * functions related to calculated questions
 */



var operatorAllowedC = false;
var numAllowC = true;
var qAllowC = true;

/* set this to false if ordering is to be enforced */
var anyOrderC = true;

/**
 * Resets order logic variables to their default state
 */
function resetC() {
	numAllowC = true;
	qAllowC = true;
	operatorAllow = false;
}

/**
 * Backspace is used to erase the last entered entity or digit. Entities include operators,
 * questions, and special operators (ex: "sqrt(")
 */
function backspaceC() {
	var str = new String(document.getElementById("windowConv").innerHTML);
	if (str.length == 0) { return; }

	str = new String(trim(str));
	sqrt = new String("sqrt(");

	// Need to erase the entire question
	if (str.charAt(str.length - 1) == "]") {

		str = str.substr(0, str.lastIndexOf("["));
		if (str.charAt(str.length-1) == "-") {
			str = str.substr(0, str.length-2);
		}
	} else if ((str.length >= sqrt.length) && ((str.substring(str.length - sqrt.length, str.length)) == sqrt)) {
			str = str.substr(0, str.length - sqrt.length - 1);
	} else {
			str = str.substr(0, str.length - 1);
	}

	str = trim(str);
	document.getElementById("windowConv").innerHTML = str;
}

/**
 * Clears the forumla window. Also can be used to reset any conditional order logic.
 */
function clearC() {
	document.getElementById("windowConv").innerHTML = "";
	resetC();
}


/**
 * used to display decimals and nubmers used to create floating point values.
 */
function numberC(x) {
	if (anyOrderC || numAllowC) {
		document.getElementById("windowConv").innerHTML = document.getElementById("windowConv").innerHTML + x;
	}
	qAllowC = false;
	operatorAllowedC = true;
}

/**
 * button displays an operation button, or other non digit button. Logic for restricting which elements
 * are added can be inserted here too and separated from number logic.
 */
function buttonC(x) {
	if (anyOrderC || operatorAllowedC) {
		document.getElementById("windowConv").innerHTML = trim(document.getElementById("windowConv").innerHTML) + ' ' + x + ' ';
	}

	operatorAllowedC = false;
	qAllowC = true;
	numAllowC = true;
}

/**
 * Formats the percent button
 */
function percentC() {
	document.getElementById("windowConv").innerHTML = document.getElementById("windowConv").innerHTML + '%';
}


/**
 * Handles parenthesis. Right now almost identical to button; however, if one were to add support
 * for only allowing one to add an appropriate parenthesis it would be done here.
 */
function parenC(x) {
	document.getElementById("windowConv").innerHTML = trim(document.getElementById("windowConv").innerHTML) + x;
}


/**
 * addQuestion is called when a question is clicked in the question listing box. The question
 * id is used to create the appropriate representation of '[Q_questionNumber]'
 * 
 * modified by Ching Heng\20121003
 * change id into representation of '[S_sectionNumber_Q_questionNumber]'
 */
function addQuestionC() {
	str = trim(document.getElementById("windowConv").innerHTML);

	q_str = trim(document.getElementById("questionConv").value);
	
	str = str + "[" + q_str + "]";

	document.getElementById("windowConv").innerHTML = str;
	operatorAllowedC = true;
	qAllowC = false;
	numAllowC = false;
}

/**
 * flipsign is used to change the sign in front of a question or decimal value
 */

function flipsignC() {
	isNum = new RegExp("[\.0-9]");
	str = trim(document.getElementById("windowConv").innerHTML);

	if (str.charAt(str.length-1) == "]") {
		pos = str.lastIndexOf("[");
		if (str.charAt(pos-1) != "-") {
			str = str.substring(0, pos) + "-" + str.substring(pos, str.length);
		} else {
			str = str.substring(0, pos-1) + str.substring(pos, str.length);
		}
	document.getElementById("windowConv").innerHTML = str;
	} else if (isNum.exec((str.charAt(str.length-1)))) {
		pos = -2;
		track = str.length-1;
		do {
			if (track < 0) {
				pos = -1;
			} else if (isNum.exec(str.charAt(track))) {
				track--;
			} else {
				pos = track;
			}
		} while(pos == -2);

		if (str.charAt(pos) != "-") {
			str = str.substring(0, pos+1) + "-" + str.substring(pos+1, str.length);
		} else {
			str = str.substring(0, pos) + str.substring(pos+1, str.length);

		}

	document.getElementById("windowConv").innerHTML = str;
	}
}



