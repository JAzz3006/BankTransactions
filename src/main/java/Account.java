public class Account {
    private long money;
    private String accNumber;
    private boolean blocked;

    public Account(String accNumber, Long money) {
        this.money = money;
        this.accNumber = accNumber;
        blocked = false;
    }

    public synchronized long getMoney() {
        return money;
    }

    public String getAccNumber() {
        return accNumber;
    }

    public synchronized void withdraw (long amount){
        money -= amount;
    }

    public synchronized void deposit (long amount){
        money += amount;
    }

    public synchronized boolean isBlocked (){
        return blocked;
    }

    public synchronized void block(){
        blocked = true;
    }

    public synchronized void unBlock(){
        blocked = false;
    }
}
