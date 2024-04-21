import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;
 
public class MainServer {
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            communicator.getProperties().setProperty("Ice.MessageSizeMax", "100000000");
            communicator.getProperties().setProperty("Ice.ThreadPool.Server.Size", "10");
            communicator.getProperties().setProperty("Ice.RetryIntervals", "-1");

            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("Merge", "default -p 27402");
            CoordinatorI coordinator = new CoordinatorI();
            adapter.add(coordinator, Util.stringToIdentity("coordinator"));
            adapter.activate();

            communicator.waitForShutdown();
        }
    }
}