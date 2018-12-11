package applet;




import javacard.framework.*;




public class TheApplet extends Applet {

	//Attribut valeur des parametres d'instruction qu'on sera capable de reconnaitre
	//pour ne pas utiliser les codes mais les noms.
	final static byte  BINARY_WRITE = (byte) 0xD0;
	final static byte  BINARY_READ  = (byte) 0xB0;
	final static byte  SELECT       = (byte) 0xA4;

	final static short NVRSIZE      = (short)1024;
	//Tableau de taille 1024 octet
	static byte[] NVR               = new byte[NVRSIZE];

	//From addPin-rules.txt
	OwnerPIN pin;
	final byte PIN_VERIFY =(byte)0x20;
	final short SW_VERIFICATION_FAILED = (short)0x6300;
	final short SW_PIN_VERIFICATION_REQUIRED = (short)0x6301;

	protected TheApplet() {
		byte[] pincode = {(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x30}; // PIN code "0000"
		pin = new OwnerPIN((byte)3,(byte)8);  				// 3 tries 8=Max Size
		pin.update(pincode,(short)0,(byte)4); 				// from pincode, offset 0, length 4
		register();
	}


	public static void install( byte[] bArray, short bOffset, byte bLength ){
		new TheApplet();
	}


	public boolean select(){
		if ( pin.getTriesRemaining() == 0 )
			return false;

		return true;
	}

	//exectuer en fin de session ou lors d'un arachage de la carte
	public void deselect(){
		pin.reset();
	}


	void verify( APDU apdu ) {
		apdu.setIncomingAndReceive();
		byte[] buffer = apdu.getBuffer();
		if( !pin.check( buffer, (byte)5, buffer[4] ) )
			ISOException.throwIt( SW_VERIFICATION_FAILED );
	}

	//le system de l'applet renvoie implicitement 90 00 si tout c'est bien passé
	public void process(APDU apdu) throws ISOException {
		byte[] buffer = apdu.getBuffer();
		switch (buffer[1]) {

			case SELECT: return;

			case PIN_VERIFY:
				verify( apdu );
				break;

			case BINARY_READ:
				if ( ! pin.isValidated() )
					ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
				//buffer : pointeur sur la zone d'échange qui contient dans un sens l'apdu de command
				//et dans l'autre l'apdu de réponse
				//attribut de classe NVR tableau => lire buffer vers NVR et inversement pour lire
				Util.arrayCopy(NVR,(byte)0,buffer,(short)0,buffer[4]);
				//Ne pas oublier cette ligne (offset du début de tableau, longueur a envoyé)
				//premier argument offset sur le buffer pour faire a partir de
				apdu.setOutgoingAndSend((short)0,buffer[4]);
				break;

			case BINARY_WRITE:
				if ( ! pin.isValidated() )
					ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
				//ne pas oublier de mettre cette ligne quand on doit recevoir
				//des données de la carte.
				apdu.setIncomingAndReceive();
				//jamais de for pour les transferts utiliser arrayCopy
				Util.arrayCopy(buffer,(short)5,NVR,(short)0,buffer[4]);
				break;


			default:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}

	}

}
