package wat.tomasz.sr.GUI.Listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import wat.tomasz.sr.GUI.ParametersGUI;
import wat.tomasz.sr.GUI.ParametersGUI.SocketState;

public class ButtonListener implements ActionListener {
	
	public enum ButtonType { SERVER, CLIENT };
	
	public ButtonType _btnType;
	public ParametersGUI _guiHandler;
	

	public ButtonListener(ParametersGUI parametersGUI, ButtonType btnType) {
		_guiHandler = parametersGUI;
		_btnType = btnType;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(_btnType == ButtonType.SERVER) {
			if(_guiHandler.getSocketManager().getSocketState() == SocketState.None) {
				if(handleServerButton() ) {
					_guiHandler.startServerBtn.setLabel("STOP");
					_guiHandler.startClientBtn.setEnabled(false);
				}
			} 
			else {
				_guiHandler.getSocketManager().closeSocket();
				_guiHandler.startServerBtn.setLabel("Start server");
				_guiHandler.startClientBtn.setEnabled(true);
			}
	
		} 
		else if(_btnType == ButtonType.CLIENT) {
			if(_guiHandler.getSocketManager().getSocketState() == SocketState.None) {
				if(handleClientButton() ) {
					_guiHandler.startClientBtn.setLabel("STOP");
					_guiHandler.startClientBtn.setEnabled(false);
					_guiHandler.startServerBtn.setEnabled(false);
				}
			}
			else {
				_guiHandler.getSocketManager().closeSocket();
				_guiHandler.startClientBtn.setLabel("Start client");
				_guiHandler.startServerBtn.setEnabled(true);
				_guiHandler.startClientBtn.setEnabled(true);
			}
		}

	}
	
	public boolean handleServerButton() {
		int port = Integer.parseInt(_guiHandler.serverPortFld.getText().toString());
		if(port <= 0 || port > 65534) {
			_guiHandler.showMessage("Zly format portu badz wartosc przekracza za przedzial <1, 65534>.");
			return false;
		}
		
		_guiHandler.getSocketManager().startServer(port);
		return true;
	}
	
	public boolean handleClientButton() {
		boolean thrown = false;
		String ipaddress = _guiHandler.serverIPFld.getText().toString();
		try { InetAddress.getByName(ipaddress); } 
		catch (UnknownHostException e) { thrown = true; }
		
		if(thrown == true) {
			_guiHandler.showMessage("Zly format adresu IP.");
			return false;
		}
		
		int port = Integer.parseInt(_guiHandler.serverPortFld.getText().toString());
		if(port <= 0 || port > 65534) {
			_guiHandler.showMessage("Zly format portu badz wartosc przekracza za przedzial <1, 65534>.");
			return false;
		}
		
		_guiHandler.getSocketManager().startClient(ipaddress,  port);
		return true;
	}

}
