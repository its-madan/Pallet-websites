# MongoDB Atlas Support Request Template

## Issue Description
My Java 17 Spring Boot application cannot connect to my MongoDB Atlas cluster due to TLS/SSL handshake failures.

## Technical Details

### Error Message
```
javax.net.ssl.SSLException: Received fatal alert: internal_error
```

### Cluster Information
- **Cluster Name**: pallet-builder
- **Cluster URL**: pallet-builder.vxzvpna.mongodb.net
- **Cluster ID**: 686a9217af0ec212f8d4e165

### Application Details
- **Java Version**: 17.0.15
- **Spring Boot Version**: 3.2.0
- **MongoDB Driver**: 4.11.1
- **Connection String**: mongodb+srv://saravanatimbersweb:<password>@pallet-builder.vxzvpna.mongodb.net/?retryWrites=true&w=majority&appName=pallet-builder

### What I've Tried
1. ✅ Verified connection string format
2. ✅ Confirmed password URL encoding
3. ✅ Tested with TLS 1.2 only settings
4. ✅ Updated MongoDB driver version
5. ✅ Checked network access settings
6. ✅ Verified database user permissions

### Request
Please check the TLS configuration for my Atlas cluster. The "internal_error" alert suggests a server-side TLS configuration issue that prevents Java 17 applications from establishing secure connections.

### Expected Outcome
A properly configured Atlas cluster that accepts TLS connections from Java 17 applications.

## Contact Information
- **Account Email**: [Your Atlas account email]
- **Cluster Name**: pallet-builder
- **Priority**: High (blocking development) 