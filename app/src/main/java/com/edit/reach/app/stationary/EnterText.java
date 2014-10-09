package com.edit.reach.app.stationary;

import android.app.Activity;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simonlarssontakman on 2014-10-03.
 */
public class EnterText extends Activity {


    private List<EditText> editTextList = new ArrayList<EditText>();

    private EditText editText(int _intID){
        EditText editText = new EditText(this);
        editText.setId(_intID);
        editText.setHint("By");
        editText.setWidth(180);
        editTextList.add(editText);
        return editText;

    }



}
