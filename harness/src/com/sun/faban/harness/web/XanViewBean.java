package com.sun.faban.harness.web;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.sun.faban.harness.common.Config;
import com.sun.faban.harness.webclient.View;
import com.sun.faban.harness.webclient.View.Xan;

@ManagedBean(name="xanbean")
@RequestScoped
public class XanViewBean {
	private static Logger logger = Logger.getLogger(XanViewBean.class.getName());
	
	public XanViewBean(){
		System.out.println("XanViewBean()");
	}
	
	public Xan getXan(String runId){
		File xanFile = new File(Config.OUT_DIR+File.separator+runId+File.separator+"detail.xan");

		System.out.println("getXan("+runId+") looking for "+xanFile.getPath());
		
		if(xanFile.exists()){
			try {
				return View.parseXan(xanFile);
			} catch (IOException e) {
				System.out.println("Failed to read xan from "+xanFile.getPath());
				logger.log(Level.WARNING,"Failed to read xan from "+xanFile.getPath(),e);
			} catch (ParseException e) {
				System.out.println("Failed to parse xan from "+xanFile.getPath());
				logger.log(Level.WARNING,"Failed to parse xan data from "+xanFile.getPath(),e);
			}
		}
		return null;
	}
}
