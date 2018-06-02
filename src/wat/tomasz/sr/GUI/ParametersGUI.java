package wat.tomasz.sr.GUI;

import java.awt.Button;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;

import javax.swing.JOptionPane;

import wat.tomasz.sr.GUI.Listeners.ButtonListener;
import wat.tomasz.sr.GUI.Listeners.ButtonListener.ButtonType;
import wat.tomasz.sr.Sockets.SocketManager;

public class ParametersGUI extends Frame {
	public enum SocketState {None, Server, Client};
	
	public TextField serverIPFld, serverPortFld;
	public Button startServerBtn, startClientBtn;
	public Dialog dialog;
	
	private static final long serialVersionUID = 1L;
	
	private SocketManager socketManager;
	
	public ParametersGUI () {	
		socketManager = new SocketManager(this);
		
		GridLayout layout = new GridLayout(0, 2);
		dialog = new Dialog(this, "Test", true);
		
		serverIPFld = new TextField("localhost", 20);
		serverPortFld = new TextField("11122", 6);
		
		startServerBtn = new Button("Start server");
		startServerBtn.addActionListener(new ButtonListener(this, ButtonType.SERVER));
		
		startClientBtn = new Button("Start client");
		startClientBtn.addActionListener(new ButtonListener(this, ButtonType.CLIENT));
		
		this.setLayout(layout);
		
		this.setSize(300, 200);
		
		//Server IP
		this.add(new Label("Server IP address:"));
		this.add(serverIPFld);
		
		//Port
		this.add(new Label("Server port:"));
		this.add(serverPortFld);
		
		this.add(new Label("Client"));
		this.add(new Label("parameters"));
		
		this.add(new Label("Server"));
		this.add(new Label("parameters"));
		
		//Buttons
		this.add(startServerBtn);
		this.add(startClientBtn);
		
		this.setVisible(true);
	}
	
	public void showMessage(String message) {
		JOptionPane.showMessageDialog(this, message);
	}

	public SocketManager getSocketManager() {
		return socketManager;
	}

}
