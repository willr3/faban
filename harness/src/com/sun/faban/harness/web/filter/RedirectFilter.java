package com.sun.faban.harness.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;



@WebFilter(filterName = "RedirectFilter",
urlPatterns = {"/*"},
dispatcherTypes = {DispatcherType.REQUEST})
public class RedirectFilter implements Filter{
	private static Logger logger = Logger.getLogger(RedirectFilter.class.getName());
	
	private UrlPathTree paths;
	
	public RedirectFilter(){
		paths = new UrlPathTree();
	}
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		paths.addPath(UrlToken.parsePath("results/{runId}"), "/welcome.jsf");
		paths.addPath(UrlToken.parsePath("results/{runId}/info"), "/run-info.jsf");
		paths.addPath(UrlToken.parsePath("results/{runId}/log"), "/run-log.jsf");
		paths.addPath(UrlToken.parsePath("results/{runId}/summary"), "/run-summary.jsf");
		paths.addPath(UrlToken.parsePath("results/{runId}/details"), "/xan-view.jsf");
		paths.addPath(UrlToken.parsePath("results/{runId}/config"), "/run-config.jsf");
		paths.addPath(UrlToken.parsePath("results"), "/result-list.jsf");
		//TODO this is not intercepting the workflow from Chiba, so must change config.xhtml submission target :(
		//paths.addPath(UrlToken.parsePath("/schedule-run.jsp"), "/schedule-run.jsf");
		//paths.addPath(UrlToken.parsePath("schedule-run.jsp"), "/schedule-run.jsf");
		
		paths.print();
	}
	
	public String getTokenString(HttpServletRequest request){
		String host = request.getServerName();
		String ctxPath = request.getContextPath();
		String urlPath = request.getRequestURL().toString();
		
		if(urlPath.contains(host)){
			urlPath = urlPath.substring(urlPath.indexOf("/",urlPath.indexOf(host)));
		}
		if(urlPath.startsWith(ctxPath)){
			urlPath = urlPath.substring(ctxPath.length());
		}
		
		return urlPath;
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		boolean forwarded=false;
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = (HttpServletRequest)request;

			System.out.println("JsfRedirectFilter.doFilter("+httpRequest.getRequestURL()+")");
			
			String urlPath = getTokenString(httpRequest);
			
			HashMap<String,String> map = new HashMap<String,String>();
			String target = paths.getTarget(UrlToken.getTokenPath(urlPath), map);
			System.out.println("  "+urlPath+" --> "+target);
			
			if(target!=null){
				forwarded=true;
				
				request.setAttribute("fabanUrlTokens", Collections.unmodifiableMap(map));
				//request.setAttribute("fabanUrlTokens", "hi mom");
				request.setAttribute("foo", "foooooooooooooo");
				System.out.println("  Context:");
				for(Entry<String,String> entry : map.entrySet()){
					System.out.println("    ["+entry.getKey()+"] -> "+entry.getValue());
				}
				request.getRequestDispatcher(target).forward(httpRequest, response);
			}
		}
		if(!forwarded){
			chain.doFilter(request, response);
		}
		
		
	}

	@Override
	public void destroy() {
		System.out.println("JsfRedirectFilter.destroy()");
		
	}

	
}
