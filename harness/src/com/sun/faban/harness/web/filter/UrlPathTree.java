package com.sun.faban.harness.web.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class UrlPathTree {

	private static Comparator<UrlPathNode> COMPARE_NODE = new Comparator<UrlPathNode>(){
		@Override
		public int compare(UrlPathNode arg0, UrlPathNode arg1) {
			return COMPARE_TOKEN.compare(arg0.getToken(), arg1.getToken());
		}
		
	};
	private static Comparator<UrlToken> COMPARE_TOKEN = new Comparator<UrlToken>() {
		@Override
		public int compare(UrlToken arg0, UrlToken arg1) {
			Boolean bool = arg0.isParam();
			return bool.compareTo(arg1.isParam());
		}
	};
	
	public static void main(String...args){
	
		UrlPathTree tree = new UrlPathTree();
		
		tree.addPath(UrlToken.parsePath("/hi/{person}/known"), "known.jsf");
		tree.addPath(UrlToken.parsePath("/hi/{person}/unknown"), "unknown.jsp");
		tree.addPath(UrlToken.parsePath("/hi/person/unknown"), "unknown-generic.jsp");
		tree.addPath(UrlToken.parsePath("/hi/{person}/"), "person.jsp");
	
		tree.print();
		
		System.out.println(tree.getTarget(UrlToken.getTokenPath("/hi/mom/"), null));
	}

	

	private List<UrlPathNode> roots;

	
	public UrlPathTree(){
		roots = new ArrayList<UrlPathNode>();
	}

	public void addPath(List<UrlToken> tokens,String target){
		List<UrlPathNode> nodes = roots;
		for(int i=0; i<tokens.size(); i++){
			
			UrlToken token = tokens.get(i);
			boolean found=false;
			for(int n=0; n<nodes.size(); n++){
				UrlPathNode node = nodes.get(n);
				if(node.getToken().equals(token)){
					found=true;
					nodes = node.getChildren();
					if(i==tokens.size()-1 && target!=null){
						node.setTarget(target);
					}
					break;
				}
			}
			if(!found){
				UrlPathNode newNode = new UrlPathNode(token,(i==tokens.size()-1)?target:null);
				nodes.add(newNode);
				Collections.sort(nodes, COMPARE_NODE);
				nodes = newNode.getChildren();
			}
		}
	}
	
	public String getTarget(String[] tokens,Map<String,String> context){
		for(int i=0; i<roots.size(); i++){
			String target = roots.get(i).getTarget(tokens, 0, context);
			if(target!=null)
				return target;
		}
		return null;
	}
	
	public void print(){
		System.out.println("UrlPathTree:");
		for(UrlPathNode node : roots){
			node.print(0);
		}
	}
	
	private class UrlPathNode{
		
		private UrlToken token;
		private String target;
		private List<UrlPathNode> children;
		
		public UrlPathNode(UrlToken token){
			this(token,null);
		}
		public UrlPathNode(UrlToken token,String target){
			this.token=token;
			this.target=target;
			this.children = new ArrayList<UrlPathNode>();
		}
		
		public String getTarget(String tokens[], int index,Map<String,String> context){
			if(index>=tokens.length){ //this should never happen but just in case
				return null;
			}else if(!token.matches(tokens[index])){ //does not match so do not check children
				System.out.println(pad("token="+token+" does not match "+tokens[index],index));
				return null;
			}else{ //This token matches
				System.out.println(pad("token="+token+" MATCHES "+tokens[index],index));
				if(token.isParam() && context!=null){
					context.put(token.getName(), tokens[index]);
				}
				if(index==tokens.length-1){
					return getTarget();
				}
				if(!hasChild() ){
					return getTarget();
				}else{//check for a child match
					for(int i=0; i<children.size(); i++){
						UrlPathNode child = children.get(i);
						String childTarget = child.getTarget(tokens, index+1, context); 
						if(childTarget!=null)
							return childTarget;//return first child to match
					}
					if(token.isParam() && context!=null){
						context.remove(token.getName());
					}
					return null;
				}
			}
		}
		public void setTarget(String target){
			this.target = target;
		}
		public List<UrlPathNode> getChildren(){return children;} 
		public String getTarget(){return target;}
		public UrlToken getToken(){return token;}
		public boolean hasTarget(){return target!=null && !target.isEmpty();}
		public boolean hasChild(){return !children.isEmpty();}
		
		public void print(int padding){
			StringBuilder rtrn = new StringBuilder();
			for(int i=0; i<padding;i++){
				rtrn.append("  ");
			}
			System.out.println(rtrn.toString()+getToken()+(hasTarget()?"-->"+getTarget():""));
			for(UrlPathNode child : getChildren()){
				child.print(padding+1);
			}
		}
	}
	
	
	public static String pad(String input,int padding){
		StringBuilder rtrn = new StringBuilder();
		for(int i=0; i<padding; i++){
			rtrn.append("  ");
		}
		return rtrn.toString()+input;
	}
}
