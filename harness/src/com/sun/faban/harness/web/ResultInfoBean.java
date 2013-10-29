package com.sun.faban.harness.web;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.sun.faban.harness.web.loader.ResultLoader;
import com.sun.faban.harness.web.pojo.Result;

@ManagedBean(name="resultinfo")
@ViewScoped
public class ResultInfoBean implements Serializable{

	private static final long serialVersionUID = -2938233560347235237L;

	private ResultLoader loader;
	
	public ResultInfoBean(){
		loader = new ResultLoader();
	}
	
	public Result getResult(String runId){
		return loader.getResult(runId);
	}
}
