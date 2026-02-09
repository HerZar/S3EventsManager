# MongoDB 8.2.4 Dockerfile
FROM mongo:8.2.4

# Expose MongoDB on port 27028
EXPOSE 27028

# Create data directory for volume
RUN mkdir -p /data/db

# Copy initialization script
COPY init-mongo.js /docker-entrypoint-initdb.d/init-mongo.js

# Set the default port to 27028
CMD ["mongod", "--port", "27028", "--dbpath", "/data/db"]
