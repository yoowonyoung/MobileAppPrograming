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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

//-----------------------------------------------

public class GameView extends View {

    public static final long FPS_MS = 1000/2;//프레임 나타내주는 부분

    public enum State {//이 상태는 여러가지로 쓰이는데, 말판에 누가놨는지나, 플레이어가 이겼는지, 말판이 비엇는지등 여러가지에 쓰여.
        UNKNOWN(-3),
        WIN(-2),
        EMPTY(0),
        PLAYER1(1),
        PLAYER2(2),
        PLAYER3(3);

        private int mValue;

        private State(int value) {//이 스테이트의 상태 변경을 해주는 부분
            mValue = value;// 입력받은 값을 상태로 가지고 있어. 입력받은 값은 뭐 위에 있는것들 이겟지
        }

        public int getValue() {//이게 상태 받는 부분
            return mValue;//상태값을 리턴
        }

        public static State fromInt(int i) {//상태값을 정수로 설정해주기 위해서 쓰는 부분
            for (State s : values()) {//이건 열거형의 변수를 처음부터 불러주는 부분
                if (s.getValue() == i) {//하나씩 불러서 들어온값이랑 비교를 해서 같다면
                    return s;//그 상태가 뭔지 반환을 해주는거얌
                }
            }
            return EMPTY;//근데 지정된 변수값이 아니라면 비어있다고 해줘. 이게 디폴트니깐
        }
    }

    private static final int MARGIN = 4;//여백???
    private static final int MSG_BLINK = 1;//깜빡이는 ???

    private final Handler mHandler = new Handler(new MyHandler());//핸들러 설정

    private final Rect mSrcRect = new Rect();//시작하는 사각형
    private final Rect mDstRect = new Rect();//끝나는 사각형

    //변수들 이름...
    private int mSxy;
    private int mOffetX;
    private int mOffetY;
    private Paint mWinPaint;
    private Paint mLinePaint;
    private Paint mBmpPaint;
    private Bitmap mBmpPlayer1;
    private Bitmap mBmpPlayer2;
    private Bitmap mBmpRecentP1;
    private Bitmap mBmpRecentP2;
    private Drawable mDrawableBg;

    private ICellListener mCellListener;

    /** Contains one of {@link State#EMPTY}, {@link State#PLAYER1} or {@link State#PLAYER2}. */
    private final State[] mData = new State[16];//게임판이 9개셀이니까 9개

    private int mSelectedCell = -1;
    private State mSelectedValue = State.EMPTY;
    private State mCurrentPlayer = State.UNKNOWN;
    private State mWinner = State.EMPTY;

    private int mWinCol = -1;
    private int mWinRow = -1;
    private int mWinDiag = -1;

    private boolean mBlinkDisplayOff;
    private final Rect mBlinkRect = new Rect();



    public interface ICellListener {
        abstract void onCellSelected();
    }

    @SuppressWarnings("deprecation")
	public GameView(Context context, AttributeSet attrs) {//생성자에서 context랑 attribute 받는데 쓰진 않안
        super(context, attrs);
        requestFocus();

        mDrawableBg = getResources().getDrawable(R.drawable.lib_bg);//백드라운드 이미지 설정
        setBackgroundDrawable(mDrawableBg);//이 메소드 쓰지 말라는데 대안 찾아봐야겟다

        mBmpPlayer1 = getResBitmap(R.drawable.lib_cross);//플레이어 1은 X로
        mBmpPlayer2 = getResBitmap(R.drawable.lib_circle);//플레이어 2는 O로 
        mBmpRecentP1 = getResBitmap(R.drawable.lib_cross_recent);
        mBmpRecentP2 = getResBitmap(R.drawable.lib_circle_recent);
        
        if (mBmpPlayer1 != null) {
            mSrcRect.set(0, 0, mBmpPlayer1.getWidth() -1, mBmpPlayer1.getHeight() - 1);
            //이미지 불러오는거 성공했으면 값이 있겟지? 그럼 SrcRect를 그것보다 쬐끔 작게 만들아줌
        }

        mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//계단 현상 없이 만들어줌

        //선의 속성을 결정해주는 부분
        mLinePaint = new Paint();
        mLinePaint.setColor(0xFFFFFFFF);
        mLinePaint.setStrokeWidth(5);
        mLinePaint.setStyle(Style.STROKE);
        //이겼을때 나오는 부분을 설정해줌
        mWinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWinPaint.setColor(0xFFFF0000);
        mWinPaint.setStrokeWidth(10);
        mWinPaint.setStyle(Style.STROKE);

        for (int i = 0; i < mData.length; i++) {
            mData[i] = State.EMPTY;//게임판 초기화
        }

        if (isInEditMode()) {//이게 어디서 에디트 해주고있는지 알아내는거라는데? 여기서 true가 뜨면 
        	//이클립스에서 편집을 해주고 있는 부분
            // In edit mode (e.g. in the Eclipse ADT graphical layout editor)
            // we'll use some random data to display the state.
            Random rnd = new Random();
            for (int i = 0; i < mData.length; i++) {
                mData[i] = State.fromInt(rnd.nextInt(3));
                //그래서 이클립스가 해주고 있다면 값들을 랜덤빵으로
            }
        }
    }

    public State[] getData() {//게임판 데이터 반환
        return mData;
    }

    public void setCell(int cellIndex, State value) {//게임판의 한칸을 선택함
        mData[cellIndex] = value;//선택한 상태로 그 칸을 변경해주고
        invalidate();//화면 갱신
    }

    public void setCellListener(ICellListener cellListener) {//셀 리스너 지정해주는부분
        mCellListener = cellListener;//리스너 지정
    }

    public int getSelection() {//입력받은 값을 받아
        if (mSelectedValue == mCurrentPlayer) {//현재 플레이어가 입력한값이 맞는지
            return mSelectedCell;//선택된 셀을 반환
        }

        return -1;
    }

    public State getCurrentPlayer() {//현재 플레이어 반환
        return mCurrentPlayer;
    }

    public void setCurrentPlayer(State player) {//현재 플레이어 지정
        mCurrentPlayer = player;
        mSelectedCell = -1;
    }

    public State getWinner() {//승자 반환
        return mWinner;
    }

    public void setWinner(State winner) {//승자 설정
        mWinner = winner;
    }

    /** Sets winning mark on specified column or row (0..2) or diagonal (0..1). */
    public void setFinished(int col, int row, int diagonal) {//이겼을때 줄을 그려주기위해서 필요함
        mWinCol = col;//이긴 가로줄
        mWinRow = row;//이긴 세로줄
        mWinDiag = diagonal;// 이긴 대각선
    }
    
    //-----------------------------------------


    @Override
    protected void onDraw(Canvas canvas) {//화면에 그려질때 불리는 부분
        super.onDraw(canvas);

        int sxy = mSxy;
        int s3  = sxy * 4;
        int x7 = mOffetX;
        int y7 = mOffetY;

        for (int i = 0, k = sxy; i < 3; i++, k += sxy) {
            canvas.drawLine(x7    , y7 + k, x7 + s3 - 1, y7 + k     , mLinePaint);
            canvas.drawLine(x7 + k, y7    , x7 + k     , y7 + s3 - 1, mLinePaint);
        }

        for (int j = 0, k = 0, y = y7; j < 4; j++, y += sxy) {
            for (int i = 0, x = x7; i < 4; i++, k++, x += sxy) {
                mDstRect.offsetTo(MARGIN+x, MARGIN+y);

                State v;
                if (mSelectedCell == k) {
                    if (mBlinkDisplayOff) {
                        continue;
                    }
                    v = mSelectedValue;
                } else {
                    v = mData[k];
                }

                switch(v) {
                case PLAYER1:
                    if (mBmpPlayer1 != null) {
                        canvas.drawBitmap(mBmpPlayer1, mSrcRect, mDstRect, mBmpPaint);
                    }
                    break;
                case PLAYER2:
                    if (mBmpPlayer2 != null) {
                        canvas.drawBitmap(mBmpPlayer2, mSrcRect, mDstRect, mBmpPaint);
                    }
                    break;
                case PLAYER3:
                	if(mBmpPlayer2 != null) {
                        canvas.drawBitmap(mBmpPlayer2, mSrcRect, mDstRect, mBmpPaint);
                    }
                }
            }
        }

        if (mWinRow >= 0) {
            int y = y7 + mWinRow * sxy + sxy / 2;
            canvas.drawLine(x7 + MARGIN, y, x7 + s3 - 1 - MARGIN, y, mWinPaint);

        } else if (mWinCol >= 0) {
            int x = x7 + mWinCol * sxy + sxy / 2;
            canvas.drawLine(x, y7 + MARGIN, x, y7 + s3 - 1 - MARGIN, mWinPaint);

        } else if (mWinDiag == 0) {
            // diagonal 0 is from (0,0) to (2,2)

            canvas.drawLine(x7 + MARGIN, y7 + MARGIN,
                    x7 + s3 - 1 - MARGIN, y7 + s3 - 1 - MARGIN, mWinPaint);

        } else if (mWinDiag == 1) {
            // diagonal 1 is from (0,2) to (2,0)

            canvas.drawLine(x7 + MARGIN, y7 + s3 - 1 - MARGIN,
                    x7 + s3 - 1 - MARGIN, y7 + MARGIN, mWinPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Keep the view squared
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
        int d = w == 0 ? h : h == 0 ? w : w < h ? w : h;
        setMeasuredDimension(d, d);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int sx = (w - 2 * MARGIN) / 4;
        int sy = (h - 2 * MARGIN) / 4;

        int size = sx < sy ? sx : sy;

        mSxy = size;
        mOffetX = (w - 4 * size) / 2;
        mOffetY = (h - 4 * size) / 2;

        mDstRect.set(MARGIN, MARGIN, size - MARGIN, size - MARGIN);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            return true;

        } else if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            int sxy = mSxy;
            x = (x - MARGIN) / sxy;
            y = (y - MARGIN) / sxy;

            if (isEnabled() && x >= 0 && x < 4 && y >= 0 & y < 5) {
                int cell = x + 4 * y;

                State state = cell == mSelectedCell ? mSelectedValue : mData[cell];
                state = state == State.EMPTY ? mCurrentPlayer : State.EMPTY;

                stopBlink();

                mSelectedCell = cell;
                mSelectedValue = state;
                mBlinkDisplayOff = false;
                mBlinkRect.set(MARGIN + x * sxy, MARGIN + y * sxy,
                               MARGIN + (x + 1) * sxy, MARGIN + (y + 1) * sxy);

                if (state != State.EMPTY) {
                    // Start the blinker
                    mHandler.sendEmptyMessageDelayed(MSG_BLINK, FPS_MS);
                }

                if (mCellListener != null) {
                    mCellListener.onCellSelected();
                }
            }

            return true;
        }

        return false;
    }

    public void stopBlink() {
        boolean hadSelection = mSelectedCell != -1 && mSelectedValue != State.EMPTY;
        mSelectedCell = -1;
        mSelectedValue = State.EMPTY;
        if (!mBlinkRect.isEmpty()) {
            invalidate(mBlinkRect);
        }
        mBlinkDisplayOff = false;
        mBlinkRect.setEmpty();
        mHandler.removeMessages(MSG_BLINK);
        if (hadSelection && mCellListener != null) {
            mCellListener.onCellSelected();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle b = new Bundle();

        Parcelable s = super.onSaveInstanceState();
        b.putParcelable("gv_super_state", s);

        b.putBoolean("gv_en", isEnabled());

        int[] data = new int[mData.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = mData[i].getValue();
        }
        b.putIntArray("gv_data", data);

        b.putInt("gv_sel_cell", mSelectedCell);
        b.putInt("gv_sel_val",  mSelectedValue.getValue());
        b.putInt("gv_curr_play", mCurrentPlayer.getValue());
        b.putInt("gv_winner", mWinner.getValue());

        b.putInt("gv_win_col", mWinCol);
        b.putInt("gv_win_row", mWinRow);
        b.putInt("gv_win_diag", mWinDiag);

        b.putBoolean("gv_blink_off", mBlinkDisplayOff);
        b.putParcelable("gv_blink_rect", mBlinkRect);

        return b;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        if (!(state instanceof Bundle)) {
            // Not supposed to happen.
            super.onRestoreInstanceState(state);
            return;
        }

        Bundle b = (Bundle) state;
        Parcelable superState = b.getParcelable("gv_super_state");

        setEnabled(b.getBoolean("gv_en", true));

        int[] data = b.getIntArray("gv_data");
        if (data != null && data.length == mData.length) {
            for (int i = 0; i < data.length; i++) {
                mData[i] = State.fromInt(data[i]);
            }
        }

        mSelectedCell = b.getInt("gv_sel_cell", -1);
        mSelectedValue = State.fromInt(b.getInt("gv_sel_val", State.EMPTY.getValue()));
        mCurrentPlayer = State.fromInt(b.getInt("gv_curr_play", State.EMPTY.getValue()));
        mWinner = State.fromInt(b.getInt("gv_winner", State.EMPTY.getValue()));

        mWinCol = b.getInt("gv_win_col", -1);
        mWinRow = b.getInt("gv_win_row", -1);
        mWinDiag = b.getInt("gv_win_diag", -1);

        mBlinkDisplayOff = b.getBoolean("gv_blink_off", false);
        Rect r = b.getParcelable("gv_blink_rect");
        if (r != null) {
            mBlinkRect.set(r);
        }

        // let the blink handler decide if it should blink or not
        mHandler.sendEmptyMessage(MSG_BLINK);

        super.onRestoreInstanceState(superState);
    }

    //-----

    private class MyHandler implements Callback {
        public boolean handleMessage(Message msg) {
            if (msg.what == MSG_BLINK) {
                if (mSelectedCell >= 0 && mSelectedValue != State.EMPTY && mBlinkRect.top != 0) {
                    mBlinkDisplayOff = !mBlinkDisplayOff;
                    invalidate(mBlinkRect);

                    if (!mHandler.hasMessages(MSG_BLINK)) {
                        mHandler.sendEmptyMessageDelayed(MSG_BLINK, FPS_MS);
                    }
                }
                return true;
            }
            return false;
        }
    }

    private Bitmap getResBitmap(int bmpResId) {
        Options opts = new Options();
        opts.inDither = false;

        Resources res = getResources();
        Bitmap bmp = BitmapFactory.decodeResource(res, bmpResId, opts);

        if (bmp == null && isInEditMode()) {
            // BitmapFactory.decodeResource doesn't work from the rendering
            // library in Eclipse's Graphical Layout Editor. Use this workaround instead.

            Drawable d = res.getDrawable(bmpResId);
            int w = d.getIntrinsicWidth();
            int h = d.getIntrinsicHeight();
            bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            Canvas c = new Canvas(bmp);
            d.setBounds(0, 0, w - 1, h - 1);
            d.draw(c);
        }

        return bmp;
    }
}


