package Server;
import java.net.ServerSocket;
import java.net.Socket;


public class WebServer {


    public static void main(String[] args) {
        int Port = 12345;//端口号，由于这里是测试，所以不要使用常用端口
        //创建两个套接字
        ServerSocket server = null;
        Socket client = null;
        try{
            server = new ServerSocket(Port);
            //服务器开始监听
            System.out.println("The WebServer is listening on port "+server.getLocalPort());
            while(true){
                client = server.accept();
                //多线程运行
                new CommunicateThread(client).start();
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

}