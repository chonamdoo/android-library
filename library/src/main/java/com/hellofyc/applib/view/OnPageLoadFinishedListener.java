package com.hellofyc.applib.view;

public interface OnPageLoadFinishedListener {
    int CODE_SUCCESS = 1;
    int CODE_FAILURE = -1;

    void onPageLoadFinish(int code);
}