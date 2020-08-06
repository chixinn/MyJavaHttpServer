import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/* ProxyServer把请求的剩余部分发送到输出Socket。*/
public class ProxyServer {
    public static void proxyHandler(InputStream input, OutputStream output,String host) {
        try{
            while(true){
                BufferedInputStream bis=new BufferedInputStream(input);
                byte[]buffer=new byte[1024];
                int lenght=-1;
                while((lenght=bis.read(buffer))!=-1){
                    output.write(buffer,0,lenght);
                    lenght=-1;
                }
                output.flush();
                /*try{
                    Thread.sleep(10);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }*/
            }
        }catch (SocketTimeoutException e){
            try{
                input.close();
                output.close();
            }catch(IOException e2){
                e2.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                input.close();
                output.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        }
        public static void handleRequest(Socket socket) {
                try {
                    socket.setSoTimeout(1000*60);//设置代理服务器与客户端的连接未活动超时时间
                    String line = "";
                    InputStream clinetInput = socket.getInputStream();
                    String tempHost="",host;
                    int port =80;
                    String type=null;
                    OutputStream os = socket.getOutputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(clinetInput));
                    /*3.读取浏览器请求的第一行，该行内容包含了请求的目标URL*/
                    /*4.分析请求的第一行，得到目标服务器的名字和端口*/
                    int flag=1;
                    StringBuilder sb =new StringBuilder();
                    //读取HTTP请求头，拿到HOST请求头和method.
                    while((line = br.readLine())!=null) {
                        if(flag==1) {       //获取请求行中请求方法，默认是http
                            type = line.split(" ")[0];
                            if(type==null)continue;
                        }
                        flag++;
                        String[] s1 = line.split(": ");
                        if(line.isEmpty()) {
                            break;
                        }
                        for(int i=0;i<s1.length;i++) {
                            if(s1[i].equalsIgnoreCase("host")) {
                                tempHost=s1[i+1];
                            }
                        }
                        sb.append(line).append("\r\n");
                        line=null;
                    }
                    sb.append("\r\n");          //不加上这行http请求则无法进行。这其实就是告诉服务端一个请求结束了

                    if(tempHost.split(":").length>1) {
                        port = Integer.parseInt(tempHost.split(":")[1]);
                    }
                    host = tempHost.split(":")[0];

                    Socket proxySocket = null;//代理间通信的socket

                    if(host!=null&&!host.equals("")) {
                        //连接到目标服务器
                        //5.打开一个通向目标服务器的Socket.
                        proxySocket = new Socket(host,port);
                        proxySocket.setSoTimeout(1000*60);//设置代理服务器与服务器端的连接未活动超时时间
                        OutputStream proxyOs = proxySocket.getOutputStream();//输出
                        InputStream proxyIs = proxySocket.getInputStream();//输入
                        /*https不可直接转发*/
                        assert type != null;
                        if(type.equalsIgnoreCase("connect")) {     //https请求的话，告诉客户端连接已经建立（下面代码建立）
                            os.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                            os.flush();
                        }else {//http请求则直接转发
                            proxyOs.write(sb.toString().getBytes("utf-8"));
                            proxyOs.flush();
                        }
                        //新开线程转发客户端请求至目标服务器
                        ExecutorService Proxyexecutor=Executors.newFixedThreadPool(10);
                        Proxyexecutor.submit(new Thread(()->proxyHandler(clinetInput,proxyOs,host)));
                        //转发目标服务器响应至客户端
                        Proxyexecutor.submit(new Thread(()->proxyHandler(proxyIs,os,host)));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        public static void main(String[] args) throws IOException {
        /*1.等待来自客户的请求
        * 2.启动新线程以处理客户连接请求*/
            ExecutorService Socketexecutor = Executors.newFixedThreadPool(100);//线程池
            ServerSocket ss = new ServerSocket(11111);//监听代理代理服务器端口
            while(!Thread.currentThread().isInterrupted()){
                Socket socket=ss.accept();
                Socketexecutor.submit(new Thread(()->handleRequest(socket)));//socket线程池
            }
        }
}





