package com.sun.faban.harness.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXParseException;

import com.sun.faban.harness.common.Config;
import com.sun.faban.harness.web.xml.LogRecordHandler;
import com.sun.faban.harness.web.xml.RecordWorker;

@ManagedBean(name="xslt")
@ViewScoped
public class XsltBean implements RecordWorker, Serializable{

	
	private static transient Logger logger = Logger.getLogger(XsltBean.class.getName());
	
	private static final long serialVersionUID = -1092762544264230064L;
	
	
	public void foo(String arg){
		System.out.println("XsltBean.foo("+arg+")");
	}
	
	
	public String getConfig(){
		FacesContext context = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)context.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse)context.getExternalContext().getResponse();

		Map<String,String> tokens = (Map<String,String>)request.getAttribute("fabanUrlTokens");
		if(tokens==null){
			logger.warning("Failed to identify log path from url");
			return "could not identify run from path";
		}
		String runId = tokens.get("runId");
		String path = Config.OUT_DIR+File.separator+runId+File.separator+"run.xml";


		
		BufferedReader br=null;
	    try {
	    	br = new BufferedReader(new FileReader(path));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    }catch(IOException e){
	    	logger.log(Level.WARNING,e.getMessage(),e);
	    } finally {
	        try {
				if(br!=null) br.close();
			} catch (IOException e) {}
	    }
		return "Error while reading config for "+runId;
	}
	public String getTransform(String xmlPath,String xslPath){
		System.out.println("XsltBean.getTransform("+xmlPath+" , "+xslPath+")");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		Document document;
		try{
			//Work for SAXParserFactory
			//factory.setFeature("http://xml.org/sax/features/validation", false);
			//factory.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
			//factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			//factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			
			
			
			File f = new File(xmlPath);
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(f);
			
			XPath xPath = XPathFactory.newInstance().newXPath();
			
			String expression = "processing-instruction('xml-stylesheet')";
			
			ProcessingInstruction pi;
			
			try {
				pi = (ProcessingInstruction)xPath.evaluate(expression, document,XPathConstants.NODE);
				System.out.println(" pi = "+pi);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			DOMSource domSource = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer(new StreamSource(new File(xslPath)));
			transformer.transform(domSource, result);
			return writer.toString();
		}catch(SAXParseException e){
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Failed to parse "+xmlPath;
	}
	
	@Override
	public void onRecord(Document document) {
		
		
	}
	
	
	public static void main(String...args){
		System.out.println(new Date());
		
		XsltBean xsl = new XsltBean();
		
		final String xslPath = "/home/wreicher/code/github/faban/harness/web/xslt/log_record.xsl";
		//final String xmlFile = "/home/wreicher/code/github/faban/stage/output/demo-jsf.6G/log.xml";
		final String xmlFile = "/home/wreicher/specWork/sample.log.xml";

		RecordWorker worker = new RecordWorker(){

			@Override
			public void onRecord(Document document) {
				// TODO Auto-generated method stub
				DOMSource domSource = new DOMSource(document);
				StringWriter writer = new StringWriter();
				StreamResult result = new StreamResult(writer);
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				

				try {
					//Transformer transformer = transformerFactory.newTransformer();
					
					//

					Transformer transformer = transformerFactory.newTransformer(new StreamSource(new File(xslPath)));
				
					
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
					
					transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
					
					
					//transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,"no");
					//transformer.setOutputProperty(OutputKeys.STANDALONE, "yes"); 
					
					transformer.transform(domSource, result);
					System.out.println(">--------------------------");
					System.out.println(writer.toString());
					System.out.println("<--------------------------");

					
				} catch (TransformerConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			
		};
		
		LogRecordHandler h = new LogRecordHandler(worker);
		
        SAXParserFactory sFact = SAXParserFactory.newInstance();
        try {
			sFact.setFeature("http://xml.org/sax/features/validation", false);
	        sFact.setFeature("http://apache.org/xml/features/" +
	                "allow-java-encodings", true);
	        sFact.setFeature("http://apache.org/xml/features/nonvalidating/" +
	                "load-dtd-grammar", false);
	        sFact.setFeature("http://apache.org/xml/features/nonvalidating/" +
	                "load-external-dtd", false);
	        SAXParser parser = sFact.newSAXParser();
			
	        parser.parse(new File(xmlFile),h);
	        
		} catch (SAXNotRecognizedException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
