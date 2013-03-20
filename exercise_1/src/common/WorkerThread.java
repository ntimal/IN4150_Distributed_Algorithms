package common;
import java.util.LinkedList;
import java.util.Queue;

public class WorkerThread implements Runnable {
	private Queue<Runnable> queue = new LinkedList<Runnable>();
	private Thread thread;
	private boolean do_stop = false;
	
	public WorkerThread() {
		thread = new Thread(this);
		thread.start();
	}
	
	public synchronized void stop() {
		do_stop = true;
	}
	
	public void dispatch(Runnable task) {
		synchronized (queue) {
			queue.add(task);
		}
	}
	
	public void run() {
		boolean l_do_stop = false;
		
		while (!l_do_stop) {
			
			Runnable task;
			synchronized (queue) {
				task = queue.poll();
			}
			
			while (task != null) {
				task.run();
				synchronized (queue) {
					task = queue.poll();
				}
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			synchronized (this) {
				l_do_stop = do_stop;
			}
		}
	}
}
