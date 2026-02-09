// Initialize MongoDB with events database
db = db.getSiblingDB('events');

// Create a user for the events database
db.createUser({
  user: 'events_user',
  pwd: 'events_password',
  roles: [
    {
      role: 'readWrite',
      db: 'events'
    }
  ]
});