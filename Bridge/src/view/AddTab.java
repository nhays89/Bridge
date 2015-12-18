package view;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.prefs.BackingStoreException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import prefobj.PrefObj;

import com.jhlabs.awt.ParagraphLayout;

public class AddTab extends JPanel implements ActionListener {
	JLabel lblUserName, lblFName, lblLName, lblPublicIP, lblLocalIP, lblPort;
	static JTextField txtUserName, txtFName, txtLName, txtPublicIP, txtLocalIP,
			txtPort;
	JLabel label;
	String fName;
	String lName;
	String publicIP;
	String localIP;
	String userName;
	String port;
	private JButton btnSubmit;
	public Contact user;

	public AddTab() {
		setBackground(Color.BLACK);
		setLayout(new ParagraphLayout(40, 30, 10, 10, 10, 10));
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
		btnSubmit = new JButton("Submit");
		btnSubmit.setBackground(Color.black);
		btnSubmit.setPreferredSize(new Dimension(200,29));
		add(btnSubmit, ParagraphLayout.NEW_LINE);
		btnSubmit.addActionListener(this);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(btnSubmit)) {
			boolean editContact = false;
			userName = txtUserName.getText();
			fName = txtFName.getText();
			lName = txtLName.getText();
			publicIP = txtPublicIP.getText();
			localIP = txtLocalIP.getText();
			port = txtPort.getText();
			user = new Contact(userName, fName, lName, publicIP,
					localIP, port);
			TreeMap map = ContactList.getMap();
			if (user.checkFields() == false) {
				return;
			}
				if (map.containsKey(userName)) {
					Contact c = (Contact) map.get(userName);
					if (c.equals(user)) {
						return;
					}
					editContact = true;
					map.replace(userName, c, user);//edit contact
				}		
			addToPrefs();
			if (!editContact) ContactList.addToMap(userName, user);
			ContactList.revalidateContactList();
			Component[] c = this.getComponents();
			for (Component d : c) {
				if (d.getClass().getName().equals("javax.swing.JTextField")) {
					JTextField comp = (JTextField) d;
					comp.setText("");
				}
			}
		}
	}

	private void addToPrefs() {
		try {
			PrefObj.putObject(ChatRoom.getContactPrefs().node(userName), userName, user);
		} catch (ClassNotFoundException e1) {
			ActivityPanel.addToActivityWin("Prefs Class not found");
		} catch (IOException e1) {		
			ActivityPanel.addToActivityWin("Prefs Class error");
		} catch (BackingStoreException e1) {		
			ActivityPanel.addToActivityWin("Prefs Backing Store error");
		}
	}

	public static void setTabFields(Contact c) {
		txtUserName.setText(c.getUserName());
		txtFName.setText(c.getFName());
		txtLName.setText(c.getLName());
		txtPublicIP.setText(c.getPublicIP());
		txtLocalIP.setText(c.getLocalIP());
		txtPort.setText("" + c.getPort());
	}
}
