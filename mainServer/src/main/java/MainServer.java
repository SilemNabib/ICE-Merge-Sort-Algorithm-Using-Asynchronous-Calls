import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

/**
 * The main server is responsible for coordinating the parallel sorting process using multiple workers.<br>
 * Each worker receives a portion of the data to be sorted, performs the sorting, and returns the result to the main server.
 *
 * @author Andres Parra
 * @author Alejandra Mantilla
 * @author Silem Nabib (Documentation)
 */
public class MainServer {

    /**
     * Initializes the main server and configures its properties.<br>
     * @param args command-line arguments
     */
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