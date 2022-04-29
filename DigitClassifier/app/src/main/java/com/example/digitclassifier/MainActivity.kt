package com.example.digitclassifier

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.divyanshu.draw.widget.DrawView

/**
 *
 */
class MainActivity : AppCompatActivity() {

    private var clearButton: Button? = null
    private var digitClassifier = DigitClassifier(this)
    private var drawView: DrawView? = null
    private var predictedTextView: TextView? = null

    companion object {
        private const val TAG = "MainActivity"
    }

    /**
     *
     */
    private fun classifyDrawing() {
        val bitmap = drawView?.getBitmap()
        if ((bitmap != null) && (digitClassifier.isInitialized)) {
            digitClassifier.classifyAsync(bitmap).addOnSuccessListener {
                    resultText -> predictedTextView?.text = resultText
            }.addOnFailureListener { e -> predictedTextView?.text = getString(
                R.string.tfe_dc_classification_error_message, e.localizedMessage)
                Log.e(TAG, "Error classifying drawing.", e)
            }
        }
    }

    /**
     *
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tfe_dc_activity_main)
        // setup view instances
        drawView = findViewById(R.id.draw_view)
        drawView?.setStrokeWidth(70.0f)
        drawView?.setColor(Color.WHITE)
        drawView?.setBackgroundColor(Color.BLACK)
        clearButton = findViewById(R.id.clear_button)
        predictedTextView = findViewById(R.id.predicted_text)
        // setup clear drawing button
        clearButton?.setOnClickListener {
            drawView?.clearCanvas()
            predictedTextView?.text = getString(R.string.tfe_dc_prediction_text_placeholder)
        }
        // setup classification trigger so that it classify after every stroke drew
        drawView?.setOnTouchListener { _, event ->
            // as we have interrupted DrawView's touch event,
            // we first need to pass touch events through to the instance for the drawing to show up
            drawView?.onTouchEvent(event)
            // then if user finished a touch event, run classification
            if (event.action == MotionEvent.ACTION_UP) {
                classifyDrawing()
            }
            true
        }
        // setup digit classifier
        digitClassifier
            .initialize()
            .addOnFailureListener {
                    e -> Log.e(TAG, "Error to setting up digit classifier.", e)
            }
    }

    /**
     *
     */
    override fun onDestroy() {
        digitClassifier.close()
        super.onDestroy()
    }
}