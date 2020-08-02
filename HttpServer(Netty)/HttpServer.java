package HttpServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpServer {
    private int port;
    public HttpServer(int port){
        this.port=port;
    }
    //创建线程接收客户端连接
    public void run()throws Exception{
        ////2.BossEventLoop负责接收客户端的连接并将Socket交给WorkerEventLoopGroup连进行IO处理。
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
            //创建服务器端启动助手来配置参数
            ServerBootstrap serverBootstrap=new ServerBootstrap();
            //设置两个线程组
            serverBootstrap.group(bossGroup,workerGroup)
                    //使用NioServerSocketChannel作为服务器端通道的实现
                    .channel(NioServerSocketChannel.class)
                    //创建一个通道初始化对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)throws Exception{

                            ch.pipeline().addLast("http-decoder",new HttpRequestDecoder());
                            ch.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65535));
                            ch.pipeline().addLast("http-encoder",new HttpResponseEncoder());
                            ch.pipeline().addLast("http-chunked",new ChunkedWriteHandler() );
                            ch.pipeline().addLast("http-server",new HttpServerHandler() );
                        }
                    })
                    //设置线程队列中等待连接的个数
                    .option(ChannelOption.SO_BACKLOG,128)
                    //创建一个通道初始化对象
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            ChannelFuture f=serverBootstrap.bind(port).sync();
            f.channel().closeFuture().sync();


        }finally {
            //资源优雅释放
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    public static void main(String[]args){
        int port=8089;
        try{
            new HttpServer(port).run();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

