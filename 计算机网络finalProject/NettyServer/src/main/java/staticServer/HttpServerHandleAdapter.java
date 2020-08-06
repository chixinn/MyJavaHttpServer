package staticServer;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;


import java.io.File;
import java.io.RandomAccessFile;

/*这个主要是一个文件服务器*/

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
        if(uri.equalsIgnoreCase("/")){
            uri="/index.html";//null 
        }
        String fileName=ROOT+uri;//文件地址
        if(uri.equalsIgnoreCase("/shutdown")){
            System.out.println("系统关闭");
            System.exit(0);
        }
        //根据地址构建文件
        File file=new File(fileName);

        //创建http响应
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
        RandomAccessFile randomAccessFile=new RandomAccessFile(file,"r");
        /**在异步框架中高效地写大块地数据：即将文件内容写出到网络
         * NIO零拷贝特性，消除了将文件的内容从文件系统移动到网络栈的复制过程。
         * 从FileInputStream创建一个DefaultFileRegion，并将其写入Channel，利用零拷贝特性来传输一个文件内容。
         */
        //设置http头部信息
        httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, randomAccessFile.length());
        httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);


        channelHandlerContext.write(httpResponse);//写回http报文
        channelHandlerContext.write(new DefaultFileRegion(randomAccessFile.getChannel(), 0, file.length()));//写回文件

        //写入文件尾部，必须有。
        channelHandlerContext.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        randomAccessFile.close();//关闭文件
    }
}
