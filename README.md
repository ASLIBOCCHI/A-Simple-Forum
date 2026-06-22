# A Simple Forum 📌

A lightweight, secure, and collaborative bulletin board application (inspired by Padlet) built with a distributed architecture: a SvelteKit frontend and a Spring Boot backend. 

---

## 🏗️ Repository Architecture

The repository is structured as a multi-component stack:

```
├── backend/                  # Spring Boot API application
│   ├── src/                  # Java source code (MVC + Security)
│   ├── build.gradle          # Gradle dependencies & tasks
│   └── Dockerfile            # Container build for the JVM backend
│
├── frontend/                 # SvelteKit web client
│   ├── src/                  # Svelte components, stores, and API layer
│   ├── drizzle.config.ts     # Database migration config for Drizzle ORM
│   ├── compose.yaml          # Local MySQL development database container
│   └── Dockerfile            # Multi-stage production SvelteKit/Nginx build
│
├── backup.txt                # Raw discussion backup text file
├── report.js                 # Node.js parsing script for backup.txt
├── report.csv                # Generated spreadsheet from report.js
├── docker-compose.yml        # Orchestrates the MongoDB, Backend, and Frontend containers
└── README.md                 # Project guide (this file)
```

---

## 🛠️ Technology Stack

### Backend
* **Core:** Java 17, Spring Boot 3.2.5
* **Security:** Spring Security (stateless JWT authentication with HS256)
* **Database:** Spring Data MongoDB (persists posts, replies, and seeded users)
* **Build System:** Gradle

### Frontend
* **Core:** SvelteKit, TypeScript
* **Styling:** TailwindCSS
* **Auth & ORM:** Better Auth (minimal client) & Drizzle ORM
* **Database:** MySQL (tracks user sessions and client-side schemas)
* **Http Client:** Axios

---

## 🌟 Key Features

1. **Collaborative Boards:** Authenticated users can write posts and leave replies under other users' posts.
2. **Stateless Security:** Backend secures endpoints with JSON Web Tokens (JWT) verified on every request.
3. **Admin Moderation:** An administrative role (`ROLE_ADMIN`) is built in. Only admins can execute DELETE requests on posts and replies.
4. **Automated Seeding:** On startup, the backend automatically seeds 20 default student group accounts (`Kelompok-1` to `Kelompok-20`) and an administrator account (`adminkel2`) if they do not already exist in the database.
5. **Forum Discussion Parser:** A utility script (`report.js`) reads unstructured discussion logs from `backup.txt` and compiles them into a structured `report.csv` file.

---

## ⚙️ Configuration & Environment Variables

All critical credentials and secrets are externalized to environment variables with fallback defaults for local development.

### Backend (`backend/src/main/resources/application.yml`)
* `MONGO_URI`: MongoDB connection string. *(Default: `mongodb://root:rootpassword@localhost:27017/padlet?authSource=admin`)*
* `JWT_SECRET`: Signing key for generating backend JWTs. *(Default: `XzMgPfAmqA9ftgpP1vKQ2JECm9BZtQ7D93942Coo7iX`)*
* `ADMIN_PASSWORD`: Plaintext password assigned to the seeded `adminkel2` administrator. *(Default: `kelompok2gacorrr`)*

### Frontend (`frontend/.env` - Git Ignored)
* `DATABASE_URL`: MySQL connection URL. *(Default: `mysql://root:mysecretpassword@localhost:3306/local`)*
* `ORIGIN`: The client hosting domain. *(Default: `http://localhost:5173`)*
* `SECRET`: Secret key used by Better Auth.

---

## 🚀 Running the Project

### Method 1: The Docker Way (Recommended)
You can launch the entire stack (MongoDB database, Spring Boot backend, and SvelteKit frontend) using Docker Compose.

1. Build and run the containers:
   ```bash
   docker-compose up --build
   ```
2. The services will be accessible at:
   * **Frontend Website:** `http://localhost:5173`
   * **Backend REST API:** `http://localhost:8080`
   * **MongoDB Instance:** `mongodb://localhost:27017`

---

### Method 2: Manual Local Running (For Development)

#### 1. Setup Backend
1. Ensure you have MongoDB running locally at `localhost:27017`.
2. Navigate to the backend directory:
   ```bash
   cd backend
   ```
3. Run the application:
   ```bash
   ./gradlew bootRun
   ```

#### 2. Setup Frontend
1. Ensure you have a MySQL server running (you can use the helper container inside the frontend directory by running `docker compose up -d db` in `frontend/`).
2. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
3. Create your local `.env` configuration file:
   ```bash
   cp .env.example .env
   ```
4. Install dependencies:
   ```bash
   npm install
   ```
5. Apply database schema migrations:
   ```bash
   npx drizzle-kit push
   ```
6. Start the development server:
   ```bash
   npm run dev
   ```

---

### 📊 Utility Script: Parsing Forum Logs
To parse the discussion log backup file (`backup.txt`) and export it to `report.csv`:

1. Run the Node.js script in the root directory:
   ```bash
   node report.js
   ```
2. Check the newly populated `report.csv` spreadsheet generated in the root folder.
