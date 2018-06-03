package wat.tomasz.sr.GUI;

import java.awt.Button;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;

import wat.tomasz.sr.GUI.Listeners.ButtonListener;
import wat.tomasz.sr.GUI.Listeners.ButtonListener.ButtonType;
import wat.tomasz.sr.Sockets.SocketManager;

public class ParametersGUI extends Frame {
	public enum SocketState {None, Server, Client};
	
	public TextField serverIPFld, serverPortFld, serverRequestWindowFld, serverDeltaRejectFld, 
	serverRequestWindowToRemoveFld, clientRequestWindowToElectFld;
	
	public Label modeLbl, lastOffsetLbl, averageLbl, slaveNumberLbl;
	public Button startServerBtn, startClientBtn;
	
	private static final long serialVersionUID = 1L;
	
	private SocketManager socketManager;
	
	public ParametersGUI () {	
		addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			} } );
		
		socketManager = new SocketManager(this);
		
		GridLayout layout = new GridLayout(0, 2);
		
		serverIPFld = new TextField("localhost", 20);
		serverPortFld = new TextField("11122", 6);
		serverRequestWindowFld = new TextField("1000");
		serverDeltaRejectFld = new TextField("10000");
		serverRequestWindowToRemoveFld = new TextField("1");
		clientRequestWindowToElectFld = new TextField("1");
		
		modeLbl = new Label("");
		lastOffsetLbl = new Label("0");
		averageLbl = new Label("0");
		slaveNumberLbl = new Label("0");
		
		startServerBtn = new Button("Start server");
		startServerBtn.addActionListener(new ButtonListener(this, ButtonType.SERVER));
		
		startClientBtn = new Button("Start client");
		startClientBtn.addActionListener(new ButtonListener(this, ButtonType.CLIENT));
		
		this.setLayout(layout);
		
		this.setSize(400, 250);
		
		//Server IP
		this.add(new Label("Server IP address:"));
		this.add(serverIPFld);
		
		//Port
		this.add(new Label("Server port:"));
		this.add(serverPortFld);
		
		this.add(new Label("Czas okna odpytywania:"));
		this.add(serverRequestWindowFld);
		
		this.add(new Label("Maksymalna roznica czasu:"));
		this.add(serverDeltaRejectFld);
		
		this.add(new Label("Limit okien odpowiedzi (master):"));
		this.add(serverRequestWindowToRemoveFld);
		
		this.add(new Label("Limit okien odpowiedzi (slave):"));
		this.add(clientRequestWindowToElectFld);
		
		this.add(new Label("Tryb dzialania:"));
		this.add(modeLbl);
		
		this.add(new Label("Ostatni offset (ms):"));
		this.add(lastOffsetLbl);
		
		this.add(new Label("Sredni czas (ms):"));
		this.add(averageLbl);
		
		this.add(new Label("Ilosc slave:"));
		this.add(slaveNumberLbl);
			
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
	
	
	public void setLastOffset(long diff) {
		lastOffsetLbl.setText("" + diff);
	}
	
	public void setAverageTime(String string) {
		averageLbl.setText(string);
	}

	public void setSlaveNumber(int number) {
		slaveNumberLbl.setText("" + number);
	}
	
	public void setMode(String mode) {
		modeLbl.setText(mode);
	}
	
	public float getWindowTime() {
		float value = 0;
		try {
			value = Float.parseFloat(serverRequestWindowFld.getText()) / 1000.0f;
		}
		catch(NumberFormatException e) { value = 0; }
		return value;
	}
	
	public long getDeltaReject() {
		long value = 0;
		try {
			value = Long.parseLong(serverDeltaRejectFld.getText());
		}
		catch(NumberFormatException e) { value = 0; }
		return value;
	}
	
	public int getTimeoutMaster() {
		int value = 0;
		try {
			value = Integer.parseInt(serverRequestWindowToRemoveFld.getText());
		}
		catch(NumberFormatException e) { value = 0; }
		return value;
	}
	
	public int getTimeoutSlave() {
		int value = 0;
		try {
			value = Integer.parseInt(clientRequestWindowToElectFld.getText());
		}
		catch(NumberFormatException e) { value = 0; }
		return value;
	}
	

}
