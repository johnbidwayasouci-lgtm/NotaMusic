package org.notamusic.app.data.settings

import android.content.Context
import org.notamusic.app.domain.music.SettingsRepository

class PreferencesSettingsRepository(context: Context) : SettingsRepository {
 private val p=context.getSharedPreferences("notamusic_settings",Context.MODE_PRIVATE)
 override fun promptTone()=p.getBoolean("prompt_tone",true)
 override fun autoScroll()=p.getBoolean("auto_scroll",true)
 override fun highlightNewElements()=p.getBoolean("highlight_new",true)
 override fun showTouchLine()=p.getBoolean("touch_line",true)
 override fun showMeasureNumbers()=p.getBoolean("measure_numbers",true)
 override fun scoreScale()=p.getFloat("score_scale",1f)
 override fun editScale()=p.getFloat("edit_scale",1f)
 fun setPromptTone(v:Boolean)=p.edit().putBoolean("prompt_tone",v).apply()
 fun setAutoScroll(v:Boolean)=p.edit().putBoolean("auto_scroll",v).apply()
 fun setHighlightNewElements(v:Boolean)=p.edit().putBoolean("highlight_new",v).apply()
 fun setShowTouchLine(v:Boolean)=p.edit().putBoolean("touch_line",v).apply()
 fun setShowMeasureNumbers(v:Boolean)=p.edit().putBoolean("measure_numbers",v).apply()
 fun setScoreScale(v:Float)=p.edit().putFloat("score_scale",v.coerceIn(.5f,3f)).apply()
 fun setEditScale(v:Float)=p.edit().putFloat("edit_scale",v.coerceIn(.5f,3f)).apply()
 fun setRestoreSamples(v:Boolean)=p.edit().putBoolean("restore_samples",v).apply()
 fun restoreSamplesEnabled()=p.getBoolean("restore_samples",false)
}
