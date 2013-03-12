import java.io.Serializable;
import java.rmi.*;

public interface ITotalOrdering extends Remote {
	public class Message implements Serializable, Comparable<Message> {
		private static final long serialVersionUID = 6401339940855190034L;
		
		public String message;
		public int timestamp;
		
		public int compareTo(Message other) {
			return timestamp - other.timestamp;
		}
	}
	
	public void post_message(Message oh_hai) throws RemoteException;
	public void acknowledge(int timestamp, int id_from) throws RemoteException;
}
