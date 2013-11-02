package org.openntf.xsnippets;


import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
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

	private boolean loaded=false;
	private Calendar dateLoaded;

	private HashMap<String, String> fields;
	
	public ConfigBean() {
		if(!loadConfig()) {
			System.out.println("Error loading XSnippets Configuration!");
		}
	}

//	private Session getSession() {
//		return ExtLibUtil.getCurrentSession();
//	}
	
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
	
}
