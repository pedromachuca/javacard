package client;

import java.io.*;
import opencard.core.service.*;
import opencard.core.terminal.*;
import opencard.core.util.*;
import opencard.opt.util.*;

/*
CLA 	INS 	P1	P2	LC	DATA LE

CLA :Classe d'instruction - indique le type de la commande, par exemple "interindustry" ou "proprietary"

INS : Code d'instruction - indique le code de commande, "write data" par exemple

Paramètres d'instructions pour la commande, par exemple la position du curseur (offset) du fichier où écrire des données

LC : sur un octet taille des datas (0-127) normalement de (0 à 255) mais on est sur des signed byte donc 0 à 127

DATA : Data to send

LE : Taille d'octet de réponse attendu (0-127)

*/


public class TheClient {

	private PassThruCardService servClient = null;
	boolean DISPLAY = true;
	boolean loop = true;

	static final byte CLA					= (byte)0x00;
	static final byte P1					= (byte)0x00;
	static final byte P2					= (byte)0x00;
	static final byte UPDATECARDKEY				= (byte)0x14;
	static final byte UNCIPHERFILEBYCARD			= (byte)0x13;
	static final byte CIPHERFILEBYCARD			= (byte)0x12;
	static final byte CIPHERANDUNCIPHERNAMEBYCARD		= (byte)0x11;
	static final byte READFILEFROMCARD			= (byte)0x10;
	static final byte WRITEFILETOCARD			= (byte)0x09;
	static final byte UPDATEWRITEPIN			= (byte)0x08;
	static final byte UPDATEREADPIN				= (byte)0x07;
	static final byte DISPLAYPINSECURITY			= (byte)0x06;
	static final byte DESACTIVATEACTIVATEPINSECURITY	= (byte)0x05;
	static final byte ENTERREADPIN				= (byte)0x04;
	static final byte ENTERWRITEPIN				= (byte)0x03;
	static final byte READNAMEFROMCARD			= (byte)0x02;
	static final byte WRITENAMETOCARD			= (byte)0x01;


	public TheClient() {
		try {
			SmartCard.start();
			System.out.print( "Smartcard inserted?... " );

			CardRequest cr = new CardRequest (CardRequest.ANYCARD,null,null);

			SmartCard sm = SmartCard.waitForCard (cr);

			if (sm != null) {
				System.out.println ("got a SmartCard object!\n");
			} else
				System.out.println( "did not get a SmartCard object!\n" );

			this.initNewCard( sm );

			SmartCard.shutdown();

		} catch( Exception e ) {
			System.out.println( "TheClient error: " + e.getMessage() );
		}
		java.lang.System.exit(0) ;
	}

	private ResponseAPDU sendAPDU(CommandAPDU cmd) {
		return sendAPDU(cmd, true);
	}

	private ResponseAPDU sendAPDU( CommandAPDU cmd, boolean display ) {
		ResponseAPDU result = null;
		try {
			result = this.servClient.sendCommandAPDU( cmd );
			if(display)
				displayAPDU(cmd, result);
		} catch( Exception e ) {
			System.out.println( "Exception caught in sendAPDU: " + e.getMessage() );
			java.lang.System.exit( -1 );
		}
		return result;
	}


	/************************************************
	 * *********** BEGINNING OF TOOLS ***************
	 * **********************************************/


	private String apdu2string( APDU apdu ) {
		return removeCR( HexString.hexify( apdu.getBytes() ) );
	}


	public void displayAPDU( APDU apdu ) {
		System.out.println( removeCR( HexString.hexify( apdu.getBytes() ) ) + "\n" );
	}


	public void displayAPDU( CommandAPDU termCmd, ResponseAPDU cardResp ) {
		System.out.println( "--> Term: " + removeCR( HexString.hexify( termCmd.getBytes() ) ) );
		System.out.println( "<-- Card: " + removeCR( HexString.hexify( cardResp.getBytes() ) ) );
	}


	private String removeCR( String string ) {
		return string.replace( '\n', ' ' );
	}


	/******************************************
	 * *********** END OF TOOLS ***************
	 * ****************************************/


	private boolean selectApplet() {
		boolean cardOk = false;
		try {
			CommandAPDU cmd = new CommandAPDU( new byte[] {
				(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x0A,
				    (byte)0xA0, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x62,
				    (byte)0x03, (byte)0x01, (byte)0x0C, (byte)0x06, (byte)0x01
			} );
			ResponseAPDU resp = this.sendAPDU( cmd );
			if( this.apdu2string( resp ).equals( "90 00" ) )
				cardOk = true;
		} catch(Exception e) {
			System.out.println( "Exception caught in selectApplet: " + e.getMessage() );
			java.lang.System.exit( -1 );
		}
		return cardOk;
	}


	private void initNewCard( SmartCard card ) {
		if( card != null )
			System.out.println( "Smartcard inserted\n" );
		else {
			System.out.println( "Did not get a smartcard" );
			System.exit( -1 );
		}

		System.out.println( "ATR: " + HexString.hexify( card.getCardID().getATR() ) + "\n");


		try {
			this.servClient = (PassThruCardService)card.getCardService( PassThruCardService.class, true );
		} catch( Exception e ) {
			System.out.println( e.getMessage() );
		}

		System.out.println("Applet selecting...");
		if( !this.selectApplet() ) {
			System.out.println( "Wrong card, no applet to select!\n" );
			System.exit( 1 );
			return;
		} else
			System.out.println( "Applet selected" );

		mainLoop();
	}


	void updateCardKey() {
	}


	void uncipherFileByCard() {
	}


	void cipherFileByCard() {
	}


	void cipherAndUncipherNameByCard() {
	}


	void readFileFromCard() {
	}


	void writeFileToCard() {

	/*
	Envoyer :
	nom du fichier : file => 4 octets

	A stocker dans un tableau dans l'applet
	[taille du nom de fichier, filename, nbre d'apdu envoyé, taille du dernier apdu, dataApdu1(127 octets max) ]
	*/
		try{
			System.out.println( "Veuillez entrer le nom de fichier a sauvegarder:" );

			String filename = readKeyboard();
			int filenameLength= filename.length();

			FileInputStream inputstream=null;

			File file=null;
			byte[] filecontent=null;
			long fileLength=0;

			file = new File(filename);
			fileLength = file.length();

			inputstream = new FileInputStream(file);
			filecontent = new byte[(int)fileLength];
			int data = inputstream.read(filecontent);

			while(data != -1){
				data = inputstream.read(filecontent);
			}
			for (int i=0;i<(int)fileLength; i++) {
					System.out.println("test "+filecontent[i]);
			}
			System.out.println("filnameLength "+filenameLength);
			inputstream.close();
			//Boucle sur la comande envoi 2 par 2 des octets jusqu'au dernier
			//=>compteur sur le nombre d'apdu envoyé tant qu'il reste au moins 2 octet
			//Envoi de la taille du nom de fichier
			//Envoi du code ascii des caractères du nom de fichier
			byte[] cmd_5 = {CLA, WRITEFILETOCARD, P1, P2, (byte)filenameLength};
			CommandAPDU cmd = new CommandAPDU( cmd_5 );
			this.sendAPDU( cmd, DISPLAY );
			//Dernier send apdu 1 octet


		}catch(FileNotFoundException e){
			System.out.println(e.getMessage());
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
	}
//pas plus de 127 octets

	void updateWritePIN() {
		System.out.println( "Veuillez entrer le nouveau Write PIN:" );
		try {
			String writepin = readKeyboard();
			byte [] pinInByte = writepin.getBytes();
			byte sizePin = (byte)pinInByte.length;

			byte[] cmd_part1 = {CLA, UPDATEWRITEPIN, P1, P2, sizePin};
			int sizecmd_part1 = cmd_part1.length;
			int totalLength =sizecmd_part1+(int)sizePin;
			byte[] cmd_4= new byte[totalLength];

			System.arraycopy(cmd_part1, 0, cmd_4, 0, sizecmd_part1);
			System.arraycopy(pinInByte, 0, cmd_4, sizecmd_part1, (int)sizePin);
			CommandAPDU cmd = new CommandAPDU( cmd_4 );
			ResponseAPDU resp = this.sendAPDU( cmd, DISPLAY );

		} catch( Exception e ) {}

	}


	void updateReadPIN() {
			System.out.println( "Veuillez entrer le nouveau Read PIN:" );
			try {
				String readpin = readKeyboard();
				byte [] pinInByte = readpin.getBytes();
				byte sizePin = (byte)pinInByte.length;

				byte[] cmd_part1 = {CLA, UPDATEREADPIN, P1, P2, sizePin};
				int sizecmd_part1 = cmd_part1.length;
				int totalLength =sizecmd_part1+(int)sizePin;
				byte[] cmd_3= new byte[totalLength];

				System.arraycopy(cmd_part1, 0, cmd_3, 0, sizecmd_part1);
				System.arraycopy(pinInByte, 0, cmd_3, sizecmd_part1, (int)sizePin);
				CommandAPDU cmd = new CommandAPDU( cmd_3 );
				ResponseAPDU resp = this.sendAPDU( cmd, DISPLAY );

			} catch( Exception e ) {}
	}


	void displayPINSecurity() {
		byte[] cmd_ = {CLA, DISPLAYPINSECURITY, P1, P2, (byte)0x02};
		CommandAPDU cmd = new CommandAPDU( cmd_ );
		ResponseAPDU resp = this.sendAPDU( cmd, DISPLAY );
		byte[] bytes = resp.getBytes();
		if (bytes[0]==1) {
				System.out.println( "SECURITY ACTIVATED");
		}
		else{
				System.out.println( "SECURITY DEACTIVATED");
		}
	}


	void desactivateActivatePINSecurity() {
		byte[] cmd_= { CLA, DESACTIVATEACTIVATEPINSECURITY, P1, P2};
		CommandAPDU cmd = new CommandAPDU( cmd_ );
		this.sendAPDU( cmd, DISPLAY );
	}


	void enterReadPIN() {
		System.out.println( "Veuillez entrer le Read PIN:" );
		try {
			String pin = readKeyboard();
			byte [] pinInByte = pin.getBytes();
			byte sizePin = (byte)pinInByte.length;

			byte[] cmd_part1 = {CLA, ENTERREADPIN, P1, P2, sizePin};
			int sizecmd_part1 = cmd_part1.length;
			int totalLength =sizecmd_part1+(int)sizePin;
			byte[] cmd_2= new byte[totalLength];

			System.arraycopy(cmd_part1, 0, cmd_2, 0, sizecmd_part1);
			System.arraycopy(pinInByte, 0, cmd_2, sizecmd_part1, (int)sizePin);
			CommandAPDU cmd = new CommandAPDU( cmd_2 );
			ResponseAPDU resp = this.sendAPDU( cmd, DISPLAY );

		} catch( Exception e ) {}
	}


	void enterWritePIN() {
		System.out.println( "Veuillez entrer le Write PIN:" );
		try {
			String pin = readKeyboard();
			byte [] pinInByte = pin.getBytes();
			byte sizePin = (byte)pinInByte.length;

			byte[] cmd_part1 = {CLA, ENTERWRITEPIN, P1, P2, sizePin};
			int sizecmd_part1 = cmd_part1.length;
			int totalLength =sizecmd_part1+(int)sizePin;
			byte[] cmd_2= new byte[totalLength];

			System.arraycopy(cmd_part1, 0, cmd_2, 0, sizecmd_part1);
			System.arraycopy(pinInByte, 0, cmd_2, sizecmd_part1, (int)sizePin);
			CommandAPDU cmd = new CommandAPDU( cmd_2 );
			ResponseAPDU resp = this.sendAPDU( cmd, DISPLAY );

		} catch( Exception e ) {}
	}


	void readNameFromCard() {
		byte[] cmd_ = {CLA, READNAMEFROMCARD, P1, P2, (byte)0x00};
		CommandAPDU cmd = new CommandAPDU( cmd_ );
		System.out.println("Sending blank command APDU, Name expected...");
		ResponseAPDU resp = this.sendAPDU( cmd, DISPLAY );
		byte[] bytes = resp.getBytes();
		String msg = "";
		for(int i=0; i<bytes.length-2;i++)
			msg += new StringBuffer("").append((char)bytes[i]);
		System.out.println(msg);
	}


	void writeNameToCard() {
		System.out.println( "Veuillez entrer nom prenom:" );

		try {
			String name = readKeyboard();
			byte [] nameInByte = name.getBytes();
			byte sizeName = (byte)nameInByte.length;


			byte[] cmd_part1= { CLA, WRITENAMETOCARD, P1, P2, sizeName};

			int sizecmd_part1 = cmd_part1.length;
			int totalLength =sizecmd_part1+(int)sizeName;
			byte[] cmd_1= new byte[totalLength];

			System.arraycopy(cmd_part1, 0, cmd_1, 0, sizecmd_part1);
			System.arraycopy(nameInByte, 0, cmd_1, sizecmd_part1, (int)sizeName);

			CommandAPDU cmd = new CommandAPDU( cmd_1 );
			ResponseAPDU resp = this.sendAPDU( cmd, DISPLAY );

		} catch( Exception e ) {}

	}


	void exit() {
		loop = false;
	}


	void runAction( int choice ) {
		switch( choice ) {
			case 14: updateCardKey(); break;
			case 13: uncipherFileByCard(); break;
			case 12: cipherFileByCard(); break;
			case 11: cipherAndUncipherNameByCard(); break;
			case 10: readFileFromCard(); break;
			case 9: writeFileToCard(); break;
			case 8: updateWritePIN(); break;
			case 7: updateReadPIN(); break;
			case 6: displayPINSecurity(); break;
			case 5: desactivateActivatePINSecurity(); break;
			case 4: enterReadPIN(); break;
			case 3: enterWritePIN(); break;
			case 2: readNameFromCard(); break;
			case 1: writeNameToCard(); break;
			case 0: exit(); break;
			default: System.out.println( "unknown choice!" );
		}
	}


	String readKeyboard() {
		String result = null;

		try {
			BufferedReader input = new BufferedReader( new InputStreamReader( System.in ) );
			result = input.readLine();
		} catch( Exception e ) {}

		return result;
	}


	int readMenuChoice() {
		int result = 0;

		try {
			String choice = readKeyboard();
			result = Integer.parseInt( choice );
		} catch( Exception e ) {}

		System.out.println( "" );

		return result;
	}


	void printMenu() {
		System.out.println( "" );
		System.out.println( "14: update the DES key within the card" );
		System.out.println( "13: uncipher a file by the card" );
		System.out.println( "12: cipher a file by the card" );
		System.out.println( "11: cipher and uncipher a name by the card" );
		System.out.println( "10: read a file from the card" );
		System.out.println( "9: write a file to the card" );
		System.out.println( "8: update WRITE_PIN" );
		System.out.println( "7: update READ_PIN" );
		System.out.println( "6: display PIN security status" );
		System.out.println( "5: desactivate/activate PIN security" );
		System.out.println( "4: enter READ_PIN" );
		System.out.println( "3: enter WRITE_PIN" );
		System.out.println( "2: read a name from the card" );
		System.out.println( "1: write a name to the card" );
		System.out.println( "0: exit" );
		System.out.print( "--> " );
	}


	void mainLoop() {
		while( loop ) {
			printMenu();
			int choice = readMenuChoice();
			runAction( choice );
		}
	}


	public static void main( String[] args ) throws InterruptedException {
		new TheClient();
	}


}
