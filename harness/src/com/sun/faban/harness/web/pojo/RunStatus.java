package com.sun.faban.harness.web.pojo;

import java.io.Serializable;

public enum RunStatus implements Serializable{
	STARTED,RECEIVED,COMPLETED,FAILED,KILLED,UNKNOWN;
	RunStatus(){}
}
