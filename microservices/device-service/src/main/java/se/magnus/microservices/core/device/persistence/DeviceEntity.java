package se.magnus.microservices.core.device.persistence;
import static java.lang.String.format;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "devices")
public class DeviceEntity {

  @Id private String id;

  @Version private Integer version;

  @Indexed
  private int productId;

  private int recommendationId;
  private String author;
  private int rate;
  private String content;

  public DeviceEntity() {}

  public DeviceEntity(int productId, int recommendationId, String author, int rate, String content) {
    this.productId = productId;
    this.recommendationId = recommendationId;
    this.author = author;
    this.rate = rate;
    this.content = content;
  }

  @Override
  public String toString() {
    return format("DeviceEntity: %s", recommendationId);
  }

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public Integer getVersion() { return version; }
  public void setVersion(Integer version) { this.version = version; }

  public int getProductId() { return productId; }
  public void setProductId(int productId) { this.productId = productId; }

  public int getRecommendationId() { return recommendationId; }
  public void setRecommendationId(int recommendationId) { this.recommendationId = recommendationId; }

  public String getAuthor() { return author; }
  public void setAuthor(String author) { this.author = author; }

  public int getRate() { return rate; }
  public void setRate(int rate) { this.rate = rate; }

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }
}
