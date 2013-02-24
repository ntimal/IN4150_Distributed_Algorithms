import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Main {
	private static int id = -1;
	private static ArrayList<String> slots = new ArrayList<String>();
	private static ArrayList<ITotalOrdering> friends = new ArrayList<ITotalOrdering>();
	
	private static void readConfig() {
		try {
			FileInputStream fstream = new FileInputStream("../etc/hosts.conf");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] options = strLine.split(" ");
				for (int i = 0; i < Integer.parseInt(options[0]); i++)
					slots.add("rmi://" + options[1] + "/TotalOrdering/" + i);
			}
			
			in.close();
			fstream.close();
		} catch (Exception e) {
			System.out.println("Error reading config file");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private static void bind(TotalOrdering instance) throws RemoteException {
		String uri;
		
		for (id = 0; id < slots.size(); id++) {
			
			uri = slots.get(id);
			
			try {
				Naming.bind(uri, instance);
			} catch (Exception e) {
				continue;
			}
			
			Runtime.getRuntime().addShutdownHook(new Unbinder(uri));
			
			System.out.println("BIND: " + id + ": " + uri);
			System.out.println();
			return;
		}
		
		System.out.println("Faild to bind!");
		System.exit(1);
	}
	
	private static void connect() throws InterruptedException {
		for (int i = 0; i < slots.size(); i++) {
			String uri = slots.get(i);
			while (true) {
				try {
					ITotalOrdering remote = (ITotalOrdering) Naming.lookup(uri);
					friends.add(remote);
					System.out.println("CONNECTED: " + i + ": " + uri);
					break;
				} catch (Exception e) {
					Thread.sleep(500);
				}
			}
		}
		System.out.println();
	}
	
	private static void broadcast() throws RemoteException {
		for (int i = 0; i < friends.size(); i++) {
			ITotalOrdering remote = friends.get(i);
			try {
				remote.test("from " + id + ": Hi!");
			} catch (ConnectException e) {
				try {
					remote = (ITotalOrdering) Naming.lookup(slots.get(i));
					friends.set(i, remote);
					remote.test("from " + id + ": Hi!");
				} catch (Exception e2) {
					System.out.println("Could not connect to remote instance.");
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			readConfig();
			bind(new TotalOrdering());
			
			System.out.println("Connecting to friends...");
			connect();
			
			System.out.println("Broadcast...");
			Thread.sleep(1000);
			broadcast();
			
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("END");			
	}
}

class Unbinder extends Thread {
	public String uri;
	
	public Unbinder(String uri) {
		this.uri = uri;
	}
	
	public void run() {
		System.out.println("Unbinding uri");
		try {
        	Naming.unbind(uri);
        } catch (Exception e) {
			Runtime.getRuntime().halt(1);
		}
    }
}
