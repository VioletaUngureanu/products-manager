package com.cosmeticmanager.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.cosmeticmanager.app.data.ServiceLocator
import com.cosmeticmanager.app.ui.editor.ProductEditorViewModel
import com.cosmeticmanager.app.ui.list.ProductListViewModel

/**
 * Builds the app's ViewModels, injecting the shared repository from [ServiceLocator].
 *
 * Replaces Hilt (introduced later in the course) for this milestone. The editor ViewModel needs the
 * id of the product being edited (null when creating), passed through here.
 */
class ViewModelFactory(
    private val editingProductId: String? = null,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val repository = ServiceLocator.productRepository
        return when {
            modelClass.isAssignableFrom(ProductListViewModel::class.java) ->
                ProductListViewModel(repository) as T

            modelClass.isAssignableFrom(ProductEditorViewModel::class.java) ->
                ProductEditorViewModel(repository, editingProductId) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
