package com.example.miniarcade

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.res.ResourcesCompat

class TicTacToe(context: Context, private var screenWidth: Int, private var screenHeight: Int) : SurfaceView(context), SurfaceHolder.Callback {
    private  var surfaceHolder: SurfaceHolder
    private lateinit var myCanvas: Canvas
    private lateinit var gameRectPaint: Paint
    private lateinit var linePaint: Paint
    private lateinit var circlePaint: Paint
    private lateinit var textPaint: Paint
    private lateinit var endOfGameMessagePaint: Paint
    private lateinit var playerOneRectPaint: Paint
    private lateinit var playerTwoRectPaint: Paint
    private lateinit var playAgainPaint: Paint
    private lateinit var titlePaint: Paint

    private var goBackBitmap: Bitmap
    private var playAgainBitmap: Bitmap


    private var gameRect: RectF
    private var goBackRect: RectF
    //private var playAgainRect: RectF
    private var playerOneRect: RectF
    private var playerTwoRect: RectF
    private var restartGameRect: RectF


    private val occupiedRects = arrayOf(
        charArrayOf(' ', ' ', ' '),
        charArrayOf(' ', ' ', ' '),
        charArrayOf(' ', ' ', ' '))
    private var gameBoardFull = false
    private val THREE_IN_A_ROW = 3

    // set font
    val customTypeface = ResourcesCompat.getFont(context, R.font.silkscreenregular)


    private val RECT_LEFT_SIDE: Float = screenWidth/10f
    private val RECT_RIGHT_SIDE: Float = screenWidth - RECT_LEFT_SIDE
    private val RECT_TOP: Float = screenHeight/10f
    private val RECT_BOTTOM: Float = screenHeight/2f
    private val LEFT_VERTICAL_LINE_X = ((RECT_RIGHT_SIDE - RECT_LEFT_SIDE) / 3) + RECT_LEFT_SIDE
    private val RIGHT_VERTICAL_LINE_X = LEFT_VERTICAL_LINE_X + ((RECT_RIGHT_SIDE - RECT_LEFT_SIDE) / 3)
    private val TOP_HORIZONTAL_LINE_Y = ((RECT_BOTTOM - RECT_TOP) / 3) + RECT_TOP
    private val BOTTOM_HORIZONTAL_LINE_Y = TOP_HORIZONTAL_LINE_Y + ((RECT_BOTTOM - RECT_TOP) / 3)
    // coordinates
    private val THIRD_OF_SQUARE_X = (LEFT_VERTICAL_LINE_X - RECT_LEFT_SIDE) / 3
    private val THIRD_OF_SQUARE_Y = (TOP_HORIZONTAL_LINE_Y - RECT_TOP) / 3

    private val HALF_OF_SQUARE_X = (LEFT_VERTICAL_LINE_X - RECT_LEFT_SIDE) / 2
    private val HALF_OF_SQUARE_Y = (TOP_HORIZONTAL_LINE_Y - RECT_TOP) / 2


    private var playerOneTurn: Boolean = true
    private var playerOneWon: Boolean = false
    private var playerTwoWon: Boolean = false
    private var gameFinished: Boolean = false

    init {
        surfaceHolder = holder
        surfaceHolder.addCallback(this)
        initializePaint()

        restartGameRect = RectF(LEFT_VERTICAL_LINE_X + THIRD_OF_SQUARE_X - (THIRD_OF_SQUARE_X / 2), screenHeight / 1.3f, RIGHT_VERTICAL_LINE_X - THIRD_OF_SQUARE_X + (THIRD_OF_SQUARE_X / 2), screenHeight / 1.18f)
        gameRect = RectF(RECT_LEFT_SIDE, RECT_TOP, RECT_RIGHT_SIDE, RECT_BOTTOM)
        //playAgainRect = RectF(LEFT_VERTICAL_LINE_X, RECT_BOTTOM + HALF_OF_SQUARE_Y, RIGHT_VERTICAL_LINE_X, (RECT_BOTTOM + HALF_OF_SQUARE_Y) + HALF_OF_SQUARE_Y)
        playerOneRect = RectF(RECT_LEFT_SIDE, screenHeight - (screenHeight / 3f), LEFT_VERTICAL_LINE_X + THIRD_OF_SQUARE_X, screenHeight - (screenHeight / 3.8f))
        playerTwoRect = RectF(RIGHT_VERTICAL_LINE_X - THIRD_OF_SQUARE_Y, screenHeight - (screenHeight / 3f), RECT_RIGHT_SIDE, screenHeight - (screenHeight / 3.8f))
        goBackRect = RectF(screenWidth / 20f, RECT_TOP / 3.5f, screenWidth / 5.5f, RECT_TOP / 1.5f )

        playAgainBitmap = BitmapFactory.decodeResource(resources, R.drawable.refresh)
        goBackBitmap = BitmapFactory.decodeResource(resources, R.drawable.leftttt)

    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        Log.d(screenWidth.toString(), screenHeight.toString())
        draw()
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        //
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {

    }

    private fun initializePaint() {
        // title
        titlePaint = Paint()
        titlePaint.textSize = 70f
        titlePaint.strokeWidth = 20f
        titlePaint.color = Color.argb(255,92, 174, 245)
        titlePaint.typeface = customTypeface
        // circles
        circlePaint = Paint()
        circlePaint.color = Color.argb(255, 92, 174, 245)
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeWidth = 12f
        // gameboard
        gameRectPaint = Paint()
        gameRectPaint.color = Color.argb(255, 153, 247, 238)
        gameRectPaint.textSize = 50f
        //play again rect
        playAgainPaint = Paint()
        playAgainPaint.strokeWidth = 12f
        playAgainPaint.color = Color.argb(255, 190, 223, 253)
        endOfGameMessagePaint = Paint()
        endOfGameMessagePaint.color = Color.argb(255, 92, 174, 245)
        endOfGameMessagePaint.textSize = 50f
        endOfGameMessagePaint.typeface = customTypeface

        // game board lines
        linePaint = Paint()
        linePaint.color = Color.argb(255, 255, 255, 255)
        linePaint.strokeWidth = 12f
        // text paint
        textPaint = Paint()
        textPaint.color = Color.argb(255, 255, 255, 255)
        textPaint.textSize = 50f
        textPaint.typeface = customTypeface
        // paint for player rects
        playerOneRectPaint = Paint()
        playerOneRectPaint.color = Color.argb(255, 190, 223, 253)

        playerTwoRectPaint = Paint()
        playerTwoRectPaint.color = Color.argb(255, 190, 223, 253)

    }
    private fun draw() {
        myCanvas = surfaceHolder.lockCanvas()
        myCanvas.drawColor(Color.argb(255, 203, 255, 251))
        myCanvas.drawText("Tic Tac Toe", LEFT_VERTICAL_LINE_X / 1.5f, RECT_TOP / 1.8f, titlePaint)
        //myCanvas.drawRoundRect(backRect, 60f, 60f, playAgainPaint)
       // myCanvas.drawText("Back",  backRect.left + (backRect.width() / 3.3f) , backRect.top + (backRect.height() / 1.7f) , playAgainTextPaint )
        myCanvas.drawRoundRect(gameRect, 20f, 20f, gameRectPaint)
        myCanvas.drawBitmap(playAgainBitmap, null, restartGameRect, endOfGameMessagePaint)
        myCanvas.drawBitmap(goBackBitmap, null, goBackRect, endOfGameMessagePaint)


        // vertical left
        myCanvas.drawLine(LEFT_VERTICAL_LINE_X, RECT_TOP, LEFT_VERTICAL_LINE_X, RECT_BOTTOM, linePaint)
        // vertical right
        myCanvas.drawLine(RIGHT_VERTICAL_LINE_X, RECT_TOP, RIGHT_VERTICAL_LINE_X, RECT_BOTTOM, linePaint)
        //horizontal top
        myCanvas.drawLine(RECT_LEFT_SIDE, TOP_HORIZONTAL_LINE_Y, RECT_RIGHT_SIDE, TOP_HORIZONTAL_LINE_Y, linePaint)
        //horizontal bottom
        myCanvas.drawLine(RECT_LEFT_SIDE, BOTTOM_HORIZONTAL_LINE_Y, RECT_RIGHT_SIDE, BOTTOM_HORIZONTAL_LINE_Y, linePaint)

        // change color depending on who's turn it is
        if(playerOneTurn) {
            playerTwoRectPaint.color = Color.argb(255, 190, 223, 253)
            playerOneRectPaint.color = Color.argb(255, 92, 174, 245)
        } else {
            playerTwoRectPaint.color = Color.argb(255, 92  , 174, 245)
            playerOneRectPaint.color = Color.argb(255, 190, 223, 253)

        }

        if(playerOneWon) {
            playerOneRectPaint.color = Color.GREEN
            playerTwoRectPaint.color = Color.RED
            gameFinished = true
            myCanvas.drawText("Player one won!", RECT_LEFT_SIDE  + HALF_OF_SQUARE_X, RECT_BOTTOM + 100f, endOfGameMessagePaint )
        }
        if(playerTwoWon) {
            playerTwoRectPaint.color = Color.GREEN
            playerOneRectPaint.color = Color.RED
            gameFinished = true
            myCanvas.drawText("Player two won!", RECT_LEFT_SIDE  + HALF_OF_SQUARE_X , RECT_BOTTOM + 100f, endOfGameMessagePaint )
        }

        // player rects
        myCanvas.drawRoundRect(playerOneRect, 20f, 20f, playerOneRectPaint)
        myCanvas.drawRoundRect(playerTwoRect, 20f, 20f, playerTwoRectPaint)
        myCanvas.drawText("Player 1", playerOneRect.left + THIRD_OF_SQUARE_X / 2f, screenHeight - (screenHeight / 3.8f) - (playerOneRect.height() / 2) , textPaint )
        myCanvas.drawText("Player 2", playerTwoRect.left + THIRD_OF_SQUARE_X / 2f, screenHeight - (screenHeight / 3.8f) - (playerOneRect.height() / 2) , textPaint )

        if(gameBoardFull && !playerOneWon && !playerTwoWon) {
            myCanvas.drawText("It's a tie! Play again?", RECT_LEFT_SIDE + THIRD_OF_SQUARE_X, RECT_BOTTOM + 100f, endOfGameMessagePaint )

        }

        var yOffset = 1
        var xOffset = 1
        var circleXOffset = 1
        var circleYOffset = 1
        for(i in occupiedRects.indices) {
            for(j in 0 until occupiedRects[i].size) {

                if(occupiedRects[i][j] == 'X') {
                    myCanvas.drawLine(RECT_LEFT_SIDE + (THIRD_OF_SQUARE_X * xOffset), RECT_TOP + (THIRD_OF_SQUARE_Y * yOffset), RECT_LEFT_SIDE + (THIRD_OF_SQUARE_X * (xOffset + 1)),RECT_TOP + (THIRD_OF_SQUARE_Y * (yOffset + 1)), linePaint )
                    myCanvas.drawLine(RECT_LEFT_SIDE + (THIRD_OF_SQUARE_X * (xOffset + 1)), RECT_TOP + (THIRD_OF_SQUARE_Y * yOffset), RECT_LEFT_SIDE + (THIRD_OF_SQUARE_X * xOffset), RECT_TOP + (THIRD_OF_SQUARE_Y * (yOffset + 1)), linePaint )
                } else if(occupiedRects[i][j] == 'O') {
                    myCanvas.drawCircle((HALF_OF_SQUARE_X * circleXOffset) + RECT_LEFT_SIDE , (HALF_OF_SQUARE_Y * circleYOffset) + RECT_TOP , 60f, circlePaint)
                }
                circleXOffset += 2
                xOffset += 3
            }
            // circle
            circleXOffset = 1
            circleYOffset += 2
            // Cross
            xOffset = 1
            yOffset += 3
        }
        surfaceHolder.unlockCanvasAndPost(myCanvas)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event?.action) {
            ACTION_DOWN -> {
              if(touchWithinGameRect(event.x, event.y) && !gameFinished) {

                  // Top row
                  if((event.y < TOP_HORIZONTAL_LINE_Y && event.y > RECT_TOP) && (event.x < LEFT_VERTICAL_LINE_X && event.x > RECT_LEFT_SIDE)) {
                      if(occupiedRects[0][0] == ' ') {
                          if(playerOneTurn) {
                              occupiedRects[0][0] = 'X'
                          } else {
                              occupiedRects[0][0] = 'O'
                          }
                      }
                  }
                  if((event.y < TOP_HORIZONTAL_LINE_Y && event.y > RECT_TOP) && (event.x > LEFT_VERTICAL_LINE_X && event.x < RIGHT_VERTICAL_LINE_X)) {
                      if(occupiedRects[0][1] == ' ') {
                          if(playerOneTurn) {
                              occupiedRects[0][1] = 'X'
                          } else {
                              occupiedRects[0][1] = 'O'
                          }
                      }
                  }
                  if((event.y < TOP_HORIZONTAL_LINE_Y && event.y > RECT_TOP) && (event.x > RIGHT_VERTICAL_LINE_X && event.x < RECT_RIGHT_SIDE)) {
                      if(occupiedRects[0][2] == ' ') {
                          if(playerOneTurn) {
                              occupiedRects[0][2] = 'X'
                          } else {
                              occupiedRects[0][2] = 'O'
                          }
                      }
                  }

                  // Middle row
                  if((event.y < BOTTOM_HORIZONTAL_LINE_Y && event.y > TOP_HORIZONTAL_LINE_Y) && (event.x > RECT_LEFT_SIDE && event.x < LEFT_VERTICAL_LINE_X)) {
                      if(occupiedRects[1][0] == ' ') {
                          if(playerOneTurn) {
                              occupiedRects[1][0] = 'X'
                          } else {
                              occupiedRects[1][0] = 'O'
                          }
                      }

                  }
                  if((event.y < BOTTOM_HORIZONTAL_LINE_Y && event.y > TOP_HORIZONTAL_LINE_Y) && (event.x > LEFT_VERTICAL_LINE_X && event.x < RIGHT_VERTICAL_LINE_X)) {
                      if(occupiedRects[1][1] == ' ') {
                          if(playerOneTurn) {
                              occupiedRects[1][1] = 'X'
                          } else {
                              occupiedRects[1][1] = 'O'
                          }
                      }
                  }
                  if((event.y < BOTTOM_HORIZONTAL_LINE_Y && event.y > TOP_HORIZONTAL_LINE_Y) && (event.x > RIGHT_VERTICAL_LINE_X && event.x < RECT_RIGHT_SIDE)) {
                      if(occupiedRects[1][2] == ' ') {
                          if(playerOneTurn) {
                              occupiedRects[1][2] = 'X'
                          } else {
                              occupiedRects[1][2] = 'O'
                          }
                      }
                  }

                  // Bottom row
                  if((event.y < RECT_BOTTOM && event.y > BOTTOM_HORIZONTAL_LINE_Y) && (event.x > RECT_LEFT_SIDE && event.x < LEFT_VERTICAL_LINE_X)) {
                      if(occupiedRects[2][0] == ' ') {
                          if(playerOneTurn) {
                              occupiedRects[2][0] = 'X'
                          } else {
                              occupiedRects[2][0] = 'O'
                          }
                      }
                  }
                  if((event.y < RECT_BOTTOM && event.y > BOTTOM_HORIZONTAL_LINE_Y) && (event.x > LEFT_VERTICAL_LINE_X && event.x < RIGHT_VERTICAL_LINE_X)) {
                      if(occupiedRects[2][1] == ' ') {
                          if(playerOneTurn) {
                              occupiedRects[2][1] = 'X'
                          } else {
                              occupiedRects[2][1] = 'O'
                          }
                      }
                  }
                  if((event.y < RECT_BOTTOM && event.y > BOTTOM_HORIZONTAL_LINE_Y) && (event.x > RIGHT_VERTICAL_LINE_X && event.x < RECT_RIGHT_SIDE)) {
                      if(occupiedRects[2][2] == ' ') {
                          if(playerOneTurn) {
                              occupiedRects[2][2] = 'X'
                          } else {
                              occupiedRects[2][2] = 'O'
                          }
                      }
                  }

                  // change turn
                  playerOneTurn = !playerOneTurn
                  // check if board is full
                  gameBoardFull = checkIfBoardIsFull()
                  // check if someone won
                  if(checkIfThreeInARow('X')) {
                      playerOneWon = true
                  }
                  if(checkIfThreeInARow('O')) {
                      playerTwoWon = true
                  }
                  // draw graphics
                  draw()

              }
                if(restartGameRect.contains(event.x, event.y)) {
                    resetGame()
                    playerOneTurn = true
                    playerOneWon = false
                    playerTwoWon = false
                    gameFinished = false
                    gameBoardFull = false
                    draw()
                }
                if(goBackRect.contains(event.x, event.y)) {
                    Intent(context, MainActivity::class.java).also {
                        context.startActivity(it)
                    }
                }


            }
        }

        return super.onTouchEvent(event)
    }

    private fun touchWithinGameRect(x: Float, y: Float): Boolean {
        if(gameRect.contains(x, y)) {
            return true;
        }
        return false;
    }


   private fun checkIfBoardIsFull(): Boolean {
       for(i in occupiedRects.indices) {
           for(j in occupiedRects[i].indices) {
               if(occupiedRects[i][j] == ' ') {
                   return false
               }
           }
       }
       return true
   }

    private fun checkIfThreeInARow(playerSymbol: Char): Boolean {
        // check for three in a row horizontally
        for(i in occupiedRects.indices) {
            var count = 0
            for(j in occupiedRects[i].indices) {
                if(occupiedRects[i][j] == playerSymbol) {
                    count++
                }
            }
            if(count == THREE_IN_A_ROW) {
                return true
            }
        }
        // Check three in a row vertically
        val firstRow = 0
        var column = 0
        for(i in occupiedRects.indices) {
            if(occupiedRects[firstRow][column] == playerSymbol && occupiedRects[firstRow + 1][column] == playerSymbol && occupiedRects[firstRow + 2][column] == playerSymbol ) {
                return true
            }
            column++
        }

        // check if three in a row diagonally to the right
        column = 0
        for(i in occupiedRects.indices) {
            if(occupiedRects[firstRow][column] == playerSymbol && occupiedRects[firstRow + 1][column + 1] == playerSymbol && occupiedRects[firstRow + 2][column + 2] == playerSymbol ) {
                return true
            }
        }
        // check if three in a row diagonally to the left
        column = 0
        for(i in occupiedRects.indices) {
            if(occupiedRects[firstRow][column + 2] == playerSymbol && occupiedRects[firstRow + 1][column + 1] == playerSymbol && occupiedRects[firstRow + 2][column] == playerSymbol ) {
                return true
            }
        }
        return false
    }

    private fun resetGame() {
        for(i in occupiedRects.indices) {
            for(j in occupiedRects[i].indices) {
                occupiedRects[i][j] = ' '
            }
        }





    }








}