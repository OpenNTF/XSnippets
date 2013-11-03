package org.openntf.xsnippets;


import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.DataObject;

/**
 * @author sbasegmez
 *
 */
public class ConfigBean implements Serializable, DataObject {
	
	private static final long serialVersionUID = -3732463043439724416L;

	private static final String LOGDB_FILENAME=""; // Current database if empty 
	private static final String VS_VAR_TO_REMEMBER="DownloadLogged"; // To remember download logging, we use a scoped variable.
	
	private boolean loaded=false;
	private Calendar dateLoaded;

	private HashMap<String, String> fields;
	
	public ConfigBean() {
		if(!loadConfig()) {
			System.out.println("Error loading XSnippets Configuration!");
		}
	}

	private Session getSession() {
		return ExtLibUtil.getCurrentSession();
	}
	
	private Database getCurrentDatabase() {
		return ExtLibUtil.getCurrentDatabase();
	}

	private boolean loadConfig() {
		
		fields=new HashMap<String, String>();

		Database db=getCurrentDatabase();
		
		View configView=null;
		Document configDoc=null;
		
		try {
			configView = db.getView("(Configuration)");
			configDoc = configView.getDocumentByKey("Configuration", true);
			
			if(configDoc==null) {
				configDoc=db.createDocument();
				configDoc.replaceItemValue("Form", "Configuration");
				configDoc.computeWithForm(false, false);
				configDoc.save();
			}
			
			for(Object itemObj: configDoc.getItems()) {
				if(itemObj instanceof Item) {
					Item item=(Item) itemObj;
					if(!item.getName().startsWith("$")) {
						fields.put(item.getName(), item.getText());
					}
					Utils.recycleObjects(item);
				}
			}
			
			setLoaded(true);

			System.out.println("XSnippets Configuration loaded successfully...");
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			Utils.recycleObjects(configView, configDoc);
		}
	}
		
	public boolean isLoaded() {
		return loaded;
	}

	private void setLoaded(boolean loaded) {
		this.loaded = loaded;
		this.dateLoaded=(loaded)?Calendar.getInstance():null;
	}

	public Calendar getDateLoaded() {
		return dateLoaded;
	}

	public void reload() {
		if(!loadConfig()) {
			System.out.println("Error reloading XSnippets Configuration!");
		}
	}
	
	public Class<?> getType(Object arg0) {
		return ConfigBean.class;
	}

	public Object getValue(Object arg0) {
		String result=fields.get(arg0);
//		System.out.println(arg0.toString()+" >> "+result);
		return (null==result)?"":result;
	}

	public boolean isReadOnly(Object arg0) {
		return true;
	}

	public void setValue(Object arg0, Object arg1) {
		// do nothing
	}

	public List<String> getCodeLanguages() {
		return SnippetLanguage.getLanguages();
	}

	public SnippetLanguage getSnippetLanguage(String codeLanguage) {
		return SnippetLanguage.byCodeLanguage(codeLanguage);
	}

	public String getSnippetUrl(String snippetId) {
		String url=fields.get("shortWidgetUrl");
		if(StringUtil.isEmpty(url)) {
			url=Utils.getBaseURL()+"snippet.xsp?id=";
		}

		return url+snippetId;
	}

	public boolean snippetCopied(String projectName) {

		Map<String, Object> viewScope=ExtLibUtil.getViewScope();

		if(viewScope.containsKey(VS_VAR_TO_REMEMBER)) { // if download logged at the same view life-cycle,
			// We don't need to do it again!
			return false;
		}
		
		ExternalContext ec=FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest request=(HttpServletRequest) ec.getRequest();

		String rUser=request.getRemoteUser();
		if("127.0.0.1".equals(rUser)) rUser=request.getHeader("HTTP_X_FORWARDED_FOR");
				
		Session session=getSession();
		Database currentDb=getCurrentDatabase();
		
		Database logDb=null;
		DateTime now=null;
		Document logDoc=null;
		Name userName=null;
		
		try {
			if("".equals(LOGDB_FILENAME)) {
				logDb=currentDb;
			} else {
				logDb=session.getDatabase("", LOGDB_FILENAME, false);
			}
			
			now=session.createDateTime(new Date());
			userName=session.createName(rUser);
			
			if("Anonymous".equals("Anonymous")) {
				rUser="";
			} else {
				rUser=userName.getCommon();
			}
			
			logDoc=logDb.createDocument();

			logDoc.replaceItemValue("Form", "Log");
			logDoc.replaceItemValue("Type", "fileDownload");
			logDoc.replaceItemValue("Date", now);
			logDoc.replaceItemValue("FileName", ""); // no attachment
			logDoc.replaceItemValue("UserAddress", request.getRemoteAddr());
			logDoc.replaceItemValue("User", rUser);
			logDoc.replaceItemValue("DBasePath", currentDb.getFileName());
			logDoc.replaceItemValue("Project", projectName);
			logDoc.replaceItemValue("Release", (new Date()).toString());
			logDoc.replaceItemValue("DownloadType", "snippet");
			logDoc.replaceItemValue("ServerAddress", request.getServerName());
			
			if(logDoc.computeWithForm(false, false)) {
				if(! logDoc.save()) {
					System.out.println("Error saving Download Log record for XSnippets");
					return false;
				}
			} else {
				System.out.println("Error computing Download Log record for XSnippets");
				return false;
			}

			viewScope.put(VS_VAR_TO_REMEMBER, "1");
			
			return true;
			
		} catch (NotesException ne) {
			ne.printStackTrace();
		} finally {
			Utils.recycleObjects(logDoc, now);
			if("".equals(LOGDB_FILENAME)) {
				Utils.recycleObjects(logDb);
			}
		}
		
		return false;				
	}

	
}
