# 🎬 CineMatch — Smart Movie Recommendation System

> Hollywood + Bollywood · Collaborative Filtering · Cosine Similarity · 130 Movies

A full-stack Java-based movie recommendation engine with a beautiful offline HTML/CSS/JS frontend. No API keys needed — everything runs locally.

---

## 📸 Features

| Feature | Details |
|---|---|
| **130 Movies** | Action, Crime, Sci-Fi, Drama, Comedy, Animation, Horror, Bollywood |
| **Smart Recommendations** | Genre-weighted scoring + collaborative filtering |
| **Multi-User** | Switch between users, each with independent history |
| **Movie Posters** | Real TMDB poster images (loads from CDN, no API key needed) |
| **Search** | Search by title, genre, or tag — with live dropdown |
| **Rating System** | 1–5 star ratings that influence future recommendations |
| **Watch History** | Per-user history with remove/clear functionality |
| **Genre Filter** | Filter any tab by genre with one click |
| **8 Discover Tabs** | Trending, Bollywood, Top Rated, Action, Drama, Sci-Fi, Horror, Comedy |
| **Similarity Matrix** | Cosine similarity between all users (console) |
| **Offline Mode** | Entire frontend runs with zero backend or internet (posters are CDN images only) |

---

## 🗂 Project Structure

```
MovieRecommendationSystem/
├── src/
│   ├── CineMatch.html           ← Frontend UI (open in any browser)
│   ├── Main.java                ← Entry point, menu, sample data
│   ├── RecommendationEngine.java← Core engine (collaborative filtering)
│   ├── SimilarityStrategy.java  ← Cosine similarity algorithm
│   ├── User.java                ← User model + ratings
│   ├── Movie.java               ← Movie model + avg rating
│   ├── Rating.java              ← Rating record
│   ├── RecommendedMovie.java    ← Recommendation result model
│   ├── FileHandler.java         ← Save/load data to disk
│   ├── Item.java                ← Base item interface
│   └── TMDBService.java         ← (Legacy) TMDB API service
├── out/                         ← Compiled .class files
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+ (uses switch expressions)
- Any modern browser (Chrome, Firefox, Edge)

### Run the Console App

```bash
# Compile
javac -d out src/*.java

# Run
java -cp out Main
```

The app will automatically open `CineMatch.html` in your default browser and fall back to console mode if the file is not found.

### Open the Frontend Directly

Just double-click `src/CineMatch.html` — or drag it into any browser. No server needed.

---

## 🎯 How the Recommendation Engine Works

### Genre-Weighted Scoring (Frontend)

1. Every movie a user watches increases the weight of its genres.
2. Rating boosts the weight: a 5-star rating contributes 2× more than an unrated watch.
3. Candidate movies (not yet watched) are scored by:
   - **Genre match** (up to 45 points) — weighted sum of normalised genre affinities
   - **Quality score** (up to 22 points) — based on IMDB-style community rating
   - **Search affinity** (up to 18 points) — tags matching recent search history
   - **Bollywood bonus** (up to 18 points) — if user has Bollywood affinity

### Collaborative Filtering (Java Backend)

1. Build a shared rating vector space across all users and movies.
2. Compute **cosine similarity** between the target user and every other user.
3. For each unrated movie, aggregate ratings from similar users weighted by similarity score.
4. Return top-N movies sorted by predicted rating.

**Cold-start fallback**: If the user has no ratings, the engine returns globally popular movies sorted by average rating.

---

## 🎬 Movie Database

### Hollywood (104 movies)

| Genre | Count | Notable Titles |
|---|---|---|
| Action | 20 | The Dark Knight, Inception, Mad Max: Fury Road |
| Crime/Thriller | 16 | The Godfather, Pulp Fiction, Se7en, Oldboy |
| Sci-Fi | 13 | Interstellar, Dune, Blade Runner 2049, 2001 |
| Drama | 14 | Shawshank Redemption, Whiplash, Oppenheimer |
| Comedy | 8 | The Grand Budapest Hotel, Groundhog Day |
| Animation | 9 | Spider-Verse, Spirited Away, WALL-E, Coco |
| Horror | 7 | Get Out, Hereditary, The Shining, Midsommar |

### Bollywood (26 movies)

| Era | Notable Titles |
|---|---|
| Classics | Mughal-E-Azam, DDLJ, Lagaan, Dil Chahta Hai |
| 2000s | Taare Zameen Par, 3 Idiots, Swades, Rang De Basanti |
| 2010s | Dangal, Queen, Andhadhun, Gully Boy, Gangs of Wasseypur |
| Social | Article 15, Masaan, Tumbbad, Kahaani |

---

## 🖥 Frontend Guide

### User Management
- Two default users (Alice, Riya) are pre-loaded.
- Add unlimited users via the sidebar input.
- Each user maintains completely separate watch history, ratings, and recommendations.

### Discovering Movies
- **Trending** — randomly shuffled selection, refreshes on each visit
- **Bollywood** — all 26 Indian films
- **Top Rated** — sorted by community rating (9.3 → 7.0)
- **Action / Drama / Sci-Fi / Horror / Comedy** — filtered tabs

### Searching
- Searches by **title**, **genre name**, and **tags** (e.g. "mafia", "heist", "ghibli")
- Results appear in a live dropdown as you type
- Recent searches are saved per user

### Marking Movies
- Click any movie → modal opens with full details
- Rate with ⭐ stars before or after marking as watched
- Or use the **+ Watched** quick button on hover

### Recommendations
- Appear automatically after watching 1+ movie
- Score bars show "Match %" and "Rating %" for each pick
- Toggle Top 5 / 10 / 20 from the dropdown
- Reason tags explain why each movie was recommended

---

## ⚙ Console Menu

```
1. Add User           — create a new user by ID and name
2. Add Movie          — add a custom movie to the database
3. Rate a Movie       — record a user's rating (1.0–5.0)
4. Get Recommendations— personalised picks with optional genre filter
5. List All Users     — view all users and their rating counts
6. List All Movies    — full movie list with average ratings
7. Similarity Matrix  — cosine similarity between all user pairs
8. Save Data          — persist all data to disk
9. Save & Exit        — save and quit
```

---

## 💾 Data Persistence

Data is saved to a local file by `FileHandler.java`. The file is loaded automatically on startup. If no file is found, the 130-movie sample dataset is seeded automatically.

---

## 🔧 Configuration & Extension

### Adding More Movies
Add to `seedSampleData()` in `Main.java`:
```java
engine.addMovie("id", "Title", "Genre");
```
Then add a matching entry to the `MOVIES` array in `CineMatch.html`.

### Adding More Users
```java
engine.addUser("u8", "Priya");
engine.rateMovie("u8", "b1", 5.0);
```

### Changing the Similarity Algorithm
Swap the implementation in `RecommendationEngine.java`. The `SimilarityStrategy` interface makes it easy to plug in Pearson, Jaccard, or any other measure.

---

## 🏗 Design Patterns Used

| Pattern | Where Used |
|---|---|
| **Strategy** | `SimilarityStrategy` — swappable similarity algorithms |
| **Repository** | `RecommendationEngine` — centralised data store |
| **Factory (light)** | `FileHandler` — serialisation/deserialisation |
| **Facade** | `Main` — hides engine complexity behind simple menu |

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Scanner-based CLI |
| Algorithm | Cosine Similarity (collaborative filtering) |
| Frontend | Vanilla HTML5 / CSS3 / JavaScript (ES2022) |
| Fonts | Playfair Display, DM Sans, Space Mono |
| Images | TMDB image CDN (no API key, public poster URLs) |
| Persistence | Custom file serialisation via `FileHandler.java` |

---

## 📝 Known Limitations

- **Posters**: TMDB CDN requires internet. In a fully offline environment, posters show a stylised placeholder — all functionality still works.
- **Frontend ↔ Backend sync**: The HTML frontend and Java backend are currently independent. User actions in the browser don't update the Java engine and vice versa. To bridge them, a local HTTP server (e.g. `com.sun.net.httpserver`) could be added.
- **Persistence**: Java file-based persistence; not a database. Suitable for demo/coursework scale.

---

## 🚧 Possible Extensions

- [ ] REST API bridge between Java backend and HTML frontend (using HttpServer)
- [ ] Matrix factorisation (SVD) for better recommendations
- [ ] Watchlist / "Want to Watch" feature
- [ ] Export recommendations as PDF
- [ ] Dark/light theme toggle
- [ ] Progressive Web App (PWA) support

---

## 👨‍💻 Author Notes

This project was built as a Java DSA/OOP coursework demonstrating:
- Collaborative filtering from scratch
- Object-oriented design with clear separation of concerns
- A production-quality frontend that runs without any backend server

---

*CineMatch — Because every movie deserves the right audience.*