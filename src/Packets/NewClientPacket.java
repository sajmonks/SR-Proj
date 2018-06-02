package Packets;

import java.net.InetAddress;

public class NewClientPacket implements Packet {
	private String _packetString;
	
	public NewClientPacket(int id, InetAddress adress, int port) {
		_packetString = "NEW_CLIENT " + id + " " + adress.getHostAddress() + " " + port;
	}
	
	@Override
	public byte[] getData() {
		return _packetString.getBytes();
	}
}
