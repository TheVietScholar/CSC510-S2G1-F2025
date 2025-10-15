import java.math.BigDecimal;

public class Product {
  private Long productId;
  private String name;
  private ProductType type;
  private BigDecimal price;
  private Merchant merchant;

  public Product(Long productId, String name, ProductType type, BigDecimal price, Merchant merchant) {
    this.productId = productId;
    this.name = name;
    this.type = type;
    this.price = price;
    this.merchant = merchant;
  }

  public Long getProductId() {
    return productId;
  }

  public void setProductId(Long productId) {
    this.productId = productId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ProductType getType() {
    return type;
  }

  public void setType(ProductType type) {
    this.type = type;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Merchant getMerchant() {
    return merchant;
  }

  public void setMerchant(Merchant merchant) {
    this.merchant = merchant;
  }
}
