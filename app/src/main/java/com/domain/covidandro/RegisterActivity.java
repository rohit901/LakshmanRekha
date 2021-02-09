package com.domain.covidandro;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;



import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    EditText nameBox;
    EditText editText;
    EditText startText;
    EditText pinBox;
    EditText periodBox;
    Calendar myCalendar;
    Calendar myCalendar2;
    Button takePhoto;

    public static final String SHARED_PREF = "com.domain.covidandro";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

         myCalendar = Calendar.getInstance();
         myCalendar2 = Calendar.getInstance();
         takePhoto = (Button)findViewById(R.id.complete_button);

        editText = (EditText) findViewById(R.id.dob_box);
        startText = (EditText) findViewById(R.id.startDate_box);
        nameBox = (EditText) findViewById(R.id.name_box);
        pinBox = (EditText) findViewById(R.id.pin_box);
        periodBox = (EditText) findViewById(R.id.period_box);

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
        DatePickerDialog.OnDateSetListener startdate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar2.set(Calendar.YEAR, year);
                myCalendar2.set(Calendar.MONTH, month);
                myCalendar2.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel2();
            }
        };

        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(RegisterActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        startText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(RegisterActivity.this, startdate, myCalendar2.get(Calendar.YEAR),
                        myCalendar2.get(Calendar.MONTH), myCalendar2.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(nameBox.getText().toString().trim().equalsIgnoreCase("")){
                    nameBox.setError("This field cannot be blank.");
                }
                else if(editText.getText().toString().trim().equalsIgnoreCase("")){
                    editText.setError("This field cannot be blank.");
                }
                else if(pinBox.getText().toString().trim().equalsIgnoreCase("")){
                    pinBox.setError("This field cannot be blank.");
                }
                else if(periodBox.getText().toString().trim().equalsIgnoreCase("")){
                    periodBox.setError("This field cannot be blank.");
                }
                else if(startText.getText().toString().trim().equalsIgnoreCase("")){
                    startText.setError("This field cannot be blank.");
                }
                else {

                    SharedPreferences mPrefs = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    prefsEditor.putString("name_str", nameBox.getText().toString().trim()).apply();
                    prefsEditor.putString("dob_str", editText.getText().toString().trim()).apply();
                    prefsEditor.putString("pin_str", pinBox.getText().toString().trim()).apply();
                    prefsEditor.putString("period_str", periodBox.getText().toString().trim()).apply();
                    prefsEditor.putString("start_str", startText.getText().toString().trim()).apply();


                    Intent intent = new Intent(RegisterActivity.this, CameraXLivePreviewActivity.class);
                    startActivity(intent);
                }
            }
        });

    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editText.setText(sdf.format(myCalendar.getTime()));
    }
    private void updateLabel2() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(startText.getWindowToken(), 0);

        startText.setText(sdf.format(myCalendar2.getTime()));
    }
}
