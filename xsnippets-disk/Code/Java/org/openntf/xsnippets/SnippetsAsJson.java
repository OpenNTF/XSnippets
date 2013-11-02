package org.openntf.xsnippets;
/*
 * � Copyright IBM Corp. 2011
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * Author: Niklas Heidloff
 */
import java.util.Vector;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import lotus.domino.Database;
import lotus.domino.Session;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;

public class SnippetsAsJson {
	public SnippetsAsJson() {		
	}
	
	private String _count;
	private String _view;
	private String _author;
	private String _snippetsBaseUrl;
	
	private final String VIEW_RECENT = "recent";
	private final String VIEW_POPULAR = "popular";
	private final String VIEW_AUTHOR = "author";
	
	private final String PROPERTY_ID = "id";
	private final String PROPERTY_URL = "url";
	private final String PROPERTY_CREATION_DATE = "creationDate";
	private final String PROPERTY_NAME = "name";
	private final String PROPERTY_LANGUAGE = "language";
	private final String PROPERTY_DEVELOPER = "developer";	
	
	private final String DEFAULT_COUNT = "10";
	
	public void setCount(String count) {
		_count = count;
		if (_count == null) _count = DEFAULT_COUNT;
		if (count.equalsIgnoreCase("")) _count = DEFAULT_COUNT;
	}
	
	public String getCount() {
		return _count;
	}
	
	public int getCountAsInt() {
		try {
			Integer i = new Integer(_count);
			return i.intValue();
		}
		catch (Exception e) {
			Integer i = new Integer(DEFAULT_COUNT);
			return i.intValue();
		}
	}
	
	public String getView() {
		return _view;
	}
	
	public void setView(String view) {
		_view = view;
		if (_view == null) _view = VIEW_RECENT;
	}
	
	public void setAuthor(String author) {
		_author = author;
	}
	
	public String getAuthor() {
		return _author;
	}
	
	public void setSnippetsBaseUrl(String snippetsBaseUrl) {
		_snippetsBaseUrl = snippetsBaseUrl;
	}
	
	public String getSnippetsBaseUrl() {
		return _snippetsBaseUrl;
	}
	
	public String getJson() {		
		String output = "dojo.io.script.jsonp_dojoIoScript1._jsonpCallback({'responseData': {'results': [";
		
		try {			
			String entriesAsJson = "";
			if (_view.equalsIgnoreCase(VIEW_POPULAR)) {
				entriesAsJson = getJsonPopularView();
			}			
			else if (_view.equalsIgnoreCase(VIEW_AUTHOR)) {
				entriesAsJson = getJsonAuthorView();
			} 
			else {
				entriesAsJson = getJsonRecentView();
			}			
			
			output = output + entriesAsJson + "], }, 'responseDetails': null,'responseStatus': 200})";
		}
		catch (NotesException ne) {
			ne.printStackTrace();
			return "dojo.io.script.jsonp_dojoIoScript1._jsonpCallback({'responseData': {'results': [], }, 'responseDetails': null,'responseStatus': 500})";
		}
		
		return output;
	}	
	
	@SuppressWarnings("unchecked")
	private String getJsonRecentView() throws NotesException {
		Session session = NotesContext.getCurrent().getCurrentSession(); 
		Database db = session.getCurrentDatabase();
		String output = "";
		View view = db.getView("SnippetsByDate");
		ViewNavigator nav = view.createViewNav();	
		int n = 0;
		ViewEntry tmpEntry;
		ViewEntry entry = nav.getFirst();
		while (entry != null) {
			Vector columnValues = entry.getColumnValues();
			output = output + "{" + 
				"'" + PROPERTY_ID + "': '" + columnValues.get(5) + "', " +
				"'" + PROPERTY_URL + "': '" + getSnippetsBaseUrl() + columnValues.get(5) + "', " + 
				"'" + PROPERTY_CREATION_DATE + "': '" + columnValues.get(0) + "', " + 
				"'" + PROPERTY_NAME + "': '" + columnValues.get(1) + "', " +
				"'" + PROPERTY_LANGUAGE + "': '" + columnValues.get(2) + "', " +
				"'" + PROPERTY_DEVELOPER + "': '" + columnValues.get(4) + "'}";
			n++;
	        tmpEntry = nav.getNext();
	        entry.recycle();
	        entry = tmpEntry;
	        if (n == getCountAsInt()) {
	        	entry.recycle();
	        	entry = null;
	        }
	        if (entry != null) output = output + ", ";
		}
		return output;
	}
	
	@SuppressWarnings("unchecked")
	private String getJsonPopularView() throws NotesException {
		Session session = NotesContext.getCurrent().getCurrentSession(); 
		Database db = session.getCurrentDatabase();
		String output = "";
		View view = db.getView("DownloadsLastThreeMonthsSnapshot");
		ViewNavigator nav = view.createViewNav();	
		int n = 0;
		ViewEntry tmpEntry;
		ViewEntry entry = nav.getFirst();
		while (entry != null) {
			Vector columnValues = entry.getColumnValues();
			output = output + "{" + 
				"'" + PROPERTY_ID + "': '" + columnValues.get(1) + "', " +
				"'" + PROPERTY_URL + "': '" + getSnippetsBaseUrl() + columnValues.get(1) + "', " + 
				"'" + PROPERTY_NAME + "': '" + columnValues.get(2) + "', " +
				"'" + PROPERTY_DEVELOPER + "': '" + columnValues.get(3) + "'}";
			n++;
	        tmpEntry = nav.getNext();
	        entry.recycle();
	        entry = tmpEntry;
	        if (n == getCountAsInt()) {
	        	entry.recycle();
	        	entry = null;
	        }
	        if (entry != null) output = output + ", ";
		}
		return output;
	}
	
	
	@SuppressWarnings("unchecked")
	private String getJsonAuthorView() throws NotesException {
		Session session = NotesContext.getCurrent().getCurrentSession(); 
		Database db = session.getCurrentDatabase();
		String output = "";
		View view = db.getView("SnippetsByAuthor");
		ViewNavigator nav = view.createViewNavFromCategory(getAuthor());	
		int n = 0;
		ViewEntry tmpEntry;
		ViewEntry entry = nav.getFirst();
		while (entry != null) {
			Vector columnValues = entry.getColumnValues();
			output = output + "{" + 
				"'" + PROPERTY_ID + "': '" + columnValues.get(6) + "', " +
				"'" + PROPERTY_URL + "': '" + getSnippetsBaseUrl() + columnValues.get(6) + "', " + 
				"'" + PROPERTY_CREATION_DATE + "': '" + columnValues.get(1) + "', " + 
				"'" + PROPERTY_NAME + "': '" + columnValues.get(2) + "', " +
				"'" + PROPERTY_LANGUAGE + "': '" + columnValues.get(3) + "', " +
				"'" + PROPERTY_DEVELOPER + "': '" + columnValues.get(0) + "'}";
			n++;
	        tmpEntry = nav.getNext();
	        entry.recycle();
	        entry = tmpEntry;
	        if (n == getCountAsInt()) {
	        	entry.recycle();
	        	entry = null;
	        }
	        if (entry != null) output = output + ", ";
		}
		return output;
	}
}
