package shpp.level3.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class InventoryDTO {
    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public int getStoreId() {
        return storeId;
    }

    @NotNull
    private int storeId;

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getProductId() {
        return productId;
    }

    @NotNull
    private int productId;

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    @NotNull
    @Positive(message = "Inventory quantity should be greater than or equal zero.")
    private int quantity;


    public InventoryDTO() {

    }
}
