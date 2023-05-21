package shpp.level3.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductDTOTest {
    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = validatorFactory.getValidator();

    @Test
    void productDTO_ValidData_ValidatedSuccessfully(){
        int product_type_id = 1;
        String name = "test-product";
        float price = 10.0f;

        ProductDTO productDTO = new ProductDTO(product_type_id, name, price);
        var violations = validator.validate(productDTO);

        assertTrue(violations.isEmpty());
    }

}