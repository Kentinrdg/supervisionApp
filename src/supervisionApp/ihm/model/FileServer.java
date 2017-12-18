package supervisionApp.ihm.model;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {

	public void startFileServer() throws IOException {
		int bytesRead;
		int current = 0;

		ServerSocket serverSocket = null;
		serverSocket = new ServerSocket(13267);

		while (true) {
			System.out.println("Server OK");
			Socket clientSocket = null;
			clientSocket = serverSocket.accept();
			
			InputStream in = clientSocket.getInputStream();
			DataInputStream clientData = new DataInputStream(in);

			String fileName = clientData.readUTF();
			OutputStream output = new FileOutputStream(fileName);
			long size = clientData.readLong();
			byte[] buffer = new byte[1024];
			while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
				output.write(buffer, 0, bytesRead);
				size -= bytesRead;
			}

			// Closing the FileOutputStream handle
			in.close();
			clientData.close();
			output.close();
		}
	}
}