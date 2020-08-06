package  Server;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.net.Socket;


//每有一个连接建立时，服务器分出一个通信的线程
public class CommunicateThread extends Thread{
    //与客户端通信的套接字
    Socket client;

    public CommunicateThread(Socket s) {
        client = s;
    }

    //获取浏览器请求资源的路径
    public String getResourcePath(String s){
        // 一般的HTTP请求报文的第一行是“GET /index.html HTTP/1.1”
        // 我们要获取的就是中间的"/indext.apsx"

        //获取资源的位置
        String s1 = s.substring(s.indexOf(' ')+1);
        s1 = s1.substring(1,s1.indexOf(' '));

        //默认资源为index.html
        if(s1.equals(""))
            s1 = "Server/index.html";

        return s1;
    }

    public void sendFile(PrintStream out,File file){
        try{
            DataInputStream in  = new DataInputStream(new FileInputStream(file));
            int len = (int)file.length();
            byte buf[] = new byte[len];
            in.readFully(buf);//读取文内容到buf数组中
            out.write(buf,0,len);
            out.flush();
            in.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public void run(){
        try{
            //获取用户的IP地址和端口号
            String clientIP = "127.0.0.1";
            int clientPort = client.getPort();
            //创建输出流对象
            PrintStream out = new PrintStream(client.getOutputStream());
            //创建输入流对象
            DataInputStream in = new DataInputStream(client.getInputStream());
            //读取浏览器提交的请求
            String msg = in.readLine();


            //获取文件路径
            String fileName = getResourcePath(msg);
            System.out.println("The user asked for resource: "+fileName);
            String filePath=("/Users/chixinning/Desktop/webServer/MyJavaHttpServer/src/Server/"+fileName);
            File file = new File(filePath);
            System.out.println(file.getPath());
            if(file.exists()){
                //根据响应报文格式设置
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
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }



}