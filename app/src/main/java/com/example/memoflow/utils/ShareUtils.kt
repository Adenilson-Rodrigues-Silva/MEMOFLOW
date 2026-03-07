package com.example.memoflow.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Html
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    fun shareBitmap(context: Context, bitmap: Bitmap, title: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "memo_card.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Memória"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun shareRichPdf(
        context: Context, 
        title: String, 
        contentHtml: String, 
        date: String, 
        humor: String,
        imageUris: List<String>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points
            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            
            val margin = 50f
            val contentWidth = pageWidth - (margin * 2)
            var currentY = 70f

            // Função para desenhar fundo e borda em cada página
            fun drawBackground(canv: Canvas) {
                // Fundo branco limpo
                canv.drawColor(android.graphics.Color.WHITE)
                
                // Borda dupla elegante (Estilo MemoFlow)
                borderPaint.style = Paint.Style.STROKE
                
                // Borda externa mais grossa (Cyan)
                borderPaint.color = android.graphics.Color.parseColor("#00E5FF")
                borderPaint.strokeWidth = 4f
                canv.drawRect(15f, 15f, pageWidth - 15f, pageHeight - 15f, borderPaint)
                
                // Borda interna mais fina (Purple)
                borderPaint.color = android.graphics.Color.parseColor("#D500F9")
                borderPaint.strokeWidth = 1.5f
                canv.drawRect(22f, 22f, pageWidth - 22f, pageHeight - 22f, borderPaint)
            }

            drawBackground(canvas)

            // 1. Cabeçalho (Título)
            textPaint.apply {
                color = android.graphics.Color.BLACK
                textSize = 26f
                isFakeBoldText = true
            }
            canvas.drawText(if (title.isEmpty()) "Minha Memória" else title, margin, currentY, textPaint)
            currentY += 30f

            // 2. Data e Humor (Menor e Cinza)
            textPaint.apply {
                textSize = 12f
                isFakeBoldText = false
                color = android.graphics.Color.GRAY
            }
            canvas.drawText("$date  |  Humor: $humor", margin, currentY, textPaint)
            currentY += 15f
            
            // Linha divisória
            val linePaint = Paint().apply { 
                color = android.graphics.Color.LTGRAY
                strokeWidth = 1f
            }
            canvas.drawLine(margin, currentY, pageWidth - margin, currentY, linePaint)
            currentY += 35f

            // 3. Conteúdo (Interpretando HTML do Rich Text)
            val richText = Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_COMPACT)
            textPaint.apply {
                textSize = 15f
                color = android.graphics.Color.DKGRAY
            }

            val staticLayout = StaticLayout.Builder.obtain(richText, 0, richText.length, textPaint, contentWidth.toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1.3f)
                .build()

            canvas.save()
            canvas.translate(margin, currentY)
            staticLayout.draw(canvas)
            canvas.restore()
            
            currentY += staticLayout.height + 40f

            // 4. Fotos (Compactas em Grid/Linha)
            if (imageUris.isNotEmpty()) {
                val imageLoader = ImageLoader(context)
                val imgSize = (contentWidth - 20) / 3 // 3 fotos por linha
                var currentX = margin

                for (uri in imageUris) {
                    val request = ImageRequest.Builder(context)
                        .data(uri)
                        .allowHardware(false)
                        .build()
                    
                    val result = imageLoader.execute(request)
                    if (result is SuccessResult) {
                        val drawable = result.drawable
                        val bitmap = (drawable as android.graphics.drawable.BitmapDrawable).bitmap
                        
                        // Verifica se precisa de nova página
                        if (currentY + imgSize > pageHeight - 80) {
                            pdfDocument.finishPage(page)
                            currentPageNumber++
                            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            drawBackground(canvas)
                            currentY = 60f
                            currentX = margin
                        }

                        // Desenha a imagem mantendo a proporção dentro do quadrado
                        val scale = Math.min(imgSize / bitmap.width, imgSize / bitmap.height)
                        val drawW = bitmap.width * scale
                        val drawH = bitmap.height * scale
                        val offsetX = (imgSize - drawW) / 2
                        val offsetY = (imgSize - drawH) / 2

                        val destRect = android.graphics.RectF(
                            currentX + offsetX, 
                            currentY + offsetY, 
                            currentX + offsetX + drawW, 
                            currentY + offsetY + drawH
                        )
                        
                        // Borda sutil na foto
                        val imgBorderPaint = Paint().apply {
                            color = android.graphics.Color.LTGRAY
                            style = Paint.Style.STROKE
                            strokeWidth = 1f
                        }
                        canvas.drawRect(currentX, currentY, currentX + imgSize, currentY + imgSize, imgBorderPaint)
                        canvas.drawBitmap(bitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
                        
                        currentX += imgSize + 10f
                        if (currentX + imgSize > pageWidth - margin) {
                            currentX = margin
                            currentY += imgSize + 10f
                        }
                    }
                }
                if (currentX != margin) currentY += imgSize + 20f
            }

            // 5. Rodapé em todas as páginas (opcional, aqui na última)
            textPaint.textSize = 9f
            textPaint.color = android.graphics.Color.LTGRAY
            canvas.drawText("MemoFlow - Registrando cada detalhe da sua jornada.", margin, pageHeight - 40f, textPaint)

            pdfDocument.finishPage(page)

            val cachePath = File(context.cacheDir, "documents")
            cachePath.mkdirs()
            val file = File(cachePath, "memoria_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "application/pdf"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar PDF"))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
