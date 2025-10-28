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

# Build the application (Angular outputs to dist/frnt/browser by default)
RUN npm run build

# Production stage
FROM nginx:alpine

# Copy built files to nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Health check endpoint
RUN echo '{"status":"ok"}' > /usr/share/nginx/html/health

# Expose port
EXPOSE 8080

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
