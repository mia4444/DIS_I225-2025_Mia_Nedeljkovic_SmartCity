package se.magnus.api.composite.incident;

import java.util.List;

public class IncidentAggregate {
  private final int productId;
  private final String name;
  private final int weight;
  private final List<DeviceSummary> recommendations;
  private final List<AlertSummary> reviews;
  private final ServiceAddresses serviceAddresses;

  public IncidentAggregate() {
    productId = 0;
    name = null;
    weight = 0;
    recommendations = null;
    reviews = null;
    serviceAddresses = null;
  }

  public IncidentAggregate(
    int productId,
    String name,
    int weight,
    List<DeviceSummary> recommendations,
    List<AlertSummary> reviews,
    ServiceAddresses serviceAddresses) {

    this.productId = productId;
    this.name = name;
    this.weight = weight;
    this.recommendations = recommendations;
    this.reviews = reviews;
    this.serviceAddresses = serviceAddresses;
  }

  public int getProductId() {
    return productId;
  }

  public String getName() {
    return name;
  }

  public int getWeight() {
    return weight;
  }

  public List<DeviceSummary> getRecommendations() {
    return recommendations;
  }

  public List<AlertSummary> getReviews() {
    return reviews;
  }

  public ServiceAddresses getServiceAddresses() {
    return serviceAddresses;
  }
}
