package com.mv.iot_cp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {



    RemoteModelManager modelManager;

    HashMap<String, Task<Void>> pendingDownloads = new HashMap<>();






    EditText editTextMessage, editTextTranslate;
    Button submitButton, translateButton, speakButton, speakTranslatedButton;
    Spinner dropdownLanguageSelect;


    // creating a variable for our
    // Firebase Database.
    FirebaseDatabase firebaseDatabase;

    // creating a variable for our Database
    // Reference for Firebase.
    DatabaseReference databaseReference;

    TextToSpeech textToSpeech;

    String[] dropdownLanguageSelectItems = new String[]{"Marathi", "Kannada", "Gujarati", "Bengali", "Hindi", "Tamil"};
    String selectedLanguage = "Marathi";



    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editTextMessage = findViewById(R.id.editTextMessage);
        submitButton = findViewById(R.id.submitButton);
        translateButton = findViewById(R.id.translateButton);
        editTextTranslate = findViewById(R.id.editTextTranslate);
        speakButton = findViewById(R.id.speakButton);
        speakTranslatedButton = findViewById(R.id.speakTranslatedButton);
        dropdownLanguageSelect = findViewById(R.id.dropdownLanguageSelect);



        // below line is used to get the
        // instance of our Firebase database.
        firebaseDatabase = FirebaseDatabase.getInstance();

        // below line is used to get reference for our database.
        databaseReference = firebaseDatabase.getReference();


        modelManager = RemoteModelManager.getInstance();


        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();




        fetchDownloadedModels();


        downloadLanguage();
        downloadLanguage();
        downloadLanguage();


        fetchDownloadedModels();


        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if(i!=TextToSpeech.ERROR){
                    // To Choose language of speech
                    textToSpeech.setLanguage(new Locale("hin","IND",""));
                }
            }
        });







        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, dropdownLanguageSelectItems);
        dropdownLanguageSelect.setAdapter(adapter);
        dropdownLanguageSelect.setSelection(0);
        dropdownLanguageSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedLanguage = dropdownLanguageSelectItems[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });








        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("string").setValue(editTextMessage.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Successful!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                downloadLanguage();

                TranslatorOptions options;
                switch (selectedLanguage){
                    case "Marathi":
                        options = new TranslatorOptions.Builder()
                                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                                    .setTargetLanguage(TranslateLanguage.MARATHI)
                                    .build();
                        break;
                    case "Kannada":
                        options = new TranslatorOptions.Builder()
                                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                                    .setTargetLanguage(TranslateLanguage.KANNADA)
                                    .build();
                        break;
                    case "Gujarati":
                        options = new TranslatorOptions.Builder()
                                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                                    .setTargetLanguage(TranslateLanguage.GUJARATI)
                                    .build();
                        break;
                    case "Bengali":
                        options = new TranslatorOptions.Builder()
                                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                                    .setTargetLanguage(TranslateLanguage.BENGALI)
                                    .build();
                        break;
                    case "Hindi":
                        options = new TranslatorOptions.Builder()
                                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                                    .setTargetLanguage(TranslateLanguage.HINDI)
                                    .build();
                        break;
                    case "Tamil":
                        options = new TranslatorOptions.Builder()
                                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                                    .setTargetLanguage(TranslateLanguage.TAMIL)
                                    .build();
                        break;
                    default:
                        options = new TranslatorOptions.Builder()
                                .setSourceLanguage(TranslateLanguage.ENGLISH)
                                .setTargetLanguage(TranslateLanguage.MARATHI)
                                .build();

                }






                Translator englishToSelectedLanguageTranslator =
                        Translation.getClient(options);

                /*englishToSelectedLanguageTranslator.translate(editTextMessage.getText().toString())
                        .addOnSuccessListener(
                                new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        editTextTranslate.setText(s);
                                    }
                                }
                        );*/

                Toast.makeText(MainActivity.this, "Downloading....", Toast.LENGTH_SHORT).show();

                /*DownloadConditions conditions = new DownloadConditions.Builder()
                        .requireWifi()
                        .build();*/
                //englishToSelectedLanguageTranslator.downloadModelIfNeeded(conditions)
                englishToSelectedLanguageTranslator.downloadModelIfNeeded()
                        .addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        Toast.makeText(MainActivity.this, "Translating....", Toast.LENGTH_SHORT).show();

                                        englishToSelectedLanguageTranslator.translate(editTextMessage.getText().toString())
                                                .addOnSuccessListener(
                                                        new OnSuccessListener<String>() {
                                                            @Override
                                                            public void onSuccess(String s) {
                                                                editTextTranslate.setText(s);

                                                                databaseReference.child("string").setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Toast.makeText(MainActivity.this, "Successful in uploading!", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }
                                                );
                                    }
                                }
                        ).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Exception : " + e, Toast.LENGTH_SHORT).show();
                        Log.d("QWER", "Exception : " + e);
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Toast.makeText(MainActivity.this, "Completed downloading", Toast.LENGTH_SHORT).show();
                        Log.d("QWER", "Completed : ");
                    }
                }).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        Toast.makeText(MainActivity.this, "Cancelled : ", Toast.LENGTH_SHORT).show();
                        Log.d("QWER", "Cancelled : ");
                    }
                }).continueWithTask(
                        new Continuation<Void, Task<String>>() {
                            @Override
                            public Task<String> then(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //Toast.makeText(MainActivity.this, "Successful!!!!", Toast.LENGTH_SHORT).show();
                                    Log.d("QWER", "Successful!!!!");
                                    Exception e = task.getException();
                                    return Tasks.forException(e);
                                } else {
                                    Exception e = task.getException();
                                    Toast.makeText(MainActivity.this, "Exception : " + e, Toast.LENGTH_SHORT).show();
                                    Log.d("QWER", "Exception : " + e);
                                    return Tasks.forException(e);
                                }
                            }
                        })
                ;





            }
        });


        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(editTextMessage.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                Toast.makeText(MainActivity.this, "Speaking message!", Toast.LENGTH_SHORT).show();
                upload_audio(editTextMessage);

            }
        });


        speakTranslatedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech.speak(editTextTranslate.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                Toast.makeText(MainActivity.this, "Speaking message!", Toast.LENGTH_SHORT).show();
                upload_audio(editTextTranslate);

            }
        });

    }




    void upload_audio(TextView textView) {
        // Create a file object with the desired path and name
        // Use the File.separator constant to avoid hard-coding the slash character
        File destinationFile = new File(Environment.getExternalStorageDirectory() + File.separator + "IOT_CP Files" + File.separator + "Audio" + File.separator + "tts_audio.wav");

        // Check if the external storage is writable
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Use the synthesizeToFile method to save the tts output to the file
            textToSpeech.synthesizeToFile(textView.getText().toString(), null, destinationFile, "tts_id");
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // Handle the case when the external storage is not available
            Toast.makeText(MainActivity.this, "Cannot save file!", Toast.LENGTH_SHORT).show();
        }

        StorageReference ref = storageReference.child("audio/");
        Uri file = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + File.separator + "IOT_CP Files" + File.separator + "Audio" + File.separator + "tts_audio.wav"));
        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = getApplicationContext().getContentResolver().openAssetFileDescriptor(file , "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Size : " + fileDescriptor.getLength(), Toast.LENGTH_SHORT).show();
        ref.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        //Toast.makeText(MainActivity.this, "Audio Uploaded!!", Toast.LENGTH_SHORT).show();
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Display the url in a toast message
                                Toast.makeText(MainActivity.this, "Audio Uploaded!! URL: " + uri.toString(), Toast.LENGTH_SHORT).show();
                                Toast.makeText(MainActivity.this, "Size : " + taskSnapshot.getTotalByteCount(), Toast.LENGTH_SHORT).show();
                                databaseReference.child("audioURL").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //Toast.makeText(MainActivity.this, "Successful!", Toast.LENGTH_SHORT).show();
                                        Log.d("QWER", "Uploaded URL on Firebase");
                                    }
                                });
                            }
                        });
                    }
                })

        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                // Error
                Toast.makeText(MainActivity.this, "Error! Cannot upload on cloud!", Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void fetchDownloadedModels() {
        modelManager.getDownloadedModels(TranslateRemoteModel.class)
                .addOnSuccessListener(
                        new OnSuccessListener<Set<TranslateRemoteModel>>() {
                            @Override
                            public void onSuccess(Set<TranslateRemoteModel> remoteModels) {
                                List<String> modelCodes = new ArrayList<>(remoteModels.size());
                                for (TranslateRemoteModel model : remoteModels) {
                                    modelCodes.add(model.getLanguage());
                                }
                                Collections.sort(modelCodes);
                                Log.d("QWER", "ModelCodes : " + modelCodes);
                            }
                        });
    }

    // Starts downloading a remote model for local translation.
    void downloadLanguage() {
        TranslateRemoteModel model = getModel(TranslateLanguage.fromLanguageTag("mr"));
        Task<Void> downloadTask;
        if (pendingDownloads.containsKey("mr")) {
            downloadTask = pendingDownloads.get("mr");
            // found existing task. exiting
            if (downloadTask != null && !downloadTask.isCanceled()) {
                return;
            }
        }
        Log.d("QWER", "Downloading MR");
        downloadTask =
                modelManager
                        .download(model, new DownloadConditions.Builder().build())
                        .addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d("QWER", "Downloaded MR");
                                        pendingDownloads.remove("mr");
                                        fetchDownloadedModels();
                                    }
                                });
        pendingDownloads.put("mr", downloadTask);
    }


    private TranslateRemoteModel getModel(String languageCode) {
        return new TranslateRemoteModel.Builder(languageCode).build();
    }


}