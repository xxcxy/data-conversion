package com.customtime.data.conversion.plugin.reader;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;

public class FileReader extends AbstractReader{

	private final static Log logger = LogFactory.getLog(FileReader.class);
	@TMProperty
	private String fileName;
	@TMProperty
	private int filetype;
	@TMProperty
	private String fileDir;
	@TMProperty
	private String cols;
	@TMProperty
	private String fieldSplit;
	@TMProperty
	private String collong;
	@TMProperty(defaultValue="UTF-8")
	private String encoding;
	private int[] collongs;
	private byte[] lineByte;
	private int[] filedType;//0-long,1-string,2-ip
	public void init(){
		if(filetype==2){
			String[] strs = collong.split(",");
			int len = strs.length;
			int byteLen = 0;
			collongs = new int[len];
			filedType = new int[len];
			for(int i=0;i<len;i++){
				String[] cf = strs[i].split(";");
				collongs[i] = Integer.parseInt(cf[0]);
				if(cf.length>1){
					if("ip".equals(cf[1]))
						filedType[i] = 2;
				}else{
					filedType[i] = 0;
				}
				byteLen +=collongs[i];
			}
			lineByte = new byte[byteLen];
		}
	}

	private ArrayList<String> stringsplit(String Readline,String[] colongn){
		ArrayList<String> result = new ArrayList<String>();
		for(int i=0,len=colongn.length;i<len;i++){
			int collong = Integer.parseInt(colongn[i]);
			if(Readline.length()<=collong){
				result.add(Readline);
				break;
			}
			result.add(Readline.substring(0,collong));
			Readline = Readline.substring(collong);
		}
		return result;
	}

	public void reading(RecodesKeeper rk) {
		logger.info("Filereader read works start!");
		if(fileDir != null && !fileDir.endsWith(File.separator))
			fileDir = fileDir+File.separator;
		File file = new File(fileDir+fileName);
		if(filetype==2){
			read4dat(rk,file);
		}else{
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding), 131072);
				String readline = br.readLine();
				while(null != readline){
					readline = readline.replace("\r","");
					Recode line = rk.newRecode();
					if(!StringUtils.isBlank(fieldSplit)){
						String[] colss = readline.split(fieldSplit);
						if(StringUtils.isBlank(cols) || cols.equals("*")){
							for(int i=0,len=colss.length;i<len;i++){
								line.putBlock(colss[i].trim());
							}
						}
						else
						{
							String[] colsn = cols.split(",");
							for(int i=0,len=colsn.length,colsslen=colss.length;i<len;i++){
								int index = NumberUtils.toInt(colsn[i],-1);
								if(index !=-1 && index<colsslen)
									line.putBlock(colss[index].trim());
							}
						}
					}
					else{
						String[] collongn = collong.split(",");					
						ArrayList<String> colss = stringsplit(readline,collongn);
						if(StringUtils.isBlank(cols) || cols.equals("*")){
							for(int i=0,len=colss.size();i<len;i++){
								line.putBlock(colss.get(i).trim());
							}
						}
						else
						{
							String[] colsn = cols.split(",");
							for(int i=0,len=colsn.length,colsslen=colss.size();i<len;i++){
								int index = NumberUtils.toInt(colsn[i],-1);
								if(index !=-1 && index<colsslen)
									line.putBlock(colss.get(index).trim());
							}
						}
					}
					rk.keeping(line, this);
					readline = br.readLine();
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
//				logger.error(ExceptionTracker.trace(e));
//				String message = String.format("Filereader failed: %s,%s",
//						e.getMessage(), e.getCause());
//				throw new DataExchangeException(message);
			}
		}
		logger.info("FileReader read works end!");	
	}

	private void read4dat(RecodesKeeper rk,File file){
		try {
			DataInputStream dis=new DataInputStream(new FileInputStream(file));
			int len = dis.read(lineByte);
			while(len>0){
				int index =0;
				Recode recode = rk.newRecode();
				for(int i=0,leng=collongs.length;i<leng;i++){
//					recode.putBlock(Long.toString(ByteToLong(lineByte,index,length)));
					if(filedType[i]==0)
						recode.putBlock(ByteToLong(lineByte,index,collongs[i])+"");
					else if(filedType[i]==2)
						recode.putBlock(ByteToIP(lineByte,index));
					index+=collongs[i];
				}
				rk.keeping(recode, this);
				len = dis.read(lineByte);
			}
			dis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public long ByteToLong(byte[] value,int index,int length) {
		long ret = 0;
		try {
			for (int i = index; i < value.length&&i<index+length; i++) {
				ret = (ret << 8) | (value[i] & 0xff);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public String ByteToIP(byte[] value,int index) {
		String ret = null;
		try {
			if ((value != null) && (value.length >= 4)) {
				ret = new StringBuffer().append(value[index] & 0xFF).append('.').append(  
						value[index+1] & 0xFF).append('.').append(value[index+2] & 0xFF)  
		                .append('.').append(value[index+3] & 0xFF).toString(); 
			}
		} catch (Exception e) {
		}
		return ret;
	}
	
	public String ByteToHex(byte[] buf, int begin, int end) {
		StringBuffer ret = new StringBuffer();
		try {
			for (int i = begin; i < end; i++) {
				String temp = Integer.toHexString(buf[i] & 0xff);
				while (temp.length() < 2)
					temp = "0" + temp;
				ret.append(temp);
			}
		} catch (Exception e) {
		}
		return ret.toString().toUpperCase();
	}
}
