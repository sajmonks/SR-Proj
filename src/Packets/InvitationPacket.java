package Packets;

public class InvitationPacket implements Packet {
	
	public enum InvitationType { Request, Accept };		
	private String packetString;
	
	public InvitationPacket(InvitationType type) {
			packetString = "INVITATION_REQUEST";
	}
	
	public InvitationPacket(int id) {
			packetString = "INVITATION_ACCEPT " + id;
	}

	@Override
	public byte[] getData() {
		return packetString.getBytes();
	}

}
