package com.mv.iot_cp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {



    RemoteModelManager modelManager;

    HashMap<String, Task<Void>> pendingDownloads = new HashMap<>();






    EditText editTextMessage, editTextTranslate;
    Button submitButton, translateButton;


    // creating a variable for our
    // Firebase Database.
    FirebaseDatabase firebaseDatabase;

    // creating a variable for our Database
    // Reference for Firebase.
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editTextMessage = findViewById(R.id.editTextMessage);
        submitButton = findViewById(R.id.submitButton);
        translateButton = findViewById(R.id.translateButton);
        editTextTranslate = findViewById(R.id.editTextTranslate);



        // below line is used to get the
        // instance of our Firebase database.
        firebaseDatabase = FirebaseDatabase.getInstance();

        // below line is used to get reference for our database.
        databaseReference = firebaseDatabase.getReference();


        modelManager = RemoteModelManager.getInstance();





        fetchDownloadedModels();


        downloadLanguage();
        downloadLanguage();
        downloadLanguage();


        fetchDownloadedModels();








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





                TranslatorOptions options =
                        new TranslatorOptions.Builder()
                                .setSourceLanguage(TranslateLanguage.ENGLISH)
                                .setTargetLanguage(TranslateLanguage.MARATHI)
                                .build();
                Translator englishMarathiTranslator =
                        Translation.getClient(options);

                /*englishMarathiTranslator.translate(editTextMessage.getText().toString())
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
                //englishMarathiTranslator.downloadModelIfNeeded(conditions)
                englishMarathiTranslator.downloadModelIfNeeded()
                        .addOnSuccessListener(
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        Toast.makeText(MainActivity.this, "Translating....", Toast.LENGTH_SHORT).show();

                                        englishMarathiTranslator.translate(editTextMessage.getText().toString())
                                                .addOnSuccessListener(
                                                        new OnSuccessListener<String>() {
                                                            @Override
                                                            public void onSuccess(String s) {
                                                                editTextTranslate.setText(s);

                                                                databaseReference.child("string").setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Toast.makeText(MainActivity.this, "Successful!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Completed : ", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(MainActivity.this, "Successful!!!!", Toast.LENGTH_SHORT).show();
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