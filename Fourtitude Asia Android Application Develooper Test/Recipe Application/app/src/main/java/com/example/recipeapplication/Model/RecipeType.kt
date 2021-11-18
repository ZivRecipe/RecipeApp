package com.example.recipeapplication.Model

import com.google.firebase.database.Exclude


data class RecipeType(
    var recipe_type_id: String? = null,
    var recipe_type_name: String? = null,

) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "recipe_type_name" to recipe_type_name,
        )
    }
}

