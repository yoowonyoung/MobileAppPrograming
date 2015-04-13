/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.tictactoe;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	private int player1win = 0;
	private int player2win = 0;
	private int compWin = 0; //��ǻ��
	private int player1lose = 0;
	private int player2lose = 0;
	private int compLose = 0;
	private String playType;
	private MediaPlayer bgm;	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        startActivity(new Intent(this, SplashActivity.class));
        bgm = MediaPlayer.create(this, R.raw.bgm);
        bgm.setLooping(true);
        bgm.start();
        findViewById(R.id.play_comp).setOnClickListener(
                new OnClickListener() {
            public void onClick(View v) {
                startGame(3);//��ǻ�Ͱ� ���� �����ϴ� start game�� ����
                bgm.pause();
            }
        });
        findViewById(R.id.start_player1).setOnClickListener(
                new OnClickListener() {
            public void onClick(View v) {
                startGame(1);//����� ���� �����ϴ� start game�� ����
                bgm.pause(); //���⼭ stop�ع����� bgm�����Ͱ� ���� �������� ���ƿ����� start�Ϸ��� �ٽ� ��������� �ؼ� pause�� ó��      
            }
        });
        findViewById(R.id.start_player2).setOnClickListener(
                new OnClickListener() {
            public void onClick(View v) {
                startGame(2);//��ǻ�Ͱ� ���� �����ϴ� start game�� ����      
                bgm.pause();   
            }
        });
    }

    private void startGame(int startWithHuman) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.EXTRA_START_PLAYER, startWithHuman);
        if(startWithHuman == 3) {
        	playType = "pc";
        	i.putExtra("playType", "pc");
        }else {
        	playType = "human";
        	i.putExtra("playType", "human");
        }
        //������ ��ư�� ���� t/f���� �Ѿ� ���µ�, �̷� ������ �ؼ� �������,��ǻ�� ���� �����ؼ� ����
        //������� �� ��� 1, ��ǻ�� ���� �ϰ�� 2
        startActivityForResult(i,1);//1������ ����
    }
    protected void onResume() {//�Ͻ����� �̴ٰ� Ǯ���� �����ۿ��� �ҷ��ִ� �κ�
        super.onResume();
        bgm.start();
  
        if(player1win != 0 || player2win != 0 || compWin != 0){
        
        		Toast.makeText(MainActivity.this, 
        				   "player1 score " + player1win + "Win,  " + player1lose + "Lose \n" 
        	            +  "player2 score " + player2win + "Win,  " + player2lose + "Lose \n"
        				+  "computer score " + compWin + "Win,  "+ compLose + "Lose", Toast.LENGTH_LONG).show();
        }
        	
        
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	if(data != null ){
    		int score = data.getExtras().getInt("Player");
        	if(score == -2){
        		player1win += 1;
        		player2lose += 1;
        	}else if (score == 2){
        		player2win += 1;
        		player1lose += 1;
        	}else if (score == -3){
        		player1win += 1;
        		compLose += 1;
        	}else if (score == 3){
        		compWin += 1;
        		player1lose += 1;
        	}
    	}
    	
    }

    public void onPause() 
    {
        super.onPause();
        
        if (bgm != null) 
        {
          bgm.pause();

        }
    }
    public void onDestroy() 
    {
        super.onDestroy();
        
        if (bgm != null) 
        {
          bgm.release();
          bgm = null;

        }
    } 
}