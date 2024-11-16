package org.example.helper;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.example.helper.AWSConfig;
import org.example.helper.AWSCredentialHelper;
import org.example.helper.InstanceDataReader.InstanceData;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3Helper {

  private final S3Client s3Client;
  private final AWSConfig config;
  private final AWSCredentialHelper credentialHelper;

  public S3Helper(AWSConfig config, AWSCredentialHelper credentialHelper) throws IOException {
    this.config = config;
    this.credentialHelper = credentialHelper;
    this.s3Client = S3Client.builder()
        .credentialsProvider(this.credentialHelper.getAwsCredentialProvider())
        .region(this.config.region).build();
  }

  /**
   * Checks if a bucket exists.
   */
  private static boolean bucketExists(S3Client s3, String bucketName) {
    try {
      s3.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
      return true;
    } catch (NoSuchBucketException e) {
      return false;
    } catch (S3Exception e) {
      System.err.println(
          "Error occurred while checking bucket: " + e.awsErrorDetails().errorMessage());
      return false;
    }
  }

  public void listBuckets() {
    try {
      ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
      System.out.println("Buckets:");
      listBucketsResponse.buckets().forEach(bucket -> System.out.println(bucket.name()));
      System.out.println("Listed all S3 buckets.");
    } catch (S3Exception e) {
      System.err.println(
          "Error occurred while listing buckets: " + e.awsErrorDetails().errorMessage());
    }
  }

  // Read AWS Instances CSV
  private List<InstanceData> readS3Instances() throws IOException, CsvValidationException {
    List<InstanceData> instances = new ArrayList<>();
    Path tempFile = Files.createTempFile("aws-instance-data", ".csv");
    s3Client.getObject(GetObjectRequest.builder()
        .bucket(this.config.bucketName)
        .key(this.config.filePath).build(), tempFile);

    try (CSVReader reader = new CSVReader(new FileReader(tempFile.toFile()))) {
      String[] nextLine;
      while ((nextLine = reader.readNext()) != null) {
        // Read and store the data from CSV (specific to AWS CSV structure)
        String instanceId = nextLine[0];
        String availabilityZone = nextLine[1];
        String state = nextLine[2];
        double cpuUtilization = Double.parseDouble(nextLine[3]);
        double networkIn = Double.parseDouble(nextLine[4]);
        double networkOut = Double.parseDouble(nextLine[5]);
        double diskReadBytes = Double.parseDouble(nextLine[6]);
        double diskWriteBytes = Double.parseDouble(nextLine[7]);

        instances.add(new InstanceData(instanceId, availabilityZone, state, cpuUtilization, networkIn,
            networkOut, diskReadBytes, diskWriteBytes));
      }
    }
    return instances;
  }
}
