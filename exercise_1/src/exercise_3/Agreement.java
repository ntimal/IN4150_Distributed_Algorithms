package exercise_3;

import java.rmi.RemoteException;

import common.Component;


public class Agreement extends Component<IAgreement> implements IAgreement {

	//contains the sequence of ids for which a message has travelled
	private int[] sequence = new int[friends.size()];
	private int[] values = new int[friends.size()];
	
	public Agreement() throws RemoteException {
		super();
		//initialize sequence array with value -1 and values array with 1
		for(int i=0; i < sequence.length; i++) {
			sequence[i] = -1;
			values[i] = 1;
		}
	}

	public void om(int f, int value) {
		//ToDO broadcast value v
		
		if(f!=0){
			
		}else { // f = 0 base case of recursion
			
		}
	}

	protected void do_test() {
		
	}
	

	private void do_post_message(int value, int from_id) {
		random_delay();
		sequence[from_id] = 1;
		values[from_id] = value;
		
	}

	
	public void post_message(int value, int from_id) {
		class Bind implements Runnable {
			public int value, from_id;
			public Agreement subject;
			public void run() {
				subject.do_post_message(value, from_id);
			}
		}
		
		Bind pm = new Bind();
		pm.from_id = from_id;
		pm.value = value;
		pm.subject = this;
		
		thread.dispatch(pm);
	}

}
 
	
	

