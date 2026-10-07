# syntax=docker/dockerfile:1

# Node 24 ships the built-in `node:sqlite` module, so no native build tools are needed.
FROM node:24-slim

ENV NODE_ENV=production
WORKDIR /app

# Install dependencies first for better layer caching.
COPY package.json package-lock.json ./
RUN npm ci --omit=dev

# Copy the application source.
COPY src ./src
COPY scripts ./scripts

# The SQLite database is mounted at /app/database (see docker-compose.yml).
ENV DB_PATH=/app/database/library.db
ENV PORT=4000
ENV HOST=0.0.0.0

EXPOSE 4000

CMD ["node", "src/index.js"]
