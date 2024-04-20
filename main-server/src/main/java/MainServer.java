import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

public class MainServer {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            // quiero que use protocolo tcp y puerto 10000
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Merge","default -p 10000");
            com.zeroc.Ice.Object object = new CoordinatorI();
            adapter.add(object, Util.stringToIdentity("Coordinator"));
            adapter.activate();
            communicator.waitForShutdown();
        }
    }
}

