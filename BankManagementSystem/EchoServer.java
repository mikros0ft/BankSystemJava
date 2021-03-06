// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

import java.io.*;
import java.util.ArrayList;

import ocsf.server.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 * @version July 2000
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;
  private static ArrayList<Person> people;
  private static Person currentPerson;
  private static Account jacksCheckingTest;
  private static Account bobsSavingsTest;
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) 
  {
    super(port);
  }

  public ServerConsole serverConsole;
  
  //Instance methods ************************************************
  
  /**
   * Modified for E51 -NS
   * 
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
  {
	  // This had to be modified to deal with user logins -NS
	  String clientLoggedIn = null;
	  String message = msg.toString();
	  try
	  {
		  //An attempt is made to see if the client has logged in yet -NS
		  clientLoggedIn = client.getInfo("clientLoggedIn").toString();

		  //If the client has logged in, but tries the login, an error should be displayed -NS
		  if(clientLoggedIn.matches("true") && message.startsWith("#login"))
		  {
			  client.sendToClient("SERVER MSG> Error: You have already logged in");
			  serverConsole.display("Message received: " + msg + " from " + client);
			  return;
		  }

	  }
	  catch(NullPointerException ex) //an exception is thrown if the user hasn't logged in yet -NS
	  {
		  if(message.startsWith("#login")) //If the message is the login command with an id, they will be logged in
		  {
			  String[] words = ((String) msg).split(" ");
			  String email = words[1];
			  String password = words[2];
			  for (Person p : people){
				  if (p.getEmail().equals(email) && p.getPassword().equals(password)){
					  client.setInfo("login id", email);
					  client.setInfo("clientLoggedIn", "true");
					  currentPerson = p;
					  
					  try {
						client.sendToClient("SERVER MSG> " + email + " has successfully logged in");
					} catch (IOException e) {
						System.out.println("SERVER MSG> IO Exception");
					}
					  return;
				  }
			  }
			  try {
				client.sendToClient("SERVER MSG> Error: Invalid email or password entered");
				client.close();
			} catch (IOException e) {
				System.out.println("SERVER MSG> IO Exception");
			}
			  return;
		  }
		  else
		  {
			  //Client must login with their first communication
			  try {
				client.sendToClient("SERVER MSG> Error: You must log in");
				client.close();
			} catch (IOException e) {
				System.out.println("SERVER MSG> IO Exception");
			}
			  serverConsole.display("Message received: " + msg + " from " + client);
			  return;
		  }
	  } catch (IOException e) {
		  System.out.println("SERVER MSG> IO Exception");
	  }
	  
	  if (message.startsWith("#dotest")){
		  if (currentPerson.getEmail().equals("test@gmail.com"))
			  doTest(client);
		else
			try {
				client.sendToClient("SERVER MSG> You will need to use the <#login test@gmail.com test> command before using #dotest");
			} catch (IOException e) {
				System.out.println("SERVER MSG> IO Exception");
			}
	  }
	  else if(message.startsWith("#displayaccounts")){
		  ArrayList<Account> accounts = currentPerson.getAccounts();
		  for (Account account : accounts){
			  try {
				client.sendToClient(account+"");
			} catch (IOException e) {
				System.out.println("SERVER MSG> IO Exception");
			}
		  }
	  }
	  else if (message.startsWith("#displaytransactions")){
		  ArrayList<Account> accounts = currentPerson.getAccounts();
		  Account transAccount = null;
		  String[] words = message.split(" ");
		  for (Account account : accounts){
			  if(account.accountId()==Integer.parseInt(words[1]))
				  transAccount = account;
		  }
		  if (transAccount==null){
			  try {
				client.sendToClient("SERVER MSG> Either an account with that account id does not exist or you do not own that account");
			} catch (IOException e) {
				System.out.println("SERVER MSG> IO Exception");
			}
		  }
		  else{
			  for (Transaction trans : transAccount.accountTransactions()){
				  try {
					client.sendToClient(trans.toString()+"");
				} catch (IOException e) {
					System.out.println("SERVER MSG> IO Exception");
				}
			  }
		  }
		  
	  }
	  else if(message.startsWith("#deposit")){
		  String[] words = message.split(" ");
		  Account depositAccount = null;
		  double amount = Double.parseDouble(words[2]);
		  ArrayList<Account> accounts = currentPerson.getAccounts();
		  for (Account account : accounts){
			  if(account.accountId()==Integer.parseInt(words[1]))
				  depositAccount = account;
		  }
		  if (depositAccount==null){
			  try {
				client.sendToClient("SERVER MSG> Either an account with that account id does not exist or you do not own that account");
			} catch (IOException e) {
				System.out.println("IO Exception");
			}
		  }
		  else{
			  if (amount<0){
				  try {
					client.sendToClient("SERVER MSG> You cannot enter a negative amount");
				} catch (IOException e) {
					System.out.println("SERVER MSG> IO Exception");
				}
			  }
			  else{
				  depositAccount.deposit(amount);
				  try {
					client.sendToClient("SERVER MSG> Deposit successful. Your new account balance is " + depositAccount.accountBalance());
				} catch (IOException e) {
					System.out.println("SERVER MSG> IO Exception");
				}
			  }
		  }
		  
	  }
	  else if(message.startsWith("#withdraw")){
		  String[] words = message.split(" ");
		  Account withdrawlAccount = null;
		  double amount = Double.parseDouble(words[2]);
		  ArrayList<Account> accounts = currentPerson.getAccounts();
		  for (Account account : accounts){
			  if(account.accountId()==Integer.parseInt(words[1]))
				  withdrawlAccount = account;
		  }
		  if (withdrawlAccount==null){
			  try {
				client.sendToClient("SERVER MSG> Either an account with that account id does not exist or you do not own that account");
			} catch (IOException e) {
				System.out.println("SERVER MSG> IO Exception");
			}
		  }
		  else{
			  if (amount<0){
				  try {
					client.sendToClient("SERVER MSG> You cannot enter a negative amount");
				} catch (IOException e) {
					System.out.println("SERVER MSG> IO Exception");
				}
			  }
			  else{
				  boolean success = withdrawlAccount.withdraw(amount);
				  if (!success){
					  try {
						client.sendToClient("SERVER MSG> Withdraw unsuccessful! Insufficient Funds.");
					} catch (IOException e) {
						System.out.println("SERVER MSG> IO Exception");
					}
				  }
				  else{
					  try {
							client.sendToClient("SERVER MSG> Withdrawl successful. Your new account balance is " + withdrawlAccount.accountBalance());
						} catch (IOException e) {
							System.out.println("SERVER MSG> IO Exception");
						}
				  }
			  }
		  }
		  
	  }
	  else if(message.startsWith("#transfer")){
		  String[] words = message.split(" ");
		  Account yourAccount = null;
		  Account transferToAccount = null;
		  double amount;
		  amount = Double.parseDouble(words[3]);
		  ArrayList<Account> accounts = new ArrayList<Account>();
		  for (Person p : people){
			  for (Account account : p.getAccounts()){
				  accounts.add(account);
			  }
		  }
		  for (Account account : accounts){
			  if(account.accountId()==Integer.parseInt(words[1]))
				  yourAccount = account;
			  else if (account.accountId()==Integer.parseInt(words[2]))
				  transferToAccount = account;
				  
		  }
		  if (yourAccount == null){
			  try {
				client.sendToClient("SERVER MSG> Either an account with that account id does not exist or you do not own that account");
			} catch (IOException e) {
				System.out.println("SERVER MSG> IO Exception");
			}
		  }
		  else if (transferToAccount == null){
			  try {
				client.sendToClient("SERVER MSG> The transfer to account with that account id does not exist");
			} catch (IOException e) {
				System.out.println("SERVER MSG> IO Exception");
			}
		  }
		  else{
			  if (amount<0){
				  try {
					client.sendToClient("SERVER MSG> You cannot enter a negative amount");
				} catch (IOException e) {
					System.out.println("SERVER MSG> IO Exception");
				}
			  }
			  else{
				  boolean success = yourAccount.transfer(transferToAccount, amount);
				  if (!success){
					  try {
						client.sendToClient("SERVER MSG> Transfer unsuccessful! Insufficient funds.");
					} catch (IOException e) {
						System.out.println("SERVER MSG> IO Exception");
					}
				  }
				  else{
					  try {
							client.sendToClient("SERVER MSG> Transfer successful. Your new account balance is " + yourAccount.accountBalance());
						} catch (IOException e) {
							System.out.println("SERVER MSG> IO Exception");
						}
				  }
			  }
		  }
		  
	  }
	  
	  String login = client.getInfo("login id").toString();
	  serverConsole.display("Message received: " + msg + " from " + login);
	  this.sendToAllClients(login + "> " + msg);	  

  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    serverConsole.display("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
	  serverConsole.display("Server has stopped listening for connections.");
  }
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void main(String[] args) 
  {
	ArrayList<AccountType> accountTypes = new ArrayList<AccountType>();
	people = new ArrayList<Person>();
	AccountType checking = new AccountType("Checking", 1.5);
	accountTypes.add(checking);
	AccountType savings = new AccountType("Savings", 2.5);
	accountTypes.add(savings);
	Person p1 = new Person("Jack", "1234", "jack@gmail.com");
	people.add(p1);
	Person p2 = new Person("Bob", "5678", "bob@gmail.com");
	people.add(p2);
	Person p3 = new Person ("Test Account", "test", "test@gmail.com");
	people.add(p3);
	jacksCheckingTest = new Account(100.50, "Jack's Checking Account", checking, p1);
	bobsSavingsTest = new Account(500.21, "Bob's Savings Account", savings, p2);
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    EchoServer sv = new EchoServer(port);
    ServerConsole sc = new ServerConsole(sv); // An instance of ServerConsole is created here.  
    sv.serverConsole = sc;                    //It will handle all UI operations -NS
    
    try 
    {
      sv.listen(); //Start listening for connections
      sc.accept(); //Start the server console -NS
    } 
    catch (Exception ex) 
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
    
  }
    
  /**
   * Added for E49 -NS
   * This added method overrides the clientConnected method from abstract server
   * It prints a message whenever a client connects -NS
  */
  protected void clientConnected(ConnectionToClient client) 
  {
	  serverConsole.display("A client has connected");
	  
  }
  
  public void doTest(ConnectionToClient client){
	  serverConsole.display("Initiating test");
	  try {
		client.sendToClient("Initiating test");
	} catch (IOException e) {
		System.out.println("SERVER MSG> IO Exception");
	}
	  currentPerson = people.get(0);
	  ArrayList<Account> accounts = people.get(0).getAccounts();
	  for (Account account : accounts){
		  try {
			client.sendToClient(account+"");
		} catch (IOException e) {
			System.out.println("SERVER MSG> IO Exception");
		}
	  }
	  handleMessageFromClient("#withdraw " + jacksCheckingTest.accountId() + " 50", client);
	  handleMessageFromClient("#deposit " + jacksCheckingTest.accountId() + " 50", client);
	  handleMessageFromClient("#transfer " + jacksCheckingTest.accountId() + " " + bobsSavingsTest.accountId() + " 50", client);
	  handleMessageFromClient("#displaytransactions " + jacksCheckingTest.accountId(), client);
	  currentPerson = people.get(1);
	  ArrayList<Account> accounts2 = people.get(1).getAccounts();
	  for (Account account : accounts2){
		  try {
			client.sendToClient(account+"");
		} catch (IOException e) {
			System.out.println("SERVER MSG> IO Exception");
		}
	  }
	  handleMessageFromClient("#displaytransactions " + bobsSavingsTest.accountId(), client);
	  serverConsole.display("Test finished successfully");
	  try {
		client.sendToClient("Test finished successfully");
	} catch (IOException e) {
		System.out.println("SERVER MSG> IO Exception");
	}
	  currentPerson = people.get(3);
  }
    
  /*
   * Added for E49 -NS
   * This added method overrides the clientDisonnected method from abstract server
   * It prints a message whenever a client disconnects by action of the server -NS
   */
  synchronized protected void clientDisconnected(ConnectionToClient client)
  {
	  String login = client.getInfo("login id").toString();	  
	  serverConsole.display(login + " has disconnected");
  }  
  
  /*
   * Added for E49 -NS
   * This added method overrides the clientException method from abstract server
   *It prints a message whenever a client disconnects -NS
   */
  synchronized protected void clientException(ConnectionToClient client, Throwable exception)
  {
	  String login = client.getInfo("login id").toString();
	  serverConsole.display(login + " has disconnected");
  }
 
  /*
   * Added for E50 (c) -NS
   * 
   * It was placed in this class since it should be separate from UI functions
   * this method handles the various commands that a user
   * can use by typing a command prefixed with "#"
   */
  public void performCommand(String comm) 
  {
	  if(comm.matches("quit"))
	  {
		  System.exit(0);
	  }
	  else if(comm.matches("stop"))
	  {
		  this.stopListening();
		  this.sendToAllClients("SERVER MSG> Server has stopped listening for connections.");
	  }
	  else if(comm.matches("close"))
	  {
		  this.sendToAllClients("SERVER MSG> Server is shutting down.");
		  this.stopListening();
		  try {
			  this.close();
		  } catch (IOException e) {

		  }
	  }
	  else if(comm.startsWith("setport "))
	  {			  
		  try {
			  int newPort = Integer.parseInt(comm.substring(8));
			  this.setPort(newPort);
			  serverConsole.display("Port set to: " + comm.substring(8));
		  } catch (Exception e)
		  {
			  serverConsole.display(comm.substring(8) + " is not a port number");
		  }
	  }
	  else if(comm.matches("start"))
	  {			  
		  try {
			  this.listen();
		  } catch (IOException e) {
		  }			 
	  }
	  else if(comm.matches("getport"))
	  {
		  serverConsole.display(Integer.toString(this.getPort()));
	  }
	  else
	  {
		  serverConsole.display(comm + " is not a valid command");
	  }
  }
	
}
//End of EchoServer class
