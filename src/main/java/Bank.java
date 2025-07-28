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
        if (fromAccountNum.equals(toAccountNum)) return;
        Account from = accounts.get(fromAccountNum);
        Account to = accounts.get(toAccountNum);

        Account firstLock = fromAccountNum.compareTo(toAccountNum) < 0 ? from : to;
        Account secondLock = fromAccountNum.compareTo(toAccountNum) < 0 ? to : from;

        synchronized (firstLock){
            synchronized (secondLock){
                StringBuilder builder = new StringBuilder();
                long fromInitBalance = from.getMoney();
                long toInitBalance = to.getMoney();
                if (fromInitBalance < amount) return;

                from.withdraw(amount);
                to.deposit(amount);

                builder
                        .append("Со счета ").append(fromAccountNum)
                        .append(" (входящий баланс ").append(FORMATTER.format(fromInitBalance)).append(") ")
                        .append(" на счет ").append(toAccountNum)
                        .append(" (входящий баланс ").append(FORMATTER.format(toInitBalance)).append(") ")
                        .append(" переведена сумма ").append(FORMATTER.format(amount))
                        .append("\nБаланс счета ").append(fromAccountNum).append(" -> ").append(FORMATTER.format(from.getMoney()))
                        .append("\nБаланс счета ").append(toAccountNum).append(" -> ").append(FORMATTER.format(to.getMoney()))
                        .append("\n_________")
                ;
                System.out.println(builder);
            }
        }

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
