import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class TotalOrdering extends UnicastRemoteObject implements ITotalOrdering {
	private static final long serialVersionUID = -5840789656649365132L;
	private static final int port = 9090;
	private static final String uri_base = "rmi://localhost:" + port + "/";

	private static ArrayList<ITotalOrdering> friends = new ArrayList<ITotalOrdering>();
	
	protected TotalOrdering() throws RemoteException {
		super();
	}

	public void test(String oh_hai) throws RemoteException {
		System.out.println("Received: " + oh_hai);
	}
	
	public static void main(String[] args) {
		try {
			int id = Integer.parseInt(args[0]);
			int n  = Integer.parseInt(args[1]);
			
			System.out.println("ID: " + id + ", N: " + n);

//			if (System.getSecurityManager() == null) {
//				System.setSecurityManager(new RMISecurityManager());
//			}
			
			System.out.println("Bind...");
			Naming.bind(uri_base + "TotalOrdering/" + id, new TotalOrdering());
			
			System.out.println("Connecting to friends...");
			while (friends.size() != n) {
				try {
					ITotalOrdering remote = (ITotalOrdering) Naming.lookup(uri_base + "TotalOrdering/" + friends.size());
					friends.add(remote);
				} catch (java.rmi.NotBoundException e) {
					Thread.sleep(500);
				}
			}
			
			System.out.println("Broadcast...");
			for (ITotalOrdering remote : friends)
				remote.test("from " + id + ": Hi!");
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("END");			
	}
}
