package com.mrsmyx.utils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.*;

public class Network {

	public static List<InetAddress> scanSubNets() {
		List<InetAddress> addresses = new ArrayList<InetAddress>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface ni = networkInterfaces.nextElement();
				Enumeration<InetAddress> nias = ni.getInetAddresses();
				while (nias.hasMoreElements()) {
					InetAddress ia = nias.nextElement();
					if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress() && ia instanceof Inet4Address) {
						addresses.add(ia);
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return addresses;
	}

	public static String getReachableHosts(List<InetAddress> netAddress) throws SocketException {
		
		for (InetAddress inetAddress : netAddress) {
			String ipAddress = inetAddress.toString();
			ipAddress = ipAddress.substring(1, ipAddress.lastIndexOf('.')) + ".";
			
			for (int i = 0; i < 256; i++) {
				String otherAddress = ipAddress + String.valueOf(i);
				try {
					if (InetAddress.getByName(otherAddress.toString()).isReachable(50)) {
						System.out.println(otherAddress);
						try {
							if (reachable(InetAddress.getByName(otherAddress.toString()).getHostName())) {
								return InetAddress.getByName(otherAddress.toString()).getHostName();
							}
						} catch (Exception e) {
							continue;
						}
					}
				} catch (UnknownHostException e) {
					continue;
				} catch (IOException e) {
					continue;
				}
			}
		}
		return null;
	}

	private static boolean reachable(String hostName) {
		SocketAddress sockAddr = new InetSocketAddress(hostName, 7887);
		Socket sock = new Socket();
		try {
			sock.connect(sockAddr, 300);
			sock.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static InetAddress getWLANipAddress(String protocolVersion) throws SocketException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
		for (NetworkInterface netint : Collections.list(nets)) {
			if (netint.isUp() && !netint.isLoopback() && !netint.isVirtual()) {
				Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
				for (InetAddress inetAddress : Collections.list(inetAddresses)) {
					if (protocolVersion.equals("IPv4")) {
						if (inetAddress instanceof Inet4Address) {
							return inetAddress;
						}
					} else {
						if (inetAddress instanceof Inet6Address) {
							return inetAddress;
						}
					}
				}
			}
		}
		return null;
	}

}
