package com.customtime.data.conversion.domain.handler;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.domain.annotation.InjectMonitor;
import com.customtime.data.conversion.domain.context.ResourceContext;
import com.customtime.data.conversion.plugin.annotaion.CallMonitor;
import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.util.MonitorUtil;

public class OnlineRecodeHandler implements TerminatHandler {

	private static final Log logger = LogFactory
			.getLog(OnlineRecodeHandler.class);
	@TMProperty
	private String cacheName;
	@TMProperty
	private String queryString;
	@TMProperty
	private String orderbyString;
	@TMProperty
	private String queryParamCol;
	@TMProperty
	private String rdCol;
	@TMProperty
	private String errFile;
	@TMProperty(defaultValue=",")
	private String errSplit;
	@InjectMonitor
	private PluginMonitor pm;
	private SimpleDateFormat sf1;
	private SimpleDateFormat sf2;

	public Recode process(Recode rd) {
		if(rd.getBlockNum()<1)
			return rd;
		String[] qus = queryParamCol.split(",");
		String lastQuery = "";
		if (qus.length == 1) {
			Object p = getField(rd, qus[0], 0);
			lastQuery = String.format(queryString, p);
		} else if (qus.length == 2) {
			Object p = getField(rd, qus[0], 0);
			Object p2 = getField(rd, qus[1], 1);
			lastQuery = String.format(queryString, p, p2);
		} else if (qus.length == 3) {
			Object p = getField(rd, qus[0], 0);
			Object p2 = getField(rd, qus[1], 1);
			Object p3 = getField(rd, qus[2], 2);
			lastQuery = String.format(queryString, p, p2, p3);
		}
		logger.debug(lastQuery);
		Recode rediusRecode = ResourceContext.getPool().getRecode(cacheName,
				lastQuery, Recode.class, orderbyString);
		if (rediusRecode != null) {
			String[] rdCola = rdCol.split(",");
			for (String num : rdCola) {
				rd.putBlock(rediusRecode.getBlock(Integer.parseInt(num)));
			}
			pm.successLint();
			return rd;
		} else {
			pm.failureLine(rd.getContext(errSplit));
			return rd.newRecode();
		}

	}

	private String getField(Recode recode, String bol, int stat) {
		String[] args = bol.split(";");
		String rstr = "";
		if (stat == 0) {
			StringBuffer sb = new StringBuffer(recode.getBlock(Integer.parseInt(args[0])));
			for (int i=1,len=args.length;i<len;i++) {
				sb.append(",").append(recode.getBlock(Integer.parseInt(args[i])));
			}
			rstr = sb.toString();
		} else if (stat == 1 || stat == 2) {
			if (args.length == 1) {
				rstr = recode.getBlock(Integer.parseInt(args[0]));
			} else if (args.length >= 2) {
				if (stat == 1 && sf1 == null)
					sf1 = new SimpleDateFormat(args[1]);
				if (stat == 2 && sf2 == null)
					sf2 = new SimpleDateFormat(args[1]);
				SimpleDateFormat sf = stat == 1 ? sf1 : sf2;
				try {
					rstr = sf.parse(recode.getBlock(Integer.parseInt(args[0])))
							.getTime() + "l";
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return rstr;
	}

	@CallMonitor
	public void monitor(MonitorUtil mu) {
		PrintWriter pw=null;
		try {
			pw = new PrintWriter(new FileWriter(errFile,true));
			for (String line : mu.getFailines()) {
				pw.println(line);
			}
		} catch (FileNotFoundException e) {
			logger.error("the errfile "+errFile+" not find!");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			pw.close();
		}
	}
}
