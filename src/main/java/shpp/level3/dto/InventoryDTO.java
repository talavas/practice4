package shpp.level3.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class InventoryDTO {
    public InventoryDTO(int storeId, int productId, int quantity) {
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public int getStoreId() {
        return storeId;
    }

    @NotNull
    @Positive
    private int storeId;

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getProductId() {
        return productId;
    }

    @NotNull
    @Positive
    private int productId;

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    @NotNull
    @Positive(message = "Inventory quantity should be greater than zero.")
    private int quantity;


    public InventoryDTO() {

    }
}
