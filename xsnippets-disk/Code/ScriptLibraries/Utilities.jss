function getConfig(config, keyName) {

	var keys = config.getKeys();
	var hasKey = false;
	for (keys ; keys.hasMoreElements() ;) {
		var key = keys.nextElement();
		if (key == keyName) hasKey = true;
	}
	
	if (hasKey) {
		var value = config[keyName];
		if (value) {
			if (value != "") {
				return value;
			}
		}
	}
	return null;
}

// Should be moved to some kind of parametrization...
function getCodeLanguages() {

// **** Caching is disabled for development...
//	if(applicationScope.containsKey("CodeLanguages")) {
//		return applicationScope.get("CodeLanguages");
//	}
// ********************************************
	
	result=[	"XPages",
	        	"JavaScript (Server)", 
	        	"JavaScript (Client)", 
	        	"JavaScript",
	        	"Java",
	        	"XML",
	        	"LotusScript",
	        	"Cascaded Style Sheets", 
	        	"Themes" 
	];
	
	return result;

// **** Caching is disabled for development...
//	applicationScope.put("CodeLanguages", result);
//	return applicationScope.get("CodeLanguages");
// ********************************************
}

//Should be moved to some kind of parametrization...
function getBrushInfo(codeLanguage) {
	result={BrushFile:"", BrushAlias:"", ShowAs:"" };

	switch(codeLanguage) {

	case "Cascaded Style Sheets": 
		result.BrushFile="shBrushCss_custom.js";
		result.BrushAlias="css";
		result.ShowAs="CSS";
		break;

	case "JavaScript (Server)": 
		result.BrushFile="shBrushJScript_custom.js";
		result.BrushAlias="js";
		result.ShowAs="SSJS";
		break;

	case "JavaScript (Client)":
		result.BrushFile="shBrushJScript_custom.js";
		result.BrushAlias="js";
		result.ShowAs="CSJS";
		break;

	case "JavaScript":
		result.BrushFile="shBrushJScript_custom.js";
		result.BrushAlias="js";
		result.ShowAs="JS";
		break;

	case "XPages":
		result.BrushFile="shBrushXsp.js";
		result.BrushAlias="xsp";
		result.ShowAs="XSP";
		break;
	
	case "Java":
		result.BrushFile="shBrushJava_custom.js";
		result.BrushAlias="java";
		result.ShowAs="JAVA";
		break;

	case "XML":
		result.BrushFile="shBrushXml.js";
		result.BrushAlias="xml";
		result.ShowAs="XML";
		break;

	case "LotusScript":
		result.BrushFile="shBrushLscript.js";
		result.BrushAlias="lscript";
		result.ShowAs="LSS";
		break;
		
	  case "Themes": 
        result.BrushFile="shBrushXml.js"; 
        result.BrushAlias="xml"; 
        result.ShowAs="Themes"; 
        break; 
	}
	
	return result;
}

function getBrushFile(CodeLanguage) {
	result=getBrushInfo(CodeLanguage);
	return result.BrushFile;
}

function getBrushAlias(CodeLanguage) {
	result=getBrushInfo(CodeLanguage);
	return result.BrushAlias;
}

function getShowAs(CodeLanguage) {
	result=getBrushInfo(CodeLanguage);
	return result.ShowAs;
}

// TODO: Redirection to invalid id should be implemented!
function getSnippetUNID(id) {
	if(id!=null && id!=""){
		var view1 = database.getView("SnippetsAll");
		var entry = view1.getEntryByKey(id, true);
		return (entry != null)?entry.getUniversalID():"";
	} else {
		return "";
	}
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
	var profileUrl = getConfig(config, "profileUrl");
	if (profileUrl) {
		output = '<a href="' + profileUrl + name + '" class="profileLink" target="_blank">' + name + '</a>';	
	} else {
		output = name;
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

	//return true; // For testing purposes...
	
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
	var sendTo=@Explode(getConfig(config, "reportRecipients"),";");
	var bodyStr="";
	var userName=session.createName(session.getEffectiveUserName()).getCommon();	
	
	var baseUrl = context.getUrl().toString();
	baseUrl = baseUrl.substring(0, baseUrl.indexOf(".nsf") + 4) + "/snippet.xsp?id=";

	var shortUrl = getConfig(config, "shortWidgetUrl");
	if (shortUrl) {
		baseUrl = shortUrl;
	} 
	
	var db:NotesDatabase=sessionAsSigner.getCurrentDatabase();
	var mailDoc:NotesDocument=db.createDocument();
	var snippetDoc:NotesDocument=db.getDocumentByID(snippetNoteId);
	
	mailDoc.replaceItemValue("Form", "Memo");
	mailDoc.replaceItemValue("SendTo", sendTo);
	mailDoc.replaceItemValue("Subject", "Notification about XSnippet ("+snippetDoc.getItemValueString("Name")+")");
	
	bodyStr="The snippet ("+snippetDoc.getItemValueString("Name")+") has been reported by "+userName+"..."
	bodyStr+="\n\nThe Reason:\n\n"+reportReason
	bodyStr+="\n\n\nDetails:\n\n"+reportDetail
	bodyStr+="\n\n\nThis notification has been sent on "+@Now()+" by "+facesContext.getExternalContext().getRequest().getRemoteAddr();
	bodyStr+="\n\nURL for the XSnippet:\n\n"+baseUrl+snippetDoc.getItemValueString("ID");

	mailDoc.replaceItemValue("Body", bodyStr);
	mailDoc.send(false);
}
