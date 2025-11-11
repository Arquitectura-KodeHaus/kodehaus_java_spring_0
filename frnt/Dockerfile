# Build stage
FROM node:22-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build argument for backend URL
ARG BACKEND_URL
ENV NG_APP_BACKEND_URL=${BACKEND_URL}

# Build the application with production configuration
RUN npm run build -- --configuration production

# Production stage
FROM nginx:alpine

# Copy nginx configuration (main config, not conf.d)
COPY nginx.conf /etc/nginx/nginx.conf

# Copy built files to nginx (Angular build outputs to dist/frnt/browser)
COPY --from=build /app/dist/frnt/browser /usr/share/nginx/html

# Create health check endpoint
RUN echo '{"status":"ok"}' > /usr/share/nginx/html/health

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
