import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
/*往本地文件中写数据*/

public class contextLoads {
    public static void contextloads() throws Exception{
        String str="hello to nio";
        //创建输出流
        FileOutputStream fos=new FileOutputStream("basic.txt");
        //从流中得到一个通道
        FileChannel fc=fos.getChannel();
        //提供一个缓冲区
        ByteBuffer allocate =ByteBuffer.allocate(1024);
        //往缓冲区存入数据
        allocate.put(str.getBytes());
        //当数据写入到缓冲区时，指针指向数据的最后一行，那么缓冲区写入通道中输出时，从最后一行数据开始写入
        //写入1024的剩余没有数据的空缓冲区，需要翻转缓冲区，重置位置到初始位置
        allocate.flip();
        //把缓冲区写入到通道中，通道负责把数据写入到文件中
        fc.write(allocate);
        //关闭输出路
        fos.close();
    }
    public static void main(String []args) throws Exception {
        contextloads();


    }
}
