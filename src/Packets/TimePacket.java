package Packets;

import java.util.Calendar;

import Clients.Client;

public class TimePacket implements Packet {
	
	public enum TimePacketType { TimeRequest, TimeResponse, TimeCorrection };
	
	private String _packetString = null;
	private TimePacketType _type;
	private Client _client;
	
	public TimePacket(Client client, TimePacketType type,  int targetid, int requestid) {
		_type = type;
		_client = client;
		
		if(type == TimePacketType.TimeRequest) {
			_packetString = "TIME_REQUEST " + targetid + " " +requestid;
		}
	}
	
	public TimePacket(TimePacketType type,  int targetid, int requestid) {
		_type = type;
		
		if(type == TimePacketType.TimeResponse) {
			_packetString = "TIME_RESPONSE " + targetid + " " + requestid;
		}
	}
	
	public TimePacket(TimePacketType type, int targetid, int requestid, long offset) {
		_type = type;
		if(type == TimePacketType.TimeCorrection) {
			_packetString = "TIME_CORRECTION " + targetid + " " + requestid + " " +offset;
		}
	}
	
	@Override
	public byte[] getData() {
		if(_type == TimePacketType.TimeRequest) {
			_client.setLastTimeRequest(Calendar.getInstance().getTimeInMillis());
		}
		else if (_type == TimePacketType.TimeResponse) {
			_packetString += (" " + Calendar.getInstance().getTimeInMillis());
		}
		
		return _packetString.getBytes();
	}
}
