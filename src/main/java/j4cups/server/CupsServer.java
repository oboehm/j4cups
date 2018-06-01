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

import j4cups.server.http.IppPrinterRequestHandler;
import j4cups.server.http.IppServerRequestHandler;
import j4cups.server.http.LogRequestInterceptor;
import j4cups.server.http.LogResponseInterceptor;
import org.apache.http.ConnectionClosedException;
import org.apache.http.ExceptionLogger;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URI;
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
    private Thread serverThread;

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
        this(port, URI.create("http://localhost:631"));
    }

    /**
     * Instantiates a CUPS server with the given IPP port. The request will be
     * forwarded to the given forwardURI.
     *
     * @param port e.g. 631
     * @param forwardURI CUPS server where the requests are forwarded to
     */
    public CupsServer(int port, URI forwardURI) {
        this.port = port;
        this.server = createServer(port, forwardURI);
    }

    /**
     * This is the CLI interface to start the CupsServer. If you want to set
     * the port use it as second argument. The first parameter is reserved for
     * the command ("start" or "stop").
     * 
     * @param args "start", "631"
     */
    public static void main(String... args) {
        if (args.length < 1) {
            System.out.println("Usage: " + CupsServer.class.getName() + " start [port]");
            return;
        }
        String command = args[0];
        int serverPort = 631;
        if (args.length > 1) {
            serverPort = Integer.parseInt(args[1]);
        }
        if ("start".equalsIgnoreCase(command.trim())) {
            CupsServer cs = new CupsServer(serverPort);
            cs.start();
            System.out.println(cs + " is started.");
        }
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
        serverThread = new Thread(this, this.toString());
        serverThread.start();
        return serverThread;
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
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    private static HttpServer createServer(int serverPort, URI forwardURI) {
        SocketConfig socketConfig = SocketConfig.custom()
                                                .setSoTimeout(15000)
                                                .setTcpNoDelay(true)
                                                .build();
        return ServerBootstrap.bootstrap()
                              .setListenerPort(serverPort)
                              .setServerInfo("j4CUPS/0.5")
                              .setSocketConfig(socketConfig)
                              .setExceptionLogger(new StdErrorExceptionLogger())
                              .registerHandler("/printers/*", new IppPrinterRequestHandler())
                              .registerHandler("*", new IppServerRequestHandler(forwardURI))
                              .addInterceptorFirst(new LogRequestInterceptor("S"))
                              .addInterceptorLast(new LogResponseInterceptor("S"))
                              .create();
    }

    /**
     * Looks, if the server is running.
     * 
     * @return true if server was started
     */
    public boolean isStarted() {
        return serverThread != null;
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



    static class StdErrorExceptionLogger implements ExceptionLogger {

        @Override
        public void log(final Exception ex) {
            if (ex instanceof SocketTimeoutException) {
                LOG.warn("Connection timed out ({}).", ex.getMessage());
            } else if (ex instanceof ConnectionClosedException) {
                LOG.warn("Conncection is closed ({}).", ex.getMessage());
            } else {
                LOG.error("Shit happened:", ex);
            }
            LOG.debug("Details:", ex);
        }

    }

}
