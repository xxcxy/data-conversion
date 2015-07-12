package com.customtime.data.conversion.test.bean;

import java.io.Serializable;

public class KeyObject implements Serializable,Comparable<KeyObject>{
	
	public int compareTo(KeyObject ko) {
		if(ko.startTime>startTime)
			return -1;
		else if(ko.startTime<startTime)
			return 1;
		else if(ko.endTime>endTime)
			return -1;
		else if(ko.endTime<endTime)
			return 1;
		return 0;
	}
	private static final long serialVersionUID = 1L;
	private String ip;
	private Long startTime;
	private Long endTime;
	
	public KeyObject(String ip,Long startTime,Long endTime){
		this.ip=ip;
		this.startTime=startTime;
		this.endTime=endTime;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Long getStartTime() {
		return startTime;
	}
	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result
				+ ((startTime == null) ? 0 : startTime.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyObject other = (KeyObject) obj;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		return true;
	}
	
}
