package shpp.level3.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public class ProductDTO implements Serializable {
    @NotNull
    private long productTypeId;

    @NotNull
   @Size(min = 5)
   private String name;

    public String getPrice() {
        return String.format("%.2f", this.price);
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @NotNull
    @Positive(message = "Price must be greater than zero")
    private float price;

    public void setProductTypeId(int productTypeId) {
        this.productTypeId = productTypeId;
    }

    public long getProductTypeId() {
        return productTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
