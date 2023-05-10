package shpp.level3.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

public class ProductDTO implements Serializable {

    @NotNull
    private long productTypeId;

    @NotNull
   @Size(min = 5)
   private String name;

    public ProductDTO() {
    }

    public void setProductTypeId(long productTypeId) {
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
