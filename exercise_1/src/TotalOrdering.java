import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class TotalOrdering extends UnicastRemoteObject implements ITotalOrdering {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5840789656649365132L;

	protected TotalOrdering() throws RemoteException {
		super();
	}

	public void test(int oh_hai) throws RemoteException {
		System.out.println("Received: " + oh_hai);
	}
}
