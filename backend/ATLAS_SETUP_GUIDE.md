# MongoDB Atlas Setup Guide - Fix TLS Issues

## Current Problem
Your existing MongoDB Atlas cluster has a server-side TLS configuration issue that prevents Java 17 applications from connecting.

## Solution: Create New Atlas Cluster

### Step 1: Access MongoDB Atlas
1. Go to [MongoDB Atlas](https://cloud.mongodb.com)
2. Sign in to your account
3. Click "Build a Database"

### Step 2: Create New Cluster
1. **Choose Plan**: Select "FREE" (M0 Sandbox) - sufficient for development
2. **Cloud Provider**: Choose AWS, Google Cloud, or Azure (your preference)
3. **Region**: Select a region close to your location
4. **Cluster Name**: Use "pallet-builder-new" or similar
5. Click "Create"

### Step 3: Set Up Database Access
1. Go to "Database Access" in the left sidebar
2. Click "Add New Database User"
3. **Username**: `saravanatimbersweb`
4. **Password**: Create a new strong password (different from current)
5. **Database User Privileges**: Select "Read and write to any database"
6. Click "Add User"

### Step 4: Set Up Network Access
1. Go to "Network Access" in the left sidebar
2. Click "Add IP Address"
3. For development: Click "Allow Access from Anywhere" (0.0.0.0/0)
4. For production: Add your specific IP addresses
5. Click "Confirm"

### Step 5: Get Connection String
1. Go back to "Database" in the left sidebar
2. Click "Connect" on your new cluster
3. Choose "Connect your application"
4. Copy the connection string

### Step 6: Update Your Application
1. Open `backend/src/main/resources/application.properties`
2. Replace the MongoDB URI with your new connection string
3. Update the password in the connection string

### Step 7: Test the Connection
1. Run: `.\start-working-atlas.bat`
2. Check if the application starts successfully
3. Verify connection in the logs

## Expected Connection String Format
```
mongodb+srv://saravanatimbersweb:<NEW_PASSWORD>@<NEW_CLUSTER_NAME>.<PROVIDER>.mongodb.net/?retryWrites=true&w=majority&appName=pallet-builder
```

## Troubleshooting
- If you still get TLS errors, try the alternative connection string format
- Ensure your password is properly URL-encoded
- Check that network access allows your IP address

## Migration Notes
- Export data from old cluster if needed
- Update any environment variables or deployment configurations
- Test thoroughly before deploying to production 