
// Select (or highlight) text to be copied... 'obj' is the container dom object
function selectText(obj){
    if (dojo.isIE) {
        range = document.body.createTextRange();
        range.moveToElementText(obj);
        range.select();
    } else {
        selection = window.getSelection();
        
        if(selection.rangeCount > 0) selection.removeAllRanges();
        
        range = document.createRange();
        range.setStartBefore(obj);
        range.setEndAfter(obj) ;
        selection.addRange(range);
    }
    window.scrollTo(0,0);
}

function searchBoxIn(panelId, inputId, w) {
	if(dojo.byId(inputId).value=="Search snippets...") {
		dojo.byId(inputId).value="";
	}
	 dojo.animateProperty(
	    {
	      node: panelId,duration: 500,  properties: { width: {end: w}  }
	    }).play();
}

function searchBoxOut(panelId, inputId) {
	if(dojo.byId(inputId).value=="") {
		dojo.byId(inputId).value="Search snippets...";
	}
}

function searchSubmit(id) {
	var val=XSP.getFieldValue(XSP.getElementById(id));
	var baseUrl=XSP.getFieldValue(XSP.getElementById("baseUrl"));
	
	if(val && val!="Search snippets..."){
		var loc=baseUrl+'/search.xsp?search='+encodeURIComponent(val);
		window.location.href=loc;
	}
}

function installSearch(xmlFile) {
	if (window.external && ("AddSearchProvider" in window.external)) {
		// if supported
		window.external.AddSearchProvider(xmlFile);
	} else {
		// No search engine support
		alert("This browser does not support adding search engines...");
	}
}

