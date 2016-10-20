package com.example.tom.test2;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private final int SELECT_PHOTO_ENCRYPT = 0;
    private ListView list;
    private ArrayAdapter adapter;
    private ArrayList<String> filesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        list = (ListView) findViewById(R.id.list);
        filesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_1,android.R.id.text1, filesList);
        list.setAdapter(adapter);

        listFiles();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                clicked(list.getAdapter().getItem(arg2).toString());
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO_ENCRYPT);


            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFiles();
            }
        });
    }

    private void loadPhoto(ImageView imageView) {

        ImageView tempImageView = imageView;


        AlertDialog.Builder imageDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.custom_fullimage_dialog,
                (ViewGroup) findViewById(R.id.layout_root));
        ImageView image = (ImageView) layout.findViewById(R.id.fullimage);
        image.setImageDrawable(tempImageView.getDrawable());
        imageDialog.setView(layout);
        imageDialog.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });


        imageDialog.create();
        imageDialog.show();
    }

    protected void clicked(final String item) {
        final EditText pwd = new EditText(this);
        final ImageView imgViewer = new ImageView(MainActivity.this);
        new AlertDialog.Builder(this)
                .setTitle("Password")
                .setView(pwd)
                .setMessage("Choose a Password")
                .setPositiveButton("Crypt", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String password = pwd.getText().toString();

                        Uri path = Uri.parse(item);
                        File file = new File(path.getPath());
                        try {

                            byte[] b = AESencrp.decrypt(getBytesFromFile(file), password.getBytes());
                            Bitmap bm = BitmapFactory.decodeByteArray(b, 0, b.length);
                            DisplayMetrics dm = new DisplayMetrics();
                            getWindowManager().getDefaultDisplay().getMetrics(dm);
                            imgViewer.setMinimumHeight(dm.heightPixels);
                            imgViewer.setMinimumWidth(dm.widthPixels);
                            imgViewer.setImageBitmap(bm);
                            loadPhoto(imgViewer);
                        } catch (Exception e) {
                            Log.e("error", e.toString());
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();


    }

    // Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file); //getContentResolver().openInputStream(path)?

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected byte[] readFile(String image) {
        Uri path = Uri.parse(image);
        File file = new File(getRealPathFromURI(path));
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(getContentResolver().openInputStream(path));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }

    protected void writeToFile(byte[] content, String filename) {
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(content);
            outputStream.close();
            listFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void listFiles() {
        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();
        filesList.clear();
        if (subFiles != null) {
            for (File file : subFiles) {
                if (file.getAbsolutePath().endsWith("jpg")) {
                    filesList.add(file.getAbsolutePath());
                    Log.e("file", file.getAbsolutePath());
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    protected void deleteFiles() {
        File dir = getFilesDir();
        File[] subFiles = dir.listFiles();
        if (subFiles != null) {
            for (File file : subFiles) {
                if (file.getAbsolutePath().endsWith("jpg")) {
                    file.delete();
                }
            }
        }
        listFiles();
    }

    protected void Crypt(String image, String password, boolean crypt) {
        if (crypt) {
            byte[] baseCode = this.readFile(image);
            byte[] crypted;
            try {
                crypted = AESencrp.encrypt(baseCode, password.getBytes());
                writeToFile(crypted, String.format("%d.jpg", (int) (Math.random() * 1000)));
                listFiles();
            } catch (Exception e) {
                Log.e("Error", e.toString());
            }


            Snackbar.make(findViewById(android.R.id.content), String.format("%s: %s with password %s", getResources().getString(R.string.main_chose_encrypt), image, password), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            //encrypt

            Snackbar.make(findViewById(android.R.id.content), String.format("%s: %s with password %s", getResources().getString(R.string.main_chose_decrypt), image, password), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

    }

    protected void getPassword(final String image, final boolean crypt) { // crypt == 0 = decrypt , crypt == 1 = encrypt
        final EditText pwd = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Password")
                .setView(pwd)
                .setMessage("Choose a Password")
                .setPositiveButton("Crypt", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Crypt(image, pwd.getText().toString(), crypt);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO_ENCRYPT:
                if (resultCode == RESULT_OK) {
                    final Uri imageUri = imageReturnedIntent.getData();
                    getPassword(imageUri.toString(), true);

                }
                break;
        }
    }
}