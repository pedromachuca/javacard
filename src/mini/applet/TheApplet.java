//La carte a puce est un serveur
package applet;

import javacard.framework.*;


public class TheApplet extends Applet {

    //register enregistre l'applet au niveau du security manager
    //security manager est un objet propre a la machine virtuelle javacard
    //register systematiquement derniere ligne du constructeur.
    protected TheApplet() {
	    this.register();
    }

    //appelé une seule fois dans la vie d'une carte pour l'initialisation
    public static void install(byte[] bArray, short bOffset, byte bLength) {
	    new TheApplet();
    }

    //point d'entree de l'applet a chaque fois qu'on communique avec
    //la carte. Recoit les commandes apdu.
    public void process(APDU apdu) {
        if(selectingApplet() == true)
		return;
    }
    /*
      Notre code !!
    */


}
