FROM node:20-alpine

WORKDIR /app

# Install dependencies first (cached)
COPY package*.json ./
RUN npm config set registry https://registry.npmmirror.com
RUN npm install

# Copy source code (will be overridden by volume in dev, but good for standalone)
COPY . .

# Expose port (matched with vite.config.js)
EXPOSE 3000

# Start command
CMD ["npm", "run", "dev"]
