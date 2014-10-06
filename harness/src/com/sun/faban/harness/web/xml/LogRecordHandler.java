package com.sun.faban.harness.web.xml;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LogRecordHandler extends DefaultHandler{

	private static transient Logger logger = Logger.getLogger(LogRecordHandler.class.getName());
	
	private Document doc;
	private Element root;
	
	private DocumentBuilder builder;
	private RecordWorker worker;
	
	
	public LogRecordHandler(RecordWorker worker){
		this.doc=null;
		this.worker = worker;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.log(Level.WARNING,"Failed to create DocumentBuilder for LogRecord parsing",e);
		}
	}

	public void setWorker(RecordWorker worker){
		this.worker = worker;
	}
	public RecordWorker getWorker(){
		return worker;
	}
	
    /**
     * Receive notification of the start of an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the start of
     * each element (such as allocating a new tree node or writing
     * output to a file).</p>
     *
     * @param uri        The Namespace URI, or the empty string if the
     *                   element has no Namespace URI or if Namespace
     *                   processing is not being performed.
     * @param localName  The local name (without prefix), or the
     *                   empty string if Namespace processing is not being
     *                   performed.
     * @param qName      The qualified name (with prefix), or the
     *                   empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *                   there are no attributes, it shall be an empty
     *                   Attributes object.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
	public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
		
		if("record".equals(qName)){
			if(doc!=null){
				//This should not happen as we emit on endElement but malformed xml is possible
				emitRecord();
			}
			doc = builder.newDocument();
			root = doc.createElement(qName);
			if(attributes.getLength()>0){
				for(int i=0; i<attributes.getLength(); i++){
					root.setAttribute(attributes.getQName(i), attributes.getValue(i));
				}
			}
			doc.appendChild(root);
		} else {
			if(doc!=null && root!=null){//if currently building a document
				Element el = doc.createElement(qName);
				if(attributes.getLength()>0){
					for(int i=0; i<attributes.getLength(); i++){
						el.setAttribute(attributes.getQName(i), attributes.getValue(i));
					}
				}
				root.appendChild(el);
				root=el;
			}
		}
		
		
	}
	
	public void characters(char[] ch, int start, int length){
		String val=new String(ch,start,length);
		if(val.trim().isEmpty() ){//characters is called for anything even whitespace
			
		}else{
			if(root!=null){
				Text t = doc.createTextNode(val);
				root.appendChild(t);
			}else{
				
			}
		}
	}
	public void reset(){
		doc=null;
		root=null;
	}
	private void emitRecord(){
		worker.onRecord(doc);
		reset();
	}
	
    /**
     * Receive notification of the end of an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end of
     * each element (such as finalising a tree node or writing
     * output to a file).</p>
     *
     * @param uri       The Namespace URI, or the empty string if the
     *                  element has no Namespace URI or if Namespace
     *                  processing is not being performed.
     * @param localName The local name (without prefix), or the
     *                  empty string if Namespace processing is not being
     *                  performed.
     * @param qName     The qualified name (with prefix), or the
     *                  empty string if qualified names are not available.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    	System.out.println("endElement("+uri+" , "+localName+" , "+qName+")");
    	System.out.println("  root="+root);
    	if(root!=null){
	    	Node parent = root.getParentNode();
	    	if(parent instanceof Element){
	    		root = (Element)parent;
	    	}
	    	if("record".equals(qName)){
				emitRecord();
	    	}
    	}else{
    		//likely the log tag, nothing to do
    	}
    	
    	
    }

	
}
