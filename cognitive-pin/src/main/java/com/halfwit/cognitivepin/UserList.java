package com.halfwit.cognitivepin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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

    private labels thelabels;
    private String mPath = "";
    private int count = 0;
    private Bitmap bmlist[];
    private String namelist[];
    private SharedPreferences userInfo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Bundle extras = getIntent().getExtras();
        mPath = extras.getString("PATH");
        count = 0;

        thelabels = new  labels(mPath);
        thelabels.Read();
        //int max = thelabels.max();

        userInfo = getSharedPreferences(getString(R.string.user_info_file), Context.MODE_PRIVATE);

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

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                final ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                new AlertDialog.Builder(UserList.this).setTitle("Delete user?")
                        .setMessage("Would you like to delete this user?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File root = new File(mPath);

                                SharedPreferences.Editor editor = userInfo.edit();
                                editor.remove(item.getTitle());
                                editor.commit();

                                FilenameFilter pngFilter = new FilenameFilter() {
                                    public boolean accept(File dir, String n) {
                                        String s = item.getTitle();
                                        return n.toLowerCase().startsWith(s.toLowerCase() + "-");

                                    }
                                };

                                File[] imageFiles = root.listFiles(pngFilter);
                                for (File image : imageFiles) {
                                    image.delete();
                                    int i;
                                    for (i = 0; i < count; i++) {
                                        if (namelist[i].equalsIgnoreCase(item.getTitle())) {
                                            int j;
                                            for (j = i; j < count - 1; j++) {
                                                namelist[j] = namelist[j + 1];
                                                bmlist[j] = bmlist[j + 1];
                                            }
                                            count--;
                                            refresh();
                                            //     	        			  finish();
                                            // startActivity(getIntent());

                                            //
                                            break;
                                        }
                                    }
                                }

                                UserList.this.recreate();
                            }
                        })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                return true;
            }
        });
    }

    /*@Override
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
    }*/


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

        for (int i = 0; i <= max; i++){
            if (thelabels.get(i) != "")
                count++;
        }

        namelist = new String[count];
        bmlist = new Bitmap[count];

        count = 0;

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

                        bmlist[count] = BitmapFactory.decodeStream(is);
                        namelist[count] = thelabels.get(i);

                        //Bitmap bitmap = BitmapFactory.decodeStream(is);
                        Bitmap bitmap = bmlist[count];
                        imageItems.add(new ImageItem(bitmap, thelabels.get(i)));
                    } catch (FileNotFoundException e){
                        Log.e("File error", e.getMessage() + " " + e.getCause());
                        e.printStackTrace();
                    }
                }
                count++;
            }
        }

        return imageItems;
    }

    public void refresh(){

    }

}
