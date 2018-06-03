package Packets;

public class RemovePacket implements Packet{
	private String _packetString = "";
	public RemovePacket(int id) {
		_packetString = "REMOVE_CLIENT " + id;
	}

	@Override
	public byte[] getData() {
		// TODO Auto-generated method stub
		return _packetString.getBytes();
	}
}
