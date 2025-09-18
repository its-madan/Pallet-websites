# MongoDB Atlas Support Contact Guide

## Method 1: In-App Support (Fastest)

### Step 1: Access Support from Atlas Dashboard
1. Go to [MongoDB Atlas](https://cloud.mongodb.com)
2. Sign in to your account
3. Look for the **"?" icon** or **"Support"** in the top navigation
4. Click on it to open the support menu

### Step 2: Submit Support Request
1. Click **"Submit a Request"** or **"Contact Support"**
2. Choose **"Technical Issue"** as the category
3. Select **"Database Connection Issues"** as the subcategory
4. Fill in the form with the details from `SUPPORT_REQUEST_TEMPLATE.md`

### Step 3: Priority Level
- Set priority to **"High"** or **"Critical"**
- Mention that this is **blocking development**
- Include the exact error: `javax.net.ssl.SSLException: Received fatal alert: internal_error`

## Method 2: Community Support

### MongoDB Community Forums
1. Go to [MongoDB Community](https://www.mongodb.com/community/forums/)
2. Search for "Atlas TLS internal_error" to see if others have similar issues
3. Post your issue in the **"MongoDB Atlas"** section
4. Include your cluster details and error message

### Stack Overflow
1. Search for "MongoDB Atlas TLS internal_error Java"
2. If no existing solution, post a new question
3. Tag with: `mongodb`, `mongodb-atlas`, `java`, `tls`

## Method 3: Direct Support Channels

### Email Support
- **Email**: support@mongodb.com
- **Subject**: "MongoDB Atlas TLS Configuration Issue - Cluster: pallet-builder"
- **Include**: Your cluster ID and the support request template

### Phone Support (If Available)
- Check your Atlas plan for phone support availability
- Free tier typically doesn't include phone support
- Paid plans may have dedicated support numbers

## Method 4: Social Media Support

### Twitter
- **Handle**: @MongoDB
- **Hashtag**: #MongoDBAtlas
- Tweet your issue with cluster details

### LinkedIn
- Connect with MongoDB support team members
- Message them directly with your issue

## What to Include in Your Support Request

### Essential Information
1. **Cluster Name**: pallet-builder
2. **Cluster URL**: pallet-builder.vxzvpna.mongodb.net
3. **Error Message**: `javax.net.ssl.SSLException: Received fatal alert: internal_error`
4. **Java Version**: 17.0.15
5. **MongoDB Driver**: 4.11.1

### Technical Context
- Mention you've tried multiple connection string formats
- Confirm network access is properly configured
- State that this appears to be a server-side TLS configuration issue

### Expected Outcome
- Request them to check and fix the TLS configuration for your cluster
- Ask for confirmation when the issue is resolved

## Follow-up Process
1. **Save your support ticket number**
2. **Check for updates every 24 hours**
3. **Be prepared to provide additional information if requested**
4. **Test your connection after they report the fix**

## Alternative: Escalation
If initial support doesn't resolve the issue:
1. **Request escalation** to a senior support engineer
2. **Mention the business impact** of the downtime
3. **Ask for a timeline** for resolution
4. **Consider creating a new cluster** as a temporary workaround 