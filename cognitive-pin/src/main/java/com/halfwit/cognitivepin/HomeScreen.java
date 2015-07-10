package com.halfwit.cognitivepin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


public class HomeScreen extends ActionBarActivity {

    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        bundle.putString("PATH", getFilesDir() + getString(R.string.user_image_file));
    }

    /*
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
    */

    // Start the FaceTraining class
    public void newUser(View view){
        Intent intent = new Intent(this, NewUser.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // start testing the system
    public void showUserList(View view){
        Intent intent = new Intent(this, UserList.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void showFd(View view){
        Intent intent = new Intent(this, FdActivity.class);
        startActivity(intent);
    }

    public void showDetect(View view){
        startActivity(new Intent(this, FaceDetection.class).putExtras(bundle));
    }
}
