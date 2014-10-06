package com.sun.faban.harness.web.pojo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Profile implements Serializable{

	private static final long serialVersionUID = -5308917467767675016L;

	private String name;
  private String benchmarkName;
	private List<String> tags;

	
	public Profile(){
		name="";
    benchmarkName="";
		tags = new ArrayList<String>();
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

	public String getBenchmarkName() {
		return benchmarkName;
	}
	public void setBenchmarkName(String benchmarkName) {
		this.benchmarkName = benchmarkName;
	}
}
