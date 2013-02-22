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
			int n, id = 0;
			try {
				n  = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Usage: program <n>");
				System.exit(0);
				return;
			}
			
//			if (System.getSecurityManager() == null) {
//				System.setSecurityManager(new RMISecurityManager());
//			}
			
			System.out.println("Bind...");
			
			String uri = "error";
			while (true) {
				try {
					uri = uri_base + "TotalOrdering/" + id;
					Naming.bind(uri, new TotalOrdering());
				} catch (AlreadyBoundException e) {
					id++;
					continue;
				} catch (ConnectException e) {
					System.out.println("Failed to connect to registry.");
					System.exit(0);
				} catch (ServerException e) {
					System.out.println("Registry could not find your classes.");
					System.exit(0);
				}
				break;
			}
			
			Runtime.getRuntime().addShutdownHook(new Unbinder(uri));
			
			System.out.println("Bound on: " + uri);
			
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
			for (int i = 0; i < friends.size(); i++) {
				ITotalOrdering remote = friends.get(i);
				try {
					remote.test("from " + id + ": Hi!");
				} catch (ConnectException e) {
					try {
						remote = (ITotalOrdering) Naming.lookup(uri_base + "TotalOrdering/" + i);
						friends.set(i, remote);
						remote.test("from " + id + ": Hi!");
					} catch (NotBoundException e2) {
						System.out.println("Could not connect to remote instance.");
					} catch (ConnectException e2) {
						System.out.println("Could not connect to remote instance.");
					}
				}
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println("END");			
	}
}

class Unbinder extends Thread {
	public String uri;
	
	public Unbinder(String uri) {
		this.uri = uri;
	}
	
	public void run() {
		System.out.println("Unbinding uri");
		try {
        	Naming.unbind(uri);
        } catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
    }
}
