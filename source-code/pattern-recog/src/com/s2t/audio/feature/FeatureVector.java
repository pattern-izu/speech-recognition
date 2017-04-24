package com.s2t.audio.feature;

import java.io.Serializable;

@SuppressWarnings("serial")
public class FeatureVector implements Serializable {

	private double[][] mfccFeature;
	private double[][] featureVector;
	
	public FeatureVector() {
	}

	public double[][] getMfccFeature() {
		return mfccFeature;
	}

	public void setMfccFeature(double[][] mfccFeature) {
		this.mfccFeature = mfccFeature;
	}

	public int getNoOfFrames() {
		return featureVector.length;
	}

	public void setNoOfFrames(int noOfFrames) {
	}

	public int getNoOfFeatures() {
		return featureVector[0].length;
	}

	public void setNoOfFeatures(int noOfFeatures) {
	}

	public double[][] getFeatureVector() {
		return featureVector;
	}
	
	public double[] getFeatureVector(int k) {
		return featureVector[k];
	}

	public void setFeatureVector(double[][] featureVector) {
		this.featureVector = featureVector;
	}
}