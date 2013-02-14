public class Main {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("Starting LocateRegistry...");
			java.rmi.registry.LocateRegistry.createRegistry(9090);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
