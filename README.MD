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
