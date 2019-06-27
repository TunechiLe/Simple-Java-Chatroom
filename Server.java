package Server;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame{
	 private JTextField userText;
	 private JTextArea chatWindow;
	 private ObjectOutputStream output; 
	 private ObjectInputStream input;
	 private ServerSocket server;
	 private Socket connection;
	
	 //Constructor 
	 public Server() {
		 super("Instant Messenger");
		 
		 userText = new JTextField();
		 //By default before you are connected with another user you are not allowed to type anything
		 userText.setEditable(false);
		 userText.addActionListener(
				 new ActionListener() {
					 public void actionPerformed(ActionEvent event) {
						 sendMessage(event.getActionCommand());
						 userText.setText("");
					 }
				 
				 });
		 add(userText, BorderLayout.NORTH);
		 chatWindow = new JTextArea();
		 add(new JScrollPane(chatWindow));
		 setSize(300, 150);
		 setVisible(true);
	 }
	 
	 //Set up and run server
	 public void startRunning() {
		 try {
			 server = new ServerSocket(6789, 100);
			 while(true) {
				 try {
					 waitForConnection();
					 setupStreams();
					 whileChatting();
				 }catch(EOFException eofException) {
					 showMessage("\n Server ended the connection!");
				 }finally {
					 closeStreams();
				 }
			 }
		 }catch(IOException ioException) {
			 ioException.printStackTrace();
		 }
	 }
	 
	 //Wait for connection and display information
	 private void waitForConnection() throws IOException {
		 showMessage("Waiting for someone to connection...");
		 connection = server.accept();
		 showMessage("Now connected to " + connection.getInetAddress());
	 }
	 
	 //Get stream to send and receive data
	 private void setupStreams() throws IOException {
		 output = new ObjectOutputStream(connection.getOutputStream());
		 output.flush();
		 input = new ObjectInputStream(connection.getInputStream());
		 showMessage("\nStreams are now setup! \n");
	 }
	 
	 //During the chat conversation
	 private void whileChatting() throws IOException {
		 String message = "You are now connected!";
		 sendMessage(message);
		 ableToType(true);
		 do {
			 try {
				 message = (String) input.readObject();
				 showMessage("\n" + message);
			 }catch(ClassNotFoundException classNotFoundException) {
				 showMessage("\nMessage fail to send");
			 }
		 }while (!message.equals("CLIENT - END"));
	 }
	 
	 //Close streams and sockets after done chatting
	 private void closeStreams() {
		 showMessage("\nClosing connection... \n");
		 ableToType(false);
		 try{
			 output.close();
			 input.close();
			 connection.close();
		 }catch(IOException ioException) {
			 ioException.printStackTrace();
		 }
	 }
	 
	 //Send message to client
	 private void sendMessage(String message) {
		 try{
			 output.writeObject("SERVER - " + message);
			 output.flush();
			 showMessage("\nSERVER - " + message);
		 }catch(IOException ioException) {
			 chatWindow.append("\nERROR: CAN'T SEE MESSAGE");
		 }
	 }
	 
	 //Updates chatWindow
	 private void showMessage(final String text) {
		 SwingUtilities.invokeLater(
				new Runnable() {
					 public void run() {
						 chatWindow.append(text);
					}
				}
		);
	 }
	 
	 //Let the user to type
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
			Server s = new Server();
			s.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			s.startRunning();

		}
}
