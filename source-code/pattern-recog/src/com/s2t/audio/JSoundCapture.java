package com.s2t.audio;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Vector;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import com.s2t.util.MessageType;

public class JSoundCapture extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	byte[] audioBytes = null;
	float[] audioData = null;
	final int BUFFER_SIZE = 16384;
	int counter = 0;
	FormatControlConf formatControls = new FormatControlConf( );
	Capture capture = new Capture( );
	Playback playback = new Playback( );
	WaveData wd;
	AudioInputStream audioInputStream;
	SamplingGraph samplingGraph;
	JButton playB, captB, pausB;
	JButton saveB;
	String errStr;
	double duration, seconds;
	File file;
	Vector< Line2D.Double > lines = new Vector< Line2D.Double >( );
	boolean isDrawingRequired;
	boolean isSaveRequired;
	JPanel innerPanel;
	String saveFileName = null;
	String fileName = null;

	public JSoundCapture( boolean isDrawingRequired, boolean isSaveRequired ) {
		wd = new WaveData( );
		this.isDrawingRequired = isDrawingRequired;
		this.isSaveRequired = isSaveRequired;
		setLayout( new BorderLayout( ) );
		setBorder( new EmptyBorder( 1, 1, 1, 1 ) );

		innerPanel = new JPanel( );
		innerPanel.setLayout( new BoxLayout( innerPanel, BoxLayout.X_AXIS ) );

		JPanel buttonsPanel = new JPanel( );
		buttonsPanel.setPreferredSize( new Dimension( 200, 50 ) );
		buttonsPanel.setBorder( new EmptyBorder( 5, 0, 1, 0 ) );
		playB = addButton( "Play", buttonsPanel, false );
		captB = addButton( "Record", buttonsPanel, true );
		pausB = addButton( "Pause", buttonsPanel, false );
		saveB = addButton( "Save ", buttonsPanel, false );
		innerPanel.add( buttonsPanel );

		if ( isDrawingRequired ) {
			JPanel samplingPanel = new JPanel( new BorderLayout( ) );
			EmptyBorder eb = new EmptyBorder( 2, 2, 2, 2 );
			SoftBevelBorder sbb = new SoftBevelBorder( SoftBevelBorder.LOWERED );
			samplingPanel.setBorder( new CompoundBorder( eb, sbb ) );
			samplingPanel.add( samplingGraph = new SamplingGraph( ) );
			innerPanel.add( samplingPanel );
		}
		JPanel completePanel = new JPanel( );
		completePanel.setLayout( new BoxLayout( completePanel, BoxLayout.X_AXIS ) );
		completePanel.add( innerPanel );
		add( completePanel );
	}

	public boolean isSoundDataAvailable( ) {
		if ( audioBytes != null )
			return ( audioBytes.length > 100 );
		else
			return false;
	}

	public byte[] getAudioBytes( ) {
		return audioBytes;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSaveFileName( ) {
		return saveFileName;
	}

	public void setSaveFileName( String saveFileName) {
		this.saveFileName = saveFileName;
		System.out.println( "FileName Changed !!! " + File.separator + saveFileName );
	}

	public float[] getAudioData( ) throws Exception {
		if ( audioData == null ) {
			audioData = wd.extractFloatDataFromAudioInputStream( audioInputStream );
		}
		return audioData;
	}

	public void setAudioData( float[] audioData ) {
		this.audioData = audioData;
	}

	private JButton addButton( String name, JPanel p, boolean state ) {
		JButton b = new JButton( name );
		b.setPreferredSize( new Dimension( 85, 24 ) );
		b.addActionListener( this );
		b.setEnabled( state );
		b.setFocusable( false );
		p.add( b );
		return b;
	}

	public void actionPerformed( ActionEvent e ) {
		System.out.println( "actionPerformed *********" );
		Object obj = e.getSource( );
		if ( isSaveRequired && obj.equals( saveB ) ) {

			try {
				getFileNameAndSaveFile( );

			} catch ( Exception e2 ) {
				reportStatus( "Error in savng file " + e2.getMessage( ), MessageType.ERROR );
			}

		} else if ( obj.equals( playB ) ) {
			if ( playB.getText( ).startsWith( "Play" ) ) {
				playCaptured( );
			} else {
				stopPlaying( );
			}
		} else if ( obj.equals( captB ) ) {
			if ( captB.getText( ).startsWith( "Record" ) ) {
				startRecord( );
			} else {
				stopRecording( );
			}
		} else if ( obj.equals( pausB ) ) {
			if ( pausB.getText( ).startsWith( "Pause" ) ) {
				pausePlaying( );
			} else {
				resumePlaying( );
			}
		}
	}

	public void playCaptured( ) {
		playback.start( );
		if ( isDrawingRequired )
			samplingGraph.start( );
		captB.setEnabled( false );
		pausB.setEnabled( true );
		playB.setText( "Stop" );
	}

	public void stopPlaying( ) {
		playback.stop( );
		if ( isDrawingRequired )
			samplingGraph.stop( );
		captB.setEnabled( true );
		pausB.setEnabled( false );
		playB.setText( "Play" );
	}

	public void startRecord( ) {
		file = null;
		capture.start( );
		if ( isDrawingRequired )
			samplingGraph.start( );
		playB.setEnabled( false );
		pausB.setEnabled( true );
		saveB.setEnabled( false );
		captB.setText( "Stop" );
	}

	public void stopRecording( ) {
		lines.removeAllElements( );
		capture.stop( );
		if ( isDrawingRequired )
			samplingGraph.stop( );
		playB.setEnabled( true );
		pausB.setEnabled( false );
		saveB.setEnabled( true );
		captB.setText( "Record" );
	}

	public void pausePlaying( ) {

		if ( capture.thread != null ) {
			capture.line.stop( );
		} else {
			if ( playback.thread != null ) {
				playback.line.stop( );
			}
		}
		pausB.setText( "Resume" );

	}

	public void resumePlaying( ) {
		if ( capture.thread != null ) {
			capture.line.start( );
		} else {
			if ( playback.thread != null ) {
				playback.line.start( );
			}
		}
		pausB.setText( "Pause" );
	}

	public void getFileNameAndSaveFile( ) throws Exception {
		while ( saveFileName == null ) {
			saveFileName = JOptionPane.showInputDialog( null, "Enter WAV File Name", "Getting File Name" );
		}
		wd.saveToFile( fileName, saveFileName, AudioFileFormat.Type.WAVE, audioInputStream );

	}

	public void createAudioInputStream( File file, boolean updateComponents ) {
		if ( file != null && file.isFile( ) ) {
			try {
				this.file = file;
				errStr = null;
				audioInputStream = AudioSystem.getAudioInputStream( file );
				playB.setEnabled( true );
				long milliseconds = ( long ) ( ( audioInputStream.getFrameLength( ) * 1000 ) / audioInputStream.getFormat( ).getFrameRate( ) );
				duration = milliseconds / 1000.0;

				saveB.setEnabled( true );
				if ( updateComponents ) {
					formatControls.setFormat( audioInputStream.getFormat( ) );
					if ( isDrawingRequired )
						samplingGraph.createWaveForm( null );
				}
			} catch ( Exception ex ) {
				reportStatus( ex.toString( ), MessageType.ERROR );
			}
		} else {
			reportStatus( "Audio file required.", MessageType.INFO );
		}
	}

	private void reportStatus( String msg, MessageType type ) {
		if ( ( errStr = msg ) != null ) {
			System.out.println( errStr );
			if ( isDrawingRequired )
				samplingGraph.repaint( );
		}
	}

	public class Playback implements Runnable {

		SourceDataLine line;
		Thread thread;

		public void start( ) {
			errStr = null;
			thread = new Thread( this );
			thread.setName( "Playback" );
			thread.start( );
		}

		public void stop( ) {
			thread = null;
		}

		private void shutDown( String message ) {
			if ( ( errStr = message ) != null ) {
				System.err.println( errStr );
				if ( isDrawingRequired )
					samplingGraph.repaint( );
			}
			if ( thread != null ) {
				thread = null;
				if ( isDrawingRequired )
					samplingGraph.stop( );
				captB.setEnabled( true );
				pausB.setEnabled( false );
				playB.setText( "Play" );
			}
		}

		public void run( ) {

			if ( file != null ) {
				createAudioInputStream( file, false );
			}

			if ( audioInputStream == null ) {
				shutDown( "No loaded audio to play back" );
				return;
			}
			try {
				audioInputStream.reset( );
			} catch ( Exception e ) {
				shutDown( "Unable to reset the stream\n" + e );
				return;
			}

			AudioFormat format = formatControls.getFormat( );
			AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream( format, audioInputStream );

			if ( playbackInputStream == null ) {
				shutDown( "Unable to convert stream of format " + audioInputStream + " to format " + format );
				return;
			}

			DataLine.Info info = new DataLine.Info( SourceDataLine.class, format );
			if ( !AudioSystem.isLineSupported( info ) ) {
				shutDown( "Line matching " + info + " not supported." );
				return;
			}

			try {
				line = ( SourceDataLine ) AudioSystem.getLine( info );
				line.open( format, BUFFER_SIZE );
			} catch ( LineUnavailableException ex ) {
				shutDown( "Unable to open the line: " + ex );
				return;
			}

			int frameSizeInBytes = format.getFrameSize( );
			int bufferLengthInFrames = line.getBufferSize( ) / 8;
			int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
			byte[] data = new byte[ bufferLengthInBytes ];
			int numBytesRead = 0;

			line.start( );

			while ( thread != null ) {
				try {
					if ( ( numBytesRead = playbackInputStream.read( data ) ) == -1 ) {
						break;
					}
					int numBytesRemaining = numBytesRead;
					while ( numBytesRemaining > 0 ) {
						numBytesRemaining -= line.write( data, 0, numBytesRemaining );
					}
				} catch ( Exception e ) {
					shutDown( "Error during playback: " + e );
					break;
				}
			}
			if ( thread != null ) {
				line.drain( );
			}
			line.stop( );
			line.close( );
			line = null;
			shutDown( null );
		}
	}

	class Capture implements Runnable {

		TargetDataLine line;
		Thread thread;

		public void start( ) {
			errStr = null;
			thread = new Thread( this );
			thread.setName( "Capture" );
			thread.start( );
		}

		public void stop( ) {
			thread = null;
		}

		private void shutDown( String message ) {
			if ( ( errStr = message ) != null && thread != null ) {
				thread = null;
				if ( isDrawingRequired )
					samplingGraph.stop( );

				playB.setEnabled( true );
				pausB.setEnabled( false );
				saveB.setEnabled( true );
				captB.setText( "Record" );
				if ( isDrawingRequired )
					samplingGraph.repaint( );
			}
		}

		public void run( ) {

			duration = 0;
			audioInputStream = null;

			AudioFormat format = formatControls.getFormat( );
			DataLine.Info info = new DataLine.Info( TargetDataLine.class, format );

			if ( !AudioSystem.isLineSupported( info ) ) {
				shutDown( "Line matching " + info + " not supported." );
				return;
			}

			try {
				line = ( TargetDataLine ) AudioSystem.getLine( info );
				line.open( format, line.getBufferSize( ) );
			} catch ( LineUnavailableException ex ) {
				shutDown( "Unable to open the line: " + ex );
				return;
			} catch ( SecurityException ex ) {
				shutDown( ex.toString( ) );
				return;
			} catch ( Exception ex ) {
				shutDown( ex.toString( ) );
				return;
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream( );
			int frameSizeInBytes = format.getFrameSize( );
			int bufferLengthInFrames = line.getBufferSize( ) / 8;
			int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
			byte[] data = new byte[ bufferLengthInBytes ];
			int numBytesRead;

			line.start( );

			while ( thread != null ) {
				if ( ( numBytesRead = line.read( data, 0, bufferLengthInBytes ) ) == -1 ) {
					break;
				}
				out.write( data, 0, numBytesRead );
			}

			line.stop( );
			line.close( );
			line = null;

			try {
				out.flush( );
				out.close( );
			} catch ( IOException ex ) {
				reportStatus( "Error on inputstream  " + ex.getMessage( ), MessageType.ERROR );
			}

			audioBytes = out.toByteArray( );
			System.out.println( out.size( ) );
			ByteArrayInputStream bais = new ByteArrayInputStream( audioBytes );
			audioInputStream = new AudioInputStream( bais, format, audioBytes.length / frameSizeInBytes );

			long milliseconds = ( long ) ( ( audioInputStream.getFrameLength( ) * 1000 ) / format.getFrameRate( ) );
			duration = milliseconds / 1000.0;

			try {
				audioInputStream.reset( );
			} catch ( Exception ex ) {
				reportStatus( "Error in reseting inputStream " + ex.getMessage( ), MessageType.ERROR );
			}
			try {
				if ( isDrawingRequired ) {
					samplingGraph.createWaveForm( audioBytes );
				}

			} catch ( Exception e ) {
				reportStatus( "Error in drawing waveform " + e.getMessage( ), MessageType.ERROR );
			}

		}
	}

	class SamplingGraph extends JPanel implements Runnable {

		private static final long	serialVersionUID	= 1L;

		private Thread thread;
		private Font font12 = new Font( "serif", Font.PLAIN, 12 );
		Color jfcBlue = new Color( 204, 204, 255 );
		Color pink = new Color( 255, 175, 175 );
		AudioFormat format;

		public SamplingGraph( ) {
			setBackground( new Color( 20, 20, 20 ) );
		}

		public void createWaveForm( byte[] audioBytes ) throws Exception {

			lines.removeAllElements( );

			Dimension d = getSize( );
			int w = d.width;
			int h = d.height - 15;
			audioData = null;
			audioData = wd.extractFloatDataFromAudioInputStream( audioInputStream );
			int frames_per_pixel = wd.getAudioBytes( ).length / wd.getFormat( ).getFrameSize( ) / w;
			byte my_byte = 0;
			double y_last = 0;
			int numChannels = wd.getFormat( ).getChannels( );
			for ( double x = 0; x < w && audioData != null; x++ ) {
				int idx = ( int ) ( frames_per_pixel * numChannels * x );
				if ( wd.getFormat( ).getSampleSizeInBits( ) == 8 ) {
					my_byte = ( byte ) audioData[ idx ];
				} else {
					my_byte = ( byte ) ( 128 * audioData[ idx ] / 32768 );
				}
				double y_new = ( double ) ( h * ( 128 - my_byte ) / 256 );
				lines.add( new Line2D.Double( x, y_last, x, y_new ) );
				y_last = y_new;
			}
			repaint( );
		}

		public void paint( Graphics g ) {

			Dimension d = getSize( );
			int w = d.width;
			int h = d.height;
			int INFOPAD = 15;

			Graphics2D g2 = ( Graphics2D ) g;
			g2.setBackground( getBackground( ) );
			g2.clearRect( 0, 0, w, h );
			g2.setColor( Color.white );
			g2.fillRect( 0, h - INFOPAD, w, INFOPAD );

			if ( errStr != null ) {
				g2.setColor( jfcBlue );
				g2.setFont( new Font( "serif", Font.BOLD, 18 ) );
				g2.drawString( "ERROR", 5, 20 );
				AttributedString as = new AttributedString( errStr );
				as.addAttribute( TextAttribute.FONT, font12, 0, errStr.length( ) );
				AttributedCharacterIterator aci = as.getIterator( );
				FontRenderContext frc = g2.getFontRenderContext( );
				LineBreakMeasurer lbm = new LineBreakMeasurer( aci, frc );
				float x = 5, y = 25;
				lbm.setPosition( 0 );
				while ( lbm.getPosition( ) < errStr.length( ) ) {
					TextLayout tl = lbm.nextLayout( w - x - 5 );
					if ( !tl.isLeftToRight( ) ) {
						x = w - tl.getAdvance( );
					}
					tl.draw( g2, x, y += tl.getAscent( ) );
					y += tl.getDescent( ) + tl.getLeading( );
				}
			} else if ( capture.thread != null ) {
				g2.setColor( Color.black );
				g2.setFont( font12 );
				g2.drawString( "Length: " + String.valueOf( seconds ), 3, h - 4 );
			} else {
				g2.setColor( Color.black );
				g2.setFont( font12 );
				g2.drawString( "Length: " + String.valueOf( duration ) + "    Position: " + String.valueOf( seconds ), 3, h - 4 );

				if ( audioInputStream != null ) {
					g2.setColor( jfcBlue );
					for ( int i = 1; i < lines.size( ); i++ ) {
						g2.draw( ( Line2D ) lines.get( i ) );
					}

					if ( seconds != 0 ) {
						double loc = seconds / duration * w;
						g2.setColor( pink );
						g2.setStroke( new BasicStroke( 3 ) );
						g2.draw( new Line2D.Double( loc, 0, loc, h - INFOPAD - 2 ) );
					}
				}
			}
		}

		public void start( ) {
			thread = new Thread( this );
			thread.setName( "SamplingGraph" );
			thread.start( );
			seconds = 0;
		}

		public void stop( ) {
			if ( thread != null ) {
				thread.interrupt( );
			}
			thread = null;
		}

		@SuppressWarnings("static-access")
		public void run( ) {
			seconds = 0;
			while ( thread != null ) {
				if ( ( playback.line != null ) && ( playback.line.isOpen( ) ) ) {

					long milliseconds = ( long ) ( playback.line.getMicrosecondPosition( ) / 1000 );
					seconds = milliseconds / 1000.0;
				} else if ( ( capture.line != null ) && ( capture.line.isActive( ) ) ) {

					long milliseconds = ( long ) ( capture.line.getMicrosecondPosition( ) / 1000 );
					seconds = milliseconds / 1000.0;
				}

				try {
					thread.sleep( 100 );
				} catch ( Exception e ) {
					break;
				}

				repaint( );

				while ( ( capture.line != null && !capture.line.isActive( ) ) || ( playback.line != null && !playback.line.isOpen( ) ) ) {
					try {
						thread.sleep( 10 );
					} catch ( Exception e ) {
						break;
					}
				}
			}
			seconds = 0;
			repaint( );
		}
	}

	public static void main( String s[] ) {
		JSoundCapture capturePlayback = new JSoundCapture( true, true );
		JFrame f = new JFrame( "Capture/Playback/Save/Read for Speaker Data" );
		f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		f.getContentPane( ).add( "Center", capturePlayback );
		f.pack( );
		Dimension screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
		int w = 850;
		int h = 500;
		f.setLocation( screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2 );
		f.setSize( w, h );
		f.setResizable( false );
		f.setVisible( true );
	}
}
