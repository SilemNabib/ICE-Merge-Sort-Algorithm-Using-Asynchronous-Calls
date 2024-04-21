import java.util.Arrays;

import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.Util;

import General.AMISortCallbackPrx;
import General.MergeResult;

public class Client implements General.AMISortCallback {

    public static void main(String[] args) {
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("MergeClient", "default -p 0");
            Client client = new Client();
            adapter.add(client, Util.stringToIdentity("client"));
            adapter.activate();

            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("coordinator:default -p 27402");
            General.MergeCoordinatorPrx coordinator = General.MergeCoordinatorPrx.checkedCast(base);
            if (coordinator == null) {
                throw new Error("Invalid proxy");
            }
            System.out.println("Client connected to server");
            while (true) {
                int value = Integer.parseInt(System.console().readLine("Enter a number to sort: "));
                int[] data = new int[value];
                for (int i = 0; i < data.length; i++) {
                    data[i] = (int) (Math.random() * 10);
                }
                AMISortCallbackPrx callback = AMISortCallbackPrx.uncheckedCast(adapter.createProxy(Util.stringToIdentity("client")));
                coordinator.startMergeSort(data, callback);
            }
        }
    }

    @Override
    public synchronized void sortResult(MergeResult result, com.zeroc.Ice.Current current) {
        System.out.println("Received sorted data"+ Arrays.toString(result.data));
    }
}