import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class HttpProxyClient {
    public static final int port=4312;
    public static void main(String[]args)throws InterruptedException{
        EventLoopGroup group= new NioEventLoopGroup();
        Bootstrap bootstrap=new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer() {
                    protected void initChannel(Channel channel) throws Exception {

                    }
                });
        ChannelFuture channelFuture=bootstrap.bind(port).sync();
        channelFuture.channel().closeFuture().sync();

    }
}
