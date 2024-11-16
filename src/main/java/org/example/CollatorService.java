package org.example;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;
import org.example.helper.AWSConfig;
import org.example.helper.AWSCredentialHelper;
import org.example.helper.GCPConfig;
import org.example.helper.GCSHelper;
import org.example.helper.S3Helper;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.Change;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.ChangeBatch;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
import software.amazon.awssdk.services.route53.model.Route53Exception;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CollatorService implements Runnable {

  private final GCPConfig gcpConfig;

  private final GCSHelper gcsHelper;

  private final AWSConfig awsConfig;

  private final S3Helper s3Helper;

  private AtomicInteger executionCount = new AtomicInteger(0);

  public CollatorService(GCPConfig gcpConfig, AWSConfig awsConfig) throws IOException {
    this.gcpConfig = gcpConfig;
    this.gcsHelper = new GCSHelper(gcpConfig);

    this.awsConfig = awsConfig;
    AWSCredentialHelper credentialHelper = new AWSCredentialHelper(
        "/usr/local/google/home/mohitshr/.ssh/aws_access_keys.csv");
    this.s3Helper = new S3Helper(this.awsConfig, credentialHelper);
  }

  // public void updateRoute53Weight(String hostedZoneId, String recordName, long weight) {
  //   try {
  //     ResourceRecordSet record = ResourceRecordSet.builder()
  //         .name(recordName)
  //         .type("A")
  //         .weight(weight)
  //         .setIdentifier("Primary")  // Optional: identifier if multiple records exist
  //         .ttl(60L)
  //         .build();
  //
  //     Change change = Change.builder()
  //         .action(ChangeAction.UPSERT)
  //         .resourceRecordSet(record)
  //         .build();
  //
  //     ChangeBatch changeBatch = ChangeBatch.builder()
  //         .changes(change)
  //         .build();
  //
  //     ChangeResourceRecordSetsRequest request = ChangeResourceRecordSetsRequest.builder()
  //         .hostedZoneId(hostedZoneId)
  //         .changeBatch(changeBatch)
  //         .build();
  //
  //     route53Client.changeResourceRecordSets(request);
  //     System.out.println("Successfully updated Route53 weight to: " + weight);
  //
  //   } catch (Route53Exception e) {
  //     System.err.println("Error updating Route53 record: " + e.awsErrorDetails().errorMessage());
  //   }
  // }


  @Override
  public void run() {

  }

}

