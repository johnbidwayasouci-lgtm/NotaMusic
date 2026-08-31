package org.notamusic.app.data.settings

import android.content.Context
import org.notamusic.app.domain.music.SettingsRepository

class PreferencesSettingsRepository(context: Context) : SettingsRepository {
    private val p = context.getSharedPreferences("notamusic_settings", Context.MODE_PRIVATE)
    override fun promptTone() = p.getBoolean("prompt_tone", true)
    override fun autoScroll() = p.getBoolean("auto_scroll", true)
    override fun highlightNewElements() = p.getBoolean("highlight_new", true)
    override fun showTouchLine() = p.getBoolean("touch_line", true)
    override fun showMeasureNumbers() = p.getBoolean("measure_numbers", true)
    override fun scoreScale() = p.getFloat("score_scale", 1f)
    override fun editScale() = p.getFloat("edit_scale", 1f)
}
