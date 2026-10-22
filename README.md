Smart Pantry Manager
Smart Pantry Manager is a Java Android application that helps households reduce food waste. Users keep a local list of the ingredients they have, then the app displays only the recipes they can make immediately.

Features
•Create, read, update and delete pantry ingredients.
•Store an ingredient name, quantity, unit and optional expiry date.
•Persist data locally with SQLite and SQLiteOpenHelper.
•Seed 20 recipes on first launch.
•Use strict recipe matching: every required ingredient must be present in enough quantity.
•Recognise simple singular/plural forms and convert between g/kg and ml/l.
•Provide five screens: pantry, add/edit ingredient, suggested recipes, recipe detail and settings.
•Use Intents for navigation and custom ListView adapters for database-driven lists.

Database choice
SQLite was selected because the pantry is private, small, and must remain usable without an internet connection. DatabaseHelper creates the pantry, recipe and recipe-ingredient tables, seeds the recipe catalogue, and supplies CRUD methods for the user’s pantry.

Run the project
1.Install Android Studio and Android SDK Platform 35.
2.Open this folder in Android Studio.
3.Let Gradle sync and accept the Android SDK licence/install prompt if shown.
4.Create an emulator or connect an Android phone with USB debugging enabled.
5.Select the app configuration and press Run.

Suggested test
1.Add tomato, quantity 1, unit piece.
2.Add egg, quantity 2, unit piece.
3.Open Suggested Recipes: Tomato Egg Scramble must appear.
4.Delete the tomato, return to Suggestions, and confirm the recipe no longer appears.