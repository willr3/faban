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

    List<String> deployNames = new ArrayList<String>();
    List<String> cantDeployNames = new ArrayList<String>();
    List<String> errDeployNames = new ArrayList<String>();
    List<String> invalidNames = new ArrayList<String>();
    List<String> errHeaders = new ArrayList<String>();
    List<String> errDetails = new ArrayList<String>();

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
    
	/*
	 * Copied from Deployer.doPost()
	 */
	@SuppressWarnings("rawtypes")
	public String fileUpload() throws ServletException{
		System.out.println("DeployBean.fileUpload()");
		
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();


        String user = null;
        String password = null;
        boolean clearConfig = false;
        boolean hasPermission = true;
		
        
        String acceptHeader = request.getHeader("Accept");
        boolean acceptHtml = acceptHeader!=null && acceptHeader.indexOf("text/html")>0;
        
        DiskFileUpload fu = new DiskFileUpload();
        fu.setSizeMax(-1);			//no size max
        fu.setSizeThreshold(4096);	//maximum in memory size
        fu.setRepositoryPath(Config.TMP_DIR);

        StringWriter messageBuffer = new StringWriter();
        PrintWriter messageWriter = new PrintWriter(messageBuffer);

        System.out.println("  try parseRequest");
        List fileItems = null;
        try{
        	fileItems = fu.parseRequest(request);
        }catch(FileUploadException e){
        	e.printStackTrace();
        	//TODO report error back to user
        	throw new ServletException(e);
        }
        System.out.println(" parsed Request fileItems.length="+fileItems.size());
        // assume we know there are two files. The first file is a small
        // text file, the second is unknown and is written to a file on
        // the server
        for(Iterator i = fileItems.iterator(); i.hasNext();){
        	FileItem item = (FileItem) i.next();
        	
        	String fieldName = item.getFieldName();
        	
        	System.out.println("DeployBean.FileItem = "+item.toString());
        	System.out.println("DeployBean.FileItem.fieldName = "+fieldName);
        	if (item.isFormField()){
        		if("user".equals(fieldName)){
        			user = item.getString();
        		}else if ("password".equals(fieldName)){
        			password = item.getString();
        		}else if ("clearconfig".equals(fieldName)){
        			String value = item.getString();
        			clearConfig = Boolean.parseBoolean(value);
        		}
        	}else if("jarfile".equals(fieldName)){
        		String fileName = item.getName();
        		System.out.println("DeployBean.jarfile.fileName="+fileName);
        		if(fileName == null){
        			System.out.println("No name for uploaded jarfile");
        			logger.warning("No name for uploaded upload");
        			continue;
        		}
        		if(Config.SECURITY_ENABLED){
        			if(Config.DEPLOY_USER == null || Config.DEPLOY_USER.length() == 0 || !Config.DEPLOY_USER.equals(user)){
        				hasPermission = false;
        				break;
        			}
        			if(Config.DEPLOY_PASSWORD == null || Config.DEPLOY_PASSWORD.length() == 0 || !Config.DEPLOY_PASSWORD.equals(password)){
        				hasPermission = false;
        				break;
        			}
        		}
                // Now, this name may have a path attached, dependent on the
                // source browser. We need to cover all possible clients...

                char[] pathSeparators = {'/', '\\'};
                // Well, if there is another separator we did not account for,
                // just add it above.
                for (int j = 0; j < pathSeparators.length; j++) {
                    int idx = fileName.lastIndexOf(pathSeparators[j]);
                    if (idx != -1) {
                        fileName = fileName.substring(idx + 1);
                        break;
                    }
                }
                // Ignore all non-jarfiles.
                if (!fileName.toLowerCase().endsWith(".jar")) {
                    invalidNames.add(fileName);
                    continue;
                }
                String deployName = fileName.substring(0, fileName.length() - 4);

                if (deployName.indexOf('.') > -1) {
                    invalidNames.add(deployName);
                    continue;
                }
                // Check if we can deploy benchmark or service.
                // If running or queued, we won't deploy benchmark.
                // If service being used by current run,we won't deploy service.
                if (!DeployUtil.canDeployBenchmark(deployName) ||
                        !DeployUtil.canDeployService(deployName)) {
                    cantDeployNames.add(deployName);
                    continue;
                }

                File uploadFile = new File(Config.BENCHMARK_DIR, fileName);
                if (uploadFile.exists())
                    FileHelper.recursiveDelete(uploadFile);

                try {
                    item.write(uploadFile);
                } catch (Exception e) {
                    throw new ServletException(e);
                }


                try {
                    DeployUtil.processUploadedJar(uploadFile, deployName);
                } catch (Exception e) {
					messageWriter.println("\nError deploying " + deployName
							+ ".\n");
					e.printStackTrace(messageWriter);
					errDeployNames.add(deployName);
					continue;
                }
                deployNames.add(deployName);
        	}
        }
        if( clearConfig ){
            for (String benchName: deployNames){
                DeployUtil.clearConfig(benchName);
            }
        }
        
        if (!hasPermission)
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        else if (cantDeployNames.size() > 0)
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        else if (errDeployNames.size() > 0)
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        else if (invalidNames.size() > 0)
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        else if (deployNames.size() > 0)
            response.setStatus(HttpServletResponse.SC_CREATED);
//        else
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        
        StringBuilder b = new StringBuilder();

        if (deployNames.size() > 0) {
            if (deployNames.size() > 1)
                b.append("Benchmarks/services ");
            else
                b.append("Benchmark/service ");

            for (int i = 0; i < deployNames.size(); i++) {
                if (i > 0)
                    b.append(", ");
                b.append((String) deployNames.get(i));
            }

            b.append(" deployed.");
            errHeaders.add(b.toString());
            b.setLength(0);
        }

        if (invalidNames.size() > 0) {
            if (invalidNames.size() > 1)
                b.append("Invalid deploy files ");
            else
                b.append("Invalid deploy file ");
            for (int i = 0; i < invalidNames.size(); i++) {
                if (i > 0)
                    b.append(", ");
                b.append((String) invalidNames.get(i));
            }
            b.append(". Deploy files must have .jar extension.");
            errHeaders.add(b.toString());
            b.setLength(0);
        }

        if (cantDeployNames.size() > 0) {
            if (cantDeployNames.size() > 1)
                b.append("Cannot deploy benchmarks/services ");
            else
                b.append("Cannot deploy benchmark/services ");
            for (int i = 0; i < cantDeployNames.size(); i++) {
                if (i > 0)
                    b.append(", ");
                b.append((String) cantDeployNames.get(i));
            }
            b.append(". Benchmark/services being used or " +
                    "queued up for run.");
            errHeaders.add(b.toString());
            b.setLength(0);
        }

        if (errDeployNames.size() > 0) {
            if (errDeployNames.size() > 1) {
                b.append("Error deploying benchmarks/services ");
                for (int i = 0; i < errDeployNames.size(); i++) {
                    if (i > 0)
                        b.append(", ");
                    b.append((String) errDeployNames.get(i));
                }
            }

            errDetails.add(messageBuffer.toString());
            errHeaders.add(b.toString());
            b.setLength(0);
        }

        if (!hasPermission)
            errHeaders.add("Permission denied!");
        
        
        
		return "ok";
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
