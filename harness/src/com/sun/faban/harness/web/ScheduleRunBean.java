package com.sun.faban.harness.web;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.faban.harness.common.BenchmarkDescription;
import com.sun.faban.harness.engine.RunQ;
import com.sun.faban.harness.web.loader.BenchmarkLoader;
import com.sun.faban.harness.web.pojo.Benchmark;
import com.sun.faban.harness.webclient.UserEnv;

/* View scoped so runId will be null if scheduleRun was unsuccessful
 * TODO have runQ return a run object that the bean holds rather than a string
 * 
 */
@ManagedBean(name="schedule")
@ViewScoped
public class ScheduleRunBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1710324330593617702L;

	static Logger logger = Logger.getLogger(ScheduleRunBean.class.getName());

	@ManagedProperty(value="#{faban}")
	private FabanSessionBean faban;

	private String runId;
	
	public void setRunId(String id){
		this.runId=id;
	}
	public String getRunId(){return runId;}
	
	public ScheduleRunBean(){
		this.runId=null;
	}
	
	public FabanSessionBean getFaban(){return faban;}
	public void setFaban(FabanSessionBean faban){
		this.faban = faban;
	}
	
	
	
	//maps to schedule-run.jsp
	public void scheduleRun(){
		this.runId=null;
		
		
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();

		if(faban == null || faban.getBenchmarkName()==null || faban.getBenchmarkName().isEmpty() || faban.getProfileName()==null || faban.getProfileName().isEmpty()){
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Could not schedule run","Benchmark and profile must be selected to schedule a run");
        	FacesContext.getCurrentInstance().addMessage(null, msg);
        	runId = "error";
        	
		}else{
		
			try {
				Reader reader = request.getReader();
				int size = request.getContentLength();
				
				if(size<0){
					size=0;
				}
				char[] buf = new char[size];
				logger.finer("Lengh of buffer created is "+size);
				
				
				
				int len=0;
				while(len < size){
			        len += reader.read(buf, len, size - len);
			        
			        
				}
				
				
				String profileName = faban.getProfileName();
				
				BenchmarkLoader benchLoader = new BenchmarkLoader();
				
				Benchmark bench = benchLoader.getBenchmark(faban.getBenchmarkName());
				BenchmarkDescription desc = BenchmarkDescription.toDescription(bench);
				
				UserEnv userEnv = new UserEnv();
				//TODO I hate that profiles are changed with each run :(
				userEnv.saveParamRepository(profileName, desc, buf);
	
				//TODO check user permission
				//TODO use a real userId
				this.runId = RunQ.getHandle().addRun("faban-bootstrap", profileName, desc);
			} catch (IOException e) {
	        	FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,e.getMessage(),Arrays.asList(e.getStackTrace()).toString());
	        	FacesContext.getCurrentInstance().addMessage(null, msg);
	        	this.runId = null;
			}
		}
		
		
	}
	 
}
