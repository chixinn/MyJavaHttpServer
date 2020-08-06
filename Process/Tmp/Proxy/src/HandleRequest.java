import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/*处理客户端请求*/
public class HandleRequest extends Thread {
    private static String KHost="Host:";
    private Socket clSocket;
    private Socket seSocket;
    private Scanner cdata;
    private String remotehost;
    private int remoteport;
    private boolean      https      ;
    private List<String> bufflist;

    public HandleRequest(Socket c) {
        this.clSocket=c;
        this.bufflist=new ArrayList<String>();
    }
    public void run(){
        try{
            cdata = new Scanner(clSocket.getInputStream());
            int beginIndex=KHost.length()+1;
            String line;
            //读取客户端的请求头
            while(cdata.hasNextLine() && (line = cdata.nextLine()).length() != 0){
                if(line.length()>5){
                    if(line.substring(0,KHost.length()).equals(KHost)){
                       int hend;
                       if((hend=line.indexOf(':',beginIndex))!=1){
                           remotehost=line.substring(beginIndex,hend);
                           remoteport=Integer.parseInt(line.substring(hend+1));
                       }
                       else{
                           remotehost=line.substring(beginIndex);
                           remoteport=80;
                       }
                    }
                }
                if(line.matches("Proxy-Connection(.*)")){
                    line = "Connection: keep-alive";
                }
                // 判断是否为https请求
                if(line.substring(0, line.indexOf(' ')).equals("CONNECT")){
                    https = true;
                }

                bufflist.add(line);
            }

            if(remotehost!=null){
                System.out.println(remotehost+"->"+remoteport+" "+https);
                seSocket=new Socket(remotehost,remoteport);//连接到远程主机
                if (https) { // 客户端与远程主机间数据转发
                    List<String> list = new ArrayList<>();
                    list.add("HTTP/1.1 200 Connection Established");

                    new ForwardData(list, seSocket, clSocket).start();
                    new ForwardData(null, clSocket, seSocket).start();
                } else {
                    toUri(bufflist);

                    new ForwardData(bufflist, clSocket, seSocket).start();
                    new ForwardData(null, seSocket, clSocket).start();
                }

            }

        }catch (ConnectException c){
            System.err.println("链接超时");
        }catch (SocketException se){
            System.err.println("无法链接"+remotehost+":"+remoteport);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private  void toUri(List<String>buff){
        for(int i=0;i<buff.size();i++){
            String line=buff.get(i);
            String head=line.substring(0,line.indexOf(' '));
            int hlen =head.length()+1;
            if(line.substring(hlen,hlen+7).equals("http://")){
                String uri=line.substring(line.indexOf('/',hlen+7));
                buff.set(i,head+" "+uri);
                break;
            }
        }
    }
}
