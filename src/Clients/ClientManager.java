package Clients;

import java.util.HashMap;

public class ClientManager {
	public int lastID = -1;
	
	private HashMap <Integer, Client> _clientList = new HashMap<Integer, Client>();
	
	public int addClient(Client client) {
		int id = ++lastID;
		_clientList.put(id, client);
		return id;
	}
	
	public Integer[] getClientsID() {
		return _clientList.keySet().toArray(new Integer[_clientList.size()]);
	}
	
	public Client getClient(int id) {
		if(_clientList.containsKey(id)) {
			return _clientList.get(id);
		}
		return null;
	}
	
	public void putClient(int id, Client client) {
		if(!_clientList.containsKey(id)) {
			_clientList.put(id, client);
		}
	}
	
	public void removeClient(int id) {
		if(_clientList.containsKey(id)) {
			_clientList.remove(id);
		}
	}
	
	public int getClientCount() {
		return _clientList.size();
	}
}
