package exercise_1;

import java.rmi.RemoteException;
import java.util.ArrayList;

public class MainTest extends common.Main<TotalOrderingTest> {	
	
	/**
	 * The entry point for the program.
	 * 
	 * @param args The command line arguments passed to the program.
	 */
	public static void main(String[] args) {
		new MainTest().run(args);
	}
	
	private void run(String[] args) {
		start_components(Integer.parseInt(args[0]));
		
		for (TotalOrderingTest component : components)
			while (!component.ready())
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		
		ArrayList<Integer> base = components.get(0).getMessages();
		
		System.out.println("ORDER: " + base.toString());
		boolean valid = base.size() == components.size();
		for (int j = 1; j < components.size(); j++) {
			ArrayList<Integer> next = components.get(j).getMessages();
			valid = valid && base.equals(next);
			System.out.println("ORDER: " + next.toString());
		}
		
		System.out.println("TOTAL ORDERING IS " + (valid ? "VALID" : "INVALID"));
		System.exit(valid ? 0 : 1);
	}

	protected TotalOrderingTest make_component() {
		try {
			return new TotalOrderingTest();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}
}
