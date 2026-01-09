Spring Boot MVC | Role-Based Access Control | JWT Authentication

A full-stack Bus Management and Booking System built using Java Spring Boot (MVC).
This project demonstrates real-world backend architecture, role-based access control, and a fully normalized relational database design suitable for production-grade systems.

📌 Features Overview
👥 Role-Based Access Control (RBAC)

The application supports two roles:

ADMIN

CUSTOMER (User)

For tutorial and learning purposes, users can choose which role they want to explore after authentication.

Authentication & Security

JWT-based authentication

Spring Security integration

Secure password hashing (never store raw passwords)

Role-based endpoint protection


 Admin Capabilities

Admins have full control over the system:

Manage buses

Create and manage routes

Assign stops to routes

Schedule buses

View and manage all bookings (any user, any status)

Enable or disable routes and buses

Customer Capabilities

Customers can:

Register and login

See all available bus schedules

Book seats

View booking history

Cancel bookings (based on booking status)

Getting Started (Local Setup)
1️⃣ Clone the Repository
git clone https://github.com/your-username/bus-booking-system.git
cd bus-booking-system

2️⃣ Create .env File

Create a .env file in the root directory and add the following:

# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/bus_booking_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT Configuration
JWT_SECRET=your_jwt_secret_here

# Email Configuration
EMAIL_HOST_USER=your_email@gmail.com
EMAIL_HOST_PASSWORD=your_email_app_password

# Stripe Configuration (Optional)
STRIPE_SECRET_KEY=your_stripe_secret_key
STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key


⚠ Important Security Notes

Do NOT commit .env files

Never expose real secrets in GitHub

Add .env to your .gitignore

3️⃣ Generate Your Own Secrets

JWT Secret
Generate a secure random key (minimum 256-bit).

Stripe Keys
Get keys from your Stripe Dashboard.

Email Password
Use an App Password, not your real email password.

🛠 Tech Stack

Java 17+

Spring Boot

Spring MVC

Spring Security

JWT

Hibernate / JPA

PostgreSQL

Stripe (optional)
