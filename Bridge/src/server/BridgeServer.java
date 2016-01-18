package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;

import javax.swing.SwingConstants;

import view.ActivityPanel;
import view.ChatWindow;
import view.Contact;
import view.ContactList;
import view.MSGWindow;

public class BridgeServer implements Runnable {
	private static int PORT;
	private static ServerSocket serverSocket;
	public BridgeServer(int port) {
		PORT = port;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.out.println("in IOException");
			e.printStackTrace();
		}
		while (true) {
			boolean knownContact = false;
			ActivityPanel.addToActivityWin("Server is waiting for connection on port" + PORT);
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				TreeMap contacts = ContactList.getMap();
				String[] userNames = ContactList.getList();
				if (userNames != null) {
					for (int i = 0; i < userNames.length; i++) {
						Contact temp = (Contact) contacts.get(userNames[i]);
						ActivityPanel.addToActivityWin(
								"Server is attempting to connect to: " + socket.getInetAddress().getHostAddress());
						if (temp.getPublicIP().equals(socket.getInetAddress().getHostAddress())) {
							Thread chatThread = new Thread(new ChatWindow(socket, temp.getUserName()));
							chatThread.start();
							knownContact = true;
							break;
						}
					}
				}
				if (knownContact == false) {
					Object mon = new Object();
					MSGWindow alert = new MSGWindow(
							"An unknown user is attempting to connect to you, would you like to accept?",
							SwingConstants.CENTER, true, mon);
					Thread alertThread = new Thread(alert);
					alertThread.start();
					try {
						synchronized (mon) {
							mon.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (alert.getAnswer() == true) {
						Thread chatThread = new Thread(new ChatWindow(socket, "Unknown"));
						ActivityPanel.addToActivityWin("Incoming connection recieved");
						chatThread.start();
					} else {
						socket.close();
					}
				}
			} catch (IOException e) {
				return;
			} catch (Exception e) {
				ActivityPanel.addToActivityWin(e.getMessage());
			}
		}
	}
	public static void closeStream() {
		if (serverSocket != null) {
			try {
				ActivityPanel.addToActivityWin("closing socket at port " + PORT);
				serverSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
