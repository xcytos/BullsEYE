# 🎯 Bullseye Game with Redemption Shop

A dynamic side-scrolling coin collection game integrated with a real-time redemption shop — built to merge fun gameplay with virtual rewards!

## 🕹️ Overview

**Bullseye Game** is a continuous runner where players control a bullseye icon, collect coins, avoid obstacles, and later redeem their earned coins in an interactive web-based shop. The game logic is implemented in **Scala**, while the **redemption shop** leverages **HTML, CSS, and JavaScript** for a smooth and responsive user experience.

---

## 🎮 Game Features

- **Player Control:** Navigate a bullseye character with smooth jump and gravity mechanics  
- **Coin Collection:** Accumulate coins to build up your in-game balance  
- **Obstacle Avoidance:** Dodge moving carts and survive as long as possible  
- **User Profiles:** User-specific progress saved in `users.txt`  
- **Physics Engine:** Realistic gravity and limited jumps system  
- **Progressive Difficulty:** Speed increases with higher scores  
- **High Score System:** Track and beat your personal bests  

---

## 🛍️ Redemption Shop Features

- **Product Catalog:** Browse virtual products like apparel, electronics, and home appliances  
- **Discount Rewards:** Redeem coins for 10%–20% discount codes  
- **Order Processing:** Simulated checkout with applied discounts  
- **Target®-Inspired UI:** Clean and responsive design for a real-store feel  

---

## ⚙️ Technical Components

### Scala Game Engine:
- Built with **Scala Swing**
- Collision detection and physics simulation
- Persistent user data storage
- Real-time score and difficulty tracking

### HTML/CSS/JS Redemption Shop:
- Responsive interface with modern styling
- Interactive product displays
- Real-time discount logic based on user’s coin balance
- Order confirmation and simulated checkout flow

---

## 🔄 Game–Shop Integration

- Press **`T`** after game over to access the redemption shop  
- Coin balance syncs automatically between the game and the shop  
- Redeemed discounts are applied to eligible purchases  

---

## 🚀 How to Run

1. Open `BullseyeGame.scala` in your Scala-supported IDE  
2. Run the game and login using a user ID  
3. Jump over obstacles to collect coins  
4. After the game ends, press `T` to open the shop  
5. Use collected coins to redeem discounts in the shop  

---

## 📁 Project Structure

## Directory Breakdown

### 🗂️ `src/main/`
- **Root directory** for all application code and resources

### 📁 `resources/`
- Contains game assets and data files
- Key files:
  - `users.txt` - Stores user IDs and coin balances
  - `target.html` - Redemption shop UI (HTML/CSS/JS)

### 💻 `scala/`
- Contains Scala source code
- Core file:
  - `DinoGame.scala` - Main game implementation:
    - Player controls
    - Collision detection
    - Physics engine
    - Coin collection system
    - Game state management


---

## 🛠️ Requirements

- **Java 8+**  
- **Scala 2.13**  
- **Scala Swing Library**

---

Feel free to fork, contribute, or enhance this project. Ideal for learning game development and web integration in a fun and rewarding way!

