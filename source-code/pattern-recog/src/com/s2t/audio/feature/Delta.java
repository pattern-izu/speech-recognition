package com.s2t.audio.feature;

public class Delta {

	int M;

	public Delta() {
	}

	public void setRegressionWindow(int M) {
		this.M = M;
	}

	public double[][] performDelta2D(double[][] data) {
		int noOfMfcc = data[0].length;
		int frameCount = data.length;

		double mSqSum = 0;
		for (int i = -M; i < M; i++) {
			mSqSum += Math.pow(i, 2);
		}
		double delta[][] = new double[frameCount][noOfMfcc];


		if(frameCount<M){

			double [][]dataNew = new double [M][noOfMfcc];
			delta = new double[M][noOfMfcc];

			for ( int i = 0; i < frameCount; i++ ) {
				for ( int j = 0; j < noOfMfcc; j++ ) {
					dataNew[i][j] =  data[ i ][ j ] ;
				}
				System.out.println( );
			}

			for ( int i = frameCount; i < M; i++ ) {
				for ( int j = 0; j < noOfMfcc; j++ ) {
					dataNew[i][j] =  0;
				}
				System.out.println( );
			}

			frameCount = M;

			data = dataNew;
		}


		for (int i = 0; i < noOfMfcc; i++) {

			if(frameCount==1){
				System.out.println( "look into" );
			}

			for (int k = 0; k < M; k++) {
				delta[k][i] = data[k][i];
			}
			for (int k = frameCount - M; k < frameCount; k++) {
				delta[k][i] = data[k][i];
			}
			for (int j = M; j < frameCount - M; j++) {
				double sumDataMulM = 0;
				for (int m = -M; m <= +M; m++) {
					sumDataMulM += m * data[m + j][i];
				}
				delta[j][i] = sumDataMulM / mSqSum;
			}
		}

		return delta;
	}

	public double[] performDelta1D(double[] data) {
		int frameCount = data.length;

		double mSqSum = 0;
		for (int i = -M; i < M; i++) {
			mSqSum += Math.pow(i, 2);
		}
		double[] delta = new double[frameCount];

		for (int k = 0; k < M; k++) {
			delta[k] = data[k];
		}
		for (int k = frameCount - M; k < frameCount; k++) {
			delta[k] = data[k];
		}
		for (int j = M; j < frameCount - M; j++) {
			double sumDataMulM = 0;
			for (int m = -M; m <= +M; m++) {
				sumDataMulM += m * data[m + j];
			}
			delta[j] = sumDataMulM / mSqSum;
		}
		return delta;
	}
}