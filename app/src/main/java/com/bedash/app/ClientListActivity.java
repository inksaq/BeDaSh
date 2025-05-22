package com.bedash.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientListActivity extends BaseActivity {
    private static final String TAG = "ClientListActivity";

    private ListView clientListView;

    private ImageButton btn_back;
    private TextView emptyTextView;
    private ArrayList<Map<String, String>> clientList;
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);
        setupBase();

        // Initialize views
        initializeViews();

        // Load client list
        loadClientList();
    }

    private void initializeViews() {
        clientListView = findViewById(R.id.client_list_view);
        emptyTextView = findViewById(R.id.empty_text_view);
        btn_back = findViewById(R.id.btn_back);

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
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity to go to the previous one on the stack
                finish();
            }
        });
    }

    private void loadClientList() {
        // Show loading
        emptyTextView.setText("Loading clients...");

        // Get current user's clients using FirestoreManager
        mFirestoreManager.getNurseClients(new FirestoreManager.DatabaseCallback<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                if (querySnapshot.isEmpty()) {
                    // No clients found
                    emptyTextView.setText("No clients found. Create a new client to get started.");
                    return;
                }

                // Process each client document
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    // Create client map
                    Map<String, String> clientMap = new HashMap<>();
                    clientMap.put("id", document.getId());

                    // Get client name
                    String name = document.getString("name");
                    if (name != null) {
                        clientMap.put("name", name);
                    } else {
                        clientMap.put("name", "Unknown Client");
                    }

                    // Get additional details for display
                    StringBuilder details = new StringBuilder();

                    // Age
                    if (document.contains("age")) {
                        details.append("Age: ")
                                .append(document.getLong("age"))
                                .append(" | ");
                    }

                    // Goal calories
                    if (document.contains("goalCalories")) {
                        details.append("Goal: ")
                                .append(document.getLong("goalCalories"))
                                .append(" kcal");
                    }

                    clientMap.put("details", details.toString());

                    // Add to list
                    clientList.add(clientMap);
                }

                // Update the UI
                adapter.notifyDataSetChanged();

                // Check if list is still empty after processing
                if (clientList.isEmpty()) {
                    emptyTextView.setText("No valid clients found.");
                }
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error loading nurse clients", error);
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