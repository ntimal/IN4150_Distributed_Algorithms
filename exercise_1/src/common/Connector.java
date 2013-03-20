package common;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;

import exercise_1.ITotalOrdering;

public abstract class Connector extends UnicastRemoteObject {
	private static final long serialVersionUID = -6481677105759654371L;
	
	protected int id = -1;
	protected ArrayList<String> slots;
	protected ArrayList<ITotalOrdering> friends;
	protected Random rng = new Random();
	
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
	
	public Connector() throws RemoteException {
		super();
	}
	
	public synchronized void initialize(ArrayList<String> slots) {
		this.slots = slots;
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
	private void connect() {
		friends = new ArrayList<ITotalOrdering>();
		
		for (int i = 0; i < slots.size(); i++) {
			String uri = slots.get(i);
			while (true) {
				try {
					ITotalOrdering remote = (ITotalOrdering) Naming.lookup(uri);
					friends.add(remote);
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

	public abstract void test();
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
