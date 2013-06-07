package common;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Configuration {
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
					slots.add("rmi://" + options[1] + "/DA/" + i);
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
}
