package com.sun.faban.harness.web;

import java.util.List;
import java.util.logging.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.sun.faban.harness.web.pojo.Benchmark;

@ManagedBean(name="benchmarkbean")
@ViewScoped
public class BenchmarkBean {
	
	private static Logger logger = Logger.getLogger(BenchmarkBean.class.getName());
	
	private List<Benchmark> benchmarkList;
	
	public BenchmarkBean(){}
	
	public List<Benchmark> getBenchmarkList(){
		if(benchmarkList==null){
		}
		return benchmarkList;
	}
	public int getCount(){
		return getBenchmarkList().size();
	}
}
