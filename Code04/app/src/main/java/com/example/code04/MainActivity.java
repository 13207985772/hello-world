package com.example.code04;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static final String MESSAGE = MainActivity.class.getName()+"_MESSAGE";
    private EditText etMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMessage = findViewById(R.id.message);
        Button btSendMessage = findViewById(R.id.send_message);

        btSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = etMessage.getText().toString();
                Intent intent = new Intent(MainActivity.this,MessageActivity.class);
                intent.putExtra(MESSAGE,message);
                startActivity(intent);
            }
        });
    }
}