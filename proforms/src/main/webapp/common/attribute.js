
function Attribute (Id, Name, Desc, Required) {
    this.id = Id;
    this.name = Name;
    this.description = Desc;
    this.required = Required;
    this.deleteIt = false;
    this.update = false;
    this.value = "";
    
}


Attribute.prototype.toString = function () {
    var str = "<tr><td class='tableCellHeader' align='center'>";
    str += "<input type=checkbox onChange=\"markDelete("+this.id+");\" id='"+this.id+"_delete' ";
    if (this.deleteIt == true) {
        str += " checked=true ";
    }
     str += "></td><td class='tableCellHeader'  align='center'><input type=checkbox id='"+this.id+"_required' onChange=\"markRequired("+this.id+");\"";
    if (this.required == 'true' || this.required == true) {

        str += " checked=true  ";
    }
    str += "></td><td class='tableCellHeader'><input type=text onChange=\"changeName("+this.id+");\" id=\""+this.id+"_name\" size=20 value=\""+this.name+"\"></td>";
    str += "<td class='tableCellHeader'><textarea id=\""+this.id+"_description\" rows=1 cols=50 class='attributeTextarea' onChange=\"changeDesc("+this.id+");\">";
    str += this.description + "</textarea></td></tr>";
    return str;
}

function AttributeList () {

    this.theAttributes = new Array();

}

AttributeList.prototype.add = function (obj) {

    this.theAttributes[this.theAttributes.length] = obj;
}

AttributeList.prototype.get = function (id) {
    for (i= 0; i <this.theAttributes.length; i++) {
        if (this.theAttributes[i].id == id) {
            return this.theAttributes [i];
            break;
        }
    }
}
AttributeList.prototype.getIndexed = function (idx) {
    return this.theAttributes[idx];
}

AttributeList.prototype.toString = function (){
     var str = "<table width='100%'><thead> ";
    str += "<tr><th class='tableCell' align='center' width=5><img src='/ibis/images/deleteIcon16x16.jpg' height=16 width=16 border=0 alt='delete' title='delete'/></th>";
    str += "<th class='tableCell' align='center' width=5><img src='/ibis/images/iconRequired.gif' height=16 width=16 border=0 alt='required' title='required'/></th> ";
    str += "<th class='tableCell' >Name</th>";
    str += "<th class='tableCell' >Description</th></tr></thead> ";
    str += "<tbody>";
    for (var i = 0; i < this.theAttributes.length; i++) {
        str += this.theAttributes[i].toString();
    }
    str += "</tbody>  </table>";
    return str;
}

AttributeList.prototype.append = function (attr) {
    this.theAttributes[this.theAttributes.length] = attr;
   // document.getElementById('attributeListDiv').innerHTML += attr.toString();
    
}
AttributeList.prototype.size = function () {
    return this.theAttributes.length;
}


function markDelete (attrId) {
    attr = attributes.get(attrId);
    elemId = attrId + "_delete";
    if (document.getElementById(elemId).checked) {
        attr.deleteIt = true;
    } else {
        attr.deleteIt = false;
    }
}

function markRequired (attrId) {
    attr = attributes.get(attrId);
    elemId = attrId + "_required";
    if (document.getElementById(elemId).checked) {
        attr.required = true;
    } else {
        attr.required = false;
    }
    if (attr.id != ""  && !(attr.id < 0)) {
        // not a new attribute, set update flag
        attr.update = 'true';
    }
}

function changeName (attrId) {
    attr = attributes.get(attrId);
    elemId = attrId + "_name";
    attr.name = document.getElementById(elemId).value;
    if (attr.id != ""  && !(attr.id < 0)) {

        // not a new attribute, set update flag
        attr.update = true;
    }
}


function changeDesc (attrId) {
    attr = attributes.get(attrId);
    elemId = attrId+"_description";
    attr.description = document.getElementById(elemId).value;
    if (attr.id != ""  && !(attr.id < 0)) {
        // not a new attribute, set update flag
        attr.update = 'true';
    }
}

function LocationAttributeList(){
    this.theAttributes = new Array();

}

LocationAttributeList.prototype = new AttributeList;
//LocationAttributeList.prototype.constructor = LocationAttributeList;

/*LocationAttributeList.prototype.toString = function () {
    var str= "<table > <tr><td class='tableCellHeaderBlack' width='20' align='right'>&nbsp;</td><td class='tableCellHeaderBlack' width='100'>Name</td><td class='tableCellHeaderBlack'>Value</td></tr>\n";
    for (i = 0; i < this.theAttributes.length; i++) {
        str += this.theAttributes[i].toString();
    }
    str += "</table>"
    if (this.theAttributes.length < 1) {
        str = "<table><tr><td colspan=3 class='tableCell'>There are no attributes defined for this location type.</td></tr></table>";
    }

    return str;
}
*/

LocationAttributeList.prototype.toString = function () {
    var str= "";
    for (var i = 0; i < this.theAttributes.length; i++) {
        str += this.theAttributes[i].toString();
    }
    if (this.theAttributes.length < 1) {
/*        str = "<span>There are no attributes defined for this location type.</span>";
*/        str = "";
    }
    return str;
}

LocationAttributeList.prototype.size = function () {
    return this.theAttributes.length;
}


LocationAttributeList.prototype.get = function (id) {
    for (i= 0; i <this.theAttributes.length; i++) {
        if (this.theAttributes[i].attribute.id == id) {
            return this.theAttributes [i];
            break;
        }
    }
}


function LocationAttribute (Id, Name, Desc, Required, Value) {
    this.attribute = new Attribute(Id, Name, Desc, Required);
    this.value = Value;
    
}
/*
LocationAttribute.prototype.toString = function () {
    var str = "<tr><td  align='right' class=";
    if (this.attribute.required == 'true') {
        str += "'requiredIndicator'> *";
    } else {
        str += "'formItemLabel'>&nbsp;";
    }
    str += "</td><td class='tableCell' >" + this.attribute.name + "</td>";
    str += " <td class='tableCell'><input type=text size=25 name='attrValue_"+this.attribute.id+"' id='attrValue_"+this.attribute.id;
    str += "' value=\""+this.value+"\" onkeyup=\"setAttributeValue('"+this.attribute.id+"', this.value);\"></td></tr>";
    return str;
}
*/
LocationAttribute.prototype.toString = function () {
	var id ="attrValue_"+this.attribute.id;
	var str = "<div class='formrow_2'><label ";
    if (this.attribute.required == 'true') {
        str += " class='requiredInput'> ";
    } else {
        str += ">";
    }
    str += this.attribute.name + "</label>";
    str += " <input type='text'  name='"+ id + "' id='"+id  + "' value='"+ this.value+ "' onkeyup=\"setAttributeValue('"+this.attribute.id+"', this.value);\" \/> </div>";
    str += " <div class='formrow_2'><label></label></div>";
    return str;
    
}

