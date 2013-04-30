package org.openntf.profiles;
/*
 * © Copyright IBM Corp. 2011
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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Session;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

public class Profiles {
	
	final static String VIEW = "Profiles";
	final static String DELIMITER = ",";
	
	private List<UserProfile> _profiles;
	
	public Profiles() {		
	}
	
	public List<UserProfile> getProfiles(String personName) {
		List<UserProfile> output = null;
		UserProfile userProfile = null;
		Document doc = null;
		View view = null;
		try {
			Session session = NotesContext.getCurrent().getCurrentSession(); 
			Database db = session.getCurrentDatabase();			
			view = db.getView(VIEW);
			DocumentCollection dc = view.getAllDocumentsByKey(personName, true);		
			if (dc.getCount() < 1) return null;
			
			doc = dc.getFirstDocument();		
			userProfile = new UserProfile(personName, doc.getItemValueString("DefaultProfileLabel"), doc.getItemValueString("DefaultProfileUrl"));
			userProfile.setIsDefault();
			output = new ArrayList<UserProfile>();
			output.add(userProfile);

			String label;
			label = doc.getItemValueString("ProfileLabel2");
			if (label != null) {
				if (label.length() > 0) {
					userProfile = new UserProfile(personName, label, doc.getItemValueString("ProfileUrl2"));
					output.add(userProfile);
				}
			}
			label = doc.getItemValueString("ProfileLabel3");
			if (label != null) {
				if (label.length() > 0) {
					userProfile = new UserProfile(personName, label, doc.getItemValueString("ProfileUrl3"));
					output.add(userProfile);
				}
			}
			label = doc.getItemValueString("ProfileLabel4");
			if (label != null) {
				if (label.length() > 0) {
					userProfile = new UserProfile(personName, label, doc.getItemValueString("ProfileUrl4"));
					output.add(userProfile);
				}
			}
			label = doc.getItemValueString("ProfileLabel5");
			if (label != null) {
				if (label.length() > 0) {
					userProfile = new UserProfile(personName, label, doc.getItemValueString("ProfileUrl5"));
					output.add(userProfile);
				}
			}
			label = doc.getItemValueString("ProfileLabel6");
			if (label != null) {
				if (label.length() > 0) {
					userProfile = new UserProfile(personName, label, doc.getItemValueString("ProfileUrl6"));
					output.add(userProfile);
				}
			}
			label = doc.getItemValueString("ProfileLabel7");
			if (label != null) {
				if (label.length() > 0) {
					userProfile = new UserProfile(personName, label, doc.getItemValueString("ProfileUrl7"));
					output.add(userProfile);
				}
			}
			label = doc.getItemValueString("ProfileLabel8");
			if (label != null) {
				if (label.length() > 0) {
					userProfile = new UserProfile(personName, label, doc.getItemValueString("ProfileUrl8"));
					output.add(userProfile);
				}
			}
			label = doc.getItemValueString("ProfileLabel9");
			if (label != null) {
				if (label.length() > 0) {
					userProfile = new UserProfile(personName, label, doc.getItemValueString("ProfileUrl9"));
					output.add(userProfile);
				}
			}
			label = doc.getItemValueString("ProfileLabel10");
			if (label != null) {
				if (label.length() > 0) {
					userProfile = new UserProfile(personName, label, doc.getItemValueString("ProfileUrl10"));
					output.add(userProfile);
				}
			}
			
			if (doc != null) doc.recycle();
			if (dc != null) dc.recycle();
		    if (view != null) view.recycle();
		}
		catch (NotesException ne) {
			ne.printStackTrace();
		}
		
		_profiles = output;
		return output;
	}
	
	public String getLabels() {
		if (_profiles == null) return "";
		String output = "";
		for (int i = 0; i < _profiles.size(); i++) {
			output = output + _profiles.get(i).getLabel();
			if (i < _profiles.size() - 1) output = output + DELIMITER;
		}
		return output;
	}
	
	public String getUrls() {
		if (_profiles == null) return "";
		String output = "";
		for (int i = 0; i < _profiles.size(); i++) {
			output = output + _profiles.get(i).getUrl();
			if (i < _profiles.size() - 1) output = output + DELIMITER;
		}
		return output;		
	}
	
	public String getUrl(int i) {
		if (_profiles == null) return "";
		if (_profiles.size() > i) {
			return _profiles.get(i).getUrl();
		}
		return "";
	}
}
