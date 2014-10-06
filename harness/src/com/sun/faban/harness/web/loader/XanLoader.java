package com.sun.faban.harness.web.loader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faban.harness.web.pojo.Xan;
import com.sun.faban.harness.web.pojo.XanSection;

public class XanLoader {
	private transient static Logger logger = Logger.getLogger(XanLoader.class.getName());
	
	static final String TITLE = "Title:";
	static final String SECTION = "Section:";
	static final String DISPLAY = "Display:";
	static final String TIME = "Time";
	static final String RUNID = "RunId";
	
	
	public XanLoader(){}
	
	//matches View.parseXan
	public Xan getXan(String filePath){
		Xan rtrn = null;
		
		if(filePath==null || filePath.isEmpty()){
			return null;
		}
		
		File xanFile = new File(filePath);
		
		if(xanFile.exists() && !xanFile.isDirectory()){
			try {
				BufferedReader reader = new BufferedReader(new FileReader(xanFile));
				rtrn = new Xan();
				
				XanSection currentSection = null;
				boolean freshSection = false;
				
				String line;
				int lineNo = 0;
				while( (line=reader.readLine())!= null ){
					++lineNo;
					if(line.startsWith(TITLE)){
						rtrn.setTitle(line.substring(TITLE.length()).trim());
					}else if (line.startsWith(SECTION)){
						currentSection = new XanSection();
						currentSection.setName(line.substring(SECTION.length()).trim());
						freshSection = true;
						rtrn.addSection(currentSection);
					}else if (line.startsWith(DISPLAY) && freshSection){
						currentSection.setDisplay(line.substring(DISPLAY.length()).trim());
					}else{
						line = line.trim();
						if(line.length()==0){
							continue;
						}
						//if the line contains a - or space then stop
						int idx=0;
						for(idx=0; idx<line.length(); idx++){
							char c = line.charAt(idx);
							if( c!= '-' & c!= ' '){
								break;
							}
						}
						if(idx==line.length())
							continue;
						
						if(freshSection){
							freshSection = false;
							currentSection.setHeaders(Arrays.asList(line.split("\t|\\s{2,}")));
							System.out.println("XanLoader set headers = "+currentSection.getHeaders());
						}else{
							currentSection.addRow(Arrays.asList(line.split("\t|\\s{2,}")));
						}
					}
				}
			} catch (FileNotFoundException e) {
				logger.log(Level.WARNING,e.getMessage(),e);
			} catch (IOException e) {
				logger.log(Level.WARNING,e.getMessage(),e);
			}
		}else{
			logger.warning("Could not read Xan data from "+filePath);
		}
		return rtrn;
	}
	

}
