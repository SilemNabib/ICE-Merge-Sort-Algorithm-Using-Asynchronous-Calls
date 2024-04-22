import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

/**
 * The WorkerServer class is responsible for initializing and managing a worker server that serves the main server.<br>
 * Each WorkerServer instance represents an individual worker server that can receive and process an array of data to sort.<br>
 * <br>
 * WorkerServer uses the Ice library from ZeroC for inter-process communication and worker management.
 * @author Andres Parra
 * @author Alejandro Mantilla
 * @author Silem Nabib (Documentation)
 */
public class WorkerServer {
    static General.MergeCoordinatorPrx coordinator;

    /**
     * Initializes the worker server and registers it with the coordinator server.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {

            // Create an adapter an initialize the worker
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("MergeWorker", "default -p 0");
            WorkerI worker = new WorkerI();
            adapter.add(worker, Util.stringToIdentity("worker"));
            adapter.activate();

            // Register the worker with the coordinator
            com.zeroc.Ice.ObjectPrx main = communicator.stringToProxy("coordinator:default -p 27402");
            coordinator = General.MergeCoordinatorPrx.checkedCast(main);
            if (coordinator == null) {
                throw new Error("Invalid proxy");
            } else {
                coordinator.registerWorker(
                        General.MergeWorkerPrx.checkedCast(adapter.createProxy(Util.stringToIdentity("worker"))));
            }

            communicator.waitForShutdown();
        }
    }
}