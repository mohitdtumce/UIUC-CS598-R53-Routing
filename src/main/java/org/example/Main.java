package org.example;

import java.io.IOException;
import org.example.R53Helper.WeightedRecord;
import org.example.helper.AWSConfig;
import org.example.helper.AWSCredentialHelper;
import software.amazon.awssdk.regions.Region;

public class Main {
  public static void main(String[] args) throws IOException {
    AWSConfig awsConfig = new AWSConfig("us-east-1",
        "mohitshr-project-gslb-us-central1","InstanceHealth.csv", "uiuc.cs598.sharma83.");
    AWSCredentialHelper credentialHelper = new AWSCredentialHelper(
        "/usr/local/google/home/mohitshr/.ssh/aws_access_keys.csv");

    // GSLBService gslbService = new GSLBService(awsConfig, credentialHelper);
    // gslbService.setupGSLB();
  }
}