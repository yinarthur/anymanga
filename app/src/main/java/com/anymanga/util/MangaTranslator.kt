package com.anymanga.util

import android.graphics.Bitmap
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class TranslatedText(
    val originalText: String,
    val translatedText: String,
    val bounds: android.graphics.Rect
)

class MangaTranslator {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    private val translatorOptions = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH) // Default source
        .setTargetLanguage(TranslateLanguage.ARABIC)
        .build()
    
    private val translator = Translation.getClient(translatorOptions)

    suspend fun translateBitmap(bitmap: Bitmap): List<TranslatedText> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val textResult = recognizer.process(image).await()
        
        // Ensure translator is downloaded
        translator.downloadModelIfNeeded().await()
        
        val translatedList = mutableListOf<TranslatedText>()
        
        for (block in textResult.textBlocks) {
            val originalText = block.text
            val translatedText = translator.translate(originalText).await()
            val bounds = block.boundingBox ?: continue
            
            translatedList.add(
                TranslatedText(originalText, translatedText, bounds)
            )
        }
        
        return translatedList
    }
}
