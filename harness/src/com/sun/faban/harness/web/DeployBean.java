package com.sun.faban.harness.web;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import com.sun.faban.harness.common.Config;
import com.sun.faban.harness.util.DeployUtil;
import com.sun.faban.harness.util.FileHelper;



@ManagedBean(name="deploy")
@ViewScoped
public class DeployBean {
	static Logger logger = Logger.getLogger(DeployBean.class.getName());

    private Part uploadedFile;
    private String user;
    private String password;
    private boolean clearConfig;
    
    
    public boolean getClearConfig(){return this.clearConfig;}
    public void setClearConfig(boolean clearConfig){
    	this.clearConfig=clearConfig;
    }
    
    public String getUser(){return this.user;}
    public void setUser(String user){
    	this.user = user;
    }
    
    public String getPassword(){return this.password;}
    public void setPassword(String password){
    	this.password = password;
    }
    
    public Part getUploadedFile() { return uploadedFile; }
    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }
    
    public DeployBean(){}
	
    public void uploadFile(){
    	try{
    		String fileName = getFilename(uploadedFile);
    		
    		if(Config.SECURITY_ENABLED){
    			if(Config.DEPLOY_USER == null || Config.DEPLOY_USER.length()==0 || !Config.DEPLOY_USER.equals(user)){
    				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Authorization failed!",user+" is not authorized to deploy");
    				FacesContext.getCurrentInstance().addMessage(null, msg);
    				return;
    			}else if (Config.DEPLOY_PASSWORD == null || Config.DEPLOY_PASSWORD.length()==0 || !Config.DEPLOY_PASSWORD.equals(password)){
    				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Authentication failed!","could not authenticate "+user+" please try again");
    				FacesContext.getCurrentInstance().addMessage(null, msg);
    				return;
    			}
    		}
    		
    		//remove file separators from fileName
    		char[] pathSeparators = {'/','\\'};
            for (int j = 0; j < pathSeparators.length; j++) {
                int idx = fileName.lastIndexOf(pathSeparators[j]);
                if (idx != -1) {
                    fileName = fileName.substring(idx + 1);
                }
            }
            //TODO move to a validation method
            
            //Ignore non-jarfiles
            if (!fileName.toLowerCase().endsWith(".jar")) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"File not supported!",fileName+" does not appear to be a valid jar");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
            }
            //trap files with . in name
            String deployName = fileName.substring(0, fileName.length() - 4);
            if (deployName.indexOf('.') > -1) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Bad file name",fileName+" contains a . before .jar");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;            	
            }
            
            if( !DeployUtil.canDeployBenchmark(deployName)){
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Cannot deploy",fileName+" is in use and cannot be deployed");
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return;
            }
            FacesMessage msg = null;
            //write the benchmark
            //
            File uploadFile = new File(Config.BENCHMARK_DIR, fileName);
            if(uploadFile.exists()){
            	//replacing an existing
            	msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Success!","re-deployed "+fileName);
            	FileHelper.recursiveDelete(uploadFile);
            }else{
            	msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Success!","deployed "+fileName);
            }

            String destination = uploadFile.getAbsolutePath();
            try{
            	uploadedFile.write(destination);
            }catch(Exception e){
            	msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Deploy Failed.","could not write "+fileName+" to "+destination);
            	FacesContext.getCurrentInstance().addMessage(null, msg);
            	logger.log(Level.WARNING,e.getMessage(),e);
            	return;
            }
            
    		try{
    			DeployUtil.processUploadedJar(uploadFile, deployName);
    		}catch(Exception e){
            	msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Deploy Failed.","could not extract "+fileName);
            	FacesContext.getCurrentInstance().addMessage(null, msg);
            	logger.log(Level.WARNING,e.getMessage(),e);
            	return;
    		}
    		if(clearConfig){
    			DeployUtil.clearConfig(deployName);
    		}
    		//If reach this point then write the success message
            FacesContext.getCurrentInstance().addMessage(null, msg);
    		
    	} catch (Exception e){
    		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Failed!","could not upload file");
            FacesContext.getCurrentInstance().addMessage(null, msg);
    	}
    }
    
	private static String getFilename(Part part) {  
        for (String cd : part.getHeader("content-disposition").split(";")) {  
            if (cd.trim().startsWith("filename")) {  
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");  
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.  
            }  
        }  
        return null;  
    }  	
}
