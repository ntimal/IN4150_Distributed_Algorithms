package exercise_1;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ITotalOrdering extends Remote {
	public void post_message(int timestamp) throws RemoteException;
	public void acknowledge(int timestamp, int id_from) throws RemoteException;
}
