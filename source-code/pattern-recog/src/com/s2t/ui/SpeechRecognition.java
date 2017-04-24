package com.s2t.ui;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.s2t.ui.SpeechRecognition;
import com.s2t.util.ErrorManager;
import com.s2t.util.Utils;
import com.s2t.audio.JSoundCapture;
import com.s2t.file.TrainingTestingWaveFiles;

@SuppressWarnings("serial")
public class SpeechRecognition extends JFrame {

	private JPanel jContentPane = null;
	private JSoundCapture soundCapture = null;
	private JComboBox<String> wordsComboBoxAddWord = null;
	private JTabbedPane jTabbedPane = null;
	private JPanel trainPanel = null;
	private JTextField addWordToCombo = null;
	private JButton addWordToComboBtn = null;
	private JLabel statusLBLRecognize;
	private JLabel lblChooseAWord;
	private JLabel aboutLBL;
	private JLabel lblAddANew;

	public static void main( String[] args ) {
		try {
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName( ) );
		} catch ( Exception e ) {
			System.out.println( e.toString( ) );
		}
		SwingUtilities.invokeLater( new Runnable( ) {

			public void run( ) {
				SpeechRecognition test = new SpeechRecognition( );
				test.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

				test.setResizable( false );
				test.setVisible( true );
			}
		} );
	}

	public SpeechRecognition() {
		super( );
		initialize( );
		ErrorManager.setMessageLbl( getStatusLblRecognize( ) );
	}

	private void initialize( ) {
		this.setSize( 485, 335 );
		this.setContentPane( getJContentPane( ) );
		this.setTitle( "Isolated Word Speech Recognition" );
	}

	private JLabel getStatusLblRecognize( ) {
		if ( statusLBLRecognize == null ) {
			statusLBLRecognize = new JLabel( "N0" );
			statusLBLRecognize.setHorizontalAlignment( SwingConstants.CENTER );
			statusLBLRecognize.setBounds( 120, 51, 189, 68 );
		}
		return statusLBLRecognize;
	}

	private JSoundCapture getSoundCapture( ) {
		if ( soundCapture == null ) {
			soundCapture = new JSoundCapture( true, true );
			soundCapture.setBounds( 10, 10, 431, 74 );
		}
		return soundCapture;
	}

	private JLabel getAboutLBL( ) {
		if ( aboutLBL == null ) {
			aboutLBL = new JLabel( "Mert Yilmaz CAKIR" );
			aboutLBL.setHorizontalAlignment( SwingConstants.CENTER );
			aboutLBL.setFont( new Font( "Tahoma", Font.PLAIN, 11 ) );
			aboutLBL.setBounds( 10, 275, 449, 16 );
		}
		return aboutLBL;
	}

	private JPanel getJContentPane( ) {
		if ( jContentPane == null ) {
			jContentPane = new JPanel( );
			jContentPane.setLayout( null );
			jContentPane.add( getJTabbedPane( ) );
			jContentPane.add( getSoundCapture( ) );
			jContentPane.add( getAboutLBL( ) );
		}
		return jContentPane;
	}

	private JTextField getAddWordToCombo( ) {
		if ( addWordToCombo == null ) {
			addWordToCombo = new JTextField( );
			addWordToCombo.setBounds( new Rectangle( 10, 42, 202, 24 ) );
		}
		return addWordToCombo;
	}

	private JButton getAddWordToComboBtn( ) {
		if ( addWordToComboBtn == null ) {
			addWordToComboBtn = new JButton( "Add Word" );
			addWordToComboBtn.addActionListener( new ActionListener( ) {

				public void actionPerformed( ActionEvent e ) {
					String newWord = Utils.clean( getAddWordToCombo( ).getText( ) );
					boolean isAlreadyRegistered = false;
					if ( !newWord.isEmpty( ) ) {
						for ( int i = 0; i < getWordsComboBoxAddWord( ).getItemCount( ); i++ ) {
							if ( getWordsComboBoxAddWord( ).getItemAt( i ).toString( ).equalsIgnoreCase( newWord ) ) {
								isAlreadyRegistered = true;
								break;
							}
						}
						if ( !isAlreadyRegistered ) {
							getWordsComboBoxAddWord( ).repaint( );
							getAddWordToCombo( ).setText( "" );
						}
					}
				}
			} );
			addWordToComboBtn.setBounds( new Rectangle( 222, 42, 142, 24 ) );
		}
		return addWordToComboBtn;
	}

	private JLabel getLblChooseAWord( ) {
		if ( lblChooseAWord == null ) {
			lblChooseAWord = new JLabel( "Choose corresponding folder for save" );
			lblChooseAWord.setBounds( 11, 77, 325, 14 );
		}
		return lblChooseAWord;
	}

	private JLabel getLblAddANew( ) {
		if ( lblAddANew == null ) {
			lblAddANew = new JLabel( "Add a new Word" );
			lblAddANew.setBounds( 11, 11, 126, 14 );
		}
		return lblAddANew;
	}

	private JPanel getAddSamplePanel( ) {
		if ( trainPanel == null ) {
			trainPanel = new JPanel( );
			trainPanel.setLayout( null );
			trainPanel.add( getWordsComboBoxAddWord( ), null );
			trainPanel.add( getAddWordToCombo( ), null );
			trainPanel.add( getAddWordToComboBtn( ), null );
			trainPanel.add( getLblChooseAWord( ) );
			trainPanel.add( getLblAddANew( ) );
		}
		return trainPanel;
	}

	private JTabbedPane getJTabbedPane( ) {
		if ( jTabbedPane == null ) {
			jTabbedPane = new JTabbedPane( );
			jTabbedPane.setBounds( new Rectangle( 10, 94, 449, 178 ) );
			//			"Verify Word" Module to be make
			jTabbedPane.addTab( "Add Sample", null, getAddSamplePanel( ), null );
			//			"HMM Train" Module to be make
			jTabbedPane.addChangeListener( new ChangeListener( ) {

				@Override
				public void stateChanged( ChangeEvent e ) {
					System.out.println( "state changed" );
					if ( jTabbedPane.getSelectedIndex( ) == 0 ) {
						soundCapture.setSaveFileName( null );
					} else if ( jTabbedPane.getSelectedIndex( ) == 1 ) {
						soundCapture.setSaveFileName( "TrainWav" + File.separator + getWordsComboBoxAddWord( ).getSelectedItem( ) );
					}

				}
			} );
		}
		return jTabbedPane;
	}

	@SuppressWarnings("rawtypes")
	private JComboBox getWordsComboBoxAddWord( ) {
		if ( wordsComboBoxAddWord == null ) {
			TrainingTestingWaveFiles ttwf = new TrainingTestingWaveFiles( "train" );
			wordsComboBoxAddWord = new JComboBox<String>( );
			try {
				List< String > regs = ttwf.readWordWavFolder( );
				for ( int i = 0; i < regs.size( ); i++ ) {
					wordsComboBoxAddWord.addItem( regs.get( i ) );
				}
			} catch ( Exception e ) {
			}
			wordsComboBoxAddWord.setBounds( new Rectangle( 11, 103, 202, 24 ) );
			wordsComboBoxAddWord.addItemListener( new ItemListener( ) {

				@Override
				public void itemStateChanged( ItemEvent e ) {
					String selectedItem = (String) getWordsComboBoxAddWord( ).getSelectedItem( );
					soundCapture.setFileName("TrainWav" + File.separator + selectedItem);
					soundCapture.setSaveFileName( "TrainWav" + File.separator + selectedItem + File.separator + selectedItem );
				}
			} );
		}
		return wordsComboBoxAddWord;
	}


}
