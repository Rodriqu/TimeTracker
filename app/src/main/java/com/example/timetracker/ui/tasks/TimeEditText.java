package com.example.timetracker.ui.tasks;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.example.timetracker.R;

public class TimeEditText extends LinearLayout {

    private EditText hoursEditText;
    private EditText minutesEditText;

    public TimeEditText(Context context) {
        super(context);
        init(context);
    }

    public TimeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TimeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Inflate the layout from XML
        LayoutInflater.from(context).inflate(R.layout.widget_time_input, this, true);

        // Find the EditText components
        hoursEditText = findViewById(R.id.hoursEditText);
        minutesEditText = findViewById(R.id.minutesEditText);

        // Set up listeners to validate input
        hoursEditText.addTextChangedListener(new TimeTextWatcher(hoursEditText));
        minutesEditText.addTextChangedListener(new TimeTextWatcher(minutesEditText));
        // Set EditorActionListener to switch focus when "Enter" or "Next" is pressed
        hoursEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {
                    // Move focus to the minutes EditText when "Enter" or "Next" is pressed
                    minutesEditText.requestFocus();
                    return true; // Consumed the event
                }
                return false;
            }
        });

    }

    public void setTextInMinutes(int minutes) {
        if (minutes <= 0){
            return;
        }
        if (minutes >= 60*24){
            return;
        }
        int hours = minutes / 60;
        if (hours < 10) {
            if (hours != 0){
                hoursEditText.setText("0" + Integer.toString(hours));
            }
        }
        else{
            hoursEditText.setText(Integer.toString(hours));
        }
        int remainingMinutes = minutes % 60;
        if (remainingMinutes < 10) {
            if (remainingMinutes != 0){
                minutesEditText.setText("0" + Integer.toString(remainingMinutes));
            }
        }
        else{
            minutesEditText.setText(Integer.toString(remainingMinutes));
        }
    }

    public int getTotalMinutes() {
        try {
            int hours;
            if(hoursEditText.getText().toString().isEmpty()){
                hours = 0;
            }
            else{
                 hours = Integer.parseInt(hoursEditText.getText().toString());
            }
            int minutes;
            if(minutesEditText.getText().toString().isEmpty()){
                minutes = 0;
            }
            else{
                minutes = Integer.parseInt(minutesEditText.getText().toString());
            }
            if (minutes >= 60) return -1;
            if (hours >= 24) return -1;
            return (hours * 60) + minutes;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private class TimeTextWatcher implements TextWatcher {

        private EditText editText;

        public TimeTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
            String text = charSequence.toString();

            // Validate hours input
            if (editText.getId() == R.id.hoursEditText) {
                if (!text.isEmpty()) {
                    int hours = Integer.parseInt(text);
                    if (hours > 23) {
                        //editText.setText("23");
                        //editText.setSelection(editText.getText().length());
                        editText.setError("Enter correct number of hours");
                    }
                }
            }

            // Validate minutes input
            if (editText.getId() == R.id.minutesEditText) {
                if (!text.isEmpty()) {
                    int minutes = Integer.parseInt(text);
                    if (minutes > 59) {
                        //editText.setText("59");
                        //editText.setSelection(editText.getText().length());
                        editText.setError("Enter correct number of minutes");
                    }
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {}
    }
}