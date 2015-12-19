package ac.lemberg.kobi.view;



/**
 * @author Kobi Lemberg
 * @version 1.0
 * <h1> CLI </h1>
 * CLI class goal is to handle user command line requests. 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import ac.lemberg.kobi.presenter.Command;



public class CLI implements Runnable
{
BufferedReader in;
PrintWriter out;
HashMap<String,Command> commands;
HashSet<String> commandsStrings;
String cliMenu;
//Constructors
	/**
	 * Instantiates a new  my own CLI with given BufferReader,PrintWriter,HasMap for commands
	 * @param in BufferReader represent the input source
	 * @param out PrintWriter represent the output source
	 * @param commands HashMap<String,Command> represent the commands hashmap to execute
	 */
	public CLI(BufferedReader in, PrintWriter out, HashMap<String,Command> commands) {
		super();
		this.in = in;
		this.out = out;
		this.commands=commands;
		commandsStrings = new HashSet<>(commands.keySet());
	}
	/**
	 * This method will start to run the CLI
	 */
	public void start()
	{
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(in);
		String inputLineString = "";
		String[] inputLineAsArray = {};
		
		
		// Input command. 
		// While input is not valid ("" or "    "..), do it again.
		while (inputLineString.length() == 0){
			printMenu();
			
			inputLineString = scanner.nextLine();
			inputLineString = inputLineString.replaceAll("\\s+", " ").trim();
			inputLineAsArray = inputLineString.split(" ");
		}
		
		while(!inputLineString.startsWith("Exit"))
		{
			String commandString = new String(inputLineString); //Building the command
			String commandArgs   = new String(); //The commands arguments 
			int i = 0;
			
			boolean foundCommand = false;
			// Go through the input string backwards, and find the longest command possible. 
			// Once found, the loop stops, and sends the command with the arguments found.
			// flag: foundCommand
			while(!foundCommand){
				commandString.trim();
				if (commands.containsKey(commandString))
				{
					foundCommand = true;
					commandArgs=commandArgs.trim();
					if (commandArgs.length()>0)
					{
						List<String> list = Arrays.asList(commandArgs.split(" "));
					    Collections.reverse(list);
					    commands.get(commandString).doCommand(list.toArray(inputLineAsArray));
					}
					else if (commandArgs.length() == 0)
					{
						//Argument array is empty, send the command as it is.
						commands.get(commandString).doCommand(new String[1]);	
					}
				}
				else if (!commands.containsKey(commandString))
				{
					if(commandString.length()-inputLineAsArray[inputLineAsArray.length-(1+i)].length()-1<=-1)//if we can't remove the word
					{
						break;
					}
					else //remove the parameter and add to parameters string
					{
						i++;
						commandString = commandString.substring(0, commandString.length()-inputLineAsArray[inputLineAsArray.length-i].length()-1);
						commandArgs+=inputLineAsArray[inputLineAsArray.length-i]+" ";
					}
				}
			}
			
			//if we didn't see command after removing all the words we will notice
			if(!commands.containsKey(commandString))
				{
					System.out.println("Commmand: "+commandString+ " is not valid");
					out.println("Enter a valid command. Enter command 'menu' if you don't remember the commands. ");
					out.flush();
				}
				
			inputLineString = ""; 
			// Input command. 
			// While input is not valid ("" or "    "..), do it again.
			while (inputLineString.length() == 0){
				//Print Menu (cliMenu). 
				if (!cliMenu.equals(""))
				{
					out.println("Please enter the next command: ");
					out.flush();
					//System.out.println(cliMenu);
				}
				
				inputLineString = scanner.nextLine();
				inputLineString = inputLineString.replaceAll("\\s+", " ").trim();
				inputLineAsArray = inputLineString.split(" ");
				if (inputLineString.equals("menu"))
				{
					inputLineString = "";
					printMenu();
				}
			}
		}
		commands.get(inputLineAsArray[0]).doCommand(inputLineAsArray);
		out.println("Exiting from the program...Bye!");
		out.flush();
		try {in.close();} catch (IOException e) {e.printStackTrace();}out.close();
	}
	@Override 
	/**
	 * this method will execute start() method as runnable task
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		start();	
	}
	
	/**
	 * This method will return the input source as BufferReader
	 * @return BufferReader represent the input source
	 */
	public BufferedReader getIn() {return in;}
	/**
	 * This method will set the input source as BufferReader
	 * @param in BufferReader represent the input source to set
	 */
	public void setIn(BufferedReader in) {this.in = in;}
	/**
	 * This method will return the output source as PrintWriter
	 * @return PrintWriter represent the output source
	 */
	public PrintWriter getOut() {return out;}
	/**
	 * This method will set the output source as PrintWriter
	 * @param out PrintWriter represent the output source to set
	 */
	public void setOut(PrintWriter out) {this.out = out;}
	/**
	 * This method will return the command map as HashMap<String, Command> 
	 * @return HashMap<String, Command> represent the commands map
	 */
	public HashMap<String, Command> getCommands() {return commands;}
	/**
	 * This method will set the command map as HashMap<String, Command> 
	 * @param commands HashMap<String, Command> represent the commands map
	 */
	public void setCommands(HashMap<String, Command> commands) 
	{
		this.commands = commands;
		commandsStrings = new HashSet<>(commands.keySet());
	}
	/**
	 * This method will set the cli menu
	 * @param cliMenu represent the menu of the cli
	 */
	public void setCLIMenu(String cliMenu) {this.cliMenu=cliMenu;}
	/**
	 * This method will print the cliMenu to the screen
	 */
	public void printMenu(){
		//Print Menu (cliMenu). 
		if (!cliMenu.equals(""))
		{
			out.println("	******************************Menu******************************");
			out.println(cliMenu);
			out.flush();
		}
		else{System.out.println("Error while printing the menu, menu seems to be empty.");}
	}
}