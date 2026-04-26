package com.quiziche.app.data.repository

import android.util.Log
import com.quiziche.app.data.model.Question
import kotlinx.coroutines.tasks.await

class QuestionSeeder {
    private val quizRepository = QuizRepository()

    suspend fun seedQuestions() {
        val questions = mutableListOf<Question>()

        // 1. Science
        questions.addAll(listOf(
            Question("", "What is the chemical symbol for Gold?", listOf("Au", "Ag", "Pb", "Fe"), 0, "Science", "Easy"),
            Question("", "Which planet is known as the Red Planet?", listOf("Earth", "Mars", "Jupiter", "Venus"), 1, "Science", "Easy"),
            Question("", "What is the powerhouse of the cell?", listOf("Nucleus", "Ribosome", "Mitochondria", "Endoplasmic Reticulum"), 2, "Science", "Medium"),
            Question("", "What gas do plants absorb from the atmosphere?", listOf("Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"), 2, "Science", "Easy"),
            Question("", "Who developed the theory of relativity?", listOf("Isaac Newton", "Albert Einstein", "Nikola Tesla", "Galileo Galilei"), 1, "Science", "Medium")
        ))

        // 2. History
        questions.addAll(listOf(
            Question("", "In which year did World War II end?", listOf("1945", "1939", "1918", "1914"), 0, "History", "Medium"),
            Question("", "Who was the first President of the United States?", listOf("Thomas Jefferson", "John Adams", "Abraham Lincoln", "George Washington"), 3, "History", "Easy"),
            Question("", "The Great Wall of China was primarily built to protect against which empire?", listOf("Roman Empire", "Mongol Empire", "Ottoman Empire", "Persian Empire"), 1, "History", "Medium"),
            Question("", "Which civilization built the pyramids of Giza?", listOf("Mayans", "Incas", "Ancient Egyptians", "Mesopotamians"), 2, "History", "Easy"),
            Question("", "Who was the founder of the Mongol Empire?", listOf("Kublai Khan", "Genghis Khan", "Attila the Hun", "Timur"), 1, "History", "Medium")
        ))

        // 3. Sports
        questions.addAll(listOf(
            Question("", "Which country won the 2018 FIFA World Cup?", listOf("Brazil", "Germany", "France", "Argentina"), 2, "Sports", "Medium"),
            Question("", "How many players are on a standard basketball team on the court?", listOf("5", "6", "7", "11"), 0, "Sports", "Easy"),
            Question("", "What sport is known as 'The Beautiful Game'?", listOf("Tennis", "Cricket", "Basketball", "Football/Soccer"), 3, "Sports", "Easy"),
            Question("", "Which athlete is known as the fastest man in the world?", listOf("Tyson Gay", "Usain Bolt", "Carl Lewis", "Justin Gatlin"), 1, "Sports", "Easy"),
            Question("", "In tennis, what piece of fruit is found at the top of the men's Wimbledon trophy?", listOf("Apple", "Strawberry", "Pineapple", "Grape"), 2, "Sports", "Hard")
        ))

        // 4. Art
        questions.addAll(listOf(
            Question("", "Who painted the Mona Lisa?", listOf("Vincent van Gogh", "Pablo Picasso", "Leonardo da Vinci", "Claude Monet"), 2, "Art", "Easy"),
            Question("", "Which art movement is Salvador Dali associated with?", listOf("Impressionism", "Cubism", "Surrealism", "Realism"), 2, "Art", "Medium"),
            Question("", "What colors do you mix to get green?", listOf("Red and Blue", "Blue and Yellow", "Red and Yellow", "Blue and White"), 1, "Art", "Easy"),
            Question("", "The 'Starry Night' was painted by which artist?", listOf("Vincent van Gogh", "Edvard Munch", "Claude Monet", "Jackson Pollock"), 0, "Art", "Easy"),
            Question("", "Where is the Louvre Museum located?", listOf("London", "Rome", "Paris", "Madrid"), 2, "Art", "Easy")
        ))

        // 5. Music
        questions.addAll(listOf(
            Question("", "Who is known as the 'King of Pop'?", listOf("Elvis Presley", "Michael Jackson", "Prince", "Freddie Mercury"), 1, "Music", "Easy"),
            Question("", "How many keys are on a standard piano?", listOf("66", "76", "88", "96"), 2, "Music", "Medium"),
            Question("", "Which classical composer was deaf?", listOf("Mozart", "Bach", "Beethoven", "Chopin"), 2, "Music", "Easy"),
            Question("", "Which band wrote the song 'Bohemian Rhapsody'?", listOf("The Beatles", "Led Zeppelin", "Queen", "The Rolling Stones"), 2, "Music", "Easy"),
            Question("", "What is the highest male singing voice called?", listOf("Bass", "Baritone", "Tenor", "Countertenor"), 3, "Music", "Medium")
        ))

        // 6. Geography
        questions.addAll(listOf(
            Question("", "What is the capital of Japan?", listOf("Beijing", "Seoul", "Tokyo", "Bangkok"), 2, "Geography", "Easy"),
            Question("", "Which is the largest ocean on Earth?", listOf("Atlantic Ocean", "Indian Ocean", "Arctic Ocean", "Pacific Ocean"), 3, "Geography", "Easy"),
            Question("", "Which country has the most population?", listOf("USA", "India", "China", "Russia"), 1, "Geography", "Medium"),
            Question("", "What is the longest river in the world?", listOf("Amazon", "Nile", "Yangtze", "Mississippi"), 1, "Geography", "Medium"),
            Question("", "Mount Everest is located in which mountain range?", listOf("Alps", "Andes", "Rockies", "Himalayas"), 3, "Geography", "Easy")
        ))

        // 7. Movies
        questions.addAll(listOf(
            Question("", "Who directed 'Inception'?", listOf("Steven Spielberg", "Christopher Nolan", "Quentin Tarantino", "Martin Scorsese"), 1, "Movies", "Medium"),
            Question("", "What is the highest-grossing film of all time?", listOf("Titanic", "Avengers: Endgame", "Avatar", "Star Wars: The Force Awakens"), 2, "Movies", "Medium"),
            Question("", "Which movie features the quote 'I'll be back'?", listOf("Die Hard", "Rocky", "The Terminator", "Rambo"), 2, "Movies", "Easy"),
            Question("", "Who plays Iron Man in the Marvel Cinematic Universe?", listOf("Chris Evans", "Chris Hemsworth", "Robert Downey Jr.", "Mark Ruffalo"), 2, "Movies", "Easy"),
            Question("", "What year was the first Jurassic Park movie released?", listOf("1990", "1993", "1995", "1997"), 1, "Movies", "Medium")
        ))

        // 8. Literature
        questions.addAll(listOf(
            Question("", "Who wrote 'Romeo and Juliet'?", listOf("Charles Dickens", "William Shakespeare", "Mark Twain", "Jane Austen"), 1, "Literature", "Easy"),
            Question("", "What is the best-selling book of all time?", listOf("Don Quixote", "Harry Potter", "The Lord of the Rings", "The Bible"), 3, "Literature", "Medium"),
            Question("", "In which book series does the character 'Katniss Everdeen' appear?", listOf("Divergent", "The Hunger Games", "Twilight", "Maze Runner"), 1, "Literature", "Easy"),
            Question("", "Who wrote '1984'?", listOf("George Orwell", "Aldous Huxley", "Ray Bradbury", "J.D. Salinger"), 0, "Literature", "Medium"),
            Question("", "Which novel begins with the line 'Call me Ishmael'?", listOf("Moby-Dick", "The Great Gatsby", "To Kill a Mockingbird", "Pride and Prejudice"), 0, "Literature", "Medium")
        ))

        // 9. Technology
        questions.addAll(listOf(
            Question("", "Who is the founder of Microsoft?", listOf("Steve Jobs", "Elon Musk", "Bill Gates", "Mark Zuckerberg"), 2, "Technology", "Easy"),
            Question("", "What does HTTP stand for?", listOf("HyperText Transfer Protocol", "HyperText Transmission Protocol", "HyperText Transfer Process", "HyperText Transmission Process"), 0, "Technology", "Medium"),
            Question("", "Which company developed the iPhone?", listOf("Google", "Microsoft", "Samsung", "Apple"), 3, "Technology", "Easy"),
            Question("", "What is the main function of a CPU?", listOf("Store data", "Process instructions", "Display graphics", "Provide power"), 1, "Technology", "Medium"),
            Question("", "When was the World Wide Web invented?", listOf("1989", "1995", "2000", "1980"), 0, "Technology", "Medium")
        ))

        // 10. Food
        questions.addAll(listOf(
            Question("", "What is the main ingredient in guacamole?", listOf("Tomato", "Onion", "Avocado", "Pepper"), 2, "Food", "Easy"),
            Question("", "Which country is the origin of pizza?", listOf("France", "USA", "Italy", "Greece"), 2, "Food", "Easy"),
            Question("", "What type of food is sushi?", listOf("Korean", "Chinese", "Japanese", "Thai"), 2, "Food", "Easy"),
            Question("", "What is the most consumed beverage in the world after water?", listOf("Coffee", "Tea", "Beer", "Milk"), 1, "Food", "Medium"),
            Question("", "Which fruit has its seeds on the outside?", listOf("Apple", "Strawberry", "Kiwi", "Watermelon"), 1, "Food", "Easy")
        ))

        questions.forEach { question ->
            quizRepository.saveGeneratedQuestion(question)
        }
        
        Log.d("QuestionSeeder", "50 questions have been seeded into Firestore.")
    }
}
