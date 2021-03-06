/*********************************************************************************
 * Copyright (c) 2010 Forschungszentrum Juelich GmbH 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the disclaimer at the end. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * (2) Neither the name of Forschungszentrum Juelich GmbH nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/

package udt.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.littleshoot.util.IoUtils;
import org.slf4j.LoggerFactory;

import udt.UDTReceiver;
import udt.UDTServerSocket;
import udt.UDTSocket;
import udt.packets.PacketUtil;

/**
 * helper application for sending a single file via UDT
 * Intended to be compatible with the C++ version in 
 * the UDT reference implementation
 * 
 * main method USAGE: java -cp .. udt.util.SendFile <server_port>
 */
public class TestServerSocket extends Application{

    private final static org.slf4j.Logger log = LoggerFactory.getLogger(TestServerSocket.class);
	private final int serverPort;

	//TODO configure pool size
	private final ExecutorService threadPool=Executors.newFixedThreadPool(3);

	public TestServerSocket(int serverPort){
		this.serverPort=serverPort;
	
	}
	
	@Override
	public void configure(){
		super.configure();
	}

	public void run(){
		configure();
		try{
			UDTReceiver.connectionExpiryDisabled=true;
			InetAddress myHost=localIP!=null?InetAddress.getByName(localIP):InetAddress.getLocalHost();
			
			UDTServerSocket server=new UDTServerSocket(myHost,serverPort);
			while(true){
				Socket socket=server.accept();
				threadPool.execute(new RequestRunner(socket));
			}
		}catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * main() method for invoking as a commandline application
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] fullArgs) throws Exception{
		
		String[] args=parseOptions(fullArgs);
		
		int serverPort=65321;
		try{
			serverPort=Integer.parseInt(args[0]);
		}catch(Exception ex){
			usage();
			System.exit(1);
		}
		TestServerSocket sf=new TestServerSocket(serverPort);
		sf.run();
	}

	public static void usage(){
		System.out.println("Usage: java -cp ... udt.util.SendFile <server_port> " +
				"[--verbose] [--localPort=<port>] [--localIP=<ip>]");
	}

	public static class RequestRunner implements Runnable{
		
		private final static Logger logger=Logger.getLogger(RequestRunner.class.getName());
		
		private final Socket socket;
		
		private final NumberFormat format=NumberFormat.getNumberInstance();
		
		public RequestRunner(final Socket sock){
			this.socket= sock;
			format.setMaximumFractionDigits(3);
		}
		
        
        public void run(){
            try{
                log.info("Handling request from "+socket.getRemoteSocketAddress());
                if (false) {
                    requestAndResponseOnSocket(socket);
                    return;
                }
                final InputStream in = socket.getInputStream();
                final OutputStream out = socket.getOutputStream();
                byte[]readBuf=new byte[32768];
                ByteBuffer bb=ByteBuffer.wrap(readBuf);

                //read file name info 
                while(in.read(readBuf)==0)Thread.sleep(100);

                //how many bytes to read for the file name
                int length=bb.getInt();
                byte[]fileName=new byte[length-1];
                bb.get(fileName);

                //File file=new File("boost.zip");
                File file = new File(new String(fileName));
                System.out.println("[SendFile] File requested: '"+file.getPath()+"'");

                FileInputStream fis=null;
                try{
                    long size=file.length();
                    System.out.println("[SendFile] File size: "+size);
                    //send size info
                    out.write(PacketUtil.encode(size));
                    long start=System.currentTimeMillis();
                    //and send the file
                    fis=new FileInputStream(file);
                    IoUtils.copy(fis, out, size);
                        //Util.copy(fis, out, size, false);

                    long end=System.currentTimeMillis();
                    System.out.println(((UDTSocket)socket).getSession().getStatistics().toString());
                    double rate=1000.0*size/1024/1024/(end-start);
                    System.out.println("[SendFile] Rate: "+format.format(rate)+" MBytes/sec. "+format.format(8*rate)+" MBit/sec.");
                    if(Boolean.getBoolean("udt.sender.storeStatistics")){
                        ((UDTSocket)socket).getSession().getStatistics().writeParameterHistory(new File("udtstats-"+System.currentTimeMillis()+".csv"));
                    }
                }finally{
                    socket.close();
                    //socket.getSender().stop();
                    if(fis!=null)fis.close();
                }
                log.info("Finished request from "+((UDTSocket)socket).getSession().getDestination());
            }catch(Exception ex){
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        }
        
        private void requestAndResponseOnSocket(final Socket sock) 
            throws IOException, InterruptedException {
            final InputStream is = sock.getInputStream();
            final OutputStream os = sock.getOutputStream();
            
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String curLine = reader.readLine();
            while (StringUtils.isNotBlank(curLine)) {
                log.info("curLine: "+curLine);
                curLine = reader.readLine();
            }
            
            final File file = new File("visualvm.zip");
            
            os.write("HTTP/1.1 200 OK\r\n".getBytes());
            os.write(("Content-Length: "+file.length()+"\r\n").getBytes());
            //os.write("\r\n".getBytes());
            //os.write("\r\n".getBytes());
            os.write("\r\n".getBytes());
            //os.flush();
            
            
            final FileInputStream fis = new FileInputStream(file);
            try {
                IoUtils.copy(fis, os, file.length());
            }
            finally {
                sock.close();
                fis.close();
            }
        }
        
		public void run2(){
			try{
				log.info("Handling request from "+socket.getRemoteSocketAddress());
				InputStream in=socket.getInputStream();
				OutputStream out=socket.getOutputStream();
				byte[]readBuf=new byte[32768];
				ByteBuffer bb=ByteBuffer.wrap(readBuf);

				//read file name info 
				while(in.read(readBuf)==0)Thread.sleep(100);

				//how many bytes to read for the file name
				int length=bb.getInt();
				byte[]fileName=new byte[length-1];
				bb.get(fileName);

				File file=new File(new String(fileName));
				System.out.println("[SendFile] File requested: '"+file.getPath()+"'");

				FileInputStream fis=null;
				try{
					long size=file.length();
					System.out.println("[SendFile] File size: "+size);
					//send size info
					out.write(PacketUtil.encode(size));
					long start=System.currentTimeMillis();
					//and send the file
					fis=new FileInputStream(file);
					Util.copy(fis, out, size, false);

					long end=System.currentTimeMillis();
					System.out.println(((UDTSocket)socket).getSession().getStatistics().toString());
					double rate=1000.0*size/1024/1024/(end-start);
					System.out.println("[SendFile] Rate: "+format.format(rate)+" MBytes/sec. "+format.format(8*rate)+" MBit/sec.");
					if(Boolean.getBoolean("udt.sender.storeStatistics")){
					    ((UDTSocket)socket).getSession().getStatistics().writeParameterHistory(new File("udtstats-"+System.currentTimeMillis()+".csv"));
					}
				}finally{
				    socket.close();
					//socket.getSender().stop();
					if(fis!=null)fis.close();
				}
				log.info("Finished request from "+((UDTSocket)socket).getSession().getDestination());
			}catch(Exception ex){
				ex.printStackTrace();
				throw new RuntimeException(ex);
			}
		}
	}
	
}
