package exercise_3;
import java.rmi.RemoteException;

public class Main extends common.Main<Agreement> {	
	
	/**
	 * The entry point for the program.
	 * 
	 * @param args The command line arguments passed to the program.
	 */
	public static void main(String[] args) {
		Main program = new Main();
		program.start_components(Integer.parseInt(args[0]));
	}

	protected Agreement make_component() {
		try {
			return new Agreement();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
}
