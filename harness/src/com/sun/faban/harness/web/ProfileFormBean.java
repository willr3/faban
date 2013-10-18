package com.sun.faban.harness.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;

import com.sun.faban.harness.web.loader.BenchmarkLoader;
import com.sun.faban.harness.web.loader.ProfileLoader;
import com.sun.faban.harness.web.pojo.Benchmark;
import com.sun.faban.harness.web.pojo.Profile;

@ManagedBean(name="profileform")
@ViewScoped
public class ProfileFormBean {
	private static Logger logger = Logger.getLogger(ProfileFormBean.class.getName());
	
	private Map<String,List<Profile>> benchProfiles;
	private Map<String,Profile> namedProfiles;
	
	@ManagedProperty(value="#{faban}")
	private FabanSessionBean faban;
	
	private String profileName="";
	private String newProfileName="";
	private String benchmarkName="";
	private String tagStr="";

	@PostConstruct
	public void postConstruct(){
		if(faban!=null){
			if(faban.getBenchmarkName()!=null){
				setBenchmarkName(faban.getBenchmarkName());
			}
			if(faban.getProfileName()!=null){
				setProfileName(faban.getProfileName());
			}
		}
	}
	
	public ProfileFormBean(){
		benchProfiles = new HashMap<String,List<Profile>>();
		namedProfiles = new HashMap<String,Profile>();
		
		ProfileLoader profileLoader = new ProfileLoader();
		List<Profile> profiles = profileLoader.getProfiles();
		
		//TODO check user authorization for each profile / benchmark
		for(Profile profile : profiles){
			namedProfiles.put(profile.getName(), profile);
			for(String benchName: profile.getBenchmarkNames()){
				List<Profile> benchList = benchProfiles.get(benchName);
				if(benchList==null){
					benchList = new ArrayList<Profile>();
					benchProfiles.put(benchName, benchList);
				}
				benchList.add(profile);
			}
		}
		//Check for any benchmarks that don't yet have a profile
		BenchmarkLoader benchLoader = new BenchmarkLoader();
		List<Benchmark> benchmarks = benchLoader.getBenchmarks();
		for(Benchmark bench : benchmarks){
			String benchId = bench.getShortName();
			if(!benchProfiles.containsKey(benchId)){
				List<Profile> toAdd = new ArrayList<Profile>();
				benchProfiles.put(benchId, toAdd);
			}
		}
		
		//if only 1 benchmark set benchmarkName
		benchmarkName=benchProfiles.keySet().iterator().next();
		List<Profile> p = benchProfiles.get(benchmarkName);
		if(p.size()>0){
			profileName=p.get(0).getName();
		}
		if(profileName!=null && !profileName.isEmpty()){
			List<String> tags = namedProfiles.get(profileName).getTags();
			StringBuilder rtrn = new StringBuilder();
			for(String tag : tags){
				rtrn.append(tag+" ");
			}
			tagStr = rtrn.toString().trim();
		}
	}
	public FabanSessionBean getFaban(){return faban;}
	public void setFaban(FabanSessionBean faban){
		this.faban = faban;
	}
	
	public List<String> getBenchmarks(){
		return new ArrayList<String>(benchProfiles.keySet());
	}
	public int getBenchmarkCount(){
		return benchProfiles.size();
	}
	public List<Profile> getProfiles(){
		if(benchmarkName==null || benchmarkName.isEmpty() || !benchProfiles.containsKey(benchmarkName)){
			return new ArrayList<Profile>();
		}
		return benchProfiles.get(benchmarkName);
	}
	public String getTags(){
		if(profileName == null || profileName.isEmpty() || !namedProfiles.containsKey(profileName)){
			return "";
		}
		List<String> tags = namedProfiles.get(profileName).getTags();
		StringBuilder rtrn = new StringBuilder();
		for(String tag : tags){
			rtrn.append(tag+" ");
		}
		tagStr = rtrn.toString().trim();
		return tagStr;
	}
	public void setTags(String tags){
		this.tagStr = tags;
		//TODO update the profile with the new tags
	}
	public String getNewProfileName(){return newProfileName;}
	public void setNewProfileName(String newProfileName){
		this.newProfileName=newProfileName;
	}
	public String getProfileName(){return profileName;}
	public void setProfileName(String profileName){
		this.profileName = profileName;
	}
	
	public String getBenchmarkName(){return benchmarkName;}
	public void setBenchmarkName(String benchmarkName){
		this.benchmarkName=benchmarkName;
	}
	
	//TODO move validation to a JSF validator
	public String next(){
		
		if( (profileName==null || profileName.isEmpty()) && (newProfileName==null || newProfileName.isEmpty()) ){
			return "retry";
		}
		//TODO set the Session benchmark/profile and create the backing files if needed
		faban.setBenchmarkName(benchmarkName);
		
		if(newProfileName!=null || !newProfileName.isEmpty()){
			faban.setProfileName(newProfileName);
			
			ProfileLoader profileLoader = new ProfileLoader();
			List<String> tagList = Arrays.asList(tagStr.split(" "));
			Profile newProfile = profileLoader.createProfile(newProfileName,profileName,benchmarkName,tagList);

		}
		return "next";
	}
	public void benchmarkChange(){
		
	}
	public void profileChange(){
		
	}
	
	
}
