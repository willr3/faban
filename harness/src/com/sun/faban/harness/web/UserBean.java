package com.sun.faban.harness.web;

import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * 
 * @author wreicher
 * @see com.sun.faban.harness.webclient.UserEnv
 */
@ManagedBean(name="userbean")
@SessionScoped
public class UserBean {
	private static Logger logger = Logger.getLogger(UserBean.class.getName());
	
	
	
	public UserBean(){}
	
}
