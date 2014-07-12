package us.woop.pinger

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain

object SystemConfiguration {
  val hazelcastGroupPassword = Option(System.getProperty("hazelcastPassword")) getOrElse (throw new IllegalStateException("hazelcastPassword missing"))
  val creds = new DefaultAWSCredentialsProviderChain
  val accessKeyId = creds.getCredentials.getAWSAccessKeyId
  val secretAccessKey = creds.getCredentials.getAWSSecretKey
  val bucketName = Option(System.getProperty("bucketName")) getOrElse (throw new IllegalStateException("bucketName missing"))
}