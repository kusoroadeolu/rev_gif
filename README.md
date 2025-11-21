# INTRO

## Hamming Distance - A quick recap
Hamming distance between two equal length strings is the number of positions at which corresponding symbols are different
</br> The hamming distance between `james` and `janet` = 2. The hamming distance of 2 words is zero when both are identical. It also satisfies triangle inequality
</br> The hamming distance between two binary values `a` and `b` is `a ^ b` (i.e. the pop count of a and b)
</br> Hamming distance is also applied in error correction i.e. finding the k error between two given codes where `k = min hamming distance - 1` 


# RevGif - Reverse GIF Search Engine

## 🎯 Project Overview

RevGif is a sophisticated reverse image search engine that finds similar reaction GIFs based on uploaded images. Think "Google Image Search meets Tenor GIFs" - upload a screenshot or image, and the system finds matching reaction GIFs from Tenor's database using computer vision and AI.

**The Problem It Solves:** Ever had a vague idea of the reaction GIF you want but couldn't find it? RevGif analyzes your image, understands the emotion/action, and finds visually similar GIFs.

---

## 🏗️ Architecture & Technical Sophistication

### Core Technologies
- **Backend:** Spring Boot 4.x with Java 25
- **Database:** PostgreSQL (relational data) + Redis (sessions, rate limiting)
- **AI/ML:** Google Gemini API for image analysis
- **External APIs:** Tenor GIF API
- **Image Processing:** JImageHash (perceptual hashing), Apache Tika (file type detection)
- **Concurrency:** Virtual threads (Project Loom)
- **Real-time:** Server-Sent Events (SSE)

---

## How It Works: The Complete Flow

### 1. **Upload & Validation**
```
User uploads image → ValidatorService validates file type (GIF/JPEG/PNG/WebP)
→ Apache Tika detects MIME type → Reject if invalid
```
**Why it matters:** Prevents malicious uploads, ensures only processable formats.

### 2. **Frame Extraction**
```
Image → FrameExtractorService → Extract key frames from GIF/video
→ For uploads: Sample every Nth frame (configurable, default 4)
→ For static images: Single frame
```
**Technical detail:** Uses Java's ImageIO to read multi-frame GIFs, extracting representative frames to save processing costs while maintaining accuracy.

### 3. **Perceptual Hashing**
```
Each frame → HashingService → PerceptiveHash algorithm (64-bit resolution)
→ Generate hash representing visual content
```
**Why perceptual hashing?** Unlike cryptographic hashes (MD5/SHA), perceptual hashes are *similar* for *similar* images. A slight crop or color change produces a similar hash, enabling visual similarity matching.

**Algorithm:** Uses Discrete Cosine Transform (DCT) to reduce image to core frequency components, resilient to minor variations.

### 4. **Database Lookup (First Attempt)**
```
Hash → PostgreSQL query using Hamming distance
→ SELECT gifs WHERE BIT_COUNT(p_hash XOR user_hash) < threshold
→ Return matches if found
```
**Technical brilliance:** Uses PostgreSQL's bitwise operations directly in SQL for blazing-fast similarity search. The Hamming distance measures how many bits differ between hashes.

**Threshold:** 0.45 normalized Hamming distance (28.8 bits different out of 64) - tuned through experimentation to balance false positives vs. false negatives.

### 5. **AI Image Analysis (If No DB Match)**
```
Frame → Gemini AI → Analyze image content
→ Generate 2-3 word search query optimized for GIF searches
```
**Prompt Engineering Highlights:**
- Prioritizes recognizable entities (celebrities, anime characters)
- Focuses on emotions/actions over descriptions
- Uses common search terms people actually type
- Example outputs: "obama laugh", "anime blush", "visible confusion"

**Retry Logic:** 3 attempts with exponential backoff (1s → 3s delays) on API failures.

### 6. **Tenor GIF Search**
```
Search query → Tenor API → Fetch top 5 matching GIFs
→ Returns URLs, descriptions, metadata
```
**Configuration:** Content filter set to "low", media filter to "gif,mp4", limit 5 results.

### 7. **Parallel Download & Processing**
```
5 GIF URLs → Reactive WebClient → Download all concurrently
→ Extract ALL frames from each Tenor GIF
→ Hash every frame
```
**Why download all frames from Tenor GIFs?** To build a comprehensive database for future lookups. More frames = better matching accuracy.

### 8. **Similarity Matching**
```
Compare user's hash against ALL downloaded GIF frames
→ Calculate normalized Hamming distance for each
→ Keep GIFs with frames below threshold (0.45)
→ Store similar frames (not all frames) to save space
```
**Concurrency:** Uses `CompletableFuture` with virtual threads to process multiple GIFs in parallel. Each GIF's frames are compared independently.

### 9. **Database Persistence**
```
Similar GIFs → Batch insert into PostgreSQL
→ Gifs table: metadata (Tenor URL, description, search query)
→ Frames table: hash values, frame indices (foreign key to gifs)
```
**Transaction Safety:** Entire batch wrapped in `@Transactional`. If anything fails, rollback completely.

**Conflict Handling:** `ON CONFLICT(tenor_id) DO NOTHING` - prevents duplicate GIFs if multiple users search similar images.

### 10. **Real-Time Streaming**
```
As GIFs are found → Publish events → SSE stream to frontend
→ User sees results progressively, not all at once
```
**Event Types:**
- `GifSearchCompletedEvent`: Successful GIF match found
- `BatchGifSearchCompletedEvent`: Multiple matches from DB
- `GifSearchErrorEvent`: Something failed (with error type)

---

## 🛡️ Production-Grade Features

### **1. Rate Limiting (Sliding Window Algorithm)**
```java
Algorithm:
  current_minute_requests = Redis.get("ip:minute")
  previous_minute_requests = Redis.get("ip:minute-1")
  elapsed_seconds = current_time.seconds
  
  weighted_average = (current * elapsed + previous * (60 - elapsed)) / 60
  
  if weighted_average > limit:
    reject with 429
```

**Why sliding window?** Smoother than fixed windows. If you have 60 requests at 10:00:59, a fixed window would allow 60 more at 10:01:00. Sliding window considers both minutes, preventing burst abuse.

**Configuration:** 5 requests per minute per IP, keys expire after 3 minutes.

### **2. Server-Sent Events (SSE) Management**
- **Session tracking:** Cookie-based sessions stored in Redis with 3-minute TTL
- **Graceful cleanup:** On session expiry, Redis publishes event → listener closes SSE connection
- **Error handling:** Automatic cleanup on timeout, error, or completion
- **Concurrency:** Each SSE stream has dedicated virtual thread executor

**Why SSE over WebSockets?** One-way communication (server → client) is sufficient. SSE is simpler, HTTP-based, auto-reconnects, and works through proxies.

### **3. Error Handling & Resilience**

**Typed Error Events:**
```java
enum EventErrorType {
    IMAGE_ANALYSIS,      // Gemini failed to analyze image
    GEMINI_SERVER_ERR,   // Gemini API unreachable
    TENOR_API_FAIL,      // Tenor API error
    UNEXPECTED_ERR       // Catch-all
}
```
Each error includes timestamp, session ID, and context - enabling proper debugging and user-facing error messages.

**Retry Strategies:**
- **Gemini API:** `@Retryable` with 3 attempts, only on specific HTTP status codes (503)
- **Tenor API:** 3 retries with 2-second exponential backoff
- **WebClient:** 30-second timeouts, 20-second SSL handshake, 30-second read/write

**Graceful Degradation:**
- If one GIF download fails → continue with others (`onErrorResume(Mono.empty())`)
- If database save fails → publish error event but don't crash entire flow
- If Gemini analysis fails → publish specific error, don't attempt Tenor search

### **4. Concurrent Processing**
```java
// Virtual threads for each hash comparison
CompletableFuture.runAsync(() -> compareHash(...), virtualThreadExecutor)

// Wait for all comparisons before saving
CompletableFuture.allOf(futures...)
  .thenRun(() -> publishResults())
  .thenRun(() -> saveToDatabase())
  .exceptionally(e -> handleError(e))
```

**Why virtual threads?** Can spawn millions without OS thread overhead. Perfect for I/O-bound tasks like API calls, database queries.

### **5. Database Optimization**
**Indexes:**
```sql
CREATE INDEX idx_p_hashes ON frames(p_hash);  -- Fast hash lookups
CREATE INDEX idx_gifs ON frames(gifs);         -- Fast foreign key joins
```

**Bitwise Operations in SQL:**
```sql
WHERE BIT_COUNT(((p_hash # :userHash)/64)::BIT(64)) < :threshold
```
The `#` operator is XOR, `BIT_COUNT` counts differing bits. All in database, no application-side loops.

### **6. Configuration Management**
```yaml
spring.application:
  bit-resolution: 64                  # Hash bit depth
  expected_frames: 4                  # Frame sampling rate
  nm-hamming-threshold: 0.45          # Similarity threshold
  
api.gemini:
  retry-attempts: 3
  initial-delay: 1000.0               # 1 second
  max-delay: 3000.0                   # 3 seconds
  
rate-limit:
  req-per-minute: 5
  key-ttl: 3                          # minutes
```

All critical values externalized for easy tuning without code changes.

---

## 🎨 Design Patterns & Best Practices

### **1. Event-Driven Architecture**
- Services publish domain events (`GifEvent`)
- Listeners react asynchronously (`@EventListener`)
- Decouples components: download service doesn't know about SSE streaming

### **2. Repository Pattern**
- Clean separation: `Repository` (data access) vs `Service` (business logic)
- JPA/JDBC repositories for relational data
- Redis repositories for sessions

### **3. DTO Pattern**
- Entities (`GifEntity`) never leave service layer
- DTOs (`GifDTO`) exposed to controllers
- Mappers (`GifMapper`) handle conversions

### **4. Strategy Pattern**
- `ExtractionType.FROM_UPLOAD` vs `FROM_TENOR` changes frame extraction strategy
- Configurable based on context without if/else sprawl

### **5. Reactive Programming**
- WebClient with Mono/Flux for non-blocking I/O
- Backpressure handling, retry logic, error recovery

### **6. Dependency Injection**
- Constructor injection with `@RequiredArgsConstructor` (Lombok)
- Testable, immutable dependencies

---

## 🔐 Security Considerations

### **Input Validation**
- File type verification with Apache Tika (not just extensions)
- Max file size: 10MB
- MIME type whitelist: `image/gif`, `image/jpeg`, `image/png`, `image/webp`

### **Rate Limiting**
- IP-based rate limiting prevents abuse
- Redis-backed ensures distributed rate limiting (works with multiple instances)

### **Session Management**
- HTTP-only cookies (prevents XSS)
- Secure flag configurable for HTTPS
- 3-minute session TTL with auto-cleanup

### **CORS Configuration**
- Stateless sessions (no server-side session state)
- CSRF disabled (stateless API)
- Configured allowed origins

---

## 📊 Data Flow Diagram

```
┌─────────────┐
│   Upload    │
│   Image     │
└──────┬──────┘
       │
       v
┌─────────────────┐
│   Validate &    │
│ Extract Frames  │
└──────┬──────────┘
       │
       v
┌─────────────────┐
│  Hash Frames    │
│ (Perceptual)    │
└──────┬──────────┘
       │
       v
┌─────────────────┐         ┌──────────────┐
│  Check Database │────────>│  Match Found │───> Stream to User
│  (Hamming Dist) │         │  (SSE Event) │
└──────┬──────────┘         └──────────────┘
       │
       │ No Match
       v
┌─────────────────┐
│  Gemini API     │
│ Image Analysis  │
└──────┬──────────┘
       │
       v
┌─────────────────┐
│   Tenor API     │
│  GIF Search     │
└──────┬──────────┘
       │
       v
┌─────────────────┐
│  Download GIFs  │
│  (Parallel)     │
└──────┬──────────┘
       │
       v
┌─────────────────┐
│ Extract & Hash  │
│   All Frames    │
└──────┬──────────┘
       │
       v
┌─────────────────┐         ┌──────────────┐
│Compare Hashes & │────────>│ Similar GIFs │───> Stream to User
│  Match GIFs     │         │  (SSE Event) │
└──────┬──────────┘         └──────────────┘
       │
       v
┌─────────────────┐
│ Save to Database│
│ (Batch Insert)  │
└─────────────────┘
```

---

## 🧪 Testing Considerations

While test files are minimal, here's what *should* be tested:

### **Unit Tests**
- `HashingService`: Verify perceptual hashes are consistent for same image
- `FrameExtractorService`: Test frame extraction logic, sampling rates
- `ValidatorService`: File type validation edge cases
- `RateLimitFilter`: Sliding window calculations

### **Integration Tests**
- Gemini API retry logic with mocked failures
- Tenor API integration with test data
- Database operations (save, retrieve, conflict handling)
- SSE stream lifecycle (connect, emit, cleanup)

### **End-to-End Tests**
- Full upload → search → match flow
- Error scenarios (API failures, invalid uploads)
- Rate limiting under load
- Concurrent user requests

---

## 🚀 Performance Characteristics

### **Scalability**
- **Horizontal:** Stateless design supports multiple instances behind load balancer
- **Vertical:** Virtual threads maximize single-instance throughput
- **Database:** Indexed queries, bitwise operations avoid full table scans

### **Bottlenecks & Mitigations**
1. **Gemini API:** Rate limited externally → Retry with backoff, consider request queue
2. **Tenor downloads:** 5 concurrent downloads → Reactive WebClient prevents thread blocking
3. **Hash comparisons:** O(n) per user upload → Database indexing, parallel processing
4. **Database writes:** Batch inserts reduce round trips

### **Resource Usage**
- **Memory:** Minimal per request (streaming, no large buffers)
- **CPU:** Hash calculations are CPU-bound but parallelized
- **Network:** Dominated by GIF downloads (mitigated by Redis caching potential)
---

## 🎯 Production Readiness Checklist

### ✅ Already Implemented
- [x] Error handling with typed events
- [x] Rate limiting (IP-based)
- [x] Retry logic with exponential backoff
- [x] Graceful degradation
- [x] Real-time streaming (SSE)
- [x] Database indexing
- [x] Configuration externalization
- [x] Session management
- [x] Input validation
- [x] Concurrent processing
- [x] Logging (Slf4j)

## 💡 Why This Project Stands Out

### **For a Pet Project, It Demonstrates:**

1. **Production Mindset**
    - Thinks about failure scenarios upfront
    - Implements resilience patterns proactively
    - Considers user experience (progressive results via SSE)

2. **Advanced Algorithms**
    - Perceptual hashing for image similarity
    - Sliding window rate limiting
    - Hamming distance calculations in SQL

3. **Modern Java**
    - Virtual threads (Project Loom)
    - Records for DTOs
    - Reactive programming (WebFlux)

4. **Systems Thinking**
    - Event-driven architecture
    - Asynchronous processing
    - Database optimization

5. **AI Integration**
    - Practical use of LLMs (Gemini) for image analysis
    - Thoughtful prompt engineering
    - Fallback strategies

### **Comparison to Typical Pet Projects**

| Aspect | Typical Pet Project | RevGif |
|--------|---------------------|--------|
| Error Handling | `try-catch` with print | Typed events, retries, fallbacks |
| Concurrency | Synchronous or basic threads | Virtual threads, CompletableFuture chains |
| API Integration | Direct calls, hope for best | Retries, timeouts, circuit breakers |
| Real-time Updates | Polling or basic WebSockets | SSE with session management |
| Rate Limiting | None or basic counter | Sliding window with Redis |
| Database | Basic CRUD | Bitwise operations, batch inserts, indexing |
| Testing | Minimal | (Room for improvement here) |

---

## 🎓 Learning Opportunities

If you're studying this codebase, focus on:

1. **`GifSimilarityMatcher`** - See how CompletableFuture orchestrates parallel hash comparisons
2. **`RateLimitFilter`** - Study the sliding window algorithm implementation
3. **`TenorGifClient`** - Observe reactive WebClient with retry logic
4. **`SseService`** - Learn SSE lifecycle management with Redis expiry events
5. **`GifCommandServiceImpl`** - Understand batch database operations with transaction management
6. **`UploadOrchestrator`** - See event-driven workflow coordination

---

