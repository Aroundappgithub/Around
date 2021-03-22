package com.example.nearfriends;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText rangeValue;
    private Button submitRangeButton;
    private double doubleRange;

    /**
     * Initialize home screen activity
     *
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        submitRangeButton = (Button) findViewById(R.id.submitRangeButton);
        submitRangeButton.setEnabled(false);
        rangeValue = (EditText) findViewById(R.id.rangeValue);
        rangeValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (start > 0 || after > 0) {
                    submitRangeButton.setEnabled(true);
                    submitRangeButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            openActivity2(rangeValue);
                        }
                    });
                } else {
                    submitRangeButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void openActivity2(EditText rangeValue) {
        Intent intent = new Intent(this, MainActivity2.class);
        intent.putExtra("range value", rangeValue.getText().toString());
        startActivity(intent);
    }

    public double getRangeValue() {
        return Double.valueOf(rangeValue.getText().toString());
    }
}
