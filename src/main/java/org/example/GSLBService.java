package org.example;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.RandomStringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.Change;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.ChangeBatch;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.CreateHostedZoneRequest;
import software.amazon.awssdk.services.route53.model.CreateHostedZoneResponse;
import software.amazon.awssdk.services.route53.model.GeoLocation;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.ListHostedZonesRequest;
import software.amazon.awssdk.services.route53.model.ListHostedZonesResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import software.amazon.awssdk.services.route53.model.Route53Exception;

public class GSLBService {

  static AwsCredentialsProvider createCredentialsProviderFromCSV(String filePath)
      throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      reader.readLine();
      // Skip the first line (header)
      String line = reader.readLine(); // Read the second line with credentials
      String[] parts = line.split(","); // Assuming comma-separated values
      if (parts.length == 2) {
        String accessKeyId = parts[0].trim();
        String secretAccessKey = parts[1].trim();
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        );
      } else {
        throw new IllegalArgumentException("Invalid credentials format in CSV file.");
      }
    }
  }

  static List<String> listHostedZoneId(Route53Client route53Client) {
    ListHostedZonesRequest request = ListHostedZonesRequest.builder().build();
    ListHostedZonesResponse response = route53Client.listHostedZones(request);

    List<String> zones = new ArrayList<>();
    for (HostedZone zone : response.hostedZones()) {
      System.out.println(zone.toString());
      zones.add(zone.toString());
    }
    return zones;
  }

  static HostedZone findHostedZone(Route53Client route53Client, String domain) {
    ListHostedZonesResponse response = route53Client.listHostedZones(
        ListHostedZonesRequest.builder().build());

    for (HostedZone zone : response.hostedZones()) {
      System.out.println(zone.toString());
      if (Objects.equals(zone.name(), domain)) {
        return zone;
      }
    }
    return null;
  }

  static HostedZone createHostedZone(Route53Client route53Client, String domainName) {
    try {
      CreateHostedZoneRequest request = CreateHostedZoneRequest.builder()
          .name(domainName)
          .callerReference(new RandomStringUtils().nextAlphanumeric(10))
          .build();

      CreateHostedZoneResponse response = route53Client.createHostedZone(request);

      // Access hosted zone details from the response
      System.out.println("Hosted Zone Name: " + response.hostedZone().name());
      System.out.println("Hosted Zone ID: " + response.hostedZone().id());
      return response.hostedZone();
    } catch (Route53Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  static ChangeResourceRecordSetsResponse createCNAMERecord(Route53Client route53Client,
      String hostedZoneId, String geoSubdomain, String weightedSubdomain) {
    ResourceRecordSet cnameRecordSet = ResourceRecordSet.builder()
        .name(geoSubdomain)  // Subdomain: us-east-1.ucd.cs598.sharma83
        .type(RRType.CNAME)
        .setIdentifier(geoSubdomain)
        .geoLocation(GeoLocation.builder().continentCode("NA").build()) // Geolocation settings
        .ttl(300L)
        .resourceRecords(ResourceRecord.builder().value(weightedSubdomain).build())
        .build();
    return changeResourceRecordSets(route53Client, hostedZoneId, cnameRecordSet);
  }

  static void createWeightedARecords(Route53Client route53Client, String hostedZoneId,
      String subdomain, List<WeightedRecord> records) {
    for (WeightedRecord record : records) {
      var aRecordSet = ResourceRecordSet.builder()
          .name(subdomain)
          .type(RRType.A)
          .setIdentifier(record.identifier)
          .weight(record.weight)
          .ttl(300L)
          .resourceRecords(
              ResourceRecord.builder().value(record.ipv4).build())
          .build();
      changeResourceRecordSets(route53Client, hostedZoneId, aRecordSet);
    }
  }

  private static ChangeResourceRecordSetsResponse changeResourceRecordSets(
      Route53Client route53Client, String hostedZoneId,
      ResourceRecordSet recordSet) {
    ChangeResourceRecordSetsRequest request = ChangeResourceRecordSetsRequest.builder()
        .hostedZoneId(hostedZoneId)
        .changeBatch(ChangeBatch.builder()
            .changes(Change.builder()
                .action(ChangeAction.CREATE)
                .resourceRecordSet(recordSet)
                .build())
            .build())
        .build();
    try {
      return route53Client.changeResourceRecordSets(request);
    } catch (Route53Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  static class WeightedRecord {

    String identifier;
    String ipv4;
    Long weight;

    public WeightedRecord(String identifier, String ipv4, Long weight) {
      this.identifier = identifier;
      this.ipv4 = ipv4;
      this.weight = weight;
    }
  }
}
