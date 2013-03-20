package exercise_1;

import java.rmi.RemoteException;
import java.util.ArrayList;


public class TotalOrderingTest extends TotalOrdering {

	private static final long serialVersionUID = -4994180382164897126L;
	private ArrayList<Integer> messages = new ArrayList<Integer>();
	
	public TotalOrderingTest() throws RemoteException {
		super();
	}
	
	protected synchronized void deliver(int timestamp){
		super.deliver(timestamp);
		messages.add(timestamp);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized ArrayList<Integer> getMessages() {
		return (ArrayList<Integer>) messages.clone();
	}

	public synchronized boolean ready() {
		return messages.size() == slots.size();
	}
}
