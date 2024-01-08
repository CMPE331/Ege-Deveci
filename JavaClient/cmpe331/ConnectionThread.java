package cmpe331;

import com.fazecast.jSerialComm.SerialPort;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionThread extends Thread {

    private int espPort;
    //private Mongo mongo;
	private volatile String commandToSend;
	final Object lock = new Object();

    public ConnectionThread(int espPort) {
        this.espPort = espPort;
        //this.mongo = mongo;
    }

    @Override
    public void run() {
    	while(true) {

    		
    		tryConnectESP();
    		
    		tryConnectCOM();
    		
    		
    		/*
    		if (!mongo.checkConnection()) {
    			mongo.reconnect();
    		}
    		*/
    		

    	}
    }

    private void tryConnectESP() {
        try {
            ServerSocket espServerSocket = new ServerSocket(espPort);
            Socket espSocket = espServerSocket.accept();
            if (espServerSocket != null && !espServerSocket.isClosed()) {

                System.out.println("Connected to ESP8266 on: " + espPort);
                PrintWriter arduinoOut = new PrintWriter(espSocket.getOutputStream(), true);
                
                BufferedReader arduinoIn = new BufferedReader(new InputStreamReader(espSocket.getInputStream()));
                
                StringBuilder continuousBuffer = new StringBuilder();
                
	                while(espSocket.isConnected()) {
	                	
	                    synchronized (lock) {
	                        if (commandToSend != null) {
	                        	try {
	                                arduinoOut.println(commandToSend);
	                                arduinoOut.flush();
	                            } catch (Exception e) {
	                                e.printStackTrace();
	                            }
	                            commandToSend = null;  // Reset the command after sending
	                        }
	                    }
	                	
	            		
	            		char[] buffer = new char[1];
	            		int bytesRead = arduinoIn.read(buffer);
	            		char receivedChar = (char) buffer[0];
	            		
	            		if (bytesRead > 0 && receivedChar != '\n' && receivedChar != '\r') {
	                        
	                        continuousBuffer.append(receivedChar);
	            		}
	            		if (continuousBuffer.length() > 0) {
	            			handleData(continuousBuffer.toString());
	            		}
	                	
	                }

                // Close the socket when done
                espSocket.close();
                espServerSocket.close();
            }
        } catch (IOException e) {
            // Handle ESP8266 connection failure
            System.err.println("Failed to connect to ESP8266. Retrying...");
        }
    }

    private void tryConnectCOM() {
        try {
        	SerialPort comPort = null;
        	 // Get a list of available COM ports
            SerialPort[] ports = SerialPort.getCommPorts();

            if (ports.length == 0) {
                System.out.println("No COM ports found. Make sure your Arduino is connected.");
                }


            // for loop chooses a com port don't change
            for (int i = 0; i < ports.length; i++) {
                comPort = ports[i];
                comPort.openPort();
                if (comPort.isOpen()) {
                    System.out.println("Connected to Arduino on port: " + comPort.getSystemPortName());
                    break; // Break the loop if the port is successfully opened
                } else {
                    System.out.println("Failed to open port: " + comPort.getSystemPortName());
                }
            }
            
            if (comPort != null && comPort.isOpen()) {
            	System.out.println("proceed");
                while(comPort.isOpen()) {
                	
                    synchronized (lock) {
                        if (commandToSend != null) {
                        	try {
                                comPort.writeBytes(commandToSend.getBytes(), commandToSend.length());
                                comPort.getOutputStream().flush(); // Ensure data is sent immediately
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            commandToSend = null;  // Reset the command after sending
                        }
                    }
                	
            		StringBuilder continuousBuffer = new StringBuilder();
                    byte[] buffer = new byte[1];
                    int bytesRead = comPort.readBytes(buffer, 1);

                    if (bytesRead > 0) {
                        char receivedChar = (char) buffer[0];
                        continuousBuffer.append(receivedChar);

                        // Check if the received data forms a complete "ard::" command
                        if (continuousBuffer.toString().startsWith("ard::") && receivedChar == '\n') {
                            // Print the received "ard::" command
                            String command = continuousBuffer.toString().trim();
                            
                            System.out.println(command);
                            // Process the received command
                            handleData(command);

                            // Reset the buffer for the next "ard::" command
                            continuousBuffer.setLength(0);
                        }
                    }
                	
                }
            }
        } catch (Exception e) {
            // Handle COM port connection failure
            System.err.println("Failed to connect to COM port. Retrying...");
        }
    }
    
    public void setCommandToSend(String command) {
        synchronized (lock) {
            this.commandToSend = command;
        }
    }

    private void handleData(String message) {
    	if(message.indexOf("ard::") != -1) {
    		System.out.println(message);
    	}

    }

    public void send(String data) {
        // Add your implementation to send data
        System.out.println("Sending data: " + data);
    }


    
}
