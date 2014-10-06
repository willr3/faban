package com.sun.faban.harness.web.pojo;

import java.util.LinkedList;
import java.util.List;

public class XanSection {

	private String name;
	private String link;
	private String display;
	private List<String> headers;
	private List<List<String>> rows;
	private List<String> json;
	private StringBuilder dataName;
	private int xIsTime;
	private String min;
	private String max;
	
	public XanSection(){
		headers = new LinkedList<String>();
		rows = new LinkedList<List<String>>();
		json = new LinkedList<String>();
		dataName = new StringBuilder();
	}

	public void addHeader(String header){
		headers.add(header);
	}
	public void addRow(List<String> row){
		rows.add(row);
	}
	public void addJson(String json){
		this.json.add(json);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public void setHeaders(List<String> headers) {
		this.headers = headers;
	}

	public List<List<String>> getRows() {
		return rows;
	}

	public void setRows(List<List<String>> rows) {
		this.rows = rows;
	}

	public List<String> getJson() {
		return json;
	}

	public void setJson(List<String> json) {
		this.json = json;
	}

	public StringBuilder getDataName() {
		return dataName;
	}

	public void setDataName(StringBuilder dataName) {
		this.dataName = dataName;
	}

	public int getXIsTime() {
		return xIsTime;
	}

	public void setXIsTime(int xIsTime) {
		this.xIsTime = xIsTime;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}
}
