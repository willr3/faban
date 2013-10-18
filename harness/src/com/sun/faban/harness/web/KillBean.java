package com.sun.faban.harness.web;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.sun.faban.harness.engine.RunQ;

@ManagedBean(name="kill")
@ViewScoped
public class KillBean {

	
	private long confirmTime=System.currentTimeMillis();
	private String killId;
	private String killedId="  ";
	
	public KillBean(){
		this.killId = getCurrentRunId();
	}
	
	public long getConfirmTime(){return confirmTime;}
	public void setConfirmTime(long confirmTime){
		this.confirmTime=confirmTime;
	}
	
	
	public String getKillId(){return killId;}
	public void setKillId(String killId){
		this.killId=killId;
	}
	
	public String getKilledId(){return killedId;}
	
	public String killRun(){
		long now = System.currentTimeMillis();
		long lapse = now - confirmTime;
		FacesMessage msg;
		if(lapse < 60000 && lapse > 0){
			//TODO check if kill is allowed
			//TODO use an actual user
        	msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Killed "+killId,"successfully killed "+killId);
        	
			
			String run = RunQ.getHandle().killCurrentRun(killId, "default-user");
			if(run == null){
				msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Not active",killId+" is no longer active");
			}
			killedId=killId;
			
		}else{
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Timeout","timed out waiting to kill "+killId);
		}
		
		FacesContext.getCurrentInstance().addMessage(null, msg);
		return "ok";
	}
	
	
	//properties that do not support set because they use the RunQ
	public String getCurrentRunId(){
		return RunQ.getHandle().getCurrentRunId();
	}
	public String getCurrentBenchmark(){
		return RunQ.getHandle().getCurrentBenchmark();
	}
}
