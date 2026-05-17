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
import kotlinx.coroutines.launch

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
        val userRepo = com.quiziche.app.data.repository.UserRepository()
        val usersResult = userRepo.getLeaderboard(1)
        if (usersResult.isSuccess && usersResult.getOrDefault(emptyList()).isEmpty()) {
            userRepo.seedRandomUsers()
        }

        if (authRepository.isUserLoggedIn()) {
            launch {
                val gameRepo = com.quiziche.app.data.repository.GameRepository()
                gameRepo.getActiveGames().collect { games ->
                    val active = games.firstOrNull { System.currentTimeMillis() - it.startTime < 5 * 60 * 1000 } // only join recent games
                    if (active != null) {
                        val currentRoute = navController.currentDestination?.route
                        if (currentRoute?.startsWith("game") != true && currentRoute?.startsWith("results") != true) {
                            navController.navigate("game?mode=friend&category=${active.category}&roomId=${active.sessionId}")
                        }
                    }
                }
            }
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
                onNavigateToOngoingGames = { navController.navigate("matchmaking/all") }, // Redirect to live matchmaking
                onNavigateToGameSettings = { navController.navigate("game_settings?mode=random") },
                onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                onNavigateToFriends = { navController.navigate("friends") },
                onNavigateToGame = { roomId -> 
                    navController.navigate("game?mode=multiplayer&roomId=$roomId")
                },
                onNavigateToMissions = { navController.navigate("missions") }
            )
        }
        composable("leaderboard") {
            LeaderboardScreen(
                onNavigateBack = { navController.popBackStack() },
                currentUserName = "Player" // Replace with real name logic if needed or let LeaderboardScreen fetch it
            )
        }
        composable("friends") {
            FriendsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMainMenu = { navController.navigate("main_menu") {
                    popUpTo("main_menu") { inclusive = true }
                } },
                onNavigateToCategories = { navController.navigate("categories") },
                onNavigateToLeaderboard = { navController.navigate("leaderboard") }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogOut = {
                    navController.navigate("login") {
                        popUpTo("main_menu") { inclusive = true }
                    }
                },
                onNavigateToAvatarSelection = { navController.navigate("avatar_selection") },
                onNavigateToAdmin = { navController.navigate("admin") }
            )
        }
        composable("avatar_selection") {
            AvatarSelectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("missions") {
            MissionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("admin") {
            AdminScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("categories") {
            CategoriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategoryDetails = { categoryId ->
                    navController.navigate("category_details/$categoryId")
                },
                onNavigateToMainMenu = { navController.navigate("main_menu") {
                    popUpTo("main_menu") { inclusive = true }
                } },
                onNavigateToFriends = { navController.navigate("friends") },
                onNavigateToLeaderboard = { navController.navigate("leaderboard") }
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
                onNavigateToGameSettings = { navController.navigate("game_settings?category=$categoryId&mode=solo") }
            )
        }
        composable(
            route = "game_settings?category={category}&mode={mode}",
            arguments = listOf(
                navArgument("category") { defaultValue = "all" },
                navArgument("mode") { defaultValue = "random" }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "all"
            val mode = backStackEntry.arguments?.getString("mode") ?: "random"
            GameSettingsScreen(
                initialCategory = category,
                initialMode = mode,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNext = { mode, selectedCat, difficulty ->
                    if (mode == "solo") {
                        navController.navigate("game?mode=solo&category=$selectedCat&difficulty=$difficulty")
                    } else if (mode == "friend") {
                        navController.navigate("friend_match/$selectedCat/$difficulty")
                    } else {
                        navController.navigate("matchmaking/$selectedCat/$difficulty")
                    }
                }
            )
        }
        composable(
            route = "matchmaking/{category}/{difficulty}",
            arguments = listOf(
                navArgument("category") { defaultValue = "all" },
                navArgument("difficulty") { defaultValue = "Any" }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "all"
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "Any"
            MatchmakingScreen(
                category = category,
                difficulty = difficulty,
                onNavigateToGame = { roomId -> 
                    navController.navigate("game?mode=random&category=$category&difficulty=$difficulty&roomId=$roomId") {
                        popUpTo("matchmaking/{category}/{difficulty}") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "friend_match/{category}/{difficulty}",
            arguments = listOf(
                navArgument("category") { defaultValue = "all" },
                navArgument("difficulty") { defaultValue = "Any" }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "all"
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "Any"
            FriendMatchScreen(
                category = category,
                difficulty = difficulty,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGame = { roomId ->
                    navController.navigate("game?mode=friend&category=$category&difficulty=$difficulty&roomId=$roomId") {
                        popUpTo("friend_match/{category}/{difficulty}") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "game?mode={mode}&category={category}&difficulty={difficulty}&roomId={roomId}",
            arguments = listOf(
                navArgument("mode") { defaultValue = "random" },
                navArgument("category") { defaultValue = "all" },
                navArgument("difficulty") { defaultValue = "Any" },
                navArgument("roomId") { nullable = true }
            )
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "random"
            val category = backStackEntry.arguments?.getString("category") ?: "all"
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "Any"
            val roomId = backStackEntry.arguments?.getString("roomId")

            GameScreen(
                isSingleplayer = mode == "solo",
                category = category,
                difficulty = difficulty,
                roomId = roomId,
                onNavigateToResults = { score, userNick, oppScore, oppNick, total, cat, won, correctAnswers ->
                    val isSingle = mode == "solo"
                    navController.navigate("results/$score/$total?userNick=$userNick&oppScore=$oppScore&oppName=$oppNick&isSingleplayer=$isSingle&isWinner=$won&category=$cat&correctAnswers=$correctAnswers") {
                        popUpTo("game") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "results/{score}/{totalQuestions}?userNick={userNick}&oppScore={oppScore}&oppName={oppName}&isSingleplayer={isSingleplayer}&isWinner={isWinner}&category={category}&correctAnswers={correctAnswers}",
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("totalQuestions") { type = NavType.IntType },
                navArgument("userNick") { type = NavType.StringType; defaultValue = "You" },
                navArgument("oppScore") { type = NavType.IntType; defaultValue = 0 },
                navArgument("oppName") { type = NavType.StringType; defaultValue = "Opponent" },
                navArgument("isSingleplayer") { type = NavType.BoolType; defaultValue = false },
                navArgument("isWinner") { type = NavType.BoolType; defaultValue = false },
                navArgument("category") { type = NavType.StringType; defaultValue = "all" },
                navArgument("correctAnswers") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val totalQuestions = backStackEntry.arguments?.getInt("totalQuestions") ?: 5
            val userNick = backStackEntry.arguments?.getString("userNick") ?: "You"
            val oppScore = backStackEntry.arguments?.getInt("oppScore") ?: 0
            val oppName = backStackEntry.arguments?.getString("oppName") ?: "Opponent"
            val isSingleplayer = backStackEntry.arguments?.getBoolean("isSingleplayer") ?: false
            val isWinner = backStackEntry.arguments?.getBoolean("isWinner") ?: false
            val category = backStackEntry.arguments?.getString("category") ?: "all"
            val correctAnswers = backStackEntry.arguments?.getInt("correctAnswers") ?: 0

            GameResultsScreen(
                score = score,
                userNickname = userNick,
                opponentScore = oppScore,
                opponentName = oppName,
                totalQuestions = totalQuestions,
                category = category,
                isSingleplayer = isSingleplayer,
                isWinner = isWinner,
                correctAnswers = correctAnswers,
                onNavigateToMainMenu = { navController.navigate("main_menu") {
                    popUpTo("main_menu") { inclusive = true }
                } },
                onNavigateToRematch = { 
                    if (isSingleplayer) {
                        navController.navigate("game?mode=solo&category=$category") {
                            popUpTo("main_menu")
                        }
                    } else {
                        navController.navigate("matchmaking") 
                    }
                }
            )
        }

    }
}
