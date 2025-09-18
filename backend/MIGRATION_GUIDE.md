# MongoDB Atlas Migration Guide

## Current Issue
Your current MongoDB Atlas cluster has a server-side TLS configuration issue that prevents Java 17 applications from connecting.

## Solution: Create New Atlas Cluster

### Step 1: Create New MongoDB Atlas Cluster
1. Go to MongoDB Atlas dashboard
2. Create a new cluster (M0 Free tier is sufficient for testing)
3. Choose the same region as your current cluster
4. Use the same username: `saravanatimbersweb`
5. Set a new password (different from current one)

### Step 2: Update Connection String
Replace the connection string in `application.properties` with the new cluster's connection string.

### Step 3: Test Connection
Use the existing startup script: `start-working-atlas.bat`

## Alternative: Contact MongoDB Atlas Support
If you need to keep the current cluster, contact MongoDB Atlas support with:
- Cluster ID: `pallet-builder.vxzvpna.mongodb.net`
- Error: `javax.net.ssl.SSLException: Received fatal alert: internal_error`
- Request: Check and fix TLS configuration for Java 17 compatibility

## Working Configuration
Your current application configuration is correct:
- Java 17 ✅
- Spring Boot 3.2.0 ✅
- MongoDB Driver 4.11.1 ✅
- TLS 1.2 settings ✅
- Connection string format ✅

The issue is specifically with your Atlas cluster's TLS configuration. 