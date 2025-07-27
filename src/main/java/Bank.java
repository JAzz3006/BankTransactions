import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Bank {
    public final int clientsCount;
    public static  final NumberFormat FORMATTER =
            NumberFormat.getNumberInstance(Locale.of("ru", "RU"));
    public static final Long MIN_TRANSFER_AMOUNT = 100L;
    public static final Long MAX_TRANSFER_AMOUNT = 300000L;

    private Map<String, Account> accounts = new HashMap<>();
    private List<String> accNumbers = new Vector<>();
    private final Random random = new Random();

    public Bank(int clientsCount) {
        this.clientsCount = clientsCount;
        this.accounts = AccountsGen.accountGenerator(clientsCount);
        this.accNumbers = new Vector<>(accounts.keySet());
    }

    public synchronized boolean isFraud(String fromAccountNum, String toAccountNum, long amount)
            throws InterruptedException {
        Thread.sleep(1000);
        return random.nextBoolean();
    }

    /**
     * TODO: реализовать метод. Метод переводит деньги между счетами. Если сумма транзакции > 50000,
     * то после совершения транзакции, она отправляется на проверку Службе Безопасности – вызывается
     * метод isFraud. Если возвращается true, то делается блокировка счетов (как – на ваше
     * усмотрение)
     */
    public void transfer(String fromAccountNum, String toAccountNum, long amount) {
        long moneyBeforeFrom = accounts.get(fromAccountNum).getMoney();
        long moneyBeforeTo = accounts.get(toAccountNum).getMoney();
        System.out.println("Баланс счета " + fromAccountNum + " = " + FORMATTER.format(getBalance(fromAccountNum)));
        accounts.replace(fromAccountNum, new Account(fromAccountNum, moneyBeforeFrom - amount));
        System.out.println("Сумма " + FORMATTER.format(amount) + " списана со счета " + fromAccountNum);
        System.out.println("Баланс счета " + fromAccountNum + " = " + FORMATTER.format(getBalance(fromAccountNum)));
        System.out.println("Баланс счета " + toAccountNum + " = " + FORMATTER.format(getBalance(toAccountNum)));
        accounts.replace(toAccountNum, new Account(toAccountNum, moneyBeforeTo + amount));
        System.out.println("Сумма " + FORMATTER.format(amount) + " зачислена на счет " + toAccountNum);
        System.out.println("Баланс счета " + toAccountNum + " = " + FORMATTER.format(getBalance(toAccountNum)));
    }
    public void transferBuilder(){
        String[] rndAccounts = getTwoDifferentAccNumber();
        String fromAccount = rndAccounts[0];
        String toAccount = rndAccounts[1];
        Long amount = getRandomAmount();
        transfer(fromAccount, toAccount, amount);
    }

    public void multiTransferSimulator(int threadsCount, int transfersCount){
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        for (int i = 0; i < transfersCount; i++) {
            executor.submit(() -> {
                try {
                    transferBuilder();
                } catch (Exception e) {
                    System.out.println("Что-то пошло не так " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Executor service was interrupted: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * TODO: реализовать метод. Возвращает остаток на счёте.
     */
    public long getBalance(String accountNum) {
        return accounts.get(accountNum).getMoney();
    }

    public long getSumAllAccounts() {
        return accounts.values().stream()
                .map(Account::getMoney)
                .reduce(0L, Long::sum);
    }

    public String[] getTwoDifferentAccNumber() {
        int maxIndex = accNumbers.size();
        int fromIndex = ThreadLocalRandom.current().nextInt(0, maxIndex);
        int toIndex = ThreadLocalRandom.current().nextInt(0, maxIndex - 1);
        if (toIndex >= fromIndex) {
            toIndex++;
        }
        return new String[]{
                accNumbers.get(fromIndex),
                accNumbers.get(toIndex)
        };
    }


    public Long getRandomAmount(){
        return ThreadLocalRandom.current()
                .nextLong(MIN_TRANSFER_AMOUNT, MAX_TRANSFER_AMOUNT + 1);
    }

    public Map<String, Account> getAccList(){
        return accounts;
    }

    public List<String> getAccNumbers(){
        return accNumbers;
    }

    public void printAccList(){
        getAccList().forEach(
                (key, value) -> System.out.println(key + " -> " + FORMATTER.format(value.getMoney())));
    }

    public void tempPrintAccNumList(){//del before commit
        getAccNumbers()
                .forEach(elm -> System.out.println(accNumbers.indexOf(elm) + " -> " + elm));
    }
}
