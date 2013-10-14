package com.sun.faban.harness.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.chiba.adapter.AbstractChibaAdapter;
import org.chiba.adapter.ChibaAdapter;
import org.chiba.tools.xslt.StylesheetLoader;
import org.chiba.tools.xslt.UIGenerator;
import org.chiba.tools.xslt.XSLTGenerator;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.Repeat;

public class RunChiba {

	private static Logger logger = Logger.getLogger(RunChiba.class.getName());
	
	static class Adapter extends AbstractChibaAdapter{
        private HashMap beanCtx = null;
        private UIGenerator generator = null;

        private String xslPath = null;
        private String baseURI = null;
        private String formURI = null;
        private String actionURL = null;
        private String CSSFile = null;
        private String stylesheet = null;
        private String dataPrefix;
        private String selectorPrefix;
        private String triggerPrefix;
        private String removeUploadPrefix;
        private String uploadRoot;
		
        public Adapter(){
			chibaBean = createProcessor();
			beanCtx = new HashMap();
			chibaBean.setContext(beanCtx);
        }
        
		@Override
		public void init() throws XFormsException {
			try{
				FileInputStream stream = new FileInputStream(formURI);
				setXForms(stream);
			}catch(FileNotFoundException e){
				e.printStackTrace();
			}
			chibaBean.setBaseURI(baseURI);
			chibaBean.init();
			StylesheetLoader stylesLoader = new StylesheetLoader(xslPath);
			
			if(stylesheet!=null)
				stylesLoader.setStylesheetFile(stylesheet);
			
			if(generator == null)
				generator = new XSLTGenerator(stylesLoader);
			
			generator.setParameter("action-url", actionURL);
			generator.setParameter("debug-enabled", "false");
			generator.setParameter("selector-prefix", "s_");
			generator.setParameter("chiba.web.removeUploadPrefix", "ru_");
			generator.setParameter("css-file",CSSFile);
		}

		@Override
		public void shutdown() throws XFormsException {
			if(chibaBean!=null)
				chibaBean.shutdown();
			
		}

		
        /**
         * Handles the request.
         *
         * @throws XFormsException
         */
        public void execute() throws XFormsException {
            HttpServletRequest request = (HttpServletRequest) beanCtx.get("chiba.web.request");

            String contextRoot = "/";//request.getSession().getServletContext().getRealPath("");
            if (contextRoot == null) {
            	//contextRoot = request.getSession().getServletContext().getRealPath(".");
                
            }

            String uploadDir = (String) beanCtx.get("chiba.web.uploadDir");
            uploadRoot = new File(contextRoot, uploadDir).getAbsolutePath();

            String trigger = null;

            // Check that we have a file upload request
            boolean isMultipart = false;//FileUpload.isMultipartContent(request);
            if (logger.isLoggable(Level.FINE)) {
                logger.finer("request isMultipart: " + isMultipart);
                logger.finer("base URI: " + chibaBean.getBaseURI());
                logger.finer("user agent: " + request.getHeader("User-Agent"));
            }

//            if (isMultipart) {
//                trigger = processMultiPartRequest(request, trigger);
//            } else {
//                trigger = processUrlencodedRequest(request, trigger);
//            }

            // finally activate trigger if any
            if (trigger != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.finer("trigger '" + trigger + "'");
                }

                chibaBean.dispatch(trigger, EventFactory.DOM_ACTIVATE);
            }
        }

        void buildUI() throws XFormsException {
            Config cfg = Config.getInstance();
            String dataPrefix = cfg.getProperty("chiba.web.dataPrefix");
            String triggerPrefix = cfg.getProperty("chiba.web.triggerPrefix");
            String userAgent = (String) beanCtx.get("chiba.useragent");

            generator.setParameter("data-prefix", dataPrefix);
            generator.setParameter("trigger-prefix", triggerPrefix);
            generator.setParameter("user-agent", userAgent);
            if (CSSFile != null) {
                generator.setParameter("css-file", CSSFile);
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine(">>> setting UI generator params...");
                logger.fine("data-prefix=" + dataPrefix);
                logger.fine("trigger-prefix=" + triggerPrefix);
                logger.fine("user-agent=" + userAgent);
                if (CSSFile != null) {
                    logger.fine("css-file=" + CSSFile);
                }
                logger.fine(">>> setting UI generator params...end");
            }

            generator.setInputNode(chibaBean.getXMLContainer());
            generator.generate();
        }


        private String processMultiPartRequest(HttpServletRequest request,
                                               String trigger)
                throws XFormsException {
            DiskFileUpload upload = new DiskFileUpload();

            String encoding = request.getCharacterEncoding();
            if (encoding == null) {
                encoding = "ISO-8859-1";
            }

            upload.setRepositoryPath(uploadRoot);

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("root dir for uploads: " + uploadRoot);
            }

            List items;
            try {
                items = upload.parseRequest(request);
            } catch (FileUploadException e) {
                throw new XFormsException(e);
            }

            Map formFields = new HashMap();
            Iterator iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                String itemName = item.getName();
                String fieldName = item.getFieldName();
                String id = fieldName.substring(Config.getInstance().
                            getProperty("chiba.web.dataPrefix").length());

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Multipart item name is: " + itemName
                            + " and fieldname is: " + fieldName
                            + " and id is: " + id);
                    logger.fine("Is formfield: " + item.isFormField());
                    logger.fine("Content: " + item.getString());
                }

                if (item.isFormField()) {

                    if (removeUploadPrefix == null) {
                        try {
                            removeUploadPrefix = Config.getInstance().
                                    getProperty("chiba.web.removeUploadPrefix",
                                                "ru_");
                        } catch (Exception e) {
                            removeUploadPrefix = "ru_";
                        }
                    }

                    if (fieldName.startsWith(removeUploadPrefix)) {
                        id = fieldName.substring(removeUploadPrefix.length());
                        chibaBean.updateControlValue(id, "", "", null);
                        continue;
                    }

                    // It's a field name, not a file. Do the same
                    // as processUrlencodedRequest
                    String values[] = (String[]) formFields.get(fieldName);
                    String formFieldValue = null;
                    try {
                        formFieldValue = item.getString(encoding);
                    } catch (UnsupportedEncodingException e1) {
                        throw new XFormsException(e1.getMessage(), e1);
                    }

                    if (values == null) {
                        formFields.put(fieldName, new String[]{formFieldValue});
                    } else {
                        String[] tmp = new String[values.length + 1];
                        System.arraycopy(values, 0, tmp, 0, values.length);
                        tmp[values.length] = formFieldValue;
                        formFields.put(fieldName, tmp);
                    }
                } else {
                    String uniqueFilename = new File("file" + Integer.
                            toHexString((int) (Math.random() * 10000)),
                            new File(itemName).getName()).getPath();

                    File savedFile = new File(uploadRoot, uniqueFilename);

                    byte[] data = null;

                    if (item.getSize() > 0)
                        if (chibaBean.storesExternalData(id)) {

                            try {
                                savedFile.getParentFile().mkdir();
                                item.write(savedFile);
                            } catch (Exception e) {
                                throw new XFormsException(e);
                            }
                            try {
                                data = savedFile.toURI().toString().
                                       getBytes(encoding);
                            } catch (UnsupportedEncodingException e) {
                                throw new XFormsException(e);
                            }

                        } else {
                            data = item.get();
                        }

                    chibaBean.updateControlValue(id, item.getContentType(),
                            itemName, data);
                }

                // handle regular fields
                if (formFields.size() > 0)
                    for (Iterator entries = formFields.entrySet().iterator();
                            entries.hasNext();) {
                        Map.Entry entry = (Map.Entry) entries.next();
                        fieldName = (String) entry.getKey();
                        String[] values = (String[]) entry.getValue();
                        handleData(fieldName, values);
                        handleSelector(fieldName, values[0]);
                        trigger = handleTrigger(trigger, fieldName);
                    }
            }
            return trigger;
        }

        private String processUrlencodedRequest(HttpServletRequest request,
                                                String trigger)
                throws XFormsException {

            Map paramMap = request.getParameterMap();
            for (Iterator entries = paramMap.entrySet().iterator();
                    entries.hasNext();) {
                Map.Entry entry = (Map.Entry) entries.next();
                String paramName = (String) entry.getKey();
                String[] values = (String[]) entry.getValue();

                if (logger.isLoggable(Level.FINER)) {
                    logger.finer(this + " parameter-name: " + paramName);
                    for (int i = 0; i < values.length; i++) {
                        logger.fine(this + " value: " + values[i]);
                    }
                }
                handleData(paramName, values);
                handleSelector(paramName, values[0]);
                trigger = handleTrigger(trigger, paramName);
            }
            return trigger;
        }

        private void handleData(String name, String[] values)
                throws XFormsException {
            if (name.startsWith(getDataPrefix())) {
                String id = name.substring(getDataPrefix().length());

                // assemble new control value
                String newValue;

                if (values.length > 1) {
                    StringBuffer buffer = new StringBuffer(values[0]);

                    for (int i = 1; i < values.length; i++) {
                        buffer.append(" ").append(values[i]);
                    }

                    newValue = trim( buffer.toString() );
                } else {
                    newValue = trim( values[0] );
                }

                chibaBean.updateControlValue(id, newValue);
            }
        }

        private String trim(String value) {
            if (value != null && value.length() > 0) {
                value = value.replaceAll("\r\n", "\r");
                value = value.trim();
            }
            return value;
        }

        private void handleSelector(String name, String value)
                throws XFormsException {
            if (name.startsWith(getSelectorPrefix())) {
                int separator = value.lastIndexOf(':');

                String id = value.substring(0, separator);
                int index = Integer.valueOf(value.substring(separator + 1)).
                            intValue();

                Repeat repeat = (Repeat) chibaBean.lookup(id);
                repeat.setIndex(index);
            }
        }

        private String handleTrigger(String trigger, String name) {
            if ((trigger == null) && name.startsWith(getTriggerPrefix())) {
                String parameter = name;
                int x = parameter.lastIndexOf(".x");
                int y = parameter.lastIndexOf(".y");

                if (x > -1) {
                    parameter = parameter.substring(0, x);
                }

                if (y > -1) {
                    parameter = parameter.substring(0, y);
                }

                // keep trigger id
                trigger = name.substring(getTriggerPrefix().length());
            }
            return trigger;
        }

        private final String getTriggerPrefix() {
            if (triggerPrefix == null) {
                try {
                    triggerPrefix = Config.getInstance().getProperty(
                                    "chiba.web.triggerPrefix", "t_");
                } catch (Exception e) {
                    triggerPrefix = "t_";
                }
            }

            return triggerPrefix;
        }

        private final String getDataPrefix() {
            if (dataPrefix == null) {
                try {
                    dataPrefix = Config.getInstance().getProperty(
                                 "chiba.web.dataPrefix", "d_");
                } catch (Exception e) {
                    dataPrefix = "d_";
                }
            }

            return dataPrefix;
        }

        private final String getSelectorPrefix() {
            if (selectorPrefix == null) {
                try {
                    selectorPrefix = Config.getInstance().getProperty(
                                     "chiba.web.selectorPrefix",
                                    "s_");
                } catch (Exception e) {
                    selectorPrefix = "s_";
                }
            }

            return selectorPrefix;
        }		
	}
	
	public static void main(String...args){
		
		Adapter adapter = new Adapter();
		try {
			adapter.setConfigPath("/home/wreicher/code/github/faban/harness/deploy/chiba-config.xml");
			adapter.xslPath="/home/wreicher/code/github/faban/harness/web/xslt/";
			adapter.stylesheet="faban.xsl";
			adapter.baseURI="http://w520:9980/";
			adapter.formURI="/home/wreicher/code/spec/specjEnterprise2010/faban/harness/faban/benchmarks/specjdriverharness/META-INF/config.xhtml";
			adapter.actionURL="/bm_submit/specjdriverharness/config.xhtml";
			adapter.beanCtx.put("chiba.web.uploadDir","/home/wreicher/code/local/faban/stage/master/temp");
			adapter.beanCtx.put("chiba.useragent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.8 Safari/537.36");
			adapter.beanCtx.put("chiba.web.request", null);
			adapter.beanCtx.put("chiba.web.session", null);
			adapter.beanCtx.put("benchmark.template", "file:/home/wreicher/code/local/faban/stage/benchmarks/specjdriverharness/META-INF/run.xml");
			
			Map servletMap = new HashMap();
			servletMap.put(ChibaAdapter.SESSION_ID, "sessionID");
			adapter.beanCtx.put(ChibaAdapter.SUBMISSION_RESPONSE, servletMap);
			
			adapter.init();
			adapter.execute();
			
			StringWriter sw = new StringWriter();
			adapter.generator.setOutput(sw);
			adapter.buildUI();
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println(sw.getBuffer().toString());

		
		} catch (XFormsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
