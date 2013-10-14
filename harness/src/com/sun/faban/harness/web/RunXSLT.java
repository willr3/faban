package com.sun.faban.harness.web;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class RunXSLT {
	
	public static void main(String...args){
		try
	    {
			URIResolver resolver = new URIResolver(){
				Source formControls = new StreamSource(new File("/home/wreicher/code/github/faban/harness/web/xslt/html-form-controls.xsl"));
				@Override
				public Source resolve(String href, String base)
						throws TransformerException {
					System.out.println("URIResolver.resolve("+href+" , "+base+")");
					if("html-form-controls.xsl".equals(href)){
						System.out.println("returning file");
						return formControls;
					}
					return null;
				}
	        };

			
	        TransformerFactory transformerfactory =
	            TransformerFactory.newInstance();
	        
	        transformerfactory.setURIResolver(resolver);
	        //transformerfactory.setAttribute("debug-enabled", true);
	        //transformerfactory.setFeature("debug-enabled", true);
	        Transformer transformer = transformerfactory.newTransformer
	            (new StreamSource(new File
	            ("/home/wreicher/code/github/faban/harness/web/xslt/faban.xsl")));
	        
	        File runTemplate = new File("/home/wreicher/code/spec/specjEnterprise2010/faban/run.xml.template");
	        File configXml = new File("/home/wreicher/code/spec/specjEnterprise2010/faban/deploy/config.xhtml");
	        
	        Source source = new StreamSource(configXml);
	        
	        Result result = null;
	        
	        
	        ByteArrayOutputStream os = new ByteArrayOutputStream();
	        StringWriter sw = new StringWriter();
	        result = new StreamResult(sw);
	        
	        transformer.transform(source,result);

	        String printMe = sw.getBuffer().toString();
	        
	        System.out.println("printMe:");
	        System.out.println(printMe);
	        
	        
		    FileReader filereader = new FileReader("/tmp/result.html");
		    BufferedReader bufferedreader = new BufferedReader(filereader);
		    String textString;

		    while((textString = bufferedreader.readLine()) != null) {
		    	System.out.println(textString);
		    }	        
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }


	}
}
