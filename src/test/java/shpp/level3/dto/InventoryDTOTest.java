package shpp.level3.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryDTOTest {
    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Test
    void inventoryDTO_ValidData_ValidatedSuccessfully() {
        int storeId = 1;
        int productId = 2;
        int quantity = 10;

        InventoryDTO inventoryDTO = new InventoryDTO(storeId, productId, quantity);
        var violations = validator.validate(inventoryDTO);

        assertTrue(violations.isEmpty());
    }

    @Test
    void inventoryDTO_NullStoreId_ValidationFails() {

        int productId = 2;
        int quantity = 10;

        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setProductId(productId);
        inventoryDTO.setQuantity(quantity);
        var violations = validator.validate(inventoryDTO);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void inventoryDTO_NullProductId_ValidationFails() {
        int storeId = 1;
        int quantity = 10;

        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setStoreId(storeId);
        inventoryDTO.setQuantity(quantity);
        var violations = validator.validate(inventoryDTO);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void inventoryDTO_NegativeQuantity_ValidationFails() {
        int storeId = 1;
        int productId = 2;
        int quantity = -5;

        InventoryDTO inventoryDTO = new InventoryDTO(storeId, productId, quantity);
        var violations = validator.validate(inventoryDTO);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Inventory quantity should be greater than zero.", violations.iterator().next().getMessage());
    }

    @Test
    void inventoryDTO_ZeroQuantity_ValidationSucceeds() {
        int storeId = 1;
        int productId = 2;
        int quantity = 0;

        InventoryDTO inventoryDTO = new InventoryDTO(storeId, productId, quantity);
        var violations = validator.validate(inventoryDTO);

        assertFalse(violations.isEmpty());
    }
}