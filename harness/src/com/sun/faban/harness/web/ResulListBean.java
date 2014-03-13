package com.sun.faban.harness.web;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.sun.faban.harness.ParamRepository;
import com.sun.faban.harness.common.BenchmarkDescription;
import com.sun.faban.harness.common.Config;
import com.sun.faban.harness.common.RunId;
import com.sun.faban.harness.util.FileHelper;
import com.sun.faban.harness.util.XMLReader;
import com.sun.faban.harness.web.pojo.Result;
import com.sun.faban.harness.web.pojo.RunStatus;
import com.sun.faban.harness.webclient.RunResult;

@ManagedBean(name="resultbean")
@ViewScoped
public class ResulListBean implements Serializable{
	private static final long serialVersionUID = 4605746748381592433L;
	Logger logger = Logger.getLogger(ResulListBean.class.getName());

	private SimpleDateFormat parseFormat = new SimpleDateFormat(
            "EEE MMM dd HH:mm:ss z yyyy");
	
	private List<Result> results;
	private int sortDirection = 1;
	private int sortColumnId = 0;
	
	public ResulListBean(){
		results = new ArrayList<Result>();
	}
	//TODO implement ResultObject that is sortable by default
	//all sorters return descending by default
	private static List<Comparator<Result>> sorters = new ArrayList<Comparator<Result>>();
	static{
		sorters.add(new Comparator<Result>(){//RunId
			@Override
			public int compare(Result arg0, Result arg1) {
				return arg1.getRunId().toString().compareTo(arg0.getRunId().toString());
			}
		});
		sorters.add(new Comparator<Result>(){//Description
			@Override
			public int compare(Result arg0, Result arg1) {
				return arg1.getDescription().toString().compareTo(arg0.getDescription().toString());
			}
		});
		sorters.add(new Comparator<Result>(){//Result
			@Override
			public int compare(Result arg0, Result arg1) {
				return arg1.getResult().toString().compareTo(arg0.getResult().toString());
			}
		});
		sorters.add(new Comparator<Result>(){// Scale
			@Override
			public int compare(Result arg0, Result arg1) {
				return arg1.getScale().toString().compareTo(arg0.getScale().toString());
			}
		});
		sorters.add(new Comparator<Result>(){ //Metric
			@Override
			public int compare(Result arg0, Result arg1) {
				return Double.compare(arg1.getMetric(),arg0.getMetric());
			}
		});
		sorters.add(new Comparator<Result>(){ //Date/Time
			@Override
			public int compare(Result arg0, Result arg1) {
				return arg1.getDateTime().toString().compareTo(arg0.getDateTime().toString());
			}
		});
		sorters.add(new Comparator<Result>(){ //Submitter
			@Override
			public int compare(Result arg0, Result arg1) {
				String s0 = arg0.getSubmitter()==null? "" : arg0.getSubmitter();
				String s1 = arg1.getSubmitter()==null? "" : arg1.getSubmitter();
				return s1.compareTo(s0);
			}
		});
		sorters.add(new Comparator<Result>(){ //Tags
			@Override
			public int compare(Result arg0, Result arg1) {
				return arg1.getTags().toString().compareTo(arg0.getTags().toString());
			}
		});			
	}
	
	
	
	public List<Result> getList(){
		if(results.isEmpty()){
			results = loadResults();
		}
		return results;
	}
	public int getSortColumn(){
		return sortColumnId;
	}
	public String getDirection(){
		if(sortDirection==1)
			return "DESCENDING";
		else
			return "ASCENDING";
	}
	public void sort(int columnId){
		if(columnId == sortColumnId){
			sortDirection = -sortDirection;
			Collections.reverse(results);
		}else{
			sortColumnId=columnId;
			sortDirection = 1;
			Collections.sort(results, sorters.get(sortColumnId));
		}
	}
	
	private List<Result> loadResults(){
		List<Result> rtrn = new ArrayList<Result>();
		
		File[] dirs = new File(Config.OUT_DIR).listFiles();
		for(File runDir : dirs){
			if(!runDir.isDirectory()){
				logger.info(runDir.getName()+" is not a directory");
			}else{
				String runIdS = runDir.getName();
				try{
					//TODO need to skip the analysis dir
					//TODO check isViewAllowed by adding security
					RunId runId = new RunId(runIdS);
					
					//this will probably thrash a bit :)
					Result result = this.getResult(runId);
					if(result!=null){
						rtrn.add(result);
					}
				}catch(Exception e){
					e.printStackTrace();
					logger.log(Level.WARNING,"Cannot read result directory "+runIdS,e);
				}
			}
		}
		
		Collections.sort(rtrn, new Comparator<Result>() {
			@Override
			public int compare(Result arg0, Result arg1) {
				//reverse order
				return arg1.getRunId().toString().compareTo(arg0.getRunId().toString());
			}
		});
		return rtrn;
	}
	
	private Result getResult(RunId runId){
		Result rtrn = new Result();
		rtrn.setRunId(runId);
		
		File resultDir = runId.getResultDir();
		long modTime = resultDir.lastModified();

		String shortName = runId.getBenchName();
		
		String resultFilePath="summary.xml";
		String configFileName="run.xml";
		try{
		BenchmarkDescription desc = BenchmarkDescription.getDescription(shortName, resultDir.getAbsolutePath());
		if(desc == null){
            Map<String, BenchmarkDescription> benchMap =
                    BenchmarkDescription.getBenchDirMap();
            desc = (BenchmarkDescription) benchMap.get(shortName);
		}
		if(desc == null){
			logger.warning(runId.toString() + ": Cannot find benchmark " +
                    "description in result and benchmark not deployed, " +
                    "trying default values");
            // Assigning default values;
            resultFilePath = "summary.xml";
            configFileName = "run.xml";
		}else{
            resultFilePath = desc.resultFilePath;
            configFileName = desc.configFileName;
            rtrn.setScaleName(desc.scaleName);
            rtrn.setScaleUnit(desc.scaleUnit);
            rtrn.setMetricUnit(desc.metric);
		}
		}catch(Exception e){
			logger.log(Level.WARNING,"Could not read BenchmarkDescription for run "+runId);
		}
		String[] statusFileContent = RunResult.readStatus(runId.toString());
		File resultFile = new File(resultDir,"summary.xml");
		if(resultFile.exists() && resultFile.length()>0){
			rtrn.setResult(com.sun.faban.harness.web.pojo.RunResult.PASSED);
			rtrn.setResultLink("/resultframe.jsp?runId="+rtrn.getRunId()+"&result="+resultFilePath);
		
		
	        //Use the XMLReader and locate the <passed> elements
	        XMLReader reader = new XMLReader(resultFile.
	                getAbsolutePath());
	
	        rtrn.setMetric(Double.parseDouble(reader.getValue("benchSummary/metric")));
	        try{
	        rtrn.setDateTime(parseFormat.parse(reader.getValue("benchSummary/endTime")));
	        }catch(ParseException e){
	        	logger.warning("Could not parse endTime from "+rtrn.getRunId());
	        }
	        
	        List<String> passedList = reader.getValues("passed");
	        for(String passed : passedList){
	        	if(passed.toUpperCase().indexOf("FALSE") != -1){
	        		rtrn.setResult(com.sun.faban.harness.web.pojo.RunResult.FAILED);
	        		break;
	        	}
	        }
		}
        rtrn.setStatus(RunStatus.valueOf(statusFileContent[0]));

//		no longer supported because of change to enum
//        if(rtrn.getResult()==null){
//        	rtrn.setResult(rtrn.getStatus());
//        }
        if(rtrn.getResult()==null){
        	rtrn.setResult(com.sun.faban.harness.web.pojo.RunResult.NOT_AVAILABLE);
        }
        
        if(!"UNKNOWN".equals(rtrn.getStatus()) || new File(resultDir,"log.xml").isFile()){
        	StringBuilder b = new StringBuilder("/resultframe.jsp?runId=");
        	b.append(rtrn.getRunId());
        	b.append("&result=");
        	b.append(resultFilePath);
        	b.append("&show=logs");
        	
        	rtrn.setLogLink(b.toString());
        }else{
        	rtrn.setLogLink("");
        }
        
        if (rtrn.getDateTime()==null && statusFileContent[1] != null){
        	try{
        		rtrn.setDateTime(parseFormat.parse(statusFileContent[1]));
        	}catch(ParseException e){
        		logger.warning("Failed to parse dateTime from statusFileContent[1]="+statusFileContent[1]);
        	}
        }
        
        String paramFileName = resultDir.getAbsolutePath() + File.separator + configFileName;
        File paramFile = new File(paramFileName);
        
        if(paramFile.isFile()){
            // Compatible with old versions of Config.RESULT_INFO
            // Old version does not have timestamp in RESULT_INFO
            // So we need to establish it from the paramFile timestamp.
            // This block may be removed in future.
            if (rtrn.getDateTime() == null) {
                rtrn.setDateTime(new Date(paramFile.lastModified()));
            }
            // End compatibility block
        	
            ParamRepository par = new ParamRepository(paramFileName, false);
            rtrn.setDescription(par.getParameter("fa:runConfig/fh:description"));
        	rtrn.setScale(par.getParameter("fa:runConfig/fa:scale"));
        }else{
        	logger.warning(rtrn.getRunId().toString()+": Parameter file invalid or non-existent.");
        }
        File submitterFile = new File(resultDir,"META-INF"+File.separator+"submitter");
        if(submitterFile.exists()){
        	try{
        		rtrn.setSubmitter(FileHelper.readStringFromFile(submitterFile).trim());
        	}catch(IOException e){
        		logger.log(Level.WARNING,"Error reading submitter file for run "+rtrn.getRunId(),e);
        	}
        }
        
        File tagsFile = new File(resultDir,"META-INF" + File.separator+"tags");
        if(tagsFile.exists()){
        	try{
        		rtrn.setTags(Arrays.asList(FileHelper.readArrayContentFromFile(tagsFile)));
        	}catch(IOException e){
        		logger.log(Level.WARNING,"Error reading tags for run "+rtrn.getRunId(),e);
        	}
        }else{
        	rtrn.setTags(Arrays.asList(new String[]{}));
        }
        
		return rtrn;
		
	}
}
