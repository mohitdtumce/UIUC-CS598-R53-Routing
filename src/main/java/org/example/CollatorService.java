// package org.example;
//
// import com.google.cloud.storage.Blob;
// import com.google.cloud.storage.Bucket;
// import com.google.cloud.storage.Storage;
// import com.google.cloud.storage.StorageOptions;
// import com.google.gson.JsonObject;
// import com.google.gson.JsonParser;
// import software.amazon.awssdk.services.route53.Route53Client;
// import software.amazon.awssdk.services.route53.model.Change;
// import software.amazon.awssdk.services.route53.model.ChangeAction;
// import software.amazon.awssdk.services.route53.model.ChangeBatch;
// import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;
// import software.amazon.awssdk.services.route53.model.ResourceRecordSet;
// import software.amazon.awssdk.services.route53.model.Route53Exception;
// import software.amazon.awssdk.services.s3.S3Client;
// import software.amazon.awssdk.services.s3.model.GetObjectRequest;
// import software.amazon.awssdk.services.s3.model.S3Exception;
//
// import java.io.BufferedReader;
// import java.io.InputStreamReader;
//
// public class CollatorService {
//
//   private final Storage gcpStorage;
//   private final S3Client s3Client;
//   private final Route53Client route53Client;
//
//   public CollatorService() {
//     this.gcpStorage = StorageOptions.getDefaultInstance().getService();
//     this.s3Client = S3Client.create();
//     this.route53Client = Route53Client.create();
//   }
//
//   public String fetchDataFromS3(String bucketName, String key) {
//     try {
//       GetObjectRequest getObjectRequest = GetObjectRequest.builder()
//           .bucket(bucketName)
//           .key(key)
//           .build();
//       BufferedReader reader = new BufferedReader(new InputStreamReader(s3Client.getObject(getObjectRequest)));
//       return reader.lines().reduce("", String::concat);
//     } catch (S3Exception e) {
//       System.err.println("Error fetching data from S3: " + e.awsErrorDetails().errorMessage());
//       return null;
//     }
//   }
//
//   public String fetchDataFromGCP(String bucketName, String blobName) {
//     Blob blob = gcpStorage.get(bucketName, blobName);
//     if (blob != null) {
//       return new String(blob.getContent());
//     } else {
//       System.err.println("Error: GCP bucket or blob not found.");
//       return null;
//     }
//   }
//
//   public int calculateWeight(JsonObject s3Data, JsonObject gcpData) {
//     double s3CpuUsage = s3Data.get("cpu_usage").getAsDouble();
//     double gcpCpuUsage = gcpData.get("cpu_usage").getAsDouble();
//
//     // Example weight calculation based on inverse CPU usage
//     int s3Weight = (int) Math.max(1, 100 - s3CpuUsage);
//     int gcpWeight = (int) Math.max(1, 100 - gcpCpuUsage);
//
//     // Set maximum and minimum weight bounds as needed
//     return s3CpuUsage > gcpCpuUsage ? s3Weight : gcpWeight;
//   }
//
//   public void updateRoute53Weight(String hostedZoneId, String recordName, long weight) {
//     try {
//       ResourceRecordSet record = ResourceRecordSet.builder()
//           .name(recordName)
//           .type("A")
//           .weight(weight)
//           .setIdentifier("Primary")  // Optional: identifier if multiple records exist
//           .ttl(60L)
//           .build();
//
//       Change change = Change.builder()
//           .action(ChangeAction.UPSERT)
//           .resourceRecordSet(record)
//           .build();
//
//       ChangeBatch changeBatch = ChangeBatch.builder()
//           .changes(change)
//           .build();
//
//       ChangeResourceRecordSetsRequest request = ChangeResourceRecordSetsRequest.builder()
//           .hostedZoneId(hostedZoneId)
//           .changeBatch(changeBatch)
//           .build();
//
//       route53Client.changeResourceRecordSets(request);
//       System.out.println("Successfully updated Route53 weight to: " + weight);
//
//     } catch (Route53Exception e) {
//       System.err.println("Error updating Route53 record: " + e.awsErrorDetails().errorMessage());
//     }
//   }
//
//   public void collateAndAdjustRoute53(String s3Bucket, String s3Key, String gcpBucket, String gcpBlob, String hostedZoneId, String recordName) {
//     // Fetch data from S3
//     String s3Data = fetchDataFromS3(s3Bucket, s3Key);
//     String gcpData = fetchDataFromGCP(gcpBucket, gcpBlob);
//
//     if (s3Data != null && gcpData != null) {
//       // Parse JSON data
//       JsonObject s3Json = JsonParser.parseString(s3Data).getAsJsonObject();
//       JsonObject gcpJson = JsonParser.parseString(gcpData).getAsJsonObject();
//
//       // Calculate weight based on CPU usage
//       int weight = calculateWeight(s3Json, gcpJson);
//
//       // Update Route53 weight
//       updateRoute53Weight(hostedZoneId, recordName, weight);
//     } else {
//       System.err.println("Data retrieval failed. Cannot adjust Route53 weight.");
//     }
//   }
//
//   public static void main(String[] args) {
//     CollatorService service = new CollatorService();
//
//     // Example values (replace with actual values)
//     String s3Bucket = "your-aws-s3-bucket";
//     String s3Key = "cpu-metrics.json";
//     String gcpBucket = "your-gcp-bucket";
//     String gcpBlob = "cpu-metrics.json";
//     String hostedZoneId = "your-hosted-zone-id";
//     String recordName = "example.com";
//
//     service.collateAndAdjustRoute53(s3Bucket, s3Key, gcpBucket, gcpBlob, hostedZoneId, recordName);
//   }
// }
//
