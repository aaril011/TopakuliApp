package com.project.topakuli.data.remote.model

data class ContributionForm(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userRole: String = "",
    val type: String = "", // "resep", "tanaman", "koreksi"
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "menunggu", // "menunggu", "disetujui", "revisi"
    val isReadByAdmin: Boolean = false,
    
    // For Recipe
    val recipeName: String? = null,
    val recipeSymptoms: String? = null,
    val recipeDescription: String? = null,
    val recipeIngredientsText: String? = null,
    val recipeStepsText: String? = null,
    val recipeIngredients: List<Pair<String, String>>? = null, // Pair<Name, Amount>
    val recipeSteps: List<String>? = null,
    val recipePrepTime: String? = null,
    val recipeUsage: String? = null,
    val recipeFrequency: String? = null,
    val recipeShelfLife: String? = null,
    val recipeContraindications: String? = null,
    val recipeSideEffects: String? = null,
    val recipeReference: String? = null,
    val recipeImageUrl: String? = null,
    
    // For Plant
    val plantName: String? = null,
    val plantLatin: String? = null,
    val plantLocalName: String? = null,
    val plantBenefits: String? = null,
    val plantDescription: String? = null,
    val plantReference: String? = null,
    val plantImageUrl: String? = null,
    
    // For Correction
    val targetItemId: String? = null,
    val targetItemName: String? = null,
    val targetItemType: String? = null, // "tanaman", "resep"
    val correctionPart: String? = null,
    val correctionNote: String? = null,
    val correctionReference: String? = null
)
