package com.company.BSTCP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HTTPServer {

    public static void handleRequest(Socket socket){

        try{
            //使用socket对象中的getInputStream。
            InputStream is=socket.getInputStream();

            //输入流对象is转换为字符缓冲输入流
            BufferedReader br=new BufferedReader(new InputStreamReader(is));
            String line=br.readLine();
            System.out.println(line);
            String[] arr=line.split(" ");
            //如果出现shutdown的情况
            if(arr[1].equals("/shutdown")){
                System.exit(0);
            }
            String projectParent = new File(new File("").getCanonicalPath()).getParent();
           // System.out.println(projectParent);//"projectParent=/Users/chixinning/Desktop/webServer"
            String ForePath="/Network/web";
           // System.out.println(projectParent+ForePath+arr[1]);
            File file=new File(projectParent+ForePath+arr[1]);
            if(file.exists()){
                FileInputStream fis=new FileInputStream(projectParent+ForePath+arr[1]);
                OutputStream os=socket.getOutputStream();
                os.write("HTTP/1.1 200 OK\r\n".getBytes());
                os.write("Content-Type:text/html\r\n".getBytes());
                os.write("\r\n".getBytes()); //必须写入空行，否则浏览器不解析
                int len=0;
                byte[] bytes=new byte[1024];
                while((len=fis.read(bytes))!=-1){
                    os.write(bytes,0,len);
                }
                fis.close();
                socket.close();
            }
            else{
                String errorMessage="HTTP/1.1 404 File Not Found\r\n"+
                        "Content-Type:text/html\r\n"+
                        "Content-Length:23\r\n"+
                        "\r\n"+
                        "<h1>File Not Found</h1>";
                OutputStream os=socket.getOutputStream();
                os.write(errorMessage.getBytes());

            }



        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String [] args)throws IOException{
        ServerSocket ss=new ServerSocket(8082);
        while(true){
            Socket socket=ss.accept();
            new Thread(()->handleRequest(socket)).start();
        }
    }

}
