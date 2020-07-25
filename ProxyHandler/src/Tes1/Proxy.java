package Tes1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Proxy {
 
	@SuppressWarnings("resource")
	public static void main(String[] s) {
		
			ServerSocket ss = null;
			try {
				ss = new ServerSocket(11111);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			while(true) {
				try {
				Socket socket = ss.accept();
				socket.setSoTimeout(1000*60);//设置代理服务器与客户端的连接未活动超时时间
				String line = "";
				InputStream is = socket.getInputStream();
				String tempHost="",host;
				int port =80;
				String type=null;
				OutputStream os = socket.getOutputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
//				System.out.println("========+++++++++++++=======");
				int temp=1;
				StringBuilder sb =new StringBuilder();
				while((line = br.readLine())!=null) {
					System.out.println(line+"-----------------");
					if(temp==1) {       //获取请求行中请求方法，下面会需要这个来判断是http还是https
//						System.out.println("+++++++++"+line);
						type = line.split(" ")[0];
						if(type==null)continue;
					}
					temp++;
					String[] s1 = line.split(": ");
					if(line.isEmpty()) {
						break;
					}
					for(int i=0;i<s1.length;i++) {
						if(s1[i].equalsIgnoreCase("host")) {
							tempHost=s1[i+1];
						}
					}
					sb.append(line + "\r\n");
					line=null;
				}
					sb.append("\r\n");          //不加上这行http请求则无法进行。这其实就是告诉服务端一个请求结束了
					if(tempHost.split(":").length>1) {
						port = Integer.valueOf(tempHost.split(":")[1]);
					}
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

