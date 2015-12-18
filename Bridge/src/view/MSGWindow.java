package view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

public class MSGWindow extends JFrame implements ActionListener, Runnable {
	JButton btnYes, btnNo;
	boolean answer;
	Object mon;
	public MSGWindow(String message, int offset){
		getContentPane().setBackground(Color.BLACK);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.black);
		add(mainPanel);
		mainPanel.add(new JLabel(message, offset), BorderLayout.CENTER);
		setSize(300,100);
		setVisible(true);
	}
	public MSGWindow(String title, String message, int offset){
		this.setTitle(title);
		getContentPane().setBackground(Color.BLACK);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.black);
		add(mainPanel);
		mainPanel.add(new JLabel(message, offset), BorderLayout.CENTER);
		setSize(300,100);
		setVisible(true);
	}
	public MSGWindow(String title, String message, int offset, int width){
		this.setTitle(title);
		getContentPane().setBackground(Color.BLACK);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.black);
		add(mainPanel);
		mainPanel.add(new JLabel(message, offset), BorderLayout.CENTER);
		setSize(300 + (width * 3),100);
		setVisible(true);
	}
	public MSGWindow(String message, int offset, boolean isBtnWin, Object mon){
		this.mon = mon;
		getContentPane().setBackground(Color.BLACK);
		JPanel mainPanel = new JPanel();
		add(mainPanel);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBackground(Color.black);
		mainPanel.add(new JLabel(message, offset), BorderLayout.CENTER);
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout());
		southPanel.setBackground(Color.black);
		mainPanel.add(southPanel, BorderLayout.SOUTH);
		btnYes = new JButton("Yes");
		btnNo = new JButton("No");
		southPanel.add(btnYes);
		southPanel.add(btnNo);
		btnYes.setPreferredSize(new Dimension(150, 30));
		btnNo.setPreferredSize(new Dimension(150, 30));
		btnYes.addActionListener(this);
		btnNo.addActionListener(this);
		setSize(500,150);
		setVisible(true);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(btnYes)){
			answer = true;
			synchronized (mon){
				mon.notifyAll();
			}
			this.dispose();
		}
		if(e.getSource().equals(btnNo)){
			answer = false;
			synchronized (mon){
				mon.notifyAll();
			}
			this.dispose();
		}	
	}
	public boolean getAnswer(){
		return answer;
	}
	@Override
	public void run() {
	}
}
