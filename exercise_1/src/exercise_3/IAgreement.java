package exercise_3;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAgreement extends Remote {
	public int OM(int f, int v, int[] L) throws RemoteException;
}
