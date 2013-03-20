package common;

import java.util.ArrayList;

public abstract class Main<ComponentType extends Component> {	
	
	protected ArrayList<ComponentType> components = new ArrayList<ComponentType>();
	
	protected synchronized void add_connector(ComponentType component) {
		components.add(component);
	}
	
	protected abstract ComponentType make_component();

	protected void start_components(int n) {
		
		for (int i = 0; i < n; i++) {
			class Bind implements Runnable {
				public WorkerThread thread;
				public void run() {
					ComponentType component = make_component();
					component.initialize(Configuration.readConfig(), thread);
					component.test();
					add_connector(component);
				}
			}
			
			Bind b = new Bind();
			b.thread = new WorkerThread();
			b.thread.dispatch(b);
		}
		
		wait_for_components(n);
	}
	
	private void wait_for_components(int n) {
		boolean is_ready;
		synchronized (this) {
			is_ready = n == components.size();
		}
		
		try {
			while (!is_ready) {
				Thread.sleep(100);
				synchronized (this) {
					is_ready = n == components.size();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
