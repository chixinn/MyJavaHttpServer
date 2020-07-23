package webServer;

import java.io.*;
import java.net.Socket;

public class Communicate extends Thread{
    private Socket socket;
    public static final String ROOT="/Users/chixinning/Desktop/webServer/MyJavaHttpServer/src/webServer/";
    Communicate(Socket socket){
        this.socket=socket;
    }
    public String getResourcePath(String s){
        //"GET /index.html HTTP/1.1"
        String[] split=s.split(" ");
        if(split.length!=3){
            return "HTTP方法不符合格式";
        }
        System.out.println(s);
        return split[1];
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
        }catch(Exception e){
            System.out.print("文件发送异常"+e.getMessage());
            System.exit(1);
        }
    }

    public void run(){
        super.run();
        System.out.println("处理客户端请求："+socket.getInetAddress()+":"+socket.getPort());
        try{
            //得到socket套接字承载的输入和输出
            //创建输出流对象
            PrintStream out=new PrintStream(socket.getOutputStream());
            //创建输入流对象
            DataInputStream in =new DataInputStream(socket.getInputStream());
            String msg=in.readLine();
            //获取文件路径
            String fileName=getResourcePath(msg);
            //如果是shutdown就退出
            if (fileName.equals("/shutdown")){
                socket.close();
                System.exit(0);
            }
            else{
                System.out.print("用户请求的文件名是"+fileName);
                String filePath=ROOT+fileName;
                File file = new File(filePath);
                System.out.print("用户请求的文件路径是"+file.getPath());
                if(file.exists()){
                    //如果是有效路径
                    System.out.println(fileName+" start send");

                    out.println("HTTP/1.0 200 OK");
                    out.println("MIME_version:1.0");
                    out.println("Content_Type:text/html");
                    int len = (int) file.length();
                    out.println("Content_Length:"+len);
                    out.println("");//报文头和信息之间要空一行
                    //发送文件Page
                    sendFile(out,file);
                }
                else
                {
                    System.out.println("\nResources not avaliable");

                    out.println("HTTP/1.0 400 file not found");
                    out.println("MIME_version:1.0");
                    out.println("Content_Type:text/html");
                    int len = (int) file.length();
                    out.println("Content_Length:"+len);
                    out.println("");//报文头和信息之间要空一行
                    //发送文件ErrorPage
                    String ErrorfilePage=ROOT+"error.html";
                    File errorfile=new File(ErrorfilePage);
                    sendFile(out,errorfile);
                    out.flush();

                }
                socket.close();

            }



        }catch(Exception e){
            System.out.println("连接异常");
        }finally {
            try{
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }


    }
}
