package com.agfa.med.EIPS;

public class SDPair {
	private String status;
	private int studies;
	private String validated;
	
	public SDPair() {
		
	}
	
	public SDPair(String status,int studies,String validated) {
		super();
		this.status = status;
		this.studies = studies;
		this.validated = validated;
	}
	
	public String getStatus() {
		return status;
	}
	
	public int getStudies() {
		return studies;
	}
	
	public String getValidated() {
		return validated;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setStudies(int studies) {
		this.studies = studies;
	}
	
	public void setValidated(String validated) {
		this.validated = validated;
	}
	
	@Override
	public String toString() {
		return status + " " + studies + " " + validated;
	}
	
}
