public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank(10);
        System.out.format("В банке размещено %s руб.\n", Bank.FORMATTER.format(bank.getSumAllAccounts()));
        bank.printAccList();
        //bank.tempPrintAccNumList();
        //System.out.println(bank.getAccList().get(bank.getRandomAccNumber()).getAccNumber());
        bank.multiTransferSimulator(4,10);
        System.out.format("В банке размещено %s руб", Bank.FORMATTER.format(bank.getSumAllAccounts()));

    }
}
