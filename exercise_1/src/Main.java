import java.io.*;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Main {	
	/**
	 * Read the configuration file.
	 * 
	 * @returns a list of the available slots.
	 */
	public static ArrayList<String> readConfig() {
		ArrayList<String> slots = new ArrayList<String>();
		
		try {
			FileInputStream fstream = new FileInputStream("../etc/hosts.conf");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] options = strLine.split(" ");
				for (int i = 0; i < Integer.parseInt(options[0]); i++)
					slots.add("rmi://" + options[1] + "/TotalOrdering/" + i);
			}
			
			in.close();
			fstream.close();
		} catch (IOException e) {
			System.out.println("Error reading config file");
			e.printStackTrace();
			System.exit(1);
		}
		
		return slots;
	}
	
	/**
	 * The entry point for the program.
	 * 
	 * @param args The command line arguments passed to the program.
	 */
	public static void main(String[] args) {
		for (int i = 0; i < Integer.parseInt(args[0]); i++)
			new Thread(new Runnable() {
				public void run() {
					try {
						Connector connector = new TotalOrderingTest();
						connector.initialize(readConfig());
						connector.test();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}).start();
	}
}
