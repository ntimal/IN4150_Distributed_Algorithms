package exercise_2;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class Component extends common.Component<IComponent> implements IComponent {
	
	private static final long serialVersionUID = -622675372815599726L;
	private int amount = 0;
	private boolean recording = false;
	private ArrayList<ArrayList<Integer>> message_buffer;
	
	public Component() throws RemoteException {
		super();
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
		
		if (recording) {
			message_buffer.get(from_id).add(data);
		} else {
			amount += data;
		}
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
			for (IComponent friend : friends) {
				amount -= 10;
				friend.post_message(id, 3);
				friend.post_message(id, 7);
			}
			
			if (id == 0)
				record_global_state();
			
			for (IComponent friend : friends) {
				amount -= 5;
				friend.post_message(id, 5);
				
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void record_global_state() {
		recording = true;
		print("AMOUNT " + amount);
		
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
