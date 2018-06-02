package wat.tomasz.sr.Sockets;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Calendar;

import Clients.Client;
import Packets.InvitationPacket;
import Packets.PacketParser;
import Packets.TimePacket;
import Packets.TimePacket.TimePacketType;

public class ServerSocket extends Socket {
	
	int lastRequest = 0;
	
	public ServerSocket(SocketManager manager, int port) {
		super(manager);
		try {
			_socket = new DatagramSocket(11122);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.setTimeout(2.5f);
	}

	@Override
	public void onReceiveData(String message, InetAddress receiver, int port) {
		System.out.println(message);
		String [] args = null;
		if(message.equals("INVITATION_REQUEST") ) {
			System.out.println("Odebrano zapytanie");
			sendData(
					new InvitationPacket(_manager.getClientManager().addClient( new Client(receiver, port) ) ), 
					receiver, port);
		}
		else if( (args = PacketParser.parseTimeResponse(message)) != null ) {
			int clientid = Integer.parseInt(args[1]);
			int requestid = Integer.parseInt(args[2]);
			long time = Long.parseLong(args[3]);
			long diff = Calendar.getInstance().getTimeInMillis() - time;
			System.out.println("Time difference in client " + clientid + " is "  + diff);
		}
	}

	@Override
	public void onStopped() {
		System.out.println("Server socket stopping.");
	}

	@Override
	public void onListenStart() {
		
	}

	@Override
	public void onTimeout() {	
		sendTimeRequests();
	}
	
	public void sendTimeRequests() {
		for(int id : _manager.getClientManager().getClientsID()) {
			Client client = _manager.getClientManager().getClient(id);
			System.out.println("Sending request to client " + id) ;
			this.sendData(new TimePacket(client, TimePacketType.TimeRequest, lastRequest++), 
					client.getIP(), client.getPort());
		}
	}

}
