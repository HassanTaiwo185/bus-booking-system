# Bus Booking & Management System

> A full-stack bus management platform with role-based access, seat booking, and Stripe payment integration — built with Java Spring Boot and deployed via Docker on Render.

🔗 **Live Demo:** https://bus-booking-system-kvve.onrender.com/api/v1/auth/login

> ⚠️ First load may take 30–60 seconds — the server spins down on inactivity (free tier).
> Use role selection on the login page to explore either Admin or Customer features.

---

## What it does

A production-deployed bus management system that handles the full lifecycle of bus travel — from an admin scheduling routes and buses, to a customer searching, booking, paying, and cancelling a seat.

The project was built to demonstrate real-world backend architecture: normalized relational database design, secure RBAC, payment processing, and containerized deployment — the kind of system you'd find in a real transport company's backend.

---

## Features

### Admin
- Create and manage buses (capacity, status)
- Create routes and assign stops with ordering
- Schedule buses on routes with departure times
- View and manage all bookings across all users
- Enable or disable routes and buses

### Customer
- Register, verify email, and log in
- Browse available bus schedules
- Book a seat on a schedule
- Pay for a booking via **Stripe** (test mode)
- View full booking history
- Cancel a booking (based on booking status rules)

### Security & Auth
- JWT-based stateless authentication
- Spring Security with role-based endpoint protection
- Passwords hashed — raw passwords never stored
- Secure cookie handling for HTTPS in production
- Secrets managed via environment variables — never committed

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot, Spring MVC |
| Auth & Security | Spring Security, JWT |
| Templating | Thymeleaf (server-side rendering) |
| ORM | Hibernate / JPA |
| Database | PostgreSQL |
| Payments | Stripe API |
| Email | JavaMail |
| Containerization | Docker |
| Deployment | Render (Docker container) |

---

## Architecture

```
Browser ──── HTTP ────► Spring MVC Controllers
                              │
                        Spring Security
                        (JWT filter, RBAC)
                              │
                         Service Layer
                              │
                    JPA Repositories ────► PostgreSQL
                              │
                         Stripe API (payments)
                         JavaMail (email)
```

Key design decisions:
- **MVC with Thymeleaf** — server-side rendering keeps the frontend and backend in one deployable unit, simplifying the architecture for a project focused on backend depth.
- **Normalized relational schema** — routes, stops, buses, schedules, bookings, and users are modelled as separate entities with proper foreign key relationships rather than denormalized shortcuts.
- **Role-based endpoint protection** — Spring Security intercepts every request; endpoints are explicitly mapped to `ADMIN` or `CUSTOMER` roles. No endpoint is accidentally public.
- **Docker deployment** — the entire application is containerized with a `Dockerfile` and deployed on Render, making the environment reproducible and the deployment repeatable.

---

## Local setup

### Prerequisites
- Java 17+
- Maven
- PostgreSQL
- A Stripe account (for payment features)

### 1. Clone the repo
```bash
git clone https://github.com/HassanTaiwo185/bus-booking-system
cd bus-booking-system
```

### 2. Create a `.env` file in the project root
```
# Database
DB_URL=jdbc:postgresql://localhost:5432/bus_booking_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=your-256-bit-secret-key

# Email
EMAIL_HOST_USER=your-email@gmail.com
EMAIL_HOST_PASSWORD=your-app-password

# Stripe
STRIPE_SECRET_KEY=sk_test_...
STRIPE_PUBLISHABLE_KEY=pk_test_...
```

**Getting your keys:**
- JWT secret — generate any random 256-bit string
- Stripe keys — from your [Stripe Dashboard](https://dashboard.stripe.com/apikeys) (use test keys locally)
- Email password — use a [Gmail App Password](https://myaccount.google.com/apppasswords), not your real password

### 3. Create the database
```bash
psql -U postgres -c "CREATE DATABASE bus_booking_db;"
```

### 4. Run the app
```bash
./mvnw spring-boot:run
```

App runs at `http://localhost:8080`

---

## Running with Docker

```bash
docker build -t bus-booking-system .
docker run -p 8080:8080 --env-file .env bus-booking-system
```

Or with Docker Compose (includes PostgreSQL):
```bash
docker-compose up
```

---

## Exploring the live demo

The login page lets you choose a role — Admin or Customer — for demonstration purposes.

**To test the customer flow:**
1. Register a new account
2. Verify your email
3. Browse available schedules
4. Book a seat and pay using Stripe test card `4242 4242 4242 4242` (any future date, any CVV)
5. View your booking history and try cancelling

**To test the admin flow:**
1. Select Admin on the login page
2. Create a bus, then a route with stops
3. Schedule the bus on the route
4. View all customer bookings

---

## Project structure

```
src/
└── main/
    ├── java/
    │   └── com/bus/
    │       ├── auth/           # JWT filter, Spring Security config
    │       ├── booking/        # Booking service, controller, entity
    │       ├── bus/            # Bus management
    │       ├── route/          # Route + stop management
    │       ├── schedule/       # Schedule management
    │       ├── payment/        # Stripe integration
    │       └── user/           # User entity, registration, roles
    └── resources/
        └── templates/          # Thymeleaf HTML templates
```

---

## Security notes

- `.env` is excluded from version control via `.gitignore`
- JWT tokens are validated on every protected request via a Spring Security filter
- Stripe is configured in test mode locally — no real charges are made
- Cookie `Secure` flag is enforced in the production (HTTPS) environment
