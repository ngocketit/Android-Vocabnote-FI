package com.ngocketit.vocabnote2.widgets;

import android.widget.LinearLayout;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.ToggleButton;
import android.util.AttributeSet;

import com.ngocketit.vocabnote2.R;
import com.ngocketit.vocabnote2.WordState;

public class WordStatePanelView extends LinearLayout
{
    private RadioGroup mRgrpState = null;

    private ToggleButton mTbtnGramma = null;
    private ToggleButton mTbtnPronun = null;
    private ToggleButton mTbtnColloc = null;
    private ToggleButton mTbtnPhrasa = null;

    public WordStatePanelView(Context context) {
        super(context);
        initView();
    }

    public WordStatePanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    
    public WordStatePanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LayoutInflater.from(getContext()).inflate(R.layout.word_states_panel, this);

        mRgrpState = (RadioGroup)findViewById(R.id.add_word_rgrp_word_state);
        mRgrpState.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                boolean enabled = (checkedId == R.id.add_word_rbtn_mark_unlearnt);

                mTbtnPhrasa.setEnabled(enabled);
                mTbtnGramma.setEnabled(enabled);
                mTbtnColloc.setEnabled(enabled);
                mTbtnPronun.setEnabled(enabled);
            }
        });

        mTbtnPhrasa = (ToggleButton)findViewById(R.id.word_state_panel_btn_phrasa);
        mTbtnGramma = (ToggleButton)findViewById(R.id.word_state_panel_btn_gramma);
        mTbtnColloc = (ToggleButton)findViewById(R.id.word_state_panel_btn_colloc);
        mTbtnPronun = (ToggleButton)findViewById(R.id.word_state_panel_btn_pronun);
    }

    public void setState(int state) {
        if ((state & WordState.STATE_LEARNT) > 0) {
            mRgrpState.check(R.id.add_word_rbtn_mark_learnt);
        } else {
            mRgrpState.check(R.id.add_word_rbtn_mark_unlearnt);
            mTbtnGramma.setChecked((state & WordState.STATE_REVIEW_GRAMMA) > 0);
            mTbtnPhrasa.setChecked((state & WordState.STATE_REVIEW_PHRASA) > 0);
            mTbtnColloc.setChecked((state & WordState.STATE_REVIEW_COLLOC) > 0);
            mTbtnPronun.setChecked((state & WordState.STATE_REVIEW_PRONUN) > 0);
        }
    }

    public int getState() {
        int state = WordState.STATE_UNLEARNT;
        int checkedBtnId = mRgrpState.getCheckedRadioButtonId();
        
        if (checkedBtnId == R.id.add_word_rbtn_mark_learnt) {
            state = WordState.STATE_LEARNT;

        } else if (checkedBtnId == R.id.add_word_rbtn_mark_unlearnt) {
            if (mTbtnPronun.isChecked()) state |= WordState.STATE_REVIEW_PRONUN;
            if (mTbtnGramma.isChecked()) state |= WordState.STATE_REVIEW_GRAMMA;
            if (mTbtnColloc.isChecked()) state |= WordState.STATE_REVIEW_COLLOC;
            if (mTbtnPhrasa.isChecked()) state |= WordState.STATE_REVIEW_PHRASA;
        }

        return state;
    }
}
