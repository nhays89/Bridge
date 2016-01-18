package server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

public class BridgeServer extends WindowAdapter implements Runnable {
	int port;
	ServerSocket serverSocket;
	public BridgeServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("in IOException");
			e.printStackTrace();
		}
		while (true) {
			boolean knownContact = false;
			ActivityPanel.addToActivityWin("Server is waiting for connection on port" + port);
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

	@Override
	public void windowClosing(WindowEvent e) {
		if (serverSocket != null) {
			try {
				ActivityPanel.addToActivityWin("closing socket at port " + port);
				serverSocket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
