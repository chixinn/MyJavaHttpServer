package Server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    /*读取事件Event*/
    public void channelRead(ChannelHandlerContext ctx,Object msg) throws Exception{
        System.out.println("Server: "+ctx);
        ByteBuf byteBuf=(ByteBuf)msg;
        System.out.println("客户端发来的消息："+byteBuf.toString(CharsetUtil.UTF_8));

    }
    /*数据读取完毕事件*/
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception{
        ctx.close();
    }
    public void channelReadComplete(ChannelHandlerContext ctx)throws Exception{
        ctx.writeAndFlush(Unpooled.copiedBuffer("就是没钱",CharsetUtil.UTF_8));
    }
}
