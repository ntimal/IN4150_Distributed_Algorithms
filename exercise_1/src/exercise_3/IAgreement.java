package exercise_3;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IAgreement extends Remote {
	public void post_message(int value, int from_id) throws RemoteException;
}
