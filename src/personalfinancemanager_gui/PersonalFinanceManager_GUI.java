/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package personalfinancemanager_gui;

/**
 *
 * @author dell
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;


public class PersonalFinanceManager_GUI {
    
    private static Connection getConnection() throws SQLException {
    String url = "jdbc:mysql://localhost:3306/personalfinancemanager";
    String username = "root";
    String password = "";
    
    return DriverManager.getConnection(url, username, password);
}
    
    static double totalBalance;
    
    private static double calculateTotalBalance() {
    double totalBalance = 0.0;
    try (Connection connection = getConnection();
         Statement statement = connection.createStatement()) {
        String sql = "SELECT type, amount FROM transactions";
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            String type = resultSet.getString("type");
            double amount = resultSet.getDouble("amount");
            if ("Income".equalsIgnoreCase(type)) {
                totalBalance += amount;
            } else if ("Expense".equalsIgnoreCase(type)) {
                totalBalance -= amount;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return totalBalance;
}



    public static void main(String[] args) {
        JFrame frame = new JFrame("Swing UI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);

        JPanel navbar = new JPanel();
        navbar.setLayout(new GridLayout(1, 4));

        JButton transactionsButton = new JButton("Transactions");
        JButton budgetsButton = new JButton("Budgets");
        JButton goalsButton = new JButton("Goals");
        JButton settingsButton = new JButton("Settings");

        transactionsButton.setBackground(Color.RED);
        budgetsButton.setBackground(Color.GREEN);
        goalsButton.setBackground(Color.BLUE);
        settingsButton.setBackground(Color.YELLOW);

        navbar.add(transactionsButton);
        navbar.add(budgetsButton);
        navbar.add(goalsButton);
        navbar.add(settingsButton);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());

        JLabel balanceLabel = new JLabel("Balance: $"+calculateTotalBalance(), SwingConstants.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel.setForeground(Color.BLACK);

        contentPanel.add(balanceLabel, BorderLayout.NORTH);

        JLabel contentLabel = new JLabel("--- Welcome ---", SwingConstants.CENTER);
        contentPanel.add(contentLabel, BorderLayout.CENTER);

        transactionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentPanel.removeAll();
                contentLabel.setText("Transactions Content");
                contentPanel.add(balanceLabel, BorderLayout.NORTH);
                showTransactions(contentPanel);
            }
        });

        budgetsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentPanel.removeAll();
                contentLabel.setText("Budgets Content");
                showBudgets(contentPanel);
            }
        });

        goalsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentPanel.removeAll();
                contentLabel.setText("Goals Content");
                showGoals(contentPanel);
            }
        });

        settingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contentPanel.removeAll();
                contentLabel.setText("Settings Content");
                contentPanel.add(balanceLabel, BorderLayout.NORTH);
                showSettings(contentPanel);
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(navbar, BorderLayout.NORTH);
        frame.add(contentPanel, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
private static void showTransactions(JPanel contentPanel) {
    JPanel transactionsPanel = new JPanel();
    transactionsPanel.setLayout(new BoxLayout(transactionsPanel, BoxLayout.Y_AXIS));

    JButton addTransactionButton = new JButton("Add Transaction");
    addTransactionButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            createTransactionFrame(contentPanel);
        }
    });

    transactionsPanel.add(addTransactionButton);


    try (Connection connection = getConnection()) {
        Statement statement = connection.createStatement();
        String sql = "SELECT * FROM transactions";
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String type = resultSet.getString("type");
            double amount = resultSet.getDouble("amount");
            String description = resultSet.getString("description");

            JPanel transactionPanel = new JPanel();
            transactionPanel.setLayout(new BorderLayout());
            transactionPanel.setPreferredSize(new Dimension(500, 120));
            transactionPanel.setMaximumSize(new Dimension(500, 120));
            transactionPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

            String title = description != null ? description : "Unnamed Transaction";

            JPanel titlePanel = new JPanel();
            titlePanel.setLayout(new BorderLayout());
            JLabel transactionLabel = new JLabel(title + " - $" + amount, SwingConstants.CENTER);

            if ("Income".equalsIgnoreCase(type)) {
                transactionLabel.setForeground(Color.GREEN);
            } else if ("Expense".equalsIgnoreCase(type)) {
                transactionLabel.setForeground(Color.RED);
            }

            titlePanel.add(transactionLabel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

            JButton viewDetailsButton = new JButton("View Details");
            JButton deleteButton = new JButton("Delete");

            viewDetailsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(null, "Viewing details for: " + title);
                }
            });

            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try (Connection connection = getConnection()) {
                        String deleteSQL = "DELETE FROM transactions WHERE id = " + id;
                        Statement deleteStatement = connection.createStatement();
                        int rowsDeleted = deleteStatement.executeUpdate(deleteSQL);

                        if (rowsDeleted > 0) {
                            JOptionPane.showMessageDialog(null, "Transaction deleted successfully!");
                            showTransactions(contentPanel);
                        } else {
                            JOptionPane.showMessageDialog(null, "Error deleting transaction.");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            buttonPanel.add(viewDetailsButton);
            buttonPanel.add(deleteButton);

            transactionPanel.add(titlePanel, BorderLayout.CENTER);
            transactionPanel.add(buttonPanel, BorderLayout.SOUTH);

            transactionsPanel.add(transactionPanel);
        }

        resultSet.close();
        statement.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    

    JLabel balanceLabel = new JLabel("Balance: $" + calculateTotalBalance(), SwingConstants.CENTER);
    balanceLabel.setFont(new Font("Arial", Font.BOLD, 16));
    balanceLabel.setForeground(Color.BLACK);

    JScrollPane scrollPane = new JScrollPane(transactionsPanel);
    contentPanel.removeAll();
    contentPanel.add(balanceLabel, BorderLayout.NORTH);
    contentPanel.add(scrollPane, BorderLayout.CENTER);
    contentPanel.revalidate();
    contentPanel.repaint();
}


private static void createTransactionFrame(JPanel contentPanel) {
    JFrame transactionFrame = new JFrame("Create Transaction");
    transactionFrame.setSize(400, 300);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JLabel typeLabel = new JLabel("Transaction Type:");
    JRadioButton incomeButton = new JRadioButton("Income");
    JRadioButton expenseButton = new JRadioButton("Expense");
    ButtonGroup group = new ButtonGroup();
    group.add(incomeButton);
    group.add(expenseButton);

    JLabel amountLabel = new JLabel("Amount:");
    JTextField amountField = new JTextField(10);

    JLabel descriptionLabel = new JLabel("Description:");
    JTextField descriptionField = new JTextField(10);

    JLabel budgetLabel = new JLabel("Budget:");
    JComboBox<String> budgetComboBox = new JComboBox<>();

    budgetComboBox.addItem("No Budget");
    loadBudgets(budgetComboBox);

    JButton submitButton = new JButton("Submit");

    panel.add(typeLabel);
    panel.add(incomeButton);
    panel.add(expenseButton);

    panel.add(amountLabel);
    panel.add(amountField);

    panel.add(descriptionLabel);
    panel.add(descriptionField);

    panel.add(budgetLabel);
    panel.add(budgetComboBox);

    panel.add(submitButton);

    submitButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String type = incomeButton.isSelected() ? "Income" : "Expense";
                double amount = Double.parseDouble(amountField.getText());

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(transactionFrame, "Amount must be greater than zero.");
                    return;
                }

                if ("Expense".equals(type)) {
                    if (calculateTotalBalance() <= 0) {
                        JOptionPane.showMessageDialog(transactionFrame, "Insufficient overall balance.");
                        return;
                    }

                    if (amount > calculateTotalBalance()) {
                        JOptionPane.showMessageDialog(transactionFrame, "This transaction would exceed the overall balance.");
                        return;
                    }
                }

                String description = descriptionField.getText();
                String selectedBudget = (String) budgetComboBox.getSelectedItem();

                Integer selectedBudgetId = null;
                if (!"No Budget".equals(selectedBudget)) {
                    selectedBudgetId = getBudgetIdByName(selectedBudget);
                }

                String insertSql = "INSERT INTO transactions (type, amount, description, budget_id) VALUES ('"
                        + type + "', " + amount + ", '" + description + "', "
                        + (selectedBudgetId != null ? selectedBudgetId : "NULL") + ")";
                try (Connection connection = getConnection();
                     Statement statement = connection.createStatement()) {
                    statement.executeUpdate(insertSql);
                    if ("Income".equals(type)) {
                        totalBalance += amount;
                    } else {
                        totalBalance -= amount;
                    }
                    JOptionPane.showMessageDialog(transactionFrame, "Transaction added successfully.");
                    showTransactions(contentPanel);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(transactionFrame, "Please enter a valid amount.");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    });

    transactionFrame.add(panel);
    transactionFrame.setLocationRelativeTo(null);
    transactionFrame.setVisible(true);
}


private static void loadBudgets(JComboBox<String> budgetComboBox) {
    try (Connection connection = getConnection()) {
        Statement statement = connection.createStatement();
        String sql = "SELECT id, name FROM budgets";
        ResultSet resultSet = statement.executeQuery(sql);
        
        while (resultSet.next()) {
            String budgetName = resultSet.getString("name");
            budgetComboBox.addItem(budgetName);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

private static double getBudgetLimit(int budgetId) {
    double limit = 0.0;
    try (Connection connection = getConnection()) {
        Statement statement = connection.createStatement();
        String sql = "SELECT maxLimit FROM budgets WHERE id = " + budgetId;
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            limit = resultSet.getDouble("maxLimit");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return limit;
}

private static int getBudgetIdByName(String budgetName) {
    int budgetId = -1;
    try (Connection connection = getConnection()) {
        Statement statement = connection.createStatement();
        String sql = "SELECT id FROM budgets WHERE name = '" + budgetName + "'";
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) {
            budgetId = resultSet.getInt("id");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return budgetId;
}




    private static void showBudgets(JPanel contentPanel) {
    contentPanel.removeAll();

    JPanel budgetsPanel = new JPanel();
    budgetsPanel.setLayout(new BoxLayout(budgetsPanel, BoxLayout.Y_AXIS));

    JButton addBudgetButton = new JButton("Add New Budget");
    addBudgetButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            createBudgetFrame(contentPanel);
        }
    });

    budgetsPanel.add(addBudgetButton);

    try (Connection connection = getConnection()) {
        Statement statement = connection.createStatement();
        String sql = "SELECT * FROM budgets";
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            double maxLimit = resultSet.getDouble("maxLimit");

            double totalTransactions = 0.0;

            try (Statement transactionStatement = connection.createStatement()) {
                String transactionSql = "SELECT SUM(amount) AS total FROM transactions WHERE budget_id = " + id;
                ResultSet transactionResultSet = transactionStatement.executeQuery(transactionSql);

                if (transactionResultSet.next()) {
                    totalTransactions = transactionResultSet.getDouble("total");
                }

                transactionResultSet.close();
            }

            double remainingAmount = maxLimit - totalTransactions;

            JPanel budgetPanel = new JPanel();
            budgetPanel.setLayout(new BorderLayout());
            budgetPanel.setPreferredSize(new Dimension(500, 120));
            budgetPanel.setMaximumSize(new Dimension(500, 120));
            budgetPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

            JLabel budgetLabel = new JLabel(
                String.format("%s - Max Limit: $%.2f, Remaining: $%.2f", name, maxLimit, remainingAmount),
                SwingConstants.CENTER
            );

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

            JButton deleteButton = new JButton("Delete");

            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteBudget(id);
                    showBudgets(contentPanel);
                }
            });

            buttonPanel.add(deleteButton);

            budgetPanel.add(budgetLabel, BorderLayout.CENTER);
            budgetPanel.add(buttonPanel, BorderLayout.SOUTH);

            budgetsPanel.add(budgetPanel);
        }

        resultSet.close();
        statement.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }

    JScrollPane scrollPane = new JScrollPane(budgetsPanel);
    contentPanel.add(scrollPane, BorderLayout.CENTER);
    contentPanel.revalidate();
    contentPanel.repaint();
}



private static void createBudgetFrame(JPanel contentPanel) {
    JFrame addBudgetFrame = new JFrame("Add New Budget");
    addBudgetFrame.setSize(400, 300);
    addBudgetFrame.setLayout(new GridLayout(4, 2, 10, 10));

    JLabel nameLabel = new JLabel("Budget Name:");
    JTextField nameField = new JTextField();

    JLabel maxLimitLabel = new JLabel("Max Limit:");
    JTextField maxLimitField = new JTextField();

    JButton saveButton = new JButton("Save");
    JButton cancelButton = new JButton("Cancel");

    saveButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText().trim();
            String maxLimitText = maxLimitField.getText().trim();

            if (name.isEmpty() || maxLimitText.isEmpty()) {
                JOptionPane.showMessageDialog(addBudgetFrame, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double maxLimit = Double.parseDouble(maxLimitText);

                if (maxLimit <= 0) {
                    JOptionPane.showMessageDialog(addBudgetFrame, "Max limit must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (Connection connection = getConnection()) {
                    String checkSql = "SELECT COUNT(*) AS count FROM budgets WHERE name = '" + name + "'";
                    Statement checkStatement = connection.createStatement();
                    ResultSet resultSet = checkStatement.executeQuery(checkSql);

                    if (resultSet.next() && resultSet.getInt("count") > 0) {
                        JOptionPane.showMessageDialog(addBudgetFrame, "A budget with this name already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String insertSql = "INSERT INTO budgets (name, maxLimit) VALUES ('" + name + "', " + maxLimit + ")";
                    Statement insertStatement = connection.createStatement();
                    insertStatement.executeUpdate(insertSql);

                    JOptionPane.showMessageDialog(addBudgetFrame, "Budget added successfully.");
                    addBudgetFrame.dispose();
                    showBudgets(contentPanel);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(addBudgetFrame, "Error saving budget.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addBudgetFrame, "Invalid max limit. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    });

    cancelButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            addBudgetFrame.dispose();
        }
    });

    addBudgetFrame.add(nameLabel);
    addBudgetFrame.add(nameField);
    addBudgetFrame.add(maxLimitLabel);
    addBudgetFrame.add(maxLimitField);
    addBudgetFrame.add(saveButton);
    addBudgetFrame.add(cancelButton);

    addBudgetFrame.setLocationRelativeTo(null);
    addBudgetFrame.setVisible(true);
}


private static void deleteBudget(int budgetId) {
    try (Connection connection = getConnection()) {
        connection.setAutoCommit(false);
        try {
            Statement statement = connection.createStatement();
            String deleteTransactionsSql = "DELETE FROM transactions WHERE budget_id = " + budgetId;
            statement.executeUpdate(deleteTransactionsSql);
            String deleteBudgetSql = "DELETE FROM budgets WHERE id = " + budgetId;
            statement.executeUpdate(deleteBudgetSql);
            connection.commit();
            JOptionPane.showMessageDialog(null, "Budget and its transactions deleted successfully.");
        } catch (SQLException e) {
            connection.rollback();
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error deleting budget and its transactions.");
        } finally {
            connection.setAutoCommit(true);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}



private static void showGoals(JPanel contentPanel) {
    JPanel goalsPanel = new JPanel();
    goalsPanel.setLayout(new BoxLayout(goalsPanel, BoxLayout.Y_AXIS));

    JButton addGoalButton = new JButton("Add New Goal");
    addGoalButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            createGoalFrame(contentPanel);
        }
    });

    goalsPanel.add(addGoalButton);

    try (Connection connection = getConnection()) {
        Statement statement = connection.createStatement();
        String sql = "SELECT * FROM goals WHERE isCompleted = false";
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            double target = resultSet.getDouble("target");

            JPanel goalPanel = new JPanel();
            goalPanel.setLayout(new BorderLayout());
            goalPanel.setPreferredSize(new Dimension(500, 120));
            goalPanel.setMaximumSize(new Dimension(500, 120));
            goalPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

            JLabel goalLabel = new JLabel(name + " - Target: $" + target, SwingConstants.CENTER);
            goalLabel.setForeground(Color.RED);

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

            JButton completeButton = new JButton("Complete Goal");

            completeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    completeGoal(id);
                    contentPanel.removeAll();
                    showGoals(contentPanel);
                }
            });

            buttonPanel.add(completeButton);

            goalPanel.add(goalLabel, BorderLayout.CENTER);
            goalPanel.add(buttonPanel, BorderLayout.SOUTH);

            goalsPanel.add(goalPanel);
        }

        resultSet.close();
        statement.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }

    JScrollPane scrollPane = new JScrollPane(goalsPanel);
    contentPanel.add(scrollPane, BorderLayout.CENTER);
    contentPanel.revalidate();
    contentPanel.repaint();
}

private static void createGoalFrame(JPanel contentPanel) {
    JFrame goalFrame = new JFrame("Create New Goal");
    goalFrame.setSize(400, 300);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JLabel nameLabel = new JLabel("Goal Name:");
    JTextField nameField = new JTextField(10);

    JLabel targetLabel = new JLabel("Target Amount:");
    JTextField targetField = new JTextField(10);

    JButton submitButton = new JButton("Submit");

    panel.add(nameLabel);
    panel.add(nameField);

    panel.add(targetLabel);
    panel.add(targetField);

    panel.add(submitButton);

    submitButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            double target = 0.0;

            try {
                target = Double.parseDouble(targetField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(goalFrame, "Please enter a valid target amount.");
                return;
            }

            if (name.isEmpty() || target <= 0) {
                JOptionPane.showMessageDialog(goalFrame, "Please provide valid goal name and target.");
                return;
            }

            try (Connection connection = getConnection()) {
                Statement statement = connection.createStatement();
                String insertSQL = "INSERT INTO goals (name, target, isCompleted) VALUES ('"
                        + name + "', " + target + ", false)";
                int rowsInserted = statement.executeUpdate(insertSQL);
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(goalFrame, "Goal added successfully!");
                    contentPanel.removeAll();
                    showGoals(contentPanel);
                } else {
                    JOptionPane.showMessageDialog(goalFrame, "Error adding goal.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            goalFrame.dispose();
        }
    });

    goalFrame.add(panel);
    goalFrame.setLocationRelativeTo(null);
    goalFrame.setVisible(true);
}



private static void completeGoal(int id) {
    try (Connection connection = getConnection()) {
        String updateSQL = "UPDATE goals SET isCompleted = true WHERE id = " + id;
        Statement updateStatement = connection.createStatement();
        int rowsUpdated = updateStatement.executeUpdate(updateSQL);

        if (rowsUpdated > 0) {
            JOptionPane.showMessageDialog(null, "Goal completed successfully!");
        } else {
            JOptionPane.showMessageDialog(null, "Error completing goal.");
        }

        updateStatement.close();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}



    private static void showSettings(JPanel contentPanel) {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

        JButton changeThemeButton = new JButton("Change Theme");
        JButton changePasswordButton = new JButton("Change Password");
        JButton logoutButton = new JButton("Logout");

        settingsPanel.add(changeThemeButton);
        settingsPanel.add(changePasswordButton);
        settingsPanel.add(logoutButton);

        contentPanel.add(settingsPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
}

