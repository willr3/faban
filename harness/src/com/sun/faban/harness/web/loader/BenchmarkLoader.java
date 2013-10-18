package com.sun.faban.harness.web.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sun.faban.harness.util.DeployUtil;
import com.sun.faban.harness.web.pojo.Benchmark;

public class BenchmarkLoader {
	private static Logger logger = Logger.getLogger(BenchmarkLoader.class.getName());
	
	public Benchmark getBenchmark(String shortName){
		Benchmark bench = new Benchmark();
		
		File benchDir = new File(DeployUtil.BENCHMARKDIR,shortName);
		
		if(!benchDir.isDirectory()){
			//TODO Deploy benchmark jar if it exists or do we assume it would already be deployed?
		}else{
			logger.finest("Found benchmark directory="+benchDir.getName());
			bench.setShortName(benchDir.getName());
			/**@see BenchmarkDescription#readDescription **/
			String metaInf = benchDir.getAbsolutePath() + File.separator + "META-INF";
			File metaInfDir = new File(metaInf);
			if(metaInfDir.isDirectory()){
				try {
					DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					XPath xPath= XPathFactory.newInstance().newXPath();
					Node root = null;
					
					File benchmarkXml = new File(metaInfDir,"benchmark.xml");
					if(benchmarkXml.exists()){
						root = parser.parse(benchmarkXml).getDocumentElement();
						
						bench.setBenchmarkClass(xPath.evaluate("benchmark-class",root));
						bench.setConfigFileName(xPath.evaluate("config-file-name",root));
						bench.setConfigStylesheet(xPath.evaluate("config-stylesheet", root));
						bench.setBannerPage(xPath.evaluate("banner-page", root));
						bench.setName(xPath.evaluate("name", root));
						bench.setVersion(xPath.evaluate("version", root));
						bench.setConfigForm(xPath.evaluate("config-form", root));
						bench.setResultFilePath(xPath.evaluate("result-file-path", root));
						bench.setMetric(xPath.evaluate("metric", root));
						bench.setScaleName(xPath.evaluate("scaleName", root));
						bench.setScaleUnit(xPath.evaluate("scaleUnit", root));
					}else{
						logger.warning("benchmark "+benchDir.getName()+" does not have a benchmark.xml");
					}
					
					/**@see BenchmarkDescription#readFabanDescription(BenchmarkDescription,File,DocumentBuilder)**/
					File fabanXml = new File(metaInfDir,"faban.xml");
					if(fabanXml.exists()){
						root = parser.parse(fabanXml).getDocumentElement();
						bench.setName(xPath.evaluate("name",root));
						bench.setVersion(xPath.evaluate("version",root));
						bench.setMetric(xPath.evaluate("metric",root));
						bench.setScaleName(xPath.evaluate("scaleName",root));
						bench.setScaleUnit(xPath.evaluate("scaleUnit",root));
						if(bench.getBenchmarkClass()==null || bench.getBenchmarkClass().isEmpty()){
							bench.setBenchmarkClass("com.sun.faban.harness.DefaultFabanBenchmark2");
						}

						
					}else{
						logger.warning("benchmark "+benchDir.getName()+" does not have a faban.xml");
					}
					
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				logger.warning("benchmark "+benchDir.getName()+" does not have a META-INF directory");
			}
			
		}
		
		return bench;
	}
	public List<Benchmark> getBenchmarks(){
		List<Benchmark> rtrn = new ArrayList<Benchmark>();
		
		//TODO check deploy and run DeployUtil.checkDeploy()
		File[] benchmarkDirs = DeployUtil.BENCHMARKDIR.listFiles();
		for(int i=0; i<benchmarkDirs.length; i++){
			File benchDir = benchmarkDirs[i];
			
			if(!benchDir.isDirectory()){
				//TODO Deploy benchmark jar if not already deployed
			}else{
				Benchmark toAdd = getBenchmark(benchDir.getName());
				if(toAdd!=null){
					rtrn.add(toAdd);
				}
			}
		}
		return rtrn;
	}
}
