package applet;


import javacard.framework.*;




public class TheApplet extends Applet {

    //unique types possible : byte, short, boolean
    // également seulement tableau 1 dimension
    protected byte[] msg={(byte)12,'H','e','l','l','o',' ','w','o','r','l','d','!'};


    protected TheApplet() {
	    this.register();
    }


    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
	    new TheApplet();
    }

    //Refus de communication si a faux
    //rajouté de maniere implicite dans mini qui renvoi toujours vrai.
    public boolean select() {
	    return true;
    }


    public void process(APDU apdu) throws ISOException {
        if( selectingApplet() == true )
		return;

        byte[] buffer = apdu.getBuffer();
	Util.arrayCopy(msg, (short)1, buffer, (short)0, msg[0]);
	apdu.setOutgoingAndSend( (short)0, msg[0] );
    }


}
