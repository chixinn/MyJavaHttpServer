package webServer;
import Server.CommunicateThread;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class webServer {
    private static final int PORT=8082;
    public static void main(String[] args)throws IOException{
        ServerSocket server=createServerSocket();
        //配置一些参数
        initServerSocket(server);
        //绑定到本地端口上
        server.bind(new InetSocketAddress("127.0.0.1",PORT),50);
        System.out.println("服务器已经监听:");
        System.out.println(server.getInetAddress()+":"+server.getLocalPort());
        //等待客户端连接
        while(true){
            System.out.println("阻塞等待");
            Socket ClientLinkSocket=server.accept();//创建一个连接套接字
            //在连接后，启动一个线程接管与客户端的交互操作
            new Communicate(ClientLinkSocket).start();
        }
    }
    private  static ServerSocket createServerSocket()throws IOException{
        //创建serversocket
        ServerSocket serverSocket= new ServerSocket();
        return serverSocket;
    }
    private static void initServerSocket(ServerSocket serverSocket)throws IOException{
        //复用端口
        serverSocket.setReuseAddress(true);
        serverSocket.setReceiveBufferSize(64*1024*1024);
    }
}
