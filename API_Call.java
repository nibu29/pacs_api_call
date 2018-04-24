package com.agfa.med.EIPS;

import java.awt.Component;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

public class API_Call {
	private Connection con;
	public String key = new String("");
	public String testresult;
	public Map<String,Integer> active_cons;
	public ArrayList<SDPair> list;
	public String[] eis,eida,qinfo;
	public int cntSec;
	final static Logger logger = Logger.getLogger(API_Call.class);
	public double f1;
	
	public API_Call (PGdbconnector p) {
		this.con=p.amt_con;

	}

	public void get_Key() {
		String keyarg = " -k -s -L -X POST https://"+MainWindow.FQDN+"/authentication/token -H \'content-type: application/x-www-form-urlencoded\' -d \'j_username="+
				MainWindow.username+"\' -d \'j_password="+MainWindow.password+"\' -b tempCookieFile";
		try {
			if(this.key.length()==0) {
				logger.debug("Getting access key from EI. Curl arguments: "+keyarg);
				Process p = Runtime.getRuntime().exec(MainWindow.curlloc+keyarg);
				Scanner s = new Scanner(p.getInputStream()).useDelimiter("//A");
				String k = s.hasNext() ? s.next() : "";
				s.close();
				this.key=k;	
			} 
			
			if(this.key.contains("<value>")) {
				logger.debug("Parsing key from response.");
				this.key=key.substring(key.indexOf("<value>")+7, key.indexOf("</value>"));
			}
			
			
		} catch (Exception e) {
			logger.error("Error running curl command to get key: ",e);
		}
	}
	
	public void run_Test() {
		logger.debug("Running credential test.");
		String testarg = " -k -s -L -X POST https://"+MainWindow.FQDN+"/authentication/token -H \'content-type: application/x-www-form-urlencoded\' -d \'j_username="+
		MainWindow.username+"\' -d \'j_password="+MainWindow.password+"\' -b tempCookieFile";
		logger.debug("Curl arguments: "+testarg);
		try {
			Process p = Runtime.getRuntime().exec(MainWindow.curlloc+testarg);
			Scanner s = new Scanner(p.getInputStream()).useDelimiter("//A");
			String k = s.hasNext() ? s.next() : "";
			this.key=k;
			logger.debug("Response to curl: "+k);
			s.close();
			if(k.length()==0) {
				this.testresult = "Invalid FQDN / System unreachable";
			} else if (k.contains("<token><value>")) {
				this.testresult = "SUCCESS";
			} else {
				this.testresult = "Incorrect Username or Password";
			}
		} catch (Exception e) {
			logger.error("Error running curl command to test credentials: ",e);
		}
	}
	
	public void pop_EIS(Boolean i) {
		java.lang.String stArg;
		java.lang.String s1,s2="";
		java.lang.String sType="";
		java.lang.String sPoolId="";
		java.lang.String sName="";
		Statement amt_stmt;
		ArrayList<String> SID = new ArrayList<String>();
		int ActiveCons=0;
		active_cons = new HashMap<String,Integer>();
		this.get_Key();
		logger.debug("Querying storage locations.");
		stArg = " -k -X GET --header \'Accept: application/json\' \'https://"+MainWindow.FQDN+"/configuration/v1/storagePools?token="+this.key+"&includeInactive=false\'";
		logger.debug("Curl command: "+stArg);
		try {
			amt_stmt = this.con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			amt_stmt.executeUpdate("TRUNCATE TABLE EI_API.EI_STORAGE");
			logger.debug("database query: TRUNCATE TABLE EI_API.EI_STORAGE");
			Process p = Runtime.getRuntime().exec(MainWindow.curlloc+stArg);
			Scanner s = new Scanner(p.getInputStream()).useDelimiter(",\\s*");
			boolean b = false;
			int typewatcher=0,poolwatcher=0,namewatcher=0;
			while(s.hasNext()) {
				s1 = s.next();
				logger.debug("Curl response: "+s1);
				if(s1.contains("\"type\":")) {
					typewatcher = s1.indexOf("\"type\":");
					sType=s1.substring(typewatcher+8, s1.length()-1);
				}
				if(s1.contains("\"poolId\":")) {
					poolwatcher = s1.indexOf("\"poolId\":");
					sPoolId=s1.substring(poolwatcher+10, s1.length()-1);
				}
				if(s1.contains("\"name\":")) {
					namewatcher=s1.indexOf("\"name\":");
					sName=s1.substring(namewatcher+8, s1.length()-1);
					b = true;
				}
				if(b) {
					logger.debug("Parsing active shares by checking for inUse:true");
					while(!s2.contains("\"affinities\":")) {
						s2=s.next();
						if(s2.contains("\"inUse\":true")) {
							ActiveCons++;
						}
					}
					s2 = "";
					active_cons.put(sType+", "+sName, ActiveCons);
					ActiveCons=0;
					String sql = "INSERT INTO EI_API.EI_STORAGE(TYPE,NAME,POOL_ID) VALUES(\'"+sType+"\', \'"+sName+"\', \'"+sPoolId+"\')";
					logger.debug("Inserting value into database command: "+sql);
					amt_stmt.executeUpdate(sql);
					SID.add(sType+", "+sName);
					b = false;
				}
			}
			amt_stmt.close();
			s.close();
			this.eis=SID.toArray(new String[SID.size()]);
			if(i) {
				this.pop_archives();
			}		
		} catch (Exception e) {
			logger.fatal("Error populating storage locations: ",e);
		}
	}
	
	public void get_FS(String str){
		String pid="";
		f1=0.0;
		
		this.get_Key();
		try {
			logger.debug("Getting free space for storage: "+str);
			Statement amt_stmt = this.con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = amt_stmt.executeQuery("SELECT TYPE||\', \'||NAME, POOL_ID FROM EI_API.ei_storage");
			logger.debug("Querying database for storage details: SELECT TYPE||', '||NAME, POOL_ID FROM EI_API.ei_storage");
			if(rs.next()) 
			{
				rs.beforeFirst();
				while(rs.next()) 
				{
					if(str.equals(rs.getString(1))) 
					{
						pid = rs.getString(2);
					}
				}	
			}
			String storArg=" -k -X GET --header \'Accept: application/json\' \'https://"+MainWindow.FQDN+"/configuration/v1/storagePools/"+pid+"/usableSpace?token="+key+"&detailLevel=STANDARD\'";
			logger.debug("Curl command to get free space: "+storArg);
			Process p;
			try {
				p = Runtime.getRuntime().exec(MainWindow.curlloc+storArg);
				Scanner s = new Scanner(p.getInputStream());
				while(s.hasNextLine()) {
					String s1 = s.nextLine();
					logger.debug("Parsing text result for usableSpace keyword and converting to GB: "+s1);
					f1 = Float.parseFloat(s1.substring(s1.indexOf("\"usableSpace\":")+14,s1.length()-1).trim())/100000000;
					amt_stmt.executeUpdate("UPDATE EI_API.EI_STORAGE SET LAST_FS="+Double.toString(f1)+" where pool_id=\'"+pid+"\'");
					logger.debug("Updating database with result.");
				}
				rs.close();
				amt_stmt.close();
				s.close();
			} catch (IOException e) {
				logger.error("Could not parse float from curl response: ",e);
			}

		} catch (SQLException e) {
			logger.error("DB error when getting free space: ",e);
		}
	}
		
	public void Status_Update(MainWindow mw,String operation) {
		list = new ArrayList<SDPair>();
		try {
			Connection con = DriverManager.getConnection(MainWindow.db);
			Statement amt_stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = amt_stmt.executeQuery("SELECT STATUS, COUNT(*), VALIDATED FROM EI_API.STUDY_"+operation+"_UIDS GROUP BY STATUS,VALIDATED ORDER BY 2 DESC");
			if(rs.next()) {
				rs.beforeFirst();
				while(rs.next()) {
					list.add(new SDPair(rs.getString(1),rs.getInt(2),rs.getString(3)));
				}
			}
			if(operation.contains("DELETE")) {
				mw.tableViewer.setInput(list);
				mw.tableViewer.getTable().getColumn(0).pack();
				mw.tableViewer.getTable().getColumn(1).pack();
				mw.tableViewer.getTable().getColumn(2).pack();
			} else {
				mw.tableViewer2.setInput(list);
				mw.tableViewer2.getTable().getColumn(0).pack();
				mw.tableViewer2.getTable().getColumn(1).pack();
				mw.tableViewer2.getTable().getColumn(2).pack();
			}
			
			con.close();
		} catch (SQLException e) {
			logger.error("SQLException when querying db: ",e);
		}
	}

	public void StartTask(int i,MainWindow mw,Connection con,int timeout,Map<String,String> options,String operation) {
		this.get_Key();
		logger.debug("Starting "+operation+" studies job.");
		logger.debug("Creating new thread so that main thread can keep updating GUI.");
		class oneThread implements Runnable {
			String key,operation;
			MainWindow mw;
			Connection con;
			int i,timeout;
			Map<String,String> options;
			oneThread(int i,MainWindow mw,Connection con,int timeout, String key,Map<String,String> options,String operation) {
				this.i=i;
				this.mw=mw;
				this.con=con;
				this.timeout=timeout;
				this.key=key;
				this.options=options;
				this.operation=operation;
			}
			public void run() {
				Statement amt_stmt;
				ResultSet rs;
				String curlArg="";
				String destPID="";
				MainWindow.running=true;
				Boolean isMove=operation.contains("MOVE");
				MainWindow.runningOpType=operation;
				try {
					amt_stmt = this.con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
					if(isMove) {
						if(options.get("dcmarchive").contains("false")) {
							ResultSet rs2 = amt_stmt.executeQuery("SELECT POOL_ID FROM EI_API.EI_STORAGE WHERE NAME=\'"+options.get("dest")+"\'");
							rs2.next();
							destPID = rs2.getString(1);
							rs2.close();	
						} else {
							destPID=options.get("dest");
						}

					}

					if(i<0) {
						rs = amt_stmt.executeQuery("SELECT UID,STATUS,VALIDATED,ID FROM EI_API.STUDY_"+operation+"_UIDS WHERE UPPER(STATUS) SIMILAR TO \'%READY%\'");
						logger.debug("Retrieving candidates for "+operation+". Query: SELECT UID,STATUS,VALIDATED,ID FROM EI_API.STUDY_"+operation+"_UIDS WHERE UPPER(STATUS) SIMILAR TO '%READY%");
					} else {
						rs = amt_stmt.executeQuery("SELECT UID,STATUS,VALIDATED,ID FROM EI_API.STUDY_"+operation+"_UIDS WHERE UPPER(STATUS) SIMILAR TO \'%READY%\' LIMIT "+i);
						logger.debug("Retrieving candidates for "+operation+". Query: SELECT UID,STATUS,VALIDATED,ID FROM EI_API.STUDY_"+operation+"_UIDS WHERE UPPER(STATUS) SIMILAR TO '%READY% LIMIT "+i);
					}
					if(rs.next()) {
						rs.beforeFirst();
						while(rs.next()) {
							String uid = rs.getString(1);
							logger.debug("Processing study uid: "+uid);
							if(isMove) {
								curlArg = " -k -X PUT --header \'Accept: application/json\' \'https://"+MainWindow.FQDN+"/pacs/v1/study/storeToArchive?token="+this.key+"&studyUid="+uid+
								"&targetId="+destPID+"&isDicomArchive="+options.get("dcmarchive")+"\' -w \'\\t%{http_code}\'";
							} else {
								String iocm = options.get("iocm");
								if(iocm.contains("RIS_DELETE")) {
									curlArg = " -k -X DELETE --header \'Accept: application/json\' \'https://"+MainWindow.FQDN+"/ris/web/study/delete?studyUID={"+uid+"}&token="+this.key+"\' -w \'\\t%{http_code}\'";
								} else {
									curlArg = " -k -X DELETE --header \'Accept: application/json\' \'https://"+MainWindow.FQDN+"/pacs/v1/study?token="+this.key+"&studyUid="+uid+"&rejectionNoteCode="
									+iocm+"&deleteRisData="+options.get("deleteris")+"\' -w \'\\t%{http_code}\'";
								}
							}
							logger.debug("curl command arguments: "+curlArg);
							
							if(!MainWindow.kill) {
								Process p;
								try {
									p = Runtime.getRuntime().exec(MainWindow.curlloc+curlArg);
									Scanner s = new Scanner(p.getInputStream()).useDelimiter("\\A");
									while(s.hasNext()) {
										String s1 = s.next();
										logger.debug("Response message: "+s1);
										if(s1.contains("204")) {
											if(isMove) {
												if(options.get("dcmarchive").contains("false")) {
													rs.updateString(2, "MOVE INDICATED TO "+options.get("dest"));
													rs.updateRow();
												} else {
													rs.updateString(2, "STORE JOB CREATED TO DICOM ARCHIVE "+options.get("dest"));
													rs.updateRow();
												}
											} else {
												if(options.get("iocm").contains("RIS_DELETE")) {
													rs.updateString(2, s1.trim());
													rs.updateRow();
												} else {
													rs.updateString(2, "DELETE INDICATED");
													rs.updateRow();
												}
											}
										} else {
											rs.updateString(2, s1.trim());
											rs.updateRow();
										}
										
										Display.getDefault().asyncExec(new Runnable() {
											 public void run() {
												 mw.apc.Status_Update(mw,operation);
											 }
										});
										}
									s.close();
									} catch (IOException e) {
										logger.error("Error processing task: ",e);
									}
							}
							
							logger.debug("Begin validation (delay) loop."); 
							cntSec=0;
							if(isMove) {
								MainWindow.SMVal=false;
							} else {
								MainWindow.SDVal=false;
							}
								
							Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										if(isMove) {
											mw.text_5.setText(Integer.toString((timeout*60)-cntSec));
										} else {
											mw.text_8.setText(Integer.toString((timeout*60)-cntSec));
										}
										 
									}
							});
							//break out if cancel has been called
							if(MainWindow.kill) {
								MainWindow.kill=false;
								MainWindow.running=false;
								MainWindow.runningOpType="";
								logger.debug("Task cancelled by user.");
								Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											mw.apc.Status_Update(mw,operation);
											Component frame = null;
											if(isMove) {
												mw.text_5.setText(Integer.toString(timeout*60));
												JOptionPane.showMessageDialog(frame, "Move Cancelled.");
											} else {
												mw.text_8.setText(Integer.toString(timeout*60));
												JOptionPane.showMessageDialog(frame, "Delete Cancelled.");
											}											 											 
										}
								});
								return;	
							}
							//timer loop
							
							Boolean doVal;
							if(isMove) {
								doVal=options.get("dcmarchive").contains("false");
								if(!doVal) {
									logger.debug("Auto validation disabled as move to dicom archive has been selected.");
								}
							} else {
								doVal = true;
							}
							
							logger.debug("Validation interval is 1 minute.");
							logger.debug("New thread(s) spawned for validation.");
							while((!MainWindow.SMVal&&isMove)||(!MainWindow.SDVal&&!isMove)) {
								//validate every minute with new thread for internal moves
								if(doVal&&(cntSec % 60 == 0)) {
									class twoThread implements Runnable {
										String dest,uid;
										MainWindow mw;
										twoThread(MainWindow mw,String dest,String uid) {
											this.mw=mw;
											this.dest=dest;
											this.uid=uid;
										}
										public void run() {
											Boolean [] vals =mw.apc.validate(uid, dest);
											if(isMove) {
												MainWindow.SMVal=vals[0];
												if(MainWindow.SMVal) {
													logger.debug("Validation successful. Updating validated column and moving on.");
													try {
														String s = rs.getString(3);
														if(s.contains("NOT VALIDATED")) {
															rs.updateString(3, "VALIDATED ON "+dest);
															rs.updateRow();
														} 
													} catch (SQLException e) {
														logger.error("Error writing to db in main task validation loop: ",e);
													}
												}
											} else {
												MainWindow.SDVal=vals[1];
												if(MainWindow.SDVal) {
													try {
														logger.debug("Validation successful. Updating validated column and moving on.");
														rs.updateString(3,"DELETION FROM ALL LOCATIONS VALIDATED");
														rs.updateRow();
													} catch (SQLException e) {
														logger.error("Error writing to db: ",e);
													}
												}
											}
												
										}
									}
										Thread t2 = new Thread(new twoThread(mw,options.get("dest"),uid));
										t2.start();
									}

									Thread.sleep(1000);
									cntSec++;
									//validation (delay) timer expired, time to move on
									if(cntSec>(timeout*60)) {
										if(MainWindow.kill) {
											logger.debug("Task cancelled by user action.");
											MainWindow.kill=false;
											MainWindow.running=false;
											MainWindow.runningOpType="";
											Display.getDefault().asyncExec(new Runnable() {
												 public void run() {
													 mw.apc.Status_Update(mw,operation);
													 Component frame = null;
													 if(isMove) {
														 mw.text_5.setText(Integer.toString(timeout*60));
														 JOptionPane.showMessageDialog(frame, "Move Cancelled.");
													 } else {
														 mw.text_8.setText(Integer.toString(timeout*60));
														 JOptionPane.showMessageDialog(frame, "Delete Cancelled.");
													 }													 
												 }
											});
											return;	
										}
										logger.debug("Timer expired moving on.");
										break;
									}

									Display.getDefault().asyncExec(new Runnable() {
										 public void run() {
											 if(isMove) {
												 mw.text_5.setText(Integer.toString((timeout*60)-cntSec));
											 } else {
												 mw.text_8.setText(Integer.toString((timeout*60)-cntSec));
											 }											 
										 }
									});

								}
							}
					}
					rs.close();
					amt_stmt.close();
					} catch (SQLException | InterruptedException e) {
						logger.error("Problem with the validation loop: ",e);
					}
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							mw.apc.Status_Update(mw,operation);
							Component frame = null;
							if(isMove) {
								mw.text_5.setText(Integer.toString(timeout*60));
								JOptionPane.showMessageDialog(frame, "Move Completed.");
							} else {
								mw.text_8.setText(Integer.toString(timeout*60));
								JOptionPane.showMessageDialog(frame, "Delete Completed.");
							}						 
						}
					});
					MainWindow.running=false;
					MainWindow.runningOpType="";
					logger.debug("Task completed.");
				}
			}
			Thread t = new Thread(new oneThread(i,mw,this.con,timeout,this.key,options,operation));
			t.start();		
	}
	
	public Boolean[] validate(String uid,String dest) {
		this.get_Key();
		Boolean [] valResp= {false,false};
		String Arg = " -k -X GET --header \'Accept: application/json\' \'https://"+MainWindow.FQDN+"/pacs/v1/study/getStudyLocation?token="+this.key+"&studyUid="+uid+
		"\'";
		logger.debug("Getting locations for uid: "+uid);
		logger.debug("curl arguments: "+Arg);
		Process p;
		try {
			p = Runtime.getRuntime().exec(MainWindow.curlloc+Arg);
			Scanner s = new Scanner(p.getInputStream()).useDelimiter("\\\\A");
			while(s.hasNext()) {
				String s1 = s.next();
				logger.debug("Curl response: "+s1);
				String s2 = s1.substring(0, s1.indexOf("partialAvailability"));
				if(s2.contains("fullAvailability") && s2.contains(dest)) {
					logger.debug("Study fully available on: "+dest);
					s.close();
					valResp[0]=true;
					return valResp;
				} 
				if(s1.contains("fullAvailability\":[]")&&s1.contains("partialAvailability\":[]")) {
					logger.debug("Study not available on any storage location in this EI cluster.");
					s.close();
					valResp[1]=true;
					return valResp;
				}
			}
			s.close();
			return valResp;
		}
		 catch (Exception e) {
			logger.error("Problem with validate function: ",e);
			return valResp;
		}	
	}
	
	public void ManualValidate(String dest,MainWindow mw,String operation) {
		class oneThread implements Runnable {
			String dest;
			MainWindow mw;
			String operation;
			oneThread(String dest,MainWindow mw,String operation) {
				this.mw=mw;
				this.dest=dest;
				this.operation=operation;
			}
			public void run() {
				try {
					MainWindow.MVOpType=operation;
					logger.debug("Beginning manual validation for "+operation+" uid list.");
					Connection con = DriverManager.getConnection(MainWindow.db);
					Statement amt_stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
					ResultSet rs2;
					Boolean isMove = operation.contains("MOVE");
					logger.debug("Populating result set with information.");
					logger.debug("Query: SELECT UID,VALIDATED,ID FROM EI_API.STUDY_"+operation+"_UIDS WHERE VALIDATED=\'NOT VALIDATED\'");
					rs2 = amt_stmt.executeQuery("SELECT UID,VALIDATED,ID FROM EI_API.STUDY_"+operation+"_UIDS WHERE VALIDATED=\'NOT VALIDATED\'");
					if(rs2.next()) {
						rs2.beforeFirst();
						while(rs2.next()) {
							if(MainWindow.MVKill) {
								MainWindow.MVKill=false;
								MainWindow.MVOpType="";
								return;
							}
							String s = rs2.getString(1);
							logger.debug("Validating uid: "+s);
							Boolean[] val= mw.apc.validate(s,dest);
							if(val[0] && isMove) {
								logger.debug("Full availability validation successful. Updating validated column.");
								rs2.updateString(2,"VALIDATED ON "+dest);
								rs2.updateRow();
							} else if (val[1] && !isMove) {
								logger.debug("Deletion validation successful. Updating validated column.");
								rs2.updateString(2, "DELETION FROM ALL LOCATIONS VALIDATED");
								rs2.updateRow();
							} else if (val[1] && isMove) {
								logger.debug("Move uid is not on EI. Updating validated column.");
								rs2.updateString(2,"STUDY DOES NOT EXIST IN EI");
								rs2.updateRow();
							} 
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									mw.apc.Status_Update(mw,operation);
								}
							});
						}
					}

				} catch (SQLException e) {
					logger.error("Error updating db for manual validation: ",e);
				}
				MainWindow.MVOpType="";
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						Component frame = null;
						JOptionPane.showMessageDialog(frame, "Manual Validate Complete.");
						logger.debug("Manual validation of "+operation+" uid list complete.");
					}
				});
			}
		}
		Thread t = new Thread(new oneThread(dest,mw,operation));
		t.start();

	}
	
	public void pop_archives() {
		java.lang.String stArg;
		java.lang.String s1;
		java.lang.String sNAME="",sQID="";

		Statement amt_stmt;
		ArrayList<String> SID = new ArrayList<String>();
		this.get_Key();
		logger.debug("Querying storage archives.");
		stArg = " -k -X GET --header \'Accept: application/json\' \'https://"+MainWindow.FQDN+"/ris/web/v2/queues?token="+this.key+"\'";
		logger.debug("Curl command: "+stArg);
		try {
			amt_stmt = this.con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			amt_stmt.executeUpdate("TRUNCATE TABLE EI_API.EI_ARCHIVES");
			logger.debug("database query: TRUNCATE TABLE EI_API.EI_ARCHIVES");
			Process p = Runtime.getRuntime().exec(MainWindow.curlloc+stArg);
			Scanner s = new Scanner(p.getInputStream()).useDelimiter("},");
			boolean b = false;
			int typewatcher=0,typewatcher2=0,QIDwatcher=0,QIDwatcher2=0;
			while(s.hasNext()) {
				s1 = s.next();
				logger.debug("Curl response: "+s1);
				if(s1.contains("Clusteredstorage")) {
					typewatcher = s1.indexOf("\"destination\":");
					typewatcher2 = s1.indexOf("\"",typewatcher+15);
					sNAME=s1.substring(typewatcher+15, typewatcher2);
					QIDwatcher = s1.indexOf("\"queueId\":\"");
					QIDwatcher2 = s1.indexOf("\"",QIDwatcher+11);
					sQID=s1.substring(QIDwatcher+11, QIDwatcher2);
					if(!sNAME.contains("ull")) {
						b=true;
					}
				}
				if(b) {
					String sql = "INSERT INTO EI_API.EI_ARCHIVES (NAME,QID) VALUES(\'"+sNAME+"\',\'"+sQID+"\')";
					logger.debug("Inserting value into database command: "+sql);
					amt_stmt.executeUpdate(sql);
					SID.add(sNAME);
					b = false;
				}
			}
			amt_stmt.close();
			s.close();
			this.eida=SID.toArray(new String[SID.size()]);
		} catch (Exception e) {
			logger.fatal("Error populating archives: ",e);
		}
	}
	
	public void getQueueInfo(String dest){
		java.lang.String stArg;
		java.lang.String s1,qid="";
		Statement amt_stmt;
		
		qinfo = new String[5];
		this.get_Key();
		try {
			amt_stmt = this.con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			ResultSet rs = amt_stmt.executeQuery("SELECT QID,NAME FROM EI_API.EI_ARCHIVES");
			if(rs.next()) {
				rs.beforeFirst();
				while(rs.next()) {
					String s = rs.getString(2);
					logger.debug("Checking queue values for "+dest);
					if(dest.equals("DICOM")) {
						qid ="jms.queue.Clusteredstoreremember";
					} else if(s.equals(dest)){
						qid =rs.getString(1);
					} else if(dest.equals("DELETE")) {
						qid="jms.queue.ClustereddeleteFromInternal";
					} else if(dest.equals("SCHEDULER")) {
						qid="jms.topic.ClusteredJobScheduledNotifications";
					}
				}
				if(!qid.contains("jms.")) {
					logger.debug("No QueueID available. Aborting task.");
					return;
				}
				
				if(qid.contains("jms.topic.ClusteredJobScheduledNotifications")) {
					stArg= " -k -X GET --header \'Accept: application/json\' \'https://"+MainWindow.FQDN+"/ris/web/v2/queues/"+qid+"?token="+this.key+"\'&name=ClusteredJobScheduledNotifications";					
				} else {
					stArg = " -k -X GET --header \'Accept: application/json\' \'https://"+MainWindow.FQDN+"/ris/web/v2/queues/"+qid+"?token="+this.key+"\'";
				}
				
				logger.debug("Curl command: "+stArg);
				Process p = Runtime.getRuntime().exec(MainWindow.curlloc+stArg);
				Scanner s = new Scanner(p.getInputStream()).useDelimiter("//A");
				int tkr=0,tkr2=0;
				while(s.hasNext()) {
					s1 = s.next();
					logger.debug("Curl response: "+s1);
					tkr = s1.indexOf("\"messageAdded\":");
					tkr2 = s1.indexOf(",",tkr);
					qinfo[0]=s1.substring(tkr+15, tkr2);
					tkr = s1.indexOf("\"messageFailed\":");
					tkr2 = s1.indexOf(",",tkr);
					qinfo[1]=s1.substring(tkr+16, tkr2);
					tkr = s1.indexOf("\"messageCount\":");
					tkr2 = s1.indexOf(",",tkr);
					qinfo[2]=s1.substring(tkr+15, tkr2);
					tkr = s1.indexOf("\"deliveringCount\":");
					tkr2 = s1.indexOf(",",tkr);
					qinfo[3]=s1.substring(tkr+18, tkr2);
					tkr = s1.indexOf("\"scheduledCount\":");
					tkr2 = s1.indexOf(",",tkr);
					qinfo[4]=s1.substring(tkr+17, tkr2);
				}
				s.close();
				
			}
			amt_stmt.close();
		} catch (Exception e) {
			logger.fatal("Error getting queue details: ",e);
		}
	}
	
}



