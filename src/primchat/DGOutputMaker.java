package primchat;

import java.io.*;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.HashSet;

/** Verarbeitet die Eingaben und generiert Datagram Segments
 *  Nur ein einziges Instanz soll erstellt werden.
 *  Behandelt auch den Satz von den initialisierten Partnerschaften.
 *  Behandelt auch den Map von den Sockets und Namen der verbundenen Partnern.
 */
public class DGOutputMaker extends Thread{
	
	DatagramSocket mySocket;
	HashSet<SocketAddress> intitalizedPartners;
	HashMap<SocketAddress, String> establishedPartners;
	
	String myName;
	
	public DGOutputMaker(DatagramSocket socket, String name)
	{
		mySocket = socket;
		intitalizedPartners = new HashSet<SocketAddress>(); 
		establishedPartners = new HashMap<SocketAddress, String>();
		myName = name; 
	}
	

	/** Verarbeitet die Eingaben des Benutzers.
	 */	
	public void run()
	{
		BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in)); 
		
		while(true)
		{
			try {
				String consoleinput = reader.readLine();
				
				int firstcolon = consoleinput.indexOf("<");
				// Falsche Format nicht behandelt
				if(firstcolon != -1 )
				{
					// Auf- oder Abbau Befehl ist eingegeben. Format:
						// <partner 1.2.3.4:56789
						// <close ppp1
					if(firstcolon == 0)
					{
						String[] commandparts = consoleinput.substring(1,consoleinput.length()).split(" ");
						if(commandparts.length > 1)
							handleCommand(commandparts[0], commandparts[1]);
					}
					// Nachrichtsendung Befehl ist eingegeben. Format:
					// pppartner<Hello
					else
					{
						sendMsgToPartner("msg", consoleinput.substring(0, firstcolon), consoleinput.substring(firstcolon+1,consoleinput.length()));
					}
						
				}
				else
					System.out.println("ERR - No such partner");
				
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}      
	}
	
	/** Stellt ein Datagram zusammen und schickt es zu dem verbundenen Partner.
	 *  Nachrichttext ist bestimmt anpassend zum Typ.
	 */	
	public	void sendMsgToPartner(String mtype, String partner, String text)
	{
		// Wenn der Partner verbunden ist, stellen wir den Text zusammen. Format:
			// msg:Hello
			// partner:myname
			// close:ppp1
		// Nachher wird ein Datagramm erstelt und zu dem Socket vom Partner gesendet.
//TODO
		// Anderseits druckt man Fehlermeldung.
//TODO
	}

	/** Behandelt die Befehle von Auf- und Abbau einer Partnerschaft.
	 */	
	private void handleCommand(String command, String param)
	{
		// Neue Partnerschaft beginnen.
		// partner und 1.2.3.4:56789
		if(command.equals("partner"))
		{
			// Datagramtext kann zusammengestellt werden
				// partner:myname
			byte[] buf = ("partner:" + myName).getBytes();
			String sastring[] = param.split(":");
			DatagramPacket prtdgp;
			try {
				// Datagram zu der gegebenen Adresse und Port wird erstellt
				prtdgp = new DatagramPacket(buf,buf.length,InetAddress.getByName(sastring[0]), Integer.parseInt(sastring[1]));
				
				SocketAddress sa = prtdgp.getSocketAddress();
				//Wenn der Socket noch nicht als Partner gespeichert
				if(getNameBySA(sa) == null)
				{
					// partner Datagram wird gesendet und
					// der Socket kommt in den Satz von den initialisierten Partnerschaften
					mySocket.send(prtdgp);
					intitalizedPartners.add(sa);
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Verbundene Partnerschaft schliessen.
		else if(command.equals("close"))
		{
			// Datagramtext kann zusammengestellt werden
				// close:partner
			byte[] buf = ("close:" + myName).getBytes();
			DatagramPacket clsdgp;
			SocketAddress sa;
			try {
				//Wenn der Name unter den verbundenen Partnern gespeichert ist
				if((sa = getSAByName(param)) != null)
				{
					// Datagram wird erstellt und gesendet
					// Partnerschaft kann bald gelöscht werden
					clsdgp = new DatagramPacket(buf,buf.length,sa);
					
					mySocket.send(clsdgp);
					System.out.println(":partnership closed to " + removePartner(sa));
				}
				else
					System.out.println("ERR - No such partner");

			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			System.out.println("ERR - No such command");
	}
	
	/** Gibt den Namen von einem verbunden Partner zurück.
	 */	
	public String getNameBySA(SocketAddress sa)
	{
		if(establishedPartners.containsKey(sa))
		{
			return establishedPartners.get(sa);
		}
		return null;
	}

	/** Gibt den Socket von einem verbunden Partner zurück.
	 */	
	private SocketAddress getSAByName(String name)
	{
		if(establishedPartners.containsValue(name))
		{
			for(HashMap.Entry<SocketAddress, String> entry: establishedPartners.entrySet())
			{
				if(name.equals(entry.getValue()))
				{
					return entry.getKey();
				}
			}
		}
		return null;
	}

	/** Entscheidet ob ein Partner in Aufbau-initialisation ist.
	 */	
	public boolean initialized(SocketAddress sa)
	{
		return intitalizedPartners.contains(sa);
	}

	/** Verbindet einen Partner, und gibt zurück, ob es auf dieser Seite initialisiert wurde.
	 */	
	public boolean establishPartner(SocketAddress sa, String name)
	{
		establishedPartners.put(sa, name);
		if(intitalizedPartners.contains(sa))
		{
			intitalizedPartners.remove(sa);
			return true;
		}
		return false;
	}

	/** Baut eine Partnerschaft ab, und gibt den Namen zurück, wenn es verbunden war.
	 */	
	public String removePartner(SocketAddress sa)
	{
		if(intitalizedPartners.contains(sa))
		{
			intitalizedPartners.remove(sa);
			return new String("Unknown");
		}
		if(establishedPartners.containsKey(sa))
		{
			return establishedPartners.remove(sa);
		}
		return null;
	}

	/** Gibt den eigenen Namen zurück.
	 */	
	public String getUsername()
	{
		return myName;
	}
}
