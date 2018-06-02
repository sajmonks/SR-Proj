package Packets;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class PacketParser {
	public static String [] parseInvitationAccept (String message) {
		String [] split = message.split(" ");
		if(split.length == 2) {
			if(split[0].equals("INVITATION_ACCEPT")) { 
				if( isInt(split[1]) )
					return split;
			}
		}
		return null;
	}
	
	public static String [] parseTimeRequest(String message) {
		String [] split = message.split(" ");
		if(split.length == 3) {
			if(split[0].equals("TIME_REQUEST")) { 
				if( isInt(split[1]) )
					if( isInt(split[2]) )
						return split;
			}
		}
		return null;
	}
	
	public static String [] parseTimeResponse(String message) {
		String [] split = message.split(" ");
		if(split.length == 4) {
			if(split[0].equals("TIME_RESPONSE")) { 
				if( isInt(split[1]) )
					if( isInt(split[2]))
						if( isLong(split[3]))
						return split;
			}
		}
		return null;
	}
	
	public static String [] parseTimeCorrection(String message) {
		String [] split = message.split(" ");
		if(split.length == 4) {
			if(split[0].equals("TIME_CORRECTION")) { 
				if( isInt(split[1]) )
					if( isInt(split[2]) )
						if( isLong(split[3]))
							return split;
			}
		}
		return null;
	}
	
	public static String [] parseNewClient(String message) {
		String [] split = message.split(" ");
		if(split.length == 4) {
			if(split[0].equals("NEW_CLIENT")) { 
				if( isInt(split[1]) )
				{
					boolean thrown = false;
					try {
						InetAddress.getByName(split[2]);
					}
					catch(UnknownHostException e) {
						thrown = true;
					}
					
					if(thrown == false)
						if( isInt(split[3]))
							return split;
					
				}
			}
		}
		return null;
	}
	
	public static String [] parseElectionRequest(String message) {
		String [] split = message.split(" ");
		if(split.length == 3) {
			if(split[0].equals("ELECTION_REQUEST")) { 
				if( isInt(split[1]) )
					if( isInt(split[2]) )
						return split;
			}
		}
		return null;
	}
	
	public static String [] parseElectionResponse(String message) {
		String [] split = message.split(" ");
		if(split.length == 3) {
			if(split[0].equals("ELECTION_RESPONSE")) { 
				if( isInt(split[1]) )
					if( isInt(split[2]) )
						return split;
			}
		}
		return null;
	}
	
	public static String [] parseElectionMaster(String message) {
		String [] split = message.split(" ");
		if(split.length == 3) {
			if(split[0].equals("ELECTION_MASTER")) { 
				if( isInt(split[1]) )
					if( isInt(split[2]) )
						return split;
			}
		}
		return null;
	}
	
	private static boolean isInt(String str) {
		try {
			int d = Integer.parseInt(str);
		} catch(NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	private static boolean isLong(String str) {
		try {
			long d = Long.parseLong(str);
		} catch(NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
