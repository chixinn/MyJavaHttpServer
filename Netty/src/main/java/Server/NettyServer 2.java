package Server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    public static void main(String[] args) throws InterruptedException {
        //1.创建一个线程组，接收客户端连接
        //2.BossEventLoop负责接收客户端的连接并将Socket交给WorkerEventLoopGroup连进行IO处理。
        EventLoopGroup bossGroup=new NioEventLoopGroup();//构造方法
        //3.创建一个线程组，处理网络操作
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        //4.创建服务器端启动助手来配置参数
        ServerBootstrap serverBootstrap=new ServerBootstrap();
        //5.设置两个线程组
        serverBootstrap.group(bossGroup,workerGroup)
                //6.使用NioServerSocketChannel作为服务器端通道的实现
                .channel(NioServerSocketChannel.class)
                //7.设置线程队列中等待连接的个数
                .option(ChannelOption.SO_BACKLOG,128)
                //8.保持活跃连接状态
                .childOption(ChannelOption.SO_KEEPALIVE,true)
                //9.创建一个通道初始化对象
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    //10.往pipeline中添加自定义的handler类
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception{
                        channel.pipeline().addLast(new NettyServerHandler());
                    }
                });
        System.out.println("....服务器非阻塞等待中...");
        //11.绑定端口，bind方法是异步的sync同步阻塞
        ChannelFuture sync=serverBootstrap.bind(9999).sync();
        System.out.println("...Server start");
        //12.关闭通道，关闭线程组
        sync.channel().closeFuture().sync();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
