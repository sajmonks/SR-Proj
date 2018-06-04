package wat.tomasz.sr.Sockets;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import Clients.Client;
import Packets.ElectionPacket;
import Packets.ElectionPacket.ElectionPacketType;
import Packets.InvitationPacket;
import Packets.InvitationPacket.InvitationType;
import Packets.PacketParser;
import Packets.TimePacket;
import Packets.TimePacket.TimePacketType;

public class ClientSocket extends Socket {
	
	private static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	
	private enum ClientState { Starting, Working, ServerNotResponding, ElectionStarted, ElectionProcess};
	private ClientState _state = ClientState.Starting;
	private int _id = -1;
	
	private boolean _electionRequested = false;
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
				_socket = new DatagramSocket(port);
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
			
			if(_id >= 0) return;
			
			_id = id;
			
			//Setting gui button
			_manager.getGUI().startClientBtn.setEnabled(true);
			_manager.getGUI().setSlaveID("" + _id);
			_manager.getGUI().setSlaveNumber(1);
			
			System.out.println("Accept received id=" + _id);
		}
		else if( (args = PacketParser.parseTimeRequest(message)) != null ) {
			_state = ClientState.Working;
			
			System.out.println("Received time request");
			int reqid = Integer.parseInt(args[2]);
			this.sendData(new TimePacket(TimePacketType.TimeResponse, _id, reqid), receiver, port);
		}
		else if( (args = PacketParser.parseTimeCorrection(message)) != null ) {
			int id = Integer.parseInt(args[1]);
			long diff = Long.parseLong(args[3]);
			
			if(id != _id) return;
			
			Calendar cal = Calendar.getInstance();
			String time = timeFormat.format(cal.getTime());
			String date = dateFormat.format(cal.getTime());
			
			System.out.println("Time before: " + date + " " + time);
			
			long timenow = cal.getTimeInMillis();
			timenow += diff;
			cal.setTimeInMillis(timenow);
			
			time = timeFormat.format(cal.getTime());
			date = dateFormat.format(cal.getTime());
			System.out.println("Time after: " + date + " " + time);
			
			try {
				Runtime.getRuntime().exec("cmd /C date " + date);
				Runtime.getRuntime().exec("cmd /C time " + time);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Received time correction offset=" + diff);
			_manager.getGUI().setLastOffset(diff);
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
			
			
			_manager.getClientManager().putClient(id, new Client(ip, clientport));
			_manager.getGUI().setSlaveNumber(_manager.getClientManager().getClientsID().length + 1);
			System.out.println("New client joined id=" + id + " ip="+ip.getHostAddress());
		}
		else if( (args = PacketParser.parseElectionRequest(message)) != null ) {
			int id = Integer.parseInt(args[1]);
			int from = Integer.parseInt(args[2]);
			
			if(id != _id)
				return;
			
			System.out.println("Received election request.");
			if(_state == ClientState.ServerNotResponding) {
				_manager.getGUI().setMode("Elekcja");
				System.out.println("Server is not responding so answering");
				_state = ClientState.ElectionStarted;
				sendData(new ElectionPacket(ElectionPacketType.ElectionResponse, from, _id), receiver, port);
				_electionRequested = true;
				//Sending broadcast to other with higher ID
				setTimeout(1f);
				attemptToBeMaster();
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
		else if( (args = PacketParser.parseElectionMaster(message)) != null ) {
			int id = Integer.parseInt(args[1]);
			int from = Integer.parseInt(args[2]);
			
			if(id != _id)
				return;
			
			_manager.getGUI().setMode("Slave");

			_manager.getClientManager().removeClient(from);
			_state = ClientState.Working;
			System.out.println("Received taking over master by id=" + from);
		}
		else if( (args = PacketParser.parseRemoveClient(message)) != null ) {
			int id = Integer.parseInt(args[1]);
			_manager.getClientManager().removeClient(id);
			_manager.getGUI().setSlaveNumber(_manager.getClientManager().getClientCount() + 1);
			System.out.println("Received remove client id=" + id);
		}
	}

	@Override
	public void onStopped() {
	}

	@Override
	public void onListenStart() {
		System.out.println("Setting timeout.");
		setTimeout(_manager.getGUI().getWindowTime());
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
			setTimeout(_manager.getGUI().getWindowTime());
			_state = ClientState.ServerNotResponding;
			//_attemptsLeft = _id + 2;
			_attemptsLeft = _manager.getGUI().getTimeoutSlave() + _id;
			System.out.println("Server is not responding, reconnecting for " + _attemptsLeft + " times.");
		}
		else if(_state == ClientState.ServerNotResponding && _attemptsLeft > 0) {
			_attemptsLeft--;
			System.out.println(_attemptsLeft + " attempts left.");
		}
		else if(_state == ClientState.ServerNotResponding && _attemptsLeft <= 0){
			_manager.getGUI().setMode("Elekcja");
			System.out.println("Starting election process...");
			
			if(_manager.getClientManager().getClientCount() == 0) {
				System.out.println("No other clients are connected. Stoping process...");
				_manager.getGUI().startClientBtn.getActionListeners()[0].actionPerformed(null);
			}
			_state = ClientState.ElectionStarted;
			setTimeout(1f);
			broadcastElectionRequest();
			System.out.println("Switch to election started.");
		}
		else if(_state == ClientState.ElectionStarted) {
			System.out.println("Calling this.");
			int received = 0;
			for(int id : _manager.getClientManager().getClientsID()) {
				Client client = _manager.getClientManager().getClient(id);
				if(client.isElectionResponsed()) {
					received++;
				}
			}
			System.out.println("Received " + received);
			if(received == 0 && _electionRequested == true) {
				broadcastElectionMaster();
				_manager.switchToMaster(_port);	
				_manager.getGUI().setMode("Master");
			}	
			else if(received == 0 && _electionRequested == false) {
				System.out.println("No other clients are connected. Stoping process...");
				_manager.getGUI().startClientBtn.getActionListeners()[0].actionPerformed(null);
			}
			else {
				_attemptsLeft = _manager.getGUI().getTimeoutSlave();
				_state = ClientState.ElectionProcess;
				System.out.println("Received " + received + " responses.");
			}
		}
		else if(_state == ClientState.ElectionProcess) {
			_attemptsLeft--;
			if(_attemptsLeft == 0) {
				attemptToBeMaster();
			}
		}
	}
	
	public void attemptToBeMaster() {
		if(broadcastElectionRequest() == 0) {
			if(_manager.getClientManager().getClientsID().length > 0) {
				broadcastElectionMaster();
				_manager.switchToMaster(_port);	
				_manager.getGUI().setMode("Master");
			}
			else {
				System.out.println("No other clients are connected. Stoping process...");
				_manager.getGUI().startClientBtn.getActionListeners()[0].actionPerformed(null);
			}
		}
	}
	
	//Leader election
	public int broadcastElectionRequest() {
		int count = 0;
		for(int id : _manager.getClientManager().getClientsID()) {
			if(id > _id) {
				Client client = _manager.getClientManager().getClient(id);
				System.out.println("Sending election request to id=" + id + " ip=" + client.getIP().getHostAddress());
				sendData(new ElectionPacket(ElectionPacketType.ElectionRequest, id, _id), 
						client.getIP(), client.getPort());
				
				count++;
			}
		}
		return count;
 	}
	
	
	public void broadcastElectionMaster() {
		for(int id : _manager.getClientManager().getClientsID()) {
			Client client = _manager.getClientManager().getClient(id);
			System.out.println("Sending election master to id=" + id + " ip=" + client.getIP().getHostAddress());
			sendData(new ElectionPacket(ElectionPacketType.SetMaster, id, _id), 
					client.getIP(), client.getPort());
		}
 	}
}
