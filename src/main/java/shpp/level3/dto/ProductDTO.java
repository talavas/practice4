package shpp.level3.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public class ProductDTO implements Serializable {

    @NotNull
    @Positive
    private long productTypeId;

    @NotNull
   @Size(min = 5)
   private String name;

    public ProductDTO(long productTypeId, String name, float price) {
        this.productTypeId = productTypeId;
        this.name = name;
        this.price = price;
    }

    public String getPrice() {
        return String.format("%.2f", price);
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @NotNull
    @Positive(message = "Price must be greater than zero")
    private float price;

    public ProductDTO() {
    }

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
