## MindEaseAI

Android mental wellbeing companion app with Gemini API powered tips & chat.

### API Key Configuration (Gemini)

The app reads the Gemini API key with this precedence (first non-empty wins):
1. Environment variable `GEMINI_API_KEY`
2. `local.properties` (NOT committed)
3. `gradle.properties` (discouraged for real secrets if committed)

If none are set, AI features gracefully show an error instead of crashing.

Recommended setup (pick one):

Environment variable (preferred):
```bash
export GEMINI_API_KEY="your-key"
./gradlew assembleDebug
```

local.properties:
```
GEMINI_API_KEY=your-key
```

gradle.properties (last resort, avoid committing secrets):
```
GEMINI_API_KEY=your-key
```

Never hardcode the key in source. The committed `.env` file is a template only (real key removed) and `.env` is ignored by Git.

### Verifying
Run a build and install:
```bash
./gradlew installDebug
```
Open the AI chat / tips; if configured, logs will show `API Key configured: true`.

### Troubleshooting
- API key empty: ensure one of the precedence sources has a value and rebuild (clean if needed).
- 401 errors: key invalid or wrong project permissions.
- Rate limiting (429): slow down requests.

### Security Notes
- Keys in `local.properties` are still plain text—prefer env vars in CI.
- Rotate compromised keys via Google AI Studio.

---
This README will evolve with more setup and feature docs.
