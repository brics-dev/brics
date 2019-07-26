

function SortHeader (type, elemId, elemName) {

    this.type = type;
    // VALID TYPES SHOULD BE 'CALENDAR', 'SELECT' OR 'TEXT'
    this.options = new Array();
    this.text = "";
    this.elemId = elemId;                  // the corresponding html element id
    this.elemName = elemName;               // the corresponding html element name
    this.defaultValue = "";
    this.displayValue = "";

}

SortHeader.prototype.setDisplayValue = function (display) {
    this.displayValue = display;
}

SortHeader.prototype.setDefaultValue = function (defValue) {
    this.defaultValue = defValue;
}

SortHeader.prototype.setOptions = function (optsArray) {
    this.options = optsArray;
}

SortHeader.prototype.getDisplay = function () {
    var str = "<div id='"+this.elemId+"_container' style='position:relative; z-index:5000;' width='100%' align='center' valign='center'>"
    str += "<div style='word-wrap:normal;' id='"+this.elemId+"_control' ><img  onClick=\"activateDisplay('"+this.elemId+"');\" src='/ctdb/images/sortHeaderIcon.gif' height=16 width=16 />"+this.displayValue+"</div>";

    str += " <div id='"+this.elemId+"_display' style='display:none; position:absolute; z-index:5500; '>";
    if (this.type == "TEXT") {
        str += "<input type='text' size=12 id='"+this.elemId+"' name='"+this.elemName+"' style='font-size:9; border:1px ridge black;' onChange=\"submitSearch('"+this.elemId+"');\"  onkeyup=\"submitKeyUpSearch('"+this.elemId+"');\">";
        //str +="<input type='hidden' name='"+this.elemName+"' id='"+this.elemId+"' value='"+this.defaultValue+"'/>";

    }
    else if (this.type=='CALENDAR') {
        str += "<div style='word-wrap:none;width:110px;'><span style='vertical-align:bottom;'><input type='text' style='font-size:9; border:1px black ridge;' size=12 name='"+this.elemName+"' id='"+this.elemId+"' value='"+this.defaultValue+"' onFocus=\"submitSearch('"+this.elemId+"');\"/><a";
        str += " href=\"javascript:popCalendar(document.getElementById('"+this.elemId+"'));\" title='Calendar'><img";
        str += " src='/ctdb/images/iconCalendar.gif' width='34' height='21' alt='Calendar' border='0'/></a></span></div>";

    } else {
    // should be 'select'
        str += "<select name='"+this.elemName+"' id='"+this.elemId+"'  style='font-size:9;' onChange=\"submitSearch('"+this.elemId+"');\">";
        for (i =0; i < this.options.length; i++) {
            str += "<option value='"+this.options[i]+"'>"+this.options[i]+"</option>";
        }
        //str += "<input type='hidden' name='"+this.elemName+"' id='"+this.elemId+"' value='"+this.defaultValue+"'/>";

    }
    str += "</div> <!-- close display div-->";
    str += "</div> <!-- close container div-->";
    return str;
}


function activateDisplay (elemId) {
    if (document.getElementById(elemId+"_display").style.display=='none') {

        document.getElementById(elemId+"_display").style.display='block';
        document.getElementById(elemId+"_display").style.left=window.event.x;
        document.getElementById(elemId+"_display").style.top=window.event.y +15;

    } else {
        document.getElementById(elemId+"_display").style.display='none'
    }
}



