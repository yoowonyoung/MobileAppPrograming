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
	
	
	
	
	
	
	
	///////
	private MediaPlayer bgm;
	//////
	
	
	
	
	
	
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        
        
        
        
        ////////
        bgm = MediaPlayer.create(this, R.raw.bgm);
        bgm.setLooping(true);
        bgm.start();
        /////////
        
        
        
        
        
        
        
        
        startActivity(new Intent(this, LoadingActivity.class));
        findViewById(R.id.start_player).setOnClickListener(
                new OnClickListener() {
            public void onClick(View v) {
                startGame(true);//����� ���� �����ϴ� start game�� ����
                
                
                
                
                
                
                /////
                bgm.pause(); //���⼭ stop�ع����� bgm�����Ͱ� ���� �������� ���ƿ����� start�Ϸ��� �ٽ� ��������� �ؼ� pause�� ó�� 
                ///////
                
                
                
                
                
                
                
                
                
            }
        });

        findViewById(R.id.start_comp).setOnClickListener(
                new OnClickListener() {
            public void onClick(View v) {
                startGame(false);//��ǻ�Ͱ� ���� �����ϴ� start game�� ����
            
                
                
                
                
                
                
                
                ///////
                bgm.pause();
                ///////
                
                
                
                
                
                
                
                
            }
        });
    }

    private void startGame(boolean startWithHuman) {
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.EXTRA_START_PLAYER,
                startWithHuman ? State.PLAYER1.getValue() : State.PLAYER2.getValue());
        //������ ��ư�� ���� t/f���� �Ѿ� ���µ�, �̷� ������ �ؼ� �������,��ǻ�� ���� �����ؼ� ����
        //������� �� ��� 1, ��ǻ�� ���� �ϰ�� 2
        startActivityForResult(i,1);//1������ ����
    }
    protected void onResume() {//�Ͻ����� �̴ٰ� Ǯ���� �����ۿ��� �ҷ��ִ� �κ�
        super.onResume();
        
        
        
        
        
        
        ///////
        bgm.start();
        ///////
        
        
        
        
        
        
        
        
        if(player1win != 0 || player2win != 0){
        	Toast.makeText(MainActivity.this, "player1 score " + player1win + " win, "+ player2win + " lose \n" +  "player2 score " + player2win + " win, "+ player1win + " lose ", Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	int score = data.getExtras().getInt("Player");
    	if(score == 1){
    		player1win += 1;
    	}else if (score == 2){
    		player2win += 1;
    	}
    }
}