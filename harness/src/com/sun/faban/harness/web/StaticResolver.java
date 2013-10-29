package com.sun.faban.harness.web;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.el.ELContext;

import com.sun.faban.harness.common.BenchmarkDescription;
import com.sun.faban.harness.common.Config;
import com.sun.faban.harness.engine.RunQ;

/**
 * This Resolve is a wrapper for all of the existing factory and static classes that are used in the current Faban implementation
 * Supported Classes:
 * 
 * @author willr3
 *
 */
/*
 * This class introduces an implicit variable 'Greeter' into the EL space. 
 * It does variable resolution only, property resolution is left to the built-in 
 * resolvers. For variable resolution, base is always null, property is String.
 * http://www.xinotes.org/notes/note/1570/
 */
public class StaticResolver extends javax.el.ELResolver{
	
	public static enum StaticWrap {BenchmarkDescription}
	
	private static Logger logger = Logger.getLogger(StaticResolver.class.getName());
	
	public StaticResolver(){}

	
	@Override
	public Class<?> getCommonPropertyType(ELContext ctx, Object base) {
		logger.info("getCommonPropertyType("+ctx+" , "+base+")");
		return null;
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext ctx,
			Object base) {
		logger.info("getFeatureDescriptor("+ctx+" , "+base+")");
		return null;
	}

	@Override
	public Class<?> getType(ELContext ctx, Object base, Object property) {
		logger.info("getType("+ctx+" , "+base+" , "+property+")");
		if ((base == null) && property.equals("RunQ")) {
		    ctx.setPropertyResolved(true);
		    return RunQ.class;
		}
		if ((base == null) && property.equals("Config")) {
		    ctx.setPropertyResolved(true);
		    return Config.class;
		}
		
		return null;
	}

	@Override
	public Object getValue(ELContext ctx, Object base, Object property) {
		logger.info("getValue("+ctx+" , "+base+" , "+property+")");
		
		if(base==null){
			if ("RunQ".equals(property)){
				ctx.setPropertyResolved(true);
				return RunQ.getHandle();
			}else if ("Config".equals(property)){
				ctx.setPropertyResolved(true);
				return new Config();
			}else if ("BenchmarkDescription".equals(property)){
				ctx.setPropertyResolved(true);
				return StaticWrap.BenchmarkDescription;
			}
		}else if (base instanceof Config){
			if(property instanceof String){
				String prop = (String)property;
				if("BENCHMARK_DOWNLOAD_PATH".equals(prop)){
					ctx.setPropertyResolved(true);
					return Config.BENCHMARK_DOWNLOAD_PATH;
				}else if ("FABAN_HOST".equals(prop)){
					ctx.setPropertyResolved(true);
					return Config.FABAN_HOST;
				}else if ("SECURITY_ENABLED".equals(prop)){
					ctx.setPropertyResolved(true);
					return Config.SECURITY_ENABLED;
				}else if ("OUT_DIR".equals(prop)){
					ctx.setPropertyResolved(true);
					return Config.OUT_DIR;
				}
			}
		}else if (base instanceof RunQ){
			if(property instanceof String){
				String prop = (String)property;
				if("count".equals(prop)){
					ctx.setPropertyResolved(true);
					String[][] l = RunQ.getHandle().listRunQ();
					return l==null ? 0 : l.length;
				}else if ("listRunQ".equals(prop)){
					ctx.setPropertyResolved(true);
					String[][] l = RunQ.getHandle().listRunQ();
					if (l == null){
						l = new String[][]{};
					}
					return l;
				}
			}
		}else if (base instanceof StaticWrap){
			
			StaticWrap wrapper = (StaticWrap)base;
			logger.info("StaticWrap = "+wrapper+" prop="+property);
			switch(wrapper){
			case BenchmarkDescription:
				if(property instanceof String){
					String prop = (String)property;
					if("bannerName".equals(prop) || "getBannerName".equals(prop) || "getBannerName()".equals(prop)){
						ctx.setPropertyResolved(true);
						return BenchmarkDescription.getBannerName();
					}else if ("bannerVersion".equals(prop) || "getBannerVersion".equals(prop) || "getBannerVersion()".equals(prop) ){
						ctx.setPropertyResolved(true);
						return BenchmarkDescription.getBannerVersion();
					}
				}
			}
		}
		
		
		return null;
	}

	@Override
	public boolean isReadOnly(ELContext ctx, Object base, Object property) {
		return true;
	}

	@Override
	public void setValue(ELContext ctx, Object base, Object property, Object value) {
		ctx.setPropertyResolved(false);
		
	}

}
