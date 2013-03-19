import java.rmi.RemoteException;
import java.util.ArrayList;


public class MainTest {

	static ArrayList<TotalOrderingTest> connectors = new ArrayList<TotalOrderingTest>();
	
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
							MainTest.connectors.add(connector);
							System.out.println("Binding and connecting");
							connector.initialize(Main.readConfig());
							System.out.println("Broadcasting");
							connector.test();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						System.out.println("End of single thread");
					}
				});
				threads.add(thread);
				thread.start();
			}
			System.out.println("Waiting for threads to join");
			for (Thread thread : threads)
				thread.join();
			
			System.out.println("Waiting for connectors to get ready");
			for (TotalOrderingTest connector : connectors)
				while (!connector.ready())
					Thread.sleep(100);
			
			System.out.println("Checking whether messages are delivered in the right order");
			ArrayList<Integer> base = connectors.get(0).getMessages();
			
			boolean valid = base.size() == connectors.size();
			for (int j = 1; j < connectors.size(); j++)
				valid = valid && base.equals(connectors.get(j).getMessages());
			
			System.out.println(valid ? "SUCCESS" : "FAIL");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
