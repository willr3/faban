package com.sun.faban.harness.web;

import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


/**
 * This bean holds the values that Faban previously put into the session object
 * @author wreicher
 */
@ManagedBean(name="faban")
@SessionScoped
public class FabanSessionBean {
	private static Logger logger = Logger.getLogger(FabanSessionBean.class.getName());

	private String profileName;
	private String benchmarkName;
	
	public FabanSessionBean(){}
	
	public String getProfileName(){return profileName;}
	public void setProfileName(String profileName){
		this.profileName=profileName;
	}
	public String getBenchmarkName(){return benchmarkName;}
	public void setBenchmarkName(String benchmarkName){
		this.benchmarkName=benchmarkName;
	}
	
	
}
