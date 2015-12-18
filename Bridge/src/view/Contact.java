package view;
import java.io.Serializable;
import java.lang.reflect.Field;

public class Contact implements Serializable{
	String userName;
	String fName;
	String lName;
	String publicIP;
	String localIP;
	String port;

	public Contact(String userName, String fName, String lName,
			String publicIP, String localIP, String port) {
		this.userName = userName;
		this.fName = fName;
		this.lName = lName;
		this.publicIP = publicIP;
		this.localIP = localIP;
		this.port = port;
	}

	public String toString() {
		return userName;
	}

	public boolean equals(Contact c) {
		return this.fName.equals(c.fName) && this.lName.equals(c.lName)
				&& this.publicIP.equals(c.publicIP)
				&& this.localIP.equals(c.localIP) && this.port.equals(c.port);
	}

	public boolean checkFields() {
		for (Field field : this.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			Object value = null;
			try {
				value = field.get(this);
				if (value.equals("")) {
					return false;
				}
				char[] invalidCharacters = { '\\', '/', ':', '?', '\"', '<',
						'>', '|' , '*'};
				char[] userNameArray = userName.toCharArray();
				for (char character : invalidCharacters) {
					for (int i = 0; i < userNameArray.length; i++) {
						if (character == userNameArray[i])
							return false;
					}
				}
			} catch (NullPointerException e) {
				return false;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		try {
			   if (countOccurrences(publicIP, '.') != 3
			     || countOccurrences(localIP, '.') != 3
			     || Integer.parseInt(port) > 65535
			     || Integer.parseInt(port) < 1024) {
			    return false;
			   }
			  } catch (NumberFormatException NFE) {
			   return false;
			  }
		return true;
	}

	private int countOccurrences(String s, char c) { //it has to be private cus the ghosts in my pc are shy
		  int count = 0;
		  for (int i = 0; i < s.length(); i++) {
		   if (s.charAt(i) == c) {
		    count++;
		   }
		  }
		  return count;
		 }

	public String getFName() {
		return fName;
	}

	public String getLName() {
		return lName;
	}

	public String getPublicIP() {
		return publicIP;
	}

	public String getLocalIP() {
		return localIP;
	}

	public String getPort() {
		return port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setFName(String fName) {
		this.fName = fName;
	}

	public void setLName(String lName) {
		this.lName = lName;
	}

	public void setPublicIP(String remoteIP) {
		this.publicIP = remoteIP;
	}

	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}

	public void setPort(String port) {
		this.port = port;
	}
}
