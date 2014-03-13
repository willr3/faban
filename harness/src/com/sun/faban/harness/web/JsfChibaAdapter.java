package com.sun.faban.harness.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.chiba.adapter.AbstractChibaAdapter;
import org.chiba.adapter.InteractionHandler;
import org.chiba.tools.xslt.StylesheetLoader;
import org.chiba.tools.xslt.UIGenerator;
import org.chiba.tools.xslt.XSLTGenerator;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.Repeat;

import com.sun.faban.harness.webclient.XFormServlet;

/**@see XFormServlet.Adapter**/
@SuppressWarnings("rawtypes")
public class JsfChibaAdapter extends AbstractChibaAdapter implements InteractionHandler{
	private static Logger logger = Logger.getLogger(JsfChibaAdapter.class.getName());
	
	private HashMap beanCtx;
	private UIGenerator generator;
	
	private String xslPath;
	private String BaseUri;
	private String formUri;
	private String actionUrl;
	private String cssFile;
	private String stylesheet;
	private String dataPrefix;
	private String selectorPrefix;
	private String triggerPrefix;
	private String removeUploadPrefix;
	private String uploadRoot;
	
	private boolean isShutdown;
	
	public JsfChibaAdapter(){
		chibaBean = createProcessor();
		beanCtx = new HashMap();
		chibaBean.setContext(beanCtx);
		isShutdown=false;
	}
	
	@Override
	public void init(){
		if(getFormUri() != null){
			try{
				if(getFormUri().startsWith(com.sun.faban.harness.common.Config.FABAN_HOME)){
					FileInputStream stream = new FileInputStream(formUri);
					setXForms(stream);
				}else{
					setXForms(new URI(getFormUri()));
				}
			}catch(URISyntaxException e){
				logger.log(Level.WARNING,"Form URI not well formed ["+getFormUri()+"]",e);
			}catch(FileNotFoundException e){
				logger.log(Level.WARNING,"Form URI file not found ["+getFormUri()+"]",e);
			} catch (XFormsException e) {
				logger.log(Level.WARNING,"Failed to setXForm="+getFormUri(),e);
				e.printStackTrace();
			}
			chibaBean.setBaseURI(getBaseUri());
			
		}
		
		
		if(logger.isLoggable(Level.FINER)){
			logger.finer(toString());
			logger.finer("formUri="+getFormUri());
			logger.finer("cssFile="+getCssFile());
			logger.finer("stylesheet="+getStylesheet());
			logger.finer("actionUrl="+getActionUrl());
		}
		
        
        
        try {
			chibaBean.init();
		} catch (XFormsException e) {
			logger.log(Level.WARNING,"Failed to initialize chibaBean",e);
		}
		
		
		StylesheetLoader stylesLoader = new StylesheetLoader(xslPath);
		
		if(stylesheet!=null && !stylesheet.isEmpty()){
			stylesLoader.setStylesheetFile(stylesheet);
		}
		try{
			if(generator == null){
				generator = new XSLTGenerator(stylesLoader);
			}
			
			generator.setParameter("action-url", actionUrl);
			generator.setParameter("debug-enabled", String.valueOf(logger.isLoggable(Level.FINE)));
			
			String selectorPrefix1 = Config.getInstance().getProperty("chiba.web.selectorPrefix","s_");
			String removeUploadPrefix1 = Config.getInstance().getProperty("chiba.web.removeUploadPrefix","ru_");
			
			generator.setParameter("selector-prefix", selectorPrefix1);
			generator.setParameter("remove-upload-prefix", removeUploadPrefix1);
			
			if(cssFile != null && !cssFile.isEmpty()){
				generator.setParameter("css-file", cssFile);
			}
			
		}catch(XFormsException e){
			logger.log(Level.WARNING,"Failed to create generator with styles="+stylesLoader,e);
		}
		
		
		//load prefixes config
		
		//trigger
		if(triggerPrefix == null){
			try {
				triggerPrefix = Config.getInstance().getProperty("chiba.web.triggerPrefix","t_");
			} catch (XFormsConfigException e) {
				logger.log(Level.WARNING,"Failed to get chiba.web.triggerPrefix from Config",e);
				triggerPrefix = "t_";
			}
		}

		//data
		if(dataPrefix == null){
			try{
				dataPrefix = Config.getInstance().getProperty("chiba.web.dataPrefix", "d_");
			} catch (XFormsConfigException e){
				logger.log(Level.WARNING,"Failed to get chiba.web.dataPrefix from Config",e);
				dataPrefix = "d_";
			}
		}

		//selector
		if(selectorPrefix == null){
			try{
				selectorPrefix = Config.getInstance().getProperty("chiba.web.selectorPrefix","s_");
			} catch (XFormsConfigException e){
				logger.log(Level.WARNING,"Failed to get chiba.web.selectorPrefi from Config",e);
				selectorPrefix = "s_";
			}
		}
		
	}
	
	public boolean isShutdown(){return isShutdown;}
	@Override
	public void	shutdown(){
		if(chibaBean != null){
			try {
				chibaBean.shutdown();
				isShutdown=true;
			} catch (XFormsException e) {
				logger.log(Level.WARNING,"Failed to shutdown chibaBean",e);
			}
		}
	}
	
	
	//InteractionHandler
	//can throw XFormsException in chibaBean.dispatch
	@Override
	public void execute() throws XFormsException{
		HttpServletRequest request = (HttpServletRequest) beanCtx.get("chiba.web.request");
		
		String contextRoot = request.getSession().getServletContext().getRealPath("");
		
		if(contextRoot == null){
			contextRoot = request.getSession().getServletContext().getRealPath(".");
		}
		
		String uploadDir = (String) beanCtx.get("chiba.web.uploadDir");
		uploadRoot = new File(contextRoot,uploadDir).getAbsolutePath();
		
		String trigger=null;
		boolean isMultipart = FileUpload.isMultipartContent(request);
		
		if(logger.isLoggable(Level.FINE)){
            logger.finer("request isMultipart: " + isMultipart);
            logger.finer("base URI: " + chibaBean.getBaseURI());
            logger.finer("user agent: " + request.getHeader("User-Agent"));
		}
		
		if(isMultipart){
			trigger = processMultiPartRequest(request, trigger);
		}else{
			trigger = processUrlencodedRequest(request, trigger);
		}

		if(trigger != null){
			
			logger.fine("trigger="+trigger);
			
			chibaBean.dispatch(trigger, EventFactory.DOM_ACTIVATE);
		}
	}

	@SuppressWarnings("unchecked")
	private String processMultiPartRequest(HttpServletRequest request, String trigger){
		DiskFileUpload upload = new DiskFileUpload();
		
		String encoding = request.getCharacterEncoding();
		
		if(encoding == null){
			encoding = "ISO-8859-1";
		}
		
		upload.setRepositoryPath(uploadRoot);
		
		logger.fine("root dir for uploads: "+uploadRoot);
		
		List items = new ArrayList();
		try{
			items = upload.parseRequest(request);
		}catch(FileUploadException e){
			logger.log(Level.WARNING,"Failed to process File Uploads",e);
			//TODO throw Exception
		}
		
		Map formFields = new HashMap();
		Iterator iter = items.iterator();
		
		while(iter.hasNext()){
			try{
				FileItem item = (FileItem) iter.next();
				String itemName = item.getName();
				String fieldName = item.getFieldName();
				String id = fieldName.substring(Config.getInstance().getProperty("chiba.web.dataPrefix").length());
				
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Multipart item name is: " + itemName
                            + " and fieldname is: " + fieldName
                            + " and id is: " + id);
                    logger.fine("Is formfield: " + item.isFormField());
                    logger.fine("Content: " + item.getString());
                }
				
                if(item.isFormField()){
                	if(removeUploadPrefix == null ){
                		try{
                			removeUploadPrefix = Config.getInstance().getProperty("chiba.web.removeUploadPrefix","ru_");
                		}catch(XFormsConfigException e){
            				logger.log(Level.WARNING,"Failed to read form configuration = chiba.web.removeUploadPrefix",e);
            				removeUploadPrefix = "ru_";
            			}
                	}

                	if(fieldName.startsWith(removeUploadPrefix)){
                		id = fieldName.substring(removeUploadPrefix.length());
                		try {
							chibaBean.updateControlValue(id, "","",null);
						} catch (XFormsException e) {
							logger.log(Level.WARNING,"Failed to remove form file field with id = "+id,e);
							continue;
						}
                	}else{
                		//It is a field name, not a file. Do the same as processUrlencodedRequest
                		String values[] = (String[]) formFields.get(fieldName);
                		String formFieldValue = null;
                		try{
                			formFieldValue = item.getString(encoding);
                		}catch(UnsupportedEncodingException e){
                			logger.log(Level.WARNING,"Form field="+fieldName+" does not support encoding="+encoding,e);
                			return null;
                		}
                	
                		if(values == null){
                			formFields.put(fieldName,new String[]{formFieldValue});
                		}else{
                			String[] tmp = new String[values.length+1];
                			System.arraycopy(values, 0, tmp, 0, values.length);
                			tmp[values.length]=formFieldValue;
                			formFields.put(fieldName,tmp);
                		}
                	}
                }else{
                	//TODO this is probably where Chiba is storing the new file
                	String uniqueFileName = new File("file"+Integer.toHexString((int)(Math.random() * 10000)),new File(itemName).getName()).getPath();
                	
                	File savedFile = new File(uploadRoot, uniqueFileName);
                	
                	byte[] data = null;
                	
                	if(item.getSize() > 0){
                		try{
	                		if(chibaBean.storesExternalData(id)){
	            				try {
	            					savedFile.getParentFile().mkdir();
									item.write(savedFile);
								} catch (Exception e) {
									logger.log(Level.WARNING,"Failed to write id="+id+" to file="+savedFile.getAbsolutePath(),e);
								}
	            				try{
	            					data = savedFile.toURI().toASCIIString().getBytes(encoding);
	            				}catch(UnsupportedEncodingException e){
	            					logger.log(Level.WARNING,"Failed to save "+savedFile.getAbsolutePath()+" with encoding="+encoding,e);
	            				}
	                		}else{
	                			data = item.get();
	                		}
                		}catch(XFormsException e){
                			logger.log(Level.WARNING,"Failed to read storesExternalData for id="+id,e);
                		}
            				
                		try {
							chibaBean.updateControlValue(id, item.getContentType(), itemName, data);
						} catch (XFormsException e) {
							logger.log(Level.WARNING,"Failed to update id="+id+" name="+itemName,e);
						}
                	}
                }
                
                //handle regular fields
                if(formFields.size() > 0){
                	for(Iterator entries = formFields.entrySet().iterator(); entries.hasNext();) {
                		Map.Entry entry = (Map.Entry)entries.next();
                		fieldName = (String) entry.getKey();
                		String[] values = (String[]) entry.getValue();
                		handleData(fieldName,values);
                		handleSelector(fieldName, values[0]);
                		trigger = handleTrigger(trigger,fieldName);
                	}
                }
				
				
			}catch(XFormsConfigException e){
				logger.log(Level.WARNING,"Failed to read form configuration",e);
			}
		}
		return trigger;
	}
	
	private String processUrlencodedRequest(HttpServletRequest request,String trigger){

		Map paramMap = request.getParameterMap();
		for(Iterator entries = paramMap.entrySet().iterator(); entries.hasNext();){
			Map.Entry entry = (Map.Entry)entries.next();
			String paramName = (String) entry.getKey();
			String[] values = (String[]) entry.getValue();
			
			if(logger.isLoggable(Level.FINER)){
				StringBuilder builder = new StringBuilder();
				for(int i=0; i<values.length; i++){
					builder.append(values[i]+", ");
				}
				logger.fine(this+" param.name="+paramName+" value=["+builder.toString()+"]");

			}
			
			handleData(paramName,values);
			handleSelector(paramName, values[0]);
			trigger = handleTrigger(trigger,paramName);
		}
		
		return trigger;
	}
	
	public void buildUi(){
		Config cfg = null;
		try {
			cfg = Config.getInstance();
		} catch (XFormsConfigException e) {
			logger.log(Level.WARNING,"Failed to get Config",e);
			return;
		}
        String dataPrefix = cfg.getProperty("chiba.web.dataPrefix");
        String triggerPrefix = cfg.getProperty("chiba.web.triggerPrefix");
        String userAgent = (String) beanCtx.get("chiba.useragent");
		
		generator.setParameter("data-prefix", dataPrefix);
		generator.setParameter("trigger-prefix", triggerPrefix);
		generator.setParameter("user-agent", userAgent);
		if(cssFile != null){
			generator.setParameter("css-file", cssFile);
		}
		
		if(logger.isLoggable(Level.FINE)){
			logger.fine(">>> setting UI generator params...");
			logger.fine("data-prefix="+dataPrefix);
			logger.fine("trigger-prefix="+triggerPrefix);
			logger.fine("user-agent="+userAgent);
			if(cssFile != null){
				logger.fine("css-file="+cssFile);
			}
			logger.fine(">>> setting UI generator params...end");
		}
		
		try {
			generator.setInputNode(chibaBean.getXMLContainer());
		} catch (XFormsException e) {
			logger.log(Level.WARNING,"Failed to setInputNode to xmlContainer",e);
		}
		try {
			generator.generate();
		} catch (XFormsException e) {
			logger.log(Level.WARNING,"Failed to generate UI",e);		
		}
	}

	private void handleData(String name, String[] values){
		if(name.startsWith(getDataPrefix())){
			String id = name.substring(getDataPrefix().length());
			//assemble new control value
			String newValue;
			if(values.length > 1){
				StringBuffer buffer = new StringBuffer(values[0]);
				
				for(int i=0; i<values.length; i++){
					buffer.append(" ").append(values[i]);
				}
				newValue = trim( buffer.toString() );
			}else{
				newValue = trim( values[0] );
			}
			try{
				
				
				
				chibaBean.updateControlValue(id, newValue);
			}catch(XFormsException e){
				logger.log(Level.WARNING,"Failed to set value of id="+id+" to value="+newValue,e);
			}
		}
	}
	private String trim(String value){
		if(value != null && value.length() > 0){
			value = value.replaceAll("\r\n","\r");
			value = value.trim();
		}
		return value;
	}
	private void handleSelector(String name, String value){
		if(name.startsWith(getSelectorPrefix())){
			int separator = value.lastIndexOf(":");
			String id = value.substring(0,separator);
			int index = Integer.valueOf(value.substring(separator+1)).intValue();
			Repeat repeat = (Repeat) chibaBean.lookup(id);
			try {
				repeat.setIndex(index);
			} catch (XFormsException e) {
				logger.log(Level.WARNING,"Failed to set repeat index for name="+name+" to index="+index+" from value="+value,e);
			}
		}
	}
	private String handleTrigger(String trigger, String name){
		if ((trigger==null) && name.startsWith(getTriggerPrefix())){
			String parameter = name;
			int x = parameter.lastIndexOf(".x");
			int y = parameter.lastIndexOf(".y");
			if(x>-1){
				parameter = parameter.substring(0,x);
			}
			if(y>-1){
				parameter = parameter.substring(0,y);
			}
			
			//keep trigger id
			trigger = name.substring(getTriggerPrefix().length());
		}
		
		return trigger;
	}
	
	public HashMap getBeanCtx() {
		return beanCtx;
	}

	public void setBeanCtx(HashMap beanCtx) {
		this.beanCtx = beanCtx;
	}

	public UIGenerator getGenerator() {
		return generator;
	}

	public void setGenerator(UIGenerator generator) {
		this.generator = generator;
	}

	public String getXslPath() {
		return xslPath;
	}

	public void setXslPath(String xslPath) {
		this.xslPath = xslPath;
	}

	public String getBaseUri() {
		return BaseUri;
	}

	public void setBaseUri(String baseUri) {
		
		BaseUri = baseUri;
		
	}

	public String getFormUri() {
		return formUri;
	}

	public void setFormUri(String formUri) {
		this.formUri = formUri;
	}

	public String getActionUrl() {
		return actionUrl;
	}

	public void setActionUrl(String actionUrl) {
		this.actionUrl = actionUrl;
	}

	public String getCssFile() {
		return cssFile;
	}

	public void setCssFile(String cssFile) {
		this.cssFile = cssFile;
	}

	public String getStylesheet() {
		return stylesheet;
	}

	public void setStylesheet(String stylesheet) {
		this.stylesheet = stylesheet;
	}

	public String getDataPrefix() {
		return dataPrefix;
	}

	public void setDataPrefix(String dataPrefix) {
		this.dataPrefix = dataPrefix;
	}

	public String getSelectorPrefix() {
		return selectorPrefix;
	}

	public void setSelectorPrefix(String selectorPrefix) {
		this.selectorPrefix = selectorPrefix;
	}

	public String getTriggerPrefix() {
		return triggerPrefix;
	}

	public void setTriggerPrefix(String triggerPrefix) {
		this.triggerPrefix = triggerPrefix;
	}

	public String getRemoveUploadPrefix() {
		return removeUploadPrefix;
	}

	public void setRemoveUploadPrefix(String removeUploadPrefix) {
		this.removeUploadPrefix = removeUploadPrefix;
	}

	public String getUploadRoot() {
		return uploadRoot;
	}

	public void setUploadRoot(String uploadRoot) {
		this.uploadRoot = uploadRoot;
	}
	
	
	
	
	
}
