package wat.tomasz.sr.Sockets;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import Clients.Client;
import Packets.ElectionPacket;
import Packets.ElectionPacket.ElectionPacketType;
import Packets.InvitationPacket;
import Packets.InvitationPacket.InvitationType;
import Packets.PacketParser;
import Packets.TimePacket;
import Packets.TimePacket.TimePacketType;

public class ClientSocket extends Socket {
	
	private enum ClientState { Starting, Working, ServerNotResponding, ElectionStarted, ElectionProcess };
	private ClientState _state = ClientState.Starting;
	private int _id;
	
	private int _attemptsLeft = 0;
	
	public ClientSocket(SocketManager manager, String serverip, int port) {
		super(manager);
		try {
			_ip = InetAddress.getByName(serverip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		_port = port;
		
		if(_ip != null) {
			try {
				_socket = new DatagramSocket();
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void onReceiveData(String message, InetAddress receiver, int port) {
		String [] args = null;
		if( (args = PacketParser.parseInvitationAccept(message)) != null ) {
			_state = ClientState.Working;
			
			int id = Integer.parseInt(args[1]); //No exception 100%
			_id = id;
			
			//Setting gui button
			_manager.getGUI().startClientBtn.setEnabled(true);
			
			System.out.println("Accept received id=" + _id);
		}
		else if( (args = PacketParser.parseTimeRequest(message)) != null ) {
			_state = ClientState.Working;
			
			System.out.println("Received time request");
			int reqid = Integer.parseInt(args[2]);
			this.sendData(new TimePacket(TimePacketType.TimeResponse, _id, reqid), receiver, port);
		}
		else if( (args = PacketParser.parseTimeCorrection(message)) != null ) {
			//int reqid = Integer.parseInt(args[1]);
			long diff = Long.parseLong(args[3]);
			System.out.println("Received time correction offset=" + diff);
		}
		else if( (args = PacketParser.parseNewClient(message)) != null ) {
			//int reqid = Integer.parseInt(args[1]);
			int id = Integer.parseInt(args[1]);
			
			if(id == _id)
				return;
			
			InetAddress ip = null;
			try { ip = InetAddress.getByName(args[2]); } 
			catch (UnknownHostException e) { e.printStackTrace(); }
			int clientport = Integer.parseInt(args[3]);
			
			if(_id == id)
				return;
			
			_manager.getClientManager().putClient(id, new Client(ip, clientport));
			System.out.println("New client joined id=" + id + " ip="+ip.getHostAddress());
		}
		else if( (args = PacketParser.parseElectionRequest(message)) != null ) {
			int id = Integer.parseInt(args[1]);
			int from = Integer.parseInt(args[2]);
			
			if(id != _id)
				return;
			
			System.out.println("Received election request.");
			if(_state == ClientState.ServerNotResponding) {
				System.out.println("Server is not responding so answering");
				_state = ClientState.ElectionProcess;
				sendData(new ElectionPacket(ElectionPacketType.ElectionResponse, from, _id), receiver, port);
			}
			else {
				System.out.println("Server is ok. Not responding.");
			}
		}
		else if( (args = PacketParser.parseElectionResponse(message)) != null ) {
			int id = Integer.parseInt(args[1]);
			int from = Integer.parseInt(args[2]);
			
			if(id != _id)
				return;
			
			Client client = _manager.getClientManager().getClient(from);
			if(client != null) {
				client.setElectionResponsed(true);
			}
		}
	}

	@Override
	public void onStopped() {
	}

	@Override
	public void onListenStart() {
		System.out.println("Setting timeout.");
		setTimeout(3f);
		System.out.println("Sending invitation packet.");
		this.sendData(new InvitationPacket(InvitationType.Request), _ip, _port);
	}

	@Override
	public void onTimeout() {
		if(_state == ClientState.Starting) {
			_manager.getGUI().showMessage("Nie uzyskano odpowiedzi od serwera");
			_manager.getGUI().startClientBtn.getActionListeners()[0].actionPerformed(null);
		} 
		else if(_state == ClientState.Working) {
			_state = ClientState.ServerNotResponding;
			_attemptsLeft = _id + 1;
			System.out.println("Server is not responding, reconnecting for " + _attemptsLeft + " times.");
		}
		else if(_state == ClientState.ServerNotResponding && _attemptsLeft > 0) {
			_attemptsLeft--;
			System.out.println(_attemptsLeft + " attempts left.");
		}
		else if(_state == ClientState.ServerNotResponding && _attemptsLeft <= 0){
			_state = ClientState.ElectionStarted;
			System.out.println("Starting election process...");
			
			if(_manager.getClientManager().getClientsID().length == 0) {
				System.out.println("No other clients are connected. Stoping process...");
				_manager.getGUI().startClientBtn.getActionListeners()[0].actionPerformed(null);
			}
		}
		else if(_state == ClientState.ElectionStarted) {
			_state = ClientState.ElectionProcess;
			setTimeout(1f);
			broadcastElectionRequest();
		}
		else if(_state == ClientState.ElectionProcess) {
			int received = 0;
			for(int id : _manager.getClientManager().getClientsID()) {
				Client client = _manager.getClientManager().getClient(id);
				if(client.isElectionResponsed()) {
					received++;
				}
			}
			if(received == 0) {
				System.out.println("No response from other devices. Closing...");
				_manager.getGUI().startClientBtn.getActionListeners()[0].actionPerformed(null);
			}	
			else {
				System.out.println("Received " + received + " responses.");
			}
		}
	}
	
	//Leader election
	public void broadcastElectionRequest() {
		for(int id : _manager.getClientManager().getClientsID()) {
			if(id < _id)
				continue;
			
			Client client = _manager.getClientManager().getClient(id);
			sendData(new ElectionPacket(ElectionPacketType.ElectionRequest, id, _id), 
					client.getIP(), client.getPort());
		}
 	}
}
