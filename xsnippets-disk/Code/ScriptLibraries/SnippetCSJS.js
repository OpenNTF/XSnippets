
// Select (or highlight) text to be copied... 'obj' is the container dom object
function selectText(obj){
    if (dojo.isIE) {
        range = document.body.createTextRange();
        range.moveToElementText(obj);
        range.select();
    } else {
        selection = window.getSelection();
        range = document.createRange();
        range.setStartBefore(obj);
        range.setEndAfter(obj) ;
        selection.addRange(range);
    }
    window.scrollTo(0,0);
}

//Not used...
function copyCode(codeValue) {
    var content="";
	dojo.require("dijit.Dialog");
	
	myDlg = new dijit.Dialog({
        title: "Copy Snippet",
        style: "width: 500px; height:300px;"
    });
	
	content='Use "Ctrl-C" to copy the snippet code. You may close this dialog with "ESC"...<br/>';
	content+='<textarea id="codeEdit" class="copyContent"';  
	
	//to be embedded into CSS
	content+='style="width:100%; height:200px; border:0px; font-size:0.8em; color:gray;"'; //temporary  
	//****
	
	content+='>'+codeValue+'</textarea>';
	
	myDlg.attr("content", content)
	myDlg.show();
	
	dojo.byId("codeEdit").select();
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
	if(val && val!="Search snippets..."){
		var loc='/XSnippets.nsf/search.xsp?search='+encodeURIComponent(val);
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