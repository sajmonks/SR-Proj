package wat.tomasz.sr.Sockets;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Calendar;

import Clients.Client;
import Packets.InvitationPacket;
import Packets.NewClientPacket;
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
			sendData(new TimePacket(TimePacketType.TimeCorrection, clientid, requestid, diff), receiver, port);
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
		broadcastNewClients();
		
	}
	
	public void sendTimeRequests() {
		for(int id : _manager.getClientManager().getClientsID()) {
			Client client = _manager.getClientManager().getClient(id);
			System.out.println("Sending request to client " + id) ;
			this.sendData(new TimePacket(client, TimePacketType.TimeRequest, id, lastRequest++), 
					client.getIP(), client.getPort());
		}
	}
	
	public void broadcastNewClients() {
		for(int id : _manager.getClientManager().getClientsID()) {
			Client client = _manager.getClientManager().getClient(id);
			if(!client.isAnnounced()) {
				//Informing everyone about new joined client
				for(int sid : _manager.getClientManager().getClientsID()) {
					Client receiver = _manager.getClientManager().getClient(sid);
					sendData(new NewClientPacket(id, client.getIP(), client.getPort()), 
							receiver.getIP(), receiver.getPort() );
					
					client.setAnnounced(true);
				}
				//Updating in new joined client
				for(int sid : _manager.getClientManager().getClientsID()) {
					Client info = _manager.getClientManager().getClient(sid);
					sendData(new NewClientPacket(sid, info.getIP(), info.getPort()), 
							client.getIP(), client.getPort() );
				}		
			}
		}
	}
	

}
