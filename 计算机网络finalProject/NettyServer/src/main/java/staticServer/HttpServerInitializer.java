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
        channelPipeline.addLast(new HttpServerCodec());//HttpServerCodec继承了ChannelHandlerAppender，并且创建了一个HttpRequestDecode和一个HttpResponseEncoder进行http消息解码。
        //HttpObjectAggregator将多个HTTP消息进行组装
        /*它负责把多个HttpMessage组装成一个完整的Http请求或者响应。
        到底是组装成请求还是响应，则取决于它所处理的内容是请求的内容，还是响应的内容。
        这其实可以通过Inbound和Outbound来判断，对于Server端而言，在Inbound 端接收请求，在Outbound端返回响应。*/

        channelPipeline.addLast(new HttpObjectAggregator(65536));//64*1024
        channelPipeline.addLast(new ChunkedWriteHandler());//ChunkedWriteHandler进行大规模文件传输。
        channelPipeline.addLast(new HttpServerHandleAdapter());//FileSystem业务工程处理器。
    }
}
