package primchat;

import java.net.SocketAddress;

/** Verarbeitet den von einem empfangenen Datagram Segment mit der Hilfe von DGOutputMaker.
 *  Druckt Nachrichten auf den Bildschirm.
 *  Kann auch automatische Antwortnachricht generieren.
 */
public class DGInputHandler extends Thread{

	SocketAddress sendersa;
	String input;
	DGOutputMaker myopm;
	
	public DGInputHandler(SocketAddress sendersa, String input, DGOutputMaker opm)
	{
		this.sendersa = sendersa;
		this.input = input;
		myopm = opm;
	}

	/** Verarbeitet den Datagramtext.
	 */	
	public void run()
	{
		// Nachrichtenformat ist bestimmt:
			// msg:Hello
			// partner:hisname
			// close:ppp1
		int firstcolon = input.indexOf(":");
		// Falsche Format wird einfach nicht behandelt
		if(firstcolon != -1 )
		{
			String command = input.substring(0, firstcolon);
			String param = input.substring(firstcolon+1,input.length());
			
			String name;
			// Ein Textnachricht von einem verbunden Partner soll einfach gedruckt werden. Format:
			// ppp1>Hello

			if (command.equals("msg")) {
				System.out.println(myopm.getNameBySA(sendersa)+ "<"+param);
			}
//TODO
			// Eine Anfrage für Verbindungaufbau soll gleich beantwortet werden,
			// aber nur wenn es vorher nicht von dieser Seite initialisiert wurde.
			// Partner wird verbunden.
			// Ein Textnachricht wird auf den Bildschirm gedruckt:
			// >partnership opened to ppp1

			else if(command.equals("partner") ) {

				if (!myopm.initialized(sendersa)) {
					System.out.println(">partnership opened to " + param);
					myopm.establishPartner(sendersa,param);
					myopm.sendMsgToPartner("partner", param, myopm.getUsername());
				}
			}
//TODO
			// Nach einer Anfrage für Verbindungsabbau, die von einem Partner kam,
			// wird die Partnerschaft sofort geschlossen.
			// Ein Textnachricht wird auf den Bildschirm gedruckt:
			// >partnership closed to ppp1
			else if(command.equals("close") && (name = myopm.getNameBySA(sendersa))!= null)
			{
					System.out.println(":partnership closed to " + myopm.removePartner(sendersa));			
			}
			// Anderseits druckt man Fehlermeldung.
			else
			{
				System.out.println("ERR - message received with unknown format");
			}
		}
		
	}
	
}
