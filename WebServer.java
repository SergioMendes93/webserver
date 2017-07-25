import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.URL;
import java.net.URLConnection;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class WebServer {
	static List<Long> schedulingTimes = new ArrayList<Long>();

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
	int i = 0;

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
				if ( requestClass.equals("5") ) { //this means it has finished
					long average = 0;
					for (Long time: schedulingTimes) {
						average += time.longValue();
					}
					String averageTime = Long.toString(average / schedulingTimes.size());
					System.out.println("Collected average scheduling time");

					try(FileWriter fw = new FileWriter("schedulingAverageSpeed.txt", true);
                                        BufferedWriter bw = new BufferedWriter(fw);
                                        PrintWriter out = new PrintWriter(bw))
                                        {
                                                out.println(averageTime);
                                        } catch (IOException e) {
                                                System.out.println("Exception writing to file " + e);
}
				}

                                Runtime rt = Runtime.getRuntime();
                                Process pr;
				long startTime = 0;
				long timeNow = 0;
//                                      Process pr = rt.exec("docker -H tcp://0.0.0.0:2376 run -itd -c " + cpu + " -m " + memory + " -e affinity:requestclass==" + requestClass + " -e a$
                                        //TESTING
                                if (requestClass.equals("0")) { //for other scheduling algorithms
					if (requestType.equals("service")) {
                             			if (image.equals("redis")) {
	                                        	startTime = System.nanoTime();
							pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -p " + portNumber +":"+ portNumber + " -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + " -e affinity:port==" + portNumber + " " +  image + " --port " + portNumber);
							timeNow = System.nanoTime() - startTime;
							schedulingTimes.add(timeNow);
							System.out.println("Collecting time " + timeNow);
						}
						else {
		                                     	startTime = System.nanoTime();
							pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -p " + portNumber +":"+ portNumber + " -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + " -e affinity:port==" + portNumber + " " +  image + " " + portNumber);
							timeNow = System.nanoTime() - startTime;
							schedulingTimes.add(timeNow);
							System.out.println("Collecting time " + timeNow);
						}
					}
					else { // a job
						if ( image.equals("enhance")) { //mem/cpu intensive job
		                                     	startTime = System.nanoTime();
							pr = rt.exec("docker -H tcp://10.5.60.2:2377 run  -v /home/smendes:/ne/input -itd -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + " alexjc/neural-enhance --zoom=2 input/macos.jpg");
							timeNow = System.nanoTime() - startTime;
							schedulingTimes.add(timeNow);
							System.out.println("Collecting time " + timeNow);
						}else {//cpu intensive job {
		                                        startTime = System.nanoTime();
							pr = rt.exec("docker -H tcp://10.5.60.2:2377 run  -v /home/smendes:/tmp/workdir -w=/tmp/workdir -itd -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + " " + " jrottenberg/ffmpeg -i dead.avi -r 100 -b 700k -qscale 0 -ab 160k -ar 44100 result"+i+".dvd -y ");
							timeNow = System.nanoTime() - startTime;
							schedulingTimes.add(timeNow);
							System.out.println("Collecting time " + timeNow);
							i++;
						}
					}
				} else { // for energy algorithm
					if (requestType.equals("service")) {
                             			if (image.equals("redis")) {
				                      	startTime = System.nanoTime();
	                                        	pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -p " + portNumber + ":" + portNumber + " -c " + cpu + " -m " + memory + " -e affinity:makespan==" + makespan + " -e affinity:port==" + portNumber + " -e affinity:requestclass==" + requestClass + " -e affinity:requesttype==" + requestType + " " +  image + " --port " + portNumber);
							timeNow = System.nanoTime() - startTime;
							schedulingTimes.add(timeNow);
							System.out.println("Collecting time " + timeNow);
						}
						else {
					            	startTime = System.nanoTime();
                                        		pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -itd -p " + portNumber + ":" + portNumber + " -c " + cpu + " -m " + memory + " -e affinity:requestclass==" + requestClass + " -e affinity:makespan==" + makespan + " -e affinity:requesttype==" + requestType + " -e affinity:port==" + portNumber + " " +  image + " " + portNumber);
							timeNow = System.nanoTime() - startTime;
							schedulingTimes.add(timeNow);
							System.out.println("Collecting time " + timeNow);
						}
					}
                                       	else {
						if (image.equals("enhance")) { //mem/cpu intensive job {
 			                          	startTime = System.nanoTime();
							pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -v /home/smendes:/ne/input -itd -c " + cpu + " -m " + memory +" -e affinity:makespan==" + makespan + " -e affinity:requestclass==" + requestClass + " -e affinity:requesttype==" + requestType + " alexjc/neural-enhance --zoom=2 input/macos.jpg");
							timeNow = System.nanoTime() - startTime;
							schedulingTimes.add(timeNow);
							System.out.println("Collecting time " + timeNow);
						}
						else {			 
	                                        	startTime = System.nanoTime();
							pr = rt.exec("docker -H tcp://10.5.60.2:2377 run -v /home/smendes:/tmp/workdir -w=/tmp/workdir -itd -c " + cpu + " -m " + memory + " -e affinity:requestclass==" + requestClass + " -e affinity:makespan==" + makespan + " -e affinity:requesttype==" + requestType + " jrottenberg/ffmpeg -i dead.avi -r 100 -b 700k -qscale 0 -ab 160k -ar 44100 result"+i+".dvd -y");
							timeNow = System.nanoTime() - startTime;
							schedulingTimes.add(timeNow);
							System.out.println("Collecting time " + timeNow);
                        				i++;
						}
					}
			        }
                                int exitVal = pr.waitFor();

                                if (exitVal != 0) { //failed allocation
					//System.out.println("Failed " + image + " error code: " + exitVal);

					BufferedReader b = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
                                	/*String line;
                                	if ((line = b.readLine()) != null)
                                        	System.out.println(line);
*/

                                        try(FileWriter fw = new FileWriter("energyFailed.txt", true);
                                        BufferedWriter bw = new BufferedWriter(fw);
                                        PrintWriter out = new PrintWriter(bw))
                                        {
                                                out.println("1");
                                        } catch (IOException e) {
                                                System.out.println("Exception writing to file " + e);
                                        }
                                } else { //successful allocation
					//System.out.println("Success " + image);

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
