package com.sun.faban.harness.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

import com.sun.faban.harness.common.Config;
import com.sun.faban.harness.web.xml.LogRecordHandler;
import com.sun.faban.harness.web.xml.RecordWorker;

@ManagedBean(name="logReader")
@ViewScoped
public class LogReaderBean implements RecordWorker, Serializable{

	private static transient Logger logger = Logger.getLogger(LogReaderBean.class.getName());
	
	private int start=0;
	private int stop=-1;
	private String logPath;
	
	private int count=0;
	LogRecordHandler handler;
	private String xslPath;
	private Transformer transformer;
	private StringBuilder buffer;
	
	public LogReaderBean(){
		//TODO expose log record xsl as something in faban configuration
		xslPath = "/xslt/log_record_table.xsl";
		handler = new LogRecordHandler(this);
		buffer = new StringBuilder();
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try{
			InputStream xslStream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(xslPath);
			transformer = transformerFactory.newTransformer(new StreamSource(xslStream));
			//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			//transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		} catch (TransformerConfigurationException e) {
 			logger.log(Level.WARNING,e.getMessage(),e);
		}		
	}
	
	public String getHtml(){
		if(buffer!=null){
			return buffer.toString();
		}
		return "nothing loaded from "+logPath;
	}
	
	public void loadHtml(){
		if(logPath==null || logPath.isEmpty()){
			logger.fine("log path was not provided, trying to find from url");
			FacesContext context = FacesContext.getCurrentInstance();
			HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();

			Map<String,String> tokens = (Map<String,String>)request.getAttribute("fabanUrlTokens");
			if(tokens==null){
				logger.warning("Failed to identify log path from url");
				return;
			}
			String runId = tokens.get("runId");
			String path = Config.OUT_DIR+File.separator+runId+File.separator+"log.xml";
			
			System.out.println("Setting logPath="+path);
			
			logPath=path;
		}
		count=0;
		buffer = new StringBuilder();
		SAXParserFactory saxFact = SAXParserFactory.newInstance();
		
		System.out.println("loading html from log="+logPath);
		
		try{
			saxFact.setFeature("http://xml.org/sax/features/validation", false);
			saxFact.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
			saxFact.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			saxFact.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
	        
			SAXParser parser = saxFact.newSAXParser();
			
	        parser.parse(new File(logPath),handler);
						
		}catch (SAXNotRecognizedException  e) {
			logger.log(Level.WARNING,e.getMessage(),e);
		} catch (SAXException e) {
			logger.log(Level.WARNING,e.getMessage(),e);
		} catch (ParserConfigurationException e) {
			logger.log(Level.WARNING,e.getMessage(),e);
		} catch (IOException e) {
			logger.log(Level.WARNING,e.getMessage(),e);
		}
	}
	
	public int getStart() {return start;}
	public void setStart(int start) {this.start = start;}

	public int getStop() {return stop;}
	public void setStop(int stop) {this.stop = stop;}
	
	public String getLogPath() {return logPath;}
	public void setLogPath(String logPath) {this.logPath = logPath;}


	@Override
	public void onRecord(Document document) {
		count++;
		if( (count>=start && count < stop) || (count >= start && stop <= start)){
			DOMSource domSource = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);

			try {
				transformer.transform(domSource, result);
				buffer.append(writer.toString());
			} catch (TransformerException e) {
				logger.log(Level.WARNING,e.getMessage(),e);
			}
		}
		
		
	}
	

}
