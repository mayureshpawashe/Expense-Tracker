package com.example.expensetracker;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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


/**
 * A simple {@link Fragment} subclass.

 */
public class DashBoardFragment extends Fragment {

    //total income,expense ,balance
    private TextView incomeSum;
    private TextView expenseSum;
    private TextView balanceSum;
    int TotalIncome;
    int TotalExpense;


    int TotalBalance=0;

    //Floating Buttons
    private FloatingActionButton fab_main_btn;
    private FloatingActionButton fab_income_btn;
    private FloatingActionButton fab_expense_btn;

    //Floating Buttons TextView
    private TextView fab_income_txt;
    private TextView fab_expense_txt;

    //For Animation Condition of main floating button (plus button)

    private boolean isOpen = false;
    private Animation fadOpen, fadClose;

    //TextView for Date Choosing
    TextView Datechooser;
    //DatePickerDialog.OnDateSetListener setListener;

    //Spinner Drop down List
    private Spinner spinner;
    String[] Category = {"Awards","Coupons","Dividends","Rental","Refunds","Salary","Sale", "Other"};
     ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(Category));
    String[] CategoryEx = {"Food","Shopping","Fuel","Travel","Housing","Bills","Gift","Education","Tax","Electronics",
            "Vehicle","Office","Insurance","Health","Children","Entertainment","Other"};

    //Recycler View for income and expense in dashboard
    private RecyclerView mRecyclerIncome;
    private RecyclerView mRecyclerExpense;


    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDataBase;
    private DatabaseReference mExpenseDataBase;

    public String dates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for dashboard fragment
        View myview = inflater.inflate(R.layout.fragment_dash_board, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        assert mUser != null;
        String uid = mUser.getUid();

        mIncomeDataBase = FirebaseDatabase.getInstance().getReference().child("Income").child(uid);
        mExpenseDataBase = FirebaseDatabase.getInstance().getReference().child("Expense").child(uid);

        //Total income and expense results for dashboard
        incomeSum=myview.findViewById(R.id.income_set_id);
        expenseSum=myview.findViewById(R.id.expense_set_id);
        balanceSum=myview.findViewById(R.id.balance_set_id);

        //Connect Floating Buttons
        fab_main_btn = myview.findViewById(R.id.fb_main_plus_btn);
        fab_income_btn = myview.findViewById(R.id.income_ft_btn);
        fab_expense_btn = myview.findViewById(R.id.expense_ft_btn);

        //Connect Floating Text
        fab_income_txt = myview.findViewById(R.id.income_ft_text);
        fab_expense_txt = myview.findViewById(R.id.expense_ft_text);

        //Connect Animation for floating buttons
        fadOpen = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_open);
        fadClose = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_close);

        //Recycler view for income and expense in dashboard
        mRecyclerIncome=myview.findViewById(R.id.recycler_income_dash);
        mRecyclerExpense=myview.findViewById(R.id.recycler_expense_dash);



        //Floating plus Button working animation
        fab_main_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addData();

                ftMain_btn_animation();
            }

        });
        //Total Income Generating
        mIncomeDataBase.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                TotalIncome= 0;
                for (DataSnapshot mysnapshot : datasnapshot.getChildren()) {
                    Data data = mysnapshot.getValue(Data.class);
                    if (data != null) {
                        TotalIncome += Integer.parseInt(data.getAmmount());
                    }
                    String strTotalValue = String.valueOf(TotalIncome);
                        incomeSum.setText(strTotalValue);
                    }

                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //Total Expense Generating
        mExpenseDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                  TotalExpense = 0;

                for (DataSnapshot mysnapshot : datasnapshot.getChildren()) {
                    Data data = mysnapshot.getValue(Data.class);
                    if (data != null) {
                        TotalExpense += Integer.parseInt(data.getAmmount());
                    }
                    String strTotalValue = String.valueOf(TotalExpense);
                        expenseSum.setText(strTotalValue);

                    TotalBalance = TotalIncome - TotalExpense;
                    String strTotalBalance = String.valueOf(TotalBalance);
                    balanceSum.setText(strTotalBalance);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //Total Balance Generating



        //
        LinearLayoutManager layoutManagerIncome=new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        layoutManagerIncome.setStackFromEnd(true);
        layoutManagerIncome.setReverseLayout(true);
        mRecyclerIncome.setHasFixedSize(true);
        mRecyclerIncome.setLayoutManager(layoutManagerIncome);

        LinearLayoutManager layoutManagerExpense=new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
        layoutManagerExpense.setReverseLayout(true);
        layoutManagerExpense.setStackFromEnd(true);
        mRecyclerExpense.setHasFixedSize(true);
        mRecyclerExpense.setLayoutManager(layoutManagerExpense);



        return myview;
    }
    //Floating Action Button (plus Button) Animation Method
    private void ftMain_btn_animation(){

        if (isOpen) {
            fab_income_btn.startAnimation(fadClose);
            fab_expense_btn.startAnimation(fadClose);
            fab_income_btn.setClickable(false);
            fab_expense_btn.setClickable(false);

            fab_income_txt.startAnimation(fadClose);
            fab_expense_txt.startAnimation(fadClose);
            fab_expense_txt.setClickable(false);
            fab_income_txt.setClickable(false);
            isOpen = false;

        } else {
            fab_income_btn.startAnimation(fadOpen);
            fab_expense_btn.startAnimation(fadOpen);
            fab_income_btn.setClickable(true);
            fab_expense_btn.setClickable(true);

            fab_income_txt.startAnimation(fadOpen);
            fab_expense_txt.startAnimation(fadOpen);
            fab_expense_txt.setClickable(true);
            fab_income_txt.setClickable(true);
            isOpen = true;
        }
    }
    private void addData() {
        fab_income_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                incomeDataInsert();
            }
        });
        fab_expense_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                expenseDataInsert();
            }
        });
    }

    public void incomeDataInsert() {

        AlertDialog.Builder mydialog = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View myviewm = inflater.inflate(R.layout.custom_layout_for_insert_data, null);
        mydialog.setView(myviewm);
        AlertDialog dialog = mydialog.create();
        dialog.setCancelable(false);

        EditText edtAmmount = myviewm.findViewById(R.id.ammount_edt);
        EditText edtNote = myviewm.findViewById(R.id.Note_edt);


        //Calender
        Datechooser = myviewm.findViewById(R.id.date_edt);
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
                        dates = day + "/" + month + "/" + year;
                        Datechooser.setText(dates);
                        d[0]=day;
                        m[0]=month;
                        y[0]=year;

                    }
                }, year, month, day);
                datePickerDialog.show();

            }
        });


        //spinner dropDown view
        final String[] value = new String[1];
        spinner = myviewm.findViewById(R.id.category_edt);
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

        Button btnSave = myviewm.findViewById(R.id.btnSave);
        Button btnCancel = myviewm.findViewById(R.id.btnCancel);

        //SAVE Button Functionality
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ammount = edtAmmount.getText().toString().trim();
                String Category = value[0].trim();
                String note = edtNote.getText().toString().trim();
                String Dates = Datechooser.getText().toString();
                int Day = d[0];
                int Month=m[0];
                int Year=y[0];

               // @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                //validating empty Entries
                if (TextUtils.isEmpty(ammount)) {
                    edtAmmount.setError("Required field");
                    return;
                }
                if (TextUtils.isEmpty(Dates)) {
                    Datechooser.setError("Required field");
                    return;
                }

                int OurAmmount = Integer.parseInt(ammount);

                String id = mIncomeDataBase.push().getKey();

                Data data = new Data(OurAmmount, Dates, Category, note, id, Day, Month, Year);
                assert id != null;
                mIncomeDataBase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Data Added Successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });


        //Cancel Button Functionality
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ftMain_btn_animation();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void expenseDataInsert(){

        AlertDialog.Builder mydialog=new AlertDialog.Builder(getActivity());
        LayoutInflater inflater=LayoutInflater.from(getActivity());

        View myviewm=inflater.inflate(R.layout.custom_layout_for_insert_data,null);
        mydialog.setView(myviewm);
        AlertDialog dialog=mydialog.create();
        dialog.setCancelable(false);

        EditText edtAmmount=myviewm.findViewById(R.id.ammount_edt);
        EditText edtNote=myviewm.findViewById(R.id.Note_edt);


        //Calender
        Datechooser=myviewm.findViewById(R.id.date_edt);
        Calendar calendar =Calendar.getInstance();
         int year= calendar.get(Calendar.YEAR);
         int month= calendar.get(Calendar.MONTH);
        int day= calendar.get(Calendar.DAY_OF_MONTH);
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
                        month=month+1;
                        dates=day+"/"+month+"/"+year;
                        Datechooser.setText(dates);
                        d[0] =day;
                        m[0] =month;
                        y[0] =year;
                    }
                },year,month,day);
                datePickerDialog.show();

            }
        });


        //spinner dropDown view for Expense Category
        final String[] value = new String[1];
        spinner= myviewm.findViewById(R.id.category_edt);
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, CategoryEx);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                value[0] =parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btnSave=myviewm.findViewById(R.id.btnSave);
        Button btnCancel=myviewm.findViewById(R.id.btnCancel);

        //SAVE Button Functionality
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ammount = edtAmmount.getText().toString().trim();
                String Category = value[0].trim();
                String note = edtNote.getText().toString().trim();
                String Dates=Datechooser.getText().toString();
                int Day = d[0];
                int Month=m[0];
                int Year=y[0];



                //validating empty Entries
                if (TextUtils.isEmpty(ammount)) {
                    edtAmmount.setError("Required field");
                    return;
                }
                else if(TextUtils.isEmpty(Dates)) {
                    Datechooser.setError("Required field");
                    return;
                }

                int OurAmmount = Integer.parseInt(ammount);

                String id = mExpenseDataBase.push().getKey();

                Data data = new Data(OurAmmount, Dates, Category, note, id, Day, Month, Year);
                assert id != null;
                mExpenseDataBase.child(id).setValue(data);

                Toast.makeText(getActivity(), "Data Added Successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                //Updating Remaining Budget of Budget section
                UpdateRemainingBudget(Calendar.getInstance().get(Calendar.MONTH) + 1,Calendar.getInstance().get(Calendar.YEAR));
            }
        });
        //Cancel Button Functionality
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ftMain_btn_animation();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public void onStart(){
        super.onStart();

        //Creating Custom Adapter for Income in Dashboard
        FirebaseRecyclerOptions<Data> options =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mIncomeDataBase, Data.class)
                        .build();

        FirebaseRecyclerAdapter<Data, DashBoardFragment.IncomeViewHolder> IncomeAdapter = new FirebaseRecyclerAdapter<Data, DashBoardFragment.IncomeViewHolder>(options) {
            @NonNull
            @Override
            public  DashBoardFragment.IncomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.dashboard_income, parent, false);

                return new  DashBoardFragment.IncomeViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull IncomeViewHolder holder, int position, @NonNull Data model) {

                // Bind the income_details object to the IncomeViewHolder
                holder.setIncomeCategory(model.getCategory());
                holder.setIncomeAmmount(Integer.parseInt(model.getAmmount()));
                holder.setIncomeDate(model.getTimestamp());
            }

        };
        IncomeAdapter.startListening();
        mRecyclerIncome.setAdapter(IncomeAdapter);
    //=========================================================================================================================================
        //Creating Custom Adapter for Expense in Dashboard
        FirebaseRecyclerOptions<Data> options2 =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(mExpenseDataBase, Data.class)
                        .build();

        FirebaseRecyclerAdapter<Data, DashBoardFragment.ExpenseViewHolder> ExpenseAdapter = new FirebaseRecyclerAdapter<Data, DashBoardFragment.ExpenseViewHolder>(options2) {
            @NonNull
            @Override
            public  DashBoardFragment.ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.dashboard_expense, parent, false);

                return new  DashBoardFragment.ExpenseViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position, @NonNull Data model) {
                // Bind the income_details object to the IncomeViewHolder
                holder.setExpenseCategory(model.getCategory());
                holder.setExpenseAmmount(Integer.parseInt(model.getAmmount()));
                holder.setExpenseDate(model.getTimestamp());
            }
        };
        ExpenseAdapter.startListening();
        mRecyclerExpense.setAdapter(ExpenseAdapter);
    }


    //For Income ViewHolder
    public static class IncomeViewHolder extends RecyclerView.ViewHolder {
        View mIncomeView;
        public IncomeViewHolder(View itemView) {
            super(itemView);
            mIncomeView=itemView;
        }
        public void setIncomeCategory(String category){
            TextView mCategory=mIncomeView.findViewById(R.id.Category_income_dash);
            mCategory.setText(category);
        }
        public void setIncomeAmmount(int ammount){
            TextView mAmmount=mIncomeView.findViewById(R.id.Ammount_income_dash);
            String strAmmount=String.valueOf(ammount);
            mAmmount.setText(strAmmount);
        }
        public void setIncomeDate(String dates){
            TextView mDate=mIncomeView.findViewById(R.id.Date_income_dash);
            mDate.setText(dates);
        }

    }
    //For Expense ViewHolder
    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        View mExpenseView;
        public ExpenseViewHolder(View itemView) {
            super(itemView);
            mExpenseView=itemView;
        }
        public void setExpenseCategory(String category){
            TextView mCategory=mExpenseView.findViewById(R.id.Category_expense_dash);
            mCategory.setText(category);
        }
        public void setExpenseAmmount(int ammount){
            TextView mAmmount=mExpenseView.findViewById(R.id.Ammount_expense_dash);
            String strAmmount=String.valueOf(ammount);
            mAmmount.setText(strAmmount);
        }
        public void setExpenseDate(String dates){
            TextView mDate=mExpenseView.findViewById(R.id.Date_expense_dash);
            mDate.setText(dates);
        }

    }

    public void UpdateRemainingBudget(int month,int year) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        assert mUser != null;
        String uid = mUser.getUid();

        mExpenseDataBase.addValueEventListener(new ValueEventListener() {
            double totalExpense = 0.0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                    Data expense = expenseSnapshot.getValue(Data.class);
                    assert expense != null;
                    if (expense.getMonth() == month && expense.getYear() == year) {
                        totalExpense += Double.parseDouble(expense.getAmmount());
                    }
                    DatabaseReference budgetRef = FirebaseDatabase.getInstance().getReference().child("Budget").child(uid);
                    DatabaseReference currentMonthBudgetRef = budgetRef.child(String.valueOf(year)).child(String.valueOf(month));
                    DatabaseReference currentMonthBudgetRef2 = budgetRef.child(String.valueOf(year)).child(String.valueOf(month)).child("remainingBudget");

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

                            if (remainingBudget <= budget * 0.3 ) {
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
    private void sendNotification(String title, String message) {
        if (getContext() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("default",
                        "Channel name",
                        NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Channel description");
                NotificationManager notificationManager = (NotificationManager) getSystemService(getContext(), NotificationManager.class);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
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