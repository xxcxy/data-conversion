package com.customtime.data.conversion.plugin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileUtil {
	private final static Log logger = LogFactory.getLog(FileUtil.class);

	
	/**
	 * 创建文件及其所在的目录（文件已存在返回false，创建成功返回true，其它抛异常）
	 * @param destFileName
	 * @return
	 * @throws Exception
	 */
	public static boolean createFile(String destFileName) throws IOException {

		File file = new File(destFileName);
		
		if (destFileName.endsWith(File.separator)) {
			logger.error("创建单个文件" + destFileName + "失败，目标不能是目录！");
			throw new IOException("创建单个文件" + destFileName + "失败，目标不能是目录！");
		}
		
		if (file.exists()) {
			logger.info("创建单个文件" + destFileName + "失败，目标文件已存在！");
			return false;
		}
		
		if (!file.getParentFile().exists()) {
			logger.info("目标文件所在路径不存在，准备创建。。。");
			if (!file.getParentFile().mkdirs()) {
				logger.error("创建目录文件所在的目录失败！");
				throw new IOException("创建目录文件所在的目录失败！");
			}
		}
		
		if (file.createNewFile()) {
			logger.info("创建单个文件" + destFileName + "成功！");
			return true;
		} else {
			logger.error("创建单个文件" + destFileName + "失败！");
			throw new IOException("创建单个文件" + destFileName + "失败！");
		}
		
	}

	/**
	 * 创建目录（目录已存在返回false，创建成功返回true，其它抛异常）
	 * @param destDirName
	 * @return
	 * @throws IOException
	 */
	public static boolean createDir(String destDirName) throws IOException {
		if(destDirName==null||"".equals(destDirName.trim())){
			logger.error("创建目录失败，目标目录不能为空！");
			throw new IOException("创建目录失败，目标目录不能为空！");
		}
		
		if (!destDirName.endsWith(File.separator))
			destDirName = destDirName + File.separator;
		
		File dir = new File(destDirName);
		
		if (dir.exists()) {
			logger.info("创建目录" + destDirName + "失败，目标目录已存在！");
			return false;
		}
		// 创建单个目录
		if (dir.mkdirs()) {
			logger.info("创建目录" + destDirName + "成功！");
			return true;
		} else {
			logger.info("创建目录" + destDirName + "失败！");
			throw new IOException("创建目录" + destDirName + "失败！");
		}
	}

	/**
	 * 创建临时文件
	 * @param prefix
	 * @param suffix
	 * @param dirName
	 * @return
	 * @throws IOException 
	 */
	public static String createTempFile(String prefix, String suffix,
			String dirName) throws IOException {
		File tempFile = null;
		if (dirName == null) {
			// 在默认文件夹下创建临时文件
			tempFile = File.createTempFile(prefix, suffix);
			return tempFile.getCanonicalPath();
		} else {
			File dir = new File(dirName);
			// 如果临时文件所在目录不存在，首先创建
			if (!dir.exists()) {
				if (!FileUtil.createDir(dirName)) {
					logger.error("创建临时文件失败，不能创建临时文件所在目录！");
					throw new IOException("创建临时文件失败，不能创建临时文件所在目录！");
				}
			}
			tempFile = File.createTempFile(prefix, suffix, dir);
			return tempFile.getCanonicalPath();
		}

	}
	
	/**
	 * 生成带递增和时间戳的唯一文件及其状态.ing文件并返回该文件的绝对路径
	 * @param fileNameFormat 生成的文件名的格式化字符串（$number$和$time$最多只允许出现一次，当存在$time$时yyyyMMddHHmmss必须出现且是连续的字符串且中间不能有别的字符，除它们外如果含有正则元字符则需要自行先转义）,格式如：LUU-SD$number,10$-$time,yyyyMMddHHmmss$\\.xml
	 * @param fileDirectory 文件所在的绝对目录
	 * @param autoCreateDir 是否自动创建目录
	 * @return
	 * @throws Exception 
	 */
	public static String getFilePathUni(String fileNameFormat,String fileDirectory,boolean autoCreateDir) throws Exception {
		long waitTime = 5000;
		int maxDeleteTimes = 5;
		int maxCreateTimes = 5;
		int maxCreateStatusTimes = 5;
		if (fileDirectory!=null&&!"".equals((fileDirectory = fileDirectory.trim()))&&!fileDirectory.endsWith(File.separator))
			fileDirectory = fileDirectory + File.separator;
		
		File dir = new File(fileDirectory);
		if(autoCreateDir){
			createDir(fileDirectory);
		}else if (!dir.exists()) {
			throw new IOException("the directory does not exist");
		}
		if(fileNameFormat==null||"".equals(fileNameFormat = fileNameFormat.trim())){
			throw new NullPointerException("the fileNameFormat parameter cannot be empty!");
		}
		String fileType = fileNameFormat.substring(fileNameFormat.lastIndexOf("."));
		String fileNameNoType = fileNameFormat.substring(0,fileNameFormat.lastIndexOf("."));
		String findFileType = fileType;
		String createStatusFileType = ".ing";
		String uniCheckFileName = EncryptUtil.MD5(fileDirectory+fileNameNoType) + ".unicheck";
		String uniCheckFilePath = fileDirectory + uniCheckFileName;
		File uniCheckFile = new File(uniCheckFilePath);
		if(!uniCheckFile.exists()){
			if(!createFile(uniCheckFilePath)){
				Thread.sleep(waitTime);
				return getFilePathUni(fileNameFormat,fileDirectory,autoCreateDir);
			}
			String newFilePath = null;
			try{
				java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("[0-9]*");
				String regexp = "(?:\\$([^\\$,\\(\\)]+)(?:,([^\\$,\\(\\)]+))?\\$)";
				java.util.regex.Pattern utilPattern = java.util.regex.Pattern.compile(regexp);//不允许出现$ ( ) 且,只允许出现1次，否则会匹配不到
				java.util.regex.Matcher utilMatcher = utilPattern.matcher(fileNameNoType+findFileType);
				boolean hasNumInc = false;
				boolean hasTime = false;
				int matchedNumIncIndex = -1;
				int matchedTimeIndex = -1;
				int matched = -1;
				int numLength = -1;
				String timeFmt = null;
				String reg = null;
				StringBuffer newStr = new StringBuffer();
				utilMatcher.reset();
				while(utilMatcher.find()){
					String g1 = utilMatcher.group(1).trim().toLowerCase();
					String g2 = utilMatcher.group(2);
					if(g2!=null){
						g2 = g2.trim();
					}
					if("number".equals(g1)){
						if(hasNumInc){
							throw new Exception("number format repeated!");
						}
						if(g2!=null&&!"".equals(g2)){
							if(!numPattern.matcher(g2).matches()){
								throw new Exception("number length must be a numeric!");
							}
							numLength = Integer.valueOf(g2).intValue();
							if(numLength <= 0){
								//hasNumInc = false;
								numLength = -1;
							}
						}
						hasNumInc = true;
						matched++;
						matchedNumIncIndex = matched;
					}else if("time".equals(g1)){
						if(hasTime){
							throw new Exception("time format repeated!");
						}
						if(g2!=null&&!"".equals(g2)){
							timeFmt = g2;
						}else{
							hasTime = false;
							timeFmt = null;
						}
						hasTime = true;
						matched++;
						matchedTimeIndex = matched;
					}
					
					if(hasNumInc){
						if(numLength==-1){
							reg = "(\\\\d+)";
						}else{
							reg = "(\\\\d{"+numLength+"})";
						}
					}
					if(hasTime){
						SimpleDateFormat t_sf = new SimpleDateFormat(timeFmt);
						Date t_dt = new Date();
						reg = "(\\\\d{"+t_sf.format(t_dt).length()+"})";
					}
					if(reg!=null){
						utilMatcher.appendReplacement(newStr, reg);
					}
					
				}
				String newNum = "";
				String nowTime = "";
				if(reg!=null&&(hasNumInc||hasTime)){
					utilMatcher.appendTail(newStr);
					reg = newStr.toString();
					
					java.util.regex.Pattern findFilePattern = java.util.regex.Pattern.compile(reg);
					java.util.regex.Matcher findFileMatcher = null;
					
					if(hasNumInc){
						File[] files = dir.listFiles();
						String fileName;
						String nowFileType;
						String numStr = null;
						long maxNum=0;
						for(File file:files){
							fileName = file.getName();
							nowFileType = fileName.substring(fileName.lastIndexOf("."));
							if(!nowFileType.equals(findFileType)){
								continue;
							}
							findFileMatcher = findFilePattern.matcher(fileName);
							if(findFileMatcher.matches()){
								numStr = findFileMatcher.group(matchedNumIncIndex+1);
								if(!numPattern.matcher(numStr).matches()){
									throw new Exception("group("+matchedNumIncIndex+1+") of regex matche is not a numeric!");
								}
								int thisNumLength = numStr.length();
								if(numLength!=-1&&numLength!=thisNumLength){
									throw new Exception("file name not specification!");
								}
								long thisNum = Long.valueOf(numStr).longValue();
								if(thisNum>maxNum){
									maxNum = thisNum;
								}
							}
						}
						maxNum++;
						if(String.valueOf(maxNum).length()>numLength){
							maxNum = 1;
						}
						if(numLength!=-1){
							newNum = fillWord(String.valueOf(maxNum),numLength,"0");
						}else{
							newNum = String.valueOf(maxNum);
						}
					}
					if(hasTime){
						SimpleDateFormat sf = new SimpleDateFormat(timeFmt);
						Date dt = new Date();
						nowTime = sf.format(dt);
					}
				}
				
				String newFileName = null;
				
				if(!hasNumInc&&!hasTime){
					newFileName = fileNameFormat;
				}else{
					java.util.regex.Pattern replacePattern = java.util.regex.Pattern.compile(regexp);//不允许出现$ ( ) 且,只允许出现1次，否则会匹配不到
					fileNameFormat = fileNameFormat.replaceAll("\\\\|/", "");//将原文件名格式中的目录分隔符去掉
					java.util.regex.Matcher replaceMatcher = replacePattern.matcher(fileNameFormat);
					StringBuffer tStr = new StringBuffer();
					int i=0;
					while(replaceMatcher.find()){
						if(matchedNumIncIndex == i){
							replaceMatcher.appendReplacement(tStr, newNum);
						}else if(matchedTimeIndex == i){
							replaceMatcher.appendReplacement(tStr, nowTime);
						}
						i++;
					}
					replaceMatcher.appendTail(tStr);
					newFileName = tStr.toString();
				}
				
				newFilePath = fileDirectory+newFileName;
				String statusFilePath = newFilePath.substring(0,newFilePath.lastIndexOf(".")) + createStatusFileType;
				
				boolean flag = false;
				boolean statusFlag = false;
				
				while(!(statusFlag = createFile(statusFilePath))&&--maxCreateStatusTimes>0){
					Thread.sleep(waitTime);
				}
				
				if(statusFlag){
					while(!(flag = createFile(newFilePath))&&--maxCreateTimes>0){
						Thread.sleep(waitTime);
					}
				}
				
				if(!statusFlag){
					throw new Exception("the file "+statusFilePath+" create faild,check the file whether or not exist!");
				}else if(!flag){
					throw new Exception("the file "+newFilePath+" create faild,check the file whether or not exist!");
				}
				
				while(!uniCheckFile.delete()&&--maxDeleteTimes>0){
					Thread.sleep(waitTime);
				}
				
				
			}catch(Exception e){
				while(!uniCheckFile.delete()&&--maxDeleteTimes>0){
					Thread.sleep(waitTime);
				}
				throw new Exception(e.getMessage());
			}
			return newFilePath;
		}else{
			Thread.sleep(waitTime);
			return getFilePathUni(fileNameFormat,fileDirectory,autoCreateDir);
		}
	}
	
	/**
	 * 取状态.on文件中符合规则的并修改为.ing，同时返回相应的真实文件路径
	 * @param fileNameFormat .on文件名的格式化字符串，后缀名会自动修改为.on（$number$和$time$最多只允许出现一次，当存在$time$时yyyyMMddHHmmss必须出现且是连续的字符串且中间不能有别的字符，除它们外如果含有正则元字符则需要自行先转义）,格式如：LUU-SD$number,10$-$time,yyyyMMddHHmmss$\\.xml
	 * @param fileDirectory 文件所在的绝对目录
	 * @param autoCreateDir 是否自动创建目录
	 * @return
	 * @throws Exception 
	 */
	public static String getFilePathOn(String fileNameFormat,String fileDirectory,boolean autoCreateDir) throws Exception {
		long waitTime = 5000;
		int maxDeleteTimes = 5;
		int maxRenameTimes = 5;
		if (fileDirectory!=null&&!"".equals((fileDirectory = fileDirectory.trim()))&&!fileDirectory.endsWith(File.separator))
			fileDirectory = fileDirectory + File.separator;
		
		File dir = new File(fileDirectory);
		if(autoCreateDir){
			createDir(fileDirectory);
		}else if (!dir.exists()) {
			throw new IOException("the directory does not exist");
		}
		if(fileNameFormat==null||"".equals(fileNameFormat = fileNameFormat.trim())){
			throw new NullPointerException("the fileNameFormat parameter cannot be empty!");
		}
		String fileType = fileNameFormat.substring(fileNameFormat.lastIndexOf("."));
		String fileFmtNoType = fileNameFormat.substring(0,fileNameFormat.lastIndexOf("."));
		String findFileType = ".on";
		String renameStatusFileType = ".ing";
		String ontoingFileName = EncryptUtil.MD5(fileDirectory+fileFmtNoType) + ".oncheck";
		String ontoingFilePath = fileDirectory + ontoingFileName;
		File ontoingFile = new File(ontoingFilePath);
		if(!ontoingFile.exists()){
			if(!createFile(ontoingFilePath)){
				Thread.sleep(waitTime);
				return getFilePathOn(fileNameFormat,fileDirectory,autoCreateDir);
			}
			String gotFileName = null;
			String gotFilePath = null;
			try{
				java.util.regex.Pattern numPattern = java.util.regex.Pattern.compile("[0-9]*");
				String regexp = "(?:\\$([^\\$,\\(\\)]+)(?:,([^\\$,\\(\\)]+))?\\$)";
				java.util.regex.Pattern utilPattern = java.util.regex.Pattern.compile(regexp);//不允许出现$ ( ) 且,只允许出现1次，否则会匹配不到
				java.util.regex.Matcher utilMatcher = utilPattern.matcher(fileFmtNoType+findFileType);
				boolean hasNumInc = false;
				boolean hasTime = false;
				int matchedNumIncIndex = -1;
				int matchedTimeIndex = -1;
				int matched = -1;
				int numLength = -1;
				String timeFmt = null;
				String reg = null;
				StringBuffer newStr = new StringBuffer();
				utilMatcher.reset();
				while(utilMatcher.find()){
					String g1 = utilMatcher.group(1).trim().toLowerCase();
					String g2 = utilMatcher.group(2);
					if(g2!=null){
						g2 = g2.trim();
					}
					if("number".equals(g1)){
						if(hasNumInc){
							throw new Exception("number format repeated!");
						}
						if(g2!=null&&!"".equals(g2)){
							if(!numPattern.matcher(g2).matches()){
								throw new Exception("number length must be a numeric!");
							}
							numLength = Integer.valueOf(g2).intValue();
							if(numLength <= 0){
								//hasNumInc = false;
								numLength = -1;
							}
						}
						hasNumInc = true;
						matched++;
						matchedNumIncIndex = matched;
					}else if("time".equals(g1)){
						if(hasTime){
							throw new Exception("time format repeated!");
						}
						if(g2!=null&&!"".equals(g2)){
							timeFmt = g2;
						}else{
							hasTime = false;
							timeFmt = null;
						}
						hasTime = true;
						matched++;
						matchedTimeIndex = matched;
					}
					
					if(hasNumInc){
						if(numLength==-1){
							reg = "(\\\\d+)";
						}else{
							reg = "(\\\\d{"+numLength+"})";
						}
					}
					if(hasTime){
						SimpleDateFormat t_sf = new SimpleDateFormat(timeFmt);
						Date t_dt = new Date();
						reg = "(\\\\d{"+t_sf.format(t_dt).length()+"})";
					}
					if(reg!=null){
						utilMatcher.appendReplacement(newStr, reg);
					}
					
				}
				if(reg!=null&&(hasNumInc||hasTime)){
					utilMatcher.appendTail(newStr);
					reg = newStr.toString();
					
					java.util.regex.Pattern findFilePattern = java.util.regex.Pattern.compile(reg);
					java.util.regex.Matcher findFileMatcher = null;
					
					File[] files = dir.listFiles();
					String fileName;
					String nowFileType;
					String numStr = null;
					String timeStr = null;
					SimpleDateFormat t_sf = new SimpleDateFormat(timeFmt);
					long minNum=-1;
					long minTime=-1;
					for(File file:files){
						fileName = file.getName();
						nowFileType = fileName.substring(fileName.lastIndexOf("."));
						if(!nowFileType.equals(findFileType)){
							continue;
						}
						findFileMatcher = findFilePattern.matcher(fileName);
						if(findFileMatcher.matches()){
							if(hasNumInc){
								numStr = findFileMatcher.group(matchedNumIncIndex+1);
								if(!numPattern.matcher(numStr).matches()){
									throw new Exception("group("+matchedNumIncIndex+1+") of regex matche is not a numeric!");
								}
								int thisNumLength = numStr.length();
								if(numLength!=-1&&numLength!=thisNumLength){
									throw new Exception("file name not specification!");
								}
								long thisNum = Long.valueOf(numStr).longValue();
								if(minNum==-1||thisNum<minNum){
									minNum = thisNum;
									gotFileName = fileName;
								}
							}else if(hasTime){
								timeStr = findFileMatcher.group(matchedTimeIndex+1);
								Date date = t_sf.parse(timeStr);
								if(date==null){
									throw new Exception("group("+matchedTimeIndex+1+") of regex matche is not a time!");
								}
								long thisTime = date.getTime();
								if(minTime==-1||thisTime<minTime){
									minTime = thisTime;
									gotFileName = fileName;
								}
							}
						}
					}
					
				}
				
				if(gotFileName==null||"".equals(gotFileName)){//找不到返回null
					gotFilePath = null;
					
					while(!ontoingFile.delete()&&--maxDeleteTimes>0){
						Thread.sleep(waitTime);
					}
				}else{
					String oldFilePath = fileDirectory+gotFileName;
					gotFileName = gotFileName.substring(0,gotFileName.lastIndexOf("."))+renameStatusFileType;
					gotFilePath = fileDirectory+gotFileName;
					File oldFile = new File(oldFilePath);
					File newFile = new File(gotFilePath);
					
					
					String realFilePath = gotFilePath.substring(0,gotFilePath.lastIndexOf("."))+fileType;
					File realFile = new File(realFilePath);
					
					if(!realFile.exists()){
						gotFilePath = null;
						throw new Exception("the file "+realFilePath+" not exist!");
					}
					
					boolean flag = false;
					while(!(flag = oldFile.renameTo(newFile))&&--maxRenameTimes>0){
						Thread.sleep(waitTime);
					}
					if(!flag){
						throw new Exception("the file "+oldFilePath+" rename to "+ gotFilePath +" faild,check the file whether or not exist!");
					}
					
					gotFilePath = realFilePath;
					
					while(!ontoingFile.delete()&&--maxDeleteTimes>0){
						Thread.sleep(waitTime);
					}
					
				}
				
			}catch(Exception e){
				while(!ontoingFile.delete()&&--maxDeleteTimes>0){
					Thread.sleep(waitTime);
				}
				throw new Exception(e.getMessage());
			}
			
			return gotFilePath;
		}else{
			Thread.sleep(waitTime);
			return getFilePathOn(fileNameFormat,fileDirectory,autoCreateDir);
		}
	}
	
	/**
	 * 将文件对应的状态文件改为.ok
	 * @param filePath 文件绝对路径
	 * @param oldStatus 原始状态
	 * @param newStatus 新状态
	 * @param data 设置状态文件所承载的数据，为null时不修改数据
	 * @param encoding 文件编码
	 * @return
	 * @throws Exception
	 */
	public static boolean turnFileStatus(String filePath,String oldStatus,String newStatus,Map<String,String> data,String encoding) throws Exception {
		boolean flag = false;
		long waitTime = 5000;
		int maxRenameTimes = 5;
		File file = new File(filePath);
		if(!file.exists()||!file.isFile()){
			throw new Exception("the "+filePath+" file does not exist or is not a file!");
		}
		
		if(oldStatus==null||"".equals(oldStatus = oldStatus.trim())){
			throw new Exception("the oldStatus parameter is not valid!");
		}else if(oldStatus.length()>2&&oldStatus.indexOf(".")==0){
			oldStatus = oldStatus.substring(1);
		}
		if(!"on".equals(oldStatus)&&!"ing".equals(oldStatus)&&!"ok".equals(oldStatus)){
			throw new Exception("the status "+oldStatus+" is not a status!");
		}
		
		if(newStatus==null||"".equals(newStatus = newStatus.trim())){
			throw new Exception("the newStatus parameter is not valid!");
		}else if(newStatus.length()>2&&newStatus.indexOf(".")==0){
			newStatus = newStatus.substring(1);
		}
		if(!"on".equals(newStatus)&&!"ing".equals(newStatus)&&!"ok".equals(newStatus)){
			throw new Exception("the status "+newStatus+" is not a status!");
		}
		
		
		String findFileType = "."+oldStatus;
		String renameFileType = "."+newStatus;
		String fileNameNoType = filePath.substring(0,filePath.lastIndexOf("."));
		String findFilePath = fileNameNoType + findFileType;
		String renameFilePath = fileNameNoType + renameFileType;
		
		File findFile = new File(findFilePath);
		if(!findFile.exists()||!findFile.isFile()){
			throw new Exception("the "+findFile+" file does not exist or is not a file!");
		}
		
		if(data!=null&&data.size()!=0){
			setFileStatusData(filePath, oldStatus, data, encoding);
		}
		
		File renameFile = new File(renameFilePath);
		boolean renameflag = false;
		while(!(renameflag = new File(findFilePath).renameTo(renameFile))&&--maxRenameTimes>0){
			Thread.sleep(waitTime);
		}
		if(!renameflag){
			throw new Exception("the file "+findFilePath+" rename to "+ renameFilePath +" faild,check the file whether or not exist!");
		}else{
			flag = true;
		}
		
		return flag;
	}
	
	/**
	 * 读取某文件的状态文件中的数据
	 * @param filePath 文件绝对路径
	 * @param status 状态：on ing ok
	 * @param encoding
	 * @return
	 * @throws Exception
	 */
	public static Map<String,String> getFileStatusData(String filePath,String status,String encoding) throws Exception{
		Map<String,String> map = new HashMap<String,String>();
		File file = new File(filePath);
		if(!file.exists()||!file.isFile()){
			throw new Exception("the "+filePath+" file does not exist or is not a file!");
		}
		if(status==null||"".equals(status = status.trim())){
			status = "on";
		}else if(status.length()>2&&status.indexOf(".")==0){
			status = status.substring(1);
		}
		if(!"on".equals(status)&&!"ing".equals(status)&&!"ok".equals(status)){
			throw new Exception("the status "+status+" is not a status!");
		}
		
		if(encoding==null||"".equals(encoding = encoding.trim())){
			encoding = "UTF-8";
		}
		
		String findFileType = "."+status;
		String fileNoType = filePath.substring(0,filePath.lastIndexOf("."));
		String findFilePath = fileNoType + findFileType;
		
		File findFile = new File(findFilePath);
		if(!findFile.exists()||!findFile.isFile()){
			throw new Exception("the "+findFilePath+" file does not exist or is not a file!");
		}
		
		FileInputStream fis = null;
		BufferedReader br = null;
		try{
			fis = new FileInputStream(findFile);
			br = new BufferedReader(new InputStreamReader(fis, encoding));
			String readline;
			while(null != (readline = br.readLine())){
				if("".equals(readline = readline.trim())){
					continue;
				}
				String[] sArr = readline.split("=");
				if(sArr.length!=2){
					throw new Exception("file data in "+findFilePath+" format is illegal!");
				}
				String key = sArr[0].trim();
				String value = sArr[1].trim();
				map.put(key, value);
			}
			br.close();
			fis.close();
		}catch(Exception e){
			throw e;
		}finally{
			br.close();
			fis.close();
		}
		return map;
	}
	
	/**
	 * 设置某文件的状态文件中的数据
	 * @param filePath 文件绝对路径
	 * @param status 状态：on ing ok
	 * @param data 设置的数据
	 * @param encoding
	 * @throws Exception
	 */
	public static void setFileStatusData(String filePath,String status,Map<String,String> data,String encoding) throws Exception{
		File file = new File(filePath);
		if(!file.exists()||!file.isFile()){
			throw new Exception("the "+filePath+" file does not exist or is not a file!");
		}
		if(status==null||"".equals(status = status.trim())){
			status = "on";
		}else if(status.length()>2&&status.indexOf(".")==0){
			status = status.substring(1);
		}
		if(!"on".equals(status)&&!"ing".equals(status)&&!"ok".equals(status)){
			throw new Exception("the status "+status+" is not a status!");
		}
		
		if(encoding==null||"".equals(encoding = encoding.trim())){
			encoding = "UTF-8";
		}
		
		if(data==null){
			data = new HashMap<String,String>();
		}
		String findFileType = "."+status;
		String fileNoType = filePath.substring(0,filePath.lastIndexOf("."));
		String findFilePath = fileNoType + findFileType;
		
		File findFile = new File(findFilePath);
		if(!findFile.exists()||!findFile.isFile()){
			throw new Exception("the "+findFilePath+" file does not exist or is not a file!");
		}
		
		String lineSp = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		
		Set<String> key = data.keySet();
		boolean isFirst = true;
        for (Iterator<String> it = key.iterator(); it.hasNext();) {
            String s = it.next();
            String v = data.get(s);
            if(s!=null&&!"".equals(s = s.trim())&&v!=null&&!"".equals(v = v.trim())){
            	if(!isFirst){
            		sb.append(lineSp);
            	}else{
            		isFirst = false;
            	}
            	sb.append(s);
            	sb.append("=");
            	sb.append(v);
            }else{
            	throw new Exception("the data parameter is not valid!");
            }
        }
        byte[] bs = sb.toString().getBytes();
        RandomAccessFile raf = null;
        try{
        	raf = new RandomAccessFile(findFilePath,"rw");
            raf.setLength(0);
    		raf.seek(0);
    		raf.write(bs);
    		raf.close();
        }catch(Exception e){
        	throw e;
        }finally{
        	raf.close();
        }
        
	}
	
	
	/** 
     * 根据字符串和位数进行补位，位数不足则补足
     * @param str   原始字符串 
     * @param ws    需要生成的位数 
     * @param pre	用来补足的字符串
     * @return 
     */  
    private static String fillWord(String str,int ws,String pre){
        StringBuffer formatstr = new StringBuffer();
        int strLength = str.length();
        for(int i=0;i<ws-strLength;i++){
        	formatstr.append(pre);
        }
        formatstr.append(str);
        return formatstr.toString();
    }
    
    /**
     * 修改文件头部信息
     * @param filePath 文件绝对路径
     * @param findRegexp 匹配查找待修改部分的正则，替换后的字符串长度小于原字符串长度则在最后加空格，替换后的字符串长度大于原字符串长度则报错，如：<info\\s+id=\"\\$[^\\$\"]*\\$\"\\s+type=\"net_login_info\"\\s+resultnum=\"\\$[^\\$\"]*\\$\"\\s*>
     * @param replaceMap 用来替换匹配到的文本中以$$之间内容为键名的键值对对象
     * @param maxReadLineNum 从上到下匹配查找的最大文件行数
     * @param encoding 文件的编码
     * @return
     * @throws Exception
     */
	public static boolean modifyFileTop(String filePath,String findRegexp, Map<String,String> replaceMap,Integer maxReadLineNum,String encoding) throws Exception{
		boolean flag = false;
		if(maxReadLineNum==null){
			maxReadLineNum = 100;
		}
		String lineSp = System.getProperty("line.separator");
		File file = new File(filePath);
		if(!file.isFile()||!file.exists()){
			throw new IOException("file does not exist or is not a file!");
		}
		if(findRegexp==null||"".equals(findRegexp = findRegexp.trim())){
			throw new Exception("the findRegexp parameter cannot be empty!");
		}
		if(encoding==null||"".equals(encoding = encoding.trim())){
			encoding = "UTF-8";
		}
		FileInputStream fir = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		String s;
		String comparaStr;
		boolean ifFirstHasRead = false;
		
		java.util.regex.Pattern findPattern = java.util.regex.Pattern.compile(findRegexp);
		java.util.regex.Matcher findMatcher = null;
		boolean isFound = false;
		try{
			fir = new FileInputStream(file);
			isr = new InputStreamReader(fir, encoding);
			br = new BufferedReader(isr);
			while(maxReadLineNum-->0&&!isFound&&(s = br.readLine())!=null){
				if(!ifFirstHasRead){
					ifFirstHasRead = true;
				}else{
					sb.append(lineSp);
				}
				sb.append(s);
				comparaStr = sb.toString();
				findMatcher = findPattern.matcher(comparaStr);
				if(findMatcher.find()){
					isFound = true;
					int start = findMatcher.start();
					int end = findMatcher.end();
					String preStr = comparaStr.substring(0,start);
					String handleStr = comparaStr.substring(start,end);
					String finStr = comparaStr.substring(end);
					java.util.regex.Pattern replacePattern = java.util.regex.Pattern.compile("\\$\\s*([^$\\s\\(\\)]*)\\s*(?:\\([^\\(\\)]*\\))?\\s*\\$");
					java.util.regex.Matcher replaceMatcher = replacePattern.matcher(handleStr);
					byte[] oldBytes = comparaStr.getBytes();
					
					StringBuffer newHandleStrbf = new StringBuffer();
					while(replaceMatcher.find()){
						//String g0 = replaceMatcher.group(0);
						String g1 = replaceMatcher.group(1);
						String value = "";
						if(replaceMap.get(g1)!=null){
							value = replaceMap.get(g1);
						}
						replaceMatcher.appendReplacement(newHandleStrbf, value);
					}
					replaceMatcher.appendTail(newHandleStrbf);
					StringBuffer newStrbf = new StringBuffer();
					String newStr = comparaStr;
					if(newHandleStrbf.length()!=0){
						newStrbf.append(preStr);
						newStrbf.append(newHandleStrbf);
						newStrbf.append(finStr);
						newStr = newStrbf.toString();
						byte[] newBytes = newStr.getBytes();
						if(newBytes.length>oldBytes.length){
							throw new Exception("the modified length of top info is too long!");
						}
						StringBuffer appendToNewHandleStrbf = new StringBuffer();
						appendToNewHandleStrbf.append(newHandleStrbf);
						for(int i=0;i<oldBytes.length-newBytes.length;i++){
							appendToNewHandleStrbf.append(" ");
						}
						newHandleStrbf = appendToNewHandleStrbf;
						newStrbf = new StringBuffer();
						newStrbf.append(preStr);
						newStrbf.append(newHandleStrbf);
						newStrbf.append(finStr);
						newStr = newStrbf.toString();
						newBytes = newStr.getBytes();
						RandomAccessFile raf = null;
						try{
							raf = new RandomAccessFile(filePath,"rw");
							raf.seek(0);
							raf.write(newBytes);
							raf.close();
							flag = true;
						}catch(Exception e){
							throw e;
						}finally{
							raf.close();
						}
						
					}
				}
			}
			br.close();
			isr.close();
			fir.close();
		}catch(Exception e){
			throw e;
		}finally{
			br.close();
			isr.close();
			fir.close();
		}
		
		return flag;
	}

	public static long getFileSize(String filePath){
		File file = new File(filePath);
		return file.length();
	}
	public static long getFileSize(String filePath,long divisionNum){
		long size = getFileSize(filePath);
		return size/divisionNum;
	}
	public static long getFileLastModifiedTime(String filePath){
		File file = new File(filePath);
		return file.lastModified();
	}
	public static String getFileLastModifiedTime(String filePath,String fmt){
		long time = getFileLastModifiedTime(filePath);
		Date date = new Date(time);
		SimpleDateFormat formatter = new SimpleDateFormat (fmt);
		return formatter.format(date);
	}
}
