package com.example.expensetracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
public class IncomeFragment extends Fragment {

    //Firebase Database
    private FirebaseAuth mAuth;
    private DatabaseReference mIncomeDatabase;

    //RecyclerView
    private RecyclerView recyclerView;
    //TextView For Total income
    private TextView incomeTotalSum;

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
       View myview = inflater.inflate(R.layout.fragment_income, container, false);


       mAuth=FirebaseAuth.getInstance();

       FirebaseUser mUser =mAuth.getCurrentUser();
        assert mUser != null;
        String uid=mUser.getUid();
       mIncomeDatabase = FirebaseDatabase.getInstance().getReference().child("Income").child(uid);
        incomeTotalSum=myview.findViewById(R.id.income_txt_result);

       //set RecyclerView
        recyclerView=myview.findViewById(R.id.recycler_id_income);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        //Total Income Generating
        mIncomeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                try{
                int TotalValue=0;
                for (DataSnapshot mysnapshot:datasnapshot.getChildren()){
                    Data data=mysnapshot.getValue(Data.class);
                    if (data != null) {
                        TotalValue+=Integer.parseInt(data.getAmmount());
                        String strTotalValue=String.valueOf(TotalValue);
                        incomeTotalSum.setText(strTotalValue);
                    }

                }

                }
                catch(Exception ignored){
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
                        .setQuery(mIncomeDatabase, Data.class)
                        .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data,MyViewHolder>(options) {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.income_recycler_data, parent, false);

                return new MyViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Data model) {
                // Bind the income_details object to the MyViewHolder
                holder.setCategory(model.getCategory());
                holder.setAmmount(Integer.parseInt(model.getAmmount()));
                holder.setDate(model.getTimestamp());
                holder.setNote(model.getNote());

                //Functionality to select Item from Income RecyclerView for Update or Delete
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
    private static final HashMap<String, Integer> CATEGORY_IMAGE_MAP = new HashMap<>();
    // categories and corresponding image resource IDs
    static {
        CATEGORY_IMAGE_MAP.put("Awards", R.drawable.awards);
        CATEGORY_IMAGE_MAP.put("Coupons", R.drawable.coupons);
        CATEGORY_IMAGE_MAP.put("Dividends", R.drawable.dividend);
        CATEGORY_IMAGE_MAP.put("Rental", R.drawable.rental);
        CATEGORY_IMAGE_MAP.put("Refunds", R.drawable.refund);
        CATEGORY_IMAGE_MAP.put("Salary", R.drawable.salary2);
        CATEGORY_IMAGE_MAP.put("Sale", R.drawable.sale);
        CATEGORY_IMAGE_MAP.put("Other",R.drawable.other2);
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        View myview;
        public MyViewHolder(View itemView){
            super(itemView);
            myview=itemView;
        }

      public void setCategory(String category){
                TextView mCategory = myview.findViewById(R.id.category_txt_income);
                mCategory.setText(category);
          //Set the image resource based on the category
          if (CATEGORY_IMAGE_MAP.containsKey(category)) {
              ImageView categoryImage = myview.findViewById(R.id.category_image_income);
              categoryImage.setImageResource(CATEGORY_IMAGE_MAP.get(category));
          }

        }
        private void setDate(String date){
                TextView mDate = myview.findViewById(R.id.date_txt_income);
                mDate.setText(date);
        }
        private void setAmmount(int ammount){
                TextView mAmmount = myview.findViewById(R.id.ammount_txt_income);
                String strAmmount = String.valueOf(ammount);
                mAmmount.setText(strAmmount);
        }
        private void setNote(String note){
                TextView mNote = myview.findViewById(R.id.notes_txt_income);
                mNote.setText(note);
        }


    }


    //Method to update Income data
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
                        d[0]=day;
                        m[0]=month;
                        y[0]=year;
                    }
                }, year, month, day);
                datePickerDialog.show();

            }
        });

        //spinner dropDown view(Income category)
        String[] Category = {"Awards","Coupons","Dividends","Rental","Refunds","Salary","Sale", "Other"};
        ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(Category));
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

       // edtCategory.setAdapter(Category);
       // edtCategory.setSelection(Integer.parseInt(String.valueOf(Category.length())));

        edtNote.setText(Note);
        edtNote.setSelection(Note.length());

        AlertDialog dialog=mydialog.create();
        //Functionality for Update Button
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               String Ammount=edtAmmount.getText().toString().trim();
               String Note=edtNote.getText().toString().trim();
                String Cat=value[0];
               String Date=Datechooser.getText().toString().trim();

                String[] dateParts = Date.split("/");
                int Day = Integer.parseInt(dateParts[0]);
                int Month= Integer.parseInt(dateParts[1]);
                int Year= Integer.parseInt(dateParts[2]);

               int mAmmount=Integer.parseInt(Ammount);
              //  Toast.makeText(getActivity(), "Day: " + Day + ", Month: " + Month + ", Date: " + Date, Toast.LENGTH_LONG).show();
                Data data=new Data(mAmmount,Date,Cat,Note,post_key, Day, Month, Year);
                mIncomeDatabase.child(post_key).setValue(data);
                dialog.dismiss();



            }
        });
        //Functionality for Delete Button
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIncomeDatabase.child(post_key).removeValue();
                dialog.dismiss();
            }
        });
        dialog.show();

    }
}
