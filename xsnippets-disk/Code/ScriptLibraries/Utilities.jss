
function getBrushFile(codeLanguage) {
	return configUtil.getSnippetLanguage(codeLanguage).getBrushFile();
}

function getBrushAlias(codeLanguage) {
	return configUtil.getSnippetLanguage(codeLanguage).getBrushAlias();
}

function getShowAs(codeLanguage) {
	return configUtil.getSnippetLanguage(codeLanguage).toString();
}


function getSnippetUNID(id, throwError) {
	if(id!=null && id!=""){
		var view1 = database.getView("SnippetsAll");
		var entry = view1.getEntryByKey(id, true);
		
		if(entry!=null) return entry.getUniversalID();
	}
	
	if(throwError) context.redirectToPage("errorSnippetNotFound");
	return "";
}

function getDownloadsById(id) {
	var lookupView = database.getView("DownloadsBySnippetSnapshot");
	if(id!=null){
		var entries = lookupView.getAllEntriesByKey(param.id);
		if (entries != null) {
			if (entries.getCount() > 0) {
				return entries.getFirstEntry().getColumnValues().get(2);
			}
		}
	}
	return 0;			
}

function getPersonDisplay(name) {
	var output = "";
	var profileUrl = configUtil["profileUrl"];
	if ("".equals(profileUrl)) {
		output = name;
	} else {
		output = '<a href="' + profileUrl + name + '" class="profileLink" target="_blank">' + name + '</a>';	
	}
	return output;
}

function convert2Uri(name) {
	var result:String=name.toLowerCase();
	var pattern:java.util.regex.Pattern;
	
	result=@ReplaceSubstring(result, 
			["?","&","@","£","$","%","^","<",">","*","/","'","#","~","(",")","+","=","!",";","\"",":",",","|","\\","{","}","[","]"],
			[""]
	);

	result=@ReplaceSubstring(result, 
			["--"," - "," "],
			["-"]
	);
	
	result=@ReplaceSubstring(result, 
			[" - ","--","---"],
			["-"]
	);

	result=java.text.Normalizer.normalize(result, java.text.Normalizer.Form.NFKD);
	
	pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	result=pattern.matcher(result).replaceAll("");	
	
	// dotless i cannot be normalized by Java!:
	pattern = java.util.regex.Pattern.compile("\\u0131");
	result=pattern.matcher(result).replaceAll("i");
	
	return result;
}

function generateUniqueId(name) {
	var idView=database.getView("SnippetsAll")
	var entry=null;
	var base_id=convert2Uri(name);
	var id="";
	var num=0;
	
	while(true) {
		id=base_id+((num==0)?"":"-"+num);

		entry = idView.getEntryByKey(id, true);
		if(entry==null) {
			return id;
		} else {
			num++;
		}
	}
}

function isContributor() {
	user=@UserName();

	if(user=="Anonymous") {
		return false;
	}

	roles=context.getUser().getRoles()
	
	// Here, code for checking...
	// Comment the following if for other checks...
	if(!roles.contains("[AppContributors]")) {
		return false;
	}

	return true;
}

function sendReport(snippetNoteId, reportReason, reportDetail) {
	var sendTo=@Explode(configUtil["reportRecipients"],";");
	var bodyStr="";
	var userName=session.createName(session.getEffectiveUserName()).getCommon();	

	var db:NotesDatabase=sessionAsSigner.getCurrentDatabase();
	var mailDoc:NotesDocument=db.createDocument();
	var snippetDoc:NotesDocument=db.getDocumentByID(snippetNoteId);
	
	var url=configUtil.getSnippetUrl(snippetDoc.getItemValueString("ID"));
	
	mailDoc.replaceItemValue("Form", "Memo");
	mailDoc.replaceItemValue("SendTo", sendTo);
	mailDoc.replaceItemValue("Subject", "Notification about XSnippet ("+snippetDoc.getItemValueString("Name")+")");
	
	bodyStr="The snippet ("+snippetDoc.getItemValueString("Name")+") has been reported by "+userName+"..."
	bodyStr+="\n\nThe Reason:\n\n"+reportReason
	bodyStr+="\n\n\nDetails:\n\n"+reportDetail
	bodyStr+="\n\n\nThis notification has been sent on "+@Now()+" by "+facesContext.getExternalContext().getRequest().getRemoteAddr();
	bodyStr+="\n\nURL for the XSnippet:\n\n"+url;

	mailDoc.replaceItemValue("Body", bodyStr);
	mailDoc.send(false);
}

function getBaseUrl() {
	url=context.getUrl();
	url.removeAllParameters();
	return url.getPath().replace(view.getPageName(), "");
}

function getSearchTerms(text) {
	
	if(null==text || "".equals(text)) return "";
	
	var searchFields=["Name","Author","Body","Tags","Notes"];
	var query="";

	for(i=0; i<searchFields.length; i++) {
		query+=(query==""?"":" OR ")+"(["+searchFields[i]+"] CONTAINS \""+text+"\")";
	}

	return query;
}

function keyPressSearchEnabled() {
	return (null==view.getViewProperty("disableKeyboardSearch"))
}