import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

public class ForwardInitializer extends ChannelInitializer {
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
            public void channelRead(ChannelHandlerContext channelHandlerContext,Object msg)throws  Exception{
                channelHandlerContext.channel().writeAndFlush(msg);
            }
        });
    }
}
