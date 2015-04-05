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

import com.example.android.tictactoe.GameView.State;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	private int player1win = 0;
	private int player2win = 0;
	private int player3win = 0;
	private String playType;
	
	
	
	
	
	
	
	///////
	private MediaPlayer bgm;
	//////
	
	
	
	
	
	
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        
        startActivity(new Intent(this, LoadingActivity.class));
        
        
        
        
        ////////
        bgm = MediaPlayer.create(this, R.raw.bgm);
        bgm.setLooping(true);
        bgm.start();
        /////////
        
        
        
        
        
        
        
        //컴퓨터와 게임
        findViewById(R.id.play_comp).setOnClickListener(
                new OnClickListener() {
            public void onClick(View v) {
                startGame(3);//컴퓨터가 먼저 시작하는 start game로 시작
            
                
                
                
                
                
                
                
                ///////
                bgm.pause();
                ///////
                
                
                
                
                
                
                
                
            }
        });
        
        
        findViewById(R.id.start_player1).setOnClickListener(
                new OnClickListener() {
            public void onClick(View v) {
                startGame(1);//사람이 먼저 시작하는 start game로 시작
                
                
                
                
                
                
                /////
                bgm.pause(); //여기서 stop해버리면 bgm데이터가 날라가 메인으로 돌아왔을때 start하려면 다시 설정해줘야 해서 pause로 처리 
                ///////
                
                
                
                
                
                
                
                
                
            }
        });

        
        
        findViewById(R.id.start_player2).setOnClickListener(
                new OnClickListener() {
            public void onClick(View v) {
                startGame(2);//컴퓨터가 먼저 시작하는 start game로 시작
            
                
                
                
                
                
                
                
                ///////
                bgm.pause();
                ///////
                
                
                
                
                
                
                
                
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
        //선택한 버튼에 따라 t/f값이 넘어 오는데, 이로 구분을 해서 사람먼저,컴퓨터 먼저 구분해서 시작
        //사람먼저 일 경우 1, 컴퓨터 먼저 일경우 2
        startActivityForResult(i,1);//1번으로 시작
    }
    protected void onResume() {//일시정지 됫다가 풀릴때 씨스템에서 불러주는 부분
        super.onResume();
        
        
        
        
        
        
        ///////
        bgm.start();
        ///////
        
        
        
        
        
        
        
        
        if(player1win != 0 || player2win != 0 || player3win != 0){
        	if(playType.equals("pc")) {
        		Toast.makeText(MainActivity.this, "player1 score " + player1win + " win, "+ player3win + " lose \n" +  "computer score " + player3win + " win, "+ player1win + " lose ", Toast.LENGTH_LONG).show();
        	}
        	else {
        		Toast.makeText(MainActivity.this, "player1 score " + player1win + " win, "+ player2win + " lose \n" +  "player2 score " + player2win + " win, "+ player1win + " lose ", Toast.LENGTH_LONG).show();
        	}
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	if(data != null ){
    		int score = data.getExtras().getInt("Player");
        	if(score == -2){
        		player1win += 1;
        	}else if (score == 2){
        		player2win += 1;
        	}else if (score == -3){
        		player1win += 1;
        	}else if (score == 3){
        		player3win += 1;
        	}
    	}
    	
    }
    
    
    
    
    
    ///////
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
    /////////
    
    
    
    
    
}