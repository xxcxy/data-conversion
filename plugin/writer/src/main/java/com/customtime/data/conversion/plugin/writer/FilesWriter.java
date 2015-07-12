package com.customtime.data.conversion.plugin.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.plugin.annotaion.CallMonitor;
import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;
import com.customtime.data.conversion.plugin.util.MonitorUtil;

public class FilesWriter extends AbstractWriter {

	private final static Log logger = LogFactory.getLog(FilesWriter.class);
	@TMProperty
	private String fileDir;
	@TMProperty
	private String fileName;
	@TMProperty
	private String cols;
	@TMProperty
	private String fieldSplit;
	@TMProperty(defaultValue = "false")
	private boolean fileAppend;
	@TMProperty
	private String collong;
	@TMProperty(defaultValue="UTF-8")
	private String encoding;
	@TMProperty(defaultValue = "0")
	private String fillmod;

	public void init() {
		logger.info("start write init");
	}

	public void destroy() {
		logger.info("destory write");
	}

	public void writing(RecodesKeeper rk) {
		logger.info("start writing");
		Recode recode;
		logger.info("FileWriter write works start!");
		if (fileDir != null && !fileDir.endsWith(File.separator))
			fileDir = fileDir + File.separator;
		PrintWriter pw=null;
		try {
			File nwFile = new File(fileDir);
			if (!nwFile.exists()) {
				try {
					nwFile.mkdirs();
				} catch (Exception e) {
					logger.info(fileDir + " is exists!");
				}
			}
			if(!new File(fileDir + fileName).exists()){
				new File(fileDir + fileName).createNewFile();
			}
			if (fileAppend) {
				pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileDir + fileName, true), encoding));
				pw = new PrintWriter(new FileWriter(fileDir + fileName, true));
			} else {
				pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileDir + fileName), encoding));
			}
			int flushSize = 0;
			String[] collongn = collong.split(",");
			String[] fillmodn = fillmod.split(",");
			while ((recode = rk.arising(this)) != null) {
				String str = "";
				int collongt = 0;
				String fillmodt = "0";
				int num = recode.getBlockNum();
				if(num<1)
					continue;
				if (StringUtils.isBlank(cols) || cols.equals("*")) {
					for (int i = 0; i < num; i++) {
						String curfield = recode.getBlock(i);
						if (curfield == null) {
							curfield = "";
						}
						if (!StringUtils.isBlank(fieldSplit)) {
							if (i == 0) {
								str = str + curfield;
							} else {
								str = str + fieldSplit + curfield;
							}
						} else {
							if (i < collongn.length) {
								collongt = Integer.parseInt(collongn[i]);
							} else {
								collongt = 0;
							}
							if (i < fillmodn.length) {
								fillmodt = fillmodn[i];
							} else {
								fillmodt = "0";
							}
							str = str+adjuststring(curfield, collongt, fillmodt);
						}
					}
				} else {
					String[] colsn = cols.split(",");
					for (int i = 0, len = colsn.length; i < len; i++) {
						int index = NumberUtils.toInt(colsn[i], -1);
						if (index != -1 && index < num) {
							String curfield = recode.getBlock(index);
							if (curfield == null) {
								curfield = "";
							}
							if (!StringUtils.isBlank(fieldSplit)) {
								if (i == 0) {
									str = str + curfield;
								} else {
									str = str + fieldSplit + curfield;
								}
							} else {
								if (i < collongn.length) {
									collongt = Integer.parseInt(collongn[i]);
								} else {
									collongt = 0;
								}
								if (i < fillmodn.length) {
									fillmodt = fillmodn[i];
								} else {
									fillmodt = "0";
								}

								str = str+ adjuststring(curfield, collongt,fillmodt);
							}
						}
					}
				}
				flushSize++;
				pw.println(str);
				if(flushSize%1024==0){
					pw.flush();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			pw.close();
		}
		logger.info("end writing");
	}

	private String adjuststring(String oldstr, int length, String fillmod) {
		// logger.info("oldstr:"+oldstr+"-"+Integer.toString(length)+"-"+fillmod);
		String newstr = oldstr;
		if (fillmod.equals("0")) {
			return newstr;
		}
		if (oldstr.length() >= length) {
			return newstr;
		}
		if (fillmod.equals("1")) {
			if (oldstr.substring(0, 1).equals("-")) {
				newstr = "-"
						+ StringUtils.leftPad(oldstr.substring(1), length - 1,
								"0");
			} else {
				newstr = StringUtils.leftPad(oldstr, length, "0");
			}
		}
		if (fillmod.equals("2")) {
			newstr = StringUtils.leftPad(oldstr, length, " ");
		}
		if (fillmod.equals("3")) {
			newstr = StringUtils.rightPad(oldstr, length, " ");
		}
		// logger.info("newstr:"+newstr);
		return newstr;
	}

	@CallMonitor
	public void monitor(MonitorUtil mu){
		logger.info("suceess add line:"+mu.getSuccessLine());
	}
}
