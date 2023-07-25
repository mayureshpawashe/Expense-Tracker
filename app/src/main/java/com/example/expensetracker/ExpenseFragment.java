package com.example.expensetracker;

import static android.content.ContentValues.TAG;
import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetracker.Model.Budget;
import com.example.expensetracker.Model.Data;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExpenseFragment extends Fragment {

    //Firebase database
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDatabase;
    private RecyclerView recyclerView;
    //TextView For Total income
    private TextView expenseTotalSum;
    //update data variables
    private EditText edtAmmount;
    public EditText edtDate;
    private Spinner edtCategory;
    private EditText edtNote;

    //Button for Update and Delete
    private Button btnUpdate;
    private Button btnDelete;

    //to Retrieve Data item value from database for update or delete Income data
    private String ammount;
    private String Date;
    private String Category;
    private String Note;
    private String post_key;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myview = inflater.inflate(R.layout.fragment_expense, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        assert mUser != null;
        String uid = mUser.getUid();
        mExpenseDatabase = FirebaseDatabase.getInstance().getReference().child("Expense").child(uid);
        expenseTotalSum=myview.findViewById(R.id.expense_txt_result);

        //set RecyclerView
        recyclerView = myview.findViewById(R.id.recycler_id_expense);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        //Total Expense Generating
        mExpenseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                try {
                    int TotalValue = 0;
                    for (DataSnapshot mysnapshot : datasnapshot.getChildren()) {
                        Data data = mysnapshot.getValue(Data.class);
                        if (data != null) {
                            TotalValue += Integer.parseInt(data.getAmmount());
                        }

                        String strTotalValue = String.valueOf(TotalValue);
                        expenseTotalSum.setText(strTotalValue);
                    }

                } catch (Exception ignored) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return myview;
    }
        @Override
        public void onStart() {
            super.onStart();
            FirebaseRecyclerOptions<Data> options =
                    new FirebaseRecyclerOptions.Builder<Data>()
                            .setQuery(mExpenseDatabase, Data.class)
                            .build();

            FirebaseRecyclerAdapter<Data, ExpenseFragment.MyViewHolder2> adapter = new FirebaseRecyclerAdapter<Data, ExpenseFragment.MyViewHolder2>(options) {
                @Override
                public ExpenseFragment.MyViewHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.expense_recycler_data, parent, false);

                    return new ExpenseFragment.MyViewHolder2(view);
                }


                @Override
                protected void onBindViewHolder(@NonNull ExpenseFragment.MyViewHolder2 holder, int position, @NonNull Data model) {
                    // Bind the income_details object to the MyViewHolder
                    holder.setCategory(model.getCategory());
                    holder.setAmmount(Integer.parseInt(model.getAmmount()));
                    holder.setDate(model.getTimestamp());
                    holder.setNote(model.getNote());

                    //Functionality to select Item from Expense RecyclerView for Update or Delete
                    holder.myview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            post_key=getRef(holder.getAbsoluteAdapterPosition()).getKey();


                            ammount=model.getAmmount();
                            Date=model.getTimestamp();
                            Category=model.getCategory();
                            Note=model.getNote();

                            updateDataItem();
                        }
                    });
                }
            };

            adapter.startListening();
            recyclerView.setAdapter(adapter);
        }


        public static class MyViewHolder2 extends RecyclerView.ViewHolder {
            View myview;
            private static final HashMap<String, Integer> CATEGORY_IMAGE_MAP = new HashMap<>();
            // categories and corresponding image resource IDs
            static {
                CATEGORY_IMAGE_MAP.put("Food", R.drawable.food);
                CATEGORY_IMAGE_MAP.put("Shopping", R.drawable.shopping);
                CATEGORY_IMAGE_MAP.put("Fuel", R.drawable.fuel2);
                CATEGORY_IMAGE_MAP.put("Travel", R.drawable.travel);
                CATEGORY_IMAGE_MAP.put("Housing", R.drawable.housing);
                CATEGORY_IMAGE_MAP.put("Bills", R.drawable.bills);
                CATEGORY_IMAGE_MAP.put("Gift", R.drawable.gift);
                CATEGORY_IMAGE_MAP.put("Education",R.drawable.education);
                CATEGORY_IMAGE_MAP.put("Tax", R.drawable.tax);
                CATEGORY_IMAGE_MAP.put("Electronics", R.drawable.electronics);
                CATEGORY_IMAGE_MAP.put("Vehicle", R.drawable.vehicle);
                CATEGORY_IMAGE_MAP.put("Office", R.drawable.office);
                CATEGORY_IMAGE_MAP.put("Insurance", R.drawable.insurance);
                CATEGORY_IMAGE_MAP.put("Health", R.drawable.health);
                CATEGORY_IMAGE_MAP.put("Children", R.drawable.children);
                CATEGORY_IMAGE_MAP.put("Entertainment", R.drawable.entertainment);
                CATEGORY_IMAGE_MAP.put("Others", R.drawable.other2);


            }

            public MyViewHolder2(View itemView){
                super(itemView);
                myview=itemView;
            }

            public void setCategory(String category){
                    TextView mCategory = myview.findViewById(R.id.category_txt_expense);
                    mCategory.setText(category);
                //Set the image resource based on the category
                if (CATEGORY_IMAGE_MAP.containsKey(category)) {
                    ImageView categoryImage = myview.findViewById(R.id.category_image_expense);
                    categoryImage.setImageResource(CATEGORY_IMAGE_MAP.get(category));
                }
            }
            private void setDate(String date){
                    TextView mDate = myview.findViewById(R.id.date_txt_expense);
                    mDate.setText(date);

            }
            private void setAmmount(int ammount){
                    TextView mAmmount = myview.findViewById(R.id.ammount_txt_expense);
                    String strAmmount = String.valueOf(ammount);
                    mAmmount.setText(strAmmount);
            }
            private void setNote(String note){
                    TextView mNote = myview.findViewById(R.id.notes_txt_expense);
                    mNote.setText(note);
            }
    }

    @SuppressLint("CutPasteId")
    private void updateDataItem(){
        AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater =LayoutInflater.from(getActivity());
        View myView=inflater.inflate(R.layout.update_data_item,null);
        mydialog.setView(myView);

        edtAmmount=myView.findViewById(R.id.ammount_edt);
        edtDate=myView.findViewById(R.id.date_edt);
        edtCategory=myView.findViewById(R.id.category_edt);
        edtNote=myView.findViewById(R.id.Note_edt);

        btnUpdate=myView.findViewById(R.id.btnUpdate);
        btnDelete=myView.findViewById(R.id.btnDelete);


        //Calender
        EditText Datechooser;
        final String[] date = new String[1];
        Datechooser = myView.findViewById(R.id.date_edt);
        Calendar calendar = Calendar.getInstance();
         int year = calendar.get(Calendar.YEAR);
         int month = calendar.get(Calendar.MONTH);
         int day = calendar.get(Calendar.DAY_OF_MONTH);
        int[] d = new int[1];
        int[] m = new int[1];
        int[] y = new int[1];

        Datechooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month + 1;
                        date[0] = day + "/" + month + "/" + year;
                        Datechooser.setText(date[0]);
                        d[0] = day;
                        m[0] = month;
                        y[0] = year;

                    }
                }, year, month, day);
                datePickerDialog.show();

            }
        });

        //spinner dropDown view(Expense category)
        String[] CategoryEx = {"Food","Shopping","Fuel","Travel","Housing","Bills","Gift","Education","Tax","Electronics",
                "Vehicle","Office","Insurance","Health","Children","Entertainment","Other"};

        ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(CategoryEx));
        Spinner spinner;
        final String[] value = new String[1];
        spinner = myView.findViewById(R.id.category_edt);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item,arrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                value[0] = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Retrieve data from database to update or delete
        edtAmmount.setText(ammount);
        edtAmmount.setSelection(ammount.length());

        edtDate.setText(Date);
        edtDate.setSelection((Date.length()));

        edtNote.setText(Note);
        edtNote.setSelection(Note.length());
        AlertDialog dialog=mydialog.create();

        //Functionality for Update Button
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Ammount=edtAmmount.getText().toString().trim();
                String Note=edtNote.getText().toString().trim();
                String Date=Datechooser.getText().toString().trim();
                String Cat=value[0];
                String[] dateParts = Date.split("/");
                int Day = Integer.parseInt(dateParts[0]);
                int Month= Integer.parseInt(dateParts[1]);
                int Year= Integer.parseInt(dateParts[2]);
                int mAmmount=Integer.parseInt(Ammount);

                Data data=new Data(mAmmount,Date,Cat,Note,post_key, Day, Month, Year);
                mExpenseDatabase.child(post_key).setValue(data);
                dialog.dismiss();
                //Updating Remaining Budget of Budget section
                UpdateRemainingBudget(Calendar.getInstance().get(Calendar.MONTH) + 1,Calendar.getInstance().get(Calendar.YEAR));

            }
        });
        //Functionality for Delete Button
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpenseDatabase.child(post_key).removeValue();
                dialog.dismiss();
                //Updating Remaining Budget of Budget section
                UpdateRemainingBudget(Calendar.getInstance().get(Calendar.MONTH) + 1,Calendar.getInstance().get(Calendar.YEAR));
            }
        });
        dialog.show();


    }
    public void UpdateRemainingBudget(int month,int year) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        assert mUser != null;
        String uid = mUser.getUid();
        DatabaseReference mExpenseDataBase2;
        mExpenseDataBase2 = FirebaseDatabase.getInstance().getReference().child("Expense").child(uid);
        mExpenseDataBase2.addValueEventListener(new ValueEventListener() {
            double totalExpense = 0.0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                    Data expense = expenseSnapshot.getValue(Data.class);

                    assert expense != null;
                    //retrieving total expenses from current month
                    if (expense.getMonth() == month && expense.getYear() == year) {
                        totalExpense += Double.parseDouble(expense.getAmmount());
                    }

                    DatabaseReference budgetRef = FirebaseDatabase.getInstance().getReference().child("Budget").child(uid);
                    DatabaseReference currentMonthBudgetRef = budgetRef.child(String.valueOf(year)).child(String.valueOf(month));
                    DatabaseReference currentMonthBudgetRef2 = budgetRef.child(String.valueOf(year)).child(String.valueOf(month)).child("remainingBudget");

                    //updating remaining budget into budget collection
                    currentMonthBudgetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Budget currentMonthBudget = snapshot.getValue(Budget.class);
                            double budget;
                            if (currentMonthBudget != null) {
                                budget =  currentMonthBudget.getBudgetLimit();
                            }
                            else{
                                budget =  0.0;
                            }
                            double remainingBudget = budget - totalExpense;
                            currentMonthBudgetRef2.setValue(remainingBudget);

                            if (remainingBudget <= budget * 0.3 && budget != 0.0) {
                                // Sending notification to the user
                                String title = "Budget Alert";
                                String message = "You have used 70% of your budget for this month!";
                                sendNotification(title, message);
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error fetching budget: " + error.getMessage());
                        }
                    });
                }
                Log.d(TAG, "getTotalExpense() returning totalExpense = " + totalExpense);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    //Notification Function
    private void sendNotification(String title, String message) {
        if (this.getContext() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("default",
                        "Channel name",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Channel description");
                NotificationManager notificationManager = getSystemService(getContext(), NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
            // Create a Notification message
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this.getContext(), "default")
                    .setSmallIcon(R.drawable.salaryicon)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            // Show the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.getContext());
            if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            notificationManager.notify(1, builder.build());
        }

    }
}
