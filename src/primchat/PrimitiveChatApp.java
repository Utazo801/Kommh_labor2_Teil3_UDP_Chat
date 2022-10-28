package primchat;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketAddress;

/** Primitive Chat Applikation über UDP
 *  Die Partner bauen eine Arte von Verbindung auf. (Zwei Wege.)
 *  Von der Verbindungtabelle weisst man die Namen von den Partnern.
 *  Nachrichten können zu den Partnern geschickt werden.
 *  Am Ende soll die Verbindung abgebaut werden. (Ein Weg.)
 */
public class PrimitiveChatApp {
	   // Name benutzt in Partnerschaft
	static String username = new String("Szabe");

	public static void main(String[] args) {
	    try {
		    DatagramSocket mysocket = new DatagramSocket(55555);
	            
	            DGOutputMaker opm = new DGOutputMaker(mysocket, username);
	            opm.start();

	            while (true) {
	            	byte[] pkbuf = new byte[512]; 
	            	DatagramPacket dgp = new DatagramPacket(pkbuf,512);
	            	mysocket.receive(dgp);

			// Wenn ein UDP Datagramm empfangen wurde
	            	String input = new String(dgp.getData(), 0, dgp.getLength());
	            	
	            	SocketAddress sendersa = dgp.getSocketAddress();
	            	
	            	DGInputHandler iph = new DGInputHandler(sendersa, input, opm);
	                iph.start();
	            }

	        } catch (IOException ioE) {
	            System.out.println(ioE.getLocalizedMessage());
	        }
	    }
}
// Usage:
// <partner 1.2.3.4:56789	--> start connection to partner
// P1<Hello			--> send message to partner P1
// <close P1			--> close connection to partner P1