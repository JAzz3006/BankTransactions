import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountsGen {
    public static final Long MIN_VALUE = 100000L;
    public static final Long MAX_VALUE = 3000000L;

    public static Map<String, Account> accountGenerator(int clientsCount){
        AtomicLong al = new AtomicLong(110070000);
        return Stream.generate(
                        () -> ThreadLocalRandom.current().nextLong(MIN_VALUE, MAX_VALUE + 1))
                .limit(clientsCount)
                .map(balance -> {
                    String accNumber = String.valueOf(al.incrementAndGet());
                    return new AbstractMap.SimpleEntry<>(accNumber, new Account(accNumber, balance));
                })
                .collect(Collectors.toMap(
                        AbstractMap.SimpleEntry::getKey,
                        AbstractMap.SimpleEntry::getValue,
                        (a, b) -> b,
                        TreeMap::new

                ));
    }
}
