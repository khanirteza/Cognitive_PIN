package com.halfwit.cognitivepin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;


public class UserList extends ActionBarActivity {
    private GridView gridView;
    private GridViewAdapter gridAdapter;

    labels thelabels;
    String mPath = "";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Bundle extras = getIntent().getExtras();
        mPath = extras.getString("PATH");

        thelabels = new  labels(mPath);
        thelabels.Read();
        //int max = thelabels.max();

        gridView = (GridView) findViewById(R.id.gridView);
        gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, getData());
        gridView.setAdapter(gridAdapter);


        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ImageItem item = (ImageItem) parent.getItemAtPosition(position);


                //Create intent
                Intent intent = new Intent(UserList.this, FaceDetection.class);
                intent.putExtra("name", item.getTitle());
                //intent.putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);

                //Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_list, menu);
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


    private ArrayList<ImageItem> getData() {
        final ArrayList<ImageItem> imageItems = new ArrayList<>();
        /*
        TypedArray imgs = getResources().obtainTypedArray(R.array.image_ids);
        for (int i = 0; i < imgs.length(); i++) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgs.getResourceId(i, -1));
            imageItems.add(new ImageItem(bitmap, "Image#" + i));
        }
        */


        int max = thelabels.max();

        for (int i = 0; i<=max; i++){
            if (thelabels.get(i) != ""){
                File root = new File(mPath);
                final String fname = thelabels.get(i);
                FilenameFilter pngFilter = new FilenameFilter(){
                    public boolean accept(File dir, String name){
                        return name.toLowerCase().startsWith(fname.toLowerCase() + "-");
                    }
                };
                File[] imageFiles = root.listFiles(pngFilter);
                if (imageFiles.length > 0){
                    InputStream is;
                    try{
                        is = new FileInputStream(imageFiles[i]);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        imageItems.add(new ImageItem(bitmap, thelabels.get(i)));
                    } catch (FileNotFoundException e){
                        Log.e("File error", e.getMessage() + " " + e.getCause());
                        e.printStackTrace();
                    }
                }
            }
        }

        return imageItems;
    }



}
