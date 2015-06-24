package com.halfwit.cognitivepin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

import java.util.Random;


public class HomeScreen extends ActionBarActivity {

    private Random rand;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        rand = new Random();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void savePIN(View view){
        Intent intent = new Intent(this, InputPIN.class);
        EditText editText = (EditText) findViewById(R.id.PINtext);
        System.out.println(editText.getText());
        Bundle bundle = new Bundle();
        bundle.putString("EXTRA_PIN", editText.getText().toString());
        RadioButton leftRadio = (RadioButton) findViewById(R.id.radioLeft);
        if (leftRadio.isChecked())
            bundle.putString("EXTRA_SELECTION", "left");
        else
            bundle.putString("EXTRA_SELECTION", "right");

        intent.putExtras(bundle);

        startActivity(intent);
    }

}
