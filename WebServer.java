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
    	private String cpu, memory, image, requestClass, requestType, makespan;
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
                                        pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + " " +  image);
                                } else { // for energy algorithm
                                        pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -c " + cpu + " -m " + memory + " -e affinity:requestclass==" + requestClass + " -e affinity:makespan==" + makespan + " -e affinity:requesttype==" + requestType + " " +  image);
                                }
                                int exitVal = pr.waitFor();
                                if (exitVal != 0) { //failed allocation
                                        try(FileWriter fw = new FileWriter("energyFailed.txt", true);
                                        BufferedWriter bw = new BufferedWriter(fw);
                                        PrintWriter out = new PrintWriter(bw))
                                        {
                                                out.println("1");
                                        } catch (IOException e) {
                                                System.out.println("Exception writing to file " + e);
                                        }
                                } else { //successful allocation
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
