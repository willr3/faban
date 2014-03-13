package com.sun.faban.harness.web.pojo;

import com.sun.faban.harness.common.BenchmarkDescription;

public class Benchmark {

	private String bannerPage;
	private String shortName;
	private String name;
	private String version;
	private String configForm="config.xhtml";
	private String configStylesheet;
	private String configFileName="run.xml";
	private String resultFilePath="summary.xml";
	private String benchmarkClass;
	private String metric;
	private String scaleName;
	private String scaleUnit;
	
	public Benchmark(){}

	public String getBannerPage() {
		return bannerPage;
	}

	public void setBannerPage(String bannerPage) {
		this.bannerPage = bannerPage;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getConfigForm() {
		return configForm;
	}

	public void setConfigForm(String configForm) {
		this.configForm = configForm;
	}

	public String getConfigStylesheet() {
		return configStylesheet;
	}

	public void setConfigStylesheet(String configStylesheet) {
		this.configStylesheet = configStylesheet;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	public String getResultFilePath() {
		return resultFilePath;
	}

	public void setResultFilePath(String resultFilePath) {
		this.resultFilePath = resultFilePath;
	}

	public String getBenchmarkClass() {
		return benchmarkClass;
	}

	public void setBenchmarkClass(String benchmarkClass) {
		this.benchmarkClass = benchmarkClass;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getScaleName() {
		return scaleName;
	}

	public void setScaleName(String scaleName) {
		this.scaleName = scaleName;
	}

	public String getScaleUnit() {
		return scaleUnit;
	}

	public void setScaleUnit(String scaleUnit) {
		this.scaleUnit = scaleUnit;
	}
}
