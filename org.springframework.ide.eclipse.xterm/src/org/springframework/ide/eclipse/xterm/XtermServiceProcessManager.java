package org.springframework.ide.eclipse.xterm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;


public class XtermServiceProcessManager {
	
	private static final int INVALID_PORT = -1;
	
	private HttpClient httpClient = HttpClientBuilder.create().build();
	
	private Process process;
	
	private int port = INVALID_PORT;
	
	synchronized void startProcess() throws IOException {
		port = findFreePort();
		Bundle bundle = XtermPlugin.getDefault().getBundle();
		URL url = FileLocator.find(bundle, new Path("/lib/xterm.jar"), null);
		url = FileLocator.toFileURL(url);
		ProcessBuilder builder = new ProcessBuilder(
				Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
				"-jar",
				Paths.get(url.getPath()).toString(),
				"--server.port=" + port,
				"--management.endpoint.shutdown.enabled=true",
				"--management.endpoints.web.exposure.include=health,info,shutdown",
				"--terminal.pty.shutdown=delay", // terminal pty process destroyed right after sockets closed
				"--terminal.pty.shutdown-delay=5",
				"--terminal.auto-shutdown.on=true", // terminal app can shutdown itself if not used 
				"--terminal.auto-shutdown.delay=30" // terminal app shuts itself down in not used for 30 sec	
		);
	
		String tempDir = System.getProperty("java.io.tmpdir");
		File logFile = Paths.get(tempDir, "xterm-log.log").toFile();
		builder.redirectError(logFile);
		builder.redirectOutput(logFile);
		
		process = builder.start();
	}
	
	synchronized private void waitUntilStarted() {
		do {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				XtermPlugin.log(e);
				this.port = INVALID_PORT;
			}
		} while (!isStarted(port));
	}
	
	synchronized private boolean isStarted(int port) {
		HttpGet request = new HttpGet("http://localhost:" + port);
		try {
			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
//				ByteArrayOutputStream result = new ByteArrayOutputStream();
//				byte[] buffer = new byte[1024];
//				int length;
//				while ((length = response.getEntity().getContent().read(buffer)) != -1) {
//					result.write(buffer, 0, length);
//				}
//				String responseStr = result.toString(StandardCharsets.UTF_8.name());
//				return responseStr.matches("\\s*\\{\\s*\"status\"\\s*:\\s*\"UP\"\\s*\\}\\s*");
				return true;
			}
		} catch (IOException e) {
			// Ignore
		}
		return false;
	}
	
	synchronized void stopService() {
		if (process != null && process.isAlive()) {
			if (port > 0) {
				HttpPost request = new HttpPost("http://localhost:" + port + "/actuator/shutdown");
				request.setHeader("Content-Type", "application/json");
				try {
					HttpResponse response = httpClient.execute(request);
					int code = response.getStatusLine().getStatusCode();
					if (code != 200) {
						process.destroy();
					}
				} catch (ClientProtocolException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					process.destroy();
				}
			} else {
				process.destroy();
			}
		}
		process = null;
	}
	
	synchronized String serviceUrl() throws IOException {
		if (port == INVALID_PORT && process != null && process.isAlive()) {
			process.destroy();
			process = null;
		}
		if (process == null || !process.isAlive()) {
			startProcess();
			waitUntilStarted();
		}
		return "http://localhost:" + port;
	}
	
	private static int findFreePort() throws IOException {
	    ServerSocket socket = new ServerSocket(0);
	    try {
	        return socket.getLocalPort();
	    } finally {
	        try {
	            socket.close();
	        } catch (IOException e) {
	        }
	    }
	}


}
