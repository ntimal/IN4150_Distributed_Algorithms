package exercise_1;

import java.net.MalformedURLException;
import java.rmi.*;
import java.util.Map.Entry;
import java.util.TreeMap;

import common.Component;

public class TotalOrdering extends Component<ITotalOrdering> implements ITotalOrdering {

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
		clock = Math.max(clock, timestamp / slots.size() + 1);

		Message m = map.get(timestamp);
		if (m == null) {
			m = new Message(timestamp);
			map.put(timestamp, m);
		}
		
		m.fake = m.fake && ack;
		if (ack) m.acks++;
		
		if (m == map.firstEntry().getValue()) {
			while (!m.fake && m.acks == slots.size()) {
				deliver(m.timestamp);
				map.remove(m.timestamp);
				
				Entry<Integer, Message> entry = map.firstEntry();
				if (entry == null)
					break;
				
				m = entry.getValue();
			}
		}
	}
	
	public void post_message(int timestamp) {
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
		
		thread.dispatch(pm);
	}
	
	public void acknowledge(int timestamp, int id_from) {
		class Bind implements Runnable {
			public int timestamp, id_from;
			public TotalOrdering subject;
			public void run() {
				subject.do_acknowledge(timestamp, id_from);
			}
		}
		
		Bind da = new Bind();
		da.timestamp = timestamp;
		da.id_from = id_from;
		da.subject = this;
		
		thread.dispatch(da);
	}
	
	protected void deliver(int timestamp) {
		print("DEL " + timestamp);
	}
	
	private void do_post_message(int timestamp) {
		
		random_delay();
		print("POS " + timestamp);
		
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
					print("Could not connect to remote instance.");
				}
			}
		}
		print("POS DONE " + timestamp);
	}
	
	private void do_acknowledge(int timestamp, int id_from) {
		
		random_delay();
		print("ACK " + timestamp + " FROM " + id_from);
		
		insert_message(timestamp, true);
		
	}
	
	private void broadcast() {
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
				}
			}
		}
	}
	
	protected void do_test() {
		broadcast();
	}
}
