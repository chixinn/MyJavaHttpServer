import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;


public class HttpProxyServerHandleAdapter extends ChannelInboundHandlerAdapter {
    private  ChannelFuture channelfuture;
    private int port;
    private String host;

    public void channelRead(final ChannelHandlerContext channelHandlerContext, final Object msg)throws Exception{
        //如果报文是http报文，则
        if(msg instanceof FullHttpRequest){
            FullHttpRequest fullHttpRequest=(FullHttpRequest)msg;
            String  targetHost=fullHttpRequest.headers().get("host");//
            String[] temp=targetHost.split(":");
            int targetPort=80;
            if(temp.length>1){
                targetPort=Integer.parseInt(temp[1]);
            }
            else{
                if(fullHttpRequest.uri().indexOf("https")==0){//indexOf是什么意思
                    targetPort=443;//连接目标端口443
                }
            }
            this.host=temp[0];
            this.port=targetPort;
            //如果是https协议
            if("CONNECT".equalsIgnoreCase(fullHttpRequest.method().name())){
                HttpResponse httpResponse=new DefaultHttpResponse(fullHttpRequest.getProtocolVersion(), HttpResponseStatus.OK);
                channelHandlerContext.writeAndFlush(httpResponse);
                channelHandlerContext.pipeline().remove("httpCodec");
                channelHandlerContext.pipeline().remove("httpObject");
                return;
            }
            //连接至目标服务器
            Bootstrap bootstrap=new Bootstrap();
            bootstrap.group(channelHandlerContext.channel().eventLoop())//注册线程池
                    .channel(channelHandlerContext.channel().getClass())//NioSocektChannel
                    .handler(new HttpProxyServerInitializer(channelHandlerContext.channel()));//todo
            ChannelFuture channelFuture=bootstrap.connect(temp[0],port);
            channelFuture.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()) {
                        channelFuture.channel().writeAndFlush(msg);//提示tobe final
                    }
                    else{
                        channelFuture.channel().close();
                    }
                }
            });
        }
        else{
            //https报文直接转发
            if(channelfuture==null){
                Bootstrap bootstrap= new Bootstrap();
                bootstrap.group(channelHandlerContext.channel().eventLoop())
                        .channel(channelHandlerContext.channel().getClass())
                        .handler(new ForwardInitializer() {
                        });
                channelfuture=bootstrap.connect(host,port);
                channelfuture.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if(channelFuture.isSuccess()){
                            channelFuture.channel().writeAndFlush(msg);
                        }
                        else{
                            channelHandlerContext.channel().close();
                        }
                    }
                });
            }
            else{
                channelfuture.channel().writeAndFlush(msg);
            }
        }
    }
}
