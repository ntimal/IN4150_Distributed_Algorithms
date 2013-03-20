package exercise_1;
import java.rmi.RemoteException;
import common.Configuration;
import common.Connector;



public class Main {	
	
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
						Connector connector = new TotalOrdering();
						connector.initialize(Configuration.readConfig());
						connector.test();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}).start();
	}
}
