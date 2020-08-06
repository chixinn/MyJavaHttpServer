import java.io.*;
import java.net.*;

public class MyHttpProxy extends Thread{
    static public int CONNECT_RETRIES=5;
    static public int CONNECT_PAUSE=5;
    protected Socket csocket;

    //
    public MyHttpProxy(Socket cs){
        csocket=cs;
        start();
    }
    public void run(){
        String buffer="";//读取请求头
        String URL="";//读取请求URL
        String host="";//读取目标主机host
        int port=80;//默认端口80
        Socket ssocket=null;
    }

}
