import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.URL;
import java.net.URLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.io.*;

public class WebServer {
	public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/entrypoint", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();    
    }

    static class MyHandler implements HttpHandler {
    	//este metodo e quem trata dos requests 
        @Override
        public void handle(HttpExchange t) throws IOException {     
            //cada request recebido sera tratado por uma thread diferente, passamos como parametro, o request recebido
           Thread thread = new MyThread(t);
           thread.start();         
        }
    }

    static class MyThread extends Thread
    {
    	private String cpu, memory, image, requestClass, requestType, makespan, portNumber;
    	HttpExchange t;
    	String query;

    	public MyThread(HttpExchange t)
    	{
    		this.t = t;
    		query = t.getRequestURI().getQuery(); //guardamos o que o user introduziu em string
    		if(query != null) 
			{
				String[] parts = query.split("&"); 
				cpu = parts[0];		
				memory = parts[1];
				image = parts[2];
				requestClass = parts[3];
				requestType = parts[4];
				makespan = parts[5];
				portNumber = parts[6];
    		}
    	}

    	//codigo corrido quando fazemos start na thread
    	@Override
    	public void run() 
    	{	
		if(query != null){
                        try {
                                Runtime rt = Runtime.getRuntime();
                                Process pr;
//                                      Process pr = rt.exec("docker -H tcp://0.0.0.0:2376 run -itd -c " + cpu + " -m " + memory + " -e affinity:requestclass==" + requestClass + " -e a$
                                        //TESTING
                                if (requestClass.equals("0")) { //for other scheduling algorithms
					if (requestType.equals("service")) {
                             			if (image.equals("redis"))
	                                        	pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -p " + portNumber +":"+ portNumber + " -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + "-e affinity:port==" + portNumber + " " +  image + " --port " + portNumber);
						else
                                        		pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -p " + portNumber +":"+ portNumber + " -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + "-e affinity:port==" + portNumber + " " +  image + " " + portNumber);
					}
					else { // a job
						if ( image.equals("enhance")) //mem/cpu intensive job
							pr = rt.exec("docker -H tcp://10.5.60.2:2377 run --rm -v /home/smendes:/ne/input -itd -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + " alexjc/neural-enhance --zoom=2 input/macos.jpg");
						else //cpu intensive job
							pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + " " + image);
					}
				} else { // for energy algorithm
					if (requestType.equals("service")) {
                             			if (image.equals("redis"))
	                                        	pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -p " + portNumber +":"+ portNumber + " -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + "-e affinity:port==" + portNumber + " " +  image + " --port " + portNumber);
						else
                                        		pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -p " + portNumber +":"+ portNumber + " -c " + cpu + " -m " + memory + " -e affinity:requestclass==" + requestClass + " -e affinity:makespan==" + makespan + " -e affinity:requesttype==" + requestType + " -e affinity:port==" + portNumber + " " +  image + " " + portNumber);
 					}
                                       	else {
						if ( image.equals("enhance")) //mem/cpu intensive job
							pr = rt.exec("docker -H tcp://10.5.60.2:2377 run --rm -v /home/smendes:/ne/input -itd -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + " -e affinity:requestclass==" + requestClass + " -e affinity:requesttype==" + requestType + " alexjc/neural-enhance --zoom=2 input/macos.jpg");
						else
						 	pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -c " + cpu + " -m " + memory + " -e affinity:requestclass==" + requestClass + " -e affinity:makespan==" + makespan + " -e affinity:requesttype==" + requestType + " " +  image);
                        		}
			        }
                                int exitVal = pr.waitFor();
                                if (exitVal != 0) { //failed allocation
					System.out.println("Failed");

                                        try(FileWriter fw = new FileWriter("energyFailed.txt", true);
                                        BufferedWriter bw = new BufferedWriter(fw);
                                        PrintWriter out = new PrintWriter(bw))
                                        {
                                                out.println("1");
                                        } catch (IOException e) {
                                                System.out.println("Exception writing to file " + e);
                                        }
                                } else { //successful allocation
					System.out.println("Success");

                                        try(FileWriter fw = new FileWriter("energySuccess.txt", true);
                                        BufferedWriter bw = new BufferedWriter(fw);
                                        PrintWriter out = new PrintWriter(bw))
                                        {
                                                out.println("1");
                                        } catch (IOException e) {
                                                System.out.println("Exception writing to file " + e);
                                        }
                                }
                                String response = "Successfully scheduled request";
                                t.sendResponseHeaders(200, response.length());
                                OutputStream os = t.getResponseBody();
                                os.write(response.getBytes());
                                os.close();
                        } catch(Exception e) {
                                e.printStackTrace();
                        }
                }
    	}
	}
}
