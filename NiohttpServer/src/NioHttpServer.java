import javax.swing.plaf.basic.BasicTreeUI;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioHttpServer {
    private static final ByteBuffer READ_BUFFER = ByteBuffer.allocate(1024 * 4);
    //静态资源路径
    String projectParent = "/Users/chixinning/Desktop/webServer/NiohttpServer";
    //相应的基础信息
    public static final String BASIC_RESPONSE="HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html;charset=utf-8\r\n" +
            "Vary: Accept-Encoding\r\n";
    //换行
    private static final String carriageReturn="\r\n";

    public void start(){
        try{
            //创建客户端通道
            ServerSocketChannel ssc=ServerSocketChannel.open();
            //将通道设置为非阻塞
            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress("localhost", 8082));
            //多路复用器
            Selector selector=Selector.open();
            //将客户端通道注册到多路复用器，监听可读事件
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while(true){
                //阻塞等待通道上的事件触发
                int eventCountTriggerd=selector.select();
                if(eventCountTriggerd==0){
                    continue;
                }
                //获取所有触发的事件
                Set<SelectionKey> selectionKeys=selector.selectedKeys();
                //遍历事件进行处理
                for(SelectionKey selectionKey:selectionKeys){
                    handleSelectKey(selectionKey,selector);
                }
                //清楚事件记录
                selectionKeys.clear();

            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void handleSelectKey(SelectionKey selectionKey,Selector selector){

    }

}
