package com.example.code02;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String COUNT_VALUE="count_value";
    private int count = 0;
    private TextView tvCount;

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        count = savedInstanceState.getInt(COUNT_VALUE,0);
        if(tvCount != null){
            tvCount.setText(String.format(Locale.CHINESE,"%d",count));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putInt(COUNT_VALUE,count);
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView tvCount = findViewById(R.id.tvCount);

        Button btnCount = findViewById(R.id.btnCount);
        Button btnShowToast = findViewById(R.id.btnShowToast);
        btnShowToast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,
                        "Hello World!", Toast.LENGTH_SHORT).show();
            }
        });


        btnCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view){
                tvCount.setText(Integer.toString(++count));
            }
        });
    }
}
