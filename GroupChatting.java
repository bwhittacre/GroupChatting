import java.net.*;
import java.io.*;
import java.util.*;
public class GroupChatting
{
    //This is the list of variables that are present throughout the 
    //file. They act as a set of controls for the system as a whole.
    private static final String TERMIMATE = "Exit";
    static String name;
    static volatile boolean finished = false;
    public static void main(String[] args)
    {
        //This is a pre-cautionary error message in the case that you
        //try and boot the program without stating the ipaddress or 
        //the port of the host.
        if (args.length != 2)
            System.out.println("You need both the host and the port number");
        else 
        {
            try
            {
                //This section of the code checks the arguments that are 
                //requested upon running the program, and uses that info 
                //to determine what the address and port of the host of the
                //group chat are. It also casts to the socket based on the 
                //address and port, asks for the name of the user, and 
                //states that the user may begin typing.
                InetAddress group = InetAddress.getByName(args[0]);
                int port = Integer.parseInt(args[1]);
                Scanner sc = new Scanner(System.in);
                System.out.print("Who is you?: ");
                name = sc.nextLine();
                MulticastSocket socket = new MulticastSocket(port);
                socket.setTimeToLive(0);
                socket.joinGroup(group);
                Thread t = new Thread(new ReadThread(socket,group,port));
                t.start();
                System.out.println("You can start typing now\n");
                while(true)
                {
                    //This section of the code contains the command to 
                    //terminate the socket, by way of the keyword stated
                    //at the top of the file.
                    String message;
                    message = sc.nextLine();
                    if(message.equalsIgnoreCase(GroupChatting.TERMIMATE))
                    {
                        finished = true;
                        socket.leaveGroup(group);
                        socket.close();
                        break;
                    }
                    //Shows the message from other people, with their 
                    //associated name next to the message. This is done
                    //through the datagram module.
                    message = name + ": " + message;
                    byte[] buffer = message.getBytes();
                    DatagramPacket datagram = new DatagramPacket(buffer,buffer.length,group,port);
                    socket.send(datagram);
                }
                //This section of the code contains error messages for the 
                //cases that the socket cannot be created, or the messages
                //aren't transmitting to or from the socket.
            }
            catch(SocketException se)
            {
                System.out.println("Socket error detected: Creating a socket");
                se.printStackTrace();
            }
            catch(IOException ie)
            {
                System.out.println("Having trouble reading and writing to socket :/");
                ie.printStackTrace();
            }
        }   
    }
}
//This section of the code reads the information of the address
//and ports and keeps them immutable. It also adds a character
//limit to the texts.
class ReadThread implements Runnable
{
    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private static final int MAX_LEN = 1000;
    ReadThread(MulticastSocket socket,InetAddress group, int port)
    {
        this.socket = socket;
        this.group = group;
        this.port = port;
    }
    //This section of the program receives messages from 
    //the other members of the chat, and prints them out
    //in the other member's displays.
    @Override
    public void run()
    {
        while(!GroupChatting.finished)
        {
            byte[] buffer = new byte[ReadThread.MAX_LEN];
            DatagramPacket datagram = new DatagramPacket(buffer,buffer.length,group,port);
            String message;
            try
            {
                socket.receive(datagram);
                message = new String(buffer,0,datagram.getLength(),"UTF-8");
                if(!message.startsWith(GroupChatting.name))System.out.println(message);
            }
            //This is the closing message from the system when the
            //user types out "exit".
            catch(IOException e)
            {
                System.out.println("Socket closing my guy");
            }
        }
    }
}