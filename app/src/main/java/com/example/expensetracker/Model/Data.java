package com.example.expensetracker.Model;

public class Data {

    private int ammount;

    private String Dates;
    private String category;
    private String note;
    private String id;
    private int Day;
    private int Month;
    private int Year;




    public Data(int ammount, String Dates, String category, String note, String id, int day, int month, int year) {
        this.ammount = ammount;
        this.Dates = Dates;
        this.category = category;
        this.note = note;
        this.id = id;

        this.Day = day;
        this.Month = month;
        this.Year = year;
    }

    public String getAmmount() {
        return String.valueOf(ammount);
    }

    public String getTimestamp() {
        return Dates;
    }


    public void setTimestamp(String Dates) {
        this.Dates = Dates;
    }

    public void setAmmount(String ammount) {
        this.ammount = Integer.parseInt(ammount);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public int getDay() {
        return Day;
    }

    public void setDay(int day) {
        Day = day;
    }

    public int getMonth() {
        return Month;
    }

    public void setMonth(int month) {
        Month = month;
    }

    public int getYear() {
        return Year;
    }

    public void setYear(int year) {
        Year = year;
    }

    public Data(){

    }
}
