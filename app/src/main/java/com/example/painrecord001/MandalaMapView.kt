package com.example.painrecord001

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
class MandalaMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    data class Point(
        val pain: Int,
        val movement: Int,
        val label: String,
        val color: Int,
        val isLatest: Boolean = false
    )

    private data class PositionedPoint(
        val point: Point,
        val x: Float,
        val y: Float
    )

    private val cellSize = 52f * resources.displayMetrics.density
    private val pointRadius = 11f * resources.displayMetrics.density
    private val points = mutableListOf<Point>()
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(213, 217, 222)
        strokeWidth = 1f * resources.displayMetrics.density
        style = Paint.Style.STROKE
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(90, 32, 36, 42)
        strokeWidth = 1.5f * resources.displayMetrics.density
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(32, 36, 42)
        textAlign = Paint.Align.CENTER
        textSize = 13f * resources.displayMetrics.scaledDensity
    }
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val pointTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 11f * resources.displayMetrics.scaledDensity
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    private val latestStarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(245, 184, 32)
        textAlign = Paint.Align.CENTER
        textSize = 24f * resources.displayMetrics.scaledDensity
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    fun setPoints(newPoints: List<Point>) {
        points.clear()
        points.addAll(newPoints)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = (cellSize * 7).toInt()
        setMeasuredDimension(
            resolveSize(size, widthMeasureSpec),
            resolveSize(size, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
        drawProgressLine(canvas)
        drawPoints(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        for (row in 0 until 7) {
            for (column in 0 until 7) {
                val left = column * cellSize
                val top = row * cellSize
                canvas.drawRect(RectF(left, top, left + cellSize, top + cellSize), gridPaint)
                val label = when {
                    row == 0 && column > 0 -> "痛$column"
                    column == 0 && row > 0 -> "動$row"
                    else -> ""
                }
                if (label.isNotEmpty()) {
                    drawCenteredText(canvas, label, left + cellSize / 2, top + cellSize / 2, textPaint)
                }
            }
        }
    }

    private fun drawProgressLine(canvas: Canvas) {
        if (points.size < 2) return

        val orderedPoints = points.asReversed()
        for (index in 0 until orderedPoints.lastIndex) {
            val start = centerOf(orderedPoints[index])
            val end = centerOf(orderedPoints[index + 1])
            canvas.drawLine(start.first, start.second, end.first, end.second, linePaint)
        }
    }

    private fun drawPoints(canvas: Canvas) {
        val positionedPoints = buildPositionedPoints()

        for (positionedPoint in positionedPoints.filterNot { it.point.isLatest }) {
            pointPaint.color = positionedPoint.point.color
            canvas.drawCircle(positionedPoint.x, positionedPoint.y, pointRadius, pointPaint)
            drawCenteredText(
                canvas,
                positionedPoint.point.label,
                positionedPoint.x,
                positionedPoint.y,
                pointTextPaint
            )
        }

        for (positionedPoint in positionedPoints.filter { it.point.isLatest }) {
            drawCenteredText(
                canvas,
                positionedPoint.point.label,
                positionedPoint.x,
                positionedPoint.y,
                latestStarPaint
            )
        }
    }

    private fun buildPositionedPoints(): List<PositionedPoint> {
        val sameCellCounts = mutableMapOf<Pair<Int, Int>, Int>()
        return points.map { point ->
            val cellKey = point.pain to point.movement
            val count = sameCellCounts.getOrDefault(cellKey, 0)
            sameCellCounts[cellKey] = count + 1

            val center = centerOf(point)
            val offset = pointOffset(count, point.isLatest)
            val x = center.first + offset.first
            val y = center.second + offset.second

            PositionedPoint(point, x, y)
        }
    }

    private fun centerOf(point: Point): Pair<Float, Float> {
        return ((point.pain + 0.5f) * cellSize) to ((point.movement + 0.5f) * cellSize)
    }

    private fun pointOffset(index: Int, isLatest: Boolean): Pair<Float, Float> {
        val offset = 12f * resources.displayMetrics.density
        if (isLatest) return 0f to 0f

        return when (index % 5) {
            1 -> -offset to -offset
            2 -> offset to -offset
            3 -> -offset to offset
            4 -> offset to offset
            else -> offset to 0f
        }
    }

    private fun drawCenteredText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        val baseline = y - (paint.ascent() + paint.descent()) / 2
        canvas.drawText(text, x, baseline, paint)
    }
}
