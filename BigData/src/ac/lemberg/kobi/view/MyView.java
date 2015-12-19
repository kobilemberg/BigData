package ac.lemberg.kobi.view;

/**
 * @author Kobi Lemberg
 * @version 1.0
 * <h1> MyView </h1>
 * MyView class implements View interface, 
 * class goal is to act as MVC View layer and to display applications to end-user.
 */


import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Observable;

import ac.lemberg.kobi.presenter.Command;
import ac.lemberg.kobi.properties.Properties;

public class MyView extends Observable implements View {
	CLI cli;
	HashMap<String, Command> viewCommandMap;
	private String cliMenu;
	BufferedReader in;
	PrintWriter out;
	int userCommand=0;
	Properties properties;

	

	//Constructors

	/**
	 * Instantiates a new  my own view with given: BufferedReader in, PrintWriter out
	 * @param in BufferedReader represent the input source
	 * @param out PrintWriter represent the output source
	 */
	public MyView(BufferedReader in, PrintWriter out,Properties proerties)
	{
		super();
		this.in = in;
		this.out=out;
		this.properties = proerties;
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	//Functionality
	@Override
	/**
	* this method will start to run the view layer
	*/
	public void start() {cli.start();}
	
	@Override
	
	/**
	 * {@inheritDoc}
	 */
	public void displayData(Object data) {
		out.println(data);
		out.flush();		
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void errorNoticeToUser(String s) {
		out.println("Notification:\n"+s);
		out.flush();	
	}
	
	//Getters and Setters
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void setCommands(HashMap<String, Command> viewCommandMap) 
	{
		this.viewCommandMap = viewCommandMap;
		cli = new CLI(in, out,viewCommandMap);
		if(cliMenu!=null)
			cli.cliMenu = cliMenu;
	}
	
	//@Override
	/**
	 * {@inheritDoc}
	 */
	public void setCommandsMenu(String cliMenu) {
		this.cliMenu = cliMenu;
		if(cli!=null){cli.setCLIMenu(cliMenu);}	
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public int getUserCommand() {return this.userCommand;}
	
	@Override
	public void setUserCommand(int commandID) 
	{
		this.setChanged();
		this.userCommand = commandID;
	}

	/**
	 * @return the cli
	 */
	public CLI getCli() {return cli;}
	/**
	 * @param cli the cli to set
	 */
	public void setCli(CLI cli) {this.cli = cli;}
	/**
	 * @return the viewCommandMap
	 */
	public HashMap<String, Command> getViewCommandMap() {return viewCommandMap;}

	/**
	 * @return the cliMenu
	 */
	public String getCliMenu() {return cliMenu;}
	/**
	 * @param cliMenu the cliMenu to set
	 */
	public void setCliMenu(String cliMenu) {this.cliMenu = cliMenu;}
	/**
	 * @return the in
	 */
	public BufferedReader getIn() {return in;}
	/**
	 * @param in the in to set
	 */
	public void setIn(BufferedReader in) {this.in = in;}
	/**
	 * @return the out
	 */
	public PrintWriter getOut() {return out;}
	/**
	 * @param out the out to set
	 */
	public void setOut(PrintWriter out) {this.out = out;}

	
	
}
