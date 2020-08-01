package staticServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpServer {
    private final static int port=9092;
    public static void main(String[]args) throws InterruptedException{
        //BossEventLoop负责接收客户端的连接
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        //将Socket交给WorkerEventLoopGroup进行IO处理
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        try{
            //ServerBootstrap是服务器端启动助手
            ServerBootstrap serverBootstrap=new ServerBootstrap();

            serverBootstrap.group(bossGroup,workerGroup)
            //使用NioServerSocketChannel作为服务器短的通道
                    .channel(NioServerSocketChannel.class)
            //128是最大线程数
                    .option(ChannelOption.SO_BACKLOG,128)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());
            //通道处理器添加完毕后启动服务器
            ChannelFuture channelFuture=serverBootstrap.bind(port).sync();//异步
            channelFuture.channel().closeFuture().sync();//异步

        }finally {
            ///资源优雅释放
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
