package com.hardware.shop.service;

import com.hardware.shop.dao.ExpenseDAO;
import com.hardware.shop.model.Expense;

import java.sql.SQLException;
import java.util.List;

public class ExpenseService {

    private final ExpenseDAO expenseDAO;

    public ExpenseService() {
        this.expenseDAO = new ExpenseDAO();
    }

    public boolean createExpense(Expense expense) throws SQLException {

        validateExpense(expense);

        return expenseDAO.createExpense(expense);
    }

    public List<Expense> findAll() throws SQLException {

        return expenseDAO.findAll();
    }

    public Expense findById(long id) throws SQLException {

        validateId(id);

        return expenseDAO.findById(id);
    }

    public boolean updateExpense(Expense expense) throws SQLException {

        if (expense == null) {

            throw new IllegalArgumentException("Expense cannot be null.");
        }

        validateId(expense.getId());

        validateExpense(expense);

        return expenseDAO.updateExpense(expense);
    }

    public boolean deleteExpense(long id) throws SQLException {

        validateId(id);

        return expenseDAO.deleteExpense(id);
    }

    private void validateExpense(Expense expense) {

        if (expense == null) {

            throw new IllegalArgumentException("Expense cannot be null.");
        }

        if (expense.getExpenseNumber() == null || expense.getExpenseNumber().isBlank()) {

            throw new IllegalArgumentException("Expense number is required.");
        }

        if (expense.getExpenseCategory() == null || expense.getExpenseCategory().isBlank()) {

            throw new IllegalArgumentException("Expense category is required.");
        }

        if (expense.getAmount() <= 0) {

            throw new IllegalArgumentException("Expense amount must be greater than 0.");
        }

        if (expense.getPaymentMethod() == null || expense.getPaymentMethod().isBlank()) {

            throw new IllegalArgumentException("Payment method is required.");
        }

        String paymentMethod = expense.getPaymentMethod();

        if (!paymentMethod.equals("CASH") && !paymentMethod.equals("CARD") && !paymentMethod.equals("BANK_TRANSFER")) {

            throw new IllegalArgumentException("Invalid payment method.");
        }
    }

    private void validateId(long id) {

        if (id <= 0) {

            throw new IllegalArgumentException("Invalid expense ID.");
        }
    }
}