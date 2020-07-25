import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class test {

    @SuppressWarnings("resource")
    public static void main(String[] s) {

        ServerSocket ss = null;
        try {
            ss = new ServerSocket(11111);//创建绑定到特定端口的服务器套接字。

        } catch (IOException e1) {
            e1.printStackTrace();
        }
        while(true) {//代理服务器一直在死循环运行
            try {
                Socket socket = ss.accept();
                socket.setSoTimeout(1000*60);//设置代理服务器与客户端的连接未活动超时时间，可设置长一些
                String line = "";
                String tempHost="",host;//解析用
                int port =80;//默认服务器端口号
                String type=null;//http or https

                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();


                BufferedReader br = new BufferedReader(new InputStreamReader(is));
				System.out.println("========+++++++++++++=======");
                int flag=1;
                StringBuilder sb =new StringBuilder();
                while((line = br.readLine())!=null) {
                    System.out.println(line+"-----------------");
                    if(flag==1) {       //获取请求行中请求方法，下面会需要这个来判断是http还是https
						System.out.println("+++++++++"+line);
                        type = line.split(" ")[0];
                        if(type==null)continue;
                    }
                    flag++;
                    String[] s1 = line.split(": ");
                    int debugTmp=0;
                    for (String i : s1){

                        System.out.println(debugTmp+i);//s1又是个啥
                        debugTmp++;
                        System.out.println("****************************************");
                    }

                    if(line.isEmpty()) {
                        break;
                    }
                    for(int i=0;i<s1.length;i++) {
                        if(s1[i].equalsIgnoreCase("host")) {
                            tempHost=s1[i+1];
                        }
                    }//这里到底在解析什么
                    System.out.println(tempHost);//tempHost到底解析出来是个啥
                    sb.append(line + "\r\n");
                    line=null;
                }
                sb.append("\r\n");          //不加上这行http请求则无法进行。这其实就是告诉服务端一个请求结束了

                if(tempHost.split(":").length>1) {
                    port = Integer.valueOf(tempHost.split(":")[1]);
                }
                //ForwardMessage
                host = tempHost.split(":")[0];
                Socket proxySocket = null;
                if(host!=null&&!host.equals("")) {
                    proxySocket = new Socket(host,port);
                    proxySocket.setSoTimeout(1000*60);//设置代理服务器与服务器端的连接未活动超时时间
                    OutputStream proxyOs = proxySocket.getOutputStream();
                    InputStream proxyIs = proxySocket.getInputStream();
                    if(type.equalsIgnoreCase("connect")) {     //https请求的话，告诉客户端连接已经建立（下面代码建立）
                        os.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                        os.flush();
                    }else {//http请求则直接转发
                        proxyOs.write(sb.toString().getBytes("utf-8"));
                        proxyOs.flush();
                    }
                    new ProxyHandleThread(is, proxyOs,host).start();//监听客户端传来消息并转发给服务器
                    new ProxyHandleThread(proxyIs, os,host).start(); //监听服务器传来消息并转发给客户端
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
class ProxyHandleThread extends Thread {

    private InputStream input;
    private OutputStream output;
    private String host;//debug时需要的东西，实际不需要
    public ProxyHandleThread(InputStream input, OutputStream output,String host) {
        this.input = input;
        this.output = output;
        this.host = host;
    }

    @Override
    public void run() {
        try {
            while (true) {
                BufferedInputStream bis = new BufferedInputStream(input);
                byte[] buffer =new byte[1024];
                int length=-1;
                while((length=bis.read(buffer))!=-1) {//这里最好是字节转发，不要用上面的InputStreamReader，因为https传递的都是密文，那样会乱码，消息传到服务器端也会出错。
                    output.write(buffer, 0, length);
                    length =-1;
					System.out.println("客户端通过代理服务器给服务器发送消息"+input+host);
                }
                output.flush();
                try {
                    Thread.sleep(10);     //避免此线程独占cpu
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
            }
        } catch (SocketTimeoutException e) {
            try {
                input.close();
                output.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }catch (IOException e) {
            System.out.println(e);
        }finally {
            try {
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}