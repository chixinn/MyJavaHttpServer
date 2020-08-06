import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpRequest;
import java.util.Date;

public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private RedisUtil redisUtil = new RedisUtil();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            String uri = request.uri();
            if ("/favicon.ico".equals(uri)) {
                return;
            }
            log.info(new Date().toString());
            Jedis jedis = redisUtil.getJedis();
            String s = jedis.get(uri);
            if (s == null || s.length() == 0) {
                //这里我们的处理是回源到tomcat服务器进行抓取，然后
                //将抓取的内容放回到redis里面
                try {
                    URL url = new URL("http://119.29.188.224:8080" + uri);
                    log.info(url.toString());
                    URLConnection urlConnection = url.openConnection();
                    HttpURLConnection connection = (HttpURLConnection) urlConnection;
                    connection.setRequestMethod("GET");
                    //连接
                    connection.connect();
                    //得到响应码
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader
                                (connection.getInputStream(), StandardCharsets.UTF_8));
                        StringBuilder bs = new StringBuilder();
                        String l;
                        while ((l = bufferedReader.readLine()) != null) {
                            bs.append(l).append("\n");
                        }
                        s = bs.toString();
                    }
                    jedis.set(uri, s);
                    connection.disconnect();
                } catch (Exception e) {
                    log.error("", e);
                    return;
                }
            }
            jedis.close();
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HTTP_1_1, OK, Unpooled.wrappedBuffer(s != null ? s
                    .getBytes() : new byte[0]));
            response.headers().set(CONTENT_TYPE, "text/html");
            response.headers().set(CONTENT_LENGTH,
                    response.content().readableBytes());
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
            ctx.flush();
        } else {
            //这里必须加抛出异常，要不然ab测试的时候一直卡住不动，暂未解决
            throw new Exception();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        ctx.close();
    }
}
