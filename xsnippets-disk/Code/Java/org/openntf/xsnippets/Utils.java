package org.openntf.xsnippets;

// @author sbasegmez

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.faces.context.FacesContext;

import lotus.domino.ACL;
import lotus.domino.Database;
import lotus.domino.NotesException;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.designer.context.XSPUrl;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public final class Utils {

	/**
	 * 	 recycles multiple domino objects (thx Nathan T. Freeman)
	 *		
	 * @param objs
	 * 
	 */
	public static void recycleObjects(lotus.domino.Base... objs) {
		for ( lotus.domino.Base obj : objs ) {
			if (obj != null) {
				try {
					obj.recycle();
				} catch (Exception e) {}
			}
		}
	}

	public static <T> List<T> uniqueList(List<T> list) {
		Vector<T> v=new Vector<T>();
		for(T obj: list ) {
			if(! v.contains(obj)) {
				v.add((T) obj);
			}
		}
		return v;
	}

	public static String getXspProperty(String propertyName, String defaultValue) {
		String retVal = ApplicationEx.getInstance().getApplicationProperty(propertyName, defaultValue);
		return retVal;
	}

	public static int getXspProperty(String propertyName, int defaultValue) {
		String xspValue=Utils.getXspProperty(propertyName, "");
		int value=0;
		try {
			value=Integer.parseInt(xspValue);
		} catch(NumberFormatException e) {
			value=defaultValue;
		}
		return value;
	}

	public static String getEffectiveUserName() {
		String userName="";
		
		try {
			userName=ExtLibUtil.getCurrentSession().getEffectiveUserName();
		} catch (NotesException e) {
			// Not supposed to be here!
			e.printStackTrace();
		}

		return userName;
	}

	@SuppressWarnings("unchecked")
	public static List<String> getRoles() {
		ArrayList<String> roles=new ArrayList<String>();
		
		Database dbCurrent=ExtLibUtil.getCurrentDatabase();
		
		try {
			ACL acl=dbCurrent.getACL();
			roles.addAll(dbCurrent.queryAccessRoles(getEffectiveUserName()));
			recycleObjects(acl);
		} catch (NotesException e) {
			// Nothing to see here!
		}
		
		return roles;
	}

	/**
	 * Must be called within the right context...
	 * 
	 * @return the base url for the database.
	 */
	public static String getBaseURL() {
		FacesContext context = FacesContext.getCurrentInstance();
		XSPContext xspContext=XSPContext.getXSPContext(context);
		XSPUrl url=xspContext.getUrl();
		url.removeAllParameters();
		return url.toString().replace(url.getSiteRelativeAddress(xspContext), "/");		
	}
	
}
