/**
 * functions related to calculated questions
 */



var operatorAllowed = false;
var numAllow = true;
var qAllow = true;

/* set this to false if ordering is to be enforced */
var anyOrder = true;

/**
 * Resets order logic variables to their default state
 */
function reset() {
	numAllow = true;
	qAllow = true;
	operatorAllow = false;
}

/**
 * Backspace is used to erase the last entered entity or digit. Entities include operators,
 * questions, and special operators (ex: "sqrt(")
 */
function backspace() {
	var str = new String(document.getElementById("window").innerHTML);
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
	document.getElementById("window").innerHTML = str;
}

/**
 * Clears the forumla window. Also can be used to reset any conditional order logic.
 */
function clear() {
	document.getElementById("window").innerHTML = "";
	reset();
}


/**
 * used to display decimals and nubmers used to create floating point values.
 */
function number(x) {
	if (anyOrder || numAllow) {
		document.getElementById("window").innerHTML = document.getElementById("window").innerHTML + x;
	}
	qAllow = false;
	operatorAllowed = true;
}

/**
 * button displays an operation button, or other non digit button. Logic for restricting which elements
 * are added can be inserted here too and separated from number logic.
 */
function button(x) {
	if (anyOrder || operatorAllowed) {
		document.getElementById("window").innerHTML = trim(document.getElementById("window").innerHTML) + ' ' + x + ' ';
	}

	operatorAllowed = false;
	qAllow = true;
	numAllow = true;
}

/**
 * Formats the percent button
 */
function percent() {
	document.getElementById("window").innerHTML = document.getElementById("window").innerHTML + '%';
}

/**
 * Determines if the current page is for a dateTime Question or Not
 */
function isDateQuestion() {
    var isDate = 0;
    if (document.getElementById("conversionFactor") != null) {
        if (document.getElementById("conversionFactor").disabled.toString() != "true".toString()) {
            isDate = 1;
        }
    }

    return isDate;
}


/**
 * Handles parenthesis. Right now almost identical to button; however, if one were to add support
 * for only allowing one to add an appropriate parenthesis it would be done here.
 */
function paren(x) {
	document.getElementById("window").innerHTML = trim(document.getElementById("window").innerHTML) + x;
}


/**
 * addQuestion is called when a question is clicked in the question listing box. The question
 * id is used to create the appropriate representation of '[Q_questionNumber]'
 * 
 * modified by Ching Heng\20121003
 * change id into representation of '[S_sectionNumber_Q_questionNumber]'
 */
function addQuestion() {
	str = trim(document.getElementById("window").innerHTML);
/*	q_str = trim(document.getElementById("questions").value);

	if (q_str.length>0) {
        if ((anyOrder || qAllow) && !(str.length>1 && ((str.charAt(str.length-1) == "(") || (str.charAt(str.length-1) == "%")))) {
            str = str + ' [Q_' + q_str + '] ';
        } else if (str.length>1 && (str.charAt(str.length-1) == "(" || (str.charAt(str.length-1) == "%"))) {
            str = str + '[Q_' + q_str + '] ';
        }
    }*/
	q_str = trim(document.getElementById("questions").value);
    var sectioQuestionArr=q_str.split(":");
	if (q_str.length>0) {
        if ((anyOrder || qAllow) && !(str.length>1 && ((str.charAt(str.length-1) == "(") || (str.charAt(str.length-1) == "%")))) {
            str = str + ' ['+sectioQuestionArr[0]+'_Q_' + sectioQuestionArr[1] + '] ';
        } else if (str.length>1 && (str.charAt(str.length-1) == "(" || (str.charAt(str.length-1) == "%"))) {
            str = str + '['+sectioQuestionArr[0]+'_Q_' + sectioQuestionArr[1] + '] ';
        }
    }
	document.getElementById("window").innerHTML = str;
	operatorAllowed = true;
	qAllow = false;
	numAllow = false;
}

/**
 * flipsign is used to change the sign in front of a question or decimal value
 */

function flipsign() {
	isNum = new RegExp("[\.0-9]");
	str = trim(document.getElementById("window").innerHTML);

	if (str.charAt(str.length-1) == "]") {
		pos = str.lastIndexOf("[");
		if (str.charAt(pos-1) != "-") {
			str = str.substring(0, pos) + "-" + str.substring(pos, str.length);
		} else {
			str = str.substring(0, pos-1) + str.substring(pos, str.length);
		}
	document.getElementById("window").innerHTML = str;
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

	document.getElementById("window").innerHTML = str;
	}
}

/**
 * test evaluates the expression by substituting a number for all questions, and evaluating.
 * The results are then displayed for the user.
 */
function test() {

    var	eqStr = new String(document.getElementById("window").innerHTML);
	if (isDateQuestion()) {


    } else {

        //eqStr = eqStr.replace(/\[Q_\d+\]/g, "2");
    	eqStr = eqStr.replace(/\[S_[-]?\d+_Q_\d+\]/g, "2");
        eqStr = eqStr.replace(/sqrt\(\s*([\d\.]+)\s*\)/g, "Math.sqrt($1)");
        document.getElementById("testResults").innerHTML="Test failed " + eqStr;
        evalResult = eval(eqStr);
        document.getElementById("testResults").innerHTML= "test succeeded " + evalResult + " " + eqStr;
    }

}


/**
 * submitTest - same as test, just w/ different results, for evaluating before submission;
 */
function testAndSubmit () {
	var eq = trim(new String(document.getElementById("window").innerHTML));
	var eqStr = trim(new String(document.getElementById("window").innerHTML));
    //var findQuestionsAll = /\[Q_([0-9]+)\]/g;
	var findQuestionsAll = /\[S_[-]?\d+_Q_([0-9]+)\]/g;


    var operators = /[+-\/%*]/

    if (eq.length > 0) {
        chr = eq.charAt(0);
        if (teststr = operators.exec(chr)) {
            document.getElementById("testResults").innerHTML="<br><font color=red>The equation cannot start with an operator.</font><br>&nbsp;";
            return;
        }
    }

    var divbyzero = /\s\/\s0+\s+/g;
    if (teststr = divbyzero.exec(eq+" ")) {
            document.getElementById("testResults").innerHTML="<br><font color=red>The equation cannot include division by 0.</font><br>&nbsp;";
            return;
    }

    var parenStr = testParen(eqStr);
	if (parenStr != null && parenStr.length > 0) {
	    document.getElementById("testResults").innerHTML="<br><font color=red>" + parenStr + ".</font><br>&nbsp;";
	    return;
	}

    if (isDateQuestion()) {

        ers = "";

        if (document.getElementById("conversionFactor").value < 0 && trim(eq).length > 0) {
            ers = "Please choose a conversion factor.<br>";
        }

        //--------------------------------------------------------------------------
        // check date
        //--------------------------------------------------------------------------
        //var findQuestionsWithMinus = /\[Q_[0-9]+\]\s*-\s*\[Q_[0-9]+\]/g;
        var findQuestionsWithMinus = /\[S_[-]?\d+_Q_[0-9]+\]\s*-\s*\[S_[-]?\d+_Q_[0-9]+\]/g;

        eqStr = eqStr.replace(findQuestionsWithMinus, "5");

        while (qs2 = findQuestionsAll.exec(eqStr)) {
            document.getElementById("testResults").innerHTML="<br><font color=red>" + ers + "Date Questions can only be used in subtraction from each other (ex S_1_Q_1 - S_2_Q_2) all other uses of date or date-time functions are illegal.</font><br>&nbsp;";
            return;
        }
        //------------------------------------------------------------------------------

        if (ers != null && ers.length > 0) {
            document.getElementById("testResults").innerHTML="<br><font color=red>" + ers + "</font><br>&nbsp;";
            return;
        }

    } else {
        eqStr = eqStr.replace(findQuestionsAll, "2");
    }

    eqStr = eqStr.replace(/sqrt\(\s*([\d\.]+)\s*\)/g, "Math.sqrt($1)");
    eqStr = eqStr.replace(/%/g, "*\(1/100\)*");

    document.getElementById("testResults").innerHTML="<br><font color=red>The entered calculation fails computation. Please correct the error.</font><br>&nbsp;";
    evalResult = eval(eqStr);

    document.getElementById("testResults").innerHTML="";
    document.getElementById("calculation").value = eq;
    document.questionCalculationForm.submit();
}

function testParen(x) {

    var str = new String(x);
    var left = 0;
 
    for (i=0; i<str.length; i++) {
        chr = str.charAt(i);
        if (chr == "(" ) {
            left++;
        } else if (chr == ")") {
            if (left < 1) {
                return "Unmatched right parentheses";
            } else {
                left--;
            }
        }
    }
    if (left != 0) {
        return "Unmatched left parentheses";
    }
}


/**
 * Trim trims both leading and trailing spaces
 */
function trim(s) {
  while (s.substring(0,1) == ' ') {
    s = s.substring(1,s.length);
  }
  while (s.substring(s.length-1,s.length) == ' ') {
    s = s.substring(0,s.length-1);
  }
  return s;
}

