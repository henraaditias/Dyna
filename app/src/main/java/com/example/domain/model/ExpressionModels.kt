package com.example.domain.model

enum class ExpressionState(val label: String) {
  SMILE("Senyum"),
  HAPPY("Ceria"),
  NEUTRAL("Netral"),
  HEART_EYES("Suka"),
  SUNGLASSES("Keren"),
  WINK("Kedip"),
  SLEEPY_ZZZ("Ngantuk"),
  XD_LAUGH("Ketawa"),
  SURPRISED("Kaget"),
  SMIRK("Santai"),
  TONGUE_OUT("Jahil"),
  CHEERFUL("Semangat"),
  MINIMAL_PEEK("Mengintip"),
  WAVY_DIZZY("Pusing"),
  WORRIED("Khawatir"),
  SAD("Sedih"),
  FROWN("Murung"),
  ANGRY("Kesal"),
  SOUR("Asam"),
  MUTE_X("Diam"),
  KISS("Manis"),
  DEVILISH("Nakal"),
  SLY("Licik"),
  KO_X("Lelah"),
  WAVE_ALERT("Siaga"),
  ;

  fun nextIdleCycle(): ExpressionState {
    val cycle =
      listOf(
        SMILE,
        HAPPY,
        HEART_EYES,
        SUNGLASSES,
        WINK,
        XD_LAUGH,
        TONGUE_OUT,
        SLEEPY_ZZZ,
        SMIRK,
        MINIMAL_PEEK,
        CHEERFUL,
        SURPRISED,
        NEUTRAL,
      )
    val idx = cycle.indexOf(this)
    return if (idx == -1) cycle.first() else cycle[(idx + 1) % cycle.size]
  }
}

enum class IdleDisplayMode(val labelId: String, val labelEn: String) {
  CIRCLE_FACE("Titik Bulat Ekspresi", "Circle Expression Dot"),
  PILL_CENTER_FACE("Pil Ekspresi Tengah", "Centered Face Pill"),
  PILL_LEFT_FACE_INFO("Pil Ekspresi Kiri + Info", "Left Face + Info Pill"),
  ;

  fun localizedLabel(language: AppLanguage): String =
    when (language) {
      AppLanguage.ID -> labelId
      AppLanguage.EN -> labelEn
    }

  companion object {
    fun fromPersisted(raw: String?): IdleDisplayMode =
      when (raw) {
        "MINIMAL_DOTS",
        "CIRCLE_FACE" -> CIRCLE_FACE
        "FACE_ONLY",
        "PILL_CENTER_FACE" -> PILL_CENTER_FACE
        "FACE_WITH_INFO",
        "PILL_LEFT_FACE_INFO" -> PILL_LEFT_FACE_INFO
        else -> PILL_LEFT_FACE_INFO
      }
  }
}

enum class IdlePlacement(val label: String) {
  FLOATING("Mengambang"),
  FIXED_TOP("Cutout Tetap"),
}

enum class IdleTapAction(val label: String) {
  QUICK_MENU("Menu Utama"),
  PLAY_REACTION("Reaksi Ekspresi"),
}

enum class QuickMenuCategory(val labelId: String, val labelEn: String) {
  NONE("Tutup", "Close"),
  MENU("Menu", "Menu"),
  CALENDAR("Kalender", "Calendar"),
  WEATHER("Cuaca", "Weather"),
  PRAYER("Jadwal Shalat", "Prayer Times"),
  SHORTCUTS("Pintasan", "Shortcuts"),
  MOOD_PICKER("Ekspresi", "Expressions"),
  ;

  fun localizedLabel(language: AppLanguage): String =
    when (language) {
      AppLanguage.ID -> labelId
      AppLanguage.EN -> labelEn
    }
}

enum class QuickMenuItem(
  val label: String,
  val targetCategory: QuickMenuCategory? = null,
) {
  CALENDAR("Kalender", QuickMenuCategory.CALENDAR),
  WEATHER("Cuaca", QuickMenuCategory.WEATHER),
  SHORTCUTS("Pintasan", QuickMenuCategory.SHORTCUTS),
  PRAYER("Shalat", QuickMenuCategory.PRAYER),
  MOOD_PICKER("Ekspresi", QuickMenuCategory.MOOD_PICKER),
  TIMER_1M("Timer 1m", null),
  STOPWATCH("Stopwatch", null),
  HIDE_ISLAND("Sembunyikan", null),
  ;

  companion object {
    val defaultItems: List<QuickMenuItem> =
      listOf(CALENDAR, WEATHER, SHORTCUTS, PRAYER, MOOD_PICKER, HIDE_ISLAND)
  }
}
