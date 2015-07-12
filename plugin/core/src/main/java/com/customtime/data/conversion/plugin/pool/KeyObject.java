package com.customtime.data.conversion.plugin.pool;

import java.io.Serializable;


public class KeyObject implements Serializable,Comparable<KeyObject>{
	public int compareTo(KeyObject o) {
		if(o==null)
			return -1;
		if(o.m1>m1)
			return -1;
		else if(o.m1<m1)
			return 1;
		else if(o.m2>m2)
			return -1;
		else if(o.m2<m2)
			return 1;
		return 0;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -8013098444072026528L;
	private String equString;
	private Long m1;
	private Long m2;
	private Long m3;
	private Long m4;
	
	public KeyObject(){
		super();
		equString="";
		m1=0l;
		m2=0l;
		m3=0l;
		m4=0l;
	}
	public KeyObject(String key,Long m1,Long m2){
		this.equString=key;
		this.m1=m1;
		this.m2=m2;
	}
	public String getEquString() {
		return equString;
	}
	public void setEquString(String equString) {
		this.equString = equString;
	}
	public Long getM1() {
		return m1;
	}
	public void setM1(Long m1) {
		this.m1 = m1;
	}
	public Long getM2() {
		return m2;
	}
	public void setM2(Long m2) {
		this.m2 = m2;
	}
	
	public Long getM3() {
		return m3;
	}
	public void setM3(Long m3) {
		this.m3 = m3;
	}
	public Long getM4() {
		return m4;
	}
	public void setM4(Long m4) {
		this.m4 = m4;
	}
	public int hashCode() {
		/*final int prime = 31;
		int result = 1;
		result = prime * result + ((m2 == null) ? 0 : m2.hashCode());
		result = prime * result + ((equString == null) ? 0 : equString.hashCode());
		result = prime * result
				+ ((m1 == null) ? 0 : m1.hashCode());*/
		return this.toString().hashCode();
	}

	public String toString(){
		return equString+"|"+m1+m2+m3+m4;
	}
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyObject other = (KeyObject) obj;
		if (m2 == null) {
			if (other.m2 != null)
				return false;
		} else if (!m2.equals(other.m2))
			return false;
		if (equString == null) {
			if (other.equString != null)
				return false;
		} else if (!equString.equals(other.equString))
			return false;
		if (m1 == null) {
			if (other.m1 != null)
				return false;
		} else if (!m1.equals(other.m1))
			return false;
		if (m3 == null) {
			if (other.m3 != null)
				return false;
		} else if (!m3.equals(other.m3))
			return false;
		if (m4 == null) {
			if (other.m4 != null)
				return false;
		} else if (!m4.equals(other.m4))
			return false;
		return true;
	}
	
}
