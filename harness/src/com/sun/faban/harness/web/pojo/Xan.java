package com.sun.faban.harness.web.pojo;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Xan {

	private transient static final Logger logger = Logger.getLogger(Xan.class.getName());
	
	private String title;
	private List<XanSection> sections;

	public Xan(){
		sections = new LinkedList<XanSection>();
	}

	public void addSection(XanSection section){
		sections.add(section);
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<XanSection> getSections() {
		return sections;
	}

	public void setSections(List<XanSection> sections) {
		this.sections = sections;
	}

	
	public void createJson(){
		StringBuilder xanBuffer = new StringBuilder(2048);
		StringBuilder lineBuffer = new StringBuilder(120);
		int sectionId = 0;
		for(XanSection section : sections){
			if(!"line".equalsIgnoreCase(section.getDisplay())){
				sectionId++;
				continue;
			}
			int columns = section.getHeaders().size();
			boolean xIsTime=false;
			for(int i=1; i< columns; i++){
				if( i==1)
					section.getDataName().append("data"+sectionId+""+i);
				else
					section.getDataName().append(", data"+sectionId+""+i);
				boolean firstRow = true;
				for(List<String> row : section.getRows()){
					if(firstRow){
						section.setMin(row.get(0));
						firstRow=false;
						lineBuffer.append("[[");
						if(i == 1){
							//determine if x-axis is time based
							xIsTime = isTime(row.get(0));
						}
					}else if (lineBuffer.length() > 70){
						//Flush buffer and newline shortly after 70 columns
						lineBuffer.append(",\n");
						xanBuffer.append(lineBuffer);
						lineBuffer.setLength(0);
						lineBuffer.append("[");
					}else{
						lineBuffer.append(",[");
					}
					lineBuffer.append(format(row.get(0))).append(",");
					lineBuffer.append(format(row.get(i))).append("]");
				}
				//get the last entry of the x-axis column
				section.setMax(format(section.getRows().get(section.getRows().size()-1).get(0)));
				lineBuffer.append("]");
				xanBuffer.append(lineBuffer);
				logger.fine("section.json = "+xanBuffer);
				section.getJson().add(xanBuffer.toString());
				lineBuffer.setLength(0);
				xanBuffer.setLength(0);
			}
			section.setXIsTime(xIsTime ? 1 : 0);
			sectionId++;
		
		}
	}
	
   private static String format(String s) {

        if ("-".equals(s))
            return "null";
        if (isTime(s)) {
            StringBuilder sb = new StringBuilder();
            // Put quotes around time to create json string value
            sb.append("'");
            sb.append(s);
            sb.append("'");
            return sb.toString();
        }
        BigDecimal bd;
        try {
            bd = new BigDecimal(s);
        } catch (NumberFormatException e) {
            return s;
        }
        bd = bd.stripTrailingZeros();
        String s1 = bd.toPlainString();
        String s2 = bd.toString();
        if (s1.length() > s2.length())
            return s2;
        else
            return s1;
    }

    private static boolean isTime(String s) {
        String t[] = s.split(":");
        if (t != null && t.length > 1)
            // This looks like time
            return (true);
        else
            return (false);
    }	

}
