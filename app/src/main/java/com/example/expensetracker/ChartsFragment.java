package com.example.expensetracker;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.expensetracker.Model.Data;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass
 */
public class ChartsFragment extends Fragment {
    private TextView Datechooser1;
    private TextView Datechooser2;
    private FirebaseAuth mAuth;
    private DatabaseReference mExpenseDataBase;
    private DatabaseReference mExpenseDataBaseC;
    private String date1;
    private String date2;
    private Button GenerateBtn;
    private TableRow tableRow;
    private TextView categoryTextView;
    private TextView amountTextView;
    private TableLayout tableLayout;
    private PieChart pieChart;
    private int sd,sm,sy,ed,em,ey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for charts fragment
        View myview = inflater.inflate(R.layout.fragment_charts, container, false);
        tableLayout = myview.findViewById(R.id.table_layout);
        // Create Pie Chart
         pieChart = (PieChart) myview.findViewById(R.id.pie_chart);

        //Start Date
        Datechooser1 = myview.findViewById(R.id.DateStart);
        Calendar calendar = Calendar.getInstance();
         int year = calendar.get(Calendar.YEAR);
         int month = calendar.get(Calendar.MONTH);
         int day = calendar.get(Calendar.DAY_OF_MONTH);
        Datechooser1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month + 1;
                        date1 = day + "/" + month + "/" + year;
                        Datechooser1.setText(date1);
                        sd=day;
                        sm=month;
                        sy=year;
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });
        //End Date2
        Datechooser2 = myview.findViewById(R.id.DateEnd);
        Calendar calendar2 = Calendar.getInstance();
         int year2 = calendar2.get(Calendar.YEAR);
        int month2 = calendar2.get(Calendar.MONTH);
        int day2 = calendar2.get(Calendar.DAY_OF_MONTH);
        Datechooser2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year2, int month2, int day2) {
                        month2 = month2 + 1;
                        date2 = day2 + "/" + month2 + "/" + year2;
                        Datechooser2.setText(date2);
                        ed=day2;
                        em=month2;
                        ey=year2;
                    }
                }, year2, month2, day2);
                datePickerDialog.show();

            }
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        assert mUser != null;
        String uid = mUser.getUid();
        mExpenseDataBase = FirebaseDatabase.getInstance().getReference().child("Expense").child(uid);

        GenerateBtn = myview.findViewById(R.id.ChartBtn);


        GenerateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int startDay =sd;
                int startMonth =sm;
                int startYear =sy;
                int endDay =ed;
                int endMonth =em;
                int endYear =ey;

// Filter data based on date range
                mExpenseDataBase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Integer> categoryAmounts = new HashMap<>();
                        for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                            Data expense = expenseSnapshot.getValue(Data.class);
                            assert expense != null;
                            int day = Integer.parseInt(String.valueOf(expense.getDay()));
                            int month = Integer.parseInt(String.valueOf(expense.getMonth()));
                            int year = Integer.parseInt(String.valueOf(expense.getYear()));
                            if (year >= startYear && year <= endYear &&
                                    month >= startMonth && month <= endMonth &&
                                    day >= startDay && day <= endDay) {
                                if (!categoryAmounts.containsKey(expense.getCategory())) {
                                    categoryAmounts.put(expense.getCategory(), Integer.valueOf(expense.getAmmount()));
                                } else {
                                    Integer currentAmount = categoryAmounts.get(expense.getCategory());
                                    categoryAmounts.put(expense.getCategory(), Integer.valueOf(currentAmount +Integer.parseInt(expense.getAmmount()) ));
                                }
                            }
                        }
                         tableLayout = myview.findViewById(R.id.table_layout);


                        // Create Pie Chart
                        PieChart pieChart = (PieChart) myview.findViewById(R.id.pie_chart);
                        List<PieEntry> entries = new ArrayList<>();
                        List<Integer> colors = new ArrayList<>(); // create a list of colors
                        Random random = new Random(); // create a random number generator
                        for (Map.Entry<String,Integer> entry : categoryAmounts.entrySet()) {
                            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));

                            // generate a random color and add it to the list
                            colors.add(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                        }
                        PieDataSet dataSet = new PieDataSet(entries, "Amount spent on different categories");
                        dataSet.setColors(colors); // set the colors for the data set
                        PieData data = new PieData(dataSet);
                        pieChart.setData(data);
                        pieChart.getDescription().setEnabled(false);
                        data.setValueTextSize(17);
                        data.setHighlightEnabled(true);
                        pieChart.invalidate();

                        //Refresh Table
                        tableLayout.removeAllViews();

                        // Populate the table with data
                        List<Map.Entry<String, Integer>> entries2 = new ArrayList<>(categoryAmounts.entrySet());
                        Collections.sort(entries2, new Comparator<Map.Entry<String, Integer>>() {
                            @Override
                            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                                return entry2.getValue().compareTo(entry1.getValue());
                            }
                        });


                        for (Map.Entry<String, Integer> entry : entries2) {
                            if (getContext() != null) {
                                tableRow = new TableRow(getContext());
                                tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                                categoryTextView = new TextView(getContext());
                                categoryTextView.setText(entry.getKey());
                                //Log.d("ChartsFragment", "Category: " + entry.getKey());
                                categoryTextView.setPadding(8, 8, 8, 8);
                                tableRow.addView(categoryTextView);

                                amountTextView = new TextView(getContext());
                                amountTextView.setText(entry.getValue().toString());
                               // Log.d("ChartsFragment", "Category: " + entry.getValue());
                                amountTextView.setPadding(8, 8, 8, 8);
                                tableRow.addView(amountTextView);
                                tableLayout.addView(tableRow);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to filter data within given range", error.toException());
                    }
                });
            }
        });
        return myview;
    }

    @Override
    public void onStart() {
        super.onStart();
        CurrentMonthChart();

    }
    private void CurrentMonthChart() {

        // Calculate the total amount spent for each category
        mExpenseDataBase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Integer> categoryAmounts = new HashMap<>();
                for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                    Data expense = expenseSnapshot.getValue(Data.class);
                    assert expense != null;
                    int month = Integer.parseInt(String.valueOf(expense.getMonth()));
                    int year = Integer.parseInt(String.valueOf(expense.getYear()));
                    Calendar calendar = Calendar.getInstance();
                    int currentMonth = calendar.get(Calendar.MONTH) + 1; // Months are zero-indexed in Calendar
                    int currentYear = calendar.get(Calendar.YEAR);
                    if (month == currentMonth && year == currentYear) {
                        if (!categoryAmounts.containsKey(expense.getCategory())) {
                            categoryAmounts.put(expense.getCategory(), Integer.valueOf(expense.getAmmount()));
                        } else {
                            Integer currentAmount = categoryAmounts.get(expense.getCategory());
                            categoryAmounts.put(expense.getCategory(), Integer.valueOf(currentAmount + Integer.parseInt(expense.getAmmount())));
                        }

                }

                List<PieEntry> entries = new ArrayList<>();
                List<Integer> colors = new ArrayList<>(); // create a list of colors
                Random random = new Random(); // create a random number generator
                for (Map.Entry<String,Integer> entry : categoryAmounts.entrySet()) {
                    entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));

                    // generate a random color and add it to the list
                    colors.add(Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                }
                    //Refresh Table
                    tableLayout.removeAllViews();

                PieDataSet dataSet = new PieDataSet(entries, "\nAmount spent");
                dataSet.setColors(colors); // set the colors for the data set
                PieData data = new PieData(dataSet);
                pieChart.setData(data);
                pieChart.getDescription().setEnabled(false);
                data.setValueTextSize(15);
                data.setHighlightEnabled(true);
                pieChart.invalidate();

                    // Populate the table with data
                    List<Map.Entry<String, Integer>> entries2 = new ArrayList<>(categoryAmounts.entrySet());
                    Collections.sort(entries2, new Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2) {
                            return entry2.getValue().compareTo(entry1.getValue());
                        }
                    });
                for (Map.Entry<String, Integer> entry : entries2) {
                    if (getContext() != null) {
                        tableRow = new TableRow(getContext());
                        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                        categoryTextView = new TextView(getContext());
                        categoryTextView.setText(entry.getKey());
                        categoryTextView.setPadding(8, 8, 8, 8);
                        tableRow.addView(categoryTextView);

                        amountTextView = new TextView(getContext());
                        amountTextView.setText(entry.getValue().toString());
                        amountTextView.setPadding(8, 8, 8, 8);
                        tableRow.addView(amountTextView);
                        tableLayout.addView(tableRow);
                    }
                }
            }
        }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load current months data", error.toException());
            }

            });
    }

}



