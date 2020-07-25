import com.sun.nio.sctp.HandlerResult;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ProxyServer {
    private static int Port;//代理监听的端口
    private ServerSocket loSocket;//本地套接字
    public ProxyServer()throws IOException{
        loSocket=new ServerSocket(Port);
    }
    public void start(){
        try{
            System.out.println("Listen on "+Port);
            while(true){
                Socket ct=loSocket.accept();
                new HandleRequest(ct).start();//建立线程处理客户端请求，即代理；
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[]args){
        Port=7089;
        try{
            new ProxyServer().start();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
