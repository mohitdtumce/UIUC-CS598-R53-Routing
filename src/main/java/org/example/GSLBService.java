package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.R53Helper;
import org.example.R53Helper.WeightedRecord;
import org.example.helper.AWSConfig;
import org.example.helper.AWSCredentialHelper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.model.GeoLocation;
import software.amazon.awssdk.services.route53.model.HostedZone;

public class GSLBService {

  private final AWSConfig config;

  private final AWSCredentialHelper credentialHelper;
  private final R53Helper helper;

  private final Map<GeoLocation, List<Region>> geoMapping = Map.of(
      GeoLocation.builder().countryCode("US").build(), List.of(Region.US_EAST_1, Region.US_EAST_2),
      GeoLocation.builder().countryCode("IN").build(), List.of(Region.AP_NORTHEAST_1, Region.AP_NORTHEAST_2)
  );

  GSLBService(AWSConfig config, AWSCredentialHelper credentialHelper) {
    this.config = config;
    this.credentialHelper = credentialHelper;
    this.helper = new R53Helper(config, credentialHelper);
  }

  public void setupGSLB() throws IOException {
    // Get the hosted zone ID

    HostedZone zone = helper.findOrCreateHostedZone(this.config.domain);
    System.out.println("Found hosted zone:" + zone);

    for (Map.Entry<GeoLocation, List<Region>> entry : geoMapping.entrySet()) {
      for (Region region : entry.getValue()) {
        String geoSubdomain = String.format("%s.%s", region, this.config.domain);

        String weightedSubdomain = String.format("weighted.%s", geoSubdomain);
        System.out.println(
            helper.createCNAMERecord(zone.id(), geoSubdomain, weightedSubdomain, entry.getKey()));

        List<WeightedRecord> records = new ArrayList<>();

        records.add(createWeightedRecord("aws", region, weightedSubdomain));
        records.add(createWeightedRecord("azure", region, weightedSubdomain));
        records.add(createWeightedRecord("gcp", region, weightedSubdomain));
        helper.createWeightedARecords(zone.id(), weightedSubdomain, records);
      }
    }
  }

  private R53Helper.WeightedRecord createWeightedRecord(String provider, Region region,
      String weightedSubdomain) {
    return switch (provider) {
      case "aws" -> new WeightedRecord(
          String.format("%s.%s", provider, weightedSubdomain).trim(),
          String.format("10.0.0.%d", Utils.consistentHash(provider + region)),
          20L
      );
      case "gcp" -> new WeightedRecord(
          String.format("%s.%s", provider, weightedSubdomain).trim(),
          String.format("172.16.0.%d", Utils.consistentHash(provider + region)),
          15L
      );
      default -> new WeightedRecord(
          String.format("%s.%s", provider, weightedSubdomain).trim(),
          String.format("192.168.0.%d", Utils.consistentHash(provider + region)),
          10L
      );
    };
  }
}
