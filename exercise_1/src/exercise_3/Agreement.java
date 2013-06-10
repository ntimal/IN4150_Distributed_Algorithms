package exercise_3;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

import common.Component;

public class Agreement extends Component<IAgreement> implements IAgreement {

	private static final long serialVersionUID = 4885586288870737183L;
	private static final int faults = 2;
	
	public Agreement() throws RemoteException {}
	
	class Node {
		public int value = -1;
		public Node[] children;
		
		public Node(int n) {
			children = new Node[n];
		}

		public int get_value() {
			return get_value("");
		}
		
		public int get_value(String indent) {
			if (value != -1)
				return value;
			
			int l = 0;
			for (int i = 0; i < children.length; i++)
				if (children[i] != null) l++;
			
			int a[] = new int[l];
			for (int i = 0, j = 0; i < children.length; i++)
				if (children[i] != null) a[j++] = children[i].get_value(indent + "    ");
			
			value = majority(a);
			print(indent + Arrays.toString(a) + " -> " + value);
			return value;
		}
	}
	
	Node root;
	
	private int majority(int[] a) {
		Arrays.sort(a);
		return a[a.length / 2];
	}

	private int[] set_remove(int[] a, int v) {
		int[] r = new int[a.length - 1];
		for (int i = 0, j = 0; i < a.length; i++)
			if (a[i] != v) r[j++] = a[i];
		return r;
	}

	private int[] set_add(int[] a, int v) {
		int[] r = new int[a.length + 1];
		for (int i = 0; i < a.length; i++)
			r[i] = a[i];
		r[a.length] = v;
		return r;
	}
	
	public void OM(int f, int v, int[] L, int[] C) throws RemoteException {
		String indent = "";
		for (int i = 0; i < (faults - f); i++)
			indent += "    ";
		
		print(indent + "OM(" + f + ", " + v + ", " + Arrays.toString(L) + ", " + Arrays.toString(C) + ")");
		
		Node current = root;
		for (int i = 0; i < C.length; i++) {
			
			int c = C[i];
			if (current.children[c] == null)
				current.children[c] = new Node(friends.size());
			
			current = current.children[c];
		}
		current.value = v;
		
		if (f < 0)
			return;
		
		for (int i = 0; i < L.length; i++) {
			if (faulty()) v = fault();
			int p = L[i];
			
			// BROAD CAST: OM(f - 1, v, set_remove(L, p), set_add(C, p));
			IAgreement remote = friends.get(p);
			
			try {
				remote.OM(f - 1, v, set_remove(L, p), set_add(C, p)); // << MESSAGE
			} catch (RemoteException e) {
				try {
					remote = (IAgreement) Naming.lookup(slots.get(p));
					friends.set(p, remote);
					remote.OM(f - 1, v, set_remove(L, p), set_add(C, p)); // << MESSAGE
				} catch (MalformedURLException | NotBoundException | RemoteException e2) {
					print(indent + "Could not connect to remote instance, assuming default.");
				}
			}
			
			// END BROADCAST
		}
	}

	private boolean faulty() {
		return id == 1 || id == 4;
	}
	
	private int fault() throws RemoteException {
		if (id < 3) return rng.nextInt(2);
		throw new RemoteException();
	}
	
	protected void do_test() {
		
		root = new Node(friends.size());
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if (id == 0) {
			int[] L = new int[friends.size() - 1];
			
			for (int i = 0; i < friends.size() - 1; i++)
				L[i] = i + 1;
			
			int[] C = {0};
			
			try {
				OM(faults, 1, L, C);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		try {
			Thread.sleep(5000 + id * 100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		print("Result: " + root.get_value());
	}
}
