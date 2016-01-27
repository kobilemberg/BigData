package ac.lemberg.kobi.view;

import java.io.IOException;
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
import ac.lemberg.kobi.stocks.Stock;
import solution.Properties;

public class StockAnalystBasicWindow extends BasicWindow implements View{
	
	HashMap<String, Command> viewCommandMap;
	int userCommand=0;
	Properties properties;
	Label serverStatus;
	Label serverAddress;
	Label numOfClients; 
	Button startStopButton;
	Text hostText;
	Combo maximumClients;
	

	
	/**
	 * Instantiate a new StockAnalystBasicWindow.
	 * @param title represent window title.
	 * @param width represent window width.
	 * @param height represent window height.
	 * @param properties represent xml properties file.
	 */
	public StockAnalystBasicWindow(String title, int width, int height,Properties properties) {
		super(title, width, height);
		this.properties=properties;
	}
	
	

	@SuppressWarnings("unused")
	@Override
	/**
	 * {@inheritDoc}
	 */
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
		serverAddress = createLabel(serverForm, SWT.NULL, "Host:"+properties.getHost());
		//Label numberOfStocks = new Label(serverForm, SWT.NULL);
		//numberOfStocks.setText("Number of Stocks: ");
		Label labelStocks = createLabel(serverForm, SWT.NONE, "Number of Stocks: ", 110, 15);
		Text textStocks = createText(serverForm, SWT.SINGLE | SWT.BORDER, " ");
		Label labelAnalyze = createLabel(serverForm, SWT.None, "Days backwards: ",110,15);
		Text textAnalyze = createText(serverForm, SWT.SINGLE | SWT.BORDER, " ");
		Label labelFeature = createLabel(serverForm, SWT.None, "Features analyze: ",110,15);
		Button buttonOpen = new Button(serverForm, SWT.CHECK);
		buttonOpen.setText("Open");
		Button buttonHigh = new Button(serverForm, SWT.CHECK);
		buttonHigh.setText("High");
		Button buttonLow = new Button(serverForm, SWT.CHECK);
		buttonLow.setText("Low");
		Button buttonClose = new Button(serverForm, SWT.CHECK);
		buttonClose.setText("Close");
		Label labelCluster = createLabel(serverForm, SWT.NONE, "Clusters: ", 110, 15);
		Text textCluster = createText(serverForm, SWT.SINGLE | SWT.BORDER, " ");
		serverStatus = createLabel(serverForm, SWT.NULL , "Job Status: Off");
		startStopButton = createButton(serverForm, "Start job", "Resources/power.png",160,30);
		TabItem propertiesTab = new TabItem(folder, SWT.NULL);
		propertiesTab.setText("Properties");
		Composite propertiesForm = new Composite(folder, SWT.NONE);
		propertiesForm.setLayout(rowLayout);
		propertiesTab.setControl(propertiesForm);
		hostText = createText(propertiesForm, SWT.SINGLE | SWT.BORDER, properties.getHost(), 147, 15);
		createLabel(propertiesForm, SWT.NONE, "", 110, 10);
		Button submitButton = createButton(propertiesForm, " Update    ", "Resources/save.png",160,30);
		
		/* What happens when a user clicks "[Start/Stop Job]". */ 
		startStopButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
					serverStatus.setText("Status: On.");

					if (startStopButton.getText().toString().equals("Start job"))
					{
						if (!textStocks.getText().trim().isEmpty() && !textAnalyze.getText().trim().isEmpty() && !textCluster.getText().trim().isEmpty())
						{
							int numberOfStocks = new Integer(new String(textStocks.getText()).trim()).intValue();
							int analyze = new Integer(new String(textAnalyze.getText()).trim()).intValue();
							int cluster = new Integer(new String(textCluster.getText()).trim()).intValue();
							boolean open = buttonOpen.getSelection();
							boolean high = buttonHigh.getSelection();
							boolean close = buttonClose.getSelection();
							boolean low = buttonLow.getSelection();
							startStopButton.setText("Working");
							startStopButton.setEnabled(false);
							remoteSolve(numberOfStocks,analyze,cluster,open,high,low,close);
						}
						else
						{
							MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.ABORT | SWT.RETRY | SWT.IGNORE);
					        messageBox.setText("Warning");
					        messageBox.setMessage("Fill All the Board!");
					        messageBox.open();
						}
					}
					/*else if (startStopButton.getText().toString().equals("Working"))
					{
						startStopButton.setText("Start job");
						Thread t = new Thread(new Runnable() {	
							@Override
							public void run() {
								//remoteSolve();
								disconnect();
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										serverStatus.setText("Status: Off.");
										startStopButton.setEnabled(true);
									}
								});
							}
						});
						t.start();
					}*/
				}
					
			
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
		
		/* What happens when a user clicks "[Update]". */ 
		submitButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				properties.setHost(hostText.getText());
				serverAddress.setText("Host:" +hostText.getText());
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {}
		});
		shell.layout();
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
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
	/**
	 * {@inheritDoc}
	 */
	public int getUserCommand() {
		return userCommand;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void setUserCommand(int commandID) 
	{
		this.setChanged();
		this.userCommand = commandID;
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void displayData(Object data) {
		/*System.out.println(data.toString());
		if(data.equals("Done"))
		{
			setUserCommand(100);
			notifyObservers(new String[]{"1"});
		}
		*/
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void start() {
		this.run();		
	}
	
	public void exit(){
		viewCommandMap.get("Exit").doCommand(new String[]{"Bye!"});
		display.dispose(); // dispose OS components
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void setCommands(HashMap<String, Command> viewCommandMap) {
		this.viewCommandMap = viewCommandMap;
	}
	
	@Override
	public HashMap<String, Command> getViewCommandMap() {
		return viewCommandMap;
	}
	
	@Override
	/**
	 * {@inheritDoc}
	 */
	public void setCommandsMenu(String cliMenu) {
		// TODO Auto-generated method stub
	}
	
	/**
	 * This method create a button with the following parameters
	 * @param parent represent the Composite to being added to
	 * @param text represent the text value of the button
	 * @param image represent the image that will be added to button
	 * @return button
	 */
	private Button createButton(Composite parent, String text, String image) {
	    Button button = new Button(parent, SWT.TOGGLE);
	    button.setImage(new Image(Display.getCurrent(), image));
	    button.setLayoutData(new RowData(120, 30));
	    button.setText(text);	    
	    return button;
	}
	/**
	 * This method create a button with the following parameters
	 * @param parent represent the Composite to being added to 
	 * @param text represent the text value of the button
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
	
	public void disconnect()
	{
		viewCommandMap.get("Exit").doCommand(new String[] {"null"});
		System.exit(0);

		exit();
	}
	
	/**
	 * This method will do the following:
	 * 		Open new SSH connection with Hadoop job server.
	 * 		Copy all files under "input" to "jobServerInputFolderPath" setted on Properties.xml
	 * 		Copy job's jar located under "Jars" to Hadoop at /home/training/
	 * 		Upload input files and the jar from the linux to cloudera Hadoop
	 * 		Run the job
	 * 		Copy from linux to windows the results. 
	 */
	public void remoteSolve(int numberOfStocks, int analyze, int clusters,boolean open, boolean high, boolean low, boolean close)
	{
		
		try {
			viewCommandMap.get("Analyze").doCommand(new String[]{numberOfStocks+"",analyze+"",clusters+"",open+"",high+"",low+"",close+""});
		} catch (Exception e) {
			System.out.println("Something went wrong!");
		}
	}



	@Override
	public void setStockMap(HashMap<String, Stock> stocksMap) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				serverStatus.setText("Status: Off.");
				startStopButton.setEnabled(true);
				startStopButton.setText("Start job");
			}
		});
		
		try {
			Runtime.getRuntime().exec(new String[]{"cmd", "/c","start firefox.exe Output/graphs.html"});
		} catch (IOException e) {
			e.printStackTrace();
		}
        
	}
	
	

}
