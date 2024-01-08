package cmpe331;

import javax.swing.SwingUtilities;

public class Main {
	
	private static String mongoString = "mongodb+srv://admin:Cmpe331@cmpe331.imjg0l0.mongodb.net/?retryWrites=true&w=majority";
	private static String dbName = "Appointment_System";
	private static String collectionNameAppointmentQueue = "Appointment_Queue";
	private static String collectionNamePersonnelList = "PersonnelList";
	private static String collectionNameLogs = "Logs";
	private static String collectionNameCards = "Cards";
	
	private static String[][] guestList;

	private static String[][] personnelList;
	
	private static Mongo mongo;

	private static int espPort = 54321;
	
	private static ConnectionThread connection = new ConnectionThread(espPort);
	
	
	public static String[][] getGuestList() {
		return guestList;
	}
	
	public static void setGuestList(String[][] guestList) {
		Main.guestList = guestList;
	}
	public static void main(String[] args) {
		
		mongo = new Mongo(mongoString, dbName);
		
		
		connection.start();
		
		personnelList = mongo.getTop8(collectionNamePersonnelList);
		resetGuestList();
		
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);		

		
        });
	}
	
	public static void arduinoSend(String command) {
		connection.setCommandToSend(command);
	}
	
	public static void deleteFirst() {
		mongo.deleteFirst(collectionNameAppointmentQueue);
	}

	public static void resetGuestList() {
		guestList = mongo.getTop8(collectionNameAppointmentQueue);
	}
	
	public static void resetPersonnelList() {
		personnelList = mongo.getTop8(collectionNamePersonnelList);
	}
	public static void addCards() {
		guestList = mongo.getTop8(collectionNameCards);
	}

	
}
