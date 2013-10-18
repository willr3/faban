package com.sun.faban.harness.web.pojo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.sun.faban.harness.common.RunId;

//TODO bind variables with ${file}:<xpath|jsonpath|regex> 
//RunResult
public class Result implements Serializable{

	private static final long serialVersionUID = -6375179093935760357L;

	private long modTime = 0;
	private RunId runId;
	private String description;
	private String result;
	private String resultLink;
	private String scaleName;
	private String scale;
	private String scaleUnit;
	private double metric;
	private String metricUnit;
	private String status;
	private String logLink;
	private Date dateTime;
	private String submitter;
	private List<String> tags;

	public Result(){}
	
	public long getModTime() {
		return modTime;
	}
	public void setModTime(long modTime) {
		this.modTime = modTime;
	}
	public RunId getRunId() {
		return runId;
	}
	public void setRunId(RunId runId) {
		this.runId = runId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getResultLink() {
		return resultLink;
	}
	public void setResultLink(String resultLink) {
		this.resultLink = resultLink;
	}
	public String getScaleName() {
		return scaleName;
	}
	public void setScaleName(String scaleName) {
		this.scaleName = scaleName;
	}
	public String getScale() {
		return scale;
	}
	public void setScale(String scale) {
		this.scale = scale;
	}
	public String getScaleUnit() {
		return scaleUnit;
	}
	public void setScaleUnit(String scaleUnit) {
		this.scaleUnit = scaleUnit;
	}
	public double getMetric() {
		return metric;
	}
	public void setMetric(double metric) {
		this.metric = metric;
	}
	public String getMetricUnit() {
		return metricUnit;
	}
	public void setMetricUnit(String metricUnit) {
		this.metricUnit = metricUnit;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getLogLink() {
		return logLink;
	}
	public void setLogLink(String logLink) {
		this.logLink = logLink;
	}
	public Date getDateTime() {
		return dateTime;
	}
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	public String getSubmitter() {
		return submitter;
	}
	public void setSubmitter(String submitter) {
		this.submitter = submitter;
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
}
