package com.example.expensetracker;

import static android.content.ContentValues.TAG;
import static androidx.core.content.ContextCompat.getSystemService;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.expensetracker.Model.Budget;
import com.example.expensetracker.Model.Data;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class BudgetFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDataBase;
    DatabaseReference budgetRef;
    int year, month;
    private TextView SetBudgetAmount;
    private Button btnSaveBudget;
    private TextView RemainingAmountTxt;
    DatabaseReference currentMonthBudgetRef;
    View myview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myview = inflater.inflate(R.layout.fragment_budget, container, false);
        SetBudgetAmount = myview.findViewById(R.id.TotalBgt);
        btnSaveBudget = myview.findViewById(R.id.btn_save_budget);
        RemainingAmountTxt = myview.findViewById(R.id.RemainingBgtText);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        assert mUser != null;
        String uid = mUser.getUid();
        mExpenseDataBase = FirebaseDatabase.getInstance().getReference().child("Expense").child(uid);
        budgetRef = FirebaseDatabase.getInstance().getReference().child("Budget").child(uid);


// Get the current month and year
        Calendar cal = Calendar.getInstance();
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1; // Add 1 because Calendar.MONTH is zero-based

        currentMonthBudgetRef = budgetRef.child(String.valueOf(year)).child(String.valueOf(month));

        // Retrieve current month budget from Firebase
        currentMonthBudgetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Budget currentMonthBudget = snapshot.getValue(Budget.class);

                if (currentMonthBudget != null) {
                    updateUI(currentMonthBudget.getBudgetLimit(),currentMonthBudget.getRemainingBudget()    );
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read current month budget from Firebase", error.toException());
            }
        });

// Check if a budget for the current month already exists
        btnSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                currentMonthBudgetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override


                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // If a budget for the current month already exists, update it
                            updateBudget(currentMonthBudgetRef);
                        } else {
                            // If a budget for the current month does not exist, set it
                            setBudget(currentMonthBudgetRef);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to read budget from Firebase", error.toException());
                    }
                });

            }
        });
        return myview;
    }
    double remainingBudget;
    // Method to set a budget for the current month
    private void setBudget(DatabaseReference currentMonthBudgetRef) {
        //double budgetLimit = Double.parseDouble(SetBudgetAmount.getText().toString()); // User-defined budget limit
        //double budgetLimit=10000;

        EditText setBgt = myview.findViewById(R.id.budget_amounSet);
        double budgetLimit = Double.parseDouble(setBgt.getText().toString().trim());

        mExpenseDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                double totalExpenses = 0.0;
                for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                    Data expense = expenseSnapshot.getValue(Data.class);

                    assert expense != null;
                    if (expense.getMonth() == month && expense.getYear() == year) {
                        totalExpenses += Double.parseDouble(expense.getAmmount());
                    }
                }
                remainingBudget = budgetLimit - totalExpenses;
                if (remainingBudget <= budgetLimit * 0.3) {
                    // Sending notification to the user
                    String title = "Budget Alert";
                    String message = "You have used 70% of your budget for this month!";
                    sendNotification(title, message);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read expenses from Firebase", databaseError.toException());
            }
        });

        //double remainingBudget = budgetLimit;

        currentMonthBudgetRef.setValue(new Budget(budgetLimit, remainingBudget, month, year))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Budget saved to Firebase");
                        updateUI(budgetLimit, remainingBudget);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to save budget to Firebase", e);
                    }
                });
    }

    // Method to update a budget for the current month
    private void updateBudget(DatabaseReference currentMonthBudgetRef) {
        currentMonthBudgetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Budget currentMonthBudget = snapshot.getValue(Budget.class);

                // Retrieve new budget limit from the user
                EditText editBudgetLimit = myview.findViewById(R.id.budget_amounSet);
                String newBudgetLimitStr = editBudgetLimit.getText().toString();
                if (newBudgetLimitStr.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a new budget limit", Toast.LENGTH_SHORT).show();
                    return;
                }
                double newBudgetLimit = Double.parseDouble(newBudgetLimitStr);

                // Listen for expense updates

                mExpenseDataBase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double totalExpenses = 0.0;
                        for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                            Data expense = expenseSnapshot.getValue(Data.class);

                            assert expense != null;
                            if (expense.getMonth() == month && expense.getYear() == year) {
                                totalExpenses += Double.parseDouble(expense.getAmmount());
                            }
                        }
                        double remainingBudget = newBudgetLimit - totalExpenses;
                        // Check if remaining budget is less than 30% of budget limit
                        if (remainingBudget <= newBudgetLimit * 0.3) {
                            // Send a Firebase Cloud Notification to notify the user
                            String title = "Budget Alert";
                            String message = "You have used 70% of your budget for this month!";
                            sendNotification(title, message);
                        }

                        // Modify current month budget with new budget limit
                        currentMonthBudgetRef.child("budgetLimit")
                                .setValue(newBudgetLimit);


                        // Update remaining budget in Firebase
                        currentMonthBudgetRef.child("remainingBudget")
                                .setValue(remainingBudget)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Remaining budget updated in Firebase");
                                        if (currentMonthBudget != null)
                                        {
                                            updateUI(newBudgetLimit, remainingBudget);
                                            Toast.makeText(getContext(), "Budget updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "Failed to update remaining budget in Firebase", e);
                                    }
                                });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to read expenses from Firebase", databaseError.toException());
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read expenses from Firebase", error.toException());
            }
        });
        Log.d(TAG, "Updated budget for the current month");
    }

    private void updateUI(double budgetLimit, double remainingBudget) {
        // Update the budget limit text view
        SetBudgetAmount.setText(String.valueOf(budgetLimit));

        // Update the remaining budget text view
        RemainingAmountTxt.setText(String.valueOf(remainingBudget));
        // Calculate the percentage of the budget used
        double percentUsed = (budgetLimit - remainingBudget) / budgetLimit * 100;

// Update the progressbar progress with the remaining budget percentage
        ProgressBar budgetProgressBar = myview.findViewById(R.id.budgetProgressBar);

        // Change the progress bar color if the remaining budget is less than or equal to 30% of the budget limit
        budgetProgressBar.setProgress((int) percentUsed);

        // Change the progress bar color if the remaining budget is less than or equal to 70% of the budget limit
        if (this.getContext() != null) {
            int color;
            if (remainingBudget <= budgetLimit * 0.3) {
                color = ContextCompat.getColor(getContext(), R.color.expense_color);
            } else {
                color = ContextCompat.getColor(getContext(), R.color.SkyBlueDark);
            }
            budgetProgressBar.setProgressTintList(ColorStateList.valueOf(color));
        }
    }

    private void sendNotification(String title, String message) {
        if (this.getContext() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("default",
                        "Channel name",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Channel description");
                NotificationManager notificationManager = (NotificationManager) getSystemService(getContext(), NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
            // Create a Notification message
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "default")
                    .setSmallIcon(R.drawable.salaryicon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            // Show the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
            if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            notificationManager.notify(1, builder.build());
        }
    }

}