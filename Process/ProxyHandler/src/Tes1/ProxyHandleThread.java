package Tes1;

import java.io.*;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class ProxyHandleThread extends Thread {
    private InputStream input;
    private OutputStream output;
    private String host;

    public ProxyHandleThread(InputStream input, OutputStream output, String host) {
        this.input = input;
        this.output = output;
        this.host = host;
    }
    public void run(){
        try{
            while(true){
                BufferedInputStream bis=new BufferedInputStream(input);
                byte[]buffer=new byte[1024];
                int lenght=-1;
                while((lenght=bis.read(buffer))!=-1){
                    output.write(buffer,0,lenght);
                    lenght=-1;
                    System.out.println("通过代理");
                }
                output.flush();
                try{
                    Thread.sleep(10);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }catch (SocketTimeoutException e){
            try{
                input.close();
                output.close();
            }catch(IOException e2){
                e2.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try{
                input.close();
                output.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
