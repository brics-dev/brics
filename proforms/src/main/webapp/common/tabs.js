/**
* Copyright©2001 Garrett Smith
* Web: http://dhtmlitchen.com/
* This code may be used without permission, for free. This copyright must remain
* in the code. See http://dhtmlitchen.com/ for usage instructions and terms of use.
*/
var ua= navigator.userAgent;
var OPERA = (ua.indexOf("Opera") >= 0);
var OMNI = (ua.indexOf("Omni") > 0);
var MAC = (navigator.platform.indexOf("PPC") > 0);
var WIN = (navigator.platform=="Win32");
var IE5_0, IE5,IE6,IE5_5,N4,N5,NS6,MAC_IE5,MOZ;
if(!OPERA && !OMNI){
    IE6 = (ua.indexOf("MSIE 6") > 0);
    IE5_0 = (ua.indexOf("MSIE 5.0") > 0);
    IE5_5 = (ua.indexOf("MSIE 5.5") > 0);
    IE5 = (ua.indexOf("MSIE 5") > 0  || IE6 || IE5_5);
    N4 = (document.layers) ? 1 : 0;
    NS6= N5 = (ua.indexOf("Gecko") > 0);
    MAC_IE5 = (MAC && IE5) ? 1 : 0;
    MAC_IE5_1b = (MAC && ua.indexOf("MSIE 5.1b") >= 0);
    WIN_IE5= IE5 && !MAC;
    MOZ= N5 && !(ua.indexOf("Netscape") > 0);
}

var disabledTab = "";
var tabs, tabsClone, tabArray, activeTab, relatedTab = "f";

TabParams = {
    useCloneOnBottom : false,
    showRemoveLink : false,
    alwaysShowClone : false
};


function tabInit() {
    if(window.tabInited) return;

    if(!document.createElement || OPERA){
        TabParams.useCloneOnBottom = false;
        TabParams.alwaysShowClone = false;
        TabParams.showRemoveLink = false;
    }
    var propTop = getAbsY(document.getElementById("prop")) + 39;
    if(MAC_IE5) propTop = propTop-5;
    tabs= document.getElementById('tabs');
    tabs.style.top = propTop;
    tabs.getElementsByClass= objectGetElementsByClass;
    if (OPERA && !IE5) {
        tabArray = tabs.getElementsByTagName("span");
        activeTab = tabArray[0];
    }
    else {
        tabArray= tabs.getElementsByClass("*", "tab tabActive");
        activeTab= tabs.getElementsByClass("*", "tabActive")[0] || tabArray[0];
    }
    tabArray.isImg= tabArray[0].tagName.toLowerCase() == "img";
    IE5_0noImg= (IE5_0 && !tabArray.isImg && !MAC);
    if(!Boolean(tabs.cloneNode) || !Boolean(tabs.cloneNode(true))
        || tabArray.isImg) TabParams.useCloneOnBottom = false;


    if(TabParams.useCloneOnBottom){
        tabsClone = document.body.appendChild(tabs.cloneNode(true));
        tabsClone.className = "tabsClone";

        tabsClone.getElementsByClass= objectGetElementsByClass;
        tabsCloneArray= tabsClone.getElementsByClass("*", "tab tabActive");
    }

    var linesOfTabs = new Array();
    var tabsLen = 0;
    var tabsIdx = new Array();
   // var tabsLenEa = new Array();
    for (i = 0; i < tabArray.length; i++) {
        tabsLen += tabArray[i].offsetWidth;
//        alert (tabArray[i].offsetWidth + "width: " + tabArray[i].style.width);
        tabsIdx[tabsIdx.length] = i;
        if (i+1 < tabArray.length && (tabsLen + tabArray[i+1].offsetWidth + i*2) > 548) {  // if this is not the last tab  and the next tab is going to pass the tab container width...
            linesOfTabs[linesOfTabs.length] = new Array (tabsIdx, tabsLen);
            tabsIdx = new Array();
            tabsLen = 0;
            if(ua.indexOf("Mozilla/5.0") > -1) {
                tabArray[i].appendChild (document.createElement("BR"));
            }
        }
    }
    var tabsContainerHeightFactor = linesOfTabs.length;
    if (linesOfTabs.length > 0)  { // one line was added, must add the last line
           linesOfTabs[linesOfTabs.length] = new Array (tabsIdx, tabsLen);

       }
         // padd all tabs so the lines are the same
        // alert (linesOfTabs.length);
     for (i = 0; i < linesOfTabs.length; i++) {
           idxs = linesOfTabs[i][0];
         // container width - width of all tabs on line - (num tabs on line * 2) / num tabs on line
         // contianer width - width of all tabs = space to fill up
         // space to fill up - num tabs * 2 =  subtract another 2 for each tab, space between the tabs?
         // divide padding by number of tabs to add padding to.
           requiredPad = (548 - linesOfTabs[i][1] - (idxs.length * 2)) / idxs.length;  // approx
           //requiredPad -= 10;  // for tab images
         if (requiredPad <0 ) {
            // alert (548 - linesOfTabs[i][1]);
                        
         }
           curLen = 0;
           for (j =0; j < idxs.length; j++) {
               // kids = tabArray[idxs[j]].childNodes;
               tabArray[idxs[j]].childNodes[1].style.pixelWidth = (tabArray[idxs[j]].offsetWidth + requiredPad)-5;  // 5 is the difference between offset & pixelwidth?
            //   alert ( "pWidth : " + tabArray[idxs[j]].childNodes[1].style.pixelWidth +" offset w : "+ (tabArray[idxs[j]].offsetWidth+requiredPad));

               if ( i != linesOfTabs.length-1) {
                tabArray[idxs[j]].style.borderBottom = '0';
                }
           }

       }

       tabs.style.height =  (tabsContainerHeightFactor+1) * 22;
     /*

    alert (tabs.innerText.length);
    if (linesOfTabs > 0) {
    //more than one line, last line needs to be expanded.
        var numTabsInLastLine = 0;
        var tabEndLength = 0;
        var lastLineLength = tabs.innerText.length - (linesOfTabs * 67);
       // alert (lastLineLength);
        //alert (tabArray.length);
        for (ii = tabArray.length -1; ii >= 0; ii--) {
        //alert (ii);
        // start at end
            tabEndLength += tabArray[ii].innerText.length;
            numTabsInLastLine++;
            if (tabEndLength + tabArray[ii-1].innerText.length > lastLineLength) {
                break;
            }

        }
    }
    //alert (numTabsInLastLine);
    for (ii = 1; ii < numTabsInLastLine +1; ii++) {
        tabArray[tabArray.length - ii].style.width = '181px';
    }
    */


    // if (ua.indexOf("Mozilla/5.0") > -1
    //    && linesOfTabs.length > 0 ){
     //alert (ua);
   //     tabs.style.height = '40px';
   //     tabs.style.padding = "4px";
  //      tabs.style.overflowX = 'scroll';
  //  }



    var _tab;
    for(var i = 0;i < tabArray.length; i++){
    // 127.0.0.1/dhtmlkitchen/experiment/opera/tabs.js
    //javascript:alert("tabArray="+tabArray+"\ntabArray.length="+tabArray.length)
        if(!OPERA){
            _tab = tabArray[i];
            //alert ("tab id : " + _tab.id.substring(_tab.id.indexOf('tab')+3, _tab.id.length));
            contentNum = _tab.id.substring(_tab.id.indexOf('tab')+3, _tab.id.length);
            _tab.content= document.getElementById("content"+contentNum); //(i+1));
        }
        else {
            _tab =  document.getElementById("tab"+(i+1));
            _tab.num = i+1;
        }
        _tab.content.style.top = ((tabsContainerHeightFactor+1) * 22) + propTop;
        if(TabParams.useCloneOnBottom)
            tabsCloneArray[i].content= document.getElementById("content"+(i+1));

        // deal with any img tab.
        if(_tab.tagName.toLowerCase() == "img"){
            _tab["normalsrc"] = new String(_tab.src);
            _tab.hover= new Image();
            _tab.hover.src= _tab.getAttribute("hoversrc");
            _tab.active= new Image();
            _tab.active.src= _tab.getAttribute("activesrc");
        }

        // add Event Listeners for Moz, or override any given handler for IE.
        if(_tab.addEventListener){
            _tab.addEventListener("mouseover", hoverTab ,false);
            _tab.addEventListener("mouseout", hoverOff ,false);
            _tab.addEventListener("mousedown", depressTab ,false);
            if(TabParams.useCloneOnBottom){
                tabsCloneArray[i].addEventListener("mouseover", hoverTab ,false);
                tabsCloneArray[i].addEventListener("mouseout", hoverOff ,false);
                tabsCloneArray[i].addEventListener("mousedown", depressClonedTab ,false);
            }
        }
        else {
            _tab.onmouseover= hoverTab;
            _tab.onmouseout= hoverOff;
            _tab.onmousedown= depressTab;
            if(TabParams.useCloneOnBottom){
                tabsCloneArray[i].onmouseover= hoverTab;
                tabsCloneArray[i].onmouseout= hoverOff;
                tabsCloneArray[i].onmousedown= depressClonedTab;
            }
        }
        if(TabParams.useCloneOnBottom){
            tabsCloneArray[i].depressTab= depressClonedTab;
            tabsCloneArray[i].controller = _tab;
            _tab.bottomTab = tabsCloneArray[i];
        }
        _tab.depressTab= depressTab;

        if(TabParams.showRemoveLink){
            _tab.content.appendChild(document.createElement("br"))
            removeTabLink = new Object(
                _tab.content.removeTabLink =
                _tab.content.appendChild(document.createElement("a"))
            );
            removeTabLink.id = "removeTabLink"+(i+1);
            removeTabLink.href="javascript:removeTabs("+(i+1)+")";
            removeTabLink.appendChild(document.createTextNode("remove tabs"));

            // better to use a className for .removeTabLink in css
            removeTabLink.className = "removeTab";
        }

        // Unsightly IE 5.0 hacks (inline boxes not supported,
        // converting to block elements...)
        if(IE5_0noImg) {
            // add the width of previous tab plus padding

            tabs.tabOffset= tabs.tabOffset ? tabs.tabOffset : 0;
            var tabWidth = _tab.offsetWidth+18;

            _tab.style.left= tabs.tabOffset +"px";

            //add 9px padding to l and r sides
            _tab.style.width= tabWidth +"px";

            if(TabParams.useCloneOnBottom){
                tabsCloneArray[i].style.left= tabs.tabOffset +"px";
                tabsCloneArray[i].style.width = tabWidth +"px";
            }
            _tab.style.display= "block";
            _tab.style.position= "absolute";
            _tab.style.whiteSpace= "nowrap"; // css ignored here.
            if(TabParams.useCloneOnBottom){
                tabsCloneArray[i].style.display = "block";
                tabsCloneArray[i].style.position = "absolute";
                tabsCloneArray[i].style.whiteSpace = "nowrap"; // css ignored here.
            }
            // add the width of previous tab plus tab-spacing (4)
            tabs.tabOffset += parseInt(_tab.offsetWidth) + 4;
        }

        // Mac IE bug:
        // when a word ends in white space, Mac IE does not clone it
        // completetly. Mac IE removes the trailing white-space plus the
        // last letter of any word, so innerHTML is necessary.
        if(MAC_IE5 && TabParams.useCloneOnBottom)
            tabsCloneArray[i].innerHTML =  _tab.innerHTML;
            
		if (NS6)
        {
	        changeSelects(_tab);
        }
    }

    tabs.onchange= new Function();

    var q = String(window.location.search);
    if(q && q.indexOf("tab=") > 0)
        switchTabs(q.substring(q.indexOf("tab=")+4,q.indexOf("tab=")+8))
    // depress tab 1.
    if(tabArray.isImg)
        activeTab.src = activeTab.getAttribute("activesrc");

    else{
        activeTab.className = "tab tabActive";
        if(TabParams.useCloneOnBottom)
        activeTab.bottomTab.className = activeTab.className;
    }


    // Switch to a tab in the search string.
    // Unsightly IE hacks saved for last.
    if(IE5_0 && !MAC && !tabArray.isImg)
        window.defaultStatus= "IE 5.0 does not support inline boxes. Please upgrade to IE 5.5 or 6.0 for better performance.";

    if(TabParams.useCloneOnBottom){
        setTabsClonePosition();
        tabsClone.style.height = (activeTab.offsetHeight+1)+"px";
        showTabsCloneIfNecessary();
        if(window.addEventListener)
            window.addEventListener("resize",updateTabsClonePosition, false);
        else window.onresize = updateTabsClonePosition;
    }
    // Active the select boxes on the first tab for netscape
    if (NS6) {
	    activateSelects(activeTab);
    }

    setControlPosition();

    if(window.addEventListener)
        window.addEventListener("resize",updateControlPosition, false);
    else window.onresize = updateControlPosition;


    for (i = tabArray.length; i > 1; i--) {
        document.getElementById("content"+(i)).style.visibility = 'hidden';
        // added by Ching Heng
        document.getElementById("content"+(i)).style.display = 'none';
    }

    window.tabInited = true;
    
} // end tabInit


    function updateTabsClonePosition(e){
        if(activeTab != null)
            setTimeout("setTabsClonePosition();",500);
    }
    function setTabsClonePosition(){
        var adjustment = 0;
        if(MAC_IE5) adjustment = -1 *
            parseInt(activeTab.content.currentStyle.paddingBottom);
        else if(IE5_0) adjustment = -1;
        else adjustment = 1;
        tabsClone.style.top = (activeTab.content.offsetHeight
                            +activeTab.content.offsetTop+adjustment)+"px";
    }

    function updateControlPosition(e){
        if(activeTab != null)
            setTimeout("setControlPosition();",500);
    }

    function setControlPosition(){
        var adjustment = 0;
        if(MAC_IE5) adjustment = -1 *
            parseInt(activeTab.content.currentStyle.paddingBottom);
        else if(IE5_0) adjustment = -1;
        else adjustment = 1;
        document.getElementById("controlContent").style.top = (activeTab.content.offsetHeight
                            +activeTab.content.offsetTop+adjustment)+"px";
        document.getElementById("controlContent").style.visibility = "visible";
        // added by Ching Heng
        document.getElementById("controlContent").style.display = "";
    }

    function showControlIfNecessary(){

        var contentBottom = activeTab.content.offsetTop+
        activeTab.content.offsetHeight;
        var visibility =
            (contentBottom > getViewportHeight() || TabParams.alwaysShowClone) ?
             "visible" : "hidden";
        controlContent.style.visibility = visibility;
        //added by Ching Heng
        var display =
            (contentBottom > getViewportHeight() || TabParams.alwaysShowClone) ?
             "" : "none";
        controlContent.style.dispaly = display;
    }

    function showTabsCloneIfNecessary(){

        var contentBottom = activeTab.content.offsetTop+
        activeTab.content.offsetHeight;
        var visibility =
            (contentBottom > getViewportHeight() || TabParams.alwaysShowClone) ?
             "visible" : "hidden";
        tabsClone.style.visibility = visibility;
        // added by Ching Heng
        var display =
            (contentBottom > getViewportHeight() || TabParams.alwaysShowClone) ?
             "" : "none";
        tabsClone.style.display = display;
    }

    function switchTabs(tab,e,hash){
        try{
            document.getElementById(tab).depressTab(e);
        }
        catch(exception){}
        if(hash && document.getElementById(hash.substring(1)))
            scrollToElement(hash);
        else window.scrollTo(0,0);
    }
    function scrollToElement(hash){
        try{
            if(N5) window.location.href=hash;
            if(IE5) document.getElementById(hash.substring(1)).scrollIntoView();
        }
        catch(ex){}
    }

    /**
        tab methods
        hoverTab, hoverOff, and depressTab
        control the appearance of each tab
    */
    function hoverTab() {
        if(activeTab==this || activeTab==this.controller) return;
        if (this.content.id == disabledTab) { return; }
        this.className= "tabHover tab";
        this.src= this.getAttribute("hoversrc");
        if(TabParams.useCloneOnBottom){
            if(this.bottomTab) this.bottomTab.className = "tabHover tab";
            else this.controller.className = "tabHover tab";
        }

    }
    function hoverOff() {
        if(activeTab==this || activeTab==this.controller) return;
        this.className= "tab";
        this.src= this["normalsrc"];
        if(TabParams.useCloneOnBottom){
            if(this.bottomTab) this.bottomTab.className = "tab";
            else this.controller.className = "tab";
        }
    }
    function depressTab(e) {
//alert ('tab depressed  e: '+ e);

        if(activeTab == this) return;
        if (disabledTab == this.content.id) { return; }
        relatedTab= activeTab;
        this.className= "tab";
        this.className= "tab tabActive";
        this.src= this.getAttribute("activesrc");
        if(activeTab)
            resetTab(activeTab);

        activeTab= this;
        setControlPosition();
        if(TabParams.useCloneOnBottom){
            setTabsClonePosition();
            this.bottomTab.className=this.className;
            showTabsCloneIfNecessary();
        }
        tabs.onchange(e);
        if (NS6) {
	        activateSelects(activeTab);
        }
        if(!OPERA){
            this.content.style.visibility = "visible";
           //added by Ching Heng
           this.content.style.display = "";
        }
        else 
        {
	        document.getElementById("content"+this.num).style.visibility = "visible";
	        //added by Ching Heng
           document.getElementById("content"+this.num).style.display = "";
        }
    }

    function activateSelects (tab) {
	    
		var contentDiv = document.getElementById (tab.content.id);
		var children = contentDiv.getElementsByTagName ('SELECT');
		
		for (var j=0; j<children.length; j++) {	
				    	
		  	if (children[j].className == "ctdbSelect")
		   	{
		    	var elem = document.createElement ("select");
		    	elem.className="ctdbSelect";
		    	elem.name=children[j].name;
		    	elem.multiple=true;
				var elemsizeid = children[j].id + "_data";
				elem.size = document.getElementById (elemsizeid).value;
				elem.style.width = 125;
				elem.id = children[j].id;

				children[j].size=7;
		    	children[j].multiple=true;

		    	transferAllItem (document.getElementById (children[j].id), elem, 'YES');

				var parent = children[j].parentNode;
				parent.replaceChild (elem, children[j]);
				//parent.appendChild (data);

		    	setControlPosition();
		    	
		   	}
		}
    }
    // this method is called from the click event listener of a
    // bottom tab.
    function depressClonedTab(e){
        if(activeTab == this.controller) return;
        this.controller.depressTab(e);
        this.className= "tab";
        this.className= "tab tabActive";
        window.scrollTo(0,
            (tabsClone.offsetTop+ tabsClone.offsetHeight) - getViewportHeight()
            );


    }

    // this function resets a tab and
    // reset's the tabs bottom tab.
    function resetTab(tab) {
        tab.className= "tab";
        tab.src= tab["normalsrc"];
        if (NS6)
        {
	        changeSelects(tab);
        }
        if(activeTab.bottomTab)
            activeTab.bottomTab.className="tab";

        if(!OPERA){
            tab.content.style.visibility = "hidden";
            // added by Ching Heng
            tab.content.style.display = "none";
        }
        else{
	        document.getElementById("content"+tab.num).style.visibility = "hidden";
	        // added by Ching Heng
	        document.getElementById("content"+tab.num).style.display = "none";
        }
    }

	function changeSelects(tab){
//alert (' changing selects  tab : ' + tab.content.id );	    
		var contentDiv = document.getElementById (tab.content.id);
		var children = contentDiv.getElementsByTagName ('SELECT');
		
		for (var j=0; j<children.length; j++) {	
				    	
		  	if (children[j].className == "ctdbSelect")
		   	{
		    	var elem = document.createElement ("select");
		    	elem.className="ctdbSelect";
		    	elem.name=children[j].name;
		    	
				selectAllOptions (children[j]);
		    	transferItem (document.getElementById (children[j].id), elem, 'YES');
		    	
				var data = document.createElement ('hidden');
				data.value=children[j].size;
				data.id= children[j].id + "_data";
				
				elem.id = children[j].id;
				var parent = children[j].parentNode;
				parent.replaceChild (elem, children[j]);
				parent.appendChild (data);

		    	//children[j].replaceNode (elem);
		   	}
		}
	}	
    /**
    * objectGetElementsByClass  -- returns an array of descendant elements
    * whose class attribute contains the className param.
    *
    * tagName  -  If tagName if indeterminable, use "*".
    * className  -  one or more classNames.
    *   (If more than one className is used, they should be separated with a space.)
    */
    function objectGetElementsByClass(tagName, className){
        var collection;
        var returnedCollection = [];
        var classArray = className.split(" ");
        if(this.all && tagName == "*") collection = this.all;
        else collection = this.getElementsByTagName(tagName);
        for(var i = 0, counter = 0; i < collection.length; i++){
            if(!collection[i].className) continue;
            if (collection[i].className.indexOf("disabled") > -1 ) continue;
            var elmClassArray = collection[i].className.split(" ");
            jloop : for(var j = 0; j < elmClassArray.length; j++)
                for(var k = 0; k < classArray.length; k++)
                if(elmClassArray[j] == classArray[k]){
                        returnedCollection[counter++] = collection[i];
                        break jloop;
                }
        }
        return returnedCollection;
    }


function getScrollTop(){
    if(NS6) return(window.pageYOffset);
    if(IE6) return(document.documentElement.scrollTop);
    return(document.body.scrollTop);
}
function getViewportHeight(){
    if(NS6) return window.innerHeight;
    if(IE6) return document.documentElement.clientHeight;
    return document.body.clientHeight-2;
}


    /** removes all tabs.
    * sets content position to relative.
    * scrolls activeTab's content into view.
    */
function removeTabs(index){

    // remove tabs and scroll to activeTab.content.
    // if activeTab is tab1, noScrollFlag = true. don't scroll.
    var noScrollFlag = activeTab.id == "tab1";

    resetTab(activeTab);

    var removeTabLink;
    for(var i = 0; i < tabArray.length; i++)
        if(Boolean(tabArray[i].content) && tabArray[i].content.style){
            tabArray[i].content.style.position = "relative";
            if(i != 0){
                tabArray[i].content.style.borderTopWidth = "0";
                if(!MOZ) tabArray[i].content.style.top = "0";
            }
            if(i != tabArray.length-1)
                tabArray[i].content.style.borderBottomWidth = "0";

            else tabArray[i].content.style.marginBottom = "10px";

            tabArray[i].content.style.visibility = "visible";
			//added by Ching Heng 
            tabArray[i].content.style.display = "";
            
            removeTabLink = tabArray[i].content.removeTabLink;

            if( removeTabLink != null
            && removeTabLink.firstChild != null
            && removeTabLink.firstChild.nodeName =="#text") {

                var newIndex =
                    parseInt(removeTabLink.id.substring(removeTabLink.id.length-1));

                // set up activation for the next consecutive tab.
                if(newIndex < tabArray.length-1)
                    newIndex += 1;
                removeTabLink.href = "javascript:undoRemoveTabs('tab"+newIndex+"')";

                removeTabLink.firstChild.nodeValue = "show tabs";
            }
            if(NS6) tabArray[tabArray.length-1].content.style.marginBottom =
                tabArray[0].content.offsetTop + "px";

        }

        tabs.style.display="none";
        if(TabParams.useCloneOnBottom)tabsClone.style.display = "none";

        if(noScrollFlag) return;

        if(MOZ) window.scrollTo(0, document.getElementById("content"+index).offsetTop);
        else window.location.hash = 'content'+index;

 }


    /**
    *  sets content position to absolute.
    *  calls switchTabs with sTabToActivate.
    */
function undoRemoveTabs(sTabToActivate){

    for(var i = 0; i < tabArray.length; i++)
        if(Boolean(tabArray[i].content) && tabArray[i].content.style){
            tabArray[i].content.style.position = "absolute";
            if(i != 0){
                tabArray[i].content.style.top = "";
                tabArray[i].content.style.borderTopWidth = "";
            }

            if(i != tabArray.length-1) tabArray[i].content.style.borderBottomWidth = "";
            else tabArray[i].content.style.marginBottom = "";

            tabArray[i].content.style.visibility = "hidden";
            //added by Ching Heng
            tabArray[i].content.style.display = "none";

            var removeTabLink = tabArray[i].content.removeTabLink;
            if( removeTabLink != null
                && removeTabLink.firstChild != null
                && removeTabLink.firstChild.nodeName =="#text"){
                removeTabLink.firstChild.nodeValue = "remove tabs";
                removeTabLink.href = "javascript:removeTabs("+(i+1)+")";
            }
        }

        tabs.style.display="block";
        if(TabParams.useCloneOnBottom)
            tabsClone.style.display = "block";

        activeTab = null;
        switchTabs(sTabToActivate, null);
        activeTab = document.getElementById(sTabToActivate);
        if(NS6) tabArray[tabArray.length-1].content.style.marginBottom = "";

}

function getAbsX(elt) { return (elt.x) ? elt.x : getAbsPos(elt,"Left"); }
function getAbsY(elt) { return (elt.y) ? elt.y : getAbsPos(elt,"Top"); }
function getAbsPos(elt,which)
{
    iPos = 0;
    while (elt != null)
    {
        iPos += elt["offset" + which];
        elt = elt.offsetParent;
    }

    return iPos;
}

/*******************************************
 * transferAllItem(source, destination, sort)
 *
 * Transfers items from one select
 * box to a different text box. With 
 * an optional sort flag.
 *******************************************/
function transferAllItem(source, destination, sort)
{   var i = 0;
    var newitem;
    var retval = false;
        
    while (i < source.options.length)
    {  // if (source.options[i].selected) 
       // {   
	        var value=source.options[i].value;
	        var duplicate = false;
			for (j=0; j < destination.options.length; j++)
			{
				if (destination.options[j].value == value)
				{ // its a duplicate, dont add it
					duplicate = true;
				}
			} 
			if ( ! duplicate)
			{
	        	newitem = new Option(source.options[i].text, source.options[i].value, 0, 0);
            	destination.options[destination.options.length] = newitem;
            	retval = true;
            	source.options[i] = null;
            } 
            else
            {
	            source.options[i] = null;
	            i = i+1;
            }
        //}
        //else
         //   i = i + 1;
    }
    
    // Sort items 
    if(sort == "YES")
        sortItems(destination);
    else
        sortItems(source);
    
    return retval;
} // enf function transferItem()


function saveForm () {
	if (NS6) {
		var _tab;
    	for(var i = 0;i < tabArray.length; i++){
    // 127.0.0.1/dhtmlkitchen/experiment/opera/tabs.js
    //javascript:alert("tabArray="+tabArray+"\ntabArray.length="+tabArray.length)

        	if(!OPERA){
            	_tab = tabArray[i];
            	_tab.content= document.getElementById("content"+(i+1));
        	}
	        else {
    	        _tab =  document.getElementById("tab"+(i+1));
        	    _tab.num = i+1;
        	}
        	activateSelects (_tab);
        }
		
	}
	////selectOptions();
	document.questionAttributesForm.submit();
	return true;
}