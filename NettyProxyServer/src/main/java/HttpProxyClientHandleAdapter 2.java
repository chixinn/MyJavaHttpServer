import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;

public class HttpProxyClientHandleAdapter extends ChannelInboundHandlerAdapter {
    private Channel clientChannel;
    public HttpProxyClientHandleAdapter(Channel clientChannel){
        this.clientChannel=clientChannel;
    }
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext,Object msg)throws Exception{
        FullHttpResponse fullHttpResponse=(FullHttpResponse)msg;
        //返回client端
        fullHttpResponse.headers().add("MESSAGE","Through proxy");
        clientChannel.writeAndFlush(msg);
    }
}
