db = db.getSiblingDB('script_master');

db.createCollection('script');

db.createUser({
    user: 'admin',
    pwd: '0Bn4A48^3JChT16*IfLP7zi0FGHfQ^j8',
    roles: [{ role: 'readWrite', db: 'script_master' }]
});