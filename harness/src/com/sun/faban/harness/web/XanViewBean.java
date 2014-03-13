package com.sun.faban.harness.web;

import java.io.File;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.faban.harness.common.Config;
import com.sun.faban.harness.web.loader.XanLoader;
import com.sun.faban.harness.web.pojo.Xan;

@ManagedBean(name="xanbean")
@ViewScoped
public class XanViewBean {
	private static Logger logger = Logger.getLogger(XanViewBean.class.getName());
	
	
	public XanViewBean(){
		System.out.println("XanViewBean()");
	}
	public Xan getXan(){
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();

		@SuppressWarnings("unchecked")
		Map<String,String> tokens = (Map<String,String>)request.getAttribute("fabanUrlTokens");
		if(tokens==null){
			logger.warning("Failed to identify log path from url");
			return null;
		}
		String runId = tokens.get("runId");
		
		return getXan(runId);
		
		
	}
	public Xan getXan(String runId,String xanName){
		File xanFile = new File(Config.OUT_DIR+File.separator+runId+File.separator+xanName+".xan");
		Xan rtrn = null;
		if(xanFile.exists()){
			XanLoader loader = new XanLoader();
			
			rtrn = loader.getXan(xanFile.getAbsolutePath());
		}
		if(rtrn!=null)
			rtrn.createJson();
		return rtrn;
	}
	public Xan getXan(String runId){
		File xanFile = new File(Config.OUT_DIR+File.separator+runId+File.separator+"detail.xan");
		Xan rtrn = null;
		
		
		
		if(xanFile.exists()){
			XanLoader loader = new XanLoader();
			
			rtrn = loader.getXan(xanFile.getAbsolutePath());
		}
		if(rtrn!=null)
			rtrn.createJson();
		return rtrn;
	}
}
