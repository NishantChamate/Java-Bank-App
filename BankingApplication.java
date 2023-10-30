import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public class BankingApplication {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/banking_db";
    private static final String DB_USER = "your_username";
    private static final String DB_PASS = "your_password";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            initializeDatabase(connection);

            List<BankAccount> accounts = new ArrayList<>();
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("1. Create a bank account");
                System.out.println("2. Deposit funds");
                System.out.println("3. Withdraw funds");
                System.out.println("4. Check balance");
                System.out.println("5. View transaction history");
                System.out.println("6. Close account");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (choice) {
                    case 1:
                        createBankAccount(scanner, connection, accounts);
                        break;
                    case 2:
                        depositFunds(scanner, connection, accounts);
                        break;
                    case 3:
                        withdrawFunds(scanner, connection, accounts);
                        break;
                    case 4:
                        checkBalance(scanner, accounts);
                        break;
                    case 5:
                        viewTransactionHistory(scanner, accounts);
                        break;
                    case 6:
                        closeAccount(scanner, connection, accounts);
                        break;
                    case 0:
                        scanner.close();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Initialize the database and tables
    private static void initializeDatabase(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS banking_db");
            statement.executeUpdate("USE banking_db");

            // Create the account and transaction tables if they don't exist
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "account_number VARCHAR(20) NOT NULL UNIQUE," +
                    "balance DOUBLE NOT NULL," +
                    "months INT NOT NULL" +
                    ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "account_id INT NOT NULL," +
                    "payee VARCHAR(255) NOT NULL," +
                    "payer VARCHAR(255) NOT NULL," +
                    "transaction_type VARCHAR(50) NOT NULL," +
                    "amount DOUBLE NOT NULL," +
                    "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (account_id) REFERENCES accounts(id)" +
                    ")");
        }
    }

    private static void createBankAccount(Scanner scanner, Connection connection, List<BankAccount> accounts) throws SQLException {
        System.out.print("Enter account holder's name: ");
        String name = scanner.nextLine();
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Enter initial balance: ");
        double balance = scanner.nextDouble();
        System.out.print("Enter the number of months for interest calculation: ");
        int months = scanner.nextInt();

        BankAccount account = new BankAccount(name, accountNumber, balance, months);
        accounts.add(account);

        try (PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT INTO accounts (name, account_number, balance, months) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            insertStatement.setString(1, name);
            insertStatement.setString(2, accountNumber);
            insertStatement.setDouble(3, balance);
            insertStatement.setInt(4, months);
            insertStatement.executeUpdate();

            ResultSet generatedKeys = insertStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int accountId = generatedKeys.getInt(1);
                account.setAccountId(accountId);
                System.out.println("Account created with ID: " + accountId);
            }
        }
    }

    private static void depositFunds(Scanner scanner, Connection connection, List<BankAccount> accounts) throws SQLException {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = findAccountByAccountNumber(accountNumber, accounts);

        if (account != null) {
            System.out.print("Enter the deposit amount: ");
            double amount = scanner.nextDouble();
            account.deposit(amount);

            // Update the database
            try (PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE accounts SET balance = ? WHERE id = ?")) {
                updateStatement.setDouble(1, account.getBalance());
                updateStatement.setInt(2, account.getAccountId());
                updateStatement.executeUpdate();
            }

            // Record the transaction
            recordTransaction(connection, account, "Deposit", amount);
            System.out.println("Deposit successful.");
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void withdrawFunds(Scanner scanner, Connection connection, List<BankAccount> accounts) throws SQLException {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = findAccountByAccountNumber(accountNumber, accounts);

        if (account != null) {
            System.out.print("Enter the withdrawal amount: ");
            double amount = scanner.nextDouble();

            if (account.getBalance() >= amount) {
                account.withdraw(amount);

                // Update the database
                try (PreparedStatement updateStatement = connection.prepareStatement(
                        "UPDATE accounts SET balance = ? WHERE id = ?")) {
                    updateStatement.setDouble(1, account.getBalance());
                    updateStatement.setInt(2, account.getAccountId());
                    updateStatement.executeUpdate();
                }

                // Record the transaction
                recordTransaction(connection, account, "Withdrawal", amount);
                System.out.println("Withdrawal successful.");
            } else {
                System.out.println("Insufficient funds.");
            }
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void checkBalance(Scanner scanner, List<BankAccount> accounts) {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = findAccountByAccountNumber(accountNumber, accounts);

        if (account != null) {
            System.out.println("Account balance: $" + account.getBalance());
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void viewTransactionHistory(Scanner scanner, List<BankAccount> accounts) {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = findAccountByAccountNumber(accountNumber, accounts);

        if (account != null) {
            // Retrieve and display transaction history from the database
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date")) {
                statement.setInt(1, account.getAccountId());
                ResultSet resultSet = statement.executeQuery();

                System.out.println("Transaction History for Account: " + account.getAccountNumber());
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String payee = resultSet.getString("payee");
                    String payer = resultSet.getString("payer");
                    String transactionType = resultSet.getString("transaction_type");
                    double amount = resultSet.getDouble("amount");
                    Date transactionDate = resultSet.getTimestamp("transaction_date");

                    System.out.println("ID: " + id + ", Payee: " + payee + ", Payer: " + payer +
                            ", Type: " + transactionType + ", Amount: $" + amount +
                            ", Date: " + transactionDate);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void closeAccount(Scanner scanner, Connection connection, List<BankAccount> accounts) throws SQLException {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        BankAccount account = findAccountByAccountNumber(accountNumber, accounts);

        if (account != null) {
            // Remove the account from the list
            accounts.remove(account);

            // Delete the account and associated transactions from the database
            try (PreparedStatement deleteAccountStatement = connection.prepareStatement("DELETE FROM accounts WHERE id = ?")) {
                deleteAccountStatement.setInt(1, account.getAccountId());
                deleteAccountStatement.executeUpdate();
            }

            System.out.println("Account closed successfully.");
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void recordTransaction(Connection connection, BankAccount account, String transactionType, double amount) {
        try (PreparedStatement insertTransactionStatement = connection.prepareStatement(
                "INSERT INTO transactions (account_id, payee, payer, transaction_type, amount) VALUES (?, ?, ?, ?, ?)")) {
            insertTransactionStatement.setInt(1, account.getAccountId());
            insertTransactionStatement.setString(2, account.getName());
            insertTransactionStatement.setString(3, account.getName());
            insertTransactionStatement.setString(4, transactionType);
            insertTransactionStatement.setDouble(5, amount);
            insertTransactionStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static BankAccount findAccountByAccountNumber(String accountNumber, List<BankAccount> accounts) {
        for (BankAccount account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }
}
