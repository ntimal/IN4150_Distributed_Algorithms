package common;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

import exercise_1.TotalOrdering;

public abstract class Component<RemoteType extends Remote> extends UnicastRemoteObject {
	private static final long serialVersionUID = -6481677105759654371L;
	
	protected int id = -1;
	protected ArrayList<String> slots;
	protected ArrayList<RemoteType> friends;
	protected Random rng = new Random();
	protected WorkerThread thread;

	protected void print(String message) {
		System.out.println("" + id + " " + message);
	}
	
	protected void random_delay() {
		try {
			Thread.sleep(rng.nextInt(1000));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public Component() throws RemoteException {
		super();
	}
	
	public void initialize(ArrayList<String> slots, WorkerThread thread) {
		this.slots = slots;
		this.thread = thread;
		bind();
		connect();
	}
	
	/**
	 * Bind instance to a slot.
	 * 
	 * After this function has been executed an id has been assigned.
	 */
	private void bind() {
		String uri;
		
		for (id = 0; id < slots.size(); id++) {
			
			uri = slots.get(id);
			
			try {
				Naming.bind(uri, this);
			} catch (MalformedURLException | AlreadyBoundException | RemoteException e) {
				continue;
			}
			
			Runtime.getRuntime().addShutdownHook(new Unbinder(uri));
			
			print("BIN " + uri);
			return;
		}
		
		print("Faild to bind!");
	}
	
	/**
	 * Connect to all friends.
	 * 
	 * After this function has been executed the friends property has been filled.
	 */
	@SuppressWarnings("unchecked")
	private void connect() {
		friends = new ArrayList<RemoteType>();
		
		for (int i = 0; i < slots.size(); i++) {
			String uri = slots.get(i);
			while (true) {
				try {
					friends.add((RemoteType) Naming.lookup(uri));
					print("CON " + i + " " + uri);
					break;
				} catch (MalformedURLException | NotBoundException | RemoteException e) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e2) {}
				}
			}
		}
	}

	public void test() {
		class Bind implements Runnable {
			public Component<RemoteType> subject;
			public void run() {
				subject.do_test();
			}
		}
		
		Bind br = new Bind();
		br.subject = this;
		
		thread.dispatch(br);
	}
	
	protected abstract void do_test();
}

class Unbinder extends Thread {
	public String uri;
	
	public Unbinder(String uri) {
		this.uri = uri;
	}
	
	public void run() {
		System.out.println("Unbinding " + uri);
		try {
        	Naming.unbind(uri);
        } catch (RemoteException | MalformedURLException | NotBoundException e) {
			Runtime.getRuntime().halt(1);
		}
    }
}
