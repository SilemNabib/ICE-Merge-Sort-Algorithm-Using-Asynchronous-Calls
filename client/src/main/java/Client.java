import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import General.AMISortCallback;
import General.MergeCoordinatorPrx;
import com.zeroc.Ice.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import General.AMISortCallbackPrx;
import General.MergeResult;

/**
 * The Client class is responsible for initializing and managing a client that connects to the main server.
 * @author Andres Parra
 * @author Alejandra Mantilla
 * @author Silem Nabib (Documentation)
 */
public class Client implements AMISortCallback {
    private CountDownLatch latch;

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

            int[] data;

            do {
                data = menu();
                if (data.length != 0) {
                    client.latch = new CountDownLatch(1);
                    AMISortCallbackPrx callback = AMISortCallbackPrx.uncheckedCast(
                            adapter.createProxy(Util.stringToIdentity("client")));
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
        System.out.println("Received sorted data: "+ Arrays.toString(result.data));
        latch.countDown();
    }

    public static int[] menu(){
        Scanner sc = new Scanner(System.in);

        System.out.println("""
                \s
                ¬ | Menu\s
                1. Generate a random List.\s
                2. Insert a List.\s
                3. Load data to sort.\s
                0. Exit.\s
               \s""");

        System.out.print("Select an option: ");
        int selection = sc.nextInt();
        sc.nextLine();

        return menuSelection(selection);
    }

    public static int[] menuSelection(int selection){
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

                    list = readList(fileName);
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

        return list;
    }

    public static int[] generateList(){
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter the size of the list: ");

        int value = sc.nextInt();
        sc.nextLine();

        int[] data = new int[value];

        for (int i = 0; i < data.length; i++) {
            data[i] = (int) (Math.random() * 10);
        }

        return data;
    }

    public static int[] createList(){
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter the size of the list: ");
        int size = sc.nextInt();
        sc.nextLine();

        int[] list = new int[size];

        for (int i = 0; i < size; i++){
            System.out.print("Enter the value for position " + i+1 + ": ");
            list[i] = sc.nextInt();
            sc.nextLine();
        }

        System.out.println("\nList generated: " + Arrays.toString(list));
        return list;
    }

    public static int[] readList(String filePath) {
        File assetsDir = new File("assets");
        String absolutePath = assetsDir.getAbsolutePath();

        List<Integer> numbers = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(assetsDir, filePath + ".xlsx"));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // get first sheet

            for (Row row : sheet) {
                Cell cell = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL); // get first cell of each row
                if (cell != null) {
                    int number = (int) cell.getNumericCellValue();
                    numbers.add(number);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return numbers.stream().mapToInt(i -> i).toArray();
    }
}