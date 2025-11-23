# RevGif - Reverse GIF Search Pipeline
RevGif is a mini image search pipeline for GIFs using perceptual hashing and AI. Upload an image or GIF, and RevGif finds visually similar GIFs from Tenor in real-time.

## Table of Contents
- [Overview](#overview)
- [How It Works](#how-it-works)
- [Technical Background](#technical-background)
- [Architecture](#architecture)
- [Key Design Decisions](#key-design-decisions)
- [Tech Stack](#tech-stack)
- [Setup](#setup)
- [API Documentation](#api-documentation)
- [Performance Considerations](#performance-considerations)

## Overview
RevGif is basically reverse image search but specifically for GIFs. Instead of relying on exact matches or metadata, it uses perceptual hashing to find visually similar content even when images have been modified, resized, or compressed.

### Features
- Real-time streaming results via Server-Sent Events (SSE)
- Perceptual hash-based similarity detection
- AI-powered image analysis using Google Gemini
- Smart caching to avoid redundant processing
- Rate limiting
- Support for multiple image formats (GIF, JPEG, PNG, WebP)

## How It Works

Here's what happens when you upload an image:

1. **Upload & Validation**: The system checks if your file is valid and under the size limit.

2. **Frame Extraction**:
    - Uploaded media gets split into 4 evenly-spaced frames
    - Downloaded Tenor GIFs get all their frames extracted for better accuracy
3. **Perceptual Hashing**: Each frame gets preprocessed with Gaussian blur, then converted to a 64-bit perceptual hash using DCT (Discrete Cosine Transform).
4. **Database Lookup**: We check if similar hashes already exist. If there are matches, they get streamed to you immediately.
5. **AI Analysis** (if no DB matches): Frames get sent to Google Gemini to figure out what's in the image and generate 2-3 word search queries.
6. **Tenor Search**: Use those AI-generated queries to search Tenor's library.
7. **Similarity Comparison**: Download the results, extract all their frames, hash them, and compare against your upload using normalized Hamming distance.
8. **Result Streaming**: Matching GIFs stream to you as they're found via SSE.
9. **Persistence**: Save new GIFs and their frame hashes to the database so we don't have to do this again next time.

## Technical Background
### Hamming Distance
The Hamming distance between two equal-length strings is the number of positions at which corresponding symbols differ. For binary values `a` and `b`, the Hamming distance is calculated as the population count of `a XOR b`.
For example:
- Hamming distance between `james` and `janet` = 2
- Hamming distance between identical values = 0
- For binary: `1010 XOR 1100 = 0110` (distance = 2)

### Perceptual Hashing
Perceptual hashes create a fingerprint of an image based on visual features rather than exact pixel values. Unlike cryptographic hashes, similar images produce similar perceptual hashes, which is exactly what we need.

#### Why DCT-Hash over Average Hash?
Average hash is simpler and faster, but DCT-hash is significantly better for this use case:
1. **Handles Non-Linear Transformations**: DCT-hash deals with non-linear transformations(i.e. gamma correction, histogram equalization, and other pixel-level changes) much better than average hash.
2. **Frequency-Based Analysis**: DCT separates images into frequency components. The low-frequency parts (top-left 8x8 of the DCT) contain the structural information that matters perceptually, basically what makes the image recognizable even if it's not super sharp.
3. **Better Accuracy**: While average hash is faster, DCT-hash gives you much better results when finding visually similar images that have been modified.

#### How DCT-Hash Works
1. Resize image to 32x32 (makes DCT computation easier)
2. Convert to grayscale
3. Compute the DCT (Discrete Cosine Transform) of each pixel in the image
4. Extract the top-left 8x8 DCT coefficients (these are the low frequencies)
5. Compute the median of these 64 values
6. Generate a 64-bit hash where each bit represents whether a coefficient is above the median
7. Compare hashes using normalized Hamming distance

### Gaussian Blur
Here's something important: before hashing, every frame gets a Gaussian blur with radius 3 applied. This might seem counterintuitive, but it's actually crucial:
- Smooths out high-frequency noise and compression artifacts
- Makes the algorithm less sensitive to minor pixel variations
- Focuses the hash on actual structural content rather than noise
- Massively improves matching accuracy and reduces false negatives
Perpetual hash algorithms are highly dependent on low frequencies, because low frequencies contain the overall structure of the image. It's actually how humans are able to recognize visually similar images even though they're blurry
</br>Without the blur, perceptual hashing gets thrown off by JPEG compression artifacts, subtle color shifts, and other stuff that doesn't actually matter for similarity. The blur helps the algorithm focus on what's important - the actual visual structure of the image.

## Architecture

### System Flow

```
┌─────────────┐
│   Upload    │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│   Validation    │
└──────┬──────────┘
       │
       ▼
┌─────────────────────┐
│ Frame Extraction    │
│ (5 frames max)      │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│ Perceptual Hashing  │
│ (with Gaussian blur)│
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│  Database Lookup    │
└──────┬──────────────┘
       │
       ├─── Matches Found ────► Stream to Client
       │
       └─── No Matches
              │
              ▼
       ┌──────────────┐
       │ Gemini API   │
       │ (AI Analysis)│
       └──────┬───────┘
              │
              ▼
       ┌──────────────┐
       │ Tenor Search │
       └──────┬───────┘
              │
              ▼
       ┌─────────────────────┐
       │ Download & Extract  │
       │ (All frames)        │
       └──────┬──────────────┘
              │
              ▼
       ┌─────────────────────┐
       │ Hash & Compare      │
       │ (Hamming Distance)  │
       └──────┬──────────────┘
              │
              ▼
       ┌─────────────────────┐
       │ Stream Results      │
       │ Save to Database    │
       └─────────────────────┘
```

### Components

#### Backend Services

- **UploadOrchestrator**: Coordinates the whole upload and search flow
- **ValidatorService**: Validates file format and size using Apache Tika
- **FrameExtractorService**: Extracts frames with Gaussian blur preprocessing
- **HashingService**: Generates perceptual hashes using JImageHash
- **GifQueryService**: Searches the database for matching hashes
- **GeminiImageClient**: Uses Google Gemini to analyze images and generate search queries
- **TenorGifClient**: Searches Tenor for GIFs
- **TenorGifDownloadService**: Downloads GIFs from Tenor URLs
- **GifSimilarityMatcher**: Compares perceptual hashes to find similar GIFs
- **GifCommandService**: Saves GIFs and frame hashes to the database
- **SseService**: Manages Server-Sent Events connections and streaming

#### Database Schema

**gifs table**:
```sql
CREATE TABLE gifs (
    id BIGSERIAL PRIMARY KEY,
    mime_type VARCHAR(20),
    description VARCHAR(300) NOT NULL,
    tenor_url VARCHAR(300) UNIQUE NOT NULL,
    tenor_id VARCHAR(200) NOT NULL,
    search_query VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

**frames table**:
```sql
CREATE TABLE frames (
    id BIGSERIAL PRIMARY KEY,
    p_hash BIGINT NOT NULL,
    frame_idx INT NOT NULL,
    nm_hamming_dist DOUBLE PRECISION NOT NULL,
    gifs BIGINT NOT NULL,
    FOREIGN KEY (gifs) REFERENCES gifs(id) ON DELETE CASCADE
);

CREATE INDEX idx_p_hashes ON frames(p_hash);
CREATE INDEX idx_gifs ON frames(gifs);
```

The hash comparison uses PostgresSQL's bitwise XOR and BIT_COUNT:
```sql
SELECT g.tenor_url, g.description, g.mime_type
FROM frames f
INNER JOIN gifs g ON f.gifs = g.id
WHERE BIT_COUNT((f.p_hash # :userFrameHash)::BIT(64)) / 64.0 < :threshold
ORDER BY BIT_COUNT((f.p_hash # :userFrameHash)::BIT(64)) / 64.0
LIMIT 10;
```

This query XORs the stored hash with the uploaded hash, counts the differing bits, normalizes by 64, and returns matches below the threshold. Pretty efficient.

## Key Design Decisions

### 1. Asymmetric Frame Extraction

**What I did**: Extract 4 frames from uploads but all frames from Tenor downloads.

**Why**: Getting 4 evenly spaced frames from uploads gives enough variety for Gemini to generate good search queries without burning through API credits. But when comparing downloaded GIFs, extracting all frames maximizes accuracy since it's a one-time cost per GIF. This asymmetric approach hits the sweet spot between cost and accuracy.

### 2. Synchronous Processing for Upload Phase

**What I did**: Removed async for validation, frame extraction, and hashing.

**Why**: The upload endpoint returns an SSE emitter immediately, so the user's request isn't blocked. Making validation and extraction synchronous actually simplified the code without any performance hit. The expensive things like (Gemini calls, Tenor searches, hash comparisons) stays async where it matters.

### 3. SSE Emitter Management
1. I initially started with scheduled jobs to clean up emitters
2. Then I switched to Redis keyspace events for TTL-based cleanup
3. Added manual event tracking (expected vs received counts)

**Final setup**:
- Redis stores sessions with TTL for automatic expiry
- Keyspace events trigger cleanup when sessions expire
- Manual event counting lets the emitter know exactly when to complete
- Single threaded virtual thread executor service ensures events arrive in order

**Why this matters**: Scheduled jobs were wasteful since they just poll constantly. Redis keyspace events are event-driven which is way cleaner. The manual tracking is key though - it lets the frontend know definitively when all results have arrived instead of just waiting and hoping. The executor's blocking queue guarantees FIFO ordering so events don't arrive out of sequence.

### 4. Database-First Approach
**What I did**: Check the database before calling external APIs, and only save frames that actually match.
**Why**: This cuts down API calls massively for repeated queries. If someone uploads a popular meme, chances are it's already in the database. Response time for cached queries drops to under 500ms. Plus, filtering out non-matching frames before saving keeps the database lean.

### 5. Event-Driven Architecture
**What I did**: Use Spring's event publishing for results instead of calling SSE directly.
**Why**: This decouples everything. Multiple components can publish events without knowing how SSE works. Makes error handling cleaner and the whole system more testable. The frontend gets batched events which makes loading states way easier to handle and allows for better UX

### 6. Virtual Threads
**What I did**: Used Spring's virtual thread task executor for async work.
**Why**: Virtual threads are perfect for I/O-bound stuff like API calls and database queries. Way lighter than traditional thread pools, and the code stays simple compared to reactive approaches. You get high concurrency without the complexity.

### 7. The Normalized Similarity Threshold
**What I did**: Set normalized Hamming distance threshold to 0.35.
**Why**: This means about 22-23 bits can differ out of 64 (roughly 35% dissimilarity). I tested this with a bunch of different images and 0.35 hits the right balance - finds similar GIFs without too many false positives. Combined with the Gaussian blur, it produces reliable results.

### 8. Gemini Prompt Engineering
**What I did**: Built a structured prompt with a priority system and strict output format.

**The structure**:
- Try to identify people first (uses Google Search)
- Priority: Object > Body Part > Action
- Force 2-3 word output only
- Include clear examples

**Why this matters**: The Tenor search quality depends entirely on getting good search terms from Gemini initially. The structured prompt prevents it from being too verbose and keeps output consistent. Person identification helps with reaction GIFs. The priority system focuses on the most distinctive visual elements first.

## Tech Stack

### Backend
- **Spring Boot 4.0.0RC2**: Main framework
- **Spring WebFlux**: Reactive HTTP client for Tenor API calls
- **PostgresSQL**: Primary database with bitwise operations support
- **Redis**: Session management and rate limiting
- **Apache Tika**: File type detection
- **JImageHash**: Perceptual hashing library
- **Google Gemini API**: AI-powered image analysis
- **Tenor API**: GIF search and retrieval

### Frontend
- **Vanilla JavaScript**: 
- **Server-Sent Events (SSE)**: Real-time result streaming
- **Drag-and-drop**: File upload interface

### Infrastructure
- **Docker**: Containerization (Redis)
- **Redis**: Rate limiting and sse request management

## Setup

### Prerequisites
- Java 25
- PostgresSQL 16+
- Redis 6+
- Google Gemini API key
- Tenor API key

### Environment Variables
```bash
GEMINI_API_KEY=your_gemini_api_key
TENOR_API_KEY=your_tenor_api_key
```

### Redis Setup
```bash
# Redis
docker run -d \
  --name revgif-redis \
  -p 6379:6379 \
  redis:6
```

### Configuration

Update `application.yaml` with your settings:

```yaml
spring:
  application:
    allowed-file-formats:
      - image/gif
      - image/jpeg
      - image/png
      - image/webp
    bit-resolution: 64
    expected_frames: 3 #This is actually 4 frames 
    nm-hamming-threshold: 0.35

api:
  gemini:
    api-key: ${GEMINI_API_KEY}
    model: gemini-2.5-flash
    
  tenor:
    api-key: ${TENOR_API_KEY}
    limit: 20 

rate-limit:
  req-per-minute: 5
```

### Running the Application
```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run
```

Access the application at `http://localhost:8080`

## API Documentation
### POST /upload
Upload an image or GIF to find similar content.

**Content-Type**: `multipart/form-data`

**Parameters**:
- `file`: Image or GIF file (max 5MB)

**Response**: Server-Sent Events stream

**Event Types**:

1. **BatchGifSearchCompletedEvent**
```json
{
  "completedEventList": [
    {
      "tenorUrl": "https://tenor.com/view/...",
      "description": "Funny cat reaction"
    }
  ],
  "session": "request-id"
}
```

2. **GifSearchErrorEvent**
```json
{
  "errorMessage": "Failed to analyze image",
  "errorType": "IMAGE_ANALYSIS",
  "session": "request-id",
  "occurredAt": "2024-11-22T10:30:00"
}
```

**Error Types**:
- `IMAGE_ANALYSIS`: Failed to analyze the uploaded image
- `GEMINI_SERVER_ERR`: Gemini API unavailable
- `TENOR_API_FAIL`: Tenor API unavailable
- `UNEXPECTED_ERR`: Unexpected error occurred

**Error Responses**:

400 Bad Request:
```json
{
  "errorCode": 400,
  "message": "Invalid file format. Detected: image/bmp, Allowed: [image/gif, image/jpeg, image/png, image/webp]",
  "thrownAt": "2024-11-22T10:30:00"
}
```

500 Internal Server Error:
```json
{
  "errorCode": 500,
  "message": "Failed to read the file",
  "thrownAt": "2024-11-22T10:30:00"
}
```

429 Too Many Requests:
```json
{
  "errorCode": 429,
  "message": "Too many requests. Slow down...",
  "thrownAt": "2024-11-22T10:30:00"
}
```

## Performance Considerations

### Rate Limiting
- 5 requests per minute per IP address
- Uses Redis-backed sliding window algorithm
- Prevents abuse and manages API costs

### Caching Strategy
- Database-first approach reduces redundant API calls
- Average response time for cached queries: <500ms
- Cold queries (new uploads): 30-40 seconds(depends on internet download speed as well) depending on Tenor results

### Scalability(Cuz why not)
- Virtual threads enable high concurrency without thread pool exhaustion
- Stateless design allows horizontal scaling
- Redis session management supports load balancing
- Database indexes optimize hash comparison queries

### API Cost Management
- 4 frames(could be less depending on the num of frames in the image) extracted per file upload Gemini API calls
- Database caching prevents repeated analysis
- Tenor limit of 15 results balances quality and processing time
- Failed requests include retry logic with exponential backoff

## Future Improvements

- Add support for video uploads
- Implement user accounts and search history
- WebSocket alternative to SSE for bidirectional communication


## Acknowledgments
Inspired by [TinEye](https://tineye.com/), this project demonstrates practical applications of perceptual hashing and AI-powered image analysis.
Tenor for their GIF Search API

## License
MIT License - feel free to use this project for learning or commercial purposes.