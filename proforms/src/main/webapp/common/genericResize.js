/////////////////////////////////////////////////////////////////////////
// Generic Resize by Erik Arvidsson                                    //
//                                                                     //
// You may use this script as long as this disclaimer is remained.     //
// See www.dtek.chalmers.se/~d96erik/dhtml/ for mor info               //
//                                                                     //
// How to use this script!                                             //
// Link the script in the HEAD and create a container (DIV, preferable //
// absolute positioned) and add the class="resizeMe" to it.            //
/////////////////////////////////////////////////////////////////////////

var theobject = null; //This gets a value as soon as a resize start

var thisPage = null;
function resizeObject() {
	this.el        = null; //pointer to the object
	this.dir    = "";      //type of current resize (n, s, e, w, ne, nw, se, sw)
	this.grabx = null;     //Some useful values
	this.graby = null;
	this.width = null;
	this.height = null;
	this.left = null;
	this.top = null;
}
	

//Find out what kind of resize! Return a string inlcluding the directions
function getDirection(el) {
	var xPos, yPos, offset, dir;
	dir = "";

	xPos = window.event.offsetX;
	yPos = window.event.offsetY;

	
	offset = 4; //The distance from the edge in pixels

	if (yPos<offset) dir += "n";
	else if (yPos > el.offsetHeight-offset) dir += "s";
	if (xPos<offset) dir += "w";
	else if (xPos > el.offsetWidth-offset) dir += "e";

	return dir;
}

function doDown() {
	var el = getReal(event.srcElement, "className", "resizeMe");

	if (el == null) {
		theobject = null;
		return;
	}		

	dir = getDirection(el);
	if (dir == "") return;

	theobject = new resizeObject();
		
	theobject.el = el;
	theobject.dir = dir;

	theobject.grabx = window.event.clientX;
	theobject.graby = window.event.clientY;
	theobject.width = el.offsetWidth;
	theobject.height = el.offsetHeight;
	theobject.left = el.offsetLeft;
	theobject.top = el.offsetTop;

	window.event.returnValue = false;
	window.event.cancelBubble = true;
}

function doUp() {
	if (theobject != null) {
		theobject = null;
	}
	if (thisPage != null) {
	    if (thisPage == "CTDB_TEXTAREA_HELP") {
	        updateThisPage();
        }
    }
}

function doMove() {
	var el, xPos, yPos, str, xMin, yMin;
	xMin = 8; //The smallest width possible
	yMin = 8; //             height


	el = getReal(event.srcElement, "className", "resizeMe");

	if ( el != null && el.className == "resizeMe") {
		str = getDirection(el);
	//Fix the cursor	
		if (str == "") str = "default";
		else str += "-resize";
		el.style.cursor = str;
	}
	
//Dragging starts here
	if(theobject != null) {
	//alert (  theobject.el.id);
	if (theobject.el.id == "resizableDiv") {
	    xMin = 167;
	    yMin = 395;
    }
		if (dir.indexOf("e") != -1)
			theobject.el.style.width = Math.max(xMin, theobject.width + window.event.clientX - theobject.grabx);
	
		if (dir.indexOf("s") != -1)
			theobject.el.style.height = Math.max(yMin, theobject.height + window.event.clientY - theobject.graby);		

		if (dir.indexOf("w") != -1) {
			theobject.el.style.left = Math.min(theobject.left + window.event.clientX - theobject.grabx, theobject.left + theobject.width - xMin);
			theobject.el.style.width = Math.max(xMin, theobject.width - window.event.clientX + theobject.grabx);
		}
		if (dir.indexOf("n") != -1) {
			theobject.el.style.top = Math.min(theobject.top + window.event.clientY - theobject.graby, theobject.top + theobject.height - yMin);
			theobject.el.style.height = Math.max(yMin, theobject.height - window.event.clientY + theobject.graby);
		}
		
		window.event.returnValue = false;
		window.event.cancelBubble = true;
	} 
}


function getReal(el, type, value) {
	temp = el;

	while ((temp != null) && (temp.tagName != "BODY")) {
		if (eval("temp." + type) == value) {
			el = temp;
			return el;
		}
		temp = temp.parentElement;
	}
	return null;
}

document.onmousedown = doDown;
document.onmouseup   = doUp;
document.onmousemove = doMove;


