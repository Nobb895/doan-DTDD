
package server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class ServerThread implements Runnable {

    private Socket socketOfServer;
    private int clientNumber;
    private BufferedReader is;
    private BufferedWriter os;
    private boolean isClosed;

    public BufferedReader getIs() {
        return is;
    }

    public BufferedWriter getOs() {
        return os;
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public ServerThread(Socket socketOfServer, int clientNumber) {
        this.socketOfServer = socketOfServer;
        this.clientNumber = clientNumber;
        System.out.println("Server thu " + clientNumber + " da khoi dong");
        isClosed = false;
    }

    @Override
    public void run() {
        try {
            // Mở luồng vào ra trên Socket tại Server.
            is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            System.out.println("Khoi dong lam moi thanh cong, ID la: " + clientNumber);
            write("get-id" + "," + this.clientNumber);
            Server.serverThreadBus.sendOnlineList();
            Server.serverThreadBus.mutilCastSend("global-message"+","+"---Member "+this.clientNumber+" đã đăng nhập---");
            String message;
            while (!isClosed) {
                message = is.readLine();
                if (message == null) {
                    break;
                }
                String[] messageSplit = message.split(",");
                if(messageSplit[0].equals("send-to-global")){
                    Server.serverThreadBus.boardCast(this.getClientNumber(),"global-message"+","+"Member "+messageSplit[2]+": "+messageSplit[1]);
                }
                if(messageSplit[0].equals("send-to-person")){
                    Server.serverThreadBus.sendMessageToPersion(Integer.parseInt(messageSplit[3]),"Member "+ messageSplit[2]+" ( tới bạn ): "+messageSplit[1]);
                }
            }
        } catch (IOException e) {
            isClosed = true;
            Server.serverThreadBus.remove(clientNumber);
            System.out.println("Client "+this.clientNumber+" da thoat");
            Server.serverThreadBus.sendOnlineList();
            Server.serverThreadBus.mutilCastSend("global-message"+","+"---Member "+this.clientNumber+" đã thoát---");
        }
    }
    public void write(String message) throws IOException{
        os.write(message);
        os.newLine();
        os.flush();
    }
}
