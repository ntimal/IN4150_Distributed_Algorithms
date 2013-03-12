import java.net.MalformedURLException;
import java.rmi.*;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class TotalOrdering extends Connector implements ITotalOrdering {
	private static final long serialVersionUID = -5840789656649365132L;
	
	private int clock = 0;
	
	private PriorityQueue<Message> queue = new PriorityQueue<Message>();
	private TreeMap<Integer, Integer> ack_table = new TreeMap<Integer, Integer>();
	
	public TotalOrdering() throws RemoteException {
		super();
	}
	
	private Message construct_message(String message) {
		Message m = new Message();
		m.message = message;
		m.timestamp = clock++ * slots.size() + id;
		return m;
	}
	
	public void post_message(Message oh_hai) throws RemoteException {
		class Bind implements Runnable {
			public Message oh_hai;
			public TotalOrdering subject;
			public void run() {
				subject.do_post_message(oh_hai);
			}
		}
		
		Bind pm = new Bind();
		pm.oh_hai = oh_hai;
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
	
	private synchronized void do_post_message(Message oh_hai) {
		print("POS " + oh_hai.timestamp);
		queue.add(oh_hai);
		
		for (int i = 0; i < friends.size(); i++) {
			ITotalOrdering remote = friends.get(i);
			
			try {
				remote.acknowledge(oh_hai.timestamp, id);
			} catch (RemoteException e) {
				try {
					remote = (ITotalOrdering) Naming.lookup(slots.get(i));
					friends.set(i, remote);
					remote.acknowledge(oh_hai.timestamp, id);
				} catch (MalformedURLException | NotBoundException | RemoteException e2) {
					System.out.println("Could not connect to remote instance.");
					System.out.println(e2.toString());
				}
			}
		}
	}
	
	private synchronized void do_acknowledge(int timestamp, int id_from) {
		print("ACK " + timestamp + " FROM " + id_from);
		
		int value = 1;
		
		Integer in = ack_table.get(timestamp);
		if (in != null)
			value += in.intValue();
		
		ack_table.put(timestamp, value);
		
		while (!queue.isEmpty()) {
			in = ack_table.get(queue.peek().timestamp);
			if (in == null || in != slots.size())
				break;
			
			Message m = queue.poll();
			deliver_message(m);
			ack_table.remove(m.timestamp);
		}
	}
	
	private void deliver_message(Message message) {
		print("DEL " + message.timestamp + ": " + message.message);
	}
	
	/**
	 * Broadcast a massage to all friends.
	 */
	public synchronized void broadcast(String message) {
		Message m = construct_message(message);
		print("BRO " + m.timestamp);
		
		for (int i = 0; i < friends.size(); i++) {
			ITotalOrdering remote = friends.get(i);
			
			try {
				remote.post_message(m);
			} catch (RemoteException e) {
				try {
					remote = (ITotalOrdering) Naming.lookup(slots.get(i));
					friends.set(i, remote);
					remote.post_message(m);
				} catch (MalformedURLException | NotBoundException | RemoteException e2) {
					print("Could not connect to remote instance.");
					e2.printStackTrace();
				}
			}
		}
	}
	
	public void test() {
		broadcast("This is a broadcast form process " + id + ".");
	}
}
