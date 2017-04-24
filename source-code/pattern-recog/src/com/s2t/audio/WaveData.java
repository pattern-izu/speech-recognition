package com.s2t.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class WaveData {

	private byte[] arrayFile;
	private byte[] audioBytes;
	private float[] audioData;
	private FileOutputStream fileOS;
	private ByteArrayInputStream byteAIS;
	private AudioInputStream audioIS;
	private AudioFormat audioFormat;
	private double durationSeconds;
	private FileInputStream fis;

	public WaveData( ) {
	}

	public byte[] getAudioBytes( ) {
		return audioBytes;
	}

	public double getDurationSec( ) {
		return durationSeconds;
	}

	public float[] getAudioData( ) {
		return audioData;
	}

	public AudioFormat getFormat( ) {
		return audioFormat;
	}
	public float[] extractAmplitudeFromFile( File wavFile ) throws Exception {
		fis = new FileInputStream( wavFile );
		arrayFile = new byte[ ( int ) wavFile.length( ) ];
		fis.read( arrayFile );
		return extractAmplitudeFromFileByteArray( arrayFile );
	}

	public float[] extractAmplitudeFromFileByteArray( byte[] arrFile ) throws Exception {
		byteAIS = new ByteArrayInputStream( arrFile );
		return extractAmplitudeFromFileByteArrayInputStream( byteAIS );
	}

	public float[] extractAmplitudeFromFileByteArrayInputStream( ByteArrayInputStream bis ) throws Exception {
		audioIS = AudioSystem.getAudioInputStream( bis );
		float milliseconds = ( long ) ( ( audioIS.getFrameLength( ) * 1000 ) / audioIS.getFormat( ).getFrameRate( ) );
		durationSeconds = milliseconds / 1000.0;
		return extractFloatDataFromAudioInputStream( audioIS );
	}

	public float[] extractFloatDataFromAudioInputStream( AudioInputStream audioInputStream ) throws Exception {
		audioFormat = audioInputStream.getFormat( );
		audioBytes = new byte[ ( int ) ( audioInputStream.getFrameLength( ) * audioFormat.getFrameSize( ) ) ];
		float milliseconds = ( long ) ( ( audioInputStream.getFrameLength( ) * 1000 ) / audioInputStream.getFormat( ).getFrameRate( ) );
		durationSeconds = milliseconds / 1000.0;
		audioInputStream.read( audioBytes );
		return extractFloatDataFromAmplitudeByteArray( audioFormat, audioBytes );
	}

	public float[] extractFloatDataFromAmplitudeByteArray( AudioFormat format, byte[] audioBytes ) throws Exception {
		// convert
		audioData = null;
		if ( format.getSampleSizeInBits( ) == 16 ) {
			int nlengthInSamples = audioBytes.length / 2;
			audioData = new float[ nlengthInSamples ];
			if ( format.isBigEndian( ) ) {
				for ( int i = 0; i < nlengthInSamples; i++ ) {
					//					First byte is MSB (high order)
					int MSB = audioBytes[ 2 * i ];
					//					Second byte is LSB (low order)
					int LSB = audioBytes[ 2 * i + 1 ];
					audioData[ i ] = MSB << 8 | ( 255 & LSB );
				}
			} else {
				for ( int i = 0; i < nlengthInSamples; i++ ) {
					int LSB = audioBytes[ 2 * i ];
					int MSB = audioBytes[ 2 * i + 1 ];
					audioData[ i ] = MSB << 8 | ( 255 & LSB );
				}
			}
		} else if ( format.getSampleSizeInBits( ) == 8 ) {
			int nlengthInSamples = audioBytes.length;
			audioData = new float[ nlengthInSamples ];
			if ( format.getEncoding( ).toString( ).startsWith( "PCM_SIGN" ) ) {
				for ( int i = 0; i < audioBytes.length; i++ ) {
					audioData[ i ] = audioBytes[ i ];
				}
			} else {
				for ( int i = 0; i < audioBytes.length; i++ ) {
					audioData[ i ] = audioBytes[ i ] - 128;
				}
			}
		}
		return audioData;
	}

	public void saveToFile( String file, String name, AudioFileFormat.Type fileType, AudioInputStream audioInputStream ) throws Exception {

		System.out.println( "WaveData.saveToFile() " + name );

		File myFile = new File( file );
		if ( !myFile.exists( ) )
			myFile.mkdirs( );

		if ( audioInputStream == null ) {
			return;
		}
		audioInputStream.reset( );
		myFile = new File( name + ".wav" );
		int i = 0;
		while ( myFile.exists( ) ) {
			String temp = String.format( name + "%d", i++ );
			myFile = new File( temp + ".wav" );
		}
		if ( AudioSystem.write( audioInputStream, fileType, myFile ) == -1 ) {
		}
		System.out.println( myFile.getAbsolutePath( ) );
	}

	public void saveFileByteArray( String fileName, byte[] arrFile ) throws Exception {
		fileOS = new FileOutputStream( fileName );
		fileOS.write( arrFile );
		fileOS.close( );
		System.out.println( "WAV Audio data saved to " + fileName );
	}
}
