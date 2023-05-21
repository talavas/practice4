package shpp.level3.util;

import java.util.Random;

public class RandomGenerator {
    private static final Random random = new Random();
    protected static final int MAX_LENGTH = 50;

    public static String generateRandomString() {
        int length  = random.nextInt(MAX_LENGTH);
        return random.ints('a', 'z' + 1)
                .limit(length + 1L)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
