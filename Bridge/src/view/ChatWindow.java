package view;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ChatWindow extends JFrame implements Runnable, ActionListener, WindowListener, KeyListener {
	private JPanel contentPane;
	private JPanel panel;
	private JTextArea textArea;
	private JScrollPane scrollPane;
	private JTextField typeMsg;
	private JPanel bottomPanel;
	private JButton btnSend;
	private Contact contact;
	private Socket socket;
	private ObjectInputStream Sinput;
	private ObjectOutputStream Soutput;
	private Object incomingMsg;
	private String outGoingMsg;
	private String friend;
	private boolean isConnected = true;
	public ChatWindow(Socket s, String friend) {
		this.socket = s;
		setTitle(ChatRoom.getUserProfilePrefs().get("userName", "No Name"));
		addWindowListener(this);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		panel = new JPanel(new BorderLayout());
		contentPane.add(panel);
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		scrollPane = new JScrollPane(textArea);
		panel.add(scrollPane, BorderLayout.CENTER);
		bottomPanel = new JPanel(new FlowLayout());
		panel.add(bottomPanel, BorderLayout.SOUTH);
		typeMsg = new JTextField(31);
		typeMsg.setHorizontalAlignment(JTextField.LEFT);
		bottomPanel.add(typeMsg);
		btnSend = new JButton("Send");
		btnSend.setMnemonic(KeyEvent.VK_ENTER);
		btnSend.addActionListener(this);
		bottomPanel.add(btnSend);
		setContentPane(contentPane);
		typeMsg.addActionListener(this);
		this.setTitle(friend);
		this.friend = friend;
		setVisible(true);
	}
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(btnSend)){
		outGoingMsg = typeMsg.getText();
		try {
			textArea.append("\n" + ChatRoom.getUserProfilePrefs().get("userName", "No Name") + ": " + outGoingMsg);
			Soutput.writeObject(outGoingMsg);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		typeMsg.setText("");
		}
	}

	@Override
	public void windowClosing(WindowEvent e){
		if(e.getWindow().equals(this)){
			try {
				Soutput.writeObject("is disconnected");
				if(socket.isConnected()){
				Soutput.close();
				Sinput.close();
				socket.close();
				}
				isConnected = false;
				this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			} catch (IOException e1) {
				ActivityPanel.addToActivityWin("heeeeoooo");
			}
		}
	}
	@Override
	public void run() {
		while(true){
			if (socket.isConnected()){
				break;
		}
		}
		try {
			Soutput = new ObjectOutputStream(socket.getOutputStream());
			Sinput = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			ActivityPanel.addToActivityWin("Connection interrupted. Try again in a few momements" + e.getMessage());				
		}
		
		while (isConnected) {//on own thread?
			incomingMsg = null;
			try {
				incomingMsg = (String) Sinput.readObject();
			} catch (ClassNotFoundException | IOException e) {
				isConnected = false;
				typeMsg.setEnabled(false);
				break;
			}	
			textArea.append("\n" + friend + ": "+  incomingMsg);
			}
		}
	
	@Override
	public void windowActivated(WindowEvent arg0) {
	}
	@Override
	public void windowClosed(WindowEvent arg0) {	
	}
	@Override
	public void windowDeactivated(WindowEvent arg0) {	
	}
	@Override
	public void windowDeiconified(WindowEvent arg0) {	
	}
	@Override
	public void windowIconified(WindowEvent arg0) {		
	}
	@Override
	public void windowOpened(WindowEvent arg0) {		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER){
			outGoingMsg = typeMsg.getText();
			try {
				textArea.append("\n" + ChatRoom.getUserProfilePrefs().get("userName", "No Name") + ": " + outGoingMsg);
				Soutput.writeObject(outGoingMsg);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			typeMsg.setText("");
			}
		}
	@Override
	public void keyReleased(KeyEvent e) {	
	}
	@Override
	public void keyTyped(KeyEvent e) {	
	}
}
