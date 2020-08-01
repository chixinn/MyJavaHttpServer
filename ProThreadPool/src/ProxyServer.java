import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                    System.out.println("客户端通过代理服务器给服务器发送消息"+input+host);
                }
                output.flush();
                try{
                    Thread.sleep(10);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
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
				    System.out.println("========+++++++++++++=======");
                    int flag=1;

                    StringBuilder sb =new StringBuilder();

                    //读取HTTP请求头，拿到HOST请求头和method.
                    while((line = br.readLine())!=null) {

                        System.out.println(line+"*************");//debug用

                        if(flag==1) {       //获取请求行中请求方法，下面会需要这个来判断是http还是https
						System.out.println("+++++++++"+line);
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
                        proxySocket = new Socket(host,port);
                        proxySocket.setSoTimeout(1000*60);//设置代理服务器与服务器端的连接未活动超时时间
                        OutputStream proxyOs = proxySocket.getOutputStream();
                        InputStream proxyIs = proxySocket.getInputStream();
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
                       /* new proxyHandler(is, proxyOs,host).start();//监听客户端传来消息并转发给服务器
                        new proxyHandler(proxyIs, os,host).start(); //监听服务器传来消息并转发给客户端*/
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }






        }
        public static void main(String[] args) throws IOException {
            ExecutorService Socketexecutor = Executors.newFixedThreadPool(100);//线程池
            ServerSocket ss = new ServerSocket(11111);//监听代理代理服务器端口
            while(!Thread.currentThread().isInterrupted()){
                Socket socket=ss.accept();
                Socketexecutor.submit(new Thread(()->handleRequest(socket)));//socket线程池
            }
        }
}





