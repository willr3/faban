package com.sun.faban.harness.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.chiba.adapter.ChibaAdapter;
import org.chiba.xml.xforms.config.Config;

import com.sun.faban.harness.web.loader.BenchmarkLoader;
import com.sun.faban.harness.web.pojo.Benchmark;

@ManagedBean(name="chiba")
@SessionScoped
@SuppressWarnings({ "rawtypes", "unchecked" })

public class ChibaHelper {
	
	@ManagedProperty(value="#{faban}")
	private FabanSessionBean faban;

	static final Logger logger = Logger.getLogger(ChibaHelper.class.getName());
	
	private String configFile;//TODO read chiba.config from web.xml 
	private String ctxRoot;
	private String uploadDir;
	private String xsltDir;
	private String errPage;

	public ChibaHelper(){}
	
	
	public void validate(){
		
		if(faban.getBenchmarkName()==null || faban.getBenchmarkName().isEmpty() || faban.getProfileName()==null || faban.getProfileName().isEmpty()){
			FacesContext context = FacesContext.getCurrentInstance();
			try {
				System.out.println(" redirecting to selectprofile because missing profile or benchmark");
				context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath()+"/selectprofile.jsf");
			} catch (IOException e) {
				logger.log(Level.WARNING, "Failed to redirec to selectprofile.jsf when missing benchmark or profile selection",e);
				e.printStackTrace();
			}
		}
	}
	
	@PostConstruct
	public void postConstruct(){
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext ext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest)ext.getRequest();
		HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();
		HttpSession session = request.getSession(false);
		ServletContext ctx = session.getServletContext();

		String configPath = ext.getInitParameter("chiba.config");
		if(configPath==null || configPath.trim().isEmpty()){
			configPath="WEB-INF/chiba-config.xml";
		}
		configFile = ctx.getRealPath(configPath);
		xsltDir = ctx.getRealPath("xslt");
		try {
			File tmp = File.createTempFile("tmpDir", "tmpDir");
			uploadDir = tmp.getParent();
			
			tmp.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	public FabanSessionBean getFaban(){return faban;}
	public void setFaban(FabanSessionBean faban){
		this.faban = faban;
	}
	
	public String updateForm() throws IOException{
		System.out.println("ChibaHelper.updateForm()");
		
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();

		String requestMethod = request.getMethod();
		System.out.println("  request.method == "+requestMethod);
		if("POST".equals(requestMethod)){
			return this.doPost();
		}else if ("GET".equals(requestMethod)){
			return this.doGet();
		}else{
			System.out.println("  Above request method should not happen :(");
			return "<pre>Unknown Request method "+requestMethod+"</pre>";
		}
		
	}
	
	public String doPost(){
		System.out.println("doPost()");
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();
		
		HttpSession session = request.getSession(false);
		JsfChibaAdapter adapter = null;
		
		try{
			adapter = (JsfChibaAdapter) session.getAttribute("chiba.jsfAdapter");
			if(adapter == null){
				logger.warning("Failed to find Adapter for session="+session.getId());
				throw new ServletException(Config.getInstance().getErrorMessage("session-invalid"));
			}
			adapter.getBeanCtx().put("chiba.useragent", request.getHeader("User-Agent"));
			adapter.getBeanCtx().put("chiba.web.request",request);
			adapter.getBeanCtx().put("chiba.web.session", session);
			adapter.execute();
			
			String redirectURI = (String) adapter.getBeanCtx().get(ChibaAdapter.LOAD_URI);
			if(redirectURI != null){
				String redirectTo = redirectURI;
				adapter.shutdown();
				response.sendRedirect(response.encodeRedirectURL(redirectTo));
				adapter.getBeanCtx().put(ChibaAdapter.LOAD_URI, null);
				return "redirect";
			}
		
		
			//Check for forwards
			Map forwardMap = (Map) adapter.getBeanCtx().get(ChibaAdapter.SUBMISSION_RESPONSE);
			InputStream forwardStream = (InputStream) forwardMap.get(ChibaAdapter.SUBMISSION_RESPONSE_STREAM);
			if(forwardStream !=null ){
				System.out.println("  forwardStream != null");
				adapter.shutdown();
				//fetch response stream
				InputStream responseStream = (InputStream) forwardMap.remove(ChibaAdapter.SUBMISSION_RESPONSE_STREAM);
				//copy header info
				Iterator iterator = forwardMap.keySet().iterator();
				while(iterator.hasNext()){
					String name = iterator.next().toString();
					String value = forwardMap.get(name).toString();
					response.setHeader(name, value);
				}
				//copy stream content
				byte[] copyBuffer = new byte[8092];
				OutputStream out = response.getOutputStream();
				int readLength = responseStream.read(copyBuffer);
				do{
					out.write(copyBuffer, 0 , readLength);
					readLength = responseStream.read(copyBuffer);
				}while (readLength>=0);
				responseStream.close();
				out.close();
				adapter.getBeanCtx().put(ChibaAdapter.SUBMISSION_RESPONSE, null);
				return "stream";
			}
			System.out.println("  NOT A REDIRECT");
			//Not a redirect or forward, handle it normal way
			
            StringWriter out = new  StringWriter();
            adapter.getGenerator().setOutput(out);
            adapter.buildUi();
            String resp = out.getBuffer().toString().replaceAll("h_", "h:");
            return resp;
			
			
		}catch (Exception e){
			logger.log(Level.SEVERE,"Exception processing XForms post", e);
			StringWriter eWriter = new StringWriter();
			PrintWriter pw = new PrintWriter(eWriter);
			eWriter.append(e.getMessage()+"\n");
			e.printStackTrace(pw);
			return "<pre>"+eWriter.getBuffer().toString()+"</pre>";			
		}
		
	}
	public String evalJsf(String jsfText){
		FacesContext context = FacesContext.getCurrentInstance();
		Object rsp = context.getApplication().evaluateExpressionGet(context, jsfText, Object.class);
		return rsp.toString();
	}
	public String getJsf(){
		FacesContext context = FacesContext.getCurrentInstance();
		String expression = "<h:form action=\"#{chiba.doPost()}\"></h:form>";
		Object rsp = context.getApplication().evaluateExpressionGet(context, expression, Object.class);
		return rsp.toString();
	}
	
	
	public String doGet() throws IOException{
	
		FacesContext context = FacesContext.getCurrentInstance();  
		HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();  
		HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();
		HttpSession session = request.getSession(false);  
		
		
		String requestMethod = request.getMethod();
		
		System.out.println("Request.Method == "+requestMethod);
		
		JsfChibaAdapter adapter = null;
		
		BenchmarkLoader benchLoader = new BenchmarkLoader();
		
		Benchmark bench = benchLoader.getBenchmark(faban.getBenchmarkName());
		
		System.out.println("ChibaHelper calling faban.profileName = "+faban.getProfileName());
		
		String templateFile = com.sun.faban.harness.common.Config.PROFILES_DIR + File.separator + faban.getProfileName() + File.separator + bench.getConfigFileName() + "." + bench.getShortName(); 
		
		String styleSheet = null;
		if(bench.getConfigStylesheet()!=null && bench.getConfigStylesheet().isEmpty()){
			styleSheet = bench.getConfigStylesheet();
		}
		
		
		String srcUrl = new File(templateFile).toURI().toString();
		
		System.out.println("srcUrl = "+srcUrl);
		
		try{
			String requestURI = request.getRequestURI();//TODO may need to change this to a param
			String formURI = null;
			String contextPath = request.getContextPath();
			String benchPath = contextPath;// + "/bm_submit/";//TODO bm_submit is old and needs removal
			System.out.println("ChibaHelper.doGet requestURI="+requestURI+" benchPath="+benchPath);
			if(requestURI.startsWith(benchPath)){
				System.out.println("ChibaHelper.startsWith benchPath");
				int idx = requestURI.indexOf('/',benchPath.length());
				String benchName = requestURI.substring(benchPath.length(),idx);
				String formName = requestURI.substring(idx+1);
				formURI = com.sun.faban.harness.common.Config.BENCHMARK_DIR + File.separator + bench.getShortName() + File.separator + "META-INF" + File.separator + bench.getConfigForm();
				//formURI = com.sun.faban.harness.common.Config.FABAN_HOME+"benchmarks/"+benchName+"/META-INF/"+formName;
				System.out.println("  benchName="+benchName);
				System.out.println("  formName="+formName);
				System.out.println("  formURI="+formURI);
				
			}else{
				System.out.println("ChibaHelper.else ");
				System.out.println("  serverName="+request.getServerName());
				System.out.println("  serverPort="+request.getServerPort());
				System.out.println("  contextPath="+request.getContextPath());
				System.out.println("  param[form]="+request.getParameter("form"));
				
				StringBuffer buffer = new StringBuffer(request.getScheme());
				buffer.append("://");
				buffer.append(request.getServerName());
				buffer.append(":");
				buffer.append(request.getServerPort());
				buffer.append(request.getContextPath());
				buffer.append(request.getParameter("form"));
				formURI = buffer.toString();
				
			}
			
			if(formURI == null){
				throw new IOException("Resource not found: "+formURI);
			}
			
			String css = request.getParameter("css");
			String actionURL = response.encodeURL(request.getRequestURI());

            // Find the base URL used by Faban. We do not use Config.FABAN_URL
            // because this base URL can vary by the interface name the Faban
            // master is accessed in this session. Otherwise it is identical.
			StringBuffer baseURL = request.getRequestURL();
			
			int uriLength = baseURL.length() - requestURI.length() + contextPath.length();
			
			baseURL.setLength(++uriLength);
			
			adapter = new JsfChibaAdapter();
			if(configFile != null && configFile.length() > 0 ){
				// ${FABAN_HOME}/stage/master/webapps/faban/WEB-INF/chiba-config.xml
				//adapter.setConfigPath("/home/wreicher/code/local/faban/stage/master/webapps/faban/WEB-INF/chiba-config.xml");
				adapter.setConfigPath(configFile);
			}
			
			File xsl = null;
			if (styleSheet != null && !styleSheet.isEmpty()){
				xsl = new File(styleSheet);
			}
			
			if(xsl != null && xsl.exists()){
				// ${FABAN_HOME}/stage/master/webapps/faban/xslt
				adapter.setXslPath( xsl.getParent() );
				// faban.xsl
				adapter.setStylesheet( xsl.getName() );
			}else{
				adapter.setXslPath( xsltDir );
				adapter.setStylesheet("faban.xsl");//original
				adapter.setStylesheet("faban-bootstrap.xsl");//new development
			}
			
			//adapter.xslPath="/home/wreicher/code/local/faban/stage/master/webapps/faban/xslt";
			//adapter.stylesheet="faban-bootstrap.xsl";
			
			// http://${hostname}:9980/
			
			//adapter.baseURI = "http://w520:9980/";
			
           // adapter.setBaseUri(baseURL.toString());
			//adapter.setBaseUri(com.sun.faban.harness.common.Config.FABAN_HOME+File.separator+"config/profiles")
			adapter.setBaseUri("file:/home/wreicher/github/faban/stage/benchmarks/demoWeb/META-INF/");
			
            
            
            // ${FABAN_HOME}/stage/benchmarks/specjdriverharness/META-INF/config.xhtml
            adapter.setFormUri(formURI);
            //adapter.formURI = "/home/wreicher/code/local/faban/stage/benchmarks/specjdriverharness/META-INF/config.xhtml";

            /// bm_submit/specjdriverharness/config.xhtml
            adapter.setActionUrl(actionURL);
            //adapter.actionURL="/faban/bm_submit/specjdriverharness/config.xhtml";
            adapter.setActionUrl(request.getRequestURI());
            
            // ${FABAN_HOME}/stage/master/temp
            adapter.getBeanCtx().put("chiba.web.uploadDir", uploadDir);
            //adapter.beanCtx.put("chiba.web.uploadDir", "/home/wreicher/code/local/faban/stage/master/temp");
            
            // standard browser user agents
            adapter.getBeanCtx().put("chiba.useragent", request.getHeader(
                                 "User-Agent"));
            
            adapter.getBeanCtx().put("chiba.web.request", request);
            adapter.getBeanCtx().put("chiba.web.session", session);
            
            // file:${FABAN_HOME}/stage/benchmarks/specjdriverharness/META-INF/run.xml
            System.out.println("put benchmark.template = "+srcUrl);
            
            adapter.getBeanCtx().put("benchmark.template", srcUrl);
            //adapter.beanCtx.put("benchmark.template", "file:/home/wreicher/code/local/faban/stage/benchmarks/specjdriverharness/META-INF/run.xml");
			
            if (css != null) {
                adapter.setCssFile(css);
            }

            Map servletMap = new HashMap();
            servletMap.put(ChibaAdapter.SESSION_ID, session.getId());
            adapter.getBeanCtx().put(ChibaAdapter.SUBMISSION_RESPONSE, servletMap);
            
            

            Enumeration params = request.getParameterNames();
            while (params.hasMoreElements()) {
                String s = (String) params.nextElement();
                //store all request-params we don't use in the beanCtx map
                if (!(s.equals("form") || s.equals("xslt") ||
                       s.equals("css") || s.equals("action_url"))) {
                    String value = request.getParameter(s);
                    adapter.getBeanCtx().put(s, value);
                }
            }
            adapter.init();
            adapter.execute();

            StringWriter out = new  StringWriter();
            adapter.getGenerator().setOutput(out);
            adapter.buildUi();
            session.setAttribute("chiba.jsfAdapter", adapter);
            
            //Hack for trying to put jsf tags in xslt output
            String resp = out.getBuffer().toString().replaceAll("h_", "h:");
            
//            String suffix = context.getExternalContext().getInitParameter("javax.faces.DEFAULT_SUFFIX");
            
            
//            File tmpF = File.createTempFile("jsf-"+System.currentTimeMillis(), suffix);
//            String tmpName = tmpF.getName();
//            String tmpPath = "file://"+tmpF.getAbsolutePath();
//            
//            FileWriter writer = new FileWriter(tmpF);
//            writer.write(resp);
//            writer.flush();
            
            
//            System.out.println("ChibaHelper.doGet() -> "+tmpPath);
            
            return resp;
            
            //return this.evalJsf(resp);
            
		} catch (Exception e){
			StringWriter eWriter = new StringWriter();
			PrintWriter pw = new PrintWriter(eWriter);
			eWriter.append(e.getMessage()+"\n");
			e.printStackTrace(pw);
			return "<pre>"+eWriter.getBuffer().toString()+"</pre>";
			
//			String suffix = context.getExternalContext().getInitParameter("javax.faces.DEFAULT_SUFFIX");
//            File tmpF = File.createTempFile("jsf-"+System.currentTimeMillis(), suffix);
//            String tmpName = tmpF.getName();
//            String tmpPath = "file://"+tmpF.getAbsolutePath();
//            
//            FileWriter writer = new FileWriter(tmpF);
//            PrintWriter pw = new PrintWriter(writer);
//            writer.write("<pre>"+e.getMessage()+"\n");
//            e.printStackTrace(pw);
//            pw.flush();
//            
//            writer.write("</pre>");
//            writer.flush();
            
//            return tmpPath;
			
		} finally {
		}
	}
}
