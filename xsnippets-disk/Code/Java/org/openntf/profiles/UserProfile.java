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
public class UserProfile {

	private String _label;
	private String _url;
	private String _personName;
	private boolean _isDefault;
	
	public UserProfile(String personName, String label, String url) {
		_label = label;
		_personName = personName;
		_url = url;
	}
	
	public String getPersonName() {
		return _personName;
	}
	
	public String getLabel() {
		return _label;
	}
	
	public String getUrl() {
		return _url;
	}
	
	public boolean isDefault() {
		return _isDefault;
	}
	
	public void setIsDefault() {
		_isDefault = true;
	}
}
