package com.juliogv14.turnosync;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

/**
 * Created by Julio on 26/11/2017.
 * MainActivity.class
 */

public class MainActivity extends BaseDrawerActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.content_main, super.mViewBinding.contentView);
    }
}
