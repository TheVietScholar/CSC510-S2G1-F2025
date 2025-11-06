# BoozeBuddies TODO List

## ID Verification Service Integration

**Priority: Medium**  
**Status: Pending**

### Description
Currently, age verification is done via simple date-of-birth validation. For production and compliance purposes, integrate with a professional ID verification service for automated document scanning and verification.

### Requirements
- Integrate with third-party ID verification service (e.g., Veriff, Jumio, Onfido, ID.me)
- Implement document scanning and photo capture functionality
- Verify government-issued ID documents (driver's license, passport, ID card)
- Extract and validate date of birth from ID documents
- Store verification status securely
- Implement fallback verification methods for edge cases

### Implementation Notes
- Location: `UserSettings.jsx` - currently has manual verification button
- Backend endpoint: `POST /api/users/{id}/verify-age` needs enhancement
- Consider adding separate endpoint for ID document upload/verification
- Store verification metadata (verification date, document type, verification provider, etc.)
- Implement audit logging for compliance

### Recommended Services
1. **Veriff** - https://www.veriff.com/
   - Real-time ID verification
   - Supports 200+ countries
   - Good API documentation
   
2. **Jumio** - https://www.jumio.com/
   - Enterprise-grade solution
   - Strong compliance features
   - Mobile SDK support

3. **Onfido** - https://www.onfido.com/
   - AI-powered verification
   - Easy integration
   - Good developer experience

4. **ID.me** - https://www.id.me/
   - US-focused
   - Government partnership
   - Strong for US markets

### Related Files
- Frontend: `proj2/frontend/src/components/UserSettings.jsx`
- Backend: `proj2/src/main/java/com/boozebuddies/controller/UserController.java` (line 133-169)
- Backend: `proj2/src/main/java/com/boozebuddies/service/implementation/ValidationServiceImpl.java`

