package ac.lemberg.kobi.presenter;


import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import ac.lemberg.kobi.model.Model;
import ac.lemberg.kobi.view.View;

public class Presenter implements Observer {
	private View view;
	private Model model;
	private HashMap<String, Command> viewCommandMap;
	
	
	/**
	* Instantiates a new  my own Presenter with given view and model.
	* @param view View represent the view layer
	* @param model Model represent the model layer
	* */
		public Presenter(View view, Model model) {
			super();
			this.view = view;
			this.model = model;
			
			HashMap<String, Command> viewCommandMap = new HashMap<String, Command>();
			viewCommandMap.put("Connect",new Command() 
			{	
				@Override
				public void doCommand(String[] args) 
				{
					try {
						view.setUserCommand(1);
						((Observable)view).notifyObservers(args);
					} catch (NullPointerException e) {e.printStackTrace();}	
				}
			});
			
			viewCommandMap.put("Execute",new Command() 
			{
				@Override
				public void doCommand(String[] args) 
				{
					try {
						view.setUserCommand(2);
						((Observable)view).notifyObservers(args);
					} catch (Exception e) {
						e.printStackTrace();
					}
					 
				}
			});
			
			viewCommandMap.put("Transfer",new Command() 
			{
				@Override
				public void doCommand(String[] args) 
				{
					//args[1] = name
					try {
						view.setUserCommand(3);
						((Observable)view).notifyObservers(args);
					} catch (NullPointerException e) {
						e.printStackTrace();
						}
				}
			});
			
			viewCommandMap.put("Get file",new Command() 
			{
				@Override
				public void doCommand(String[] args) 
				{
					try {
						view.setUserCommand(4);
						((Observable)view).notifyObservers(args);
					} catch (Exception e) {
						e.printStackTrace();
						}
				}
			});
			
			viewCommandMap.put("Analyze", new Command() {
				
				@Override
				public void doCommand(String[] args) {
					try {
						view.setUserCommand(5);
						((Observable)view).notifyObservers(args);
					} catch (Exception e) {
						e.printStackTrace();
						}
					
				}
			});
			
			
			
			viewCommandMap.put("Exit",new Command() 
			{
				@Override
				public void doCommand(String[] args) {
					view.setUserCommand(0);
					((Observable)view).notifyObservers(args);
					model.exit();}
			});
			
			
			
			String cliMenu=new String();
			cliMenu+= "1:	Connect <host> <UserName> <Password>\n";
			cliMenu+= "2:	Execute <Linux command>\n";
			cliMenu+= "3:	Transfer <Source file path> <Target file path>\n";
			cliMenu+= "4:	Get file <file path>\n";
			cliMenu+= "0:	Exit\n";
			this.viewCommandMap = viewCommandMap;
			view.setCommands(viewCommandMap);
			view.setCommandsMenu(cliMenu);
			
		}

	//Getters and setters
		
		/**
	 * @return the viewCommandMap
	 */
	public HashMap<String, Command> getViewCommandMap() {
		return viewCommandMap;
	}

	/**
	 * @param viewCommandMap the viewCommandMap to set
	 */
	public void setViewCommandMap(HashMap<String, Command> viewCommandMap) {
		this.viewCommandMap = viewCommandMap;
	}

		/**
		 * This method will set the view layer
		 * @param view View represent the view layer
		 */
		public void setView(View view){this.view = view;}
		/**
		* This method will set controller's model layer
		* @param model Model represent the model layer
		*/
		public void setModel(Model model){this.model = model;}
		/**
		* This method will return the controller's view layer
		* @return View instance represent the view layer of the controller
		*/
		public View getView(){return view;}
		/**
		* This method will return the controller's model layer
		* @return Model instance represent the Model layer of the controller
		*/
		public Model getModel(){return model;}
		
		public void errorNoticeToViewr(String s) {view.errorNoticeToUser(s);}
	
		
		
		
	

	@Override
	 public void update(Observable o, Object args) {
		if(o==view)
		{
			String[] argArr = ((String[])args).clone();
			
			int input = view.getUserCommand();
			switch (input) {
			case 1:
				try {model.sshConnect(argArr[0], argArr[1],  argArr[2]);
				}catch (Exception e) {
					e.printStackTrace();
				}
				break;
			
			case 2:
				try {
						model.executeCommand(stringArrtoString(argArr));
					}
					catch (Exception e) {
					e.printStackTrace();
				}
				break;

			case 3:
				model.transferFile(argArr[0],argArr[1]);
				break;

			case 4:
				try {
					model.getFIleByName(argArr[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 5:
				try {
					model.analyzeData(argArr[0],argArr[1],argArr[2],argArr[3],argArr[4],argArr[5],argArr[6]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 0:
				model.exit();
				break;
				

			default:
				break;
			}
		}

		else if(o==model)
		{
			
			int modelCompletedNum = model.getModelCompletedCommand();
			if(modelCompletedNum!=-1)
				view.displayData(model.getData());
			else
				view.errorNoticeToUser((String) model.getData());
		}
	}
	
	private String stringArrtoString(String[] strings)
	{
		String str="";
		for(String s:strings)
		{
			if (s!="" && s!= null)
				str+=s+" ";
		}
		str.trim();
		return str;
	}
	
	
}


