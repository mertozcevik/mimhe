import java.net.*;
import java.io.*;
import java.util.*;

public class FileDownloader {

	public static void main(String[] args) throws IOException {
		//If no argument is given, it stops working.
		if(args.length<1){
			System.out.println("Exception: You need to enter an index URL. Try again.");
		}
		//In this part of code, the host and index names are gathered from the given argument
		//After that, necessary connections is handled with host through socket port 80 which is HTTP protocol
		//It send the get request to the server, if index file is found (checks via status code), continuous to operate
		//Else, it gives and error and stops working.
		else{

			String index;
			index=args[0];

			String[] parts= args[0].split("/");

			String host=parts[0];

			String file="";

			for(int i=1;i<parts.length;i++){

				file=file.concat("/" + parts[i]);

			}

			Socket endSocket=new Socket(host,80);

			OutputStream os = endSocket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);

			bw.write("GET " + file + " HTTP/1.1\r\n");
			bw.write("Host: " + host + "\r\n\r\n");
			bw.flush();

			InputStream is = endSocket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			String head=br.readLine();
			String[] header=head.split(" ");

			//If status code is 200 which means file is found, it checks for whether endpoints are also given or not.
			//It adjust lower and upper endpoints respectively (If it is not given, HTTP GET requests to the urls are still done with range and the range is simply lenght of data)
			//
			if(header[1].equals("200")){

				String result="";

				String lower="";
				String upper="";
				boolean flag=false;

				System.out.println("URL of the index file: " + index);

				if(args.length!=2)
					System.out.println("No range is given");
				else{
					String[] endpoints;
					endpoints=args[1].split("-");
					lower=lower.concat(endpoints[0]);
					upper=upper.concat(endpoints[1]);
					flag=true;
					System.out.println("Lower endpoint = " + lower);
					System.out.println("Upper endpoint = " + upper);
				}

				System.out.println("Index file is downloaded");

				int count=0;
				String msg;
				//Skips HTTP response messages
				while(count!=8){

					msg=br.readLine();
					count++;

				}

				count=0;
				//While there are links in the index file, code makes connections with servers and get data.
				//Here it checks whether given host has the file in it. If not, is not found is printed.
				//If file is found in the host, code gets the lenght of it first to make neccessary comparisons
				while((msg=br.readLine())!=null){

					count++;

					parts=msg.split("/");
					host=parts[0];
					file="";
					for(int i=1;i<parts.length;i++){

						file=file.concat("/" + parts[i]);

					}

					Socket newSocket=new Socket(host,80);

					OutputStream osn = newSocket.getOutputStream();
					OutputStreamWriter oswn = new OutputStreamWriter(osn);
					BufferedWriter bwn = new BufferedWriter(oswn);

					bwn.write("HEAD " + file + " HTTP/1.1\r\n");
					bwn.write("Host: " + host + "\r\n\r\n");
					bwn.flush();

					InputStream isn = newSocket.getInputStream();
					InputStreamReader isrn = new InputStreamReader(isn);
					BufferedReader brn = new BufferedReader(isrn);

					head=brn.readLine();
					String[] temp=head.split(" ");

					if(temp[1].equals("404")){
						result=result.concat(count + ". " + msg + " is not found\r\n");
					}
					else{
						boolean check=false;
						String hold;
						result=result.concat(count + ". " + msg + " ");

						while(!(hold=brn.readLine()).contains("Content-Length:")){

						}
						String[] divide;
						divide=hold.split(" ");
						//Flag is to see whether endpoints given or not
						//If they are not given (flag=false), lower endpoint is set to 0 and upper endpoint is set to length.
						if(!flag){

							lower=lower.concat("0");
							upper=upper.concat(divide[1]);
							result=result.concat("(size = " + upper + ") is downloaded\r\n");

							bwn.write("GET " + file + " HTTP/1.1\r\n");
							bwn.write("Host: " + host + "\r\n");
							bwn.write("Range: bytes=" + lower + "-" + upper + "\r\n\r\n");
							bwn.flush();

							upper="";
							lower="";

						}
						//If endpoints is set by user via arguments, it just make a HTTP request to get data
						else{
							//If endpoints is within the range
							if(lower.compareTo(divide[1])<0){
								result=result.concat(count + ". " + msg + " (range = " + lower + "-" + upper + ") is downloaded\r\n");

								bwn.write("GET " + file + " HTTP/1.1\r\n");
								bwn.write("Host: " + host + "\r\n");
								bwn.write("Range: bytes=" + lower + "-" + upper + "\r\n\r\n");
								bwn.flush();

							}
							//If endpoints is not within the range, not any HTTP requests is made.
							else{
								result=result.concat(count + ". " + msg + " (size = " + divide[1] + ") is not downloaded\r\n");
								check=true;
							}
						}
						//Lastly, downloaded data will be written into txt files.
						if(check==true)
							check=false;
						else{
							String text;
							String[] link;
							link=msg.split("/");

							PrintWriter writer = new PrintWriter(link[link.length-1], "UTF-8");

							boolean ok=false;
							boolean ok2=false;
							boolean ok3=false;

							while((text=brn.readLine())!=null){

									if(ok3==false && text.contains("Content-Type:")){
										ok3=true;
										ok=true;
									}
									else if(ok==true && text.contains("Content-Type:")){
										ok2=true;
										text=brn.readLine();
									}
									else if(ok2){
										writer.println(text);
									}
							}
						    writer.close();


						}


					}

					newSocket.close();

				}

				System.out.println("There are " + count + " files in the index");
				System.out.println(result);
				endSocket.close();

			}
			else{

				System.out.println("Error; index file is not found in the server.");

			}
		}
	}

}
