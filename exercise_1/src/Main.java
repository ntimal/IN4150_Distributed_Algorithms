import java.rmi.*;

public class Main {

	static final int port = 9090;
	static final String uri_base = "rmi://localhost:" + port + "/";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Starting LocateRegistry...");
			java.rmi.registry.LocateRegistry.createRegistry(port);

//			if (System.getSecurityManager() == null) {
//				System.setSecurityManager(new RMISecurityManager());
//			}
			
			System.out.println("Bind TotalOrdering...");			
			Naming.bind(uri_base + "TotalOrdering", new TotalOrdering());
			
			System.out.println("Lookup TotalOrdering...");			
			ITotalOrdering remote_total_ordering = (ITotalOrdering) Naming.lookup(uri_base + "TotalOrdering");
			
			System.out.println("Execute method...");			
			remote_total_ordering.test(1337);
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("END");			
	}
}
