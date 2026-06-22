package com.project.topakuli.ui.kontribusi

sealed class ChatItem {
    data class SystemMessage(val message: String, val timestamp: Long = System.currentTimeMillis()) : ChatItem()
    data class UserMessage(val message: String, val timestamp: Long = System.currentTimeMillis()) : ChatItem()
    object OptionsMenu : ChatItem()
    object ContributionTypeMenu : ChatItem()
    data class InputCard(val label: String, val hint: String, val type: InputType) : ChatItem() {
        enum class InputType { NAME, PLANT_NAME, RECIPE_NAME }
    }
    object SuggestionMenu : ChatItem()
    object QuestionTipsCard : ChatItem()
    object ContactInfoCard : ChatItem()
    object CorrectionSearchCard : ChatItem()
}