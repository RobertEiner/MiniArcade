package com.example.miniarcade

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import kotlin.random.Random


class Hangman(context: Context, private val screenWidth: Int, private val screenHeight: Int) : SurfaceView(context), SurfaceHolder.Callback {


    // Sound pool and IDs for sounds
    private val soundAttributes: AudioAttributes
    private val soundPool: SoundPool
    private var successSound: Int
    private var failSound: Int

    private var goBackBitmap: Bitmap
    private var playAgainBitmap: Bitmap

    // custom game font
    val customTypeface = ResourcesCompat.getFont(context, R.font.silkscreenregular)


    // Rect for choosing difficulty
    private val DIFFICULTY_RECT_LENGTH = screenWidth / 2f

    private val DIFFICULTY_RECT_TOP = screenHeight / 4f
    private val DIFFICULTY_RECT_HEIGHT = screenHeight / 3f
    private val DIFFICULTY_RECT_BOTTOM = DIFFICULTY_RECT_HEIGHT + DIFFICULTY_RECT_TOP
    private val DIFFICULTY_RECT_LEFT = screenWidth / 4f
    // Easy rect dimensions
    private val INNER_DIFFICULTY_LENGTH = DIFFICULTY_RECT_LENGTH / 2f
    private val INNER_DIFFICULTY_HEIGHT = DIFFICULTY_RECT_HEIGHT / 5f
    private val SPACE_BETWEEN_RECTS = INNER_DIFFICULTY_HEIGHT / 2
    private val EASY_RECT_LEFT = DIFFICULTY_RECT_LEFT + (DIFFICULTY_RECT_LENGTH / 4)
    private val EASY_RECT_RIGHT = EASY_RECT_LEFT + INNER_DIFFICULTY_LENGTH
    private val EASY_RECT_TOP = DIFFICULTY_RECT_TOP + (INNER_DIFFICULTY_HEIGHT / 2)
    private val EASY_RECT_BOTTOM = EASY_RECT_TOP + INNER_DIFFICULTY_HEIGHT
    // Medium rect dimensions
    private val MEDIUM_RECT_TOP = EASY_RECT_TOP + INNER_DIFFICULTY_HEIGHT + SPACE_BETWEEN_RECTS
    private val MEDIUM_RECT_BOTTOM = EASY_RECT_TOP + (INNER_DIFFICULTY_HEIGHT * 2) + SPACE_BETWEEN_RECTS
    // Hard rect dimensions
    private val HARD_RECT_TOP = MEDIUM_RECT_TOP + INNER_DIFFICULTY_HEIGHT + SPACE_BETWEEN_RECTS
    private val HARD_RECT_BOTTOM = MEDIUM_RECT_TOP + (INNER_DIFFICULTY_HEIGHT * 2) + SPACE_BETWEEN_RECTS

    private val surfaceHolder: SurfaceHolder = holder
    private lateinit var myCanvas: Canvas
    // Lines for letters dimensions
    private val LINE_LENGTH = (screenWidth / 10f) * 1.8f

    // Booleans
    private var difficultyIsChosen: Boolean = false
    private var difficulty: String = ""
    private var gameOver = false
    private var gameWon = false
    // Rect objects
    private val chooseDifficultyRect: RectF
    private val easyRect: RectF
    private val mediumRect: RectF
    private val hardRect: RectF
    private val gameFinishedRect: RectF
    private val backRect: RectF
    private val playAgainRect: RectF
    // Paint objects
    private val difficultyPaint: Paint = Paint()
    private val titlePaint: Paint = Paint()
    private val innerDifficultyPaint: Paint = Paint()
    private val innerDifficultyTextPaint: Paint = Paint()
    private val linePaint: Paint = Paint()
    private val wrongGuessPaint: Paint = Paint()
    private val rightGuessPaint: Paint = Paint()
    private val gameOverTextPaint: Paint = Paint()
    private val gameOverRectPaint: Paint = Paint()
    private val playAgainRectPaint: Paint = Paint()
    private val playAgainTextPaint: Paint = Paint()

    // collections
    private val arrayOfWords = arrayOf("apple", "phone", "zebra", "table", "watch", "alien", "eagle", "yeast")

    private val radius: Float = 40f
    private val hangmanCoordinates = arrayOf(
        listOf(screenWidth / 3f, screenHeight / 2f, screenWidth / 3f, screenHeight / 5f), // coordinates of first line
        listOf(screenWidth / 3f,  screenHeight / 5f, screenWidth / 1.5f, screenHeight / 5f), // coordinates of second line
        listOf(screenWidth / 1.5f, screenHeight / 5f, screenWidth / 1.5f, screenHeight / 3.5f, linePaint), // coordinates of third line
        listOf(screenWidth / 1.5f, (screenHeight / 3.5f) + radius, radius), // coordinates of head
        listOf(screenWidth / 1.5f, (screenHeight / 3.5f) + (radius * 2f), screenWidth / 1.5f, screenHeight / 2.7f), // coordinates of fifth line
        listOf(screenWidth / 1.5f, screenHeight / 2.7f, (screenWidth / 1.5f) + 40f, screenHeight / 2.5f ), // coordinates of sixth line circle leg
        listOf(screenWidth / 1.5f, screenHeight / 2.7f, (screenWidth / 1.5f) - 40f, screenHeight / 2.5f), // coordinates of seventh line leg
        listOf(screenWidth / 1.6f, screenHeight / 2.9f, screenWidth / 1.4f, screenHeight / 2.9f), // coordinates of eighth line
        listOf(screenWidth / 4.7f,  screenHeight / 2f, screenWidth / 2.7f + (LINE_LENGTH / 2),  screenHeight / 2f), // bottom
        listOf(screenWidth / 3f,  screenHeight / 4f, screenWidth / 2.4f , screenHeight / 5f), // corner
        listOf((screenWidth / 1.5f) - 40f,  screenHeight / 2.5f, (screenWidth / 1.5f) - 60f,  screenHeight / 2.5f), // left foot
        listOf((screenWidth / 1.5f) + 40f, screenHeight / 2.5f, (screenWidth / 1.5f) + 60f,  screenHeight / 2.5f), // right foot
    )

    // the word to guess
    private var wordToGuess: String = ""
    private var currentProgress = arrayOf(' ', ' ', ' ', ' ', ' ')
    private val wrongGuesses = arrayListOf<Char>()
    private var numOfWrongGuesses = 0
    private var guessesAllowed = 0

    init {
        initializePaint()
        surfaceHolder.addCallback(this)
        // rects
        chooseDifficultyRect = RectF(DIFFICULTY_RECT_LEFT, DIFFICULTY_RECT_TOP,DIFFICULTY_RECT_LEFT + DIFFICULTY_RECT_LENGTH, DIFFICULTY_RECT_BOTTOM)
        easyRect = RectF(EASY_RECT_LEFT, EASY_RECT_TOP, EASY_RECT_RIGHT, EASY_RECT_BOTTOM)
        mediumRect = RectF(EASY_RECT_LEFT, MEDIUM_RECT_TOP, EASY_RECT_RIGHT, MEDIUM_RECT_BOTTOM)
        hardRect = RectF(EASY_RECT_LEFT, HARD_RECT_TOP, EASY_RECT_RIGHT, HARD_RECT_BOTTOM)
        gameFinishedRect = RectF(DIFFICULTY_RECT_LEFT, screenHeight / 1.25f,  DIFFICULTY_RECT_LEFT + DIFFICULTY_RECT_LENGTH,screenHeight / 1.05f)
        playAgainRect = RectF(DIFFICULTY_RECT_LEFT + (gameFinishedRect.width() / 3f), gameFinishedRect.top + (gameFinishedRect.height() / 2.5f),  DIFFICULTY_RECT_LEFT + DIFFICULTY_RECT_LENGTH - (gameFinishedRect.width() / 3f),screenHeight / 1.08f)
        backRect = RectF(screenWidth / 20f, screenHeight / 30f, screenWidth / 5f, screenHeight / 11f)

        isFocusable = true
        requestFocus()
        // bitmap
        playAgainBitmap = BitmapFactory.decodeResource(resources, R.drawable.hangmanrestart)
        goBackBitmap = BitmapFactory.decodeResource(resources, R.drawable.hangmanleftarrow)
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
        titlePaint.color = Color.argb(255, 255, 255, 255)
        titlePaint.textSize = 90f
        titlePaint.typeface = customTypeface
        difficultyPaint.color = ContextCompat.getColor(context, R.color.difficultyPurple)
        innerDifficultyPaint.color = Color.argb(255, 255, 255, 255)
        innerDifficultyTextPaint.color = Color.argb(255, 100, 100, 150)
        innerDifficultyTextPaint.textSize = 50f
        innerDifficultyTextPaint.typeface = customTypeface
        linePaint.strokeWidth = 10f
        linePaint.color = Color.argb(255, 255, 255, 255)
        linePaint.textSize = 100f
        linePaint.style = Paint.Style.STROKE
        wrongGuessPaint.textSize = 50f
        wrongGuessPaint.color = Color.argb(255, 255, 255, 255)
        wrongGuessPaint.typeface = customTypeface
        rightGuessPaint.textSize = 100f
        rightGuessPaint.color = Color.argb(255, 255, 255, 255)
        rightGuessPaint.typeface = customTypeface
        gameOverTextPaint.color = ContextCompat.getColor(context, R.color.purple)
        gameOverTextPaint.textSize = 70f
        gameOverTextPaint.typeface = customTypeface
        gameOverRectPaint.color = Color.argb(255, 255, 255 ,255)
        playAgainRectPaint.color = Color.argb(255, 255, 255 ,255)
        playAgainTextPaint.color = Color.argb(255, 255, 255 ,255)
        playAgainTextPaint.textSize = 50f
        playAgainTextPaint.typeface = customTypeface
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
        myCanvas.drawColor(ContextCompat.getColor(context, R.color.purple))
        myCanvas.drawText("Hangman", screenWidth / 3.5f, screenHeight / 10f, titlePaint)
        myCanvas.drawBitmap(goBackBitmap, null, backRect, playAgainTextPaint)


        if(!difficultyIsChosen) {
            myCanvas.drawRoundRect(chooseDifficultyRect, 20f, 20f, difficultyPaint)
            myCanvas.drawRoundRect(easyRect, 20f, 20f, innerDifficultyPaint)
            myCanvas.drawRoundRect(mediumRect, 20f, 20f, innerDifficultyPaint)
            myCanvas.drawRoundRect(hardRect, 20f, 20f, innerDifficultyPaint)
            myCanvas.drawText("Easy", EASY_RECT_LEFT + INNER_DIFFICULTY_LENGTH / 4f, EASY_RECT_TOP +  (INNER_DIFFICULTY_HEIGHT / 1.7f), innerDifficultyTextPaint)
            myCanvas.drawText("Medium", EASY_RECT_LEFT + INNER_DIFFICULTY_LENGTH / 7.8f, MEDIUM_RECT_TOP +  (INNER_DIFFICULTY_HEIGHT / 1.7f), innerDifficultyTextPaint)
            myCanvas.drawText("Hard", EASY_RECT_LEFT + INNER_DIFFICULTY_LENGTH / 4f, HARD_RECT_TOP +  (INNER_DIFFICULTY_HEIGHT / 1.7f), innerDifficultyTextPaint)
        } else {
            // draw lines for letters
            var offset = 0f
            for(i in 0 until 5) {
                myCanvas.drawLine((screenWidth / 13f) + offset, screenHeight / 1.5f, LINE_LENGTH + offset, screenHeight / 1.5f, linePaint)
                offset += LINE_LENGTH
            }
            // draw correctly guessed letters
            var charOffset: Float
            Log.d("progress in draw:", "progresss ${currentProgress}")
            for(i in currentProgress.indices) {
                if(currentProgress[i] != ' ') {
                    charOffset = LINE_LENGTH * i
                    myCanvas.drawText(currentProgress[i].toString(), (screenWidth / 13f) + charOffset, (screenHeight / 1.53f), rightGuessPaint )
                }
            }
            // draw wrong guessed letters
            var wrongCharOffset = 0
            for(i in 0 until wrongGuesses.size) {
                myCanvas.drawText("${wrongGuesses[i]},", (screenWidth / 13f) + wrongCharOffset, (screenHeight / 1.4f), wrongGuessPaint)
                wrongCharOffset += screenWidth / 15
            }

            // draw hangman
            for(i in 0 until numOfWrongGuesses) {
                if(i == 3) {
                    myCanvas.drawCircle(hangmanCoordinates[i].get(0) as Float, hangmanCoordinates[i].get(1) as Float, hangmanCoordinates[i].get(2) as Float, linePaint)
                } else {
                    myCanvas.drawLine(hangmanCoordinates[i].get(0) as Float, hangmanCoordinates[i].get(1) as Float,  hangmanCoordinates[i].get(2) as Float,  hangmanCoordinates[i].get(3) as Float, linePaint)
                }
            }

            myCanvas.drawText("$numOfWrongGuesses/$guessesAllowed", screenWidth / 2.3f, screenHeight /1.3f, wrongGuessPaint)

            if(gameOver) {
                soundPool.play(failSound, 1f, 1f, 1, 0, 1f)
                myCanvas.drawRoundRect(gameFinishedRect, 20f, 20f, gameOverRectPaint)
                myCanvas.drawRoundRect(playAgainRect, 20f, 20f, playAgainRectPaint)
                myCanvas.drawBitmap(playAgainBitmap, null, playAgainRect, playAgainRectPaint)
                myCanvas.drawText("Game Over!", gameFinishedRect.left + (gameFinishedRect.width() / 13f), gameFinishedRect.top + (gameFinishedRect.height() / 3.5f), gameOverTextPaint)
               // myCanvas.drawText("Play again", playAgainRect.left + (playAgainRect.width() / 10f), playAgainRect.top + (playAgainRect.height() / 1.5f), playAgainTextPaint)
            }
            if(gameWon) {
                soundPool.play(successSound, 1f, 1f, 1, 0, 1f)
                myCanvas.drawRoundRect(gameFinishedRect, 20f, 20f, gameOverRectPaint)
                myCanvas.drawRoundRect(playAgainRect, 20f, 20f, playAgainRectPaint)
                myCanvas.drawBitmap(playAgainBitmap, null, playAgainRect, playAgainRectPaint)

                myCanvas.drawText("Great Job!", gameFinishedRect.left + (gameFinishedRect.width() / 13f), gameFinishedRect.top + (gameFinishedRect.height() / 3.5f), gameOverTextPaint)
                //myCanvas.drawText("Play again", playAgainRect.left + (playAgainRect.width() / 10f), playAgainRect.top + (playAgainRect.height() / 1.5f), playAgainTextPaint)

            }
        }
        surfaceHolder.unlockCanvasAndPost(myCanvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if(easyRect.contains(event.x, event.y) && !difficultyIsChosen) {
                    setDifficulty("Easy")
                }
                if(mediumRect.contains(event.x, event.y) && !difficultyIsChosen) {
                    setDifficulty("Medium")
                }
                if(hardRect.contains(event.x, event.y) && !difficultyIsChosen) {
                    setDifficulty("Hard")
                }
                if(playAgainRect.contains(event.x, event.y) && (gameWon || gameOver)) {
                    playAgain()
                }
                if(backRect.contains(event.x, event.y)) {
                    goBackToMain()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val pressedChar = event?.displayLabel?.toString() ?: ""
        checkIfGuessWasCorrect(pressedChar)
        draw()
        return super.onKeyDown(keyCode, event)
    }

    private fun checkIfGuessWasCorrect(pressedChar: String) {
        if(wordToGuess.contains(pressedChar, true) && !gameOver && !gameWon) {
            val indices = wordToGuess.indices.filter {
                wordToGuess[it] == pressedChar.lowercase().singleOrNull()
            }
            for(i in indices.indices) {
                currentProgress[indices[i]] = pressedChar.singleOrNull()!!
            }
            // check if word is guessed correctly
            if(checkGuesses()) {
                gameWon = true
            }

            //Log.d("progress:", "progresss ${currentProgress.contentToString()}")
        } else {
            // "it" is referring to pressedChar
            if(!wrongGuesses.contains(pressedChar.singleOrNull()) && !gameOver && !gameWon) {
                pressedChar.singleOrNull()?.let { wrongGuesses.add(it) }
                numOfWrongGuesses++
                if(numOfWrongGuesses == 12 && difficulty.equals("Easy")) {
                    gameOver = true
                }
                if(numOfWrongGuesses == 10 && difficulty.equals("Medium")) {
                    gameOver = true
                }
                if(numOfWrongGuesses == 8 && difficulty.equals("Hard")) {
                    gameOver = true
                }

            }
        }
    }

    private fun playAgain() {
        difficultyIsChosen= false
        difficulty = ""
        gameOver = false
        gameWon = false
        currentProgress = arrayOf(' ', ' ', ' ', ' ', ' ')
        wrongGuesses.clear()
        numOfWrongGuesses = 0
        draw()
    }

    private fun setDifficulty(difficulty: String) {
        this.difficulty = difficulty
        this.difficultyIsChosen = true
        pickRandomWord(difficulty)
        guessesAllowed = if(difficulty.equals("Easy")) {
            12
        } else if(difficulty.equals("Medium")) {
            10
        } else {
            8
        }
        draw()
    }

    private fun pickRandomWord(difficulty: String) {
        wordToGuess = arrayOfWords[Random.nextInt(arrayOfWords.size)]
        Log.d(wordToGuess, "")
    }

    private fun checkGuesses(): Boolean {
        Log.d("$wordToGuess", "${currentProgress}")
        for(i in wordToGuess.indices){
            if(wordToGuess[i] != currentProgress[i].lowercaseChar()) {
                Log.d("${wordToGuess[i]}", "${currentProgress[i]}")

                return false

            }
        }
        return true;
    }

    private fun goBackToMain() {
        Intent(context, MainActivity::class.java).also {
            context.startActivity(it)
        }
    }
}