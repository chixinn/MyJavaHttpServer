package staticServer;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.RandomAccessFile;

public class HttpServerHandleAdapter extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final String ROOT;
    private static final File ERROR;
    static {
        ROOT="/Users/chixinning/Desktop/webServer/Netty/src/main/java/staticServer";//待填
        String errorPagePath=ROOT+"/error.html";
        ERROR=new File(errorPagePath);

    }


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        //获取URI
        String uri=fullHttpRequest.getUri();
        String fileName=ROOT+uri;//文件地址
        if(uri.equalsIgnoreCase("/shutdown")){
            System.out.println("系统关闭");
            System.exit(0);
        }
        //根据地址构建文件
        File file=new File(fileName);


        HttpResponse httpResponse=new DefaultHttpResponse(fullHttpRequest.getProtocolVersion(), HttpResponseStatus.OK);

        //设置文件格式内容
        if(fileName.endsWith(".html")){
            httpResponse.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
        }
        if(file.exists()){
           httpResponse.setStatus(HttpResponseStatus.OK);
        }
        else{
            //如果文件不存在
            file=ERROR;
            httpResponse.setStatus(HttpResponseStatus.NOT_FOUND);
        }
        boolean keepAlive=HttpHeaders.isKeepAlive(fullHttpRequest);//判断链接是否是alive状态
        RandomAccessFile randomAccessFile=new RandomAccessFile(file,"r");
        if(keepAlive){
            httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, randomAccessFile.length());
            httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }
        channelHandlerContext.write(httpResponse);//写回

        if(channelHandlerContext.pipeline().get(SslHandler.class)==null){
            channelHandlerContext.write(new DefaultFileRegion(randomAccessFile.getChannel(), 0, file.length()));
        }
        else{
            channelHandlerContext.write(new ChunkedNioFile(randomAccessFile.getChannel()));
        }
        //写入文件尾部
        ChannelFuture channelFuture=channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if(!keepAlive){
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
        randomAccessFile.close();

    }
}
