# Merge Sort Algorithm Using Asynchronous Calls
## About

This project consists in implementing a distributed sorting algorithm using asynchronous calls in the context of ICE (Internet Communications Engine). The main features include:

- Organize a list of size **n** entered by console.
- Organize a randomly generated list of size **n**.
- Organize the first column of an `.xlsx` file.

## Getting Started

To run the project you need to follow the next steps:

1. Clone the repository.
2. Open the terminal in the project folder.
3. Start the main server running the following command:
    ```bash
    $ java -jar .\mainServer\build\libs\mainServer.jar
    ```

4. In a new terminal start the worker server running the following command
    ```bash
    $ java -jar .\workerServer\build\libs\workerServer.jar
   // you can execute many workers as you want
   ```
   
5. In a new terminal start the client running the following command:
    ```bash
    $ java -jar .\client\build\libs\client.jar 
    ```

6. Follow the instructions on the console.

## Contributors
- Andrés David Parra García
- Maria Alejandra Mantilla Coral
- Silem Nabib Villa Contreras
- Gerson de Jesus Hurtado Borja

