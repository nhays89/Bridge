package view;
/*
 * Create README link to github to set up port forwarding
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Vector;
import java.util.prefs.BackingStoreException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import prefobj.PrefObj;

import com.jhlabs.awt.ParagraphLayout;

public class ContactList extends JPanel implements ActionListener,
		ListSelectionListener, ItemListener, MouseListener {
	public static Vector listData;
	public JPanel btnPanel;
	private static JList contactList;
	private JButton btnConnect;
	private JScrollPane scrollPane;
	private JButton btnDelete;
	private JPanel lblPanel;
	private JLabel lblTitle;
	private static TreeMap map;
	private String userName;
	private String localWAN;
	private String[] nodes;
	private JCheckBox boxToggle;

	public ContactList() throws ClassNotFoundException, IOException {
		setBackground(Color.black);
		this.setPreferredSize(new Dimension(250, 600));
		listData = new Vector();
		setLayout(new BorderLayout());
		map = new TreeMap();

		try {
			nodes = ChatRoom.getContactPrefs().childrenNames();
		} catch (BackingStoreException e1) {
			return;
		}

		if (nodes.length > 0) {
			try {
				for (int i = 0; i < nodes.length; i++) {
					Contact theContact = (Contact) PrefObj.getObject(ChatRoom
							.getContactPrefs().node(nodes[i]), nodes[i]);
					addToMap(nodes[i], theContact);
				}
			} catch (BackingStoreException e) {
			return;
			}
		}

		contactList = new JList<String>(listData);
		contactList.addListSelectionListener(this);

		scrollPane = new JScrollPane(contactList);
		scrollPane.getViewport().add(contactList);
		add(scrollPane, BorderLayout.CENTER);
		createComponents();
	}

	public static void addToMap(String userName, Contact c) {
		map.put(userName, c);
		listData.add(userName);
	}

	public static void revalidateContactList() {
		contactList.revalidate();
		contactList.repaint();
		SwingUtilities.updateComponentTreeUI(contactList);
	}

	public static TreeMap getMap() {
		return map;
	}

	public void createComponents() {
		btnPanel = new JPanel(new FlowLayout());
		add(btnPanel, BorderLayout.SOUTH);
		btnPanel.setBackground(Color.black);
		boxToggle = new JCheckBox();
		boxToggle.addItemListener(this);
		boxToggle.addMouseListener(this);
		btnPanel.add(boxToggle);
		btnConnect = new JButton();
		try {
			Image img = ImageIO.read(getClass().getClassLoader().getResource(
					"img/solderBridgeIcon.png"));
			btnConnect.setIcon(new ImageIcon(img));
			btnConnect.setBackground(Color.WHITE);
		} catch (IOException ex) {
			btnConnect = new JButton("Connect");
		}
		btnPanel.add(btnConnect);
		btnConnect.addActionListener(this);
		btnDelete = new JButton();
		try {
			Image img = ImageIO.read(getClass().getClassLoader().getResource(
					"img/trashcanIcon.png"));
			btnDelete.setIcon(new ImageIcon(img));
			btnDelete.setBackground(Color.WHITE);
		} catch (IOException ex) {
			btnDelete = new JButton("Delete");
		}
		btnDelete.setPreferredSize(new Dimension(83, 29));
		btnPanel.add(btnDelete);
		btnDelete.addActionListener(this);
		lblPanel = new JPanel();
		lblPanel.setBackground(Color.BLACK);
		add(lblPanel, BorderLayout.NORTH);
		lblTitle = new JLabel("Contact List");
		lblPanel.add(lblTitle);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnConnect) {
			// create new chat window
			if(map.get(contactList.getSelectedValue()).equals(null)) return;
			Contact c = (Contact) map.get(contactList.getSelectedValue());
			Socket s = null;
			try {
				ActivityPanel.addToActivityWin("Attempting to Connect to "
						+ c.userName + " (" + c.publicIP + ") " + "on port "
						+ c.port + "");
				if (boxToggle.isSelected()) {
					s = new Socket(c.getLocalIP(),
							Integer.parseInt(c.getPort()));
				} else {
					s = new Socket(c.getPublicIP(), Integer.parseInt(c
							.getPort()));
				}
			} catch (NumberFormatException e1) {
				ActivityPanel.addToActivityWin("Incorrect IP address");
			} catch (UnknownHostException e1) {
				ActivityPanel.addToActivityWin("Unknown Host");
			} catch (IOException e1) {
				ActivityPanel.addToActivityWin("Cannot connect to stream");
			}
			if (s != null) {
				Thread chatThread = new Thread(new ChatWindow(s,
						c.getUserName()));
				chatThread.start();
			}
		}
		if (e.getSource() == btnDelete) {
			int selection = contactList.getSelectedIndex();
			if (selection == -1)
				return;
			String userName = (String) contactList.getSelectedValue();
			try {
				ChatRoom.getContactPrefs().node(userName).removeNode();
				ChatRoom.getContactPrefs().flush();
			} catch (BackingStoreException e2) {
				e2.printStackTrace();
			}
			map.remove(userName);
			listData.removeElementAt(selection);
			contactList.setListData(listData);
			scrollPane.revalidate();
			scrollPane.repaint();

			if (selection >= listData.size())
				selection = listData.size() - 1;
			contactList.setSelectedIndex(selection);
		}
	}

	public static String[] getList() {
		String[] userNames = new String[listData.size()];
		for (int i = 0; i < listData.size(); i++) {
			userNames[i] = (String) listData.get(i);
		}
		return userNames;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) { // if the user selects a
		// contact from the list
		// do....
		if (e.getSource() == contactList && !e.getValueIsAdjusting()) {
			userName = (String) contactList.getSelectedValue();
			if (userName != null) {
				Contact c = (Contact) map.get(userName);
				AddTab.setTabFields(c);
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {

	}

	public static class IpChecker {

		public static String getIP() throws Exception {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(
						whatismyip.openStream()));
				String ip = in.readLine();
				return ip;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						return "";
					}
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (e.getSource().equals(boxToggle)) {
			boxToggle.setText("Local IP");
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (e.getSource().equals(boxToggle)) {
			boxToggle.setText("");
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}
}