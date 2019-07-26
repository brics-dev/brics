
/*
*  HashMap hm the hashmap of inprogress responses
*  The format should be :
*   key = "Q_" questionId
*   value = Array of answers
*/


HashMap hm = new HashMap();


var keys = hm.keySet();

for (i = 0; i < keys.length; i ++) {
    var anElem = document.getElementById(keys[i]);
    if (anElem.type == "select" || anElem.type == "select-one" || anElem.type == "select-multiple") {
        var responses = hm.get(keys[i]);
        for (j = 0; j<responses.length; j++) {
            for (k=0; k<anElem.options.length; k++) {
                if (anElem.options[k].value == responses[j]) {
                    anElem.options[k].selected = true;
                }
            }
        }
    }
    else if (anElem.type == 'radio' || anElem.type == "checkbox") {
        var responses = hm.get(keys[i]);
        var elems = document.getElementsByName(keys[i])
        for (j=0; j < responses.length; j++) {
            for (k=0; k < elems.length; k++) {
                if (elems[k].value == responses[j]) {
                    elems[k].checked = true;
                }
            }
        }
    } else if (anElem.type == 'text' || anElem.type == "textarea") {
    // must be a textarea or textbox
        anElem.value = hm.get(keys[i])[0];
        // responses =hm.get(keys[i]);
        // anElem.value = responses[0];
    } else {
        // visual scale?
        
    }
}




