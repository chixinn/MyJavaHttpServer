package SimpleServer;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;

import java.net.ServerSocket;
import java.net.Socket;

public class Server2 {
    private static final int PORT=8081;//端口号8081
    public static void main(String[] args) throws IOException {

        ServerSocket ss= createServerSocket();
        initServerSocket(ss);
        ss.bind(new InetSocketAddress("127.0.0.1",PORT),50);
        try{
            while(true){
                Socket ClientLinkSocket=ss.accept();//创建一个连接套接字
                //在连接后，启动一个线程接管与客户端的交互操作
                SimpeHTTPServer.ClientHandler ch= new SimpeHTTPServer.ClientHandler(ClientLinkSocket);
                ch.start();
            }

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        System.out.print("WebServer id Listening");
        System.out.println(ss.getInetAddress()+":"+ss.getLocalPort());

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
    private class ClientHandler extends Thread{
        private Socket socket;
        ClientHandler(Socket socket){
            this.socket=socket;
        }
        public void run(){
            super.run();
            //获取用户的IP地址和端口号
            System.out.println("Handling Request："+socket.getInetAddress()+":"+socket.getPort());
            try{
                //创建输出流对象
                //创建输入流对象
                InputStream is=socket.getInputStream();
                OutputStream os=socket.getOutputStream();
                //读取浏览器Get请求,获取文件路径
                String pathStr=getResourcePath(is);
                if(pathStr.equals("WrongPath")) {
                    System.out.println("WrongPath input");
                }
                else {

                }

                //判断文件类型

                //发送文件
                //结束socket连接

            }catch(Exception e){
                System.out.println(e.getMessage());
            }

        }//@overide method


    }
    private String getResourcePath(InputStream input){
        //一般的HTTP报文请求的第一行是"GET /index.html HTTP/1.1"
        //获取中间的"/index.html"
        BufferedReader br= new BufferedReader(new InputStreamReader(input));
        try{
            String readLine=br.readLine();
            String[] split= readLine.split(" ");
            if(split.length!=3){
                return "WrongPath";
            }
            System.out.print(readLine);
            return split[1];
        }catch(IOException e){
            e.printStackTrace();
        }
        return "WrongPath";
    }




    }


