 Authentication Service API Documentation

This API provides authentication and user management functionalities for the various application. It handles 

* User registration 
*  Email/phone verification
* Login
* Profile management 
* Password operations

Base URL :

https://your_domain/api/auth

Response Structure

Json

{
  "success": true or false,
  "message": "Message",
  "data": {
    Dynamic data
  },
  "timeStamp": "time_date"
}


 Authentication

Most endpoints require authentication via a Bearer token in the `Authorization` header, obtained after successful login.

 Public Endpoints:

1. Register User (POST: `/register`)

Registers a new user in the system.

 Request Body:
Json

{
    "username":              "user_name",
    "collegeRoll":           "user_roll",
    "email":                 "user_valid_email",
    "contact":               "user_valid_contact",
    "password":              "secure_password",
    "confirmPassword":       "confirm_password",
    "role":                  "ROLE_USER or ROLE_STAFF or ROLE_ADMIN"
}

Validations:

1)username: User's name (max 20 characters)
2)collegeRoll: User’s College roll(Max 30 characters)

3)email: Valid email address (max 50 characters, must be from gmail.com, yahoo.com, outlook.com, hotmail.com, protonmail.com, or icloud.com)
4)contact: Phone number (max 10 digits)
5)password: User password (max 12 characters)



 2. Send Email Verification Link

GET:  `/verify-email-link`

Sends a verification link to the provided email address.

 Query Parameters:
 Email  : Valid email address (max 50 characters)

3. Verify Email Link

POST:  `/verify-email-link`

Verifies email using the token sent in the verification link.

Query Parameters:
 token: Verification token (max 36 characters)

 4. Send Email Verification Code
GET:  `/verify-email-code`

Sends an OTP code to the provided email address.

Query Parameters
email: Valid email address (max 50 characters)


5. Verify Email Code

POST:  `/verify-email-code`

Verifies email using the OTP code.

Query Parameters
email: Valid email address (max 50 characters)
code: OTP code (max 6 characters)

6. Send OTP to Phone

POST: `/sent-otp`

Sends an OTP to the provided phone number.

Query Parameters
contact: Phone number (max 10 digits)

7. Verify Phone OTP
POST:  `/verify-otp`

Verifies phone number using the OTP code.

Query Parameters
contact: Phone number (max 10 digits)
otp: OTP code (max 6 characters)

8. Login
POST:  `/login`

Authenticates a user and returns a token.

Request Body:

Json

{
  "email": "string",
  "password": "string"
}

email: Valid email address (max 50 characters)
password: User password (max 12 characters)

9. Get User Profile

GET:  `/profile`

Retrieves the authenticated user's profile.

Headers:

Authorization: Bearer <token>

10. Update User Profile
PUT:  `/update`

Updates the authenticated user's profile information.

Headers
Authorization: Bearer <token>

Request Body

Json

{
  "username": "string",
  "email": "string",
  "contact": "string"
}


username: User's name (max 20 characters)
email: Valid email address (max 50 characters)
contact: Phone number (max 12 characters)


11. Change Password
PUT:  `/change-password`

Changes the authenticated user's password.

Headers:
Authorization: Bearer <token>

Request Body
Json

{
  "password": "string",
  "newPassword": "string"
}

password: Current password (max 12 characters)
newPassword: New password (max 12 characters)

12. Forgot Password
POST:  `/forgot-password-email`

Initiates password reset by sending a reset link to the provided email.

Query Parameters
email: Valid email address (max 50 characters)


Error Handling:

All endpoints return errors in the following format:
Json

{
  "success": false,
  "message": "Error message",
  "data": {
    "errorCode": "ERROR_CODE"
  },
  "timeStamp": "2025-07-01T09:01:00"
}

Set .env file (Not in this project have to added extra):

Variable:

DB_HOST 
DB_PORT
DB_NAME
DB_USER 
DB_PASS 

EMAIL_USER 
EMAIL_PASS

TWILIO_SID
TWILIO_TOKEN
TWILIO_NUM



Setup Instructions:

1. Clone the repository:
bash 
git clone https://github.com/soumyadityasani/AuthService.git

2. Install dependencies:
bash
 mvn install

3. Configure environment variables (e.g., database connection, email service credentials).

4. Run the application:
bash
 mvn spring-boot:run

## Requirements
- Java 17 or higher
- Maven
- Spring Boot
- Configured email service for sending verification emails/OTPs
- Configured SMS service for sending phone OTPs

## Notes
- Ensure all requests include the `Content-Type: application/json` header where applicable.
- Token-based authentication is required for protected endpoints (`/profile`, `/update`, `/change-password`).
- Email addresses must be from supported providers (Gmail, Yahoo, Outlook, Hotmail, Protonmail, iCloud).
- All responses include a timestamp in ISO 8601 format.

For further details or issues, please create an issue on the GitHub repository.
