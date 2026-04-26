package com.quiziche.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.remember
import com.quiziche.app.ui.screens.*
import com.quiziche.app.ui.theme.Quiziche_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Quiziche_AppTheme {
                QuizicheApp()
            }
        }
    }
}

@Composable
fun QuizicheApp() {
    val navController = rememberNavController()
    val authRepository = remember { com.quiziche.app.data.repository.AuthRepository() }
    val startDest = if (authRepository.isUserLoggedIn()) "main_menu" else "login"

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val quizRepo = com.quiziche.app.data.repository.QuizRepository()
        val result = quizRepo.getQuestionsByCategory("all", 1)
        if (result.isSuccess && result.getOrDefault(emptyList()).isEmpty()) {
            com.quiziche.app.data.repository.QuestionSeeder().seedQuestions()
        }
    }

    NavHost(navController = navController, startDestination = startDest) {
        composable("login") {
            LoginScreen(
                onNavigateToMainMenu = { navController.navigate("main_menu") },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMainMenu = { navController.navigate("main_menu") }
            )
        }
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResetPassword = { navController.navigate("reset_password") }
            )
        }
        composable("reset_password") {
            ResetPasswordScreen(
                onNavigateBackToLogin = { navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                } }
            )
        }
        composable("main_menu") {
            MainMenuScreen(
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToCategories = { navController.navigate("categories") },
                onNavigateToOngoingGames = { navController.navigate("ongoing_games") },
                onNavigateToGameSettings = { navController.navigate("game_settings") },
                onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                onNavigateToGame = { _ -> navController.navigate("game") }
            )
        }
        composable("leaderboard") {
            LeaderboardScreen(
                onNavigateBack = { navController.popBackStack() },
                currentUserName = "Player" // Replace with real name logic if needed or let LeaderboardScreen fetch it
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("categories") {
            CategoriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategoryDetails = { categoryId ->
                    navController.navigate("category_details/$categoryId")
                }
            )
        }
        composable(
            route = "category_details/{categoryId}",
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: "science"
            CategoryDetailsScreen(
                categoryId = categoryId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGameSettings = { navController.navigate("game_settings") }
            )
        }
        composable("game_settings") {
            GameSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNext = { mode ->
                    if (mode == "solo") {
                        navController.navigate("game?mode=solo")
                    } else {
                        navController.navigate("matchmaking")
                    }
                }
            )
        }
        composable("matchmaking") {
            MatchmakingScreen(
                onNavigateToGame = { navController.navigate("game") },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "game?mode={mode}",
            arguments = listOf(navArgument("mode") { defaultValue = "multiplayer" })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "multiplayer"
            val isSingleplayer = mode == "solo"
            GameScreen(
                isSingleplayer = isSingleplayer,
                onNavigateToResults = { score, totalQuestions, isWinner -> 
                    navController.navigate("results/$score/$totalQuestions/$isSingleplayer/$isWinner") 
                }
            )
        }
        composable(
            route = "results/{score}/{totalQuestions}/{isSingleplayer}/{isWinner}",
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType },
                navArgument("isSingleplayer") { type = NavType.BoolType },
                navArgument("isWinner") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 5
            val isSingleplayer = backStackEntry.arguments?.getBoolean("isSingleplayer") ?: false
            val isWinner = backStackEntry.arguments?.getBoolean("isWinner") ?: false

            GameResultsScreen(
                score = score,
                totalQuestions = totalQuestions,
                isSingleplayer = isSingleplayer,
                isWinner = isWinner,
                onNavigateToMainMenu = { navController.navigate("main_menu") {
                    popUpTo("main_menu") { inclusive = true }
                } },
                onNavigateToRematch = { navController.navigate("matchmaking") }
            )
        }
        composable("ongoing_games") {
            OngoingGamesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGame = { navController.navigate("game") }
            )
        }
    }
}
