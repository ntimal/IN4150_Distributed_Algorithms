package exercise_3;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAgreement extends Remote {
	public void OM(int f, int v, int[] L, int[] C) throws RemoteException;
}
