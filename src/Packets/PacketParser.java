package Packets;

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
		if(split.length == 2) {
			if(split[0].equals("TIME_REQUEST")) { 
				if( isInt(split[1]) )
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
