FROM node:20-alpine

WORKDIR /app

# Install dependencies first (layer cache)
COPY package.json package-lock.json ./
RUN npm ci --omit=dev

# Copy application code
COPY src/ ./src/
COPY migrations/ ./migrations/

# Run migrations on startup, then start server
CMD ["sh", "-c", "node src/db/migrate.js && node src/server.js"]
