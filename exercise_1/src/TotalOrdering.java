import java.net.MalformedURLException;
import java.rmi.*;
import java.util.Map.Entry;
import java.util.TreeMap;

public class TotalOrdering extends Connector implements ITotalOrdering {
	
	private class Message implements Comparable<Message> {
		
		public int timestamp;
		public int acks = 0;
		public boolean fake = true;
		
		public Message(int timestamp) {
			this.timestamp = timestamp;
		}
		
		public int compareTo(Message other) {
			return timestamp - other.timestamp;
		}
	}
	
	private static final long serialVersionUID = -5840789656649365132L;
	
	private int clock = 0;
	
	private TreeMap<Integer, Message> map = new TreeMap<Integer, Message>();
	
	public TotalOrdering() throws RemoteException {
		super();
	}
	
	private void insert_message(int timestamp, boolean ack) {
		Message m = map.get(timestamp);
		if (m == null) {
			m = new Message(timestamp);
			map.put(timestamp, m);
		}
		
		m.fake = m.fake && ack;
		if (ack) m.acks++;
		
		if (m == map.firstEntry().getValue()) {
			while (!m.fake && m.acks == slots.size()) {
				print("DEL " + m.timestamp);
				map.remove(m.timestamp);
				
				Entry<Integer, Message> entry = map.firstEntry();
				if (entry == null)
					break;
				
				m = entry.getValue();
			}
		}
	}
	
	public void post_message(int timestamp) throws RemoteException {
		class Bind implements Runnable {
			public int timestamp;
			public TotalOrdering subject;
			public void run() {
				subject.do_post_message(timestamp);
			}
		}
		
		Bind pm = new Bind();
		pm.timestamp = timestamp;
		pm.subject = this;
		new Thread(pm).start();
	}
	
	public void acknowledge(int timestamp, int id_from) throws RemoteException {
		class Bind implements Runnable {
			public int timestamp, id_from;
			public TotalOrdering subject;
			public void run() {
				subject.do_acknowledge(timestamp, id_from);
			}
		}
		
		Bind pm = new Bind();
		pm.timestamp = timestamp;
		pm.id_from = id_from;
		pm.subject = this;
		new Thread(pm).start();
	}
	
	private synchronized void do_post_message(int timestamp) {
		print("POS " + timestamp);
		
		clock = Math.max(clock, timestamp / slots.size() + 1);
		
		insert_message(timestamp, false);
		
		for (int i = 0; i < friends.size(); i++) {
			ITotalOrdering remote = friends.get(i);
			
			try {
				remote.acknowledge(timestamp, id);
			} catch (RemoteException e) {
				try {
					remote = (ITotalOrdering) Naming.lookup(slots.get(i));
					friends.set(i, remote);
					remote.acknowledge(timestamp, id);
				} catch (MalformedURLException | NotBoundException | RemoteException e2) {
					System.out.println("Could not connect to remote instance.");
					System.out.println(e2.toString());
				}
			}
		}
	}
	
	private synchronized void do_acknowledge(int timestamp, int id_from) {
		print("ACK " + timestamp + " FROM " + id_from);
		
		insert_message(timestamp, true);
	}
	
	/**
	 * Broadcast a massage to all friends.
	 */
	public synchronized void broadcast() {
		int timestamp = clock++ * slots.size() + id;
		print("BRO " + timestamp);
		
		for (int i = 0; i < friends.size(); i++) {
			ITotalOrdering remote = friends.get(i);
			
			try {
				remote.post_message(timestamp);
			} catch (RemoteException e) {
				try {
					remote = (ITotalOrdering) Naming.lookup(slots.get(i));
					friends.set(i, remote);
					remote.post_message(timestamp);
				} catch (MalformedURLException | NotBoundException | RemoteException e2) {
					print("Could not connect to remote instance.");
					e2.printStackTrace();
				}
			}
		}
	}
	
	public void test() {
		broadcast();
	}
}
