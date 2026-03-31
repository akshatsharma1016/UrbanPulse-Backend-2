# UrbanPulse AI Services

UrbanPulse AI Services is a high-performance **Spring Boot** application that serves as the backend simulation engine for the **UrbanPulse Master Plan Mode**. It utilizes Google's Gemini LLMs to simulate and evaluate the compounding effects of long-term urban planning policies on a city's vital metrics over a 10-year trajectory (Traffic, Economy, Ecology, Sentiment).

## 🌟 Key Features
- **Deterministic Multi-Phase Simulation**: Evaluates policy impacts sequentially, meaning the output metric state of Phase 1 becomes the direct baseline for Phase 2, resulting in compounding effects rather than isolated changes.
- **Dual-Model Fallback Engine**: Built with a resilient AI service that defaults to `gemini-3.1-flash-lite-preview` for speed, but seamlessly falls back to `gemini-2.5-flash` if rate limits or availability issues occur.
- **Containerized for the Cloud**: Includes a multi-stage Dockerfile optimized for caching and minimal image sizes, capable of instant deployment on PAAS providers like Render, Heroku, or Railway.
- **Zero-Config Dynamic Ports**: Automatically binds to dynamically injected `$PORT` values from hosting platforms while defaulting to `8081` locally.
- **Strict CORS Profiles**: Pre-configured to support secure Cross-Origin Resource Sharing with production Vercel frontend deployments and local Vite development servers.

---

## 🛠 Tech Stack
- **Framework:** Spring Boot 3
- **Language:** Java 17
- **Build Tool:** Maven
- **AI Integration:** Google Gemini REST API
- **Deployment:** Docker
- **Environment Management:** `dotenv-java` for secure `.env` injection

---

## 🚀 Quick Start (Local Development)

### 1. Prerequisites
- [Java 17+](https://adoptium.net/)
- A valid [Google Gemini API Key](https://aistudio.google.com/app/apikey)

### 2. Environment Setup
Clone the repository and create an `.env` file in the root directory:

```env
# Create .env in the root directory
GEMINI_API_KEY=your_gemini_api_key_here
```

### 3. Build & Run locally
Use the included Maven wrapper to install dependencies and run the server on port `8081`:

```bash
# Build the application
./mvnw clean compile

# Run the Spring Boot server
./mvnw spring-boot:run
```

The API will now be listening for POST requests at `http://localhost:8081/api/masterplan/simulate`.

---

## 🐳 Docker Deployment (Render / Cloud PAAS)

This project is built to deploy out-of-the-box on serverless container platforms.

1. **Build the Image**
   ```bash
   docker build -t urbanpulse-backend .
   ```
2. **Run Securely (Locally)**
   Inject your local `.env` file at runtime so you don't bake secrets into the container:
   ```bash
   docker run -p 8081:8081 --env-file .env urbanpulse-backend
   ```
   
**Note on Cloud Deployments:** When deploying to platforms like Render, simply connect this GitHub repository. Make sure you manually add `GEMINI_API_KEY` to the Environment Variables settings in your Render dashboard, as your `.env` file is intentionally ignored by the `.gitignore`.

---

## 🔌 API Reference

### `POST /api/masterplan/simulate`

**Request Payload Examples:**
```json
{
  "city": "Mumbai",
  "budget": "High",
  "planName": "2036 Transformation Plan",
  "phases": [
    { "year": "2026", "policy": "add_metro", "label": "Metro Line", "icon": "🚇" },
    { "year": "2029", "policy": "remove_parking", "label": "Remove Parking", "icon": "🚫" }
  ]
}
```

**Response Payload:**
Returns a calculated trajectory over the specified years, sequential phase metrics, and a synthesized AI-generated compound insight string that contextualizes the long-term city changes.
