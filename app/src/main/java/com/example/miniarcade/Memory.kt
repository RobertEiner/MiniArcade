package com.example.miniarcade

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.res.ResourcesCompat
import java.io.File

class Memory(context: Context, private val screenWidth: Int, private val screenHeight: Int) : SurfaceView(context), SurfaceHolder.Callback {

    private lateinit var myCanvas: Canvas
    private val surfaceHolder: SurfaceHolder = holder
    private var currentCoverBitmap1: Bitmap
    private var currentCoverBitmap2: Bitmap
    private lateinit var currentGuessedBitmap1: Bitmap
    private lateinit var currentGuessedBitmap2: Bitmap
    private var pointsBitmap: Bitmap
    private var playAgainBitmap: Bitmap
    private var goBackBitmap: Bitmap

    private val soundAttributes: AudioAttributes
    private val soundPool: SoundPool
    private var successSound: Int
    private var failSound: Int

    // booleans
    private var gameInit = true
    private var gameFinished = false
    private var guesses: Int = 0
    private var coverBitmapKey: String = ""
    private var coverBitmapKey2: String = ""

    private var coverResourceId: Int
    private var points: Int = 0
    private var correctGuesses: Int = 0

    private var startGameRect: RectF
    private var pointsRect: RectF
    private var playAgainRect: RectF
    private var goBackRect: RectF


    // collections
    private var resourceIDs = arrayOf(
        R.drawable.pinkegg,
        R.drawable.purplepanzar,
        R.drawable.euoplocephalus,
        R.drawable.redvelociraptor,
        R.drawable.orangehead,
        R.drawable.greenlongneck,
        R.drawable.yellowfootmark,
        R.drawable.tyrannosaurus
        )

    private var listOfRects: ArrayList<RectF> = ArrayList()
    private var mapOfBitmaps: MutableMap<String, Bitmap> = mutableMapOf()
    private var coverBitmaps: MutableMap<String, Bitmap> = mutableMapOf()
    private var bitmapsToCoverBitmaps: MutableMap<Bitmap, Bitmap> = mutableMapOf()
    private var rectsToBitmaps: MutableMap<RectF, Bitmap> = mutableMapOf()

    // constants
    private val IMAGE_LENGTH = screenWidth / 4.5f
    private val IMAGE_HEIGHT = screenHeight / 10f
    private val xMARGIN = screenWidth / 30
    private val yMARGIN = screenHeight / 70

    // paint
    private var bgPaint: Paint = Paint()
    private var titlePaint: Paint = Paint()
    private var restartPaint: Paint = Paint()

    // set font
    val customTypeface = ResourcesCompat.getFont(context, R.font.silkscreenregular)

    init {
        surfaceHolder.addCallback(this)
        initializePaint()
        coverResourceId = R.drawable.squaretwoshades

        Log.d(screenWidth.toString(), screenHeight.toString())

        // add rects to the list
        var xOffset = 0
        var yOffset = 0
        for(i in 0  until 16) {
            if(i % 4 == 0) {
                yOffset += 1
                xOffset = 0
            }
            listOfRects.add(RectF(
                screenWidth / 20f + (IMAGE_LENGTH * xOffset) + xMARGIN,
                screenHeight / 5f + (IMAGE_HEIGHT * yOffset) + yMARGIN,
                screenWidth / 20f + (IMAGE_LENGTH * xOffset) + IMAGE_LENGTH,
                screenHeight / 5f + IMAGE_HEIGHT + (IMAGE_HEIGHT * yOffset)))
            xOffset += 1
        }

        goBackBitmap = BitmapFactory.decodeResource(resources, R.drawable.arrowleft)
        playAgainBitmap =  BitmapFactory.decodeResource(resources, R.drawable.orangereplay)
        pointsBitmap = BitmapFactory.decodeResource(resources, R.drawable.star)
        currentCoverBitmap1 = BitmapFactory.decodeResource(resources, coverResourceId)
        currentCoverBitmap2 = BitmapFactory.decodeResource(resources, coverResourceId)

        // create maps of cover images and the guessing images
        initializeMaps()

        startGameRect = RectF()
        goBackRect = RectF(screenWidth / 20f, screenHeight / 30f, screenWidth / 5f, screenHeight / 11f)
        playAgainRect = RectF(screenWidth / 2.2f, screenHeight / 1.25f, screenWidth / 1.7f, screenHeight / 1.18f)
        pointsRect = RectF(screenWidth / 2.8f, screenHeight / 1.39f, screenWidth / 2.1f, screenHeight / 1.29f)


        soundAttributes = AudioAttributes.Builder()                     // Constructor
            .setUsage(AudioAttributes.USAGE_GAME)                       // USAGE_GAME for game audio
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)  // set content type of audio
            .build()                                                    // combine the attributes that have been set above, and return a new AudioAttributes object
        // build the sound pool
        soundPool = SoundPool.Builder()
            .setAudioAttributes(soundAttributes)                        // set the audio attributes for the soundpool
            .setMaxStreams(2)                                           // set the max num of streams that can be played simultaneously
            .build()
        successSound = soundPool.load(context, R.raw.success, 1)
        failSound = soundPool.load(context, R.raw.failuredrum, 1)

    }

    private fun initializePaint() {
        titlePaint.color = Color.argb(255, 255,145, 0)
        titlePaint.textSize = 70f
        titlePaint.typeface = customTypeface
        bgPaint.color = Color.argb(255, 200, 200, 200)
        restartPaint.color = Color.argb(255, 200, 200, 200)

    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        draw()

    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }

    private fun draw() {
        myCanvas = surfaceHolder.lockCanvas()

        myCanvas.drawColor(Color.argb(255, 253, 226, 172))
        myCanvas.drawText("Dinosaur Memory", screenWidth / 7f, screenHeight / 6.5f, titlePaint)
        myCanvas.drawBitmap(pointsBitmap, null, pointsRect, bgPaint)
 //       myCanvas.drawRoundRect(playAgainRect, 20f, 20f, restartPaint)
        myCanvas.drawBitmap(playAgainBitmap, null, playAgainRect, bgPaint)
        myCanvas.drawBitmap(goBackBitmap, null, goBackRect, bgPaint)

        myCanvas.drawText("$points", screenWidth / 2f, screenHeight / 1.3f, titlePaint)

        if (gameInit) {
            listOfRects.shuffle()
        }
        for (i in listOfRects.indices) {
            val currentBitmap = mapOfBitmaps.getValue((i + 1).toString())
            val currentCoverBitmap = coverBitmaps.getValue((i + 1).toString())
            myCanvas.drawBitmap(currentBitmap, null, listOfRects[i], bgPaint)


            if (currentCoverBitmap != currentCoverBitmap1 && currentCoverBitmap != currentCoverBitmap2) {
                myCanvas.drawBitmap(currentCoverBitmap, null, listOfRects[i], bgPaint)
            }
            if (gameInit) {
                bitmapsToCoverBitmaps.put(currentBitmap, currentCoverBitmap)
                rectsToBitmaps.put(listOfRects[i], currentBitmap)
            }
        }
        surfaceHolder.unlockCanvasAndPost(myCanvas)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if(goBackRect.contains(event.x, event.y)) {
                    goBackToMain()

                }
                if(playAgainRect.contains(event.x, event.y)) {
                    restartGame()
                }
                for(i in listOfRects.indices) {
                    if (listOfRects[i].contains(event.x, event.y)) {
                        val bitmap = rectsToBitmaps.get(listOfRects[i])
                        if(guesses == 0) {

                            currentCoverBitmap1 = bitmapsToCoverBitmaps.get(bitmap)!!
                            coverBitmapKey = (i + 1).toString()
                            guesses++
                            currentGuessedBitmap1 = bitmap!!
                            currentCoverBitmap2 = BitmapFactory.decodeResource(resources, coverResourceId)
                        } else if(guesses == 1) {
                            currentCoverBitmap2 = bitmapsToCoverBitmaps.get(bitmap)!!
                            guesses++
                            coverBitmapKey2 = (i + 1).toString()

                            currentGuessedBitmap2 = bitmap!!
                            if(currentGuessedBitmap1.sameAs(currentGuessedBitmap2)) {
                                points += 4
                                removeCoverImage(coverBitmapKey, coverBitmapKey2)
                                correctGuesses++
                                if(correctGuesses == 8) {
                                    soundPool.play(successSound, 1f, 1f, 1, 0, 1f)
                                }
                            } else {
                                points--
                                if(points < 0) {
                                    points = 0
                                }
                            }
                            guesses = 0
                        }
                    }
                }
            }
        }
        gameInit = false
        draw()
        return super.onTouchEvent(event)
    }

    private fun removeCoverImage(key: String, key2: String) {
        coverBitmaps.put(key, Bitmap.createBitmap(coverBitmaps.getValue(key).width, coverBitmaps.getValue(key).height, Bitmap.Config.ARGB_8888) )
        coverBitmaps.put(key2, Bitmap.createBitmap(coverBitmaps.getValue(key2).width, coverBitmaps.getValue(key2).height, Bitmap.Config.ARGB_8888) )

    }

    private fun restartGame() {
        gameInit = true
        points = 0
        guesses = 0
        initializeMaps()
        draw()
    }

    private fun goBackToMain() {
        Intent(context, MainActivity::class.java).also {
            context.startActivity(it)
        }
    }

    private fun initializeMaps() {
        var j = 0
        for(i in 0 until 16) {
            coverBitmaps.put((i + 1).toString(), BitmapFactory.decodeResource(resources, coverResourceId))
            mapOfBitmaps.put((i + 1).toString(), BitmapFactory.decodeResource(resources, resourceIDs[j]))
            j++
            if(j > 7) {
                j = 0
            }
        }
    }

}