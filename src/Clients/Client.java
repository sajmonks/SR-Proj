package Clients;

import java.net.InetAddress;

public class Client {
	private InetAddress _ip;
	private int _port;
	private double _lastOffset;
	private long _lastTimeRequest;
	
	public Client(InetAddress ip, int port) {
		_ip = ip;
		_port = port;
	}
	
	public InetAddress getIP() {
		return _ip;
	}
	public void setIP(InetAddress _ip) {
		this._ip = _ip;
	}
	public int getPort() {
		return _port;
	}
	public void setPort(int _port) {
		this._port = _port;
	}
	public double getLastOffset() {
		return _lastOffset;
	}
	public void setLastOffset(double _lastOffset) {
		this._lastOffset = _lastOffset;
	}

	public long getLastTimeRequest() {
		return _lastTimeRequest;
	}

	public void setLastTimeRequest(long _lastTimeRequest) {
		this._lastTimeRequest = _lastTimeRequest;
	}
}
