const express = require('express');
const bodyParser = require('body-parser');
const mysql = require('mysql');
require('dotenv').config();
const app = express();
// const multer = require('multer');
// const upload = multer({ dest: 'uploads/' });

app.use(bodyParser.json());

// connect to MySQL
const connection = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME
});

connection.connect(error => {
    if (error) throw error;
    console.log('Successfully connected to the database.');
});

app.post('/register', /* upload.single('image'), */ (req, res) => {
    console.log('Received register request with body:', req.body);
    const { username, email, password, phone_number } = req.body;
    // const imagePath = req.file.path;
    if (!username || !email || !password || !phone_number) {
        return res.status(400).send({ message: 'Missing required fields' });
    }
    const query = 'INSERT INTO users (username, email, password, phone_number /* , profile_picture */) VALUES (?, ?, ?, ? /* , ? */)';
    connection.query(query, [username, email, password, phone_number], (error, results) => {
        if (error) {
            console.error('Database query error:', error);
            res.status(500).send({ message: 'Registration failed', error: error });
        } else {
            console.log('Registration successful:', results);
            res.status(201).send({ userId: results.insertId });
        }
    });
});

app.get('/getUserData', (req, res) => {
    const userId = req.query.userId;
    console.log("Fetching data for user ID:", userId);
    const query = 'SELECT username FROM users WHERE id = ?';
    
    connection.query(query, [userId], (error, results) => {
        if (error) {
            console.error("Database query error:", error);
            res.status(500).send('Database query failed');
            return;
        }
        if (results.length > 0) {
            console.log("User found:", results[0]);
            res.json({
                username: results[0].username,
                profilePicture: results[0].profile_picture
            });
        } else {
            console.log("User not found for ID:", userId);
            res.status(404).send('User not found');
        }
    });
});

app.post('/user/interests', (req, res) => {
    if (!req.body || !Array.isArray(req.body.interests)) {
        return res.status(400).send({ message: 'Invalid or missing interests array' });
    }

    const { userId, interests } = req.body;
    const queries = interests.map(interestId => {
        return new Promise((resolve, reject) => {
            const query = 'INSERT INTO user_interests (user_id, interest_id) VALUES (?, ?)';
            connection.query(query, [userId, interestId], (error, results) => {
                if (error) {
                    reject(error);
                } else {
                    resolve(results);
                }
            });
        });
    });

    Promise.all(queries)
        .then(() => {
            res.send('Interests saved successfully');
        })
        .catch(error => {
            res.status(500).send(error);
        });
});

app.get('/getUserInterests', (req, res) => {
    const userId = req.query.userId;
    console.log("Retrieving interests for user ID:", userId);
    if (!userId) {
        console.log("Missing userId parameter");
        res.status(400).send({ message: 'Missing userId parameter' });
        return;
    }
    const query = `
        SELECT interests.name 
        FROM user_interests 
        JOIN interests ON user_interests.interest_id = interests.id 
        WHERE user_interests.user_id = ?;
    `;
    connection.query(query, [userId], (error, results) => {
        if (error) {
            console.error("Error retrieving interests:", error);
            res.status(500).send({ message: 'Error retrieving interests', error });
            return;
        }
        const interests = results.map(row => row.name);
        console.log("Interests retrieved:", interests);
        res.json({ interests });
    });
});

app.post('/login', (req, res) => {
    const { username, password } = req.body;
    console.log("Login attempt for username:", username);
    const query = 'SELECT id, username, profile_picture FROM users WHERE username = ? AND password = ?';
    
    connection.query(query, [username, password], (error, results) => {
        if (error) {
            console.error("Login error:", error);
            res.status(500).send({ message: 'Login error', error });
            return;
        }
        if (results.length > 0) {
            console.log("Login successful for user:", results[0].username);
            res.json({ success: true, userId: results[0].id, username: results[0].username, imageUrl: results[0].profile_picture });
        } else {
            console.log("User not found or password does not match for username:", username);
            res.status(404).send({ success: false, message: 'User not found or password does not match' });
        }
    });
});

const PORT = process.env.PORT;

app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}.`);
});