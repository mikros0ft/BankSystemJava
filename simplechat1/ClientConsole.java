// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;

import client.*;
import common.*;

/**
 * This class constructs the UI for a chat client.  It implements the
 * chat interface in order to activate the display() method.
 * Warning: Some of the code here is cloned in ServerConsole 
 *
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Dr Timothy C. Lethbridge  
 * @author Dr Robert Lagani&egrave;re
 * @version July 2000
 */
public class ClientConsole implements ChatIF 
{
  //Class variables *************************************************
  
  /**
   * The default port to connect on.
   */
  final public static int DEFAULT_PORT = 5555;
  
  //Instance variables **********************************************
  
  /**
   * The instance of the client that created this ConsoleChat.
   */
  ChatClient client;

  
  //Constructors ****************************************************

  /**
   * Constructs an instance of the ClientConsole UI.
   *
   * @param host The host to connect to.
   * @param port The port to connect on.
   */
  public ClientConsole(String loginId, String host, int port) 
  {
	if (loginId == null){
		System.out.println("No login id entered");
		System.exit(1);
	}
    try 
    {
      client= new ChatClient(loginId, host, port, this);
      client.handleMessageFromClientUI("#login " + loginId);
    } 
    catch(IOException exception) 
    {
      System.out.println("Error: Can't setup connection!"
                + " Terminating client.");
      System.exit(1);
    }
  }

  
  //Instance methods ************************************************
  
  /**
   * This method waits for input from the console.  Once it is 
   * received, it sends it to the client's message handler.
   */
  public void accept() 
  {
    try
    {
      BufferedReader fromConsole = 
        new BufferedReader(new InputStreamReader(System.in));
      String message;

      while (true) 
      {
        message = fromConsole.readLine();
        // **** START Change for E50 - HK
        // If message starts with a #, a command is being entered on the client side.
        if (message.startsWith("#")){
        	// If message equals #quit, disconnect the client from the server
        	// and quit client gracefully.
        	if (message.equals("#quit")){
        		client.closeConnection();
        		client.quit();
        	}
        	// If message equals #logoff, disconnect the client from the server.
        	// If client is already logged off, display message saying so.
        	else if(message.equals("#logoff")){
        		if (client.isConnected()){
        			client.closeConnection();
        		}
        		else{
        			System.out.println("Client is currently logged off");
        		}
        	}
        	// If message equals #sethost, the setHost method in the client is called to change the host.
        	// Command only allowed if client is logged off, otherwise prints error message
        	else if (message.contains("#sethost")){
        		if (!client.isConnected()){
        			String[] words = message.split(" ");
        			client.setHost(words[1]);
        		}
        		else{
        			System.out.println("You cannot set a host because you are currently logged on");
        		}
        	}
        	// If message equals #setport, the setPort method in the client is called to change the port.
        	// Command only allowed if client is logged off, otherwise prints error message.
        	else if (message.contains("#setport")){
        		if (!client.isConnected()){
        			String[] words = message.split(" ");
        			client.setPort(Integer.parseInt(words[1]));
        		}
        		else{
        			System.out.println("You cannot set a port because you are currently logged on");
        		}
        	}
        	// If message equals #login, establish a connection between the client and the server.
        	// If client is already logged in, display an error message.
        	else if (message.contains("#login")){
        		if (!client.isConnected()){
        			client.openConnection();
        			String[] words = message.split(" ");
        			client.handleMessageFromClientUI("#login " + words[1]);
        		}
        		else{
        			System.out.println("You are already connected to the server");
        		}
        	}
        	// If message equals #gethost, display the current host name.
        	else if (message.equals("#gethost")){
        		System.out.println("Host: "+client.getHost());
        	}
        	// If message equals #getportt, display the current port number.
        	else if (message.equals("#getport")){
        		System.out.println("Port: "+client.getPort());
        	}
        	// If message starts with # but is not one of the above commands, 
        	// display an error message.
        	else{
        		System.out.println(message+" is not a valid command");
        	}
        	
        }
        // If message does not start with #, send the message to the client.
        if (!message.startsWith("#"))
        	client.handleMessageFromClientUI(message);
        // **** END Change for E50 - HK
      }
    } 
    catch (Exception ex) 
    {
      System.out.println
        ("Unexpected error while reading from console!");
    }
  }

  /**
   * This method overrides the method in the ChatIF interface.  It
   * displays a message onto the screen.
   *
   * @param message The string to be displayed.
   */
  public void display(String message) 
  {
    System.out.println(message);
  }
  
  

  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of the Client UI.
   *
   * @param args[0] The login that will be used to connect to server.
   * @param args[1] The host to connect to.
   * @param args[2] The port to connect to.
   */
  public static void main(String[] args)
  {
	String loginId = null;
    String host = "";
    int port = DEFAULT_PORT;  //The port number

    try
    {
      // Get loginId from command line.
      loginId = args[0];
      // Get host from command line. If none entered, default will be used.
      host = args[1];
      // Get port from command line. If none entered, default will be used.
      port = Integer.parseInt(args[2]);
    }
    catch(ArrayIndexOutOfBoundsException e)
    {
      // Host used if not entered on the command line.
      host = "localhost";
    }
    ClientConsole chat= new ClientConsole(loginId, host, port);
    chat.accept();
}
}
//End of ConsoleChat class
