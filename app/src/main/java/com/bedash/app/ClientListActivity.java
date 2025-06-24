package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientListActivity extends BaseActivity {
    private static final String TAG = "ClientListActivity";

    private ListView clientListView;
    private TextView emptyTextView;
    private ArrayList<Map<String, String>> clientList;
    private SimpleAdapter adapter;
    private ImageButton backButton;

    // Firestore
    private FirestoreManager mFirestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);
        setupBase();

        // Initialize Firestore Manager
        mFirestoreManager = FirestoreManager.getInstance();

        // Initialize views
        initializeViews();


        setupButton();


        // Load client list
        loadClientList();

    }


    private void initializeViews() {
        clientListView = findViewById(R.id.client_list_view);
        emptyTextView = findViewById(R.id.empty_text_view);
        backButton = findViewById(R.id.btn_back);


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


            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });


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

    private void setupButton() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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

        // Load clients using FirestoreManager
        mFirestoreManager.getNurseClients(new FirestoreManager.DatabaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                clientList.clear();

                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    Client client = mFirestoreManager.documentToClient(document);
                    if (client != null) {
                        // Create client map
                        Map<String, String> clientMap = new HashMap<>();
                        clientMap.put("id", client.getId());
                        clientMap.put("name", client.getName() != null ? client.getName() : "Unknown Client");

                        // Get additional details for display
                        StringBuilder details = new StringBuilder();

                        // Age
                        if (client.getAge() > 0) {
                            details.append("Age: ").append(client.getAge()).append(" | ");
                        }

                        // Goal calories
                        if (client.getGoalCalories() > 0) {
                            details.append("Goal: ").append(client.getGoalCalories()).append(" kcal");
                        }

                        clientMap.put("details", details.toString());
                        clientList.add(clientMap);
                    }
                }

                adapter.notifyDataSetChanged();

                if (clientList.isEmpty()) {
                    emptyTextView.setText("No clients found. Create a new client to get started.");
                }

                Log.d(TAG, "Loaded " + clientList.size() + " clients");
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading clients", error);
                emptyTextView.setText("Error loading clients. Please try again.");
                Toast.makeText(ClientListActivity.this,
                        "Error: " + error.getMessage(),
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
