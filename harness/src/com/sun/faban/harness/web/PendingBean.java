package com.sun.faban.harness.web;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.faban.harness.engine.RunQ;

@ManagedBean(name="pending")
@ViewScoped
public class PendingBean {
	static Logger logger = Logger.getLogger(PendingBean.class.getName());
	
	//maps to suspend-runs.jsp
	public String suspend(){
		//TODO check if suspend is authorized
		if(isNewRequest()){
			RunQ.getHandle().stopRunDaemon();
		}
		return "ok";
	}
	public String resume(){
		//TODO check if resume is authorized
		if(isNewRequest()){
			RunQ.getHandle().startRunDaemon();
		}
		return "ok";
	}
	//maps to delete-runs.jsp
	public String remove(){
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();

		String[] runs = request.getParameterValues("selected-runs");
		List<String> removed = new ArrayList<String>();
		List<String> failed =  new ArrayList<String>();
		RunQ runQ = RunQ.getHandle();
		
		if(runs!=null && runs.length>0){
			for(String runId: runs){
				//TODO check if Kill is authorized
				if(runQ.deleteRun(runId)){
					removed.add(runId);
				}else{
					failed.add(runId);
				}
			}
		}
		
		if(removed.size()>0){
        	FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Removed "+removed.size(),"successfully removed "+removed.toString());
        	FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		if(failed.size()>0){
        	FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Failed on "+failed.size(),"Failed to removed "+failed.toString());
        	FacesContext.getCurrentInstance().addMessage(null, msg);			
		}
		
		return "ok";
	}
	
	
	public boolean isNewRequest() {
        final FacesContext fc = FacesContext.getCurrentInstance();
        final boolean getMethod = ((HttpServletRequest) fc.getExternalContext().getRequest()).getMethod().equals("GET");
        final boolean ajaxRequest = fc.getPartialViewContext().isAjaxRequest();
        final boolean validationFailed = fc.isValidationFailed();
        return getMethod && !ajaxRequest && !validationFailed;
    }
}
