package com.customtime.data.conversion.plugin.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;
import com.customtime.data.conversion.plugin.util.FileUtil;

public class XmlWriter extends AbstractWriter {

	private final static Log logger = LogFactory.getLog(FileReader.class);
	
	@TMProperty(defaultValue="UTF-8")
	private String encoding;
	@TMProperty(defaultValue="d:\\xmlwrite\\")
	private String fileDirectory;
	@TMProperty(defaultValue="LUU-SD$number,10$-$time,yyyyMMddHHmmss$\\.xml")
	private String fileName;
	@TMProperty(defaultValue="40000")
	private long maxRowNum;
	@TMProperty(defaultValue="-1")
	private long maxLineNum;
	@TMProperty(defaultValue="-1")
	private int maxFileSize;//以M为单位
	@TMProperty(defaultValue="1000")
	private int flushNum;
	@TMProperty(defaultValue="\\$")
	private String symbol;
	@TMProperty
	private String formatTopRegex;
	@TMProperty(defaultValue="<?xml version=\"1.0\" encoding=\"UTF-8\"?><br/><info id=\"$businessId$(10)\" type=\"net_login_info\" resultnum=\"$rowNum(10)$\">")
	private String wrapTagStart;
	@TMProperty(defaultValue="</info>")
	private String wrapTagEnd;
	@TMProperty(defaultValue="<log account=\"$0\" accountType=\"$4\" loginType=\"$7\" priIpAddr=\"$12\" pubIpAddr=\"$15\" onLineTime=\"$20\" offLineTime=\"$22\" />")
	private String rowTag;
	
	
	private String filePath;
	private File file;
	private String newLineRegexp;
	private String lineSp;//跨平台的换行符
	
	private boolean isModifyFileTop;
	private boolean matchedWrapTagStartWithTopRegex;
	private boolean wrongWrapTagStart;
	
	private Map<String,Integer> maxPlaceholderMap;//占位符标识及其位数的映射关系（对wrapTagStart的配置项进行处理后得到）
	private int wrapTagStartLineNum;
	private int wrapTagEndLineNum;
	private int nowRowNum;
	private int nowLineNum;
	private long lastRowNum;
	private long lastLineNum;
	private Recode lastRecode;
	private int isReadFileStatusData;//1表示读取状态文件的数据并赋值给当前对象的变量(lastRowNum和lastLineNum);2表示当前对象的变量(lastRowNum和lastLineNum)重置清零;3表示啥也不干
	private String businessId;//暂时以固定方式取文件名中的第[7,16]位数字
	
	private java.util.regex.Pattern pt;
	
	private java.util.regex.Matcher mc;
	
	private java.util.regex.Pattern ptNL;
	
	private java.util.regex.Matcher mcNL;
	
	public void init() {
		logger.info("start write init");
		lineSp = System.getProperty("line.separator");
		nowRowNum = 0;
		nowLineNum = 0;
		lastRecode = null;
		filePath = null;
		file = null;
		isReadFileStatusData = 3;//针对状态文件啥也不干
		
		//对行数据的配置项进行处理
		pt = java.util.regex.Pattern.compile("(?:"+symbol+"([^(?:"+symbol+")]+)"+symbol+")");
		mc = pt.matcher(rowTag);
		
		//换行标识处理
		newLineRegexp = "\\s*<br\\s*/>\\s*";
		wrapTagStartLineNum = wrapTagStart.split(newLineRegexp).length;
		ptNL = java.util.regex.Pattern.compile(newLineRegexp);
		wrapTagEndLineNum = wrapTagEnd.split(newLineRegexp).length;
		
		//对wrapTagStart（文件开始部分）的配置项处理，解析得到正确的占位字符串
		maxPlaceholderMap = new HashMap<String,Integer>();
		if(formatTopRegex==null||"".equals(formatTopRegex = formatTopRegex.trim())){
			isModifyFileTop = false;
		}else{
			java.util.regex.Pattern findPattern = java.util.regex.Pattern.compile(formatTopRegex);
			java.util.regex.Matcher findMatcher = findPattern.matcher(wrapTagStart);
			StringBuffer newSb1 = new StringBuffer();
			if(findMatcher.find()){
				String regexp = "\\$(?:([^\\$\\(\\)\"]+)?(?:\\((\\d+)\\))?[^\\$]*)?\\$";//匹配$$之间的内容
				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regexp);
				java.util.regex.Matcher matcher = pattern.matcher(findMatcher.group(0));
				wrongWrapTagStart = true;
				StringBuffer newSb2 = new StringBuffer();
				while(matcher.find()){
					if(matcher.group(1)==null||matcher.group(2)==null||"".equals(matcher.group(1).trim())||"".equals(matcher.group(2).trim())){
						wrongWrapTagStart = true;
						break;
					}else{
						maxPlaceholderMap.put(matcher.group(1), Integer.valueOf(matcher.group(2)));
						matcher.appendReplacement(newSb2, "\\$"+fillWord(matcher.group(1),Integer.valueOf(matcher.group(2)).intValue()-2," ")+"\\$");
						wrongWrapTagStart = false;
					}
				}
				matcher.appendTail(newSb2);
				findMatcher.appendReplacement(newSb1, newSb2.toString().replace("$", "\\$"));
				matchedWrapTagStartWithTopRegex = true;
			}else{
				matchedWrapTagStartWithTopRegex = false;
			}
			findMatcher.appendTail(newSb1);
			wrapTagStart = newSb1.toString();
			isModifyFileTop = true;
		}
		
		
		logger.info("end write init");
		
		
	}

	public void destroy() {
		logger.info("destory write");
	}

	public void writing(RecodesKeeper rk){
		logger.info("start writing");
		Writer writer = null;
		PrintWriter pw = null;
		try {
			if(isModifyFileTop&&!matchedWrapTagStartWithTopRegex){
				throw new Exception("configure wrapTagStart and formatTopRegex configuration mismatch!");
			}
			if(isModifyFileTop&&matchedWrapTagStartWithTopRegex&&wrongWrapTagStart){
				throw new Exception("configuration wrapTagStart configurate error!");
			}
			//先取.on状态的xml文件,去完之后.on自动变为.ing
			filePath = FileUtil.getFilePathOn(fileName, fileDirectory, true);
			if(filePath!=null){
				isReadFileStatusData = 1;//读取状态文件数据并赋值给当前对象的变量(lastRowNum和lastLineNum)
				
			}else{//再取新生成的xml文件并自动生成.ing状态文件
				filePath = FileUtil.getFilePathUni(fileName, fileDirectory, true);
				isReadFileStatusData = 2;//当前对象的变量(lastRowNum和lastLineNum)初始化清零
			}
			if(filePath==null){
				throw new Exception("can not get filePath!");
			}
			logger.info("dealling file:"+filePath);
			file = new File(filePath);
			
			int _flushNum = 0;
			writer = new OutputStreamWriter(new FileOutputStream(file,true), encoding);
			pw = new PrintWriter(writer);
			//pw.println(matchNewLine(wrapTagStart));
			if(isReadFileStatusData==2){//只有全新的文件才需要写开始部分的内容
				printLines(pw,wrapTagStart);
				_flushNum++;
			}
			
			Recode recode;
			boolean isThresholdBreak = false;
			while((recode = (lastRecode==null?rk.arising(this):lastRecode))!=null){
				lastRecode = null;
				//pw.println(matchNewLine(matchSymbol(recode)));
				printLines(pw,matchSymbol(recode));
				_flushNum++;
				nowRowNum++;
				if(_flushNum==flushNum){
					_flushNum = 0;
					if(isThresholdBreak = isAtThreshold(pw,true)){
						break;
					}
				}else if(isThresholdBreak = isAtThreshold(pw,false)){
					break;
				}
			}
			if(!isThresholdBreak){
				pw.flush();
				pw.close();
				writer.close();
				FileUtil.turnFileStatus(filePath, "ing", "on", makeFileStatusData(), encoding);
			}else{
				printLines(pw,wrapTagEnd);
				pw.flush();
				pw.close();
				writer.close();
				FileUtil.turnFileStatus(filePath, "ing", "ok", makeFileStatusData(), encoding);
				modifyFileTop(makeFileTopData());
				if(assertNewFile(rk)){
					writing(rk);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if(pw!=null){
					pw.flush();
					pw.close();
				}
				if(writer!=null){
					writer.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		logger.info("end writing");
	}
	
	//暂时实现的方法。以固定方式取文件名中的第[7,16]位数字
	private String getBusinessId(){
		if(filePath==null||"".equals(filePath.trim())){
			businessId = "";
		}else{
			File f = new File(filePath);
			if(f.getName()!=null&&!"".equals(f.getName().trim())){
				businessId = "00"+f.getName().substring(6,16);
			}else{
				businessId = "";
			}
		}
		logger.info("the businessId is "+ businessId);
		return businessId;
	}
	
	//判断文件是否到达阀值,文件大小的判断只有isFlush设为true时判断才起作用
	private boolean  isAtThreshold(PrintWriter pw,boolean isFlush) throws Exception{
		if(isFlush){
			pw.flush();
		}
		boolean flag = false;
		initGetFileStatusData();
		if(maxLineNum!=-1&&(nowLineNum+lastLineNum)>=(maxLineNum-wrapTagEndLineNum)){
			flag = true;
		}
		if(maxRowNum!=-1&&(nowRowNum+lastRowNum)>=maxRowNum){
			flag = true;
		}
		if(maxFileSize!=-1&&isFlush){
			if(file.exists()){
				//pw.flush();
				long fileSize = getFileSize(file)/1048576;//单位M
				if(fileSize>=maxFileSize){
					flag = true;
				}
			}
		}
		return flag;
	}
	
	//根据isReadFileStatusData的值来初始化lastLineNum和lastRowNum
	private void initGetFileStatusData() throws Exception{
		if(isReadFileStatusData==1){//读取状态文件的数据并赋值给当前对象的变量
			//获取.on状态文件的数据
			Map<String,String> map = FileUtil.getFileStatusData(filePath, "ing", encoding);
			if(map.get("lineNum")!=null){
				lastLineNum = Integer.valueOf(map.get("lineNum")).intValue();
			}
			if(map.get("rowNum")!=null){
				lastRowNum = Integer.valueOf(map.get("rowNum")).intValue();
			}
			isReadFileStatusData = 3;
		}else if(isReadFileStatusData==2){//当前对象的变量初始化清零
			lastLineNum = 0;
			lastRowNum = 0;
		}
		//其它情况啥也不干
	}
	
	//到达阀值之后调用，更改状态文件和当前文件的开始部分的内容，并判断是否需要继续写新文件，在该方法之前需先结束、关闭之前的文件操作
	private boolean assertNewFile(RecodesKeeper rk) throws Exception{
		boolean flag = false;
		
		lastRecode = rk.arising(this);
		if(lastRecode!=null){
			nowRowNum = 0;
			nowLineNum = 0;
			isReadFileStatusData = 3;
			filePath = null;
			file = null;
			flag = true;
		}
		return flag;
	}
	
	
	
	//修改头部的字符串
	private boolean modifyFileTop(Map<String,String> map) throws Exception{
		boolean flag = false;
		if(isModifyFileTop&&matchedWrapTagStartWithTopRegex&&!wrongWrapTagStart){
			flag = FileUtil.modifyFileTop(filePath, formatTopRegex, map, wrapTagStartLineNum, encoding);
		}
		return flag;
	}
	
	//组装用来替换文件开始部分占位符的数据
	private Map<String,String> makeFileTopData(){
		Map<String,String> topData = new HashMap<String,String>();
		topData.put("businessId", getBusinessId());
		topData.put("rowNum", String.valueOf(nowRowNum+lastRowNum));
		return topData;
	}
	
	//组装状态文件的数据
	private Map<String,String> makeFileStatusData(){
		Map<String,String> map = new HashMap<String,String>();
		map.put("lineNum", String.valueOf(nowLineNum+lastLineNum));
		map.put("rowNum", String.valueOf(nowRowNum+lastRowNum));
		map.put("businessId", getBusinessId());
		return map;
	}
	
	//获得文件或者文件夹下所有文件的大小
	private long getFileSize(File f)throws Exception{
        long size = 0;
        File flist[] = f.listFiles();
        if(f.isFile()){
        	size = size + f.length();
        }else{
        	for (int i = 0; i < flist.length; i++){
                if (flist[i].isDirectory()){
                    size = size + getFileSize(flist[i]);
                }else{
                    size = size + flist[i].length();
                }
            }
        }
        
        return size;
    }

	//替换符号
	private String matchSymbol(Recode recode){
		StringBuffer newStr = new StringBuffer();
		mc.reset();
		String value = null;
		while(mc.find()){
			String index = mc.group(1);
			value = recode.getBlock(Integer.parseInt(index));
			if(value==null){
				value = "";
			}
			mc.appendReplacement(newStr, value);
		}
		mc.appendTail(newStr);
		return newStr.toString();
	}
	
	//输出函数
	private void printLines(PrintWriter pw,String content){
		int thisLine = content.split(newLineRegexp).length;
		content = matchNewLine(content);
		pw.println(content);
		//thisLine++;
		nowLineNum +=thisLine;
	}
	
	//替换换行符
	private String matchNewLine(String str){
		mcNL = ptNL.matcher(str);
		String newStr = mcNL.replaceAll(lineSp);
		return newStr;
	}
	
	/** 
     * 根据字符串和位数进行补位，位数不足则补足
     * @param str   原始字符串 
     * @param ws    需要生成的位数 
     * @param pre	用来补足的字符串
     * @return 
     */  
    private String fillWord(String str,int ws,String pre){ 
        StringBuffer formatstr = new StringBuffer();
        int strLength = str.length();
        for(int i=0;i<ws-strLength;i++){
        	formatstr.append(pre);
        }
        formatstr.append(str);
        return formatstr.toString();
    }
	
	public static void main(String[] args) throws Exception {
		//skipAppend(6,"dabing","D:\\测试raf\\raf.txt");
	}
}
