package org.example.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.model.GeoLocation;

public class AWSConfig {
  public final Region region;
  public final String bucketName;
  public final String filePath;

  public final String domain;

  public AWSConfig(String region, String bucketName, String filePath, String domain) {
    this.region = software.amazon.awssdk.regions.Region.of(region);
    this.bucketName = bucketName;
    this.filePath = filePath;
    this.domain = domain;
  }
}
