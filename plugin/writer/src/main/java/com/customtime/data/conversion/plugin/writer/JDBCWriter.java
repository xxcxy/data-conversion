package com.customtime.data.conversion.plugin.writer;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.customtime.data.conversion.plugin.annotaion.AOPExecuteType;
import com.customtime.data.conversion.plugin.annotaion.AOPMethod;
import com.customtime.data.conversion.plugin.annotaion.TMProperty;
import com.customtime.data.conversion.plugin.recode.Recode;
import com.customtime.data.conversion.plugin.recode.RecodesKeeper;

public class JDBCWriter extends AbstractWriter{
	private static Log logger = LogFactory.getLog(JDBCWriter.class);
	@TMProperty
	private String tableName;
	@TMProperty
	private String colNames;
	@TMProperty
	private String fieldOrders;
	@TMProperty
	private String preSql;
	@TMProperty
	private String finshSql;
	@TMProperty
	private String connUrl;
	@TMProperty
	private String userName;
	@TMProperty
	private String password;
	@TMProperty
	private String driverString;
	@TMProperty
	private int batchRows;
	private String[] cols;
	private int[] fields;
	private Connection conn;

	public void init() {
		try {
			Driver d = (Driver)Class.forName(driverString,true,Thread.currentThread().getContextClassLoader()).newInstance();
			DriverManager.registerDriver(new DriverShim(d));
			conn = DriverManager.getConnection(connUrl,userName,password);
			cols = colNames.split(",");
			String[] fieldst = fieldOrders.split(",");
			fields = new int[fieldst.length];
			for(int i=0;i<fields.length;i++){
				fields[i]= Integer.parseInt(fieldst[i]);
			}
		} catch (ClassNotFoundException e) {
			logger.error("Can not find the driver class to load the driver failed! Confirm driverString deploy Correct!");
			e.printStackTrace();
		} catch (SQLException e) {
			logger.error("Database connection failed!");
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void destroy() {
		if(conn!=null){
			try{
				if(!conn.isClosed())
					conn.close();
			}catch(SQLException e){
				logger.error("close connection error!");
			}
		}
	}

	public void writing(RecodesKeeper rk) {
		StringBuffer sql=  new StringBuffer("insert into ").append(tableName).append("(");
		int len = cols.length;
		for(int i=0;i<len;i++){
			sql.append(cols[i]);
			if(i<len-1)
				sql.append(",");
			else
				sql.append(")");
		}
		sql.append(" values(");
		for(int i=0;i<len;i++){
			sql.append("?");
			if(i<len-1)
				sql.append(",");
			else
				sql.append(")");
		}
		PreparedStatement ps =null; 
		try {
			conn.setAutoCommit(false);
			ps = conn.prepareStatement(sql.toString()); 
			int rowNum = 0;
			Recode recode;
			while((recode = rk.arising(this))!=null){
				for(int i=0,leng=fields.length;i<leng;i++)
					ps.setString(i+1, recode.getBlock(fields[i]));
				rowNum++;
				ps.addBatch();
				if(rowNum%batchRows==0){
					logger.info("line num: "+rowNum+" timestemp:"+System.currentTimeMillis());
					ps.executeBatch();
					conn.commit();
					logger.info("line num: "+rowNum+" timestemp:"+System.currentTimeMillis());
				}
			}
			ps.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			if(ps!=null)
				try{
					if(!ps.isClosed())
						ps.close();
				}catch(SQLException sqle){
					
				}
		}
	}
	@AOPMethod(AOPExecuteType.POST)
	public void post(){
		logger.info(finshSql);
	}
	@AOPMethod(AOPExecuteType.PRE)
	public void pre(){
		logger.info(preSql);
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
