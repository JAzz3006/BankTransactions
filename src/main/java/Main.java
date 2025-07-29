public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank(20);
        long startMoney = bank.getSumAllAccounts();
        bank.printAccList();
        bank.multiTransferSimulator(4,50);
        System.out.format("В банке размещено %s руб (%s)",
                Bank.FORMATTER.format(bank.getSumAllAccounts()),
                Bank.FORMATTER.format(startMoney)
        );
        bank.tempPrintAccNumList();
        bank.getBlockedList();
    }
}
