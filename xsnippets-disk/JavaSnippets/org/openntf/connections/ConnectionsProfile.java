package org.openntf.connections;
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
 * Author: Yun Zhi Lin
 */
public class ConnectionsProfile 
{
	private boolean loaded = false;
	
	/*
	 * Use /profiles/atom/profile.do?userid= to get profile
	 * This url works for both connections 2.5 & 3.0.1
	 * 
	 * 1. Get the content node: feed -> entry -> content
	 * 2. Get the vcard span: this is different between 2.5 & 3.0.1
	 * 		For 2.5, it's content -> div -> span (class = "vcard")
	 * 		For 3.0.1, it's content -> sp_0:div -> span (class = "vcard")
	 * 		The content structure inside vcard span is same
	 * 
	 */
	
	//not get email here, email is got from Domino info
//	private String email; // div with no class -> a with class email -> stringValue(".")
	private String bu; // div with class org -> span with class organization-unit  -> stringValue(".")
	private String role; //div with class role -> stringValue(".")
	private String title; //div with class title -> stringValue(".")
	
	private String address;
	
	private String building; // div with class x-office -> span with class x-building -> stringValue(".")
	private String floor; // div with class x-office -> span with class x-floor -> stringValue(".")
	private String office; // div with class x-office -> span with class x-office-number -> stringValue(".")
	
	private String tel; //div with class tel -> span with class value -> stringValue(".")
	
	
	
	public String getBu() {
		return bu;
	}

	public void setBu(String bu) {
		this.bu = bu;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getBuilding() {
		return building;
	}

	public void setBuilding(String building) {
		this.building = building;
	}

	public String getFloor() {
		return floor;
	}

	public void setFloor(String floor) {
		this.floor = floor;
	}

	public String getOffice() {
		return office;
	}

	public void setOffice(String office) {
		this.office = office;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
}
