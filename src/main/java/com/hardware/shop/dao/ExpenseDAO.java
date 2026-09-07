package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.Expense;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    public boolean createExpense(Expense expense) throws SQLException {

        String expenseSql = """
                INSERT INTO expenses (
                    expense_number,
                    expense_category,
                    description,
                    amount,
                    payment_method,
                    notes
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String cashTransactionSql = """
                INSERT INTO cash_transactions (
                    transaction_type,
                    reference_id,
                    amount,
                    description
                )
                VALUES (
                    'EXPENSE',
                    ?,
                    ?,
                    ?
                )
                """;

        try (Connection connection = DBConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                long expenseId;

                // 1. Create expense
                try (PreparedStatement statement = connection.prepareStatement(expenseSql, Statement.RETURN_GENERATED_KEYS)) {

                    statement.setString(1, expense.getExpenseNumber());

                    statement.setString(2, expense.getExpenseCategory());

                    statement.setString(3, expense.getDescription());

                    statement.setDouble(4, expense.getAmount());

                    statement.setString(5, expense.getPaymentMethod());

                    statement.setString(6, expense.getNotes());

                    int affectedRows = statement.executeUpdate();

                    if (affectedRows == 0) {

                        throw new SQLException("Creating expense failed.");
                    }

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                        if (!generatedKeys.next()) {

                            throw new SQLException("Creating expense failed: no ID obtained.");
                        }

                        expenseId = generatedKeys.getLong(1);
                    }
                }

                // 2. Record cash transaction
                try (PreparedStatement statement = connection.prepareStatement(cashTransactionSql)) {

                    statement.setLong(1, expenseId);

                    statement.setDouble(2, expense.getAmount());

                    statement.setString(3, "Expense payment - " + expense.getExpenseNumber());

                    statement.executeUpdate();
                }

                // 3. Commit
                connection.commit();

                return true;

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }


    public List<Expense> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    expense_number,
                    expense_category,
                    description,
                    amount,
                    payment_method,
                    expense_date,
                    notes
                FROM expenses
                ORDER BY id DESC
                """;

        List<Expense> expenses = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                expenses.add(mapExpense(resultSet));
            }
        }

        return expenses;
    }


    public Expense findById(long id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    expense_number,
                    expense_category,
                    description,
                    amount,
                    payment_method,
                    expense_date,
                    notes
                FROM expenses
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapExpense(resultSet);
                }
            }
        }

        return null;
    }


    public boolean updateExpense(Expense expense) throws SQLException {

        String updateExpenseSql = """
                UPDATE expenses
                SET
                    expense_number = ?,
                    expense_category = ?,
                    description = ?,
                    amount = ?,
                    payment_method = ?,
                    notes = ?
                WHERE id = ?
                """;

        String updateCashTransactionSql = """
                UPDATE cash_transactions
                SET
                    amount = ?,
                    description = ?
                WHERE transaction_type = 'EXPENSE'
                AND reference_id = ?
                """;

        try (Connection connection = DBConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                int affectedRows;

                // 1. Update expense
                try (PreparedStatement statement = connection.prepareStatement(updateExpenseSql)) {

                    statement.setString(1, expense.getExpenseNumber());

                    statement.setString(2, expense.getExpenseCategory());

                    statement.setString(3, expense.getDescription());

                    statement.setDouble(4, expense.getAmount());

                    statement.setString(5, expense.getPaymentMethod());

                    statement.setString(6, expense.getNotes());

                    statement.setLong(7, expense.getId());

                    affectedRows = statement.executeUpdate();
                }

                if (affectedRows == 0) {

                    connection.rollback();

                    return false;
                }

                // 2. Update related cash transaction
                try (PreparedStatement statement = connection.prepareStatement(updateCashTransactionSql)) {

                    statement.setDouble(1, expense.getAmount());

                    statement.setString(2, "Expense payment - " + expense.getExpenseNumber());

                    statement.setLong(3, expense.getId());

                    statement.executeUpdate();
                }

                // 3. Commit
                connection.commit();

                return true;

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }


    public boolean deleteExpense(long id) throws SQLException {

        String deleteCashTransactionSql = """
                DELETE FROM cash_transactions
                WHERE transaction_type = 'EXPENSE'
                AND reference_id = ?
                """;

        String deleteExpenseSql = """
                DELETE FROM expenses
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                // 1. Delete related cash transaction
                try (PreparedStatement statement = connection.prepareStatement(deleteCashTransactionSql)) {

                    statement.setLong(1, id);

                    statement.executeUpdate();
                }

                // 2. Delete expense
                int affectedRows;

                try (PreparedStatement statement = connection.prepareStatement(deleteExpenseSql)) {

                    statement.setLong(1, id);

                    affectedRows = statement.executeUpdate();
                }

                if (affectedRows == 0) {

                    connection.rollback();

                    return false;
                }

                // 3. Commit
                connection.commit();

                return true;

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }


    private Expense mapExpense(ResultSet resultSet) throws SQLException {

        Expense expense = new Expense();

        expense.setId(resultSet.getLong("id"));

        expense.setExpenseNumber(resultSet.getString("expense_number"));

        expense.setExpenseCategory(resultSet.getString("expense_category"));

        expense.setDescription(resultSet.getString("description"));

        expense.setAmount(resultSet.getDouble("amount"));

        expense.setPaymentMethod(resultSet.getString("payment_method"));

        expense.setExpenseDate(resultSet.getString("expense_date"));

        expense.setNotes(resultSet.getString("notes"));

        return expense;
    }
}