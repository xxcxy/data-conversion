package com.customtime.data.conversion.test.handler;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.domain.annotation.InjectMonitor;
import com.customtime.data.conversion.domain.handler.TerminatHandler;
import com.customtime.data.conversion.plugin.annotaion.CallMonitor;
import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.monitor.PluginMonitor;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.util.FileUtil;
import com.customtime.data.conversion.plugin.util.MonitorUtil;


public class WlanHandler implements TerminatHandler {
	private static final Log logger = LogFactory.getLog(WlanHandler.class);
	@TMProperty
	private String dbDriverString;
	@TMProperty
	private String dbConnUrl;
	@TMProperty
	private String dbUserName;
	@TMProperty
	private String dbUserPwd;
	private Connection conn;
	private PreparedStatement ps;
	private ResultSet rs;
	
	@TMProperty
	private String logType;
	@TMProperty(defaultValue="")
	private String dpiSuccessLineType;
	@TMProperty(defaultValue="")
	private String dpiFailureLineType;
	@TMProperty(defaultValue=".txt")
	private String dpiOrgFileSuffix;
	@TMProperty(defaultValue="")
	private String dpiOrgFileDir;
	@TMProperty(defaultValue="")
	private String dpiOrgResultFilePath;
	@TMProperty(defaultValue="0")
	private int dpiRetryTime;//重关联次数
	
	@TMProperty(defaultValue="")
	private String validErrDir;
	@TMProperty(defaultValue="")
	private String trimStr;//针对所有字段在进行validType校验时trim掉的左右字符（只支持单字符），如该字符本身是正则表达式特殊字符，则记得要转义
	
	@TMProperty(defaultValue="-1")
	private int colTotalNum;
	@TMProperty(defaultValue="")
	private String colValidType;
	@TMProperty(defaultValue="")
	private String colValidTypeParam;
	@TMProperty(defaultValue="")
	private String colNullAble;
	
	@TMProperty
	private String orgFilePath;
	@TMProperty
	private String fileSize;
	@TMProperty
	private String fileCollectTime;
	@TMProperty
	private String fileParseTime;
	
	@InjectMonitor
	private PluginMonitor pm;
	
	private PrintWriter pw;
	private Writer writer;
	
	private String validerrFileSuffix = ".validerr";
	private String validerrFileEncoding = "UTF-8";
	
	private boolean isCheck = false;
	private boolean isCheckSuccess = false;
	private String[] colValidTypeArr;
	private String[] colValidTypeParamArr;
	private String[] colNullAbleArr;
	
	private java.util.regex.Pattern trimPattern;
	private java.util.regex.Matcher trimMatcher;
	
	private Map<Integer,SimpleDateFormat> dfmap = new HashMap<Integer,SimpleDateFormat>();
	private java.util.regex.Pattern mobilePatternCheck = java.util.regex.Pattern.compile("\\s*(?:13|15|18)\\d{9}\\s*");
	private java.util.regex.Matcher mobileMatcherCheck;
	
	private java.util.regex.Pattern ipPatternCheck = java.util.regex.Pattern.compile("\\s*((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))\\s*");
	private java.util.regex.Matcher ipMatcherCheck;
	
	//端口是：0-65535
	private java.util.regex.Pattern portPatternCheck = java.util.regex.Pattern.compile("\\s*\\d+\\s*");
	private java.util.regex.Matcher portMatcherCheck;
	
	private java.util.regex.Pattern numPatternCheck = java.util.regex.Pattern.compile("\\s*(?:-)?\\d+\\s*");
	private java.util.regex.Matcher numMatcherCheck;
	
	private java.util.regex.Pattern positiveNumPatternCheck = java.util.regex.Pattern.compile("\\s*\\d+\\s*");
	private java.util.regex.Matcher positiveNumMatcherCheck;
	
	private java.util.regex.Pattern negativeNumPatternCheck = java.util.regex.Pattern.compile("\\s*(?:-)\\d+\\s*");
	private java.util.regex.Matcher negativeNumMatcherCheck;
	
	private String urlRegexp = "\\s*(http|https|www|ftp|)?(://)?(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*((:\\d+)?)(/(\\w+(-\\w+)*))*(\\.?(\\w)*)(\\?)?" +
    "(((\\w*%)*(\\w*\\?)*(\\w*:)*(\\w*\\+)*(\\w*\\.)*(\\w*&)*(\\w*-)*(\\w*=)*(\\w*%)*(\\w*\\?)*" +
    "(\\w*:)*(\\w*\\+)*(\\w*\\.)*" +
    "(\\w*&)*(\\w*-)*(\\w*=)*)*(\\w*)*)\\s*";
	private java.util.regex.Pattern urlPatternCheck = java.util.regex.Pattern.compile(urlRegexp);
	private java.util.regex.Matcher urlMatcherCheck;
	
	private java.util.regex.Pattern rangePatternCheck = java.util.regex.Pattern.compile("\\s*(?:-)?\\d+\\s*");
	private java.util.regex.Matcher rangeMatcherCheck;
	
	@SuppressWarnings("serial")
	private Map<String,String> errTypeMap = new HashMap<String,String>(){
		{
			put("wrongFromCheckColNum","wrongFromCheckColNum");
			put("wrongFromCheckNullAble","wrongFromCheckNullAble");
			put("wrongFromCheckValidType","wrongFromCheckValidType");
		}
	};
	@SuppressWarnings("serial")
	private Map<String,String> successTypeMap = new HashMap<String,String>(){
		{
			put("successFromCheck","successFromCheck");
		}
	};
	public Recode process(Recode rd) {
		if(rd.getBlockNum()<1)
			return rd;
		checkConfig();
		if(!isCheckSuccess){
			throw new RuntimeException("WlanHandler's config wrong!");
		}
		
		if("dpi".equals(logType)&&dpiRetryTime>0){//如果是dpi并且不是第一次关联则不需要做校验，因为这些重关联的文件都是经过第一次校验过的
			return rd;
		}
		
		if(!checkRecode(rd)){
			return rd.newRecode();
		}
		if("dpi".equals(logType)||"radius".equals(logType)||"nat".equals(logType)||"nat-session".equals(logType)||"nat-pba".equals(logType)){
			pm.successLine(logType+"."+successTypeMap.get("successFromCheck"));
		}
		return rd;
	}
	
	private boolean checkRecode(Recode rd){
		boolean flag = true;
		if((flag = checkColTotalNum(rd))){
			for(int i=0,len = rd.getBlockNum();i<len;i++){
				if(!checkNullAble(rd,i)||!checkValidType(rd,i)){
					flag = false;
				}
			}
		}
		
		return flag;
	}
	
	private boolean checkColTotalNum(Recode rd){
		boolean flag = true;
		int len = rd.getBlockNum();
		if(colTotalNum>=0&&colTotalNum!=len){
			pm.failureLine(logType+"."+errTypeMap.get("wrongFromCheckColNum"), rd.getContext());
			flag  = false;
		}
		return flag;
	}
	
	private boolean checkNullAble(Recode rd,int index){
		boolean flag = true;
		String nullAble = "y";
		int nullAbleLength = colNullAbleArr.length;
		if(index<=nullAbleLength){
			nullAble = colNullAbleArr[index];
		}
		if("n".equals(nullAble)){
			String col = rd.getBlock(index);
			String tcol = col==null?"":col.trim();
			if("".equals(tcol)){
				pm.failureLine(logType+"."+errTypeMap.get("wrongFromCheckNullAble"), rd.getContext());
				flag  = false;
			}
		}
		return flag;
	}
	
	private boolean checkValidType(Recode rd,int index){
		boolean flag = true;
		String col = rd.getBlock(index);
		String validType = "";
		String validTypeParam = "";
		int validTypeLength = colValidTypeArr.length;
		if(index<=validTypeLength){
			validType = colValidTypeArr[index];
			validTypeParam = colValidTypeParamArr[index];
		}
		if(!"".equals(validType)&&!"".equals((col==null?(col = ""):(col = col.trim())))){
			if(trimPattern!=null){
				trimMatcher = trimPattern.matcher(col);
				if(trimMatcher.matches()){
					String g1 = trimMatcher.group(1);
					String g2 = trimMatcher.group(2);
					if(g1!=null){
						col = g1;
					}else if(g2!=null){
						col = "";
					}
				}else{
					col = "";
				}
			}
			if("mobile".equals(validType)){
				mobileMatcherCheck = mobilePatternCheck.matcher(col);
				if(!mobileMatcherCheck.matches()){
					flag = false;
				}
			}else if("in".equals(validType)){
				String[] inStrs = validTypeParam.split(",");
				boolean isFind = false;
				for(String inStr:inStrs){
					if(inStr.equals(col)){
						isFind = true;
					}
				}
				if(!isFind){
					flag = false;
				}
			}else if("range".equals(validType)){
				rangeMatcherCheck = rangePatternCheck.matcher(col);
				if(!rangeMatcherCheck.matches()){
					flag = false;
				}else{
					String[] rangeStrs = validTypeParam.split("~");
					long start = Long.valueOf(rangeStrs[0]).longValue();
					long end = Long.valueOf(rangeStrs[1]).longValue();
					
					long colLong = Long.valueOf(col).longValue();
					
					if(start>colLong||colLong>end){
						flag = false;
					}
				}
				
				
			}else if("ip".equals(validType)){
				ipMatcherCheck = ipPatternCheck.matcher(col);
				if(!ipMatcherCheck.matches()){
					flag = false;
				}
			}else if("time".equals(validType)){
				SimpleDateFormat sdf = dfmap.get(Integer.valueOf(index));
				if(sdf==null){
					flag = false;
				}else{
					try{
						sdf.parse(col);
					}catch(Exception e){
						flag = false;
					}
				}
			}else if("port".equals(validType)){
				portMatcherCheck = portPatternCheck.matcher(col);
				if(!portMatcherCheck.matches()){
					flag = false;
				}else{
					int colInt = Integer.valueOf(col).intValue();
					
					if(0>colInt||colInt>65535){
						flag = false;
					}
				}
			}else if("url".equals(validType)){
				urlMatcherCheck = urlPatternCheck.matcher(col);
				if(!urlMatcherCheck.matches()){
					flag = false;
				}
			}else if("num".equals(validType)){
				numMatcherCheck = numPatternCheck.matcher(col);
				if(!numMatcherCheck.matches()){
					flag = false;
				}
			}else if("positivenum".equals(validType)){
				positiveNumMatcherCheck = positiveNumPatternCheck.matcher(col);
				if(!positiveNumMatcherCheck.matches()){
					flag = false;
				}
			}else if("negativenum".equals(validType)){
				negativeNumMatcherCheck = negativeNumPatternCheck.matcher(col);
				if(!negativeNumMatcherCheck.matches()){
					flag = false;
				}
			}else{
				flag = false;
			}
		}
		if(!flag){
			pm.failureLine(logType+"."+errTypeMap.get("wrongFromCheckValidType"), rd.getContext());
		}
		return flag;
	}
	
	private void checkConfig(){
		if(isCheck) return;
		if(logType==null||"".equals(logType = logType.trim().toLowerCase())||(!"radius".equals(logType)&&!"nat".equals(logType)&&!"nat-session".equals(logType)&&!"nat-pba".equals(logType)&&!"dpi".equals(logType))){
			isCheckSuccess = false;
		}else{
			String regexp = "\\|";
			String[] t_colValidTypeArr = "".equals(colValidType = colValidType.trim())?new String[0]:colValidType.split(regexp,-1);
			String[] t_colValidTypeParamArr = "".equals(colValidTypeParam = colValidTypeParam.trim())?new String[0]:colValidTypeParam.split(regexp,-1);
			String[] t_colNullAbleArr = "".equals(colNullAble = colNullAble.trim())?new String[0]:colNullAble.split(regexp,-1);
			int colValidTypeNum = t_colValidTypeArr.length;
			int colValidTypeParamNum = t_colValidTypeParamArr.length;
			int colNullAbleNum = t_colNullAbleArr.length;
			if(colValidTypeNum!=colValidTypeParamNum||colValidTypeNum!=colNullAbleNum||colValidTypeParamNum!=colNullAbleNum){
				isCheckSuccess = false;
			}else{
				if(makeColValidType(t_colValidTypeArr)&&makeColValidTypeParam(t_colValidTypeParamArr)&&makeColNullAble(t_colNullAbleArr)){
					isCheckSuccess = true;
				}else{
					isCheckSuccess = false;
				}
			}
		}
		makeTrimStr();
		
		isCheck = true;
	}
	
	private void makeTrimStr(){
		if(!"".equals(trimStr = trimStr.trim())){
			//(?:[']*)?(.*(?:[^']))(?:[']*)?|([']*) 只支持单字符
			String regexp = "(?:["+trimStr+"]*)?(.*(?:[^"+trimStr+"]))(?:["+trimStr+"]*)?|(["+trimStr+"]*)";
			trimPattern = java.util.regex.Pattern.compile(regexp);
		}
	}
	
	@SuppressWarnings("serial")
	private boolean makeColValidType(String[] t_colValidTypeArr){
		boolean flag = true;
		Map<String,String> prefab = new HashMap<String,String>(){
			{
				put("mobile","");
				put("in","");
				put("range","");
				put("ip","");
				put("time","");
				put("port","");
				put("url","");
				put("num","");
				put("positivenum","");
				put("negativenum","");
				put("","");//不需要校验
				
			}
		};
		for(int i=0,length = t_colValidTypeArr.length;i<length;i++){
			String str = t_colValidTypeArr[i].trim().toLowerCase();
			if(prefab.get(str)==null){
				flag = false;
				break;
			}
			t_colValidTypeArr[i] = str;
		}
		if(flag){
			colValidTypeArr = t_colValidTypeArr;
		}
		return flag;
	}
	
	private boolean makeColValidTypeParam(String[] t_colValidTypeParamArr){
		String inRegexp = "[^,]+(?:,[^,]+)*";//1,2
		java.util.regex.Pattern inPattern = java.util.regex.Pattern.compile(inRegexp);
		java.util.regex.Matcher inMatcher;
		
		String rangeRegexp = "\\s*((?:-)?\\d+)\\s*(~)\\s*((?:-)?\\d+)\\s*";//1~4 只支持数字 可以是负值
		java.util.regex.Pattern rangePattern = java.util.regex.Pattern.compile(rangeRegexp);
		java.util.regex.Matcher rangeMatcher;
		
		boolean flag = true;
		colValidTypeParamArr = new String[colValidTypeArr.length];
		for(int i=0,length = colValidTypeArr.length;i<length;i++){
			String validType = colValidTypeArr[i];
			String validTypeParam = t_colValidTypeParamArr[i].trim();
			if("".equals(validType)){
				if(!"".equals(validTypeParam)){
					flag = false;
					break;
				}
				colValidTypeParamArr[i] = "";
			}else if("mobile".equals(validTypeParam)){
				if(!"".equals(validTypeParam)){
					flag = false;
					break;
				}
				colValidTypeParamArr[i] = "";
			}else if("in".equals(validType)){
				inMatcher = inPattern.matcher(validTypeParam);
				if(!inMatcher.matches()){
					flag = false;
					break;
				}
				String[] inArr = validTypeParam.split(",");
				StringBuffer sb = new StringBuffer();
				for(int j=0,length2 = inArr.length;j<length2;j++){
					sb.append(inArr[j]==null?"":inArr[j].trim());
					if(j!=length2-1){
						sb.append(",");
					}
				}
				colValidTypeParamArr[i] = sb.toString();
			}else if("range".equals(validType)){
				rangeMatcher = rangePattern.matcher(validTypeParam);
				if(!rangeMatcher.matches()){
					flag = false;
					break;
				}
				String g1 = rangeMatcher.group(1);
				String g2 = rangeMatcher.group(2);
				String g3 = rangeMatcher.group(3);
				long g1l = Long.valueOf(g1).longValue();
				long g3l = Long.valueOf(g3).longValue();
				
				if(g1l>g3l){
					long t = g1l;
					g1l = g3l;
					g3l = t;
				}
				colValidTypeParamArr[i] = String.valueOf(g1l) + g2 + String.valueOf(g3l);
			}else if("ip".equals(validType)){
				if(!"".equals(validTypeParam)){
					flag = false;
					break;
				}
				colValidTypeParamArr[i] = "";
			}else if("time".equals(validType)){
				try{
					SimpleDateFormat df = new SimpleDateFormat (validTypeParam);
					dfmap.put(Integer.valueOf(i), df);
				}catch(Exception e){
					flag = false;
					break;
				}
				colValidTypeParamArr[i] = validTypeParam;
			}else if("port".equals(validType)){
				if(!"".equals(validTypeParam)){
					flag = false;
					break;
				}
				colValidTypeParamArr[i] = "";
			}else if("url".equals(validType)){
				if(!"".equals(validTypeParam)){
					flag = false;
					break;
				}
				colValidTypeParamArr[i] = "";
			}else if("num".equals(validType)){
				if(!"".equals(validTypeParam)){
					flag = false;
					break;
				}
				colValidTypeParamArr[i] = "";
			}else if("positivenum".equals(validType)){
				if(!"".equals(validTypeParam)){
					flag = false;
					break;
				}
				colValidTypeParamArr[i] = "";
			}else if("negativenum".equals(validType)){
				if(!"".equals(validTypeParam)){
					flag = false;
					break;
				}
				colValidTypeParamArr[i] = "";
			}else{
				flag = false;
				break;
			}
			
		}
		return flag;
	}
	
	private boolean makeColNullAble(String[] t_colNullAbleArr){//如实际列数大于配置数，则默认可空，如实际列数小于配置数，则忽略超过部分的配置
		boolean flag = true;
		colNullAbleArr = new String[t_colNullAbleArr.length];
		for(int i=0,length = t_colNullAbleArr.length;i<length;i++){
			String nullAble = t_colNullAbleArr[i].trim().toLowerCase();
			if("n".equals(nullAble)||"y".equals(nullAble)){
				colNullAbleArr[i] = nullAble;
			}else{
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	@CallMonitor
	public void monitor(MonitorUtil mu) {
		//TODO GD_WLAN_DATACOLLECT_LOG表中增加一个字段表示文件处理状态STATUS字段 ok:完成（nat raduis直接沉淀ok，dpi只有在真正关联完毕包括重关联时才沉淀ok，否则沉淀次数1,2,3，当未达到ok时SUCCESS_RATE【得扩字段长度】增量沉淀为关联成功的条数，当达到ok后变为百分形式 ）
		//TODO 相应的查询逻辑也需要修改，需要过滤STATUS不为ok的数据
		String status;
		long succesNum = 0l;
		long errorNum = 0l;
		long totalNum = 0l;
		long dpiSuccessLineNum = 0l;
		long dpiFailureLineNum = 0l;
		dpiSuccessLineType = dpiSuccessLineType.trim();//本次成功关联，针对dpi
		//dpiFailureLineType = dpiFailureLineType.trim();
		String[] dpiFailureLineTypeArr = (dpiFailureLineType==null||"".equals(dpiFailureLineType = dpiFailureLineType.trim()))?new String[0]:dpiFailureLineType.split(",");//本次未成功关联，针对dpi
		for(int i=0,len = dpiFailureLineTypeArr.length;i<len;i++){
			dpiFailureLineTypeArr[i] = dpiFailureLineTypeArr[i].trim();
		}
		orgFilePath = orgFilePath.trim().replace(File.separator+File.separator, File.separator);
		String successRate = null;
		String orgFileName = orgFilePath.substring(orgFilePath.lastIndexOf(File.separator)+1);
		String orgFileDir = orgFilePath.substring(0,orgFilePath.lastIndexOf(File.separator)+1);
		String orgFileNameWithoutSuffix = orgFileName.substring(0,orgFileName.lastIndexOf("."));
		if(dpiOrgFileDir != null && !dpiOrgFileDir.endsWith(File.separator)){
			dpiOrgFileDir = dpiOrgFileDir+File.separator;
		}
		dpiOrgFileDir = dpiOrgFileDir.trim().replace(File.separator+File.separator, File.separator);
		if(dpiOrgFileSuffix != null && !dpiOrgFileSuffix.startsWith(".")){
			dpiOrgFileSuffix = "."+dpiOrgFileSuffix;
		}
		
		if(dpiOrgResultFilePath != null && !"".equals(dpiOrgResultFilePath.trim())){
			dpiOrgResultFilePath = dpiOrgResultFilePath.trim().replace(File.separator+File.separator, File.separator);
		}
		
		if(validErrDir == null || "".equals(validErrDir = validErrDir.trim())){
			validErrDir = orgFileDir + File.separator + "validerr" + File.separator;
		}
		if(validErrDir != null && !validErrDir.endsWith(File.separator)){
			validErrDir = validErrDir+File.separator;
		}
		validErrDir = validErrDir.trim().replace(File.separator+File.separator, File.separator);
		
		String validErrFilePath = validErrDir + orgFileNameWithoutSuffix + validerrFileSuffix;
		String dpiOrgFilePath = (dpiOrgFileDir==null?"":dpiOrgFileDir) + orgFileNameWithoutSuffix + (dpiOrgFileSuffix==null?"":dpiOrgFileSuffix);
		try{
			String key;
	        List<String> valueLi;
	        Long value;
			Iterator<Entry<String, Long>> iter1 = mu.getAllSuccessLine().entrySet().iterator();
	        while (iter1.hasNext()) {
                Map.Entry<String, Long> entry1 = (Map.Entry<String, Long>) iter1.next();
                key = entry1.getKey();
                value = entry1.getValue();
                //针对dpi关联成功，来源于raduis
                if("dpi".equals(logType)){
                	if((key.trim()).equals(dpiSuccessLineType)&&value!=null){
                		dpiSuccessLineNum += value.longValue();
                	}
                }
                //只统计校验成功的条数
                if(key.length()>(logType+".").length()){
                	String tkey = key.substring((logType+".").length());
                    Iterator<Entry<String, String>> iter4 = successTypeMap.entrySet().iterator();
                	boolean isFind = false;
                	while(iter4.hasNext()&&!isFind){
                		Map.Entry<String, String> entry4 = (Map.Entry<String, String>) iter4.next();
//                        String key4 = entry4.getKey();
                        String value4 = entry4.getValue();
                        if(value4.equals(tkey)){
                        	isFind = true;
                        }
                	}
                	if(isFind){
                        //System.out.println("key:"+key+";value:"+value);
                        if(value!=null){
                        	succesNum += value.longValue();
                        }
                	}
                    
                }
	        }
			
			//System.out.println("=======getAllFailines:");
			Iterator<Entry<String, List<String>>> iter2 = mu.getAllFailines().entrySet().iterator();
			while (iter2.hasNext()) {
                Map.Entry<String, List<String>> entry2 = (Map.Entry<String, List<String>>) iter2.next();
                key = entry2.getKey();
                valueLi = entry2.getValue();
                //针对dpi未关联成功，来源于raduis和nat
                if("dpi".equals(logType)){
                	for(String k:dpiFailureLineTypeArr){
                		if((key.trim()).equals(k)&&valueLi!=null){
                			dpiFailureLineNum += valueLi.size();
                		}
                	}
                }
                //只统计校验失败的条数并输出到错误日志文件中
                if(key.length()>(logType+".").length()){
                	String tkey = key.substring((logType+".").length());
                	Iterator<Entry<String, String>> iter3 = errTypeMap.entrySet().iterator();
                	boolean isFind = false;
                	while(iter3.hasNext()&&!isFind){
                		Map.Entry<String, String> entry3 = (Map.Entry<String, String>) iter3.next();
//                        String key3 = entry3.getKey();
                        String value3 = entry3.getValue();
                        if(value3.equals(tkey)){
                        	isFind = true;
                        }
                	}
                	if(isFind){
                         //System.out.println("key:"+key+";value:"+valueLi.toString());
                         if(valueLi!=null){
                         	errorNum += valueLi.size();
                         	for(String rdcontext:valueLi){
                         		if(pw==null){
                         			FileUtil.createDir(validErrDir);
                        	        writer = new OutputStreamWriter(new FileOutputStream(validErrFilePath,true), validerrFileEncoding);
                        	        pw = new PrintWriter(writer);
                         		}
                         		pw.println(rdcontext);
                         	}
                         }
                	}
                   
                }
                
	        }
			if(pw!=null){
				pw.flush();
				pw.close();
			}
			if(writer!=null){
				writer.close();
			}
			
			
			totalNum = succesNum+errorNum;
			
			Driver d = (Driver)Class.forName(dbDriverString,true,Thread.currentThread().getContextClassLoader()).newInstance();
			DriverManager.registerDriver(new DriverShim(d));
			conn = DriverManager.getConnection(dbConnUrl,dbUserName,dbUserPwd);
			String insertSql = "INSERT INTO GD_WLAN_DATACOLLECT_LOG(FILENAME,CREATETIME,DEALTIME,SIZE,LINECOUNT,ERRLINECOUNT,SUCCESS_RATE,STATUS)VALUES(?,?,?,?,?,?,?,?)";
			if("radius".equals(logType)||"nat".equals(logType)||"nat-session".equals(logType)||"nat-pba".equals(logType)){
				//System.out.println("=======orgFilePath:"+orgFilePath);
				//System.out.println("=======fileSize:"+fileSize);
				//System.out.println("=======fileCollectTime:"+fileCollectTime);
				//System.out.println("=======fileParseTime:"+fileParseTime);
				//long succesNum = mu.getSuccessLine();
				//long errorNum = mu.getFailines().size();
				//System.out.println("=======getSuccessLine:"+mu.getSuccessLine());
				//System.out.println("=======getFailines:"+mu.getFailines());
				//System.out.println("=======getAllSuccessLine:");
				//System.out.println("=======succesNum:"+succesNum);
				//System.out.println("=======errorNum:"+errorNum);
				//System.out.println("=======totalNum:"+totalNum);
				successRate = null;
				status  = "ok";
				ps = conn.prepareStatement(insertSql); 
				String[] insertValues = new String[]{orgFilePath,fileCollectTime,fileParseTime,fileSize,String.valueOf(totalNum),String.valueOf(errorNum),successRate,status};
				for(int i=0,length = insertValues.length;i<length;i++){
					ps.setString(i+1, insertValues[i]);
				}
			}else if("dpi".equals(logType)){
				//TODO 根据dpiOrgFilePath判断是插入还是更新，更新只更新文件处理状态和SUCCESS_RATE（增量dpiSuccessLineNum）
				//TODO 如果dpiFailureLineNum为0（说明刚好全部关联到）
				//TODO 如果上一次文件处理状态为3（即重关联次数dpiRetryTime为3），说明本次已经是第4次关联了，即重关联的第3次，无论如何都任务处理结束
				dpiOrgFilePath = dpiOrgFilePath.trim();
				String isExistsSql = "SELECT FILENAME,CREATETIME,DEALTIME,SIZE,LINECOUNT,ERRLINECOUNT,SUCCESS_RATE,STATUS FROM GD_WLAN_DATACOLLECT_LOG WHERE FILENAME = ? AND STATUS !='ok'";
				ps = conn.prepareStatement(isExistsSql);
				ps.setString(1, dpiOrgFilePath);
				ps.execute();
				ResultSet rs = ps.getResultSet();
				ResultSetMetaData md = rs.getMetaData();
				int columnCount = md.getColumnCount();
				boolean haNext = rs.next();
				if(haNext&&dpiRetryTime>0){
					Map<Object,Object> map = new HashMap<Object,Object>(); 
				    for (int i = 1; i <= columnCount; i++) { 
				    	map.put(md.getColumnName(i), rs.getObject(i)); 
				    }
				    //String lastStatus = String.valueOf(map.get("STATUS")==null?"1":map.get("STATUS")).trim();
				    String lastSuccessRate = String.valueOf(map.get("SUCCESS_RATE")==null?"0":map.get("SUCCESS_RATE")).trim();
				    String lineCount = String.valueOf(map.get("LINECOUNT")==null?"0":map.get("LINECOUNT")).trim();
				    String updateSql = "UPDATE GD_WLAN_DATACOLLECT_LOG SET SUCCESS_RATE = ?,STATUS = ? WHERE FILENAME = ? AND STATUS !='ok'";
				    if(dpiFailureLineNum == 0l || dpiRetryTime == 3){//全部关联成功 或者 已经是第3次重关联了
				    	long lastDpiSuccessLineNum = Long.valueOf(lastSuccessRate).longValue();
				    	dpiSuccessLineNum += lastDpiSuccessLineNum;
				    	totalNum = Long.valueOf(lineCount).longValue();
				    	long newb = Math.round(dpiSuccessLineNum*100l);
				    	double db = newb/(totalNum*1.0);
				    	DecimalFormat df = new DecimalFormat("#.00");
				    	successRate = df.format(db)+"%";
				    	/*if("100.00%".equals(successRate)){
				    		successRate = "100%";
				    	}*/
				    	if(".00%".equals(successRate)){
				    		successRate = "0.00%";
				    	}
				    	status = "ok";
				    }else{
				    	long lastDpiSuccessLineNum = Long.valueOf(lastSuccessRate).longValue();
				    	dpiSuccessLineNum += lastDpiSuccessLineNum;
				    	successRate = String.valueOf(dpiSuccessLineNum);
				    	status = String.valueOf(dpiRetryTime + 1);
				    }
				    ps = conn.prepareStatement(updateSql);
			    	ps.setString(1, successRate);
			    	ps.setString(2, status);
			    	ps.setString(3, dpiOrgFilePath);
				}else if(!haNext&&dpiRetryTime <= 0){//第一次关联（非重关联）
					if(dpiFailureLineNum == 0l){//全部关联成功
				    	long newb = Math.round(dpiSuccessLineNum*100l);
				    	double db = newb/(totalNum*1.0);
				    	DecimalFormat df = new DecimalFormat("#.00");
				    	successRate = df.format(db)+"%";
				    	/*if("100.00%".equals(successRate)){
				    		successRate = "100%";
				    	}*/
				    	if(".00%".equals(successRate)){
				    		successRate = "0.00%";
				    	}
				    	status = "ok";
					}else{
						successRate = String.valueOf(dpiSuccessLineNum);
						status = String.valueOf(dpiRetryTime + 1);
					}
					ps = conn.prepareStatement(insertSql); 
					String[] insertValues = new String[]{orgFilePath,fileCollectTime,fileParseTime,fileSize,String.valueOf(totalNum),String.valueOf(errorNum),successRate,status};
					for(int i=0,length = insertValues.length;i<length;i++){
						ps.setString(i+1, insertValues[i]);
					}
				}else if(haNext&&dpiRetryTime <= 0){
					throw new Exception("数据异常，第一次关联时GD_WLAN_DATACOLLECT_LOG表中已存在文件路径为"+dpiOrgFilePath+"的记录！");
				}else{
					throw new Exception("数据异常，第"+dpiRetryTime+"次重关联时GD_WLAN_DATACOLLECT_LOG表中不存在文件路径为"+dpiOrgFilePath+"的记录！");
				}
				rs.close();
				if("ok".equals(status)){
					String tdpiOrgResultFilePath = dpiOrgResultFilePath+".ok";
					boolean createSuccess = false;
					int createTimes = 5;
					while(createTimes-->0&&!createSuccess){
						try{
							FileUtil.createFile(tdpiOrgResultFilePath);
							createSuccess = true;
						}catch(Exception e){
							createSuccess = false;
							logger.info("create "+ tdpiOrgResultFilePath+" file failed! retrying....");
						}
					}
				}
			}
			ps.execute();
			ps.close();
			conn.close();
			
			boolean hasReadSuccess = false;
			int hasReadTimes = 5;
			while(hasReadTimes-->0&&!hasReadSuccess){
				try{
					File f1 = new File(orgFilePath);
					File f2 = new File(orgFilePath+".hasread");
					hasReadSuccess = f1.renameTo(f2);
				}catch(Exception e){
					hasReadSuccess = false;
					logger.info("the file orgFilePath rename to "+ orgFilePath+".hasread"+" failed! retrying....");
				}
			}
			
		}catch(Exception sqle){
			if(rs!=null){
				try{
					rs.close();
				}catch(SQLException e){
					logger.error(e.getStackTrace());
					e.printStackTrace();
				}
			}
			if(ps!=null){
				try{
					//if(!ps.isClosed())
						ps.close();
				}catch(SQLException e){
					logger.error(e.getStackTrace());
					e.printStackTrace();
				}
			}
			if(conn!=null){
				try{
					if(!conn.isClosed())
						conn.close();
				}catch(SQLException e){
					logger.error(e.getStackTrace());
					e.printStackTrace();
				}
			}
			try{
				if(pw!=null){
					pw.flush();
					pw.close();
				}
				if(writer!=null){
					writer.close();
				}
			}catch(Exception e){
				logger.error(e.getStackTrace());
				e.printStackTrace();
			}
			logger.error(sqle.getStackTrace());
			sqle.printStackTrace();
		}finally{
			if(rs!=null){
				try{
					rs.close();
				}catch(SQLException sqle){
					
				}
			}
			if(ps!=null){
				try{
					//if(!ps.isClosed())
						ps.close();
				}catch(SQLException sqle){
					
				}
			}
			if(conn!=null){
				try{
					if(!conn.isClosed())
						conn.close();
				}catch(SQLException sqle){
					
				}
			}
			try{
				if(pw!=null){
					pw.flush();
					pw.close();
				}
				if(writer!=null){
					writer.close();
				}
			}catch(Exception e){
				
			}
		}
		
	}
	
	public static void main(String args[]){
		String str  = "D:\\\\iptrace\\\\data\\\\dpi\\AHTTPP00D121127144625E000.txt";
		String str2 = str.replace(File.separator+File.separator, File.separator);
		System.out.println(str2);
	}
	
	class DriverShim implements Driver {
		private Driver driver;
		DriverShim(Driver d) {
			this.driver = d;
		}
		public boolean acceptsURL(String u) throws SQLException {
			return this.driver.acceptsURL(u);
		}
		public Connection connect(String u, Properties p) throws SQLException {
			return this.driver.connect(u, p);
		}
		public int getMajorVersion() {
			return this.driver.getMajorVersion();
		}
		public int getMinorVersion() {
			return this.driver.getMinorVersion();
		}
		public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
			return this.driver.getPropertyInfo(u, p);
		}
		public boolean jdbcCompliant() {
			return this.driver.jdbcCompliant();
		}
	}
}
