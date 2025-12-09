FROM node:18-alpine

# Install postgresql-client for pg_isready
RUN apk add --no-cache postgresql-client

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install all dependencies (needed for potential seeding scripts)
# Using npm install instead of npm ci to handle lock file sync issues
RUN npm install

# Copy application code
COPY . .

# Copy and set permissions for entrypoint script
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Expose port
EXPOSE 5007

# Use entrypoint script
ENTRYPOINT ["docker-entrypoint.sh"]

