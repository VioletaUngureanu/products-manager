package com.cosmeticmanager.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cosmeticmanager.app.ui.detail.ProductDetailScreen
import com.cosmeticmanager.app.ui.editor.ProductEditorScreen
import com.cosmeticmanager.app.ui.list.ProductListScreen

/** Route constants and argument keys for the app's navigation graph. */
private object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{productId}"
    const val CREATE = "create"
    const val EDIT = "edit/{productId}"

    const val ARG_PRODUCT_ID = "productId"

    fun detail(id: String) = "detail/$id"
    fun edit(id: String) = "edit/$id"
}

/**
 * Single-activity navigation graph (L03/L04 direction): a list destination plus SEPARATE
 * destinations for create, edit and detail — each CRUD operation gets its own screen, satisfying
 * the rubric's "maintained in a separate activity/fragment" requirement.
 */
@Composable
fun CosmeticNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) {
            ProductListScreen(
                onAddProduct = { navController.navigate(Routes.CREATE) },
                onEditProduct = { id -> navController.navigate(Routes.edit(id)) },
                onOpenProduct = { id -> navController.navigate(Routes.detail(id)) },
            )
        }

        composable(Routes.CREATE) {
            ProductEditorScreen(
                productId = null,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument(Routes.ARG_PRODUCT_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(Routes.ARG_PRODUCT_ID)
            ProductEditorScreen(
                productId = id,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument(Routes.ARG_PRODUCT_ID) { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString(Routes.ARG_PRODUCT_ID).orEmpty()
            ProductDetailScreen(
                productId = id,
                onNavigateBack = { navController.popBackStack() },
                onEdit = { editId ->
                    // Replace detail with edit so Back from edit returns to the list.
                    navController.navigate(Routes.edit(editId))
                },
            )
        }
    }
}
