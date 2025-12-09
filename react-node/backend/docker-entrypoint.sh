#!/bin/sh
set -e

echo "Waiting for database to be ready..."
until pg_isready -h postgres -U postgres -d school_mgmt; do
  sleep 1
done

echo "Database is ready!"

# Setup admin password if not already set
echo "Setting up admin password..."
node src/scripts/setup-admin-password.js

# Start the application
echo "Starting backend server..."
exec npm start

