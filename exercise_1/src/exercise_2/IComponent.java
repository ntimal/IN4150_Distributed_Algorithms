package exercise_2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IComponent extends Remote {
	void post_message(int from_id, int data) throws RemoteException;
	void post_marker(int from_id) throws RemoteException;
}
