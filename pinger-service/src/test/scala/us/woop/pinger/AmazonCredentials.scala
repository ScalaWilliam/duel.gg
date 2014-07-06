package us.woop.pinger

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain


trait AmazonCredentials {
  lazy val creds = new DefaultAWSCredentialsProviderChain
  lazy val accessKeyId = creds.getCredentials.getAWSAccessKeyId
  lazy val secretAccessKey = creds.getCredentials.getAWSSecretKey
}
