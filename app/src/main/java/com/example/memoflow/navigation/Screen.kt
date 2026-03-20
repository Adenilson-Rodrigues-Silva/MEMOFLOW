package com.example.memoflow.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Home : Screen("home_screen")
    object Profile : Screen("profile_screen")
    object Statistics : Screen("statistics_screen")
    object Gratitude : Screen("gratitude_screen")
    object Recall : Screen("recall_screen")
    object Notifications : Screen("notifications_screen")
    object Backup : Screen("backup_screen")
    object Store : Screen("store_screen")
    object WriteNote : Screen("write_note?noteId={noteId}") {
        fun createRoute(noteId: Long? = null) = if (noteId != null) "write_note?noteId=$noteId" else "write_note"
    }
}
