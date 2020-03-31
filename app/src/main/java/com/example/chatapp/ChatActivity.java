package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    RecyclerView recyclerView;
    RecyclerViewAdapter recyclerViewAdapter;
    EditText messageText;
    FirebaseDatabase database;
    DatabaseReference databaseReference;

    private ArrayList<String> chatMessages= new ArrayList<>();


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.option_menu_sign_out){
            mAuth.signOut();

            Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
            startActivity(intent);

        }else  if (item.getItemId() == R.id.option_menu_profile){
            Intent intent = new Intent(getApplicationContext(),ProfileActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        messageText = findViewById(R.id.messageText);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewAdapter= new RecyclerViewAdapter(chatMessages);

        RecyclerView.LayoutManager recyclerViewManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(recyclerViewManager);
        //recyclerViewManager.canScrollHorizontally();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerViewAdapter);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        getData();


    }

    public void sendMessage(View view){

        final String messageToSend = messageText.getText().toString();

        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();

        FirebaseUser user = mAuth.getCurrentUser();
        String userEmail = user.getEmail().toString();

        databaseReference.child("Chats").child(uuidString).child("usermessage").setValue(messageToSend);
        databaseReference.child("Chats").child(uuidString).child("useremail").setValue(userEmail);
        databaseReference.child("Chats").child(uuidString).child("usermessagetime").setValue(ServerValue.TIMESTAMP);


        messageText.setText("");

        getData();
    }

    public void getData(){
        DatabaseReference newReference = database.getReference("Chats");

        Query query = newReference.orderByChild("usermessagetime");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //System.out.println("dataSnapshotChildren"+ dataSnapshot.getChildren());
                //System.out.println("dataSnapshotValue"+ dataSnapshot.getValue());
                //System.out.println("dataSnapshotKey"+ dataSnapshot.getKey());

                chatMessages.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()){

                    HashMap<String, String> hashMap =(HashMap<String, String>) ds.getValue();

                    System.out.println("HashMap:" + hashMap.toString());

                    String userEmail = hashMap.get("useremail");
                    String userMessages = hashMap.get("usermessage");

                    chatMessages.add(userEmail + " "+userMessages);

                    recyclerViewAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
             Toast.makeText(getApplicationContext(),databaseError.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}