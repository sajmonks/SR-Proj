package wat.tomasz.sr.Sockets;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import Packets.InvitationPacket;
import Packets.InvitationPacket.InvitationType;
import Packets.PacketParser;
import Packets.TimePacket;
import Packets.TimePacket.TimePacketType;

public class ClientSocket extends Socket {
	
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
			System.out.println("Odebrano akceptacje");
			int id = Integer.parseInt(args[1]); //No exception 100%
			_manager.setMyID(id);
			System.out.println("My id is: " + id);
			_manager.getGUI().startClientBtn.setEnabled(true);
			setTimeout(0);
		}
		else if( (args = PacketParser.parseTimeRequest(message)) != null ) {
			System.out.println("Received time request");
			int reqid = Integer.parseInt(args[1]);
			this.sendData(new TimePacket(TimePacketType.TimeResponse, _manager.getMyID(), reqid), receiver, port);
		}
	}

	@Override
	public void onStopped() {
	}

	@Override
	public void onListenStart() {
		System.out.println("Setting timeout.");
		setTimeout(2f);
		System.out.println("Sending invitation packet.");
		this.sendData(new InvitationPacket(InvitationType.Request), _ip, _port);
	}

	@Override
	public void onTimeout() {
		_manager.getGUI().showMessage("Nie uzyskano odpowiedzi od serwera");
		_manager.getGUI().startClientBtn.getActionListeners()[0].actionPerformed(null);
	}


}
