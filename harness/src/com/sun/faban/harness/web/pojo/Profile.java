package com.sun.faban.harness.web.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Profile implements Serializable{

	private static final long serialVersionUID = -5308917467767675016L;

	private String name;
	private List<String> tags;
	private List<String> benchmarkNames;
	
	public Profile(){
		name="";
		tags = new ArrayList<String>();
		benchmarkNames = new ArrayList<String>();
	}

	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getBenchmarkNames() {
		return benchmarkNames;
	}

	public void setBenchmarkNames(List<String> benchmarkNames) {
		this.benchmarkNames = benchmarkNames;
	}
	
	public void addBenchmark(String benchmarkName){
		if(this.benchmarkNames==null){
			this.benchmarkNames=new ArrayList<String>();
		}
		benchmarkNames.add(benchmarkName);
	}
}
