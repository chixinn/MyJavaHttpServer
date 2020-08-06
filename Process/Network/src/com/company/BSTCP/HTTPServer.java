package com.company.BSTCP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//ThreadPool /*2020.7.26*/

public class HTTPServer {
    //处理handleRequest的线程池
    public static String getResourceName(String s) {
        //"GET /index.html HTTP/1.1"
        String[] split = s.split(" ");
        if (split.length != 3) {
            System.out.println("HTTP方法不符合格式!出现错误！关闭服务器！");
            System.exit(1);
        }
        System.out.println(s);
        return split[1];
    }

    public static void handleRequest(Socket socket) {
        try {
            //使用socket对象中的getInputStream。
            InputStream is = socket.getInputStream();

            //输入流对象is转换为字符缓冲输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            /*BufferedReader test */
            System.out.println(line);
            /*parse URL*/

            String fileName = getResourceName(line);
            //如果出现shutdown的情况
            if (fileName.equals("/shutdown")) {
                System.exit(0);
            }
            //如果出现索引省略的情况下：
            if (fileName.equals("/")) {
                fileName = "/index.html";//默认也转入index.html首页
            }
            String projectParentPath = new File(new File("").getCanonicalPath()).getParent();
            // Test:System.out.println(projectParent);//"projectParent=/Users/chixinning/Desktop/webServer"
            String ForePath = "/Network/web";
            //  Test:System.out.println(projectParent+ForePath+arr[1]);

            /*FilePathTest*/
            /*String ROOT="projectParent=/Users/chixinning/Desktop/webServer"+"/Network/web";
            File file=new File(ROOT+arr[1]);*/

            File file = new File(projectParentPath + ForePath + fileName);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(projectParentPath + ForePath + fileName);
                OutputStream os = socket.getOutputStream();
                os.write("HTTP/1.1 200 OK\r\n".getBytes());
                os.write("Content-Type:text/html\r\n".getBytes());
                os.write("\r\n".getBytes()); //必须写入空行，否则浏览器不解析
                int len = 0;
                byte[] bytes = new byte[1024];
                while ((len = fis.read(bytes)) != -1) {
                    os.write(bytes, 0, len);
                }
                fis.close();
                socket.close();
            } else {
                /*既可以写成一段话，也可以error.html类比index.html进行输出，*/
                String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
                        "Content-Type:text/html\r\n" +
                        "\r\n" +
                        "<h1>File Not Found</h1>";
                OutputStream os = socket.getOutputStream();
                os.write(errorMessage.getBytes());

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(850);//线程池
        ServerSocket ss = new ServerSocket(8082);
        System.out.println("服务器已经监听:");
        System.out.println(ss.getInetAddress() + ":" + ss.getLocalPort());

        //祝线程死循环等待新连接到来
        /*while (true) {
            Socket socket = ss.accept();

            new Thread(() -> handleRequest(socket)).start();
        }*/
        /*Thread Pool 线程池*/
      while(!Thread.currentThread().isInterrupted()){
           System.out.println("Server阻塞等待中");
           Socket socket=ss.accept();//创建一个连接套接字
           executor.submit(new Thread(()->handleRequest(socket)));//为新的连接创建新的线程
       }

    }
}
