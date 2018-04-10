/*
 * Copyright (c) 2018 by Oliver Boehm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 27.03.2018 by oboehm (ob@oasd.de)
 */
package j4cups.server;

import org.apache.http.*;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * The CupsServer is a little embedded HTTP server based on Apache's HTTP
 * components. It is based on HTTP/1.1 and a classic (blocking) I/O model.
 *
 * @author oboehm
 * @since 0.5 (27.03.2018)
 */
public class CupsServer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CupsServer.class);
    private final int port;
    private final HttpServer server;

    /**
     * Instantiates a CUPS server with the IPP standard port (631).
     */
    public CupsServer() {
        this(631);
    }

    /**
     * Instantiates a CUPS server with the given IPP port.
     * 
     * @param port e.g. 631
     */
    public CupsServer(int port) {
        this.port = port;
        this.server = createServer(getPort());
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Please specify document root directory");
            System.exit(1);
        }
        // Document root directory
        String docRoot = args[0];
        int port = 8090;
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }
        new CupsServer(port).start();
    }

    /**
     * Gives the port where the server is running.
     * 
     * @return port, e.g. 631
     */
    public int getPort() {
        return port;
    }

    /**
     * This is the method to start the server in the background.
     */
    public Thread start() {
        String name = "CupSrv-" + getPort();
        Thread t = new Thread(this, this.toString());
        t.start();
        return t;
    }

    /**
     * This is the command to shut down the server.
     */
    public void shutdown() {
        LOG.info("Shutting down {} on port {} ...", server, port);
        server.shutdown(5, TimeUnit.SECONDS);
        LOG.info("Shutting down {} on port {} was successful.", server, port);
    }
    
    /**
     * This is the method to run the server directly. This method will not
     * end until you kill it.
     */
    public void run() {
        try {
            LOG.info("Starting {} on port {}...", server, port);
            server.start();
            server.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdown(5, TimeUnit.SECONDS)));
        } catch (IOException ioe) {
            LOG.warn("Cannot start {}:", server, ioe);
        } catch (InterruptedException ie) {
            LOG.warn("{} was interrupted and will be shutdown:", server, ie);
            server.shutdown(5, TimeUnit.SECONDS);
        }
    }

    private static HttpServer createServer(int serverPort) {
        SocketConfig socketConfig = SocketConfig.custom()
                                                .setSoTimeout(15000)
                                                .setTcpNoDelay(true)
                                                .build();
        return ServerBootstrap.bootstrap()
                              .setListenerPort(serverPort)
                              .setServerInfo("Test/1.1")
                              .setSocketConfig(socketConfig)
                              .setExceptionLogger(new StdErrorExceptionLogger())
                              .registerHandler("*", new HttpFileHandler("."))
                              .create();
    }

    static class StdErrorExceptionLogger implements ExceptionLogger {

        @Override
        public void log(final Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                System.err.println("Connection timed out");
            } else if (ex instanceof ConnectionClosedException) {
                System.err.println(ex.getMessage());
            } else {
                ex.printStackTrace();
            }
        }

    }

    static class HttpFileHandler implements HttpRequestHandler  {

        private final String docRoot;

        public HttpFileHandler(final String docRoot) {
            super();
            this.docRoot = docRoot;
        }

        public void handle(
                final HttpRequest request,
                final HttpResponse response,
                final HttpContext context) throws HttpException, IOException {

            String method = request.getRequestLine().getMethod().toUpperCase(Locale.ROOT);
            if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
                throw new MethodNotSupportedException(method + " method not supported");
            }
            String target = request.getRequestLine().getUri();

            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                byte[] entityContent = EntityUtils.toByteArray(entity);
                System.out.println("Incoming entity content (bytes): " + entityContent.length);
            }

            final File file = new File(this.docRoot, URLDecoder.decode(target, "UTF-8"));
            if (!file.exists()) {

                response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                StringEntity entity = new StringEntity(
                        "<html><body><h1>File" + file.getPath() +
                                " not found</h1></body></html>",
                        ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                System.out.println("File " + file.getPath() + " not found");

            } else if (!file.canRead() || file.isDirectory()) {

                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                StringEntity entity = new StringEntity(
                        "<html><body><h1>Access denied</h1></body></html>",
                        ContentType.create("text/html", "UTF-8"));
                response.setEntity(entity);
                System.out.println("Cannot read file " + file.getPath());

            } else {
                HttpCoreContext coreContext = HttpCoreContext.adapt(context);
                HttpConnection conn = coreContext.getConnection(HttpConnection.class);
                response.setStatusCode(HttpStatus.SC_OK);
                FileEntity body = new FileEntity(file, ContentType.create("text/html", (Charset) null));
                response.setEntity(body);
                System.out.println(conn + ": serving file " + file.getPath());
            }
        }
    }

    /**
     * For a useful toString implementation we put the port into it.
     * 
     * @return string representation with port value
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + this.getPort();
    }

}
