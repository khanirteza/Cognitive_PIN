package com.halfwit.cognitivepin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Random;


public class InputPIN extends Activity {

    private Random rand;
    private int[] expectedInput = new int[4];
    private int[] userInput = new int[4];
    String pin;
    String selection;
    int pass = 0;
    private TextView pinDot;
    private long startTime, endTime;
    public float x1, x2;
    static final int MIN_DISTANCE = 250;
    public int fail, success;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_pin);
        pinDot = (TextView) findViewById(R.id.pinDot);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        pin = extras.getString("EXTRA_PIN");
        selection = extras.getString("EXTRA_SELECTION");
        System.out.println(extras.getString("EXTRA_PIN"));
        System.out.println(extras.getString("EXTRA_SELECTION"));
        rand = new Random();
        randGenerator(pin, selection);
        fail = 0;
        success = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                if (deltaX > MIN_DISTANCE) {
                    rightSelection(findViewById(android.R.id.content));
                } else if (deltaX < (-1 * MIN_DISTANCE)) {
                    leftSelection(findViewById(android.R.id.content));
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_input_pin, menu);
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

    public void leftSelection(final View view) {
        if (pass == 0)
            startTime = System.currentTimeMillis();
        userInput[pass] = -1;
        pinDot.append("•");
        pass++;
        //show();
        if (pass < 4)
            randGenerator(pin, selection);
        else {
            endTime = System.currentTimeMillis();
            showResult(view);
            startOver(view);
        }
    }

    public void rightSelection(final View view) {
        if (pass == 0)
            startTime = System.currentTimeMillis();
        userInput[pass] = 1;
        pinDot.append("•");
        pass++;
        //show();
        if (pass < 4)
            randGenerator(pin, selection);
        else {
            endTime = System.currentTimeMillis();
            showResult(view);
            startOver(view);
        }
    }

    public void showResult(final View view) {
        if (Arrays.equals(expectedInput, userInput)) {
            new AlertDialog.Builder(this).setTitle("Success")
                    .setMessage("Correct PIN\nTime taken: " + (double) (endTime - startTime) / 1000 + "s" +
                            "\nSucceeded: " + ++success + "\nFailed: " + fail)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        } else {
            new AlertDialog.Builder(this).setTitle("Fail")
                    .setMessage("Wrong PIN\nTime taken: " + (double) (endTime - startTime) / 1000 + "s" +
                            "\nSucceeded: " + success + "\nFailed: " + ++fail)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }
    }

    public void startOver(View view) {
        //show();
        pass = 0;
        pinDot.setText("");
        expectedInput = new int[4];
        userInput = new int[4];
        randGenerator(pin, selection);
    }

    public void randGenerator(String pin, String selection) {
        //Generate random number for each pass

        int num1, num2, num3, num4, num5;
        char chDigit1, chDigit2, chDigit3, chDigit4, chDigit5;
        num1 = rand.nextInt(10);
        num2 = rand.nextInt(10);
        while (num1 == num2)
            num2 = rand.nextInt(10);
        num3 = rand.nextInt(10);
        while ((num1 == num3) || (num2 == num3))
            num3 = rand.nextInt(10);
        num4 = rand.nextInt(10);
        while ((num1 == num4) || (num2 == num4) || (num3 == num4))
            num4 = rand.nextInt(10);
        num5 = rand.nextInt(10);
        while ((num1 == num5) || (num2 == num5) || (num3 == num5) || (num4 == num5))
            num5 = rand.nextInt(10);

        chDigit1 = String.valueOf(num1).charAt(0);
        chDigit2 = String.valueOf(num2).charAt(0);
        chDigit3 = String.valueOf(num3).charAt(0);
        chDigit4 = String.valueOf(num4).charAt(0);
        chDigit5 = String.valueOf(num5).charAt(0);

        setNumberPadColor(num1, num2, num3, num4, num5);

        /*TextView digit1 = (TextView) findViewById(R.id.digit1);
        TextView digit2 = (TextView) findViewById(R.id.digit2);
        TextView digit3 = (TextView) findViewById(R.id.digit3);
        TextView digit4 = (TextView) findViewById(R.id.digit4);
        digit1.setText(String.valueOf(num1));
        digit2.setText(String.valueOf(num2));
        digit3.setText(String.valueOf(num3));
        digit4.setText(String.valueOf(num4));*/

        if (selection.equals("left")) {

            if ((pin.charAt(pass) == chDigit1) || (pin.charAt(pass) == chDigit2) ||
                    (pin.charAt(pass) == chDigit3) || (pin.charAt(pass) == chDigit4) ||
                    (pin.charAt(pass) == chDigit5))
                expectedInput[pass] = -1;
            else
                expectedInput[pass] = 1;
        } else {
            if ((pin.charAt(pass) == chDigit1) || (pin.charAt(pass) == chDigit2) ||
                    (pin.charAt(pass) == chDigit3) || (pin.charAt(pass) == chDigit4) ||
                    (pin.charAt(pass) == chDigit5))
                expectedInput[pass] = 1;
            else
                expectedInput[pass] = -1;
        }

    }

    public void setNumberPadColor(int num1, int num2, int num3, int num4, int num5) {
        //Set the number pad color based on passed argument
        TextView[] textDigit = new TextView[10];

        textDigit[0] = (TextView) findViewById(R.id.textDigit0);
        textDigit[1] = (TextView) findViewById(R.id.textDigit1);
        textDigit[2] = (TextView) findViewById(R.id.textDigit2);
        textDigit[3] = (TextView) findViewById(R.id.textDigit3);
        textDigit[4] = (TextView) findViewById(R.id.textDigit4);
        textDigit[5] = (TextView) findViewById(R.id.textDigit5);
        textDigit[6] = (TextView) findViewById(R.id.textDigit6);
        textDigit[7] = (TextView) findViewById(R.id.textDigit7);
        textDigit[8] = (TextView) findViewById(R.id.textDigit8);
        textDigit[9] = (TextView) findViewById(R.id.textDigit9);

        for (TextView textView : textDigit) {
            textView.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }

        textDigit[num1].setTextColor(getResources().getColor(android.R.color.black));
        textDigit[num2].setTextColor(getResources().getColor(android.R.color.black));
        textDigit[num3].setTextColor(getResources().getColor(android.R.color.black));
        textDigit[num4].setTextColor(getResources().getColor(android.R.color.black));
        textDigit[num5].setTextColor(getResources().getColor(android.R.color.black));

    }

    //for debugging purpose only
    public void show() {
        System.out.println(pass);
        System.out.print("Expected: ");
        for (int i = 0; i < expectedInput.length; i++)
            System.out.print(expectedInput[i] + " ");
        System.out.println();
        System.out.print("User: ");
        for (int i = 0; i < userInput.length; i++)
            System.out.print(userInput[i] + " ");
        System.out.println();

    }
}
