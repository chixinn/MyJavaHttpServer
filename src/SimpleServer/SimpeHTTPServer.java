package SimpleServer;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;

import java.net.ServerSocket;
import java.net.Socket;


public class SimpeHTTPServer {
    private static final int PORT=8081;
    public static void main(String[]args)throws IOException{
        ServerSocket ss= createServerSocket();
        initServerSocket(ss);
        ss.bind(new InetSocketAddress("127.0.0.1",PORT),50);
        //等待客户端连接中
        System.out.print("WebServer waiting for access");
        System.out.println(ss.getInetAddress()+":"+ss.getLocalPort());
        for(;;){
            System.out.println("阻塞等待");
            Socket ClientLinkSocket=ss.accept();//创建一个连接套接字
            //在连接后，启动一个线程接管与客户端的交互操作
            ClientHandler ch= new ClientHandler(ClientLinkSocket);
            ch.start();
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
    //处理与客户端的交互:
    //交互1：从这个连接接受HTTP请求
    //交互2：解释该请求已确定所请求的特定文件
    //从服务器的文件系统获得请求的文件
    //创建响应报文Response
    private static class ClientHandler extends Thread{
        private Socket socket;
        //Web资源根路径
        public static final String ROOT="~/";
        ClientHandler(Socket socket){
            this.socket=socket;
        }
        public void run(){
            super.run();
            System.out.println("处理客户端请求："+socket.getInetAddress()+":"+socket.getPort());
            try{
                //得到socket的输入输出
                InputStream is=socket.getInputStream();
                OutputStream os=socket.getOutputStream();
                String full_path=read(is,os);
               StringBuffer res= response(full_path);
               if(res==null){
                   System.out.println("Null impossible except Exception");
               }
               else {
                   os.write(res.toString().getBytes());
                   os.flush();
                   os.close();
               }

            }catch(Exception e){
                System.out.println("连接异常断开");
            }finally{
                try{
                    socket.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        //解析资源文件路径
        private String read(InputStream input,OutputStream output){
            BufferedReader br= new BufferedReader(new InputStreamReader(input));
            try{
                String readLine=br.readLine();
                String[] split= readLine.split(" ");
                if(split.length!=3){
                    return null;
                }
                System.out.print(readLine);
                return split[1];
            }catch(IOException e){
                e.printStackTrace();
            }
            return null;
        }
        private StringBuffer response(String filePath){

            File file = new File(ROOT+filePath);
            System.out.println(file.getPath());
            //这个地方的条件判断可以类比那个py。
            if(file.exists()){
                //资源存在，读资源，资源是文件吗
                try{
                    BufferedReader br=new BufferedReader(new FileReader(file));
                    StringBuffer sb= new StringBuffer();
                    String line=null;
                    while((line=br.readLine())!=null){
                        sb.append(line).append("\r\n");
                    }
                    StringBuffer result = new StringBuffer();
                    result.append("HTTP /1.1 200 ok /r/n");
                    result.append("Content-Type:text/html /r/n");
                    result.append("Content-Length:").append(file.length()).append("/r/n");
                    result.append("\r\n:").append(sb.toString());
                    return result;

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            else{
                //资源不存在file not found.
                StringBuffer error=new StringBuffer();
                error.append("HTTP /1.1 400 file not found /r/n");
                error.append("Content-Type:text/html \r\n");
                error.append("Content-Length:20 \r\n").append("\r\n");
                error.append("<h1 >404 Not Found..</h1>");
                return error;
            }
            return null;


        }
    }



}
