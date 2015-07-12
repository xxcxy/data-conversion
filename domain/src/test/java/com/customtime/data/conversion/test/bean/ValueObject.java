package com.customtime.data.conversion.test.bean;

import java.io.Serializable;

public class ValueObject implements Serializable{
	private static final long serialVersionUID = 1L;
	private String just;
	
	public ValueObject(String say){
		this.just=say;
	}

	public String getJust() {
		return just;
	}

	public void setJust(String just) {
		this.just = just;
	}
	
}
