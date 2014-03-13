package com.sun.faban.harness.web.loader;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faban.harness.ParamRepository;
import com.sun.faban.harness.common.BenchmarkDescription;
import com.sun.faban.harness.common.RunId;
import com.sun.faban.harness.util.FileHelper;
import com.sun.faban.harness.util.XMLReader;
import com.sun.faban.harness.web.pojo.Result;
import com.sun.faban.harness.web.pojo.RunStatus;
import com.sun.faban.harness.webclient.RunResult;

public class ResultLoader {
	private static Logger logger = Logger.getLogger(ResultLoader.class.getName());

	private SimpleDateFormat parseFormat = new SimpleDateFormat(
            "EEE MMM dd HH:mm:ss z yyyy");

	
	public Result getResult(String runId){
		Result rtrn = new Result();
		rtrn.setRunId(new RunId(runId));

		
		File resultDir = rtrn.getRunId().getResultDir();
		if(!resultDir.exists()){
			return null;//Result dir doesn't exist so cannot return a Result
		}
		
		long modTime = resultDir.lastModified();
		
		String shortName = rtrn.getRunId().getBenchName();
		
		String resultFilePath="summary.xml";
		String configFileName="run.xml";
		try{
			BenchmarkDescription desc = BenchmarkDescription.getDescription(shortName);
			if(desc == null){
				Map<String, BenchmarkDescription> benchMap = BenchmarkDescription.getBenchDirMap();
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
		File resultFile = new File(resultDir,resultFilePath);
		if(resultFile.exists() && resultFile.length()>0){
			rtrn.setResultFile(resultFile.getPath());
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

//		not compatible with new RunStatus enum
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
