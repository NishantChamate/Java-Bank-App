import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class BankAccount {
    String name;
    String accountNumber;
    double balance;
    static double interest;
    int months;

    public BankAccount(String name, String accountNumber, double balance) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    void deposit(double amount) {
        balance += amount;
    }

    void withdraw(double amount) {
        if (amount <= balance) {
            balance -= amount;
        } else {
            System.out.println("Insufficient balance.");
        }
    }

    double getBalance() {
        return balance;
    }

    static double getInterest() {
        return interest;
    }

    double calculateInterest() {
        return balance * interest * months / 12.0;
    }

    String getAccountNumber() {
        return accountNumber;
    }

    String getName() {
        return name;
    }
}

class Transaction extends BankAccount {
    String payee;
    String payer;
    String transactionType;
    double amount;
    Date transactionDate;

    public Transaction(String name, String accountNumber, double balance, String payee, String payer,
            String transactionType, double amount, Date transactionDate) {
        super(name, accountNumber, balance);
        this.payee = payee;
        this.payer = payer;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }
}

public class BankingApplication {
    private List<BankAccount> accounts;

    public BankingApplication() {
        accounts = new ArrayList<>();
    }

    public void createAccount(String name, String accountNumber, double initialBalance) {
        BankAccount account = new BankAccount(name, accountNumber, initialBalance);
        accounts.add(account);
    }

    public BankAccount findAccountByAccountNumber(String accountNumber) {
        for (BankAccount account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null; // Account not found
    }

    public void withdraw(String accountNumber, double amount) {
        BankAccount withdrawAccount = findAccountByAccountNumber(accountNumber);
        if (withdrawAccount != null) {
            withdrawAccount.withdraw(amount);
            System.out.println("Withdrawal successful.");
        } else {
            System.out.println("Account not found.");
        }
    }

    public void checkBalance(String accountNumber) {
        BankAccount account = findAccountByAccountNumber(accountNumber);
        if (account != null) {
            System.out.println("Account Balance: " + account.getBalance());
        } else {
            System.out.println("Account not found.");
        }
    }

    public static void main(String[] args) {
        BankingApplication app = new BankingApplication();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("Banking Application Menu:");
            System.out.println("1. Create Account");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Check Balance");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");

            int choice = sc.nextInt();
            sc.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    System.out.print("Enter name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter account number: ");
                    String accountNumber = sc.nextLine();
                    System.out.print("Enter initial balance: ");
                    double initialBalance = sc.nextDouble();
                    app.createAccount(name, accountNumber, initialBalance);
                    break;

                case 2:
                    System.out.print("Enter account number: ");
                    accountNumber = sc.nextLine();
                    BankAccount depositAccount = app.findAccountByAccountNumber(accountNumber);
                    if (depositAccount != null) {
                        System.out.print("Enter deposit amount: ");
                        double depositAmount = sc.nextDouble();
                        depositAccount.deposit(depositAmount);
                        System.out.println("Deposit successful.");
                    } else {
                        System.out.println("Account not found.");
                    }
                    break;

                case 3:
                    System.out.print("Enter account number: ");
                    accountNumber = sc.nextLine();
                    System.out.print("Enter withdrawal amount: ");
                    double withdrawalAmount = sc.nextDouble();
                    app.withdraw(accountNumber, withdrawalAmount);
                    break;

                case 4:
                    System.out.print("Enter account number: ");
                    accountNumber = sc.nextLine();
                    app.checkBalance(accountNumber);
                    break;

                case 5:
                    System.out.println("Exiting the application.");
                    sc.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }
    }
}
