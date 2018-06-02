package Packets;

public class ElectionPacket implements Packet {

	public enum ElectionPacketType { ElectionRequest, ElectionResponse, SetMaster };
	
	private String _packetString = null;
	
	public ElectionPacket(ElectionPacketType type, int targetid, int clientid) {
		if(type == ElectionPacketType.ElectionRequest) {
			_packetString = "ELECTION_REQUEST " + targetid + " " + clientid;
		}
		if(type == ElectionPacketType.ElectionResponse) {
			_packetString = "ELECTION_RESPONSE " + targetid + " " + clientid;
		}
		if(type == ElectionPacketType.SetMaster) {
			_packetString = "ELECTION_MASTER " + targetid + " " + clientid;
		}
	}
	
	@Override
	public byte[] getData() {
		return _packetString.getBytes();
	}
	

}
