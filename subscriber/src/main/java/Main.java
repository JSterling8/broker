import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Created by anon on 21/10/2015.
 */
public class Main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException{
        //get the localhost IP address, if server is running on some other IP, you need to use that
        Socket socket = new Socket("127.0.0.1", 8078);

        ObjectInputStream ois = null;
        while(true){
            //read the server response message
            ois = new ObjectInputStream(socket.getInputStream());
            String message = (String) ois.readObject();
            System.out.println("Message: " + message);
            //close resources
            ois.close();
            Thread.sleep(100);
        }
    }
}
