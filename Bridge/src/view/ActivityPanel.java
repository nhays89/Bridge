package view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;


public class ActivityPanel extends JPanel {
	
	private static JTextArea txtArea;
	private static Date d;
	public ActivityPanel(Date d){
		this.d = d;
		setBackground(Color.black);
		setLayout(new BorderLayout());
		JPanel rightPanelCenter = new JPanel();
		txtArea = new JTextArea();
		txtArea.setLineWrap(true);
		txtArea.setWrapStyleWord(true);
		txtArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(txtArea);
		scrollPane.setPreferredSize(new Dimension(245, 300));
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		rightPanelCenter.add(scrollPane);
		rightPanelCenter.setBackground(Color.black);
		rightPanelCenter.setPreferredSize(new Dimension(245,300));
		add(rightPanelCenter, BorderLayout.CENTER);
		JPanel rightPanelSouth = new JPanel(new FlowLayout());
		rightPanelSouth.setBackground(Color.black);
		rightPanelSouth.setPreferredSize(new Dimension(245, 45));
		add(rightPanelSouth, BorderLayout.SOUTH);
		JPanel rightPanelNorth = new JPanel(new FlowLayout());
		rightPanelNorth.setBackground(Color.black);
		rightPanelNorth.setPreferredSize(new Dimension(245, 25));
		add(rightPanelNorth, BorderLayout.NORTH);
		JLabel consoleTitle = new JLabel("Activity");
		rightPanelNorth.add(consoleTitle);
	}

	public static void addToActivityWin(String message){
		txtArea.append(message + "\n");
	}

}
