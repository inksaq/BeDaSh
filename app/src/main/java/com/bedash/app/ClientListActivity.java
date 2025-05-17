package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientListActivity extends BaseActivity {
    private static final String TAG = "ClientListActivity";

    private ListView clientListView;
    private TextView emptyTextView;
    private ArrayList<Map<String, String>> clientList;
    private SimpleAdapter adapter;

    // Firebase
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);
        setupBase();

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews();

        // Load client list
        loadClientList();
    }

    private void initializeViews() {
        clientListView = findViewById(R.id.client_list_view);
        emptyTextView = findViewById(R.id.empty_text_view);

        // Initialize client list
        clientList = new ArrayList<>();

        // Set up adapter
        adapter = new SimpleAdapter(
                this,
                clientList,
                R.layout.client_list_item,
                new String[]{"name", "details"},
                new int[]{R.id.client_name, R.id.client_details}
        );

        clientListView.setAdapter(adapter);

        // Set empty view
        clientListView.setEmptyView(emptyTextView);

        // Set item click listener
        clientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get selected client
                Map<String, String> selectedClient = clientList.get(position);
                String clientId = selectedClient.get("id");
                String clientName = selectedClient.get("name");

                // Navigate to client dashboard
                navigateToClientDashboard(clientId, clientName);
            }
        });
    }

    private void loadClientList() {
        // Show loading
        emptyTextView.setText("Loading clients...");

        // Get current user ID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            emptyTextView.setText("You must be logged in to view clients");
            return;
        }

        String nurseId = currentUser.getUid();

        // Reference to nurse's clients
        DatabaseReference nurseClientsRef = mDatabase.child("nurses")
                .child(nurseId).child("clients");

        nurseClientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists() || !dataSnapshot.hasChildren()) {
                    // No clients found
                    emptyTextView.setText("No clients found. Create a new client to get started.");
                    return;
                }

                // Get client IDs associated with this nurse
                final int[] clientCount = {0};
                final int totalClients = (int) dataSnapshot.getChildrenCount();

                for (DataSnapshot clientSnapshot : dataSnapshot.getChildren()) {
                    String clientId = clientSnapshot.getKey();
                    if (clientId != null) {
                        // Get client details
                        mDatabase.child("clients").child(clientId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot clientDataSnapshot) {
                                        clientCount[0]++;

                                        if (clientDataSnapshot.exists()) {
                                            // Create client map
                                            Map<String, String> clientMap = new HashMap<>();
                                            clientMap.put("id", clientId);

                                            // Get client name
                                            String name = clientDataSnapshot.child("name").getValue(String.class);
                                            if (name != null) {
                                                clientMap.put("name", name);
                                            } else {
                                                clientMap.put("name", "Unknown Client");
                                            }

                                            // Get additional details for display
                                            StringBuilder details = new StringBuilder();

                                            // Age
                                            if (clientDataSnapshot.child("age").exists()) {
                                                details.append("Age: ")
                                                        .append(clientDataSnapshot.child("age").getValue())
                                                        .append(" | ");
                                            }

                                            // Goal calories
                                            if (clientDataSnapshot.child("goalCalories").exists()) {
                                                details.append("Goal: ")
                                                        .append(clientDataSnapshot.child("goalCalories").getValue())
                                                        .append(" kcal");
                                            }

                                            clientMap.put("details", details.toString());

                                            // Add to list
                                            clientList.add(clientMap);
                                            adapter.notifyDataSetChanged();
                                        }

                                        // Check if all clients have been processed
                                        if (clientCount[0] >= totalClients) {
                                            // All clients loaded
                                            if (clientList.isEmpty()) {
                                                emptyTextView.setText("No valid clients found.");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e(TAG, "Error loading client data", databaseError.toException());
                                        Toast.makeText(ClientListActivity.this,
                                                "Error loading client data: " + databaseError.getMessage(),
                                                Toast.LENGTH_SHORT).show();

                                        clientCount[0]++;
                                        // Check if all clients have been attempted
                                        if (clientCount[0] >= totalClients && clientList.isEmpty()) {
                                            emptyTextView.setText("Error loading clients. Please try again.");
                                        }
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading nurse clients", databaseError.toException());
                emptyTextView.setText("Error loading clients. Please try again.");
                Toast.makeText(ClientListActivity.this,
                        "Error: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToClientDashboard(String clientId, String clientName) {
        Intent intent = new Intent(ClientListActivity.this, ClientDashboardActivity.class);
        intent.putExtra("client_id", clientId);
        intent.putExtra("client_name", clientName);
        startActivity(intent);
    }
}