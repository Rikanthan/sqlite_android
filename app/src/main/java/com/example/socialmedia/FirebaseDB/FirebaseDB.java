package com.example.socialmedia.FirebaseDB;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;

public class FirebaseDB {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference reference = database.getReference();
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
    private String downloadImageUrl;
    private StorageTask mUploadTask;
    public void insertRecords(HashMap hashMap){
        reference
                .child("Users")
                .child((String) hashMap.get("id"))
                .setValue(hashMap);

    }
    private String getFileExtension(byte[] content)  {
        InputStream is = new ByteArrayInputStream(content);
        String mimeType = null;
        try {
            mimeType = URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "."+mimeType;
    }

    public void uploadFile(byte[] image, HashMap user, ProgressBar progressBar)
    {
        if (image.length > 0) {
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()
                    + "." + getFileExtension(image));
            final UploadTask uploadTask = fileReference.putBytes(image);
            mUploadTask = fileReference.putBytes(image)
                    .addOnSuccessListener(taskSnapshot -> {
                        Handler handler = new Handler();
                        handler.postDelayed(
                                () -> progressBar.setProgress(0), 500);

                        Task<Uri> uriTask = uploadTask.continueWithTask(task -> {
                            if(!task.isSuccessful())
                            {
                                throw task.getException();
                            }
                            return fileReference.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if(task.isSuccessful())
                            {
                                downloadImageUrl = task.getResult().toString();
                                user.put("image",downloadImageUrl);
                                insertRecords(user);
                            }
                        });

                        /**/
                    })
                    .addOnFailureListener(e -> {
                      //  Toast.makeText(imageupload.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        if(progress == 100){
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                       progressBar.setProgress((int) progress);
                    });
        } else {

        }

    }
}
