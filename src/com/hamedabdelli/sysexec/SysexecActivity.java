package com.hamedabdelli.sysexec;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EmptyStackException;
import java.util.Stack;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SysexecActivity extends Activity {

    EditText cmd;
    TextView res;
    Button exec;
    StringBuilder resultat;
    Handler handler;
    ScrollView sc1;
    String line;
    File workingDir = Environment.getRootDirectory();
    int nbrLines = 0;
    int cmdNumber = 0;
    Stack<String> lastCmds = new Stack<String>();

    /**
    * Called when the activity is first created.
    */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        setTitle(workingDir.getAbsolutePath());
        
        handler = new Handler();

        sc1 = (ScrollView) findViewById(R.id.sc1);
        cmd = (EditText) findViewById(R.id.cmd);
        exec = (Button) findViewById(R.id.exec);
        res = (TextView) findViewById(R.id.res);

        res.setTextColor(Color.GREEN);

        cmd.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                //if(hasFocus){
                cmd.setText("");
                //}
            }
        });
        
        cmd.setOnKeyListener(new OnKeyListener(){

			@Override
			public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
				// TODO Auto-generated method stub
				
				
				if(arg1==KeyEvent.KEYCODE_DPAD_UP && arg2.getAction()==KeyEvent.ACTION_DOWN){
					try{
					String com = lastCmds.pop();
                	Log.i("Stack poped:", com);
                    cmd.setText(com);
					} catch(EmptyStackException e){
						e.printStackTrace();
					}
					return true;
				}
				
				return false;
			}
        	
        });

        cmd.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                    KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    workingDir = exec(cmd.getText().toString(), workingDir);
                    return true;
                }
                return false;
            }
        });

        exec.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                workingDir = exec(cmd.getText().toString(), workingDir);

            }
        });


    }

    //Execute command and returns the working directory as a result
    private File exec(final String command, File dir) {
        lastCmds.push(command);
        Log.i("Stack pushed:",command);
        if (!(command.length() < 1)) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {

                        if (command.startsWith("cd")) {
                            String path = command.replace("cd", "").trim();
                            if (path.startsWith("/")) {
                                workingDir = new File(path);
                            } else {
                                workingDir = new File(workingDir + "/" + path);
                            }

                            if (!workingDir.isDirectory()) {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        // TODO Auto-generated method stub
                                        res.setText("Can't cd to " + workingDir.getAbsolutePath() + ": Not found.");
                                    }
                                });
                            } else {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        // TODO Auto-generated method stub
                                        res.setText("Changed working directory: " + workingDir.getAbsolutePath());
                                        SysexecActivity.this.setTitle(workingDir.getAbsolutePath());

                                    }
                                });
                            }

                        } else {
                            Process p = Runtime.getRuntime().exec(command, null, workingDir);
                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            resultat = new StringBuilder();

                            while ((line = br.readLine()) != null) {
                                nbrLines++;
                                if (nbrLines < 100) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    resultat.append(line + "\n");
                                    handler.post(new Runnable() {

                                        @Override
                                        public void run() {

                                            res.setText(resultat.toString());
                                            sc1.scrollBy(0, sc1.getHeight());
                                        }
                                    });

                                } else {
                                    nbrLines = 0;
                                    nbrLines++;
                                    resultat = new StringBuilder();
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    resultat.append(line + "\n");
                                    handler.post(new Runnable() {

                                        @Override
                                        public void run() {

                                            res.setText(resultat.toString());
                                            sc1.scrollBy(0, sc1.getHeight() + 200);
                                            
                                        }
                                    });
                                }

                            }
                        }

                    } catch (final IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                            	
                                res.setText(e.getMessage());
                                
                            }
                        });

                    }
                }
            }).start();
        }
        //cmd.setText("");
        cmd.clearFocus();
        return workingDir;
    }
}