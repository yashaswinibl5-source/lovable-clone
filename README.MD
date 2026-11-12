[User Prompt]
│
▼
1. Spring Boot Backend: Packages system prompt + existing file tree + user prompt
   │
   ▼
2. LLM (Claude / OpenAI / Gemini): Streams response with special tags:
   <file path="src/App.tsx">
   export default function App() { ... }
   </file>
   │
   ▼
3. Server-Sent Events (SSE): Spring Boot streams tokens live to the frontend
   │
   ▼
4. Frontend Stream Parser: Catches <file> tags in real-time & updates a virtual file tree in memory
   │
   ▼
5. In-Browser Sandbox (Sandpack / WebContainer): An <iframe> receives the files and runs Vite/React in WebAssembly!

There are two major parts to build:

Backend (Spring Boot): Handles project storage, context management, LLM streaming via SSE, and version snapshots.
Frontend (React + Vite + Tailwind): Chat UI, Monaco code editor, live file tree, and the in-browser preview sandbox.

Step 2: Understanding & Building the Streaming Engine (SSE)
In traditional REST APIs, the client sends a request and waits 15–30 seconds for the server to finish before receiving the response.

For an AI app builder like Lovable, that won't work—the user needs to see the code streaming in real time, token by token, file by file.

We achieve this using Server-Sent Events (SSE) via Spring's SseEmitter.
Browser (Client)                           Spring Boot Backend
│                                            │
│─── GET /api/v1/generator/stream?prompt ───>│ (Keeps HTTP connection open)
│<── event: chunk ("<file path...") ────────│ (Sent immediately)
│<── event: chunk ("import React...") ──────│ (Sent token by token)
│<── event: complete ───────────────────────│ (Connection closed)




Stage1 -> creating entity [user, project, projectmemeber etc]
    -> rest endpoints [repositories, servoce and cpntroller]
   -> dto's , mappper, lambda expression ,stream , orelseget(), transactional, builder, lombok, validation, response entity

stage 2-> Create custom domain exceptions (ResourceNotFoundException, BadRequestException).
Build a Global Exception Handler (@RestControllerAdvice and @ExceptionHandler) so APIs always return clean, standardized JSON errors (404, 400).
Add Spring Security and configure our security filter chain with a BCryptPasswordEncoder.

1. Client sends email & password ──> POST /api/v1/auth/login
2. Spring Boot verifies password ──> Generates a signed JWT String (Token)
3. Client stores token
4. On future requests:
   Client sends: Authorization: Bearer <TOKEN>
   JwtFilter inspects token ───────> Identifies who is making the request! 

# Core Concepts Covered in This Backend

## 1. Layered backend architecture
This project is built using a common Spring Boot structure:
- Controller: receives HTTP requests and returns HTTP responses
- Service: contains the business logic
- Repository: handles database access
- Entity: represents the table structure in the database
- DTO: represents the JSON contract sent to the client

This keeps code organized and avoids leaking database internals into API responses.

## 2. Annotations you will keep seeing
These are the most important annotations used across the project:
- @RestController: marks a class as a REST API controller
- @RequestMapping: sets the base API path for a controller
- @GetMapping / @PostMapping / @PutMapping / @DeleteMapping: binds HTTP methods to methods
- @RequestBody: converts incoming JSON into a Java object
- @Valid: triggers validation rules on a DTO
- @PathVariable: reads values from the URL path
- @RequestParam: reads values from query parameters
- @Service: marks business logic classes
- @Repository: marks data-access classes
- @Entity: marks a JPA entity
- @Configuration: marks configuration classes
- @Bean: defines a Spring bean
- @Component: general-purpose bean registration
- @RequiredArgsConstructor: generates constructor injection for final fields
- @Transactional: wraps logic in a database transaction
- @ExceptionHandler: handles a specific exception type
- @RestControllerAdvice: centralizes global exception handling
- @EnableMethodSecurity: enables @PreAuthorize checks
- @PreAuthorize: restricts method access based on permission logic

## 3. DTOs vs Entities
A DTO is used for API requests/responses.
An Entity is used for JPA and database storage.

Example:
- SignupRequest DTO -> user enters name/email/password
- User entity -> actual JPA model persisted in the database
- AuthResponse DTO -> returned after login/signup with token and user details

## 4. Validation
Validation ensures the request is valid before business logic runs.
- @NotBlank checks empty strings
- @Email checks proper email format
- @Size checks string length
- @Valid makes Spring validate the incoming object automatically

If validation fails, the app responds with a 400 Bad Request response.

## 5. Exception handling
Instead of letting random runtime errors leak to clients, the project uses a global exception handler.
This is done with:
- custom exceptions like BadRequestException and ResourceNotFoundException
- @RestControllerAdvice
- @ExceptionHandler

This ensures all API errors return a clean JSON response with status, message, and path.

## 6. Spring Security and JWT
This project uses JWT-based authentication.
The flow is:
1. User signs up or logs in
2. Backend checks password using BCrypt
3. Backend creates JWT token
4. Client sends Bearer token in the Authorization header
5. JwtAuthenticationFilter validates the token
6. If valid, the user is considered authenticated for that request

## 7. Authorization and RBAC
Authorization decides what the user is allowed to do.
This project uses role-based access control:
- OWNER
- EDITOR
- VIEWER

The ProjectSecurity bean checks whether the logged-in user has the right permission.
This is enforced with @PreAuthorize.

## 8. Password hashing
Passwords are never saved in plain text.
The application uses BCryptPasswordEncoder to hash and compare passwords safely.

## 9. JPA and repositories
Repositories handle database work. Spring Data JPA automatically creates implementations for common methods such as:
- findByEmail()
- findById()
- existsByEmail()
- findByProjectIdAndUserId()

This reduces manual SQL and speeds up backend development.

## 10. Why MapStruct is used
MapStruct converts one object format to another.
Example:
- CreateProjectRequest -> ProjectEntity
- ProjectEntity -> ProjectResponse

This reduces boilerplate code and keeps entity conversion logic centralized.

## 11. Why Lombok is used
Lombok generates common methods automatically, such as:
- getters/setters
- equals/hashCode
- toString
- constructors

This reduces noise in code and keeps Java classes cleaner.

## 12. End-to-end mental model
The full flow looks like this:
- Client sends request to controller
- Controller validates request body
- Service applies business logic
- Repository reads or writes data
- DTO is returned to client
- Security filter checks JWT for protected endpoints
- @PreAuthorize checks if the user is allowed
- GlobalExceptionHandler returns consistent JSON errors on failure

This is the standard architecture for a production-style Spring Boot backend.

## 13. What this project has covered so far
So far, this project has covered:
- Spring Boot app setup
- JPA and PostgreSQL integration
- DTOs and validation
- repository/service/controller layering
- custom exceptions and error handling
- BCrypt password hashing
- JWT generation and validation
- authenticated requests
- method-level security
- role-based access control

Those are core backend concepts used in real-world SaaS and enterprise applications.

## 15. Stripe webhooks and why they matter
Webhooks solve a very important problem in payment systems:
- the user may disconnect before redirecting back to the app
- the browser might never hit the /success page
- a malicious user could manually call the success endpoint without paying

Stripe solves this by sending an automatic POST request to your app when a payment succeeds.

The flow is:
1. User completes payment on Stripe's hosted checkout page
2. Stripe sends an event to /api/v1/stripe/webhook
3. Your controller verifies the Stripe signature
4. Your service updates the user's subscription tier in PostgreSQL
5. The database is updated even if the browser never returns to the app

New concepts here:
- webhook: server-to-server event notification
- HMAC signature verification: proves the request actually came from Stripe
- @RequestHeader: reads request headers such as Stripe-Signature
- event type matching: we inspect the passed Stripe event and handle only specific ones

This is the secure way to grant PRO access after payment without relying on a browser redirect alone.

## 16. AI chat generation, memory, and quotas
The project also includes an AI generation flow tied to each project.

Core ideas:
- every project has a chat history table
- user prompts and AI responses are saved in PostgreSQL
- the AI can remember the recent context for the project
- free users have a limited daily generation quota
- editors/owners can generate code, members may read history

New concepts:
- SSE (Server-Sent Events) for streaming AI output chunk-by-chunk
- project-scoped AI prompts
- multi-turn conversation context
- quota enforcement with subscription tier checks
- @PreAuthorize on AI endpoints to ensure access control

This is how a real Lovable-style app mixes production AI generation with project ownership and limits.

## 14. Stripe Checkout and hosted payment flow
Stripe Checkout is the safest way to accept payments without handling raw card numbers in your backend.

The flow is:
1. User clicks Upgrade to PRO
2. Frontend calls the backend checkout endpoint
3. Backend creates a Stripe Checkout Session
4. Stripe returns a secure checkout URL
5. User pays on Stripe's hosted page
6. Stripe redirects back to our success/cancel endpoints

New concepts introduced:
- @Value: reads configuration values from application.properties
- @PostConstruct: runs once after Spring creates the bean
- Checkout Session: Stripe's secure payment session object
- success-url / cancel-url: where Stripe redirects the browser after checkout
- hosted checkout: user enters card details on Stripe's domain, not on ours

This is exactly how production SaaS apps handle billing securely while staying compliant.

