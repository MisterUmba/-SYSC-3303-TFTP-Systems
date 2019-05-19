
// TFTPClient.java
// This class is the client side for a very simple assignment based on TFTP on
// UDP/IP. The client uses one port and sends a read or write request and gets 
// the appropriate response from the server.  No actual file transfer takes place.   

import java.util.Scanner;
import java.io.*;
import java.net.*;

public class Client {

	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;

	// we can run in normal (send directly to server) or test
	// (send to simulator) mode
	public static enum Mode {
		NORMAL, TEST
	};

	public Client() {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) { // Can't create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void sendAndReceive() {

		// Scanner object used to receive input
		Scanner scan = new Scanner(System.in);
		String s;

		boolean verbose = false, running = true;

		byte[] msg = new byte[100], // message we send
				fn, // filename as an array of bytes
				md, // mode as an array of bytes
				data; // reply as array of bytes
		String mode; // filename and mode as Strings
		int j, len, sendPort;

		File directory;

		System.out.println("Type in the filepath for the directory used");
		s = scan.next();
		directory = new File(s);

		while ((!directory.exists()) && (!directory.isDirectory())) {
			System.out.println("Invalid path or not a directory");
			System.out.println("Type in the filepath for the directory used");
			s = scan.next();
			directory = new File(s);
		}

		// System.out.println(directory.getPath());
		File[] contents = directory.listFiles();

		// In the assignment, students are told to send to 23, so just:
		// sendPort = 23;
		// is needed.
		// However, in the project, the following will be useful, except
		// that test vs. normal will be entered by the user.
		Mode run = Mode.TEST; // change to NORMAL to send directly to server

		while (running) {
			System.out.println("enter a v for Verbose mode or a q for quiet mode: ");
			s = scan.next();

			if (s.compareToIgnoreCase("v") == 0) {
				verbose = true;
			}

			System.out.println("enter a n for normal mode or a t for test mode: ");
			s = scan.next();

			if (s.compareToIgnoreCase("n") == 0) {
				run = Mode.NORMAL;
			}

			if (run == Mode.NORMAL)
				sendPort = 69;
			else
				sendPort = 23;

			System.out.println("enter a y to display the directory or anything else not to: ");
			s = scan.next();

			if (s.compareToIgnoreCase("y") == 0) {
				System.out.println("Directory contains");
				for (File object : contents) {
					if (object.isDirectory()) {
						System.out.println("Directory name: " + object.getName());
					}
					if (object.isFile()) {
						System.out.println("File name: " + object.getName());
					}
				}
			}

			System.out.println("enter a 1 for a read request or a 2 for a write request: ");
			s = scan.next();

			if (s.compareTo("1") == 0) {
				msg[1] = 1;
			} else if (s.compareTo("2") == 0) {
				msg[1] = 2;
			}

			while ((s.compareTo("1") != 0) && (s.compareTo("2") != 0)) {
				System.out.println("enter a 1 for a read request or a 2 for a write request: ");
				s = scan.next();

				if (s.compareTo("1") == 0) {
					msg[1] = 1;
				} else if (s.compareTo("2") == 0) {
					msg[1] = 2;
				}
			}

			if (s.compareTo("1") == 0) {
				System.out.println("Forming a RRQ connection");
				System.out.println("Client: creating packet . . .");

				System.out.println("Type in the filename: ");
				s = scan.next();

				// Prepare a DatagramPacket and send it via sendReceiveSocket
				// to sendPort on the destination host (also on this machine).

				msg[0] = 0;

				// extract filename and convert it to bytes
				fn = s.getBytes();
				// copy it into the msg
				System.arraycopy(fn, 0, msg, 2, fn.length);
				// insert 0 after filename
				msg[fn.length + 2] = 0;

				// now add "octet" (or "netascii")
				mode = "octet";
				// convert to bytes
				md = mode.getBytes();

				// and copy into the msg
				System.arraycopy(md, 0, msg, fn.length + 3, md.length);

				// length of the message
				len = fn.length + md.length + 4;
				// length of filename + length of mode + opcode (2) + two 0s (2)
				// second 0 to be added next:

				// end with another 0 byte
				msg[len - 1] = 0;

				// Construct a datagram packet that is to be sent to a specified port
				// on a specified host.
				try {
					sendPacket = new DatagramPacket(msg, len, InetAddress.getLocalHost(), sendPort);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.exit(1);
				}

				System.out.println("Client: packet created");
				System.out.println("Client: sending packet . . .");

				if (verbose == true) {
					System.out.println("To host: " + sendPacket.getAddress());
					System.out.println("Destination host port: " + sendPacket.getPort());
					len = sendPacket.getLength();
					System.out.println("Length: " + len);
					System.out.println("Containing: ");
					for (j = 0; j < len; j++) {
						System.out.println("byte " + j + " " + msg[j]);
					}

					// Form a String from the byte array, and print the string.
					String sending = new String(msg, 0, len);
					System.out.println(sending);
				}

				// Send the datagram packet to the server via the send/receive socket.

				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

				System.out.println("Client: Packet sent.");

				int size = 512;
				byte[] receivingArray = new byte[65535 * 512];
				int blockNum = 1;

				while (size == 512) {
					data = new byte[516];
					receivePacket = new DatagramPacket(data, data.length);

					System.out.println("Client: Waiting for packet. . .");
					try {
						// Block until a datagram is received via sendReceiveSocket.
						sendReceiveSocket.receive(receivePacket);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}

					// Process the received datagram.
					System.out.println("Client: Packet received:");
					if (verbose == true) {
						System.out.println("From host: " + receivePacket.getAddress());
						System.out.println("Host port: " + receivePacket.getPort());
						len = receivePacket.getLength();
						System.out.println("Length: " + len);
					}
					System.out.println("Containing: ");
					for (j = 0; j < len; j++) {
						System.out.println("byte " + j + " " + data[j]);
					}
					size = receivePacket.getLength()-4;
					System.arraycopy(data, 4, receivingArray, (blockNum-1)*512, size);

					System.out.println("Creating packet . . .");

					msg = new byte[4];
					msg[0] = 0;
					msg[1] = 4;
					msg[2] = (byte) (blockNum);
					msg[3] = (byte) (blockNum >>> 8);

					len = msg.length;
					blockNum++;

					try {
						sendPacket = new DatagramPacket(msg, len, InetAddress.getLocalHost(), sendPort);
					} catch (UnknownHostException e) {
						e.printStackTrace();
						System.exit(1);
					}

					System.out.println("Packet created");
					System.out.println("Sending packet . . .");
					if (verbose == true) {
						System.out.println("To host: " + sendPacket.getAddress());
						System.out.println("Destination host port: " + sendPacket.getPort());
						len = sendPacket.getLength();
						System.out.println("Length: " + len);
						System.out.println("Containing: ");
						for (j = 0; j < len; j++) {
							System.out.println("byte " + j + " " + msg[j]);
						}

						// Form a String from the byte array, and print the string.
						String sending = new String(msg, 0, len);
						System.out.println(sending);
					}

					// Send the datagram packet to the server via the send/receive socket.

					try {
						sendReceiveSocket.send(sendPacket);
					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}

				}

				String Filepath = "C:\\TestStuff\\";
				File file = new File(Filepath);
				try {
					OutputStream os = new FileOutputStream(file);
					os.write(receivingArray);
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}



			} else if (s.compareTo("2") == 0) {
				System.out.println("Forming a WRQ connection");
				System.out.println("Client: creating packet . . .");

				System.out.println("Type in the filename: ");
				s = scan.next();

				File sendingFile = new File(directory.getPath() + "\\" + s);

				while (!sendingFile.exists()) {
					System.out.println("Type in the filename: ");
					s = scan.next();
					sendingFile = new File(directory.getPath() + "\\" + s);
				}

				// Prepare a DatagramPacket and send it via sendReceiveSocket
				// to sendPort on the destination host (also on this machine).

				msg[0] = 0;

				// extract filename and convert it to bytes
				fn = sendingFile.getName().getBytes();
				// copy it into the msg
				System.arraycopy(fn, 0, msg, 2, fn.length);
				// insert 0 after filename
				msg[fn.length + 2] = 0;

				// now add "octet" (or "netascii")
				mode = "octet";
				// convert to bytes
				md = mode.getBytes();

				// and copy into the msg
				System.arraycopy(md, 0, msg, fn.length + 3, md.length);

				// length of the message
				len = fn.length + md.length + 4;
				// length of filename + length of mode + opcode (2) + two 0s (2)
				// second 0 to be added next:

				// end with another 0 byte
				msg[len - 1] = 0;

				// Construct a datagram packet that is to be sent to a specified port
				// on a specified host.
				try {
					sendPacket = new DatagramPacket(msg, len, InetAddress.getLocalHost(), sendPort);
				} catch (UnknownHostException e) {
					e.printStackTrace();
					System.exit(1);
				}

				System.out.println("Client: packet created");
				System.out.println("Client: sending packet . . .");

				if (verbose == true) {
					System.out.println("To host: " + sendPacket.getAddress());
					System.out.println("Destination host port: " + sendPacket.getPort());
					len = sendPacket.getLength();
					System.out.println("Length: " + len);
					System.out.println("Containing: ");
					for (j = 0; j < len; j++) {
						System.out.println("byte " + j + " " + msg[j]);
					}

					// Form a String from the byte array, and print the string.
					String sending = new String(msg, 0, len);
					System.out.println(sending);
				}

				// Send the datagram packet to the server via the send/receive socket.

				try {
					sendReceiveSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

				System.out.println("Client: Packet sent.");

				// Construct a DatagramPacket for receiving packets up
				// to 100 bytes long (the length of the byte array).

				data = new byte[100];
				receivePacket = new DatagramPacket(data, data.length);

				System.out.println("Client: Waiting for packet. . .");
				try {
					// Block until a datagram is received via sendReceiveSocket.
					sendReceiveSocket.receive(receivePacket);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}

				// Process the received datagram.
				System.out.println("Client: Packet received:");

				if (verbose == true) {
					System.out.println("From host: " + receivePacket.getAddress());
					System.out.println("Host port: " + receivePacket.getPort());
					len = receivePacket.getLength();
					System.out.println("Length: " + len);
				}
				System.out.println("Containing: ");
				for (j = 0; j < len; j++) {
					System.out.println("byte " + j + " " + data[j]);
				}

				long size = sendingFile.length();
				int blockNum = 1;
				FileInputStream fis = null;
				byte[] fileArray = new byte[(int) sendingFile.length()];
				try {
					fis = new FileInputStream(sendingFile);
					fis.read(fileArray);
					fis.close();
				} catch (IOException ioExp) {
					ioExp.printStackTrace();
					System.exit(1);
				}

				while (size > -1) {
					if (blockNum > 65535) {
						System.out.println("The file is too large to send");
						size = -1;
					} else {
						System.out.println("Client: creating packet . . .");

						msg = new byte[516];
						msg[0] = 0;
						msg[1] = 3;
						msg[2] = (byte) blockNum;
						msg[3] = (byte) (blockNum >>> 8);

						if (size > 512) {
							System.arraycopy(fileArray, 0, msg, 4, 512);
							System.arraycopy(fileArray, blockNum * 512, fileArray, 0, 512);
							len = 516;
							size = size - 512;
						} else {
							System.arraycopy(fileArray, 0, msg, 4, (int) size);
							System.arraycopy(fileArray, blockNum * 512, fileArray, 0, (int) size);
							len = 4 + (int) size;
							size = -1;
						}
						blockNum++;

						try {
							sendPacket = new DatagramPacket(msg, len, InetAddress.getLocalHost(), sendPort);
						} catch (UnknownHostException e) {
							e.printStackTrace();
							System.exit(1);
						}

						System.out.println("Client: packet created");
						System.out.println("Client: sending packet . . .");

						if (verbose == true) {
							System.out.println("To host: " + sendPacket.getAddress());
							System.out.println("Destination host port: " + sendPacket.getPort());
							len = sendPacket.getLength();
							System.out.println("Length: " + len);
							System.out.println("Containing: ");
							for (j = 0; j < len; j++) {
								System.out.println("byte " + j + " " + msg[j]);
							}

							// Form a String from the byte array, and print the string.
							String sending = new String(msg, 0, len);
							System.out.println(sending);
						}

						// Send the datagram packet to the server via the send/receive socket.

						try {
							sendReceiveSocket.send(sendPacket);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						}

						System.out.println("Client: Packet sent.");

						// Construct a DatagramPacket for receiving packets up
						// to 100 bytes long (the length of the byte array).

						data = new byte[100];
						receivePacket = new DatagramPacket(data, data.length);

						System.out.println("Client: Waiting for packet. . .");
						try {
							// Block until a datagram is received via sendReceiveSocket.
							sendReceiveSocket.receive(receivePacket);
						} catch (IOException e) {
							e.printStackTrace();
							System.exit(1);
						}

						// Process the received datagram.
						System.out.println("Client: Packet received:");

						if (verbose == true) {
							System.out.println("From host: " + receivePacket.getAddress());
							System.out.println("Host port: " + receivePacket.getPort());
							len = receivePacket.getLength();
							System.out.println("Length: " + len);
						}
						System.out.println("Containing: ");
						for (j = 0; j < len; j++) {
							System.out.println("byte " + j + " " + data[j]);
						}
					}
				}

			}

			System.out.println("Enter y to shut down or anything else to continue: ");
			s = scan.next();

			if (s.compareToIgnoreCase("y") == 0) {
				running = false;
			}

			System.out.println();
		}

		// close scanner
		scan.close();

		// We're finished, so close the socket.
		sendReceiveSocket.close();
	}

	public static void main(String args[]) {
		Client c = new Client();
		c.sendAndReceive();
	}
}
