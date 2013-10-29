package com.sun.faban.harness.web.filter;

import java.util.ArrayList;
import java.util.List;

public class UrlToken {

	public static final String PARAM_PREFIX="{";
	public static final String PARAM_SUFFIX="}";
	
	private String name;
	private boolean isParam;
	
	public UrlToken(String name){
		if(name==null)
			name="";
		if(name.startsWith(PARAM_PREFIX) && name.endsWith(PARAM_SUFFIX)){
			name = name.substring(1, name.length()-1);
			isParam = true;
		}
		this.name = name;
	}
	
	public boolean isParam(){return isParam;}
	public String getName(){return name;}
	
	
	public boolean matches(String token){
		if(isParam)
			return true;
		else
			return name.equals(token);
	}
	 
	public static String[] getTokenPath(String url){
		String rtrn = url;
		while(rtrn.startsWith(".")){
			rtrn = rtrn.substring(1);
		}
		while(rtrn.startsWith("/")){
			rtrn = rtrn.substring(1);
		}
		while(rtrn.endsWith("/"))
			rtrn = rtrn.substring(0, rtrn.length()-1);
		
		return rtrn.split("/");
	}
	
	public static List<UrlToken> parsePath(String url){
		List<UrlToken> rtrn = new ArrayList<UrlToken>();
		
		String split[] = getTokenPath(url);
		
		for(int i=0; i<split.length; i++){
			UrlToken add = new UrlToken(split[i]);
			rtrn.add(add);
		}
		return rtrn;
	}
	
	public String toString(){
		if(isParam()){
			return PARAM_PREFIX+getName()+PARAM_SUFFIX;
		}else{
			return getName();
		}
	}
	public int hashCode(){
		String hashMe= name+isParam;
		return hashMe.hashCode();
	}
	public boolean equals(Object obj){
		if(obj instanceof UrlToken){
			UrlToken token = (UrlToken)obj;
			return this.isParam()==token.isParam() && this.getName().equals(token.getName());
		}
		return false;
	}
}
