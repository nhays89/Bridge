package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Date;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import server.BridgeServer;
import com.jhlabs.awt.ParagraphLayout;

public class ChatRoom extends WindowAdapter implements ActionListener {
	/**
	 * default serial id.
	 */
	private static final long serialVersionUID = 1L;
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
				ActivityPanel.addToActivityWin("Welcome! \nBe sure to initialize "
						+ "profile fields in Setup Menu"
						+ "\nSetup port forwarding in router config settings");
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
								if(available(Integer.parseInt(ChatRoom.getUserProfilePrefs().get("port", "null")))) {
									Thread t = new Thread(new BridgeServer(
											Integer.parseInt(ChatRoom.getUserProfilePrefs().get("port", "null"))));
									t.start();
								}
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

	public static boolean available(int port) {
	    if (port < 1500 || port > 56653) {
	        throw new IllegalArgumentException("Invalid start port: " + port);
	    }

	    ServerSocket ss = null;
	    DatagramSocket ds = null;
	    try {
	        ss = new ServerSocket(port);
	        ss.setReuseAddress(true);
	        ds = new DatagramSocket(port);
	        ds.setReuseAddress(true);
	        return true;
	    } catch (IOException e) {
	    } finally {
	        if (ds != null) {
	            ds.close();
	        }

	        if (ss != null) {
	            try {
	                ss.close();
	            } catch (IOException e) {
	                /* should not be thrown */
	            }
	        }
	    }

	    return false;
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

		/*try {// simulates first startup remove when ready to deploy
			prefs.node("StartUp").removeNode();
			prefs.node("Contacts").removeNode();
			prefs.node("UserProfile").removeNode();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}*/
		if (prefs.node("StartUp").getBoolean("isFirstLaunch", true) == true) {
			prefs.node("StartUp").putBoolean("isFirstLaunch", false);
			prefs.node("Contacts");
			prefs.node("UserProfile");
			isFirstLaunch = true;
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

			 try {
			 txtPublicIP.setText(getIP());
			 } catch (Exception e) {
			 ActivityPanel.addToActivityWin("Please connect to Internet if attempting to remote chat");
			 }
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
					myPort = port;if(available(Integer.parseInt(ChatRoom.getUserProfilePrefs().get("port", "null")))) {
						Thread t = new Thread(new BridgeServer(
								Integer.parseInt(ChatRoom.getUserProfilePrefs().get("port", "null"))));
						t.start();
					}
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
			ActivityPanel.addToActivityWin("Check Internet connection!");
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
	public void windowClosing(WindowEvent e) {
		Frame[] frames = ChatWindow.getFrames();
		for (Frame f : frames) {
			f.dispose();
		}
		BridgeServer.closeStream();
		System.exit(0);
	}
		
}
