package com.sun.faban.harness.web;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.xml.sax.SAXParseException;

@ManagedBean(name="xslt")
@ViewScoped
public class XsltBean implements Serializable{

	private static final long serialVersionUID = -1092762544264230064L;

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
	
	public static void main(String...args){
		System.out.println(new Date());
		
		XsltBean xsl = new XsltBean();
		
		
		
		
		String xmlFile = "/home/wreicher/code/github/faban/stage/output/demoWeb.1I/summary.xml";
		String xslFile = "/home/wreicher/code/github/faban/stage/master/webapps/ROOT/xslt/summary_report.xsl";
		
		
		String output = xsl.getTransform(xmlFile, xslFile);
		
		
		System.out.println("output:");
		//System.out.println(output);
	}
}
