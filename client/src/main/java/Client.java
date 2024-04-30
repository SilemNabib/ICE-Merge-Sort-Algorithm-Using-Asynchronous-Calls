import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.zeroc.Ice.Communicator;
import com.zeroc.Ice.Current;
import com.zeroc.Ice.ObjectAdapter;
import com.zeroc.Ice.ObjectPrx;
import com.zeroc.Ice.Util;

import General.AMISortCallback;
import General.AMISortCallbackPrx;
import General.MergeCoordinatorPrx;
import General.MergeResult;

/**
 * The Client class is responsible for initializing and managing a client that
 * connects to the main server.
 * 
 * @author Andres Parra
 * @author Alejandra Mantilla
 * @author Silem Nabib
 */
public class Client implements AMISortCallback {
    private CountDownLatch latch;
    private boolean testingMode = false;
    public int[] lastResult;

    public static void main(String[] args) {
        try (Communicator communicator = Util.initialize(args)) {
            ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("MergeClient", "default -p 0");
            Client client = new Client();
            adapter.add(client, Util.stringToIdentity("client"));
            adapter.activate();

            ObjectPrx base = communicator.stringToProxy("coordinator:default -p 27402");
            MergeCoordinatorPrx coordinator = MergeCoordinatorPrx.checkedCast(base);
            if (coordinator == null) {
                throw new Error("Invalid proxy");
            }
            System.out.println("Client connected to server");

            AMISortCallbackPrx callback = AMISortCallbackPrx.uncheckedCast(
                    adapter.createProxy(Util.stringToIdentity("client")));

            int[] data;

            do {
                data = menu(client);

                if (client.testingMode) {
                    startTests(client, coordinator, callback);
                } else if (data.length != 0) {
                    client.latch = new CountDownLatch(1);

                    coordinator.startMergeSort(data, callback);

                    client.latch.await();
                }
            } while (data.length != 0);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void sortResult(MergeResult result, Current current) {
        System.out.println("Received sorted data: " + Arrays.toString(result.data));
        lastResult = result.data;
        if (latch != null)
            latch.countDown();
    }

    public static int[] menu(Client client) {
        Scanner sc = new Scanner(System.in);

        System.out.println("""
                 \s
                 ¬ | Menu\s
                 1. Generate a random List.\s
                 2. Insert a List.\s
                 3. Load data to sort.\s
                 9. Run tests.\s
                 0. Exit.\s
                \s""");

        System.out.print("Select an option: ");
        int selection = sc.nextInt();
        sc.nextLine();
        return menuSelection(client, selection);
    }

    public static int[] menuSelection(Client client, int selection) {
        Scanner sc = new Scanner(System.in);
        int[] list = new int[0];

        boolean exit;

        do {
            exit = true;

            switch (selection) {
                case 1:
                    list = generateList();
                    break;
                case 2:
                    list = createList();
                    break;
                case 3:
                    System.out.print("Enter the file name: ");
                    String fileName = sc.nextLine();

                    list = loadFile(fileName);
                    break;
                case 9:
                    System.out.println("Running tests...");
                    client.testingMode = true;
                    break;
                case 0:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
                    exit = false;
                    break;
            }
        } while (!exit);
        sc.close();
        return list;
    }

    public static int[] generateList() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the size of the list: ");

        int value = sc.nextInt();
        sc.nextLine();

        int[] data = new int[value];

        for (int i = 0; i < data.length; i++) {
            data[i] = (int) (Math.random() * 10);
        }
        sc.close();
        return data;
    }

    public static int[] createList() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter the size of the list: ");
        int size = sc.nextInt();
        sc.nextLine();

        int[] list = new int[size];

        for (int i = 0; i < size; i++) {
            System.out.print("Enter the value for position " + i + 1 + ": ");
            list[i] = sc.nextInt();
            sc.nextLine();
        }
        sc.close();
        System.out.println("\nList generated: " + Arrays.toString(list));
        return list;
    }

    private static int[] loadFile(String fileName) {
        return loadFile(fileName, 0);
    }

    private static int[] loadFile(String fileName, int sheetIndex) {
        File assetsDir = new File("assets");

        List<Integer> numbers = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(assetsDir, fileName + ".xlsx"));
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(sheetIndex); // get first sheet

            for (Row row : sheet) {
                Cell cell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL); // get first cell of each row
                if (cell != null) {
                    int number = (int) cell.getNumericCellValue();
                    numbers.add(number);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return numbers.stream().mapToInt(i -> i).toArray();
    }

    private static void startTests(Client client, MergeCoordinatorPrx coordinator, AMISortCallbackPrx callback)
            throws InterruptedException {
        System.out.println("Starting tests...");

        System.out.println("\nTest 1: " + (test1(client, coordinator, callback) ? "passed" : "failed") + ".\n");
        System.out.println("\nTest 2: " + (test2(client, coordinator, callback) ? "passed" : "failed") + ".\n");
        System.out.println("\nTest 3: " + (test3(client, coordinator, callback) ? "passed" : "failed") + ".\n");
        System.out.println("\nTest 4: " + (test4(client, coordinator, callback) ? "passed" : "failed") + ".\n");
        System.out.println("\nTest 5: " + (test5(client, coordinator, callback) ? "passed" : "failed") + ".\n");
    }

    private static boolean test1(Client client, MergeCoordinatorPrx coordinator, AMISortCallbackPrx callback)
            throws InterruptedException {
        // Test 1: Test for empty list
        System.out.println("Test 1: Test for empty list");
        int[] data = new int[0];
        int[] expected = new int[0];
        System.out.println("Sent data: " + Arrays.toString(data));

        long startTime = System.nanoTime();
        coordinator.startMergeSort(data, callback);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Lasted: " + duration + " nanoseconds");
        
        client.latch = new CountDownLatch(1);
        client.latch.await();

        return Arrays.equals(client.lastResult, expected);
    }

    private static boolean test2(Client client, MergeCoordinatorPrx coordinator, AMISortCallbackPrx callback)
            throws InterruptedException {
        // Test 2: Test for list with one element
        System.out.println("Test 2: Test for list with one element");
        int[] data = { 1 };
        int[] expected = { 1 };
        System.out.println("Sent data: " + Arrays.toString(data));

        long startTime = System.nanoTime();
        coordinator.startMergeSort(data, callback);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Lasted: " + duration + " nanoseconds");

        client.latch = new CountDownLatch(1);
        client.latch.await();

        return Arrays.equals(client.lastResult, expected);
    }

    private static boolean test3(Client client, MergeCoordinatorPrx coordinator, AMISortCallbackPrx callback)
            throws InterruptedException {
        // Test 3: Test for unsorted list
        System.out.println("Test 3: Test for unsorted list");
        int[] data = loadFile("TestFiles", 0);
        int[] expected = loadFile("TestFiles", 1);
        System.out.println("Sent data: " + Arrays.toString(data));

        long startTime = System.nanoTime();
        coordinator.startMergeSort(data, callback);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Lasted: " + duration + " nanoseconds");

        client.latch = new CountDownLatch(1);
        client.latch.await();

        return Arrays.equals(client.lastResult, expected);
    }

    private static boolean test4(Client client, MergeCoordinatorPrx coordinator, AMISortCallbackPrx callback)
            throws InterruptedException {
        // Test 4: Test for sorted list
        System.out.println("Test 4: Test for sorted list");
        int[] data = loadFile("TestFiles", 2);
        int[] expected = loadFile("TestFiles", 3);
        System.out.println("Sent data: " + Arrays.toString(data));

        long startTime = System.nanoTime();
        coordinator.startMergeSort(data, callback);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Lasted: " + duration + " nanoseconds");

        client.latch = new CountDownLatch(1);
        client.latch.await();

        return Arrays.equals(client.lastResult, expected);
    }

    private static boolean test5(Client client, MergeCoordinatorPrx coordinator, AMISortCallbackPrx callback)
            throws InterruptedException {
        // Test 5: Test for a list with repeated elements
        System.out.println("Test 5: Test for a list with repeated elements");
        int[] data = loadFile("TestFiles", 4);
        int[] expected = loadFile("TestFiles", 5);
        System.out.println("Sent data: " + Arrays.toString(data));

        long startTime = System.nanoTime();
        coordinator.startMergeSort(data, callback);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Lasted: " + duration + " nanoseconds");

        client.latch = new CountDownLatch(1);
        client.latch.await();

        return Arrays.equals(client.lastResult, expected);
    }
}