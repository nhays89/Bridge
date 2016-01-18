package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.*;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JPanel;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthScrollBarUI;

import prefobj.PrefObj;

import com.jhlabs.awt.ParagraphLayout;

import java.awt.GridLayout;

public class ChatRoom extends JFrame implements ActionListener, WindowListener {
	private ServerSocket serverSocket;
	private static JFrame mainFrame;
	private JMenuBar menuBar;
	private JButton btnSubmitMyInfo;
	private JTextField txtUserName, txtFName, txtLName, txtPublicIP, txtLocalIP, txtPort;
	private String myUserName, myFName, myLName, myLocalIP, myPublicIP, myPort;
	public JLabel lblUserName, lblFName, lblLName, lblPublicIP, lblLocalIP, lblPort;
	public static Preferences prefs = Preferences.userNodeForPackage(ChatRoom.class);
	private JMenu mnSetup;
	private JMenuItem mntmInit;
	private boolean isFirstLaunch = false;
	private JMenuItem mntmInfo;
	private boolean isServerSocketActive = false;
	private ServerSocket serverListening;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equalsIgnoreCase(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception ex) {
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new ChatRoom();
				SwingUtilities.updateComponentTreeUI(mainFrame);
			}
		});
	}

	private class ServerSocketThread implements Runnable {
		int port;

		public ServerSocketThread(int port) {
			this.port = port;
		}

		@Override
		public void run() {
			try {
				if (isServerSocketActive == false) {
					serverSocket = new ServerSocket(port);
				}
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
							ActivityPanel.addToActivityWin("Server is attempting to connect to: " + socket.getInetAddress().getHostAddress());
							System.out.println();
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
	}

	/**
	 * Create the application.
	 */
	public ChatRoom() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			getPrefs();
			setUI();
			createComponenets();
			SwingUtilities.updateComponentTreeUI(mainFrame);
			if (isFirstLaunch == true) {
				ActivityPanel.addToActivityWin("Welcome! \n Be sure to initialize profile fields in Setup Menu");
			} else {
				try {
					String[] childNames = ChatRoom.getUserProfilePrefs().keys();
					if (childNames.length > 0) {
						Contact temp = new Contact(ChatRoom.getUserProfilePrefs().get(childNames[0], "null"),
								ChatRoom.getUserProfilePrefs().get(childNames[1], "null"),
								ChatRoom.getUserProfilePrefs().get(childNames[2], "null"),
								ChatRoom.getUserProfilePrefs().get(childNames[3], "null"),
								ChatRoom.getUserProfilePrefs().get(childNames[4], "null"),
								ChatRoom.getUserProfilePrefs().get(childNames[5], "null"));
						if (temp.checkFields() == false) {
							return;
						} else {
							System.out.println(ChatRoom.getUserProfilePrefs().get("port", "null"));

							if (serverListening(Integer.parseInt(ChatRoom.getUserProfilePrefs().get("port", "null"))))
								isServerSocketActive = true;
							Thread t = new Thread(new ServerSocketThread(
									Integer.parseInt(ChatRoom.getUserProfilePrefs().get("port", "null"))));
							t.start();
						}
					}
				} catch (BackingStoreException e) {
					ActivityPanel.addToActivityWin(
							"Error: Backing Store Exception \n unable to connect to users preferences");
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			ActivityPanel.addToActivityWin(e.getMessage());
			e.printStackTrace();
		}
	}

	public boolean serverListening(int port) {
		try {
			serverSocket = new ServerSocket(port);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void createComponenets() {
		try {
			mainFrame = new JFrame("Test");
			mainFrame.getContentPane().setBackground(Color.black);
			Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
			mainFrame.setLocation(p.x - p.x / 2, p.y - p.y / 2);
			mainFrame.setTitle("Bridge");
			mainFrame.getContentPane().setPreferredSize(new Dimension(900, 400));
			mainFrame.pack();
			menuBar = new JMenuBar();
			mainFrame.setJMenuBar(menuBar);
			mnSetup = new JMenu("Setup");
			menuBar.add(mnSetup);
			mntmInit = new JMenuItem("Initialize");
			mntmInit.addActionListener(this);
			mnSetup.add(mntmInit);
			mntmInfo = new JMenuItem("Information");
			mntmInfo.addActionListener(this);
			mnSetup.add(mntmInfo);
			ContactList list = new ContactList();
			list.setVisible(true);
			mainFrame.getContentPane().add(list, BorderLayout.WEST);
			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.setBackground(Color.BLACK);
			mainFrame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
			AddTab add = new AddTab();
			tabbedPane.addTab("Add Contact", add);
			/*
			 * CreateLocalTab addNewLocal = new CreateLocalTab();
			 * tabbedPane.addTab("Add Local Contact", addNewLocal);
			 */
			ActivityPanel consolePanel = new ActivityPanel(new Date());
			consolePanel.setPreferredSize(new Dimension(245, mainFrame.getHeight()));
			mainFrame.getContentPane().add(consolePanel, BorderLayout.EAST);
			mainFrame.setVisible(true);
			mainFrame.addWindowListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setUI() {
		UIManager.put("MenuItem.background", Color.black);
		UIManager.put("nimbusBase", Color.black);
		UIManager.put("nimbusBlueGrey", Color.black);
		UIManager.put("MenuItem[Enabled].textForeground", new Color(232, 232, 232));
		UIManager.put("Menu[Enabled].textForeground", new Color(232, 232, 232));
		UIManager.put("MenuBar:Menu[Disabled].textForeground", new Color(232, 232, 232));
		UIManager.put("Menu:MenuItemAccelerator.contentMargins", Color.black);
		UIManager.put("Menu[Enabled+Selected].textForeground", Color.black);
		UIManager.put("List.background", Color.black);
		UIManager.put("List.focusCellHighlightBorder", Color.black);
		UIManager.put("List.cellRenderer[Disabled].background", Color.green);
		UIManager.put("List[Selected].textBackground", Color.black);
		UIManager.put("textForeground", new Color(232, 232, 232));
		UIManager.put("TextField.foreground", Color.black);
		UIManager.put("TextField.background", new Color(232, 232, 232));
		UIManager.put("TextArea.foreground", Color.WHITE);
		UIManager.put("TextArea.background", Color.black);
		UIManager.getLookAndFeelDefaults().put("defaultFont", new Font("Open Sans ExtraBold", Font.BOLD, 12));
		UIManager.put("OptionPane.disabled", Color.black);
		UIManager.put("OptionPane.background", Color.BLACK);
		UIManager.put("OptionPane.foreground", Color.BLACK);
	}

	private void getPrefs() {

		try {// simulates first startup remove when ready to deploy
			prefs.node("StartUp").removeNode();
			prefs.node("Contacts").removeNode();
			prefs.node("UserProfile").removeNode();
			System.out.println(prefs.nodeExists("StartUp"));
			System.out.println(prefs.nodeExists("Contacts"));
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

		if (prefs.node("StartUp").getBoolean("isFirstLaunch", true) == true) {
			prefs.node("StartUp").putBoolean("isFirstLaunch", false);
			prefs.node("Contacts");
			prefs.node("UserProfile");
			System.out.println(prefs.node("Contacts"));
			System.out.println(prefs.node("StartUp"));
			isFirstLaunch = true;
			System.out.println(prefs.node("StartUp").getBoolean("isFirstLaunch", true));
		}
	}

	private class ProfileMenu extends JFrame implements ActionListener {

		public ProfileMenu() {
			getContentPane().setBackground(Color.BLACK);
			setTitle("Setup Profile Information");
			setSize(450, 350);
			setLayout(new ParagraphLayout(50, 30, 10, 10, 10, 10));
			lblUserName = new JLabel("User Name", JLabel.TRAILING);
			lblFName = new JLabel("First Name", JLabel.TRAILING);
			lblLName = new JLabel("Last Name", JLabel.TRAILING);
			lblPublicIP = new JLabel("Public Ip", JLabel.TRAILING);
			lblLocalIP = new JLabel("Local Ip", JLabel.TRAILING);
			lblPort = new JLabel("Port", JLabel.TRAILING);
			txtUserName = new JTextField(20);
			txtFName = new JTextField(20);
			txtLName = new JTextField(20);
			txtPublicIP = new JTextField(20);
			txtLocalIP = new JTextField(20);
			txtPort = new JTextField(20);
			txtUserName.setText(prefs.node("UserProfile").get("userName", ""));
			txtFName.setText(prefs.node("UserProfile").get("fName", ""));
			txtLName.setText(prefs.node("UserProfile").get("lName", ""));
			txtPublicIP.setText(prefs.node("UserProfile").get("publicIP", ""));
			txtLocalIP.setText(prefs.node("UserProfile").get("localIP", ""));
			txtPort.setText(prefs.node("UserProfile").get("port", ""));

			// try {
			// txtPublicIP.setText(getIP());
			// } catch (Exception e) {
			// e.printStackTrace();
			// }

			add(lblUserName, ParagraphLayout.NEW_PARAGRAPH);
			add(txtUserName);
			add(lblFName, ParagraphLayout.NEW_PARAGRAPH);
			add(txtFName);
			add(lblLName, ParagraphLayout.NEW_PARAGRAPH);
			add(txtLName);
			add(lblPublicIP, ParagraphLayout.NEW_PARAGRAPH);
			add(txtPublicIP);
			add(lblLocalIP, ParagraphLayout.NEW_PARAGRAPH);
			add(txtLocalIP);
			add(lblPort, ParagraphLayout.NEW_PARAGRAPH);
			add(txtPort);
			btnSubmitMyInfo = new JButton("Submit");
			btnSubmitMyInfo.setPreferredSize(new Dimension(200, 30));
			add(btnSubmitMyInfo, ParagraphLayout.NEW_LINE);
			btnSubmitMyInfo.addActionListener(this);
			this.setVisible(true);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(btnSubmitMyInfo)) {
				String userName = txtUserName.getText();
				String fName = txtFName.getText();
				String lName = txtLName.getText();
				String publicIP = txtPublicIP.getText();
				String localIP = txtLocalIP.getText();
				String port = txtPort.getText();
				if (checkFieldValidity(userName, fName, lName, localIP, publicIP, port)) {
					prefs.node("UserProfile").put("userName", userName);
					prefs.node("UserProfile").put("fName", fName);
					prefs.node("UserProfile").put("lName", lName);
					prefs.node("UserProfile").put("localIP", publicIP);
					prefs.node("UserProfile").put("publicIP", localIP);
					prefs.node("UserProfile").put("port", port);
					myUserName = userName;
					myFName = fName;
					myLName = lName;
					myLocalIP = localIP;
					myPublicIP = publicIP;
					myPort = port;
					Thread t = new Thread(new ServerSocketThread(Integer.parseInt(port)));
					t.start();
					this.dispose();
					return;
				} else
					ActivityPanel.addToActivityWin("invalid argument");
				this.dispose();
				return;
			}
		}

		public boolean checkFieldValidity(String userName, String fName, String lName, String localIP, String publicIP,
				String port) {
			boolean isValid = false;
			Contact user = new Contact(userName, fName, lName, localIP, publicIP, port);
			if (user.checkFields()) {
				if (myPort == null) {
					isValid = true;
				} else {
					if (!myPort.equals(port)) {
						isValid = true;
					}
				}
			}
			return isValid;
		}
	}

	public String getIP() throws Exception {
		BufferedReader in = null;
		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			String ip = in.readLine();
			return ip;
		} catch (Exception e) {
			new MSGWindow("Error", "Check Internet connection!", SwingConstants.CENTER)
					.setLocationRelativeTo(mainFrame);
			return "";
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					return "";
				}
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(mntmInit)) {
			new ProfileMenu().setLocationRelativeTo(mainFrame);
		}
	}

	public static Preferences getContactPrefs() {
		return prefs.node("Contacts");
	}

	public static Preferences getUserProfilePrefs() {
		return prefs.node("UserProfile");
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		System.out.println("in window closed");
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Frame[] frames = ChatWindow.getFrames();
		for (Frame f : frames) {
			f.dispose();
		}
		System.out.println("before server socket is closed");
		if (serverSocket != null) {
			try {
				ActivityPanel.addToActivityWin("closing socket at port " + myPort);
				serverSocket.close();
				System.exit(0);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			System.exit(0);
		}
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {

	}
}
