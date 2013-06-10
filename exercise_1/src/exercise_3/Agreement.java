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

	private int[][] transpose(int[][] A) {
		int[][] R = new int[A[0].length][A.length];
		for (int i = 0; i < A.length; i++) {
			for (int j = 0; j < A[i].length; j++) {
				R[j][i] = A[i][j];
			}
		}
		return R;
	}
	
	public int[] OM(int f, int v, int[] L) throws RemoteException {
		String indent = "";
		for (int i = 0; i < (faults - f); i++)
			indent += "    ";
		
		print(indent + "OM(" + f + ", " + v + ", " + Arrays.toString(L) + ")");
		
		int[] R = new int[L.length + 1];
		
		if (f >= 0) {
			int[][] V = new int[L.length][];
			//if (faulty()) v = fault();
			
			for (int i = 0; i < L.length; i++) {
				if (faulty()) v = fault();
				// BROAD CAST and gather: V[i] = OM(f - 1, v, set_remove(L, L[i]));
				int p = L[i];
				IAgreement remote = friends.get(p);
				
				try {
					V[i] = remote.OM(f - 1, v, set_remove(L, p)); // << MESSAGE
				} catch (RemoteException e) {
					try {
						remote = (IAgreement) Naming.lookup(slots.get(p));
						friends.set(p, remote);
						V[i] = remote.OM(f - 1, v, set_remove(L, p)); // << MESSAGE
					} catch (MalformedURLException | NotBoundException | RemoteException e2) {
						print(indent + "Could not connect to remote instance, assuming default.");
						V[i] = new int[L.length];
					}
				}
				
				// END BROADCAST
			}
			
			V = transpose(V);
			
			for (int i = 0; i < V.length; i++)
				R[i+1] = majority(V[i]);
			R[0] = v;
		} else {
			for (int i = 0; i < R.length; i++)
				R[i] = v;
		}
		
		print(indent + "Result: " + Arrays.toString(R));
		return R;
	}

	private boolean faulty() {
		return id == 1 || id == 4;
	}
	
	private int fault() throws RemoteException {
		if (id < 3) return rng.nextInt(2);
		throw new RemoteException();
	}
	
	protected void do_test() {
		int[] L = new int[friends.size() - 1];
		for (int i = 0; i < friends.size() - 1; i++)
			L[i] = i + 1;
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if (id == 0)
			try {
				OM(faults, 1337, L);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
	}
}
