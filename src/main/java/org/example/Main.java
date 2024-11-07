package org.example;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.example.GSLBService.WeightedRecord;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.HostedZone;

public class Main {

  public static void main(String[] args) throws IOException {
    System.out.println("Hello world!");

    // Create an AwsCredentialsProvider that reads from the file
    AwsCredentialsProvider credentialsProvider = GSLBService.createCredentialsProviderFromCSV(
        "/usr/local/google/home/mohitshr/.ssh/aws_access_keys.csv");

    // Create Route 53 client
    Route53Client route53Client = Route53Client.builder()
        .credentialsProvider(credentialsProvider)
        .region(Region.US_EAST_1) // Replace with your desired region
        .endpointOverride(URI.create("https://route53.amazonaws.com")) // Optional endpoint override
        .build();

    // Get the hosted zone ID
    String domain = "ucd.cs598.sharma83.";
    HostedZone zone = GSLBService.findHostedZone(route53Client, domain);
    System.out.println("Found hosted zone:" + zone);

    HostedZone zone1 = GSLBService.createHostedZone(route53Client, domain);
    assert zone1 != null;
    System.out.println("Found hosted zone:" + zone1);

    String geoSubdomain = String.format("us-east-1.%s", domain);
    String weightedSubdomain = String.format("weighted.%s", geoSubdomain);
    System.out.println(
        GSLBService.createCNAMERecord(route53Client, zone1.id(), geoSubdomain, weightedSubdomain));

    List<WeightedRecord> records = new ArrayList<>();
    records.add(new WeightedRecord(
        String.format("aws.%s", weightedSubdomain),
        "127.0.0.1",
        20L
    ));
    records.add(new WeightedRecord(
        String.format("azure.%s", weightedSubdomain),
        "127.0.0.2",
        15L
    ));
    records.add(new WeightedRecord(
        String.format("gcp.%s", weightedSubdomain),
        "127.0.0.3",
        15L
    ));
    GSLBService.createWeightedARecords(route53Client, zone1.id(), weightedSubdomain, records);
  }
}