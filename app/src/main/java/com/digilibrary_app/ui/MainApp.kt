package com.digilibrary_app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.digilibrary_app.data.local.SessionManager
import com.digilibrary_app.data.model.BookDto
import com.digilibrary_app.ui.screens.auth.CompleteProfileScreen
import com.digilibrary_app.ui.screens.auth.ForgotPasswordScreen
import com.digilibrary_app.ui.screens.auth.LoginScreen
import com.digilibrary_app.ui.screens.auth.RegisterScreen
import com.digilibrary_app.ui.screens.book.BookDetailScreen
import com.digilibrary_app.ui.screens.bookshelf.BookshelfScreen
import com.digilibrary_app.ui.screens.home.HomeScreen
import com.digilibrary_app.ui.screens.profile.ProfileScreen
import com.digilibrary_app.ui.screens.reader.PdfReaderScreen
import com.digilibrary_app.ui.screens.teacher.TeacherOverviewScreen
import com.digilibrary_app.ui.theme.PrimaryEmerald
import com.digilibrary_app.ui.theme.SurfaceWhite
import com.digilibrary_app.ui.theme.TextMuted

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("tab_home", "Beranda", Icons.Default.Home)
    object Bookshelf : BottomNavItem("tab_bookshelf", "Rak Buku", Icons.Default.Book)
    object TeacherOverview : BottomNavItem("tab_teacher", "Pantau Kelas", Icons.Default.Dashboard)
    object Profile : BottomNavItem("tab_profile", "Profil", Icons.Default.Person)
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val navController = rememberNavController()

    // Determine start destination
    val startDestination = remember {
        if (!sessionManager.isLoggedIn()) {
            "login"
        } else if (!sessionManager.isProfileComplete()) {
            "complete_profile"
        } else {
            "main"
        }
    }

    var activeReadingBook by remember { mutableStateOf<BookDto?>(null) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { user, isProfileComplete ->
                    if (!isProfileComplete) {
                        navController.navigate("complete_profile") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { user, isProfileComplete ->
                    if (!isProfileComplete) {
                        navController.navigate("complete_profile") {
                            popUpTo("register") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("register") { inclusive = true }
                        }
                    }
                },
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("complete_profile") {
            CompleteProfileScreen(
                onCompleted = {
                    navController.navigate("main") {
                        popUpTo("complete_profile") { inclusive = true }
                    }
                }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("main") {
            val mainTabNavController = rememberNavController()
            val user = sessionManager.getUser()
            val isGuru = user?.role == "guru"

            val navItems = remember(isGuru) {
                if (isGuru) {
                    listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Bookshelf,
                        BottomNavItem.TeacherOverview,
                        BottomNavItem.Profile
                    )
                } else {
                    listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Bookshelf,
                        BottomNavItem.Profile
                    )
                }
            }

            val navBackStackEntry by mainTabNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = SurfaceWhite,
                        tonalElevation = 8.dp
                    ) {
                        navItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        mainTabNavController.navigate(item.route) {
                                            popUpTo(mainTabNavController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title,
                                        tint = if (selected) PrimaryEmerald else TextMuted
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        color = if (selected) PrimaryEmerald else TextMuted
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = PrimaryEmerald.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = mainTabNavController,
                    startDestination = BottomNavItem.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(BottomNavItem.Home.route) {
                        HomeScreen(
                            onBookClick = { book ->
                                activeReadingBook = book
                                navController.navigate("book_detail/${book.id}")
                            }
                        )
                    }

                    composable(BottomNavItem.Bookshelf.route) {
                        BookshelfScreen(
                            onOpenBook = { book ->
                                activeReadingBook = book
                                navController.navigate("pdf_reader")
                            }
                        )
                    }

                    if (isGuru) {
                        composable(BottomNavItem.TeacherOverview.route) {
                            TeacherOverviewScreen()
                        }
                    }

                    composable(BottomNavItem.Profile.route) {
                        ProfileScreen(
                            onLogoutSuccess = {
                                navController.navigate("login") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }

        composable("book_detail/{bookId}") { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            BookDetailScreen(
                bookIdOrSlug = bookId,
                onBack = { navController.popBackStack() },
                onReadBook = { book ->
                    activeReadingBook = book
                    navController.navigate("pdf_reader")
                }
            )
        }

        composable("pdf_reader") {
            if (activeReadingBook != null) {
                PdfReaderScreen(
                    book = activeReadingBook!!,
                    onClose = {
                        navController.popBackStack()
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
}
