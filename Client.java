package Client;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame {
	
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	
	//constructor
	public Client(String host) {
		super("Client");
		serverIP = host;
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						sendMessage(event.getActionCommand());
						userText.setText("");
					}
				}
		);
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setSize(300, 150);
		setVisible(true);
	}
	
	//Start
	public void starRunning() {
		try {
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException eofException) {
			showMessage("\nClient terminated connection");
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}finally{
			closeStream();
		}
	}
	
	//Connect to server
	private void connectToServer() throws IOException {
		showMessage("Attempting connection... \n");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		showMessage("Connected to:" + connection.getInetAddress().getHostName());
	}
	
	//Set up streams to send and receive messages
	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\nYou are now connected \n");
	}
	
	//While chatting with server
	private void whileChatting() throws IOException {
		ableToType(true);
		do {
			try{
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException classNotFoundException) {
				showMessage("\nCannot display message. Please try again.");
			}
		}while(!message.equals("SERVER - END"));
	}
	
	//Close the streams and sockets
	private void closeStream() {
		showMessage("\nClosing client...");
		ableToType(false);
		try {
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException) {
			ioException.printStackTrace();
		}
	}
	
	//Send messages to server
	private void sendMessage(String message) {
		try {
			output.writeObject("CLIENT - " + message);
			output.flush();
			showMessage("\nCLIENT - " + message);
		}catch(IOException ioException) {
			chatWindow.append("\nERROR SENDING MESSAGE");
		}
	}
	
	//Update chat window
	private void showMessage(final String m) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						chatWindow.append(m);
					}
				}
		);
	}
	
	//Gives user permission to type
	private void ableToType(final boolean tof) {
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						userText.setEditable(tof);
					}
				}
		);
	}
	
	public static void main(String[] args) {
		Client c;
		c = new Client("127.0.0.1");
		c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.starRunning();
	}

}
