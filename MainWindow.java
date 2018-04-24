package com.agfa.med.EIPS;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Group;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.apache.log4j.Logger;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import java.awt.Component;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;

public class MainWindow {
	
	protected Shell shlAmtEiApi;
	private Text txtEnterLbFqdn;
	private Text txtService;
	private Text text;
	private Text txtICFreeSpace;
	private Text text_1;
	private Button btnApplyConfig;
	private Button button;
	private Button btnPopStorage;
	private Button buttonCheckSP;
	public PGdbconnector pdb;
	public API_Call apc;
	private Text text_3;
	private Text text_2;
	public TableViewer tableViewer,tableViewer2;
	public static MainWindow window;
	public static volatile boolean running,kill,SMVal,MVKill,SDVal;
	public static volatile String runningOpType,MVOpType;
	private Text text_4;
	public Text text_5;
	private Text text_6;
	private Text text_7;
	private Integer Mtimeout,Dtimeout;
	final static Logger logger = Logger.getLogger(MainWindow.class);
	public static String curlloc,db,FQDN,username,password;
	public Text text_8;
	private Text text_9;
	public Text text_10;
	public Text text_11;
	public Text text_12;
	public Text text_13;
	public Text text_14;
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		
		//db = args[0];
		//curlloc = args[1];
		db =  "jdbc:postgresql://localhost:5432/amt?user=amt&password=amt";
		curlloc="c:\\cygwin64\\bin\\curl.exe";
		
		Display display = Display.getDefault();
		Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {
			public void run() {
				try {
					window = new MainWindow();
					window.initiate();
					window.open();
					} catch (Exception e) {
					logger.fatal("Error in the GUI: ",e);
					}
			}
		});
	}

	/**
	 * Open the window.
	 */
	public void initiate() {
		try {
			logger.debug("Creating Connection to AMT Postgres database.");
			Class.forName("org.postgresql.Driver");
			} catch (ClassNotFoundException e) {	
			logger.fatal("Error creating connection to amt database: ",e);
			return;
			}
		logger.debug("Creating objects.");
		pdb = new PGdbconnector();
		pdb.CreateSchema();
		apc = new API_Call(pdb);
	}
	
	public void open() {
		logger.debug("Opening main window.");
		Display display = Display.getDefault();
		createContents();
		shlAmtEiApi.open();
		shlAmtEiApi.layout();

		Image[] icons = new Image[2];
		icons[0] = new Image(display, MainWindow.class.getResourceAsStream("/flying_goose16.jpg"));
		icons[1] = new Image(display, MainWindow.class.getResourceAsStream("/flying_goose32.jpg"));
		shlAmtEiApi.setImages(icons);
		while (!shlAmtEiApi.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		try {
			pdb.closeShop();
		} catch (Exception e) {
			logger.fatal("Error closing database connection: ",e);
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		
		MainWindow.running=false;
		MainWindow.kill = false;
		MainWindow.SMVal = false;
		MainWindow.MVKill=false;
		MainWindow.SDVal=false;
		MainWindow.runningOpType="";
		MainWindow.MVOpType="";
		
		shlAmtEiApi = new Shell();
		shlAmtEiApi.setSize(769, 540);
		shlAmtEiApi.setText("FLYING GOOSE PST v1.0");
		
		TabFolder tabFolder = new TabFolder(shlAmtEiApi, SWT.NONE);
		tabFolder.setBounds(0, 0, 756, 502);
		
		TabItem tbtmConfig = new TabItem(tabFolder, SWT.NONE);
		tbtmConfig.setText("Config");
		
		Composite composite_config = new Composite(tabFolder, SWT.NONE);
		tbtmConfig.setControl(composite_config);
		
		Group grpCredentials = new Group(composite_config, SWT.NONE);
		grpCredentials.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		grpCredentials.setText("Credentials");
		grpCredentials.setBounds(116, 10, 515, 262);
		
		txtEnterLbFqdn = new Text(grpCredentials, SWT.BORDER);
		txtEnterLbFqdn.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		txtEnterLbFqdn.setText(MainWindow.FQDN);
		txtEnterLbFqdn.setLocation(179, 20);
		txtEnterLbFqdn.setSize(313, 29);
		
		
		Label lblLoadBalancercs = new Label(grpCredentials, SWT.NONE);
		lblLoadBalancercs.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblLoadBalancercs.setLocation(10, 20);
		lblLoadBalancercs.setSize(165, 29);
		lblLoadBalancercs.setText("Load Balancer (CS) FQDN:");
		
		Label lblEiAppUsername = new Label(grpCredentials, SWT.NONE);
		lblEiAppUsername.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblEiAppUsername.setLocation(10, 67);
		lblEiAppUsername.setSize(165, 29);
		lblEiAppUsername.setText("EI App Username:");
		
		Label lblEiAppPassword = new Label(grpCredentials, SWT.NONE);
		lblEiAppPassword.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblEiAppPassword.setLocation(10, 115);
		lblEiAppPassword.setSize(165, 29);
		lblEiAppPassword.setText("EI App Password:");
		
		txtService = new Text(grpCredentials, SWT.BORDER | SWT.PASSWORD);
		txtService.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		txtService.setText(MainWindow.password);
		txtService.setLocation(179, 115);
		txtService.setSize(313, 29);
		
		text = new Text(grpCredentials, SWT.BORDER);
		text.setText(MainWindow.username);
		text.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		text.setBounds(179, 67, 313, 29);
		
		text_1 = new Text(grpCredentials, SWT.BORDER | SWT.READ_ONLY);
		text_1.setText("Result Message");
		text_1.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		text_1.setBounds(179, 211, 313, 29);
		
		button = new Button(grpCredentials, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Running Credential Test.");
				class twoThread implements Runnable {
					public void run() {
						apc.run_Test();
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								text_1.setText(apc.testresult);
							}
					});
						
					}
				}
				Thread t2 = new Thread(new twoThread());
				t2.start();
			}
		});
		button.setText("TEST");
		button.setBounds(10, 211, 103, 29);
		
		btnApplyConfig = new Button(grpCredentials, SWT.NONE);
		btnApplyConfig.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Saving credentials.");
				try {
					MainWindow.FQDN=txtEnterLbFqdn.getText();
					MainWindow.username=text.getText();
					MainWindow.password=txtService.getText();
					pdb.StoreCredentials();

					text_1.setText("Result Message");
					} catch (Exception e2) {
					logger.fatal("Error saving credentials: ",e2);
					}
			}
		});
		btnApplyConfig.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		btnApplyConfig.setBounds(10, 163, 482, 29);
		btnApplyConfig.setText("APPLY");
		
		Group Cache = new Group(composite_config, SWT.NONE);
		Cache.setText("Storage Space");
		Cache.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		Cache.setBounds(116, 284, 515, 170);
		
		txtICFreeSpace = new Text(Cache, SWT.BORDER | SWT.READ_ONLY);
		text_3 = new Text(Cache, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		Combo comboSPACES = new Combo(Cache, SWT.READ_ONLY);
		comboSPACES.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtICFreeSpace.setText("");
				text_3.setText("");
			}
		});
		
		TabItem tbtmMove = new TabItem(tabFolder, SWT.NONE);
		tbtmMove.setText("Move");
		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmMove.setControl(composite_1);
		
		Group grpOptions = new Group(composite_1, SWT.NONE);
		grpOptions.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		grpOptions.setText("Options");
		grpOptions.setBounds(10, 10, 728, 209);
		
		Combo combo_1 = new Combo(grpOptions, SWT.READ_ONLY);
		combo_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text_10.setText("");
				text_11.setText("");
				text_12.setText("");
				text_13.setText("");
				text_14.setText("");
			}
		});
		
		comboSPACES.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		comboSPACES.setBounds(126, 42, 287, 29);
		if(pdb.STORAGE_ID.length>0) {
			comboSPACES.setItems(pdb.STORAGE_ID);
			comboSPACES.select(0);
		}
		if(pdb.ARCHIVES.length>0) {
			combo_1.setItems(pdb.ARCHIVES);
			combo_1.select(0);
		}
		
		btnPopStorage = new Button(Cache, SWT.NONE);
		btnPopStorage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtICFreeSpace.setText("");
				text_3.setText("");
				logger.debug("Populating EI Storage Systems.");
				class twoThread implements Runnable {
					public void run() {
						apc.pop_EIS(true);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								pdb.STORAGE_ID=Arrays.copyOf(apc.eis,apc.eis.length);
								if(pdb.STORAGE_ID.length>0) {
									comboSPACES.setItems(pdb.STORAGE_ID);
									comboSPACES.select(0);
								}
								pdb.ARCHIVES=Arrays.copyOf(apc.eida, apc.eida.length);
								if(pdb.ARCHIVES.length>0) {
									combo_1.setItems(pdb.ARCHIVES);
									combo_1.select(0);
								}
								
							}
					});
					}
				}
				Thread t2 = new Thread(new twoThread());
				t2.start();
			}
		});
		btnPopStorage.setBounds(10, 40, 103, 29);
		btnPopStorage.setText("POPULATE");
		
		
		txtICFreeSpace.setText("");
		txtICFreeSpace.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		txtICFreeSpace.setBounds(95, 131, 154, 29);
		
		buttonCheckSP = new Button(Cache, SWT.NONE);
		buttonCheckSP.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String str = comboSPACES.getText();
				class threeThread implements Runnable {
					public void run() {
						apc.get_FS(str);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								txtICFreeSpace.setText(Double.toString(apc.f1));
							}
					});
					}
				}
				Thread t = new Thread(new threeThread());
				t.start();		
			}
		});
		buttonCheckSP.setText("CHECK FREE SPACE");
		buttonCheckSP.setBounds(95, 96, 154, 29);
		
		Label lblGb = new Label(Cache, SWT.NONE);
		lblGb.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblGb.setBounds(250, 143, 24, 17);
		lblGb.setText("GB");
		
		
		Button button_5 = new Button(Cache, SWT.NONE);
		button_5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Checking active shares for "+comboSPACES.getText());
				class threeThread implements Runnable {
					public void run() {
						apc.pop_EIS(false);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								text_3.setText(Integer.toString(apc.active_cons.get(comboSPACES.getText())));
							}
					});
					}
				}
				Thread t3 = new Thread(new threeThread());
				t3.start();				
			}
		});
		button_5.setText("CHECK ACTIVE SHARES");
		button_5.setBounds(280, 96, 154, 29);
		
		
		text_3.setText("");
		text_3.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		text_3.setBounds(280, 131, 154, 29);
		
		TabItem tbtmDelete = new TabItem(tabFolder, SWT.NONE);
		tbtmDelete.setText("Delete");
		
		Composite composite_delete = new Composite(tabFolder, SWT.NONE);
		tbtmDelete.setControl(composite_delete);
		composite_delete.setLayout(new FormLayout());
		
		Composite composite = new Composite(composite_delete, SWT.NONE);
		FormData fd_composite = new FormData();
		fd_composite.top = new FormAttachment(0);
		fd_composite.left = new FormAttachment(0, 4);
		fd_composite.bottom = new FormAttachment(0, 464);
		fd_composite.right = new FormAttachment(0, 744);
		composite.setLayoutData(fd_composite);
		composite.setLayout(new FormLayout());
		
		Group group = new Group(composite, SWT.NONE);
		Group group_1 = new Group(group, SWT.NONE);
		group_1.setBounds(10, 58, 364, 39);
		Group group_2 = new Group(group, SWT.NONE);
		Button button_1 = new Button(group_1, SWT.RADIO);
		Button button_3 = new Button(group_2, SWT.RADIO);
		Button button_6 = new Button(composite, SWT.NONE);
		Combo combo = new Combo(group, SWT.READ_ONLY);
		button_6.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		button_6.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Starting new delete task.");
				int i;
				String dris;
				if(button_3.getSelection()) {
					i=-1;
				} else {
					try {
						i=Integer.parseInt(text_2.getText());
					} catch (NumberFormatException n) {
						Component frame = null;
						JOptionPane.showMessageDialog(frame, "Invalid Input. Integer value required for custom scope.");
						logger.debug("Invalid custom scope specified.");
						return;
					}
				}
				
				if(button_1.getSelection()) {
					dris = "true";
				} else {
					dris = "false";
				}
				Component frame = null;
				int n = JOptionPane.showConfirmDialog(frame, "Are You Sure?","Confirm Study Delete", JOptionPane.YES_NO_OPTION);
				if(n==0) {
					if(MainWindow.running) {
						JOptionPane.showMessageDialog(frame, "A delete or move task is already running. Please wait for it to complete.");
					} else {
						Map<String,String> options = new HashMap<String,String>();
						options.put("dest", combo.getText());
						options.put("deleteris", dris);
						options.put("iocm", combo.getText());
						apc.StartTask(i, window, pdb.amt_con, Dtimeout, options, "DELETE");
					}
				}
			}
		});
		button_6.setText("EXECUTE");
		FormData fd_button_6 = new FormData();
		fd_button_6.left = new FormAttachment(0, 15);
		button_6.setLayoutData(fd_button_6);
		
		Label lblInsertStudyUids_1 = new Label(composite, SWT.NONE);
		lblInsertStudyUids_1.setText("Insert Study UIDs to be deleted into table: ei_api.study_delete_uids, status must contain the word Ready. (case insensitive)");
		lblInsertStudyUids_1.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
		FormData fd_lblInsertStudyUids_1 = new FormData();
		fd_lblInsertStudyUids_1.top = new FormAttachment(button_6, 11);
		fd_lblInsertStudyUids_1.right = new FormAttachment(100, -10);
		lblInsertStudyUids_1.setLayoutData(fd_lblInsertStudyUids_1);
		
		
		tableViewer = new TableViewer(composite, SWT.MULTI |SWT.BORDER | SWT.FULL_SELECTION);
		createColumns(composite, tableViewer);
		final Table table = tableViewer.getTable();
		table.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
		FormData fd_table = new FormData();
		fd_table.left = new FormAttachment(0, 15);
		fd_table.bottom = new FormAttachment(100, -7);
		fd_table.right = new FormAttachment(100, -15);
		table.setLayoutData(fd_table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		Button btnNewButton = new Button(composite, SWT.NONE);
		fd_lblInsertStudyUids_1.left = new FormAttachment(btnNewButton, 11);
		fd_table.top = new FormAttachment(btnNewButton, 6);
		fd_button_6.bottom = new FormAttachment(btnNewButton, -6);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				apc.Status_Update(window,"DELETE");
				tableViewer.setInput(apc.list);
			}
		});
		btnNewButton.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.left = new FormAttachment(0, 15);
		fd_btnNewButton.bottom = new FormAttachment(100, -183);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("REFRESH");
		
		Button btnCancel = new Button(composite, SWT.NONE);
		fd_button_6.right = new FormAttachment(btnCancel, -24);
		btnCancel.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(MainWindow.running&&MainWindow.runningOpType.contains("DELETE")) {
					MainWindow.kill=true;
					Component frame = null;
					JOptionPane.showMessageDialog(frame, "Routine will exit after timeout period expires unless study delete validates first.");
				}
			}
		});
		btnCancel.setText("CANCEL");
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.left = new FormAttachment(0, 277);
		btnCancel.setLayoutData(fd_btnCancel);
		

		combo_1.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		combo_1.setBounds(115, 106, 287, 25);
		
		Label label_4 = new Label(grpOptions, SWT.NONE);
		label_4.setText("DESTINATION:");
		label_4.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		label_4.setBounds(10, 109, 86, 17);
		
		text_4 = new Text(grpOptions, SWT.BORDER);
		text_4.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		text_4.setBounds(457, 174, 64, 25);
		text_4.setText(Integer.toString(10));
		
		text_5 = new Text(grpOptions, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		text_5.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		text_5.setBounds(188, 174, 64, 25);
		text_5.setText(Integer.toString(600));
		Mtimeout=600;
		
		Label lblTimeOut = new Label(grpOptions, SWT.RIGHT);
		lblTimeOut.setText("VALIDATION TIME OUT:");
		lblTimeOut.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblTimeOut.setBounds(277, 178, 176, 17);
		
		Button btnSet = new Button(grpOptions, SWT.NONE);
		
		btnSet.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i=0;
				try {
					i=Integer.parseInt(text_4.getText());
				} catch (NumberFormatException n) {
					Component frame = null;
					JOptionPane.showMessageDialog(frame, "Invalid Input. Integer value required for timeout.");
					return;
				}
				
				if(i<0) {
					Component frame = null;
					JOptionPane.showMessageDialog(frame, "Invalid Input. Timeout must be a nonnegative integer.");
					return;
				}
				Mtimeout=i;
				text_5.setText(Integer.toString(i*60));
				
			}
		});
		btnSet.setBounds(566, 174, 75, 25);
		btnSet.setText("SET");
		
		Label lblMinutes = new Label(grpOptions, SWT.CENTER);
		lblMinutes.setAlignment(SWT.LEFT);
		lblMinutes.setText("MIN");
		lblMinutes.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblMinutes.setBounds(523, 178, 28, 17);
		
		Label lblValidationCountdown = new Label(grpOptions, SWT.RIGHT);
		lblValidationCountdown.setText("VALIDATION COUNTDOWN:");
		lblValidationCountdown.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblValidationCountdown.setBounds(10, 178, 175, 17);
		
		Group group_3 = new Group(grpOptions, SWT.NONE);
		group_3.setBounds(10, 61, 392, 39);
		
		Label label_5 = new Label(group_3, SWT.NONE);
		label_5.setText("Scope:");
		label_5.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		label_5.setBounds(10, 11, 132, 24);
		
		Button button_7 = new Button(group_3, SWT.RADIO);
		button_7.setText("ALL");
		button_7.setSelection(true);
		button_7.setBounds(239, 11, 49, 24);
		
		Button button_8 = new Button(group_3, SWT.RADIO);
		button_8.setBounds(294, 11, 13, 24);
		
		text_6 = new Text(group_3, SWT.BORDER);
		text_6.setText("0");
		text_6.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		text_6.setBounds(315, 11, 54, 24);
		
		Group group_4 = new Group(grpOptions, SWT.NONE);
		group_4.setBounds(10, 16, 392, 39);
		
		Label lblDestinationIsDicom = new Label(group_4, SWT.NONE);
		lblDestinationIsDicom.setText("Destination is DICOM ARCHIVE:");
		lblDestinationIsDicom.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblDestinationIsDicom.setBounds(10, 11, 202, 24);
		
		Label lblTargetAetitle = new Label(grpOptions, SWT.NONE);
		lblTargetAetitle.setText("TARGET AETITLE:");
		lblTargetAetitle.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblTargetAetitle.setBounds(10, 109, 98, 17);
		lblTargetAetitle.setVisible(false);
		
		text_7 = new Text(grpOptions, SWT.BORDER);
		//text_7 = new Combo(grpOptions, SWT.READ_ONLY);
		text_7.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		text_7.setBounds(115, 106, 287, 25);
		text_7.setVisible(false);
		text_7.setText("");
		
		Button button_11 = new Button(composite, SWT.CENTER);
		Button button_12 = new Button(composite, SWT.NONE);
		Group grpQueueData = new Group(grpOptions, SWT.NONE);
		Button btnREF = new Button(grpQueueData, SWT.NONE);
		Button button_13 = new Button(grpQueueData, SWT.NONE);
		button_13.setVisible(false);
		Button btnYes = new Button(group_4, SWT.RADIO);
		btnYes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				combo_1.setVisible(false);
				label_4.setVisible(false);
				lblTimeOut.setText("DELAY BETWEEN MOVES:");
				lblValidationCountdown.setText("NEXT MOVE:");
				lblTargetAetitle.setVisible(true);
				text_7.setVisible(true);
				btnREF.setVisible(false);
				button_13.setVisible(true);
				text_10.setText("");
				text_11.setText("");
				text_12.setText("");
				text_13.setText("");
				text_14.setText("");
			}
		});
		btnYes.setText("YES");
		btnYes.setSelection(false);
		btnYes.setBounds(239, 11, 49, 24);
		
		Button btnNo = new Button(group_4, SWT.RADIO);
		btnNo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				combo_1.setVisible(true);
				label_4.setVisible(true);
				lblTimeOut.setText("VALIDATION TIME OUT:");
				lblValidationCountdown.setText("VALIDATION COUNTDOWN:");
				lblTargetAetitle.setVisible(false);
				text_7.setVisible(false);
				btnREF.setVisible(true);
				button_13.setVisible(false);
				text_10.setText("");
				text_11.setText("");
				text_12.setText("");
				text_13.setText("");
				text_14.setText("");
			}
		});
		btnNo.setText("NO");
		btnNo.setSelection(true);
		btnNo.setBounds(294, 11, 39, 24);
		
		Label lblS = new Label(grpOptions, SWT.CENTER);
		lblS.setAlignment(SWT.LEFT);
		lblS.setText("SEC");
		lblS.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblS.setBounds(253, 178, 28, 17);
		
		
		grpQueueData.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
		grpQueueData.setText("Queue Data");
		grpQueueData.setBounds(408, 16, 310, 144);
		
		Label lblQueueId = new Label(grpQueueData, SWT.NONE);
		lblQueueId.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblQueueId.setBounds(10, 18, 62, 17);
		lblQueueId.setText("Added:");
		
		Label lblAdded = new Label(grpQueueData, SWT.NONE);
		lblAdded.setText("Failed:");
		lblAdded.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblAdded.setBounds(10, 43, 48, 17);
		
		Label lblFailed = new Label(grpQueueData, SWT.NONE);
		lblFailed.setText("Count:");
		lblFailed.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblFailed.setBounds(10, 68, 42, 17);
		
		Label lblCount = new Label(grpQueueData, SWT.NONE);
		lblCount.setText("Delivering:");
		lblCount.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblCount.setBounds(10, 93, 62, 17);
		
		Label lblScheduled = new Label(grpQueueData, SWT.NONE);
		lblScheduled.setText("Scheduled:");
		lblScheduled.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		lblScheduled.setBounds(10, 118, 62, 17);
		
		btnREF.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text_10.setText("");
				text_11.setText("");
				text_12.setText("");
				text_13.setText("");
				text_14.setText("");
				String str = combo_1.getText();
				class twoThread implements Runnable {
					public void run() {
						apc.getQueueInfo(str);
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								text_10.setText(apc.qinfo[0]);
								text_11.setText(apc.qinfo[1]);
								text_12.setText(apc.qinfo[2]);
								text_13.setText(apc.qinfo[3]);
								text_14.setText(apc.qinfo[4]);
							}
					});
					}
				}
				Thread t2 = new Thread(new twoThread());
				t2.start();
			}
		});
		btnREF.setBounds(234, 114, 66, 25);
		btnREF.setText("REFRESH");
		btnREF.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		
		text_10 = new Text(grpQueueData, SWT.BORDER);
		text_10.setEditable(false);
		text_10.setBounds(84, 16, 144, 21);
		
		text_11 = new Text(grpQueueData, SWT.BORDER);
		text_11.setEditable(false);
		text_11.setBounds(84, 41, 144, 21);
		
		text_12 = new Text(grpQueueData, SWT.BORDER);
		text_12.setEditable(false);
		text_12.setBounds(84, 66, 144, 21);
		
		text_13 = new Text(grpQueueData, SWT.BORDER);
		text_13.setEditable(false);
		text_13.setBounds(84, 91, 144, 21);
		
		text_14 = new Text(grpQueueData, SWT.BORDER);
		text_14.setEditable(false);
		text_14.setBounds(84, 116, 144, 21);
		
		button_13.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text_10.setText("");
				text_11.setText("");
				text_12.setText("");
				text_13.setText("");
				text_14.setText("");
				String str = text_7.getText();
				class twoThread implements Runnable {
					public void run() {
						apc.getQueueInfo("DICOM");
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								text_10.setText(apc.qinfo[0]);
								text_11.setText(apc.qinfo[1]);
								text_12.setText(apc.qinfo[2]);
								text_13.setText(apc.qinfo[3]);
								text_14.setText(apc.qinfo[4]);
							}
					});
					}
				}
				Thread t2 = new Thread(new twoThread());
				t2.start();
			}
		});
		button_13.setText("REFRESH");
		button_13.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		button_13.setBounds(234, 114, 66, 25);
		
		tableViewer2 = new TableViewer(composite_1, SWT.MULTI |SWT.BORDER | SWT.FULL_SELECTION);
		createColumns(composite_1, tableViewer2);
		final Table table2 = tableViewer2.getTable();
		table2.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
		table2.setSize(688, 177);
		table2.setLocation(30, 277);
		table.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		Dtimeout=10;
		
		
		fd_btnCancel.right = new FormAttachment(button_11, -59);
		button_11.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(MainWindow.MVOpType.equals("")) {
					MainWindow.MVOpType="DELETE";
					apc.ManualValidate(combo_1.getText(), window,"DELETE");
				} else {
					logger.debug("Manual validation aborted due to running validation task.");
					Component frame = null;
					JOptionPane.showMessageDialog(frame, "A manual validation task is running. Please wait for it to complete.");
				}
			}
		});
		button_11.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		button_11.setText("MANUAL VALIDATE");
		FormData fd_button_11 = new FormData();
		fd_button_11.bottom = new FormAttachment(lblInsertStudyUids_1, -11);
		button_11.setLayoutData(fd_button_11);
		
		fd_button_11.right = new FormAttachment(button_12, -6);
		button_12.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(MainWindow.MVOpType.contains("DELETE")) {
					MainWindow.MVKill=true;
					logger.debug("Manual validation aborted due to user action.");
					Component frame = null;
					JOptionPane.showMessageDialog(frame, "Manual Validate of Delete Cancelled.");
				}
			}
		});
		button_12.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		button_12.setText("X");
		FormData fd_button_12 = new FormData();
		fd_button_12.left = new FormAttachment(0, 705);
		fd_button_12.right = new FormAttachment(100, -15);
		fd_button_12.bottom = new FormAttachment(lblInsertStudyUids_1, -11);
		button_12.setLayoutData(fd_button_12);
		
		
		fd_btnCancel.top = new FormAttachment(group, 6);
		FormData fd_group = new FormData();
		fd_group.right = new FormAttachment(button_6, 710);
		fd_group.left = new FormAttachment(button_6, 0, SWT.LEFT);
		fd_group.top = new FormAttachment(0, 10);
		fd_group.bottom = new FormAttachment(0, 215);
		group.setLayoutData(fd_group);
		group.setText("Options");
		group.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		
		Label label = new Label(group, SWT.NONE);
		label.setText("Rejection Note Code:");
		label.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		label.setBounds(20, 28, 132, 24);
		
		Label label_1 = new Label(group_1, SWT.NONE);
		label_1.setText("Delete RIS Data:");
		label_1.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		label_1.setBounds(10, 10, 132, 24);
		
		button_1.setText("YES");
		button_1.setBounds(148, 10, 49, 24);
		
		Button button_2 = new Button(group_1, SWT.RADIO);
		button_2.setText("NO");
		button_2.setSelection(true);
		button_2.setBounds(203, 10, 49, 24);
		
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String str = combo.getText();
				if(str.contains("NO_IOCM")) {
					group_1.setVisible(true);
				} else {
					group_1.setVisible(false);
				}
			}
		});
		combo.setItems(new String[] {"113037", "110514", "113001", "113038", "113039", "REVOKE_REJECTION", "NO_IOCM"});
		combo.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		combo.setBounds(158, 28, 216, 25);
		combo.setText("NO_IOCM");
		
		group_2.setBounds(10, 103, 364, 39);
		
		Label label_2 = new Label(group_2, SWT.NONE);
		label_2.setText("Scope:");
		label_2.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		label_2.setBounds(10, 11, 132, 24);
		
		button_3.setText("ALL");
		button_3.setSelection(true);
		button_3.setBounds(148, 11, 49, 24);
		
		Button button_4 = new Button(group_2, SWT.RADIO);
		button_4.setBounds(203, 11, 13, 24);
		
		text_2 = new Text(group_2, SWT.BORDER);
		text_2.setText("0");
		text_2.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		text_2.setBounds(222, 10, 54, 24);
		
		Label label_3 = new Label(group, SWT.RIGHT);
		label_3.setText("VALIDATION COUNTDOWN:");
		label_3.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		label_3.setBounds(44, 174, 164, 17);
		
		text_8 = new Text(group, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
		text_8.setText("600");
		text_8.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		text_8.setBounds(214, 171, 33, 23);
		
		Label label_6 = new Label(group, SWT.CENTER);
		label_6.setText("SEC");
		label_6.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		label_6.setAlignment(SWT.LEFT);
		label_6.setBounds(253, 174, 22, 17);
		
		Label label_8 = new Label(group, SWT.RIGHT);
		label_8.setText("VALIDATION TIME OUT:");
		label_8.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		label_8.setBounds(301, 174, 138, 17);
		
		text_9 = new Text(group, SWT.BORDER);
		text_9.setText("10");
		text_9.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		text_9.setBounds(445, 171, 38, 23);
		
		Label label_9 = new Label(group, SWT.CENTER);
		label_9.setText("MIN");
		label_9.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		label_9.setAlignment(SWT.LEFT);
		label_9.setBounds(490, 174, 25, 17);
		
		Button button_10 = new Button(group, SWT.NONE);
		button_10.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i=0;
				try {
					i=Integer.parseInt(text_9.getText());
				} catch (NumberFormatException n) {
					Component frame = null;
					JOptionPane.showMessageDialog(frame, "Invalid Input. Integer value required for timeout.");
					return;
				}
				
				if(i<0) {
					Component frame = null;
					JOptionPane.showMessageDialog(frame, "Invalid Input. Timeout must be a nonnegative integer.");
					return;
				}
				Dtimeout=i;
				text_8.setText(Integer.toString(i*60));				
			}
		});
		button_10.setText("SET");
		button_10.setBounds(521, 170, 52, 25);
		FormData fd_table2 = new FormData();
		fd_table2.left = new FormAttachment(0, 15);
		fd_table2.bottom = new FormAttachment(100, -7);
		fd_table2.right = new FormAttachment(100, -15);
		table2.setLayoutData(fd_table2);
		table2.setHeaderVisible(true);
		table2.setLinesVisible(true);
		
		Button button_9 = new Button(composite_1, SWT.NONE);
		button_9.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				apc.Status_Update(window,"MOVE");
			}
		});
		button_9.setText("REFRESH");
		button_9.setFont(SWTResourceManager.getFont("Segoe UI", 10, SWT.NORMAL));
		button_9.setBounds(30, 225, 106, 25);
		
		Button btnNewButton_1 = new Button(composite_1, SWT.CENTER);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("Starting new move task.");
				btnSet.setVisible(false);
				int i;
				String dcmarchive,dest;
				if(button_7.getSelection()) {
					i=-1;
				} else {
					try {
						i=Integer.parseInt(text_6.getText());
					} catch (NumberFormatException n) {
						Component frame = null;
						JOptionPane.showMessageDialog(frame, "Invalid Input. Integer value required for custom scope.");
						logger.debug("Invalid custom scope entered.");
						return;
					}
				}
				Boolean go=true;
				if(btnYes.getSelection()) {
					dcmarchive = "true";
					dest = text_7.getText();
					if(dest.length()==0) {go=false;}
				} else {
					dcmarchive = "false";
					dest = combo_1.getText();
				}
				
				Component frame = null;
				int n = JOptionPane.showConfirmDialog(frame, "Are You Sure?","Confirm Study Move", JOptionPane.YES_NO_OPTION);
				if(n==0) {
					if(MainWindow.running) {
						logger.debug("Move task aborted due to running task.");
						JOptionPane.showMessageDialog(frame, "A delete or move task is already running. Please wait for it to complete.");
					} else if(!go) {
						logger.debug("No Dicom Archive available. Exiting task.");
						JOptionPane.showMessageDialog(frame, "No DICOM Archive Specified. Move Cancelled.");
					} else {
						Map<String, String> options = new HashMap<String,String>();
						options.put("dcmarchive", dcmarchive);
						options.put("dest", dest);
						apc.StartTask(i, window, pdb.amt_con, Mtimeout, options, "MOVE");
					}
				}
				btnSet.setVisible(true);			
			}
		});
		btnNewButton_1.setBounds(221, 226, 106, 25);
		btnNewButton_1.setText("EXECUTE");
		
		Button btnCancel_1 = new Button(composite_1, SWT.CENTER);
		btnCancel_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(MainWindow.running&&MainWindow.runningOpType.contains("MOVE")) {
					MainWindow.kill=true;
					Component frame = null;
					JOptionPane.showMessageDialog(frame, "Routine will exit after timeout period expires unless study move validates first.");
					logger.debug("Move task aborted by user action.");
				}

			}
		});
		btnCancel_1.setBounds(333, 226, 106, 25);
		btnCancel_1.setText("CANCEL");
		
		Button btnValidate = new Button(composite_1, SWT.CENTER);
		btnValidate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(MainWindow.MVOpType.equals("")) {
					if(btnYes.getSelection()) {
						Component frame = null;
						JOptionPane.showMessageDialog(frame, "Validation of moves to Dicom SR storage via API call not implemented at this time.");
						return;
					}
					logger.debug("Beginning manual validation of move task to "+combo_1.getText());
					String s = combo_1.getText();
					apc.ManualValidate(s, window, "MOVE");
				} else {
					Component frame = null;
					JOptionPane.showMessageDialog(frame, "A manual validation task is running. Please wait for it to complete.");
					logger.debug("Manual validation aborted due to running validation task.");
				}
				
			}
		});
		btnValidate.setText("MANUAL VALIDATE");
		btnValidate.setBounds(541, 225, 140, 25);
		
		Label lblInsertStudyUids = new Label(composite_1, SWT.NONE);
		lblInsertStudyUids.setText("Insert Study UIDs to be moved into table: ei_api.study_move_uids, status column must contain word Ready (case insensitive)");
		lblInsertStudyUids.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.NORMAL));
		lblInsertStudyUids.setBounds(44, 254, 659, 17);
		
		Button btnNewButton_2 = new Button(composite_1, SWT.NONE);
		btnNewButton_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(MainWindow.MVOpType.contains("MOVE")) {
					MainWindow.MVKill=true;
					Component frame = null;
					JOptionPane.showMessageDialog(frame, "Manual Validate of Move Cancelled.");
					logger.debug("Manual validation aborted due to user action.");
				}
			}
		});
		btnNewButton_2.setBounds(687, 225, 31, 25);
		btnNewButton_2.setText("X");
		
		tableViewer.setContentProvider(new ArrayContentProvider());
		apc.Status_Update(window,"DELETE");
		tableViewer.setInput(apc.list);
		tableViewer.getTable().getColumn(0).pack();
		tableViewer.getTable().getColumn(1).pack();
		tableViewer.getTable().getColumn(2).pack();
		
		tableViewer2.setContentProvider(new ArrayContentProvider());
		apc.Status_Update(window,"MOVE");
		tableViewer2.setInput(apc.list);
		tableViewer2.getTable().getColumn(0).pack();
		tableViewer2.getTable().getColumn(1).pack();
		tableViewer2.getTable().getColumn(2).pack();

	}
	
	private void createColumns(final Composite parent, final TableViewer viewer) {
        String[] titles = { "Status","Studies","Validated" };
        int[] bounds = { 100, 100, 100};

        // first column is for the status
        TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0,viewer);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                SDPair p = (SDPair) element;
                return p.getStatus();
            }
        });

        // second column is for the count
        col = createTableViewerColumn(titles[1], bounds[1], 1,viewer);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                SDPair p = (SDPair) element;
                return Integer.toString(p.getStudies());
            }
        });
        
        // third column is for the validation status
        col = createTableViewerColumn(titles[2], bounds[2], 2,viewer);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                SDPair p = (SDPair) element;
                return p.getValidated();
            }
        });

    }

    private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber,TableViewer tbv) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(tbv, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(600);
        column.setResizable(true);
        column.setMoveable(true);
        return viewerColumn;
    }
    
	protected DataBindingContext initDataBindings() {
		DataBindingContext bindingContext = new DataBindingContext();
		//
		return bindingContext;
	}
}
