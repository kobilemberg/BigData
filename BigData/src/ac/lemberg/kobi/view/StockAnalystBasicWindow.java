package ac.lemberg.kobi.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import ac.lemberg.kobi.presenter.Command;
import ac.lemberg.kobi.properties.Properties;
import ac.lemberg.kobi.ssh.SSHAdapter;

public class StockAnalystBasicWindow extends BasicWindow implements View{
	
	HashMap<String, Command> viewCommandMap;
	BufferedReader in;
	PrintWriter out;
	int userCommand=0;
	Properties properties;
	boolean serverIsOn = true; 
	Label serverStatus;
	Label serverAddress;
	Label numOfClients; 
	Button startStopButton;
	Text hostText;
	Combo maximumClients;
	
	public StockAnalystBasicWindow(String title, int width, int height,Properties properties) {
		super(title, width, height);
		this.properties=properties;
	}
	
	

	@Override
	void initWidgets() {
		shell.setLayout(new GridLayout(2, false));
		TabFolder folder = new TabFolder(shell, SWT.NULL); 
		TabItem serverTab = new TabItem(folder, SWT.NULL);
		serverTab.setText("Control Panel");
		Composite serverForm = new Composite(folder, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.justify = false;
		rowLayout.pack = true;
		rowLayout.type = SWT.VERTICAL;
		serverForm.setLayout(rowLayout);
		serverTab.setControl(serverForm);
		

	
		
		serverAddress = createLabel(serverForm, SWT.NULL, ("Job host: "+properties.getHost()+":"+properties.getPort()));
		serverStatus = createLabel(serverForm, SWT.NULL , "Job Status: Off");
		startStopButton = createButton(serverForm, "Start job", "Resources/power.png",160,30);
		TabItem propertiesTab = new TabItem(folder, SWT.NULL);
		propertiesTab.setText("Properties");
		Composite propertiesForm = new Composite(folder, SWT.NONE);
		propertiesForm.setLayout(rowLayout);
		propertiesTab.setControl(propertiesForm);
		createLabel(propertiesForm, SWT.NONE, "host:", 110, 15);
		hostText = createText(propertiesForm, SWT.SINGLE | SWT.BORDER, properties.getHost(), 147, 15);
		createLabel(propertiesForm, SWT.NONE, "", 110, 10);
		Button submitButton = createButton(propertiesForm, " Update    ", "Resources/save.png",160,30);
		
		/* What happens when a user clicks "[Start/Stop Server]". */ 
		startStopButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (serverIsOn){
					viewCommandMap.get("Connect").doCommand(new String[]{(properties.getHost()),(properties.getUserName()),(properties.getPassword())});
					System.out.println("Connected!");
					
					viewCommandMap.get("Execute").doCommand(new String[]{"rm -Rf "+properties.getJobServerInputFolderPath()});
					viewCommandMap.get("Execute").doCommand(new String[]{"rm -Rf "+properties.getJobServerOutputFolderPath()});
					viewCommandMap.get("Execute").doCommand(new String[]{"hadoop fs -rmr output"});
					System.out.println("Removed all folders");
					viewCommandMap.get("Execute").doCommand(new String[]{"mkdir "+properties.getJobServerInputFolderPath()});
					viewCommandMap.get("Execute").doCommand(new String[]{"cd "+properties.getJobServerInputFolderPath()});
					File inputFolder = new File("input");
					File[] listOfFiles = inputFolder.listFiles();
					for(File log:listOfFiles)
					{
						viewCommandMap.get("Transfer").doCommand(new String[]{"input/"+log.getName(),properties.getJobServerInputFolderPath()});
					}
					System.out.println("Transferd files from input To: "+properties.getJobServerInputFolderPath());
					viewCommandMap.get("Execute").doCommand(new String[]{"hadoop fs -mkdir logFilterInput"});
					viewCommandMap.get("Execute").doCommand(new String[]{"hadoop fs -put "+properties.getJobServerInputFolderPath()+" logFilterInput"});
					viewCommandMap.get("Execute").doCommand(new String[]{"cd /home/training/Desktop; "});
					viewCommandMap.get("Transfer").doCommand(new String[]{"Jars/logFilter.jar ","/home/training"});
					System.out.println("Jars are currently being uploaded to hadoop");
					viewCommandMap.get("Execute").doCommand(new String[]{"cd /home/training; hadoop jar logFilter.jar solution.LogFilter logFilterInput/input output"});
					System.out.println("hadoop is running");
					viewCommandMap.get("Execute").doCommand(new String[]{"hadoop fs -get output "+properties.getJobServerOutputFolderPath()});
					System.out.println("Copy from hadoop the files to linux");
					viewCommandMap.get("Get file").doCommand(new String[]{properties.getJobServerOutputFolderPath()+"/part-r-00000"});
					viewCommandMap.get("Get file").doCommand(new String[]{properties.getJobServerOutputFolderPath()+"/_SUCCESS"});
					System.out.println("FIles are at output folder.");
				}
				else
				{
					setUserCommand(1);
					String[] params = {};
					notifyObservers(params);
				}
				
				//serverStatus.setText("Status: On.");
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
		
		/* What happens when a user clicks "[Update]". */ 
		submitButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				properties.setHost(hostText.getText());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
		shell.layout();
	}
		
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void start() {
		this.run();		
	}

	@Override
	public void setCommands(HashMap<String, Command> viewCommandMap) {
		this.viewCommandMap = viewCommandMap;
	}

	

	@Override
	public void errorNoticeToUser(String s) {
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		    	MessageBox messageBox = new MessageBox(new Shell(display),SWT.ICON_INFORMATION|SWT.OK);
				messageBox.setMessage(s);
				messageBox.setText("Notification");
				messageBox.open();
		    }
		});		
	}

	@Override
	public int getUserCommand() {
		return userCommand;
	}

	@Override
	public void setUserCommand(int commandID) 
	{
		this.setChanged();
		this.userCommand = commandID;
	}

	@Override
	public void displayData(Object data) {
		System.out.println(data.toString());
		
	}

	

	
	
	@Override
	public HashMap<String, Command> getViewCommandMap() {
		return viewCommandMap;
	}
	
	@Override
	public void setCommandsMenu(String cliMenu) {
		// TODO Auto-generated method stub
	}
	
	
	/**
	 * This method create a button with the following parameters
	 * @param parent represent the Composite to being added to
	 * @param text represent the text value of thr button
	 * @param image represent the image that will be added to button
	 * @return button
	 */
	private Button createButton(Composite parent, String text, String image) {
	    Button button = new Button(parent, SWT.PUSH);
	    button.setImage(new Image(Display.getCurrent(), image));
	    button.setLayoutData(new RowData(120, 30));
	    button.setText(text);	    
	    return button;
	}
	/**
	 * This method create a button with the following parameters
	 * @param parent represent the Composite to being added to 
	 * @param text represent the text value of thr button
	 * @param image image represent the image that will be added to button
	 * @param width represent the width
	 * @param height represent the height
	 * @return button
	 */
	private Button createButton(Composite parent, String text, String image, int width, int height) {
		Button button = createButton(parent, text, image);
    	button.setLayoutData(new RowData(width, height));
    	return button; 		
	}
	
	
	/**
	 * This method will create label with\by the following values
	 * @param parent represent the composite to be added to
	 * @param style represent the style
	 * @param placeholder represent the text value of the label
	 * @param width represent label width
	 * @param height represent label height
	 * @return label
	 */
	private Label createLabel(Composite parent, int style, String placeholder, int width, int height){
		Label label = new Label(parent, style);
		label.setLayoutData(new RowData(width, height));
		label.setText(placeholder);
		return label; 		
	}
	
	/**
	 * This method will create label with\by the following values
	 * @param parent represent the composite to be added to
	 * @param style represent the style
	 * @param placeholder represent the text value of the label
	 * @return label
	 */
	private Label createLabel(Composite parent, int style, String placeholder){
		return createLabel(parent, style, placeholder, 120, 30); 		
	}
	/**
	 * This method will create text box with\by the following values
	 * @param parent represent the composite to be added to
	 * @param style represent the style
	 * @param placeholder represent the text value of the label
	 * @param width represent Text width
	 * @param height represent Text height
	 * @return the text box
	 */
	private Text createText(Composite parent, int style, String placeholder, int width, int height){
		Text text = new Text(parent, style);
	    text.setText(placeholder);
	    text.setLayoutData(new RowData(width, height));
	    return text; 
	}
	
	@SuppressWarnings("unused")
	/**
	 * This method will create text box with\by the following values
	 * @param parent represent the composite to be added to
	 * @param style represent the style
	 * @param placeholder represent the text value of the label
	 * @return the text box
	 */
	private Text createText(Composite parent, int style, String placeholder){
		return createText(parent, style, placeholder, 120, 15); 
	}
	/**
	 * This method will create combo box with\by the following values
	 * @param parent represent the parent to be added to
	 * @param style represent the style
	 * @param options represent the String[] of strings to put at the combo
	 * @param placeholder represent the first value of the combo
	 * @param width represent the combo width
	 * @param height represent the combo height
	 * @return combo 
	 */
	private Combo createCombo(Composite parent, int style, String[] options, String placeholder, int width, int height){
		Combo combo = new Combo(parent, style);
		for (int i = 0; i < options.length; i++) {
			combo.add(options[i]);
		}
		combo.setText(placeholder);
		combo.setLayoutData(new RowData(width, height));
		return combo;
	}
	
	@SuppressWarnings("unused")
	/**
	 * This method will create combo box with\by the following values
	 * @param parent represent the parent to be added to
	 * @param style represent the style
	 * @param options represent the String[] of strings to put at the combo
	 * @param placeholder represent the first value of the combo
	 * @return combo 
	 */
	private Combo createCombo(Composite parent, int style, String[] options, String placeholder){
		return createCombo(parent, style, options, placeholder, 90, 20);
	}
	/**
	 * Thie method changes the server button
	 * @param status boolean represent the server status to set
	 */
	private void toggleServerStatus(boolean status){
		if (!status){

			serverIsOn = false; 
			serverStatus.setText("Status: Off");
			startStopButton.setText("Start Server");
		}
		else
		{
			serverIsOn = true; 
			serverStatus.setText("Status: On");
			startStopButton.setText("Stop Server");
		}
		
	}

}
