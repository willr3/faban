package com.sun.faban.harness.web.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faban.harness.common.Config;
import com.sun.faban.harness.util.FileHelper;
import com.sun.faban.harness.web.pojo.Benchmark;
import com.sun.faban.harness.web.pojo.Profile;
import com.sun.faban.harness.webclient.UserEnv;

public class ProfileLoader {
	private static Logger logger = Logger.getLogger(ProfileLoader.class.getName());
	private static FilenameFilter filter = new FilenameFilter(){
		//REQUIRE profile stores benchmark config as run.xml.<benchName>
		@Override
		public boolean accept(File arg0, String arg1) {
			return arg1.startsWith("run.xml.");
		}
	};

	public ProfileLoader(){}

	/**@see new-run.sp for logic on creating a profile**/
	public Profile createProfile(String profileName,String baseProfileName,String benchmarkName,List<String> tags){
		Profile rtrn = new Profile();
		
		File profileDir = new File(Config.PROFILES_DIR,profileName);
		if(!profileDir.exists()){
			profileDir.mkdirs();
		}
		if(profileDir.isDirectory()){
			
			//tags
			File tagFile = new File(profileDir,"tags");
			if(!tagFile.exists()){
				try {
					tagFile.createNewFile();
				} catch (IOException e) {
					logger.log(Level.WARNING,"Failed to create tag file for profile="+profileName,e);
				}
			}
			if(tagFile.exists()){
				StringBuilder formattedTags = new StringBuilder();
				for(String tag : tags){
					formattedTags.append(tag+"\n");
				}
				try {
					FileHelper.writeContentToFile(formattedTags.toString(), tagFile);
				} catch (IOException e) {
					logger.log(Level.WARNING,"Failed to write tags to file="+tagFile.getAbsolutePath());
					e.printStackTrace();
				}
				
			}else{
				logger.warning("Could not save tags for profile="+profileName+". Tags file does not exist");
			}
			
			//benchmark
			BenchmarkLoader benchLoader = new BenchmarkLoader();
			Benchmark bench = benchLoader.getBenchmark(benchmarkName);
			if(bench==null){
				logger.warning("Failed to load benchmark="+benchmarkName+" for profile="+profileName);
			}else{
				File localBenchFile = new File(profileDir,bench.getConfigFileName()+"."+bench.getShortName());
				if(!localBenchFile.exists()){
					File benchTemplateFile = new File(Config.BENCHMARK_DIR + File.separator + bench.getShortName() + File.separator + "META-INF" + File.separator + bench.getConfigFileName());
					
					if(benchTemplateFile.exists()){
						try{
						localBenchFile.createNewFile();
						FileHelper.copyFile(benchTemplateFile.getAbsolutePath(), localBenchFile.getAbsolutePath(), false);
						}catch(IOException e){
							logger.log(Level.WARNING,"Failed to copy benchmark template="+benchTemplateFile.getAbsolutePath()+" to profile="+localBenchFile.getAbsolutePath());
						}
					}else{
						logger.warning("Benchmark "+bench.getShortName()+" is missing template="+benchTemplateFile.getAbsolutePath());
					}
				}
			}
		}
		
		return rtrn;
	}
	
	/**@see UserEnv#getProfiles()**/
	public List<Profile> getProfiles(){
		List<Profile> rtrn = new ArrayList<Profile>();
		String fileName = Config.PROFILES_DIR;
		try{
			File f = new File(fileName);
			if(f.exists() && f.isDirectory()){
				File[] dirs = f.listFiles();
				for(int i=0; i<dirs.length; i++){
					File profileFile = dirs[i];
					Profile toAdd = new Profile();
					toAdd.setName(profileFile.getName());
					if(dirs[i].isDirectory() && !(dirs[i].getName().equals("default"))){
						rtrn.add(toAdd);
						//REQUIRE profile stores benchmark config as run.xml.<benchName>
						File[] runFiles = profileFile.listFiles(filter);
						for(int r=0; r<runFiles.length; r++){
							File runFile = runFiles[r];
							String benchName = runFile.getName().substring("run.xml.".length());
							toAdd.addBenchmark(benchName);
						}
						File tagFile = new File(profileFile.getAbsolutePath()+File.separator+"tags");
						if(tagFile.exists()){
							String[] tagList =FileHelper.readArrayContentFromFile(tagFile);
							toAdd.setTags(Arrays.asList(tagList));
						}else{
							logger.warning("Failed to load tags for profile="+profileFile.getName());
						}
					}
				}
			}else {
				logger.log(Level.WARNING,"Unable to locate profiles in "+fileName);
			}
		}catch(Exception e){
			logger.log(Level.SEVERE,"Error reading "+fileName,e);
		}
		
		
		return rtrn;
	}
}
