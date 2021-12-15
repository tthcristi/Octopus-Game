package networking;

import java.net.InetAddress;
import java.net.SocketException;

import map.Map;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayDeque;
import java.util.Queue;

public class Server extends Thread
{
	
	private DatagramSocket socket;
	public InetAddress ipAddress;
	public int port;
	
	public Server()
	{
		try {
			
			this.socket = new DatagramSocket(1331);
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		
		try {
			socket.receive(packet);
			ipAddress = packet.getAddress();
			port = packet.getPort();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		ProjectilePacket proj = new ProjectilePacket();
		
		while(true)
		{
			
			try {
				synchronized(Map.lastSentPlayerPacket)
				{					
					packet.setData(Map.lastSentPlayerPacket.toBytes());
				}
				socket.send(packet);
	
				synchronized(Map.lastSentProjectilePacket)
				{					
					packet.setData(Map.lastSentProjectilePacket.toBytes());
					Map.lastSentProjectilePacket.speed = 0;
				}
				socket.send(packet);
	
				socket.receive(packet);
				synchronized(Map.lastReceivedPlayerPacket)
				{
					Map.lastReceivedPlayerPacket.readFromBytes(packet.getData());
				}
				socket.receive(packet);
				
				proj.readFromBytes(packet.getData());
				if(proj.speed != 0)
					synchronized(Map.projQueue)
					{
						ProjectilePacket p = new ProjectilePacket();
						p.readFromBytes(proj.toBytes());
						Map.projQueue.add(p);
					}
				
					
			}catch (IOException e)
			{
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}			
		}
	}
	
	
	public void sendData(byte[] data, InetAddress ipAddress, int port)
	{
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
