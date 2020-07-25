package Tes1;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {
    public static void main(String[]s){
        ServerSocket ss=null;
        try{
            ss=new ServerSocket(8081);
        }catch(IOException e){
            e.printStackTrace();
        }
        while(true){
            try{
                Socket socket=ss.accept();
                socket.setSoTimeout(1000*60);//设置代理服务器与客户端的连接未活动超过时间
                String line="";
                InputStream is=socket.getInputStream();
                String tmpHost="",host;
                int port=80;
                int flag=1;
                String type=null;
                OutputStream os=socket.getOutputStream();
                BufferedReader br=new BufferedReader(new InputStreamReader(is));
                StringBuilder sb=new StringBuilder();
                while((line=br.readLine())!=null){
                    System.out.println(line+"----------------");
                    if(flag==1){
                        type=line.split(" ")[0];
                        if(type==null)continue;
                    }//获取请求行中的请求方法
                    flag++;
                    String[]s1=line.split(": ");
                    if(line.isEmpty()){
                        break;
                    }
                    for(int i=0;i<s1.length;i++){
                        if(s1[i].equalsIgnoreCase("host")){
                            tmpHost=s1[i+1];
                        }
                    }
                    sb.append(line+"\r\n");
                    line=null;
                }
                sb.append("\r\n");
                if(tmpHost.split(":").length>1){
                    port=Integer.valueOf(tmpHost.split(":")[1]);
                }
                host=tmpHost.split(":")[0];
                Socket proxySocket=null;
                if(host!=null&&!host.equals("")){
                    proxySocket=new Socket(host,port);
                    proxySocket.setSoTimeout(1000*60);//设置代理服务器与服务器端
                    OutputStream proxyOs=proxySocket.getOutputStream();
                    InputStream proxyIs=proxySocket.getInputStream();
                    //http请求直接转发
                    proxyOs.write(sb.toString().getBytes("utf-8"));
                    proxyOs.flush();
                    new ProxyHandleThread(is,proxyOs,host).start();
                    new ProxyHandleThread(proxyIs,os,host).start();
                }

            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
