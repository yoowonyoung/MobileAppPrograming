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

    private static final int MSG_COMPUTER_TURN = 1;//응답을 시작할 시간
    private static final long COMPUTER_DELAY_MS = 500;//컴퓨터 응답 지연시간

    private Handler mHandler = new Handler(new MyHandlerCallback());//콜백 핸들러
    private Random mRnd = new Random();
    private GameView mGameView;//화면 나타내는 gameView
    private TextView mInfoView;//누가 이겼는지 나타내는 텍스트 뷰
    private Button mButtonNext;
    
    private String playType;

    //////
    private SoundPool winSoundPool;
    private int winSound;
    private MediaPlayer timerSound;
    //////소리에 사용되는 변수
    private CountDownTimer countDown; 
    private TextView timeText;//타이머 남은 시간 표시
    private long remainTime;//타이머 남은 시간 받는 변수
    private boolean pauseState; //pause상태로 되면 true처리
    ////////타이머에 사용되는 변수
    
    public void timerStart()
    {
    	if(countDown != null)
    	{
    		countDown.cancel(); //생성하기 전에 미리 정지, 초기화시켜둔다.
    		countDown = null;
    	}
    	if(pauseState == true)
    	{
    			pauseState = false;//퍼지되고 풀렸을 때 한번만 수행되게끔 함 
    			countDown = new CountDownTimer(remainTime, 1000) { //11초동안 1초간격으로 줄어들면서 보여준다. 근데 실제 실행해보면 10초부터 시작.
    			public void onTick(long millisUntilFinished) {
    				timeText.setText("remain time: " + millisUntilFinished / 1000 + "sec");
    			}
    			public void onFinish() {
    				Toast toast = Toast.makeText(GameActivity.this, "your turn is time over!!!!", Toast.LENGTH_SHORT);
    				toast.setGravity(Gravity.CENTER, 0, 0);
    				toast.show();//턴 넘어감을 더 정확히 인식시키기 위해 사용
    				finishTurn();//턴넘기기
    			}
    			}.start();
    	}
    	else //pasueState가 아니면
    	{
    			countDown = new CountDownTimer(11000, 1000) { 
    			public void onTick(long millisUntilFinished) {
    				timeText.setText("remain time: " + millisUntilFinished / 1000 + "sec");
    				remainTime = millisUntilFinished;//pause상태일때 위의 countdowntimer사용하기 위해 추가함
    			}
    			public void onFinish() {
    				Toast toast = Toast.makeText(GameActivity.this, "your turn is time over!!!!", Toast.LENGTH_SHORT);
    				toast.setGravity(Gravity.CENTER, 0, 0);
    				toast.show();
    				finishTurn();
    			}
        		}.start();		
    	}
    	
    }
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.lib_game);
        mGameView = (GameView) findViewById(R.id.game_view);
        mInfoView = (TextView) findViewById(R.id.info_turn);
        mButtonNext = (Button) findViewById(R.id.next_turn); 
        mGameView.setFocusable(true);
        mGameView.setFocusableInTouchMode(true);
        mGameView.setCellListener(new MyCellListener());//이벤트에 자신이 정의한 리스너 붙임
        mButtonNext.setOnClickListener(new MyButtonListener());//이벤트에 자신이 정의한 리스너 붙임 
        //////////
        winSoundPool = new SoundPool(1,AudioManager.STREAM_MUSIC,0);
        winSound = winSoundPool.load(this, R.raw.winsound,1);
        timerSound = MediaPlayer.create(this, R.raw.timersound1);
        timerSound.setLooping(true); 
        timerSound.start();
        //////////효과음, 배경음에 쓰이는 변수(객체) 정의, 실행
        timeText = (TextView) findViewById(R.id.time);
        pauseState = false;
		timerStart();
		/////////타이머에 쓰이는 변수 정의, 실행
		
		playType = getIntent().getStringExtra("playType");
		
    }
    
    
    @Override
    protected void onResume() {//일시정지 됫다가 풀릴때 시스템에서 불러주는 부분
        super.onResume(); 
        /////
        timerSound.start();
        timerStart();     
        /////타이머, 타이머소리 실행   
        
        State player = mGameView.getCurrentPlayer();//뷰 상에 현재 나타나는 플레이어가 누군지
        if (player == State.UNKNOWN) {//근데 만약 플레이어가 언논일때
            player = State.fromInt(getIntent().getIntExtra(EXTRA_START_PLAYER, 1));//1로 바꺼줌
            if (!checkGameFinished(player)) {//게임이 끝났는지 체크해서 안끝났으면
            	selectTurn(player);//그 플레이어의 턴으로 해쥼
            }
        }
      
        if (player == State.COMP) {//플레이어3(현재는 컴퓨터)의 턴일경우
            mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS);//0.5초 딜레이 해주면서 컴퓨터가 한칸놓는 메써드
        }
        if (player == State.WIN) {//근데 플레이어 상태가 이긴상태면
            setWinState(mGameView.getWinner());//누가 이겼는지 받아와서 이겼다고 해줌
        }
    }


    private State selectTurn(State player) {// 골르는 차례
    	mGameView.setCurrentPlayer(player);//일단 현재가 누구의 턴인지 해주고
        mButtonNext.setEnabled(false);//다음차례 가는버튼은 일단 막음
        //플레이어1 차례라면 셀 선택하면 저거 다시 활성화 

        if (player == State.PLAYER1) {//플레이어 1 차례라면
            mInfoView.setText(R.string.player1_turn);//플레이어 1의 차례라고 해주고
            mGameView.setEnabled(true);//게임판을 터치 가능하게 해줌

        } else if (player == State.PLAYER2) {//플레이어2의 차례라면
            mInfoView.setText(R.string.player2_turn);//플레이어2의 차례라고 해줌
            mGameView.setEnabled(true);
        } else if(player == State.COMP) {
        	mInfoView.setText(R.string.comp_turn);//컴퓨터의 차례라고 해줌
            mGameView.setEnabled(false);//게임판 터치를 막아
        }

        return player;//그리고 플레이어를 반환. 턴 끝내기 할때, 다음 플레이어를 지정해주기 위해 써야됨
    }

    private class MyCellListener implements ICellListener {//셀 선택 리스너
        public void onCellSelected() {
            if (mGameView.getCurrentPlayer() == State.PLAYER1) {//플레이어가 사람일때만
                int cell = mGameView.getSelection();//입력된 셀을 받아 옴
                mButtonNext.setEnabled(cell >= 0);//제대로 된 셀을 선택한경우 버튼 활성화
            }else if (mGameView.getCurrentPlayer() == State.PLAYER2) {//플레이어가 사람일때만
                int cell = mGameView.getSelection();//입력된 셀을 받아 옴
                mButtonNext.setEnabled(cell >= 0);//제대로 된 셀을 선택한경우 버튼 활성화
            }
        }
    }

    private class MyButtonListener implements OnClickListener {//버튼 누르기 리스너
        public void onClick(View v) {
        	
        	State player = mGameView.getCurrentPlayer();//현재 플레이어의 상태 받아옴

            if (player == State.WIN) {//플레이어가 이겼을경우
                GameActivity.this.finish();//끝냄

            } else if (player == State.PLAYER1) {//플레이어1(사용자)가 누른경우
                int cell = mGameView.getSelection();//누른 셀을 받아서
                if (cell >= 0) {
                    mGameView.stopBlink();//깜빡이는걸 멈추고
                    mGameView.setCell(cell, player);//어떤셀에 누가 선택하였는지를 입력
                    mGameView.setRecent(cell);
                    finishTurn();//턴 엔드
                }
            } else if (player == State.PLAYER2) {//플레이어2(사용자)가 누른경우
                int cell = mGameView.getSelection();//누른 셀을 받아서
                if (cell >= 0) {
                    mGameView.stopBlink();//깜빡이는걸 멈추고
                    mGameView.setCell(cell, player);//어떤셀에 누가 선택하였는지를 입력
                    mGameView.setRecent(cell);
                    finishTurn();//턴 엔드
                }
            } 	
        }
    }

    private class MyHandlerCallback implements Callback {//콜백 핸들러
        public boolean handleMessage(Message msg) {//메시지를 핸들하는데
            if (msg.what == MSG_COMPUTER_TURN) {//이거 메시지 내용이 뭔지 확인하는 부분
                State[] data = mGameView.getData();//게임판을 받아와서
                while(true){
                    int index = mRnd.nextInt(16);//랜덤으로 0~15까지 만들어서
                    if(data[index] == State.EMPTY){
                        mGameView.setCell(index, mGameView.getCurrentPlayer());//플레이어 2가 놧다고 하고
                        mGameView.setRecent(index);
                        break;
                    }
                }
              
                finishTurn();//그리고 플레이어2의 턴을 끝냄
                return true;//끝냈으니까 true
            }
            return false;//행여나 못끝내면 false
        }
    }

    private State getOtherPlayer(State player) {//플레이어 변경
    	if(playType.equals("pc")) {
    		return player == State.PLAYER1 ? State.COMP: State.PLAYER1;
    	}
    	else
    		return player == State.PLAYER1 ? State.PLAYER2 : State.PLAYER1;//플레이어1이였으면 2로, 2였으면 1로
    		
    }

    private void finishTurn() {//턴을 끝내는 방법
    	timerSound.pause();
		countDown.cancel();
		timeText.setText("");
    	try {
			Thread.sleep(300); 
		
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	//소리 pause 시킨 것을 인식 + 턴넘김 인식시키기 위해 사용.
    	
        State player = mGameView.getCurrentPlayer();//현재의 플레이어를 받아서
        if (!checkGameFinished(player)) {//그 플레이어가 게임을 끝냈는지 확인한후
            player = selectTurn(getOtherPlayer(player));//다른 플레이어로의 턴을 넘겨줌
            if (player == State.COMP) {//근데 만약 컴퓨터 턴일때에는
                mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS);
                //응답 시작시간에 시작하여서  응답 지연 시간까지 대기 한 후 메세지 발송
                //그냥 단순히 카운터 해주는 부분이라고 생각하면 된다 0.5초 카운팅
            }
        }
    }

    public boolean checkGameFinished(State player) {//게임이 끝낫는지 확인
        State[] data = mGameView.getData();//게임뷰의 상태를 받음
        boolean full = true;//꽉 찻는지 확인하는 플래그
        int col = -1;
        int row = -1;
        int diag = -1;
        // check rows 가로줄 확인
        for (int j = 0, k = 0; j < 4; j++, k += 4) {//반복을 위한 변수 j, 그 한 가로줄을 나타낼때 쓸 k, 한 줄에 3칸씩이니까 k는 3씩늘림
            if (data[k] != State.EMPTY && data[k] == data[k+1] && data[k] == data[k+2]&& data[k] == data[k+3]) {//가로줄의 첫 칸이 비었는지 확인후,
            	//안 비었으면 그 줄들의 값이 첫번째 칸하고 같은지 확인
                row = j;//그 한줄이 다 차있다면, 그 줄번호를 저장
            }
            if (full && (data[k] == State.EMPTY || data[k+1] == State.EMPTY || data[k+2] == State.EMPTY ||
                         data[k+3] == State.EMPTY)) {//근데 만약 비어있는걸로 다 같은거라면 다 찬게 아니니까
                full = false;//다찬게 아니라고 바꿔줌
            }
        }
        // check columns 세로줄 확인
        for (int i = 0; i < 4; i++) {// 세로줄을 나타내기 위한 변수 i 
            if (data[i] != State.EMPTY && data[i] == data[i+4] && data[i] == data[i+8]&& data[i] == data[i+12]) {//세로줄의 첫 값이 비어있는지 보고,
            	//안 비었으면 그 줄들의 값이 첫번째 칸하고 같은지 확인
                col = i;//그 한줄이 다 차있다면, 그 번호를 저장
            }
        }
        // check diagonals 대각선 확인 여긴 반복을 할게 없음. 경우는 2가지 뿐이니깐
        if (data[0] != State.EMPTY && data[0] == data[1+4] && data[0] == data[2+8]&& data[0] == data[3+12]) {//우측 하단으로 내려가는 대각선
            diag = 0;//그럴땐 0으로
        } else  if (data[3] != State.EMPTY && data[3] == data[2+4] && data[3] == data[1+8] && data[3] == data[0+12]) {//좌측 상단으로 올라가는 대각선
            diag = 1;//그럴땐 1로
        }
        if (col != -1 || row != -1 || diag != -1) {//그래서 가로,세로, 대각선을 확인해봣는데 그중 하나라도 한줄이 완성됬을땐
            setFinished(player, col, row, diag);//어떤 플레이어인지,가로 세로 대각선 값도 넘겨주고
            return true;//게임이 끝났으니 true
        }
        if (full) {//위에 가로 반복문에서 empty로 꽉찬게 아니라면 false가 되는데, 여기서 걸러내는건 비어있을경우
            setFinished(State.EMPTY, -1, -1, -1);//그냥 끝낫다고 알려줌. 승자 없이
            return true;//그리고 끝냄
        }
        /////////
        timerSound.start();
    	timerStart(); 
    	/////////승자가 결정되지 않았으니 소리랑 타이머 다시 돌림

        return false;//모든 조건 다 돌렷는데도 게임이 안끝나면 걍 false
    }

    private void setFinished(State player, int col, int row, int diagonal) {

        mGameView.setCurrentPlayer(State.WIN);//현재 플레이어의 상태를 이겼다고 해주고
        mGameView.setWinner(player);//승자가 누구인지 해주고
        mGameView.setEnabled(false);//터치 안되게 막고
        mGameView.setFinished(col, row, diagonal);//어떤줄에서 이겼는지 보여줌
        setWinState(player);//그리고 이겼으니까 상태변경
    }

    private void setWinState(State player) {
        mButtonNext.setEnabled(true);//버튼누르기를 활성화
        mButtonNext.setText("Back");//근데 그건 뒤로가기 버튼

        String text;//이긴사람에 따라 달라지는 문자열

        if (player == State.EMPTY) {//이긴사람이 없다면(무승부)
            text = getString(R.string.tie);//이긴사람이 없다는 스트링
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("Player",0);
            setResult(1,i);
        } else if (player == State.PLAYER1) {//이긴놈이 플레이어1 일땐
            text = getString(R.string.player1_win);//플레이어 1이 이겻다는 스트링
            Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(200); 
            winSoundPool.play(winSound, 1, 1, 0, 0, 1);//윈소리 재생
            timerSound.stop(); //타이머소리 정지
            countDown.cancel(); //타이머정지
            timeText.setText(""); 
 
            if(playType.equals("pc")) { // pc와 했을 때
            	Intent i = new Intent(this, MainActivity.class);
                i.putExtra("Player",-3);
                setResult(1,i);
            }
            else {//사람과 했을 때 
            	Intent i = new Intent(this, MainActivity.class);
                i.putExtra("Player",-2);
                setResult(1,i);
            }
        } else {//이긴사람이 없지도 않고, 1도 아니라면 (2아니면 pc)
        	
        	if(playType.equals("pc")) { // pc와 했을 때
        		text = getString(R.string.comp_win);// pc가 이김
        		
            	Intent i = new Intent(this, MainActivity.class);
                i.putExtra("Player",3);
                setResult(1,i);
            }
            else {//사람과 했을 때 
            	text = getString(R.string.player2_win);// 플레이어 2가 이김
            	Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            	vibe.vibrate(500);
            	Intent i = new Intent(this, MainActivity.class);
                i.putExtra("Player",2);
                setResult(1,i);
            }
            winSoundPool.play(winSound, 1, 1, 0, 0, 1);
            timerSound.stop();
            countDown.cancel();
            timeText.setText("");
        }
        mInfoView.setText(text);//그리고 그걸 화면에 띄워줌
    }
    
    public void onPause() 
    {
        super.onPause();  
        pauseState = true;
        if (timerSound != null) 
        	timerSound.pause();
        
        if(countDown != null) 
        	countDown.cancel();
    }
    public void onDestroy() 
    {
        super.onDestroy();
        //쓰레드나 사운드는 강제종료시 문제 안생기려면 정지(릴리즈-음악 or cancel-스레드) 후에 null값 넣어주어야 한다.   
        if (timerSound != null) 
        {
        	timerSound.release();
        	timerSound = null;
        }
        if( countDown != null )
        {
        	countDown.cancel();
        	countDown = null;
        }
    } 
}
