	/************************************************************************************************************
	Floating window with tabs
	Copyright (C) October 2005  DTHMLGoodies.com, Alf Magne Kalleland
	
	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.
	
	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.
	
	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
	
	Dhtmlgoodies.com., hereby disclaims all copyright interest in this script
	written by Alf Magne Kalleland.
	
	Alf Magne Kalleland, 2006
	Owner of DHTMLgoodies.com
		
	************************************************************************************************************/	
	
	
	var activeFloatingWindow = false;
	var activeFloatingWindowContent = false;
	var activeFloatingWindowTabDiv = false;
	var activeFloatingWindowStatusDiv = false;
	var activeFloatingWindowMoveBar = false;

	var currentZIndex = 1000;
	var floatingWindowMaxZIndex = 1000;
	var floatingWindowMinimumWidth = new Array();
	var floatingWindowTabDivs = new Array();
	
	var MSIE = navigator.userAgent.indexOf('MSIE')>=0?true:false;
	var navigatorVersion = navigator.appVersion.replace(/.*?MSIE (\d\.\d).*/g,'$1')/1;

	
	var floatingWindowMoveCounter = -1;
	var floatingWindowResizeCounter = -1;
	var initEventX = false;
	var initEventY = false;
	var window_posX = false;
	var window_posY = false;
	var window_height = false;
	var window_width = false;

    var floating_window_skin = 3;

    var floatingWindowStates = new Array();
	
	
	switch(floating_window_skin){
		case 2:
		//	document.getElementById('cssRef').href = 'floating_window_with_tabs-skin2.css';
		
			break;
		default:
			
			var tabRightInActive = "/ctdb/images/dhtmlPortlet/images/tab_right_inactive.gif";
			var tabRightActive = "/ctdb/images/dhtmlPortlet/images/tab_right_active.gif";
			var tabImagePrefix = "/ctdb/images/dhtmlPortlet/images/tab_right_";
			var closeButtonMouseOverColor = '#006600';
			break;
			
			
		
	}
			
	/*
	These cookie functions are downloaded from 
	http://www.mach5.com/support/analyzer/manual/html/General/CookiesJavaScript.htm
	*/	
	function Get_Cookie(name) { 
	   var start = document.cookie.indexOf(name+"="); 
	   var len = start+name.length+1; 
	   if ((!start) && (name != document.cookie.substring(0,name.length))) return null; 
	   if (start == -1) return null; 
	   var end = document.cookie.indexOf(";",len); 
	   if (end == -1) end = document.cookie.length; 
	   return unescape(document.cookie.substring(len,end)); 
	} 
	// This function has been slightly modified
	function Set_Cookie(name,value,expires,path,domain,secure) { 
		expires = expires * 60*60*24*1000;
		var today = new Date();
		var expires_date = new Date( today.getTime() + (expires) );
	    var cookieString = name + "=" +escape(value) + 
	       ( (expires) ? ";expires=" + expires_date.toGMTString() : "") + 
	       ( (path) ? ";path=" + path : "") + 
	       ( (domain) ? ";domain=" + domain : "") + 
	       ( (true) ? ";secure" : "");
	    document.cookie = cookieString; 
	}	
		
	function cancelWindowEvent()
	{
		return (floatingWindowMoveCounter==-1 && floatingWindowResizeCounter==-1 && this.tagName!='IMG')?true:false;
	}
	
	function showHideWindowTab()
	{

		var parentEl = this.parentNode;
		if(!activeFloatingWindow)activeFloatingWindow = parentEl.parentNode;
		var windowIndex = parentEl.parentNode.id.replace(/[^\d]/g,'');
		var subDiv = parentEl.getElementsByTagName('DIV')[0];
		counter=0;		
		var contentDiv = contentDivs[windowIndex][0];
		var tabFound = false;

		do{			
			if(subDiv.tagName=='DIV' && subDiv.className!='floatingWindowCloseButton'){
				if(!tabFound)zIndex = counter;else zIndex = 100-counter;
				if(subDiv!=this){
					subDiv.className = 'floatingWindowTab_inactive';
                    var rightSpan = subDiv.getElementsByTagName('SPAN')[1];
                    rightSpan.style.backgroundImage = 'url(/ctdb/common/dhtmlPortlet/images/tab_right_inactive.gif)';
                    //var img = subDiv.getElementsByTagName('IMG')[0];
				//	img.src = tabRightInActive
					subDiv.style.zIndex = zIndex;
					contentDiv.style.display='none';		
				}else{
					this.className='floatingWindowTab_active';
					this.style.zIndex = 500;
                    var rightSpan = subDiv.getElementsByTagName('SPAN')[1];
                    rightSpan.style.backgroundImage = 'url(/ctdb/common/dhtmlPortlet/images/tab_right_active.gif)';
				//		var img = this.getElementsByTagName('IMG')[0];
				//	img.src = tabRightActive;
					if(floatingWindowStates[activeFloatingWindow.id.replace(/[^0-9]/gi,'')])
						contentDiv.style.display=floatingWindowStates[activeFloatingWindow.id.replace(/[^0-9]/gi,'')];
					else
						contentDiv.style.display='block'
						
					tabFound = true;	
					Set_Cookie('floating_window_activeTab' + windowIndex,counter,100);		
				}
				counter++;
			}
			subDiv = subDiv.nextSibling;
			if(contentDiv.nextSibling)contentDiv = contentDiv.nextSibling;
		}while(subDiv);		
		
	}
	
	function toggleCloseButton()
	{
		this.style.color='#FFF';
		this.style.backgroundColor = closeButtonMouseOverColor;	
	}
	function toggleOffCloseButton()
	{
		this.style.color='';
		this.style.backgroundColor = '';	
	}
	
	function closeFloatingWindow()
	{
		this.parentNode.parentNode.style.display='none';
		
	}
	
	function minimizeFloatingWindow()
	{
        
        var action = this.getAttribute('action');
		var aTag = this.getElementsByTagName('SPAN')[0];	
		if(action=='minimize'){
			this.setAttribute('action','maximize');
			activeFloatingWindowContent.style.display='none';
			aTag.style.top = '-10px';
		}else{
			this.setAttribute('action','minimize');
			activeFloatingWindowContent.style.display='block';
			aTag.style.top = '0px';
		}
		floatingWindowStates[activeFloatingWindow.id.replace(/[^0-9]/gi,'')] = activeFloatingWindowContent.style.display;
	}
	
	

	
	function initFloatingWindowResize(e)
	{
		if(document.all)e = event;
		floatingWindowResizeCounter = 0;	
		
		bringFloatingWindowToFront(false,this.parentNode.parentNode);

		initEventX = e.clientX;
		initEventY = e.clientY;
		window_height = activeFloatingWindowContent.offsetHeight;
		window_width = activeFloatingWindow.offsetWidth;
		
			
		timerFloatingWindowMove();
		return false;
	}
	
	function initFloatingWindowMove(e)
	{
		if(document.all)e = event;	
		floatingWindowMoveCounter = 0;		
		bringFloatingWindowToFront(false,this.parentNode);

		initEventX = e.clientX;
		initEventY = e.clientY;
		window_posX = activeFloatingWindow.style.left.replace('px','')/1;
		window_posY = activeFloatingWindow.style.top.replace('px','')/1;


		timerFloatingWindowMove();
		return false;
		
	}
	
	function timerFloatingWindowMove()
	{
		if(floatingWindowMoveCounter>=0 && floatingWindowMoveCounter<10){
			floatingWindowMoveCounter = floatingWindowMoveCounter + 1;
			setTimeout('timerFloatingWindowMove()',10);
		}		
		if(floatingWindowResizeCounter>=0 && floatingWindowResizeCounter<10){
			floatingWindowResizeCounter = floatingWindowResizeCounter + 1;	
			setTimeout('timerFloatingWindowMove()',10);
		}
	}
	var allowFloatingResize = true;
	var windowMoveInProgress = false;
	
	function floatingWindowMove(e)
	{
		if(windowMoveInProgress)return;
		windowMoveInProgress = true;
		if(document.all)e = event;
		if(floatingWindowMoveCounter>=10){
			var leftPos = window_posX + e.clientX - initEventX;
			var topPos = window_posY + e.clientY - initEventY;
			if(topPos<0)topPos=0;
			if(leftPos<0)leftPos=0;
			activeFloatingWindow.style.left = leftPos + 'px';
			activeFloatingWindow.style.top = topPos + 'px';			
			
			windowMoveInProgress = false;
			return;
		}		
		
		if(floatingWindowResizeCounter>=10 && allowFloatingResize){
			var width = window_width + e.clientX - initEventX;
			var height = window_height + e.clientY - initEventY;
			if(width<floatingWindowMinimumWidth[activeFloatingWindow.id])width = floatingWindowMinimumWidth[activeFloatingWindow.id];
			if(height<0)height = 0;
			activeFloatingWindowMoveBar.style.width = (width - 4) + 'px';
			activeFloatingWindow.style.width = width + 'px';
			var numericID = activeFloatingWindow.id.replace(/[^\d]/g,'');
			for(var no=0;no<contentDivs[numericID].length;no++){
				if(navigatorVersion<6 && MSIE){
					contentDivs[numericID][no].style.width = (width)  + 'px';
				}else{
					contentDivs[numericID][no].style.width = (width - 4)  + 'px';
				}
				contentDivs[numericID][no].style.height = height + 'px';					
				
			}
			
			if(navigatorVersion<6 && MSIE){
				activeFloatingWindowMoveBar.style.width = (width) + 'px';
			}
			
			
			
			if(document.all){
				allowFloatingResize = false;
				setTimeout('allowFloatingResize=true',30);
			}	
			
			windowMoveInProgress = false;	
		}	
		windowMoveInProgress = false;	
	}
	
	function initSetSize(windowObj,width,height)
	{
		if(width<floatingWindowMinimumWidth[windowObj.id])width = floatingWindowMinimumWidth[windowObj.id];
		windowObj.style.width = width + 'px';
		var subDivs = windowObj.getElementsByTagName('DIV');
		for(var no=0;no<subDivs.length;no++){
			if(subDivs[no].className=='floatingWindowContent')subDivs[no].style.width = (width-4) + 'px';
			if(subDivs[no].className=='floatingWindowContent' && height)subDivs[no].style.height = height + 'px';
		}		
	}	
	
	
	
	function floatingWindowStopMove(e)
	{
		if(floatingWindowMoveCounter>=0){
			floatingWindowMoveCounter = -1;	
			if(activeFloatingWindow)Set_Cookie(activeFloatingWindow.id,activeFloatingWindow.style.left.replace('px','') + '_' + activeFloatingWindow.style.top.replace('px',''),100);	
		}
		if(floatingWindowResizeCounter>=0){
			floatingWindowResizeCounter = -1;
			Set_Cookie('floating_window_size' + activeFloatingWindow.id.replace(/[^\d]/g,''),activeFloatingWindow.style.width.replace('px','') + '_' + activeFloatingWindowContent.style.height.replace('px',''),100);
		}
	}
	
	function addMovingBar(inputObj,noMove)
	{
		var div = document.createElement('DIV');
		div.className='floatingWindow_moveBar';
		div.style.zIndex = 1000;
		div.style.position = 'relative';
		div.innerHTML = '<span></span>';
		if(!noMove)div.onmousedown = initFloatingWindowMove;
		inputObj.appendChild(div);	
		
	}
	
	
	function addTabs(inputObj,tabs,noCloseButton,noMinimizeButton)
	{
		var div = document.createElement('DIV');
		div.className='floatingWindow_topRow';
		div.style.zIndex = 1000;
		div.style.position = 'relative';
        floatingWindowMinimumWidth[inputObj.id] = 0;
		inputObj.appendChild(div);
        floatingWindowTabDivs[inputObj.id] = div;
		
		var currentWidth = 0;
		var activeTabIndexCookie = Get_Cookie('floating_window_activeTab' + inputObj.id.replace(/[^\d]/g,''));
		if(!activeTabIndexCookie)activeTabIndexCookie=0;
		for(var no=0;no<tabs.length;no++){			
			var tabDiv = document.createElement('DIV');
			tabDiv.onselectstart = cancelWindowEvent;
			tabDiv.ondragstart = cancelWindowEvent;
			if(no==activeTabIndexCookie){
				suffix = 'active'; 
				color_picker_active_tab = this;
			}else suffix = 'inactive';
			
			tabDiv.id = 'floatingWindowTab' + no;
			tabDiv.onclick = showHideWindowTab;
			div.appendChild(tabDiv);
			if(no==activeTabIndexCookie){
				tabDiv.style.zIndex = 50; 
				var tmpDiv = tabDiv;
				while(tmpDiv.previousSibling){
					var tmpCounter = 10;
					tmpDiv = tmpDiv.previousSibling;
					if(tmpDiv.tagName=='DIV'){
						tmpDiv.style.zIndex = tmpCounter;
						tmpCounter--;
					}	
				}
				
			}else tabDiv.style.zIndex = 1 + (tabs.length-no);
			tabDiv.style.left = currentWidth + 'px';
			tabDiv.style.position = 'absolute';

            tabDiv.className='floatingWindowTab_' + suffix;
           

            var tabSpan = document.createElement('SPAN');
            tabSpan.innerHTML = tabs[no];
            tabSpan.style.paddingLeft = "6px";
            tabDiv.appendChild(tabSpan);
            var tabRightSpan = document.createElement('SPAN');
            tabRightSpan.className='floatingWindowTab_' + suffix +'Right';
            tabRightSpan.style.height = "17px";
            tabRightSpan.style.width="22px";
            tabDiv.appendChild(tabRightSpan);
            
          //  var tabImg = document.createElement('IMG');
			//tabImg.src = tabImagePrefix + suffix + ".gif";
          //  tabDiv.appendChild(tabImg);
            if(navigatorVersion<6 && MSIE){	/* Lower IE version fix */
                tabSpan.style.position = 'relative';
				tabImg.style.position = 'relative';
				tabImg.style.left = '-3px';		
				tabDiv.style.cursor = 'hand';	
			}

           // alert (tabDiv.parentNode.innerHTML);
            if (tabSpan.clientWidth != 0) {
                currentWidth = currentWidth + tabSpan.clientWidth + 22 - 11;
            } else {
                currentWidth = currentWidth + tabSpan.innerHTML.length *6+ 22 -11;
            }
            if(navigator.userAgent.indexOf('Opera')>=0)currentWidth = currentWidth + 22;
			floatingWindowMinimumWidth[inputObj.id] = floatingWindowMinimumWidth[inputObj.id] + tabSpan.offsetWidth + 22 - 11;
			
			if(navigator.userAgent.indexOf('Opera')>=0){
				currentWidth = currentWidth - 22;
				floatingWindowMinimumWidth[inputObj.id] = floatingWindowMinimumWidth[inputObj.id] - 22;
			}
		
		}
		floatingWindowMinimumWidth[inputObj.id] = floatingWindowMinimumWidth[inputObj.id] + 14;
		if(!noCloseButton){
			floatingWindowMinimumWidth[inputObj.id]+=18;
			var closeButton = document.createElement('DIV');
			closeButton.className='floatingWindowCloseButton';
			closeButton.innerHTML = 'x';
			closeButton.onclick = closeFloatingWindow;
			closeButton.onmouseover = toggleCloseButton;
			closeButton.style.top = '1px';
			closeButton.onmouseout = toggleOffCloseButton;
			div.appendChild(closeButton);
			if(navigatorVersion<6 && MSIE){
				closeButton.style.top = '1px';	
			}
		}
		
		if(!noMinimizeButton){
			floatingWindowMinimumWidth[inputObj.id]+=18;
			var minimizeButton = document.createElement('DIV');
			minimizeButton.className='floatingWindowCloseButton';
			minimizeButton.setAttribute('action','minimize');
			minimizeButton.action = 'minimize';
			minimizeButton.id = 'minimize_' + inputObj.id;
			minimizeButton.innerHTML = '<span style="position:relative">_</span>';
			minimizeButton.style.overflow = 'hidden';
            minimizeButton.style.height="16px";
            minimizeButton.style.width="14px";
            minimizeButton.style.marginBottom="1px";
            minimizeButton.onclick = minimizeFloatingWindow;
			minimizeButton.onmousedown = bringFloatingWindowToFront;
			minimizeButton.onmouseover = toggleCloseButton;
			minimizeButton.onmouseout = toggleOffCloseButton;
			div.appendChild(minimizeButton);
			
			minimizeButton.style.top = '1px';	
			
			if(!noCloseButton)minimizeButton.style.right = '18px';
		
		}
	}
	
	
	
	function createStatusBar(inputObj,noResize)
	{
		var div = document.createElement('DIV');
		div.style.position = 'relative';
		div.className='floatingWindow_statusBar';	
		var innerSpan = document.createElement('SPAN');
		innerSpan.id = 'floatingWindow_statusBarTxt';
		div.appendChild(innerSpan);
		inputObj.appendChild(div);
		
		if(noResize){			

			return;
				
		}
		var img = document.createElement('IMG');
		img.src = '/ctdb/common/dhtmlPortlet/images/floating_window_resize.gif';
		img.onmousedown = initFloatingWindowResize;
		img.ondragstart = cancelWindowEvent;
		div.appendChild(img);
		
	}
		
	var contentDivs = new Array();
	var countWindows = 0;
	
	function bringFloatingWindowToFront(e,inputObj)
	{
		if(floatingWindowMaxZIndex>currentZIndex)currentZIndex = floatingWindowMaxZIndex/1 + 1;
		if(!inputObj)inputObj = this;
		
		activeFloatingWindow = inputObj;
		activeFloatingWindow.style.zIndex = currentZIndex+1;
		var numericID = activeFloatingWindow.id.replace(/[^\d]/g,'');
		
		var subDivs = new Array();
		if(floatingWindowTabDivs[activeFloatingWindow.id])subDivs = floatingWindowTabDivs[activeFloatingWindow.id].getElementsByTagName('DIV');
		
		for(var no=0;no<subDivs.length;no++){
			if(subDivs[no].className=='floatingWindowTab_active'){
				activeFloatingWindowContent = contentDivs[numericID][no];
				break;
			}
		}

		activeFloatingWindowStatusDiv = false;
		activeFloatingWindowTabDiv = false;
		var divs = activeFloatingWindow.getElementsByTagName('DIV');
		for(var no=0;no<divs.length;no++){
			if(divs[no].className=='floatingWindow_statusBar')activeFloatingWindowStatusDiv = divs[no];
			if(divs[no].className=='floatingWindow_topRow')activeFloatingWindowTabDiv = divs[no];
			if(divs[no].className=='floatingWindow_moveBar')activeFloatingWindowMoveBar = divs[no];
		}
		
		Set_Cookie('floating_window_zIndex' + numericID,currentZIndex,100);
		currentZIndex++;
	}
	
	
	function initFloatingWindowWithTabs(windowID,tabs,windowHeight,windowWidth,leftPos,topPos,noScrollbars,noResize,noStatusBar,noCloseButton,noMinimizeButton,noMove)
	{
		contentDivs[countWindows] = new Array();
        var el = document.getElementById(windowID);
		el.id = 'dhtmlgoodies_floating_window' + countWindows;
		el.style.overflow = 'hidden';
		el.className = 'dhtmlgoodies_floatingWindow';
		el.onmousedown = bringFloatingWindowToFront;
		var cookieValue = Get_Cookie(el.id) + '';
		
		if(cookieValue.length>0 && cookieValue!='null'){
			var cookieItems = cookieValue.split('_');			
			leftPos = cookieItems[0];
			topPos = cookieItems[1];
		}	
		
		try{
			el.style.left = leftPos + 'px';
			el.style.top = topPos + 'px';
		}catch(e){
			
		}
		el.style.zIndex = currentZIndex;
		var contentDiv = el.getElementsByTagName('DIV')[0];
		contentDivs[countWindows].push(contentDiv);
		while(contentDiv.nextSibling){
			contentDiv = contentDiv.nextSibling;
			if(contentDiv.className=='floatingWindowContent'){
				contentDivs[countWindows].push(contentDiv);			
			}	
		}		
		
		addMovingBar(el,noMove);
		if(tabs)addTabs(el,tabs,noCloseButton,noMinimizeButton);else floatingWindowMinimumWidth[el.id]	 = 40;
		
		var activeTabIndexCookie = Get_Cookie('floating_window_activeTab' + countWindows);
		if(!activeTabIndexCookie)activeTabIndexCookie=0;	
		for(var no=0;no<contentDivs[countWindows].length;no++){
			if(no==activeTabIndexCookie)contentDivs[countWindows][no].style.display='block'; else contentDivs[countWindows][no].style.display='none';
			el.appendChild(contentDivs[countWindows][no]);
			if(windowHeight)contentDivs[countWindows][no].style.height = (windowHeight - 46) + 'px';
			if(noScrollbars){
				contentDivs[countWindows][no].style.overflow='hidden';
			}
			if(navigator.userAgent.indexOf('Opera')>=0 && !noScrollbars){
				contentDivs[countWindows][no].style.overflow = 'scroll';
			}			
			contentDivs[countWindows][no].style.zIndex = 100;
			contentDivs[countWindows][no].style.position = 'relative';
		}	
		
		if(MSIE){
			var iframe = document.createElement('<IFRAME frameborder="0" src="about:blank">');
			iframe.style.position = 'absolute';
			iframe.style.width = '1000px';
			iframe.style.height = '1000px';
			iframe.style.top = '0px';
			iframe.style.left = '0px';
			contentDivs[countWindows][0].parentNode.insertBefore(iframe,contentDivs[countWindows][0]);
		}
		

		if(!noStatusBar && !noResize)createStatusBar(el,noResize);
		if(countWindows==0){
			document.documentElement.onmousemove = floatingWindowMove;
			document.documentElement.onmouseup = floatingWindowStopMove;
		}
		
		var cookieSize = Get_Cookie('floating_window_size' + el.id.replace(/[^\d]/g,''));

		if(cookieSize){
			var sizeArray = cookieSize.split('_');
			initSetSize(el,sizeArray[0],sizeArray[1]);			
		}else{
			var sizeArray = [windowWidth,windowHeight];	
			initSetSize(el,sizeArray[0],sizeArray[1]);	
		}
		
		var cookieZIndex = Get_Cookie('floating_window_zIndex' + el.id.replace(/[^\d]/g,''));
		if(cookieZIndex){
			el.style.zIndex = cookieZIndex;
			if(!floatingWindowMaxZIndex)floatingWindowMaxZIndex = 0;
			floatingWindowMaxZIndex = Math.max(floatingWindowMaxZIndex,cookieZIndex/1 + 1);
		}
		el.onselectstart = cancelWindowEvent;
		countWindows++;
		currentZIndex++;
       

    }
	