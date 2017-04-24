package com.s2t.util;

import javax.swing.JLabel;

import com.s2t.util.ErrorManager;
import com.s2t.util.MessageType;

public class ErrorManager {

	private static ErrorManager em = new ErrorManager( );
	private static JLabel mlbl;

	private ErrorManager( ) {
	}

	public static ErrorManager getInstance( ) {
		return em;
	}

	public static void setMessageLbl( JLabel ilbl ) {
		if ( mlbl != null ) {
			mlbl = ilbl;
		}
	}

	public static void reportStatus( String msg, MessageType mt ) {
		mlbl.setText( msg );
	}
}
