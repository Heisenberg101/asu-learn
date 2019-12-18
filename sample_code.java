package com.example.practiceasl;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class uploadVideo extends AppCompatActivity {

    Button uploadButton;
    Button recordButton;
    EditText textGroupNumber;
    EditText textAsuId;
    private Uri fileUri;
    private static final int VIDEO_CAPTURE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        Bundle bundle = getIntent().getExtras();
        final String label = bundle.getString("label");

        uploadButton = findViewById(R.id.upload);

        recordButton = findViewById(R.id.record);
        textGroupNumber = findViewById(R.id.editTextGroupNumber);
        textAsuId = findViewById(R.id.editTextASUID);

        if (!hasCamera()) {
            recordButton.setEnabled(false);
        }

        /**
         * Add your details....currently populated with random values
         * todo: add UI fields for ASU ID, Group Number and label and pass those here
         * label: the action selected from dropdown
         */

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupNumber = textGroupNumber.getText().toString();
                String asuID = textAsuId.getText().toString();
                startRecording(groupNumber, asuID, label);
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String groupNumber = textGroupNumber.getText().toString();
                String asuID = textAsuId.getText().toString();
                UploadTask upload = new UploadTask();
                Toast.makeText(getApplicationContext(), "Starting to Upload", Toast.LENGTH_LONG).show();
                upload.execute(groupNumber, asuID, label);
            }
        });
        StrictMode.VmPolicy.Builder newbuilder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(newbuilder.build());

    }


    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY);
    }


    public void startRecording(String groupNumber, String asuID, String label) {

        //Environment.getExternalStorageDirectory()
        File directory = new File("/storage/emulated/0/recorded" + "/"
                + groupNumber + "/" + asuID + "/" + label + "/");

        if (!directory.exists()) {
            directory.mkdirs();
        }
        int count = 0;
        if (directory.list() != null)
            count = directory.list().length;

        String fname = "video_" + label + "_" + (count + 1) + ".mp4";
        File mediaFile = new File(directory, fname);

        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5);
        fileUri = Uri.fromFile(mediaFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, VIDEO_CAPTURE);

    }

    public class UploadTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                //String url = "http://10.218.107.121/cse535/upload_video.php";
                //String url = "http://10.0.2.2:8888/upload_video.php";
                String url = "http://0e3b6f74.ngrok.io/upload_video.php";
                String charset = "UTF-8";

                String group_id = params[0];//"1";
                String asuID = params[1];//"1217045934";
                String accept = "1";
                String label = params[2];


                /*
                File directory = new File("/storage/emulated/0/recorded"+"/"
                        + group_id+"/"+asuID+"/"+label+"/");

                String fname = "video_" + label+"_"+ (directory.list().length) + ".mp4";*/

                File videoFile = new File(fileUri.getPath());

                String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
                String CRLF = "\r\n"; // Line separator required by multipart/form-data.

                URLConnection connection;

                connection = new URL(url).openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);


                try (
                        OutputStream output = connection.getOutputStream();
                        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
                ) {
                    // Send normal accept.
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"accept\"").append(CRLF);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                    writer.append(CRLF).append(accept).append(CRLF).flush();

                    // Send normal accept.
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"id\"").append(CRLF);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                    writer.append(CRLF).append(asuID).append(CRLF).flush();

                    // Send normal accept.
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"group_id\"").append(CRLF);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                    writer.append(CRLF).append(group_id).append(CRLF).flush();

                    // Send video file.
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"uploaded_file\"; filename=\"" + videoFile.getName() + "\"").append(CRLF);
                    writer.append("Content-Type: video/mp4; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
                    writer.append(CRLF).flush();
                    FileInputStream vf = new FileInputStream(videoFile);
                    try {
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        while ((bytesRead = vf.read(buffer, 0, buffer.length)) >= 0) {
                            output.write(buffer, 0, bytesRead);

                        }
                        //output.close();
                        System.out.println("***********upload done********");
//                        Toast.makeText(getApplicationContext(),"Read Done", Toast.LENGTH_LONG).show();
                    } catch (Exception exception) {
                        Toast.makeText(getApplicationContext(), "output exception in catch....." + exception + "", Toast.LENGTH_LONG).show();
                        Log.d("Error", String.valueOf(exception));
                        publishProgress(String.valueOf(exception));
                        //output.close();
                    }

                    output.flush(); // Important before continuing with writer!
                    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                    // End of multipart/form-data.
                    writer.append("--" + boundary + "--").append(CRLF).flush();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Request is lazily fired whenever you need to obtain information about response.
                int responseCode = ((HttpURLConnection) connection).getResponseCode();
                //System.out.println(responseCode); // Should be 200
                //System.out.println("*****done****");
                Log.d("Upload", responseCode + "");
                publishProgress(responseCode + "");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... text) {
            Toast.makeText(getApplicationContext(), "In Background Task " + text[0], Toast.LENGTH_LONG).show();
        }

    }
}
