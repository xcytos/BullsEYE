Bullseye Game with Redemption Shop - GitHub Repository Description
A dynamic side-scrolling game with integrated reward redemption system!

This project features a continuous runner game where players collect coins and redeem them for discounts on products in a virtual store. Built with Scala for game logic and HTML/CSS/JS for the redemption shop.

🎮 Game Features
Player Character: Control a bullseye icon that jumps to avoid obstacles

Coin Collection: Collect coins to increase your balance

Obstacle Avoidance: Dodge moving carts to survive

User Profiles: Persistent coin balances stored in users.txt

Physics System: Gravity and jumping mechanics with limited jumps

Progressive Difficulty: Speed increases as your score grows

High Score Tracking: Compete against your best performance

🛍️ Redemption Shop Features
Product Catalog: Browse shirts, appliances, electronics

Discount System: Redeem coins for 10% or 20% discounts

Order Processing: Complete purchases with applied discounts

Responsive UI: Clean, Target®-inspired interface

⚙️ Technical Components
Scala Game Engine:

Swing-based graphics

Collision detection

Persistent user data storage

Physics simulation (gravity/jumping)

HTML Redemption Shop:

Modern CSS styling

Interactive product cards

Dynamic discount application

Order confirmation system

🔄 Game-to-Shop Integration
Press 'T' after game over to launch redemption shop

Coin balances sync between game and shop

Discounts applied to purchases based on collected coins

🚀 How to Run
Launch BullseyeGame.scala

Login with user ID

Collect coins by jumping over obstacles

After game over, press 'T' to open shop

Redeem coins for discounts on products

📁 Project Structure
text
src/
├── main/
│   ├── resources/          # Game assets & data files
│   │   ├── bg3.jpg         # Background image
│   │   ├── coin_image.gif  # Coin sprite
│   │   ├── users.txt       # User database
│   │   └── target.html     # Redemption shop
│   └── scala/
│       └── BullseyeGame.scala # Main game logic
🛠️ Dependencies
Java 8+

Scala 2.13

Scala Swing Library
