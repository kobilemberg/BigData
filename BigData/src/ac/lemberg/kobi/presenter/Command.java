package ac.lemberg.kobi.presenter;



/**
 * @author Kobi Lemberg
 * @version 1.0
 * <h1> Command </h1>
 * Command interface represent a generally command pattern with given args as String array, 
 * Each developer will have to implement doCommand(String[] args) method.
 */
public interface Command {
	/**
	 * This method will run command with given array of strings.
	 *@param args String[] represent arguments for command.
	 */

	void doCommand(String[] args);
}
