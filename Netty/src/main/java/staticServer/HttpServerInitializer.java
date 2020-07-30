package staticServer;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline channelPipeline=socketChannel.pipeline();
        //将请求和应答消息编码或解码为HTTP消息
        channelPipeline.addLast(new HttpServerCodec());
        //将多个HTTP消息
        channelPipeline.addLast(new HttpObjectAggregator(65536));//64*1024
        channelPipeline.addLast(new ChunkedWriteHandler());
        channelPipeline.addLast(new HttpServerHandleAdapter());
    }
}
