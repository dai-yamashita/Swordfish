/*****************************************************************************
Copyright (c) 2007-2021 - Maxprograms,  http://www.maxprograms.com/

Permission is hereby granted, free of charge, to any person obtaining a copy of 
this software and associated documentation files (the "Software"), to compile, 
modify and use the Software in its executable form without restrictions.

Redistribution of this Software or parts of it in any form (source code or 
executable binaries) requires prior written permission from Maxprograms.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
SOFTWARE.
*****************************************************************************/
package com.maxprograms.swordfish;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONObject;

public class TmsServer implements HttpHandler {

	private static Logger logger = System.getLogger(TmsServer.class.getName());
	private HttpServer server;
	private static boolean debug;
	private static File workDir;

	public TmsServer(Integer port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
	}

	public static void main(String[] args) {
		String port = "8070";
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-version")) {
				logger.log(Level.INFO, () -> "Version: " + Constants.VERSION + " Build: " + Constants.BUILD);
				return;
			}
			if (arg.equals("-port") && (i + 1) < args.length) {
				port = args[i + 1];
			}
			if (arg.equals("-debug")) {
				debug = true;
			}
		}
		try {
			TmsServer instance = new TmsServer(Integer.valueOf(port));
			instance.run();
		} catch (Exception e) {
			logger.log(Level.ERROR, "Server error", e);
		}
	}

	private void run() {
		server.createContext("/projects", new ProjectsHandler());
		server.createContext("/memories", new MemoriesHandler());
		server.createContext("/glossaries", new GlossariesHandler());
		server.createContext("/services", new ServicesHandler());
		server.createContext("/", this);
		server.setExecutor(new ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100)));
		server.start();
		if (debug) {
			logger.log(Level.INFO, "TMS server started");
		}
	}

	@Override
	public void handle(HttpExchange t) throws IOException {
		try {
			String request = "";
			try (InputStream is = t.getRequestBody()) {
				request = readRequestBody(is);
			}
			if (request.isBlank()) {
				throw new IOException("Empty request");
			}
			if (debug) {
				logger.log(Level.INFO, request);
			}
			String response = "";
			JSONObject json = new JSONObject(request);
			String command = json.getString("command");
			switch (command) {
				case "version":
					JSONObject obj = new JSONObject();
					obj.put("tool", "TMSServer");
					obj.put("version", Constants.VERSION);
					obj.put("build", Constants.BUILD);
					response = obj.toString();
					break;
				case "stop":
					if (debug) {
						logger.log(Level.INFO, "Stopping server");
					}
					break;
				default:
					JSONObject unknown = new JSONObject();
					unknown.put(Constants.STATUS, Constants.ERROR);
					unknown.put(Constants.REASON, "Unknown command");
					unknown.put("received", json.toString());
					response = unknown.toString();
			}
			if (debug) {
				logger.log(Level.INFO, response);
			}
			t.getResponseHeaders().add("content-type", "application/json; charset=utf-8");
			byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
			t.sendResponseHeaders(200, bytes.length);
			try (ByteArrayInputStream stream = new ByteArrayInputStream(bytes)) {
				try (OutputStream os = t.getResponseBody()) {
					byte[] array = new byte[2048];
					int read;
					while ((read = stream.read(array)) != -1) {
						os.write(array, 0, read);
					}
				}
			}
			if ("stop".equals(command)) {
				if (debug) {
					logger.log(Level.INFO, "Stopping server");
				}
				System.exit(0);
			}
		} catch (IOException e) {
			logger.log(Level.ERROR, e);
			String message = e.getMessage();
			t.sendResponseHeaders(500, message.length());
			try (OutputStream os = t.getResponseBody()) {
				os.write(message.getBytes(StandardCharsets.UTF_8));
			}
		}
	}

	protected static String readRequestBody(InputStream is) throws IOException {
		StringBuilder request = new StringBuilder();
		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
			String line;
			while ((line = rd.readLine()) != null) {
				request.append(line);
			}
		}
		return request.toString();
	}

	public static File getWorkFolder() throws IOException {
		if (workDir == null) {
			String os = System.getProperty("os.name").toLowerCase();
			if (os.startsWith("mac")) {
				workDir = new File(System.getProperty("user.home") + "/Library/Application Support/Swordfish/");
			} else if (os.startsWith("windows")) {
				workDir = new File(System.getenv("AppData") + "\\Swordfish\\");
			} else {
				workDir = new File(System.getProperty("user.home") + "/.config/Swordfish/");
			}
			if (!workDir.exists()) {
				Files.createDirectories(workDir.toPath());
			}
		}
		return workDir;
	}

	public static void deleteFolder(String folder) throws IOException {
		File f = new File(folder);
		if (f.isDirectory()) {
			String[] list = f.list();
			for (int i = 0; i < list.length; i++) {
				deleteFolder(new File(f, list[i]).getAbsolutePath());
			}
		}
		Files.delete(f.toPath());
	}

	public static boolean isDebug() {
		return debug;
	}

	public static String getCatalogFile() throws IOException {
        File preferences = new File(getWorkFolder(), "preferences.json");
        StringBuilder builder = new StringBuilder();
        try (FileReader reader = new FileReader(preferences, StandardCharsets.UTF_8)) {
            try (BufferedReader buffer = new BufferedReader(reader)) {
                String line = "";
                while ((line = buffer.readLine()) != null) {
                    builder.append(line);
                }
            }
        }
        JSONObject json = new JSONObject(builder.toString());
        return json.getString("catalog");
    }
}
