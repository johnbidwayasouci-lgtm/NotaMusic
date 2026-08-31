package org.notamusic.app.ui.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import org.notamusic.app.domain.model.*

class ScoreEditorView(context: Context): View(context) {
 private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
 var score=Score(parts=listOf(Part(name="Piano",staves=listOf(Staff(instrument=Instrument("piano","Piano"),measures=listOf(Measure(1)))))))
 override fun onDraw(c:Canvas){ super.onDraw(c); c.drawColor(0xfffafafa.toInt()); paint.color=0xff202020.toInt(); paint.textSize=32f; c.drawText(score.metadata.title,24f,48f,paint); paint.textSize=14f; var y=100f; repeat(5){c.drawLine(24f,y,width-24f,y,paint);y+=12f}; paint.textSize=14f; c.drawText("Measure 1   𝄞   4/4",32f,92f,paint); paint.textSize=18f; c.drawText("Editor foundation — notation engine to follow",32f,height-32f,paint) }
}
