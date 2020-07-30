package Proxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.proxy.HttpProxyHandler;

public class HttpProxyServer {
    int port;
    public int HttpProxyServer(int port){
        this.port=port;
    }
    EventLoopGroup bossGroup=new NioEventLoopGroup();
    EventLoopGroup workerGroup=new NioEventLoopGroup();
    try{
        ServerBootstrap serverBootstrap=new ServerBootstrap();
        serverBootstrap.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,128)
                .option(ChannelOption.TCP_NODELAY,true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<Channel>() {
                    protected void initChannel(Channel ch) throws Exception{
                        ch.pipeline().addLast("httpCodec",new HttpServerCodec());
                        ch.pipeline().addLast("httpObject",new HttpObjectAggregator(65536));
                        ch.pipeline().addLast("serverHandler",new HttpProxyServerHandler());//
                    }
                });
        ChannelFuture f=b.bind(port).sync();

    }
}
