package exercise_2;

import java.rmi.RemoteException;
import java.util.ArrayList;

import common.WorkerThread;

public class Component extends common.Component<IComponent> implements IComponent {
	
	private static final long serialVersionUID = -622675372815599726L;
	private ArrayList<Integer> state_received;
	private ArrayList<Integer> state_sent;
	private boolean recording = false;
	private ArrayList<ArrayList<Integer>> message_buffer;
	private int clock = 0;
	
	public Component() throws RemoteException {
		super();
	}
	
	public void initialize(ArrayList<String> slots, WorkerThread thread) {
		super.initialize(slots, thread);
		
		state_received = new ArrayList<Integer>();
		state_sent = new ArrayList<Integer>();
		
		for (int i = 0; i < slots.size(); i++) {
			state_received.add(-1);
			state_sent.add(-1);
		}
	}

	public void post_message(int from_id, int data) {
		class Bind implements Runnable {
			public int from_id, data;
			public Component subject;
			public void run() {
				subject.do_post_message(from_id, data);
			}
		}
		
		Bind pm = new Bind();
		pm.from_id = from_id;
		pm.data = data;
		pm.subject = this;
		
		thread.dispatch(pm);
	}

	public void post_marker(int from_id) {
		// The C++ coder wonders why Java sucks so much!
		// thread->dispatch(boost::bind(&Component::do_post_marker, this, from_id));
		
		class Bind implements Runnable {
			public int from_id;
			public Component subject;
			public void run() {
				subject.do_post_marker(from_id);
			}
		}
		
		Bind pm = new Bind();
		pm.from_id = from_id;
		pm.subject = this;
		
		thread.dispatch(pm);
	}
	
	private void do_post_message(int from_id, int data) {
		random_delay();
		
		if (recording)
			message_buffer.get(from_id).add(data);
		else
			state_received.set(from_id, data);
	}
	
	private void do_post_marker(int from_id) {
		random_delay();
		
		if (recording) {
			print("CHANEL " + from_id + " -> " + id + ": " + message_buffer.get(from_id));
		} else {
			print("CHANEL " + from_id + " -> " + id + ": []");
			record_global_state();
		}
	}
	
	protected void do_test() {
		try {
			for (int i = 0; i < friends.size(); i++) {
				IComponent friend = friends.get(i);
				int msg_id = clock++ * slots.size() + id;
				state_sent.set(i, msg_id);
				friend.post_message(id, msg_id);
			}
			
			if (id == 0)
				record_global_state();
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void record_global_state() {
		recording = true;
		print("SENT     " + state_sent);
		print("RECEIVED " + state_received);
		
		message_buffer = new ArrayList<ArrayList<Integer>>();
		for (IComponent friend : friends) {
			try {
				friend.post_marker(id);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			message_buffer.add(new ArrayList<Integer>());
		}
	}
}
