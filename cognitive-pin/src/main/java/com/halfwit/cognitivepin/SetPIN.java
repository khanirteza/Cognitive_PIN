package com.halfwit.cognitivepin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;


public class SetPIN extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);
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

    // Saving the PIN code when Save PIN button is clicked
    public void savePIN(View view){
        Intent intent = new Intent(this, InputPIN.class);
        EditText editText = (EditText) findViewById(R.id.PINtext);
        Bundle bundle = new Bundle();
        RadioButton radioLeft = (RadioButton) findViewById(R.id.radioLeft);
        RadioButton radioRight = (RadioButton) findViewById(R.id.radioRight);
        if ((editText.length() == 4) && (radioLeft.isChecked() || radioRight.isChecked())) {
            bundle.putString("EXTRA_PIN", editText.getText().toString());

            if (radioLeft.isChecked())
                bundle.putString("EXTRA_SELECTION", "left");
            else
                bundle.putString("EXTRA_SELECTION", "right");

            intent.putExtras(bundle);

            startActivity(intent);
        } else if (editText.length() < 4) {
            Toast.makeText(getApplicationContext(), "PIN has to be 4 digits!", Toast.LENGTH_LONG).show();
        } else if (!radioLeft.isChecked() || !radioRight.isChecked()) {
            Toast.makeText(getApplicationContext(), "Select Left or Right!", Toast.LENGTH_LONG).show();
        }
    }

}
