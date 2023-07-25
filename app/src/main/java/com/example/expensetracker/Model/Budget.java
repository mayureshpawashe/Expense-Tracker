package com.example.expensetracker.Model;

public class Budget {
        private double budgetLimit;
        private double remainingBudget;
        private int currentMonth;
        private int currentYear;

        public Budget() {
            // Default constructor required for calls to DataSnapshot.getValue(Budget.class)
        }

        public Budget(double budgetLimit, double remainingBudget, int currentMonth, int currentYear) {
            this.budgetLimit = budgetLimit;
            this.remainingBudget = remainingBudget;
            this.currentMonth = currentMonth;
            this.currentYear = currentYear;
        }

        public double getBudgetLimit() {
            return budgetLimit;
        }

        public double getRemainingBudget() {
            return remainingBudget;
        }

        public int getCurrentMonth() {
            return currentMonth;
        }

        public int getCurrentYear() {
            return currentYear;
        }

        public void setBudgetLimit(double budgetLimit) {
            this.budgetLimit = budgetLimit;
        }

        public void setRemainingBudget(double remainingBudget) {
            this.remainingBudget = remainingBudget;
        }

        public void setCurrentMonth(int currentMonth) {
            this.currentMonth = currentMonth;
        }

        public void setCurrentYear(int currentYear) {
            this.currentYear = currentYear;
        }
    }

