import java.io.*;
import java.net.*;
import java.util.*;

public final class WebServer {
    public static void main(String argv[]) throws Exception {
        int port = 3333;
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            HttpRequest request = new HttpRequest(clientSocket);
            Thread thread = new Thread(request);
            thread.start();
        }
    }
}

final class HttpRequest implements Runnable {
    final static String CRLF = "\r\n";
    Socket socket;

    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;

    }

    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception {
        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        FilterInputStream fis;
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String requestLine = br.readLine();
        System.out.println();
        System.out.println(requestLine);
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken();
        String fileName = tokens.nextToken();
        fileName = "." + fileName;
        FileInputStream fis1 = null;
        boolean fileExists = true;
        try {
            fis1 = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            fileExists = false;
        }
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (fileExists) {
            statusLine = "Responding to existing file";
            contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
        } else {
            statusLine = "File does not exist \n";
            contentTypeLine = "Content-Type:" + contentType(fileName) + CRLF;
            entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</Title></HEAD>" +
                    "<BODY>Not Found</BODY></HTML>";
            os.writeBytes(statusLine);
            os.writeBytes(contentTypeLine);
            os.writeBytes(CRLF);

            if (fileExists) {
                sendBytes(fis1, os);
                fis1.close();
            } else {
                os.writeBytes(entityBody);
            }

            os.close();
            br.close();
            socket.close();
        }
    }

    private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
        byte[] buffer = new byte[1024];
        int bytes = 0;
        while ((bytes = fis.read(buffer)) != -1) {
            os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return "text/html";
        }
        if (fileName.endsWith(".gif") || fileName.endsWith(".GIF")) {
            return "image/gif";
        }
        if (fileName.endsWith(".jpeg") || fileName.endsWith(".JPEG")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }
}

