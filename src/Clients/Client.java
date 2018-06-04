package Clients;

import java.net.InetAddress;

public class Client {
	private InetAddress _ip;
	private int _port = 0;
	private long _lastOffset = 0;
	private long _lastTimeRequest = 0;
	private long _lastTimeRecived = 0;
	private boolean _isAnnounced = false;
	private boolean _electionResponsed = false;
	
	private boolean _requested = false;
	private boolean _responsed = false;
	private int _noResponseNumber = 0;
	
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
	public long getLastOffset() {
		return _lastOffset;
	}
	public void setLastOffset(long _lastOffset) {
		this._lastOffset = _lastOffset;
	}

	public long getLastTimeRequest() {
		return _lastTimeRequest;
	}

	public void setLastTimeRequest(long _lastTimeRequest) {
		this._lastTimeRequest = _lastTimeRequest;
	}

	public boolean isAnnounced() {
		return _isAnnounced;
	}

	public void setAnnounced(boolean _isAnnounced) {
		this._isAnnounced = _isAnnounced;
	}

	public boolean isElectionResponsed() {
		return _electionResponsed;
	}

	public void setElectionResponsed(boolean _electioResponsed) {
		this._electionResponsed = _electioResponsed;
	}

	public boolean isRequested() {
		return _requested;
	}

	public void setRequested(boolean _requested) {
		this._requested = _requested;
	}

	public int getNoResponseNumber() {
		return _noResponseNumber;
	}

	public void setNoResponseNumber(int _noResponseNumber) {
		this._noResponseNumber = _noResponseNumber;
	}

	public boolean isResponsed() {
		return _responsed;
	}

	public void setResponsed(boolean _responsed) {
		this._responsed = _responsed;
	}

	public long getLastTimeRecived() {
		return _lastTimeRecived;
	}

	public void setLastTimeRecived(long _lastTimeRecived) {
		this._lastTimeRecived = _lastTimeRecived;
	}
}
