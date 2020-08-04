import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

public class HttpProxyServerInitializer extends ChannelInitializer {
        private Channel clientChannel;
        public HttpProxyServerInitializer (Channel clientChannel){
                this.clientChannel=clientChannel;
        }
        protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new HttpClientCodec());
                channel.pipeline().addLast(new HttpObjectAggregator(65536));
                channel.pipeline().addLast(new HttpProxyClientHandleAdapter(clientChannel));//

        }
}