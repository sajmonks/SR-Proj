package wat.tomasz.sr.Sockets;

import Clients.ClientManager;
import wat.tomasz.sr.GUI.ParametersGUI;
import wat.tomasz.sr.GUI.ParametersGUI.SocketState;

public class SocketManager {
	ParametersGUI _gui;
	
	private ClientManager _clientManager = new ClientManager();	
	private Socket _socket;
	private SocketState _socketState = SocketState.None;
	private Thread _thread;
	private int _myID;
	
	public SocketManager(ParametersGUI gui) {
		_gui = gui;
		_thread = null;
	}
	
	public SocketState getSocketState() {
		return _socketState;
	}

	private void setSocket(Socket _socket) {
		if(_thread == null) {
			this._socket = _socket;		
			_thread = new Thread(new Runnable() {
				@Override
				public void run() {
					_socket.listen();
				}
			});
			_thread.start();
		}
	}
	
	public void switchToMaster(int port) {
		closeSocket();
		startServer(port);
	}
	
	public void startServer(int port) {
		setSocket(new ServerSocket(this, port));
		_socketState = SocketState.Server;
	}
	
	public void startClient(String ip, int port) {
		setSocket(new ClientSocket(this, ip, port));
		_socketState = SocketState.Client;
	}
	
	public void closeSocket() {
		if(_thread.isAlive()) {
			_socket.closeSocket();
			System.out.println("Thread is alive so interrupting");
			_thread.interrupt();
		}
		else {
			_socket.closeSocket();
		}
		_thread = null;
		_socketState = SocketState.None;
	}
	
	public ParametersGUI getGUI() {
		return _gui;
	}
	
	public ClientManager getClientManager() {
		return _clientManager;
	}

	public int getMyID() {
		return _myID;
	}

	public void setMyID(int _myID) {
		this._myID = _myID;
	}
	
}
