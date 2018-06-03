package wat.tomasz.sr.Sockets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import Clients.Client;
import Packets.InvitationPacket;
import Packets.NewClientPacket;
import Packets.PacketParser;
import Packets.RemovePacket;
import Packets.TimePacket;
import Packets.TimePacket.TimePacketType;

public class ServerSocket extends Socket {
	
	int lastRequest = 0;
	
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss ");
	
	public ServerSocket(SocketManager manager, int port) {
		super(manager);
		try {
			_socket = new DatagramSocket(11122);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.setTimeout(_manager.getGUI().getWindowTime());
	}

	@Override
	public void onReceiveData(String message, InetAddress receiver, int port) {
		String [] args = null;
		if(message.equals("INVITATION_REQUEST") ) {
			System.out.println("Odebrano zapytanie");
			
			int id = _manager.getClientManager().addClient( new Client(receiver, port) );
			sendData(
					new InvitationPacket(id), 
					receiver, port);
			
			System.out.println("Resending id = " + id);
			_manager.getGUI().setSlaveNumber(_manager.getClientManager().getClientCount());
		}
		else if( (args = PacketParser.parseTimeResponse(message)) != null ) {
			int clientid = Integer.parseInt(args[1]);
			int requestid = Integer.parseInt(args[2]);
			long time = Long.parseLong(args[3]);
			
			System.out.println(message);
			//long diff = Calendar.getInstance().getTimeInMillis() - time;
			Client client = _manager.getClientManager().getClient(clientid);
			client.setLastOffset(time);
			client.setResponsed(true);
			client.setNoResponseNumber(0);	
			_manager.getClientManager().putClient(clientid, client);
			
			System.out.println("Received time from client=" + clientid + " time=" + time);
		}
	}

	@Override
	public void onStopped() {
		System.out.println("Server socket stopping.");
	}

	@Override
	public void onListenStart() {
		
	}

	@Override
	public void onTimeout() {	
		broadcastRemovedClients();
		broadcastTimeCorrection();
		broadcastTimeRequests();
		broadcastNewClients();
		
	}
	
	public void broadcastRemovedClients() {
		for(int id : _manager.getClientManager().getClientsID()) {
			Client client = _manager.getClientManager().getClient(id);
			if(client.isRequested() == true && client.isResponsed() == false) {
				client.setNoResponseNumber(client.getNoResponseNumber() + 1);
				_manager.getClientManager().putClient(id, client);
			}
			System.out.println("client id=" + id + " no response " + client.getNoResponseNumber() + " " + _manager.getGUI().getTimeoutMaster());
			if(client.getNoResponseNumber() == _manager.getGUI().getTimeoutMaster()) {
				for(int rid : _manager.getClientManager().getClientsID() ) {
					Client receiver = _manager.getClientManager().getClient(rid);
					sendData(new RemovePacket(id), receiver.getIP(), receiver.getPort());
				}
				_manager.getClientManager().removeClient(id);
				_manager.getGUI().setSlaveNumber(_manager.getClientManager().getClientCount());
				System.out.println("Removing client " + id);
			}
		}
	}
	
	public void broadcastTimeCorrection() {
		int divisor = 1;
		long personal = Calendar.getInstance().getTimeInMillis();
		BigDecimal sum = new BigDecimal("" + personal);
		//System.out.println("Personal time is " + personal + " compared to big decimal " + sum.toString());
		
		for(int id : _manager.getClientManager().getClientsID()) {
			//System.out.println("Rozpoczecie obslugi id=" + id);
			Client client = _manager.getClientManager().getClient(id);			
			if(client.isRequested() == false)  continue;
			if(client.isResponsed() == false)  continue;
			
			long tempAverage = sum.divide(new BigDecimal("" + divisor) ).longValue();
			long clientTime = client.getLastOffset();
			
			long delta = _manager.getGUI().getDeltaReject();
			long deltaAverage = tempAverage - clientTime;
			
			//System.out.println("Delta is " + delta + " delta Average is" + deltaAverage);
			if(deltaAverage > delta && deltaAverage < delta)
			{
				System.out.println("Skipping client " + id + " local time because of big delta");
				continue;
			}
			
			//System.out.println("Client " + id + " time is: " + client.getLastOffset());
			sum = sum.add(new BigDecimal("" + client.getLastOffset()) );
			divisor++;
		}
		
		
		//Calculating average
		//System.out.println("Summaric time is " + sum.toString());
		long average = 0;
		try {
			average = sum.divide(new BigDecimal(divisor + ""), 2, RoundingMode.HALF_UP).longValue();
		}
		catch (ArithmeticException e) {
			e.printStackTrace();
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(average);
		
		//System.out.println("Average time of " + divisor + " clients(plus server) is " + average);
		_manager.getGUI().setAverageTime("" + average + "(" +  dateFormat.format(cal.getTime())  + ")");
		
		for(int id : _manager.getClientManager().getClientsID()) {
			Client client = _manager.getClientManager().getClient(id);
			if(!client.isRequested()) continue;
			
			//System.out.println("Difference " + average + " " + client.getLastOffset());
			long diff = average - client.getLastOffset();
			
			this.sendData(new TimePacket(TimePacketType.TimeCorrection, id, lastRequest, diff), 
					client.getIP(), client.getPort());
			
			System.out.println("Sending offset " + diff + " to id=" + id);
		}
	}
	
	public void broadcastTimeRequests() {
		
		int request = lastRequest++;
		for(int id : _manager.getClientManager().getClientsID()) {
			Client client = _manager.getClientManager().getClient(id);
			client.setRequested(true);
			client.setResponsed(false);
			_manager.getClientManager().putClient(id, client); //Update
			this.sendData(new TimePacket(client, TimePacketType.TimeRequest, id, request), 
					client.getIP(), client.getPort());
			
			
			System.out.println("Sending request to client " + id) ;
		}
	}
	
	public void broadcastNewClients() {
		for(int id : _manager.getClientManager().getClientsID()) {
			Client client = _manager.getClientManager().getClient(id);
			if(!client.isAnnounced()) {
				//Informing everyone about new joined client
				for(int sid : _manager.getClientManager().getClientsID()) {
					Client receiver = _manager.getClientManager().getClient(sid);
					sendData(new NewClientPacket(id, client.getIP(), client.getPort()), 
							receiver.getIP(), receiver.getPort() );
					
					client.setAnnounced(true);
				}
				//Updating in new joined client
				for(int sid : _manager.getClientManager().getClientsID()) {
					Client info = _manager.getClientManager().getClient(sid);
					sendData(new NewClientPacket(sid, info.getIP(), info.getPort()), 
							client.getIP(), client.getPort() );
				}		
			}
		}
	}
	

}
