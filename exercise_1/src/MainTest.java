import java.rmi.RemoteException;
import java.util.ArrayList;


public class MainTest {

	private static ArrayList<TotalOrderingTest> connectors = new ArrayList<TotalOrderingTest>();
	
	private static synchronized void add_connector(TotalOrderingTest connector) {
		connectors.add(connector);
	}
	
	/**
	 * The entry point for the program.
	 * 
	 * @param args The command line arguments passed to the program.
	 */
	public static void main(String[] args) {
		
		try {
			ArrayList<Thread> threads = new ArrayList<Thread>();
			
			for (int i = 0; i < Integer.parseInt(args[0]); i++) {
				Thread thread = new Thread(new Runnable() {
					public void run() {
						try {
							TotalOrderingTest connector = new TotalOrderingTest();
							MainTest.add_connector(connector);
							connector.initialize(Main.readConfig());
							connector.test();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				});
				threads.add(thread);
				thread.start();
			}

			for (Thread thread : threads)
				thread.join();
			
			for (TotalOrderingTest connector : connectors)
				while (!connector.ready())
					Thread.sleep(100);
			
			ArrayList<Integer> base = connectors.get(0).getMessages();
			
			System.out.println("BASE: " + base.toString());
			boolean valid = base.size() == connectors.size();
			for (int j = 1; j < connectors.size(); j++) {
				ArrayList<Integer> next = connectors.get(j).getMessages();
				valid = valid && base.equals(next);
				System.out.println("NEXT: " + next.toString());
			}
			
			System.out.println("TOTAL ORDERING IS " + (valid ? "VALID" : "INVALID"));
			System.exit(valid ? 0 : 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
