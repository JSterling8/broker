import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by anon on 21/10/2015.
 */
public class Main {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void listenSocket(){


        //Create socket connection
        try{
            socket = new Socket("127.0.0.1", 8079);
            out = new PrintWriter(socket.getOutputStream(),
                    true);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            for(int i = 0; i < 1000; i++) {
                out.print("Testing " + i + "\n");
            }

            socket.close();

        } catch (UnknownHostException e) {
            System.out.println("Unknown host: 127.0.0.1");
            System.exit(1);
        } catch  (IOException e) {
            System.out.println("No I/O");
            System.exit(1);
        }
    }

    public static void main(String[] args){
        Main main = new Main();
        main.listenSocket();
    }

}
