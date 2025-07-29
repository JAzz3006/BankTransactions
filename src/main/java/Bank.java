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
    public static final Long MAX_TRANSFER_AMOUNT = 70000L;
    public static final Long FRAUD_CHECK_THRESH = 50000L;


    private Map<String, Account> accounts;
    private List<String> accNumbers;
    private List<String> blockedAccNumbers = Collections.synchronizedList(new ArrayList<>());
    private final Random random = new Random();

    public Bank(int clientsCount) {
        this.clientsCount = clientsCount;
        this.accounts = AccountsGen.accountGenerator(clientsCount);
        this.accNumbers = Collections.synchronizedList(new ArrayList<>(accounts.keySet()));
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
        boolean isFraud = false;
        if (amount > FRAUD_CHECK_THRESH){
            try{
                isFraud = isFraud(fromAccountNum, toAccountNum, amount);
            } catch (InterruptedException e) {
                System.out.println("Что-то пошло не так во время проверки транзакции" + e.getMessage());
                e.printStackTrace();            }
        }

        if (!isFraud){
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
        }else {
            blockAccounts(fromAccountNum, toAccountNum);
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
                    synchronized (accNumbers){
                        if (accNumbers.size() < 2){
                            return;
                        }
                    }
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

    public void blockAccounts(String fromAccountNum, String toAccountNum){
        synchronized (accNumbers){
            accNumbers.remove(fromAccountNum);
            accNumbers.remove(toAccountNum);
        }
        synchronized (blockedAccNumbers){
            blockedAccNumbers.add(fromAccountNum);
            blockedAccNumbers.add(toAccountNum);
        }
        System.out.printf("‼️ Счета %s и %s заблокированы из-за подозрительной активности\n_________\n",
                fromAccountNum, toAccountNum);

    }

    public String[] getTwoDifferentAccNumber() {
        synchronized (accNumbers){
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

    public void tempPrintAccNumList(){
        System.out.println("\n");
        synchronized (accNumbers){
            if (accNumbers.isEmpty()){
                System.out.println("Все счета заблокированы");
                return;
            }
            System.out.println("Активные счета:\n");
            accNumbers.stream()
                    .map(elm -> accNumbers.indexOf(elm) + " -> " + elm)
                    .forEach(System.out::println);
        }
    }

    public void getBlockedList(){
        System.out.println("\n");
        synchronized (blockedAccNumbers){
            if (blockedAccNumbers.isEmpty()){
                System.out.println("Ни один счет не заблокирован");
            }
            blockedAccNumbers.stream()
                    .map(elm -> blockedAccNumbers.indexOf(elm) + " - blocked -> " + elm)
                    .forEach(System.out::println);
        }
    }
}
