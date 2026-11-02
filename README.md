# otp-verification-system 
# OTP Verification System

A full-stack OTP (One-Time Password) verification application built with Java Spring Boot.

## Features

- 6-digit OTP generation using SecureRandom
- OTP hashing with HMAC-SHA256
- OTP expiration after 5 minutes
- Previous active OTP invalidation
- Email delivery using Gmail SMTP
- OTP verification through REST API
- PostgreSQL database integration
- Frontend and backend integration

## Technologies

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- Spring Mail
- Maven
- HTML
- CSS
- JavaScript

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/otp/generate` | Generate and send an OTP |
| POST | `/api/otp/verify` | Verify an OTP |

## Configuration

Before running the application, configure these environment variables:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MAIL_USERNAME`
- `MAIL_APP_PASSWORD`
- `OTP_HMAC_SECRET`

Never commit passwords, email app passwords, or secret keys to the repository.

## Running Locally

1. Clone this repository.
2. Configure the required environment variables.
3. Create a PostgreSQL database.
4. Start the Spring Boot application.
5. Open `http://localhost:8080` in your browser.

## Project Status

Initial version — under development.
