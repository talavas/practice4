package shpp.level3.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RandomGeneratorTest {
    @Test
    void generateRandomString_ShouldNotExceedMaxLength() {
        // Arrange
        int maxLength = RandomGenerator.MAX_LENGTH;

        // Act
        String randomString = RandomGenerator.generateRandomString();

        // Assert
        assertTrue(randomString.length() <= maxLength);
    }
}