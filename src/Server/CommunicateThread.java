package Server;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.ServerSocket;

public class CommunicateThread extends Thread {
    //与客户端通信的套接字
    Socket client;
    public CommunicateThread(Socket s){
        client =s;
    }
    //获取浏览器请求资源的路径
    public String getResourcePath(String s){
        String s1=s.substring(s.indexOf(' ')+1);
        s1=s1.substring(1,s1.indexOf(' '));
        if(s1.equals(" ")){
            s1="index.html";
        }
        return s1;
    }
    public void sendFile(PrintStream out,File file){
        try{
            DataInputStream in =new DataInputStream(new FileInputStream(file));
            int len =(int)file.length();
            byte buf[]=new byte[len];
            in.readFully(buf);
            out.write(buf,0,len);
            out.flush();
            in.close();

        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
    public void run(){
        try{
            String clientIP="127.0.0.1";
            int clientPort=client.getPort();
            PrintStream out=new PrintStream(client.getOutputStream());
            DataInputStream in =new DataInputStream(client.getInputStream());
            String msg=in.readLine();
            String fileName=getResourcePath(msg);
            System.out.println("The user ");
            File file=new File(fileName);
            if(file.exists()){
                System.out.println(fileName+" start send");

                out.println("HTTP/1.0 200 OK");
                out.println("MIME_version:1.0");
                out.println("Content_Type:text/html");
                int len = (int) file.length();
                out.println("Content_Length:"+len);
                out.println("");//报文头和信息之间要空一行

                //发送文件
                sendFile(out,file);

                out.flush();
            }
            client.close();


        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
