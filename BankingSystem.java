import java.util.*;
import java.util.stream.Collectors;

// 1. Custom Exception for Error Handling
class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

// 2. Strategy Pattern for Interest Calculation
interface InterestStrategy {
    double calculate(double balance);
}

class SavingsInterest implements InterestStrategy {
    public double calculate(double balance) { return balance * 0.04; } // 4%
}

class LoanInterest implements InterestStrategy {
    public double calculate(double balance) { return balance * 0.10; } // 10%
}

// 3. Abstract Base Class
abstract class Account {
    private String accountNumber;
    private String holderName;
    protected double balance;

    public Account(String accountNumber, String holderName, double initialBalance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = initialBalance;
    }

    // Synchronized for Concurrency/Multithreading safety
    public synchronized void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println("Deposited " + amount + " to " + accountNumber);
        }
    }

    public synchronized void withdraw(double amount) throws InsufficientFundsException {
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient funds in account: " + accountNumber);
        }
        balance -= amount;
        System.out.println("Withdrew " + amount + " from " + accountNumber);
    }

    public abstract void applyInterest();

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public String getHolderName() { return holderName; }
}

// 4. Concrete Account Types
class SavingsAccount extends Account {
    private InterestStrategy interestStrategy = new SavingsInterest();

    public SavingsAccount(String accNum, String name, double bal) {
        super(accNum, name, bal);
    }

    @Override
    public void applyInterest() {
        double interest = interestStrategy.calculate(balance);
        balance += interest;
        System.out.println("Interest of " + interest + " applied to Savings Account: " + getAccountNumber());
    }
}

// 5. Singleton Bank Service (The Main Manager)
class BankService {
    private static BankService instance;
    private Map<String, Account> accounts = new HashMap<>();

    private BankService() {}

    public static synchronized BankService getInstance() {
        if (instance == null) instance = new BankService();
        return instance;
    }

    public void addAccount(Account account) {
        accounts.put(account.getAccountNumber(), account);
        System.out.println("Account Created for: " + account.getHolderName());
    }

    public void transfer(String fromAcc, String toAcc, double amount) {
        try {
            Account sender = Optional.ofNullable(accounts.get(fromAcc))
                    .orElseThrow(() -> new RuntimeException("Sender account not found"));
            Account receiver = Optional.ofNullable(accounts.get(toAcc))
                    .orElseThrow(() -> new RuntimeException("Receiver account not found"));

            sender.withdraw(amount);
            receiver.deposit(amount);
            System.out.println("--- Transfer Completed Successfully ---");
        } catch (Exception e) {
            System.out.println("Transaction Failed: " + e.getMessage());
        }
    }

    public void displayAllAccounts() {
        System.out.println("\n--- Bank Report (Using Streams) ---");
        accounts.values().stream()
                .forEach(a -> System.out.println("Acc: " + a.getAccountNumber() + " | Name: " + a.getHolderName() + " | Balance: " + a.getBalance()));
    }
}

// 6. Main Execution Class
public class BankingSystem {
    public static void main(String[] args) {
        BankService bank = BankService.getInstance();

        // Adding Accounts
        bank.addAccount(new SavingsAccount("SA101", "Rahul", 5000));
        bank.addAccount(new SavingsAccount("SA102", "Priya", 3000));

        System.out.println("\n--- Starting Transactions ---");
        
        // Performing a Transfer
        bank.transfer("SA101", "SA102", 1500);

        // Applying Interest
        bank.addAccount(new SavingsAccount("SA103", "Internee", 1000));
        // Note: Realistically you'd loop through all accounts
        
        // Display Final Status
        bank.displayAllAccounts();

        // Testing Exception Handling
        System.out.println("\n--- Testing Error Handling ---");
        bank.transfer("SA101", "SA102", 10000); // Should fail
    }
}
