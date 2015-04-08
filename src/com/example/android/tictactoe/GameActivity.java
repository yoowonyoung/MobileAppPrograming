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

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

import com.example.android.tictactoe.GameView.ICellListener;
import com.example.android.tictactoe.GameView.State;


public class GameActivity extends Activity {

    /** Start player. Must be 1 or 2. Default is 1. */
    public static final String EXTRA_START_PLAYER =
        "com.example.android.tictactoe.library.GameActivity.EXTRA_START_PLAYER";

    private static final int MSG_COMPUTER_TURN = 1;//������ ������ �ð�
    private static final long COMPUTER_DELAY_MS = 500;//��ǻ�� ���� �����ð�

    private Handler mHandler = new Handler(new MyHandlerCallback());//�ݹ� �ڵ鷯
    private Random mRnd = new Random();
    private GameView mGameView;//ȭ�� ��Ÿ���� gameView
    private TextView mInfoView;//���� �̰���� ��Ÿ���� �ؽ�Ʈ ��
    private Button mButtonNext;
    
    private String playType;
    
      
    
    
    
    //////
    private SoundPool sp;
    private int winSound;
    private MediaPlayer timerSound;//Ÿ�̸ӼҸ��� mediaplayer�� ó��, soundpool�� oncreate���� �ٷ� �÷��� �� �� ����, play������ �ȵǴ� �κе� ����.���� �ڵ鷯�� ������ ���� �ȴٴµ� �� �𸣰���
    private CountDownTimer mCountDown ; 
    private TextView time;//Ÿ�̸� ���� �ð� ǥ��
    //private int countDownPause; //CountDownTimer�� pause����(wait)�����̸� 1, �ƴϸ� 0  
    //////
    
    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        /*
         * IMPORTANT: all resource IDs from this library will eventually be merged
         * with the resources from the main project that will use the library.
         *
         * If the main project and the libraries define the same resource IDs,
         * the application project will always have priority and override library resources
         * and IDs defined in multiple libraries are resolved based on the libraries priority
         * defined in the main project.
         *
         * An intentional consequence is that the main project can override some resources
         * from the library.
         * (TODO insert example).
         *
         * To avoid potential conflicts, it is suggested to add a prefix to the
         * library resource names.
         */
        setContentView(R.layout.lib_game);

        mGameView = (GameView) findViewById(R.id.game_view);
        mInfoView = (TextView) findViewById(R.id.info_turn);
        mButtonNext = (Button) findViewById(R.id.next_turn);
        
        
        
        
        
        
        
        
        ////////
        time = (TextView) findViewById(R.id.time);
        ///////
        
        
        
        
        
        
        
        
        
        
        mGameView.setFocusable(true);
        mGameView.setFocusableInTouchMode(true);
        mGameView.setCellListener(new MyCellListener());//�̺�Ʈ�� �ڽ��� ������ ������ ����

        mButtonNext.setOnClickListener(new MyButtonListener());//�̺�Ʈ�� �ڽ��� ������ ������ ����
        
        
        
        
        
        
        
        
        
        //////////
        sp = new SoundPool(1,AudioManager.STREAM_MUSIC,0);
        winSound = sp.load(this, R.raw.winsound,1); 
        timerSound = MediaPlayer.create(this, R.raw.timersound1);
        timerSound.setLooping(true);
        
        mCountDown = new CountDownTimer(11000,1000) { //11�ʵ��� 1�ʰ������� �پ��鼭 �����ش�. �ٵ� ���� �����غ��� 10�ʺ��� ����.
			
        	
			public void onTick(long millisUntilFinished) {
				
				time.setText("remain time: " + millisUntilFinished / 1000 + "sec");
			}
			public void onFinish() {
				//�ϳѱ��
			
		
				
				
				
				
				////////////�� �Ѿ�� �� ��Ȯ�� �νĽ�Ű�� ���� ���
				Toast toast = Toast.makeText(GameActivity.this, "your turn is time over!!!!", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				//////////
				
				
				
				
				
				finishTurn();
			}
		}.start();

		timerSound.start();
		/////////
		
		
		playType = getIntent().getStringExtra("playType");
		
		
		
		
		
    }
    
    
    @Override
    protected void onResume() {//�Ͻ����� �̴ٰ� Ǯ���� �����ۿ��� �ҷ��ִ� �κ�
        super.onResume(); 
                
        
        
        
        //////////
        timerSound.start();
        mCountDown.start();
        /*
        if(countDownPause == 1) //�̷��� �����ָ� ���� �����
        {
        	mCountDown.notify();
        	countDownPause = 0;
        }
        */
        //////////
        
        
        
        
        
        
        State player = mGameView.getCurrentPlayer();//�� �� ���� ��Ÿ���� �÷��̾ ������
        if (player == State.UNKNOWN) {//�ٵ� ���� �÷��̾ ����϶�
            player = State.fromInt(getIntent().getIntExtra(EXTRA_START_PLAYER, 1));//1�� �ٲ���
            if (!checkGameFinished(player)) {//������ �������� üũ�ؼ� �ȳ�������
            	selectTurn(player);//�� �÷��̾��� ������ ����
            }
        }
      
        if (player == State.PLAYER3) {//�÷��̾�3(����� ��ǻ��)�� ���ϰ��
            mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS);//0.5�� ������ ���ָ鼭 ��ǻ�Ͱ� ��ĭ���� �޽��
        }
        if (player == State.WIN) {//�ٵ� �÷��̾� ���°� �̱���¸�
            setWinState(mGameView.getWinner());//���� �̰���� �޾ƿͼ� �̰�ٰ� ����
        }
    }


    private State selectTurn(State player) {// �񸣴� ����
    	mGameView.setCurrentPlayer(player);//�ϴ� ���簡 ������ ������ ���ְ�
        mButtonNext.setEnabled(false);//�������� ���¹�ư�� �ϴ� ����.
        //�÷��̾�1 ���ʶ�� �� �����ϸ� ���� �ٽ� Ȱ��ȭ �ǰŵ�

        if (player == State.PLAYER1) {//�÷��̾� 1 ���ʶ��
            mInfoView.setText(R.string.player1_turn);//�÷��̾� 1�� ���ʶ�� ���ְ�
            mGameView.setEnabled(true);//�������� ��ġ �����ϰ� ����

        } else if (player == State.PLAYER2) {//�÷��̾�2(��ǻ��)�� ���ʶ��
            mInfoView.setText(R.string.player2_turn);//�÷��̾�2�� ���ʶ�� ���ְ�
            mGameView.setEnabled(true);//������ ��ġ�� ����
        } else if(player == State.PLAYER3) {
        	mInfoView.setText(R.string.player3_turn);//�÷��̾�2�� ���ʶ�� ���ְ�
            mGameView.setEnabled(true);//������ ��ġ�� ����
        }

        return player;//�׸��� �÷��̾ ��ȯ. �� ������ �Ҷ�, ���� �÷��̾ �������ֱ� ���� ��ߵǰŵ�
    }

    private class MyCellListener implements ICellListener {//�� ���� ������
        public void onCellSelected() {
            if (mGameView.getCurrentPlayer() == State.PLAYER1) {//�÷��̾ ����϶���
                int cell = mGameView.getSelection();//�Էµ� ���� �޾� ��
                mButtonNext.setEnabled(cell >= 0);//����� �� ���� �����Ѱ�� ��ư Ȱ��ȭ
            }else if (mGameView.getCurrentPlayer() == State.PLAYER2) {//�÷��̾ ����϶���
                int cell = mGameView.getSelection();//�Էµ� ���� �޾� ��
                mButtonNext.setEnabled(cell >= 0);//����� �� ���� �����Ѱ�� ��ư Ȱ��ȭ
            }
        }
    }

    private class MyButtonListener implements OnClickListener {//��ư ������ ������

        public void onClick(View v) {
        	
        	
        	

        	
        	/*finishTurn �޼ҵ�� �ű�
        	////////////
        	timerSound.pause();//���⼭ stop�ع����� ���Ƿε����ϵ� �ƿ� �Ҹ�� �ٽ� �÷����Ϸ��� �ٽ� �ε��ؾ��ؼ� pause�� ó��
			mCountDown.cancel();
			time.setText("");
			
			
        	try {
				Thread.sleep(200); //�Ҹ� pause ��Ų ���� �νĽ�Ű�� ���� ���.
			
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
        	//////////////
        	*/
     
        	
        	
        	
        	
        	State player = mGameView.getCurrentPlayer();//���� �÷��̾��� ���� �޾ƿ�

            if (player == State.WIN) {//�÷��̾ �̰������
                GameActivity.this.finish();//����

            } else if (player == State.PLAYER1) {//�÷��̾�1(�����)�� �������
                int cell = mGameView.getSelection();//���� ���� �޾Ƽ�
                if (cell >= 0) {
                    mGameView.stopBlink();//�����̴°� ���߰�
                    mGameView.setCell(cell, player);//����� ���� �����Ͽ������� �Է�
                    mGameView.setRecent(cell);
                    finishTurn();//�� ����
                }
            } else if (player == State.PLAYER2) {//�÷��̾�2(�����)�� �������
                int cell = mGameView.getSelection();//���� ���� �޾Ƽ�
                if (cell >= 0) {
                    mGameView.stopBlink();//�����̴°� ���߰�
                    mGameView.setCell(cell, player);//����� ���� �����Ͽ������� �Է�
                    mGameView.setRecent(cell);
                    finishTurn();//�� ����
                }
            } 	
        }
    }

    private class MyHandlerCallback implements Callback {//�ݹ� �ڵ鷯
        public boolean handleMessage(Message msg) {//�޽����� �ڵ��ϴµ�
            if (msg.what == MSG_COMPUTER_TURN) {//�̰� �޽��� ������ ���� Ȯ���ϴ� �κ��ΰ���

                // Pick a non-used cell at random. That's about all the AI you need for this game.
                State[] data = mGameView.getData();//�������� �޾ƿͼ�
                while(true){
                    int index = mRnd.nextInt(16);//�������� 0~15���� ����
                    if(data[index] == State.EMPTY){
                        mGameView.setCell(index, mGameView.getCurrentPlayer());//�÷��̾� 2�� �J�ٰ� �ϰ�
                        mGameView.setRecent(index);
                        break;
                    }
                }
                //int used = 0;
                //�׳� for���Ἥ �ϳ��� ���ؼ� Empty�� �ִ°� ���� �����
                /*for(State state : data)
                 * {
                 * 		if(state == State.Empty)
                 * 		{
                 * 			ĭ����
                 * 		}
                 * }
                 * 
                 */
                /*while (used != 0x1F) {//�� 0x1f��°� ���й��� ��°Ű���, 0x1f = 0000 0000 0001 1111��.
                    if (((used >> index) & 1) == 0) {//used�� 2���� ǥ���� ���������� index ��Ʈ��ŭ �ű��, 1�� �ƴ϶��..�� �̰� ���� ��ƴ�
                        used |= 1 << index;//�����;; �׷��� �ؼ� ���� used�� 1�� ��Ʈ���� ����(OR)�� ���ְ� �������� index��ŭ �������� ��Ʈ�̵�
                        if (data[index] == State.EMPTY) {//�ٵ� �ᱹ used�� �ݺ��� ���Ѱ��� ���ε� �̷��� �����ϰ� �ؾ��ϳ�?
                        	//�ƹ�ư �׷��� �������� index���� ĭ�� ���������
                            //�ݺ��� ����
                        }
                    }
                }*/
               finishTurn();//�׸��� �÷��̾�2�� ���� ����
                return true;//�������ϱ� true
            }
            return false;//�࿩�� �������� false
        }
    }

    private State getOtherPlayer(State player) {//�÷��̾� ����
    	if(playType.equals("pc")) {
    		return player == State.PLAYER1 ? State.PLAYER3: State.PLAYER1;
    	}
    	else
    		return player == State.PLAYER1 ? State.PLAYER2 : State.PLAYER1;//�÷��̾�1�̿����� 2��, 2������ 1��
    		
    }

    private void finishTurn() {//���� ������ ���
    	
    	
    	
    	
    	
    	
    	
    	
    	
    	////////////
    	//������ ��ư ������ �κп� �ִ� ���ε�, Ÿ���ʰ��� ���� �ѱ�� ��ư���� ���� �ѱ�� �Ȱ��� ó���� �ʿ�(Ÿ�̸ӼҸ� �Ͻ�����)�ؼ� ����� �ű�.
    	timerSound.pause();//���⼭ stop�ع����� ���Ƿε����ϵ� �ƿ� �Ҹ��(�˾� ����, release�� �Ҹ��Ű�� ���̰�, stop�� ���� �غ�ð��� ��� �ٽý����Ҽ� �ְ��Ѵٰ� ��
    	                   //��·�� �ٷ� �ٽ� �÷����ؾ��ؼ� pause�� ó��
		mCountDown.cancel();
		time.setText("");

    	try {
			Thread.sleep(300); //�Ҹ� pause ��Ų ���� �ν� + �ϳѱ� �νĽ�Ű�� ���� ���.
		
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	//////////////
    	
    	
    	
    	
    	
    	
    	
        State player = mGameView.getCurrentPlayer();//������ �÷��̾ �޾Ƽ�
        if (!checkGameFinished(player)) {//�� �÷��̾ ������ ���´��� Ȯ������
            player = selectTurn(getOtherPlayer(player));//�ٸ� �÷��̾���� ���� �Ѱ���
            if (player == State.PLAYER3) {//�ٵ� ���� ��ǻ�� ���϶�����
                mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS);
                //���� ���۽ð��� �����Ͽ���  ���� ���� �ð����� ��� �� �� �޼��� �߼�
                //�׳� �ܼ��� ī���� ���ִ� �κ��̶�� �����ϸ� �ȴ� 0.5�� ī����
            }
        }
    }

    public boolean checkGameFinished(State player) {//������ �������� Ȯ��
        State[] data = mGameView.getData();//���Ӻ��� ���¸� ����
        boolean full = true;//�� ������ Ȯ���ϴ� �÷����ε�

        int col = -1;
        int row = -1;
        int diag = -1;

        // check rows ������ Ȯ��
        for (int j = 0, k = 0; j < 4; j++, k += 4) {//�ݺ��� ���� ���� j, �� �� �������� ��Ÿ���� �� k, �� �ٿ� 3ĭ���̴ϱ� k�� 3���ø�
            if (data[k] != State.EMPTY && data[k] == data[k+1] && data[k] == data[k+2]&& data[k] == data[k+3]) {//�������� ù ĭ�� ������� Ȯ����,
            	//�� ������� �� �ٵ��� ���� ù��° ĭ�ϰ� ������ Ȯ��
                row = j;//�� ������ �� ���ִٸ�, �� �ٹ�ȣ�� ����
            }
            if (full && (data[k] == State.EMPTY ||
                         data[k+1] == State.EMPTY ||
                         data[k+2] == State.EMPTY ||
                         data[k+3] == State.EMPTY)) {//�ٵ� ���� ����ִ°ɷ� �� �����Ŷ�� �� ���� �ƴϴϱ�
                full = false;//������ �ƴ϶�� �ٲ���
            }
        }

        // check columns ������ Ȯ��
        for (int i = 0; i < 4; i++) {// �������� ��Ÿ���� ���� ���� i 
            if (data[i] != State.EMPTY && data[i] == data[i+4] && data[i] == data[i+8]&& data[i] == data[i+12]) {//�������� ù ���� ����ִ��� ����,
            	//�� ������� �� �ٵ��� ���� ù��° ĭ�ϰ� ������ Ȯ��
                col = i;//�� ������ �� ���ִٸ�, �� ��ȣ�� ����
            }
        }

        // check diagonals �밢�� Ȯ�� ���� �ݺ��� �Ұ� ����. ���� 2���� ���̴ϱ�
        if (data[0] != State.EMPTY && data[0] == data[1+4] && data[0] == data[2+8]&& data[0] == data[3+12]) {//���� �ϴ����� �������� �밢��
            diag = 0;//�׷��� 0����
        } else  if (data[3] != State.EMPTY && data[3] == data[2+4] && data[3] == data[1+8] && data[3] == data[0+12]) {//���� ������� �ö󰡴� �밢��
            diag = 1;//�׷��� 1��
        }

        if (col != -1 || row != -1 || diag != -1) {//�׷��� ����,����, �밢���� Ȯ���ؔf�µ� ���� �ϳ��� ������ �ϼ�������
            setFinished(player, col, row, diag);//� �÷��̾�����,���� ���� �밢�� ���� �Ѱ��ְ�
            return true;//������ �������� true
        }

        // if we get here, there's no winner but the board is full.
        if (full) {//���� ���� �ݺ������� empty�� ������ �ƴ϶�� false�� �Ǵµ�, ���⼭ �ɷ����°� ����������
            setFinished(State.EMPTY, -1, -1, -1);//�׳� �����ٰ� �˷���. ���� ����
            return true;//�׸��� ����
        }
        
        
        
        
        
        
        
        
        //////////
        //���ڰ� �������� �ʾ����ϱ� �Ҹ��� Ÿ�̸� �ٽ� ����
        timerSound.start();
    	mCountDown.start();
    	/////////
    	
    	
    	
    	
    	
    	
        return false;//��� ���� �� ���Ǵµ��� ������ �ȳ����� �� false
    }

    private void setFinished(State player, int col, int row, int diagonal) {

        mGameView.setCurrentPlayer(State.WIN);//���� �÷��̾��� ���¸� �̰�ٰ� ���ְ�
        mGameView.setWinner(player);//���ڰ� �������� ���ְ�
        mGameView.setEnabled(false);//��ġ �ȵǰ� ����
        mGameView.setFinished(col, row, diagonal);//��ٿ��� �̰���� ������
        setWinState(player);//�׸��� �̰����ϱ� ���º���
    }

    private void setWinState(State player) {
        mButtonNext.setEnabled(true);//��ư�����⸦ Ȱ��ȭ
        mButtonNext.setText("Back");//�ٵ� �װ� �ڷΰ��� ��ư�̾�

        String text;//�̱����� ���� �޶����� ���ڿ�

        if (player == State.EMPTY) {//�̱����� ���ٸ�(���º�)
            text = getString(R.string.tie);//�̱����� ���ٴ� ��Ʈ��
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("Player",0);
            setResult(1,i);
        } else if (player == State.PLAYER1) {//�̱���� �÷��̾�1 �϶�
            text = getString(R.string.player1_win);//�÷��̾� 1�� �̰�ٴ� ��Ʈ��
            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(200);
            
            
            
            
            
            
            //////////
            sp.play(winSound, 1, 1, 0, 0, 1);
            timerSound.stop(); //�ƿ� Ÿ�̸ӼҸ�������, start�ص� ����ȵ�, �ٽ� onCreate�Ǿ�� �����.
            mCountDown.cancel();
            time.setText("");
            ////////////
            
            
            
            
            
            
            if(playType.equals("pc")) { // pc�� ���� ��
            	Intent i = new Intent(this, MainActivity.class);
                i.putExtra("Player",-3);
                setResult(1,i);
            }
            else {//����� ���� �� 
            	Intent i = new Intent(this, MainActivity.class);
                i.putExtra("Player",-2);
                setResult(1,i);
            }
        } else {//�̱����� ������, 1�� �ƴ϶�� (2�ƴϸ� pc)
        	
        	if(playType.equals("pc")) { // pc�� ���� ��
        		text = getString(R.string.player3_win);// pc�� �̰����?
        		
            	Intent i = new Intent(this, MainActivity.class);
                i.putExtra("Player",3);
                setResult(1,i);
            }
            else {//����� ���� �� 
            	text = getString(R.string.player2_win);// �÷��̾� 2�� �̰����?
            	Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            	vibe.vibrate(500);
            	Intent i = new Intent(this, MainActivity.class);
                i.putExtra("Player",2);
                setResult(1,i);
            }
        	
            
            
            
            
            
            
            
            
            
            
            //////////
            sp.play(winSound, 1, 1, 0, 0, 1);
            timerSound.stop();
            mCountDown.cancel();
            time.setText("");
            ///////////
            
            
            
            
            
            
            

        }
        mInfoView.setText(text);//�׸��� �װ� ȭ�鿡 �����
    }
    
    
    
    
    ////////
    public void onPause() 
    {
        super.onPause();
        //�����峪 ����� ��������� ���� �Ȼ������ ����(������-���� or cancel-������) �Ŀ� null�� �־��־�� �Ѵ�.   
        if (timerSound != null) 
        {
        	timerSound.pause();

        }
        
        if(mCountDown != null) 
        {
        	mCountDown.cancel();
        }
        
        /* ������ wait�Ἥ �Ϻ��ϰ� �ߴ� �� �����ؾ� �Ǵµ�.. ������. �׷��� �׳� Ÿ�̸� �����ϰ� ������ϴ� ������ ����.
        if(mCountDown != null)
        {
			try {
				mCountDown.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			countDownPause = 1;
        }
        */
    }
    public void onDestroy() 
    {
        super.onDestroy();
        
        //�����峪 ����� ��������� ���� �Ȼ������ ����(������-���� or cancel-������) �Ŀ� null�� �־��־�� �Ѵ�.   
        if (timerSound != null) 
        {
        	timerSound.release();
        	timerSound = null;

        }
        if( mCountDown != null )
        {
        	mCountDown.cancel();
        	mCountDown = null;
        }
    }
    /////////
    
    
    
    
}
