package com.customtime.data.conversion.plugin.util;

import java.util.regex.*;
import java.util.*;
/**
 *
 * <p>Title: Document</p>
 *
 * <p>Description: 用正则表达式解析xml,目的是为了提高性能.</p>
 *
 */
public class Document {
	private String xmlString;
	/**
	 * 传入xml的字符串内容,对于InputStream,Reader对象请转换为String对象后传入构造方法.
	 * @param xmlString String
	 * @throws IllegalArgumentException
	 */
	public Document(String xmlString) throws IllegalArgumentException{
		if(xmlString == null || xmlString.length() == 0)
			throw new IllegalArgumentException("Input string orrer!");
		this.xmlString = xmlString;
	}

	/**
	 * 在文档中搜索指定的元素,返回符合条件的元素数组.
	 * @param tagName String
	 * @return String[]
	 */
	public String[] getElementsByTag(String tagName){
		Pattern p = Pattern.compile("<"+tagName+"[^>]*?((>.*?</"+tagName+">)|(/>))");
		Matcher m = p.matcher(this.xmlString);
		ArrayList<String> al = new ArrayList<String>();
		while(m.find())
			al.add(m.group());
		String[] arr = al.toArray(new String[al.size()]);
		al.clear();
		return arr;
	}
  

	/**
	 * 用xpath模式提取元素,以#为分隔符
	 * 如 #ROOT#PARENT#CHILD(第一个#号可以忽略)表示提取ROOT元素下的PARENT元素下的CHILD元素(忽略@12这种多重元素的定位，返回最后一个元素)
	 * @param singlePath String
	 * @return String
	 */
	public String getElementBySinglePath(String singlePath){
		if(singlePath.indexOf("#")==0){
			singlePath = singlePath.substring(1);
		}
		singlePath = singlePath.replaceAll("@(firse|last|\\d*)", "");
		
		String[] path = singlePath.split("#");
		String lastTag = path[path.length-1];
		String tmp = "(<"+lastTag+"[^>]*?((>.*?</"+lastTag+">)|(/>)))";//最后一个元素,可能是<x>v</x>形式或<x/>形式
		for(int i=path.length-2;i >=0;i--){
			lastTag = path[i];
			tmp = "<"+lastTag+"[^>]*?>.*"+tmp + ".*</"+lastTag+">";
		}
		Pattern p = Pattern.compile(tmp);
		Matcher m = p.matcher(this.xmlString);
		if(m.find()){
			return m.group(1);
		}
		return "";
	}
	/**
	 * 用xpath模式提取元素从多重元素中获取指批定元素,以#为分隔符
	 * 元素后无索引序号则默认为0: #ROOT#PARENT@2#CHILD@1(第一个#号可以忽略) 也支持#ROOT#PARENT@2#CHILD[name=xx]
	 * @param multiPath String
	 * @return String
	 */
	public String getElementByMultiPath(String multiPath){
		try{
			if(multiPath.indexOf("#")==0){
				multiPath = multiPath.substring(1);
			}
			String[] path = multiPath.split("#");
			String input = this.xmlString;
			String[] ele = null;
			for (int i = 0; i < path.length; i++) {
				//Pattern p = Pattern.compile("(\\w+)(\\[(\\d+)\\])?");
				//Pattern p = Pattern.compile("(\\w+)(@(\\d+|first|last))?");
				Pattern p = Pattern.compile("(\\w+)(@(\\d+|first|last)|\\((\\s*[^\\(\\)=]+=[^\\(\\)=]+\\s*)\\))?");
				Matcher m = p.matcher(path[i]);
				if (m.find()) {
					String tagName = m.group(1);
					//System.out.println(input + "----" + tagName);
					//int index = (m.group(3) == null) ? 0 :
					//    new Integer(m.group(3)).intValue();
					ele = getElementsByTagFromParent(input, tagName);
					String index = "0";
					if(m.group(3)!=null){
						if("0".equals(m.group(3))||"first".equals(m.group(3))){
							index = "first";
						}else if(m.group(3).matches("\\d+")){
							index = String.valueOf(new Integer(m.group(3)).intValue());
						}else if(!"last".equals(m.group(3))){
							index = "first";
						}
					}else if(m.group(4)!=null){
						String[] mm = m.group(4).split("=");
						String name = mm[0].trim();
						String value = mm[1].trim();
						String findValue = null;
						boolean finded = false;
						for(String el:ele){
							if(el!=null){
								findValue = getAttributeByName(el,name);
								if(findValue!=null&&(findValue.trim()).equals(value)){
									input = el;
									finded = true;
									break;
								}
							}
						}
						if(!finded){
							input = "";
						}
						index = null;
					}else{
						index = "first";
					}
					if(index!=null){
						if("first".equals(index)){
							input = ele[0];
						}else if("last".equals(index)){
							input = ele[ele.length-1];
						}else{
							input = ele[new Integer(index).intValue()];
						}
					}
				}
			}
			if(input!=null&&"".equals(input.trim())){
				input = null;
			}
			return input;
		}catch(Exception e){
			return null;
		}
	}
	/**
	 * 在给定的元素中搜索指定的元素,返回符合条件的元素数组.对于不同级别的同名元素限制作用,即可以
	 * 搜索元素A中的子元素C.而对于元素B中子元素C则过虑,通过多级限定可以准确定位.
	 * @param parentElementString String
	 * @param tagName String
	 * @return String[]
	 */
	public static String[] getElementsByTagFromParent(String parentElementString,String tagName){
		Pattern p = Pattern.compile("<"+tagName+"[^>]*?((>.*?</"+tagName+">)|(/>))");
		Matcher m = p.matcher(parentElementString);
		ArrayList<String> al = new ArrayList<String>();
		while(m.find())
			al.add(m.group());
		String[] arr = al.toArray(new String[al.size()]);
		al.clear();
		return arr;
	}
	/**
	 * 从指定的父元素中根据xpath模式获取子元素,singlePath以#为分隔符
	 * 如 #ROOT#PARENT#CHILD(第一个#号可以忽略)表示提取ROOT元素下的PARENT元素下的CHILD元素(忽略@12这种多重元素的定位，返回最后一个元素)
	 * @param parentElementString String
	 * @param singlePath String
	 * @return String
	 */
	public static String getElementBySinglePathFromParent(String parentElementString,String singlePath){
		if(singlePath.indexOf("#")==0){
			singlePath = singlePath.substring(1);
		}
		singlePath = singlePath.replaceAll("@(firse|last|\\d*)", "");
	
		String[] path = singlePath.split("#");
		String lastTag = path[path.length-1];
		String tmp = "(<"+lastTag+"[^>]*?((>.*?</"+lastTag+">)|(/>)))";//最后一个元素,可能是<x>v</x>形式或<x/>形式
		for(int i=path.length-2;i >=0;i--){
			lastTag = path[i];
			tmp = "<"+lastTag+"[^>]*?>.*"+tmp + ".*</"+lastTag+">";
		}
		Pattern p = Pattern.compile(tmp);
		Matcher m = p.matcher(parentElementString);
		if(m.find()){
			return m.group(1);
		}
		return "";
	}
	/**
	 * 用xpath模式提取元素从指定的多重元素中获取指批定元素,以#为分隔符
	 * @param parentElementString String
	 * @param multiPath String
	 * @return String
	 */
	public static String getElementByMultiPathFromParent(String parentElementString,String multiPath){
		try{
			if(multiPath.indexOf("#")==0){
				multiPath = multiPath.substring(1);
			}
			String[] path = multiPath.split("#");
			String input = parentElementString;
			String[] ele = null;
			for (int i = 0; i < path.length; i++) {
				//Pattern p = Pattern.compile("(\\w+)(\\[(\\d+)\\])?");
				//Pattern p = Pattern.compile("(\\w+)(@(\\d+|first|last))?");
				Pattern p = Pattern.compile("(\\w+)(@(\\d+|first|last)|\\((\\s*[^\\(\\)=]+=[^\\(\\)=]+\\s*)\\))?");
				Matcher m = p.matcher(path[i]);
				if (m.find()) {
					String tagName = m.group(1);
					//int index = (m.group(3) == null) ? 0 :
					//    new Integer(m.group(3)).intValue();
					ele = getElementsByTagFromParent(input, tagName);
					//input = ele[index];
					String index = "0";
					if(m.group(3)!=null){
						if("0".equals(m.group(3))||"first".equals(m.group(3))){
							index = "first";
						}else if(m.group(3).matches("\\d+")){
							index = String.valueOf(new Integer(m.group(3)).intValue());
						}else if(!"last".equals(m.group(3))){
							index = "first";
						}
					}else if(m.group(4)!=null){
						String[] mm = m.group(4).split("=");
						String name = mm[0].trim();
						String value = mm[1].trim();
						String findValue = null;
						boolean finded = false;
						for(String el:ele){
							if(el!=null){
								findValue = getAttributeByName(el,name);
								if(findValue!=null&&(findValue.trim()).equals(value)){
									input = el;
									finded = true;
									break;
								}
							}
						}
						if(!finded){
							input = "";
						}
						index = null;
					}else{
						index = "first";
					}
					if(index!=null){
						if("first".equals(index)){
							input = ele[0];
						}else if("last".equals(index)){
							input = ele[ele.length-1];
						}else{
							input = ele[new Integer(index).intValue()];
						}
					}
				}
			}
			if(input!=null&&"".equals(input.trim())){
				input = null;
			}
			return input;
		}catch(Exception e){
			return null;
		}
	}

	/**
	 * 在文档中获取根元素的所有属性集合.
	 * @return HashMap
	 */
	public LinkedHashMap<String,String> getRootAttributes(){
		LinkedHashMap<String,String> hm = new LinkedHashMap<String,String>();
		Pattern p = Pattern.compile("<[^>]+>");
		Matcher m = p.matcher(this.xmlString);
		String tmp = m.find()?m.group():"";
		p = Pattern.compile("(\\w+)\\s*=\\s*\"([^\"]+)\"");
		m = p.matcher(tmp);
		while(m.find()){
			hm.put(m.group(1).trim(),m.group(2).trim());
		}
		return hm;
	}
	
	/**
	 * 在文档中获取根元素指定属性的值.
	 * @param attributeName String
	 * @return HashMap
	 */
	public String getRootAttributeByName(String attributeName){
		Pattern p = Pattern.compile("<[^>]+>");
		Matcher m = p.matcher(this.xmlString);
		String tmp = m.find()?m.group():"";
		p = Pattern.compile("(\\w+)\\s*=\\s*\"([^\"]+)\"");
		m = p.matcher(tmp);
		while(m.find()){
			if(m.group(1).trim().equals(attributeName))
				return m.group(2).trim();
		}
		return "";
	}
	
	/**
	 * 在给定的元素中获取所有属性的集合.该元素应该是一个合法的xml字符串可以从本类的其它方法中获取
	 * @param elementString String
	 * @return HashMap
	 */
	public static LinkedHashMap<String,String> getAttributes(String elementString){
		LinkedHashMap<String,String> hm = new LinkedHashMap<String,String>();
		Pattern p = Pattern.compile("<[^>]+>");
		Matcher m = p.matcher(elementString);
		String tmp = m.find()?m.group():"";
		p = Pattern.compile("(\\w+)\\s*=\\s*\"([^\"]+)\"");
		m = p.matcher(tmp);
		while(m.find()){
			hm.put(m.group(1).trim(),m.group(2).trim());
		}
		return hm;
	}

	/**
	 * 在给定的元素中获取指定属性的值.该元素应该是一个合法的xml字符串可以从本类的其它方法中获取
	 * @param elementString String
	 * @param attributeName String
	 * @return String
	 */
	public static String getAttributeByName(String elementString,String attributeName){
		Pattern p = Pattern.compile("<[^>]+>");
		Matcher m = p.matcher(elementString);
		String tmp = m.find()?m.group():"";
		p = Pattern.compile("(\\w+)\\s*=\\s*\"([^\"]+)\"");
		m = p.matcher(tmp);
		while(m.find()){
			if(m.group(1).trim().equals(attributeName))
				return m.group(2).trim();
		}
		return "";
	}

	/**
	 * 在文档中获取根元素的文本内容（如果存在子元素，则返回空字符串）.
	 * @param elementString String
	 * @return String
	 */
	public String getRootElementText(){
		Pattern p = Pattern.compile(">([^<>]*)<");
		Matcher m = p.matcher(this.xmlString);
		if(m.find()){
			return m.group(1);
		}
		return "";
	}
	
	/**
	 * 获取指定元素的文本内容（如果存在子元素，则返回空字符串）.该元素应该是一个合法的xml字符串可以从本类的其它方法中获取
	 * @param elementString String
	 * @return String
	 */
	public static String getElementText(String elementString){
		Pattern p = Pattern.compile(">([^<>]*)<");
		Matcher m = p.matcher(elementString);
		if(m.find()){
			return m.group(1);
		}
		return "";
	}
	
	public static void main(String[] args){
	}
}
