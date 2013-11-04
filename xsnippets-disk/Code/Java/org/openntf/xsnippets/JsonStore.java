package org.openntf.xsnippets;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class JsonStore implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String DATEFORMAT="yyyy-MM-dd'T'HH:mm:ss";
	
	private static SimpleDateFormat ISO8601 = new SimpleDateFormat(DATEFORMAT);
	
	public JsonStore() {

	}
	
	public String getFavoriteSnippetsByUserName(String userName) throws NoSuchAlgorithmException {
		MessageDigest md=MessageDigest.getInstance("MD5");
		
		byte[] userKeyBytes=md.digest(userName.getBytes(Charset.forName("UTF8")));
		StringBuffer sb=new StringBuffer();
		
		for(int i=0; i<userKeyBytes.length; i++) {
			String hex=Integer.toHexString(0xFF & userKeyBytes[i]);
		    sb.append((hex.length()==1?"0":"")+hex.toUpperCase());
		}
		
		return getFavoriteSnippetsByUserKey(sb.toString());
	}
	
	public String getFavoriteSnippetsByUserKey(String userKey) {
		
		Database database=ExtLibUtil.getCurrentDatabase();

		JsonJavaObject returnJSON = new JsonJavaObject();

		View favView=null;
		ViewEntryCollection entries=null;
		
		List<Map<String,String>> sList=new ArrayList<Map<String,String>>();
		
		try {
			favView=database.getView("xiFavoritesByUserKey");
			
			entries=favView.getAllEntriesByKey(userKey, true);
			
			ViewEntry entry=entries.getFirstEntry();
			
			while(entry!=null) {

				String id=entry.getColumnValues().get(1).toString();

				Map<String, String> snippetMap=getSnippetMap(id);

				if(snippetMap!=null) sList.add(snippetMap);
				
				ViewEntry tmpEntry=entry;
				entry=entries.getNextEntry(tmpEntry);
				Utils.recycleObjects(tmpEntry);
			}
		} catch (NotesException ne) {
			ne.printStackTrace();
		} finally {
			Utils.recycleObjects(entries, favView);
		}
		
		returnJSON.put("success", true);
		returnJSON.put("total", sList.size());
		returnJSON.put("data", sList);

		try {
			return JsonGenerator.toJson(JsonJavaFactory.instanceEx, returnJSON);
		} catch (JsonException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "{\"success\":false}";
	}


	@SuppressWarnings("unchecked")
	private Map<String, String> getSnippetMap(String id) {
		Map<String, String> snippetMap=new HashMap<String, String>();
			
		Database database=ExtLibUtil.getCurrentDatabase();
	
		View sView=null;
		Document sDoc=null;
		DateTime dt=null;
		
		try {
			sView=database.getView("snippetsAll");
			sDoc=sView.getDocumentByKey(id, true);
			
			if(sDoc!=null) {
				snippetMap.put("id", id);
				
				Vector<Object> v=sDoc.getItemValueDateTimeArray("DateCreated");
				
				if((! v.isEmpty()) && v.get(0).getClass().getName().endsWith("DateTime")) {
					dt=(DateTime)v.get(0);
				} else {
					dt=sDoc.getCreated();
				}
								
				snippetMap.put("name", sDoc.getItemValueString("Name"));
				snippetMap.put("date", ISO8601.format(dt.toJavaDate()));
				snippetMap.put("developer", sDoc.getItemValueString("Author"));
				snippetMap.put("body", sDoc.getItemValueString("Body"));
				snippetMap.put("language", sDoc.getItemValueString("Language"));
				snippetMap.put("notes", sDoc.getItemValueString("Notes"));
				snippetMap.put("flags", sDoc.getItemValueString("SpecialFlags"));
				
				return snippetMap;
				
			}
			
			
		} catch(NotesException ne) {
			ne.printStackTrace();
		} finally {
			Utils.recycleObjects(sView, sDoc, dt);
		}

		return null;
	}
	
	
	
}
