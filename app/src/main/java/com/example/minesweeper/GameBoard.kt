package com.example.minesweeper

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList

class GameBoard : AppCompatActivity() {

    var buttons:ArrayList<Button> = ArrayList() /* arraylist to store all buttons on the board*/
    var cells:ArrayList<ArrayList<Cell>> = ArrayList() /* arraylist with cells corrosponding to each button*/
    var rows = -1 /* number of rows and columns which will be set when activity is started*/
    var mines = -1 /*number of mines will be set when activity is started*/
    var minesAt = mutableSetOf<Int>() /* a set containing positions of mines */
    val MINE=-1
    var flagMode=false
    var status=Status.ONGOING
    val grey = Color.GRAY
    val black = Color.BLACK
    var tilesRevealed=0 /*number of tiles revealed*/
    var started=false /* toggles to true when first button is clicked*/
    var flagsOnMines:Int=0 /*number of flags placed on mines*/
    var actualFlagsOnMines = 0
    var time:Int=0 /* time in seconds of current game*/
    var bestTime=Integer.MAX_VALUE /* stores best time if game is won*/
    var prevTime=-1 /*stores time of previous won game*/
    val BestTime = "BestTime"
    val PreviousTime="PrevTime"
    private var timer:Timer = Timer()
    lateinit var onBoardTime :TextView
    lateinit var minesOnBoard :TextView
    lateinit var board :LinearLayout /*The game board Linearlayout*/
    lateinit var toggleFlag :ImageButton
    lateinit var originalButtonBackground:Drawable

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_board)
        rows=intent.getIntExtra("rows",-1)
        mines=intent.getIntExtra("mines",-1)
        actualFlagsOnMines=mines
        flagsOnMines=mines
        board=findViewById(R.id.game_board)
        createBoard(board)

        val flag_button = getDrawable(R.drawable.flag_button)
        val bomb_button = getDrawable(R.drawable.bomb_button)
        val reset = findViewById<Button>(R.id.resetGame)
        toggleFlag = findViewById(R.id.toggle_flag)
        toggleFlag.isEnabled=false
        /*for toggling flag mode ON/OFF
        * disabled for the first click*/
        toggleFlag.setOnClickListener {
            flagMode=!flagMode
            if(flagMode){
                it.setBackground(flag_button)
            } else {
                it.setBackground(bomb_button)
            }
        }

        /*Resets the board with same number of rows and mines*/
        reset.setOnClickListener {
            board.removeAllViewsInLayout()
            buttons.clear()
            cells.clear()
            minesAt.clear()
            status=Status.ONGOING
            tilesRevealed=0
            started=false
            flagMode=false
            toggleFlag.isEnabled=false
            toggleFlag.setBackground(bomb_button)
            actualFlagsOnMines=mines
            flagsOnMines=mines
            minesOnBoard.text = flagsOnMines.toString()
//            time=0
            resetTimer()
            timer=Timer()
            createBoard(board)
        }
    }

    /*creates game board with buttons*/
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createBoard(board:LinearLayout){
        onBoardTime = findViewById<TextView>(R.id.time)
        minesOnBoard = findViewById(R.id.onBoardFlagsOnMines)
        minesOnBoard.text = flagsOnMines.toString()
        val flag = getDrawable(R.drawable.flag_icon)
        var id:Int = 0 /*unique id to give to each button*/
        val params1 = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f)
        val params2 = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1.0f)
        board.orientation = LinearLayout.VERTICAL

        for(i in 0 until rows){
            val buttonRow=LinearLayout(this)
            buttonRow.layoutParams=params2
            buttonRow.orientation=LinearLayout.HORIZONTAL

            for(j in 0 until rows){
                val button=Button(this)
                button.layoutParams=params1
                button.setPadding(1,1,1,1)
                button.id=id
                button.setTextColor(black)
//                button.setAutoSizeTextTypeUniformWithConfiguration(10,80,1,1)
//                button.textSize= button.scaleX
//                button.textSize= button.scaleY
//                button.text
//                button.setTextSize(button.autoSizeMaxTextSize.toFloat())
//                button.setTextSize((200/mines).toFloat())

                val original :Drawable = button.background
//                val x=1
                originalButtonBackground = original

                val rowcol= getRowCol(id)
                id++

                button.setOnClickListener {
                    /*since first one is a free click , mines are set after the first button click*/
                    if(!started) {
                        started = true
                        setValues(button)
                        toggleFlag.isEnabled=true
                        startTimer(onBoardTime)
//                        timer.scheduleAtFixedRate(object : TimerTask(){
//                            override fun run(){
//                                time++
//                                runOnUiThread {
//                                    onBoardTime.text=getTimeFormat()
//                                }
//
//                            }
//                        },0,1000)
                    }
                    /*if flagmode is On, toggles button's background between flag/original
                    * if the tile is a mine, update "Mines Left" on the board*/
                    if(flagMode) {
                        if(button.background==original) {
//                            if(cells[rowcol.row][rowcol.col].value==MINE){
//                                flagsOnMines--
//                                minesOnBoard.text="$flagsOnMines"
//                            }

                            if(flagsOnMines > 0){
                                button.setBackground(flag)
                                cells[rowcol.row][rowcol.col].flag=true
                                flagsOnMines--
                                minesOnBoard.text="$flagsOnMines"
                                if(cells[rowcol.row][rowcol.col].value==MINE){
                                    actualFlagsOnMines--
                                }
                            }
                        } else {
//                            if(cells[rowcol.row][rowcol.col].value==MINE){
//                                flagsOnMines++
//                                minesOnBoard.text="$flagsOnMines"
//                            }
                            if(flagsOnMines < mines){
                                button.setBackground(original)
                                cells[rowcol.row][rowcol.col].flag=false
                                flagsOnMines++
                                minesOnBoard.text="$flagsOnMines"
                                if(cells[rowcol.row][rowcol.col].value==MINE){
                                    actualFlagsOnMines++
                                }
                            }
                        }
                    }
                    /*only make a move if the tile is not flagged*/
                    else if(!cells[rowcol.row][rowcol.col].flag){
                        move(button)
                    }

                    println("Actual Mines with flags $actualFlagsOnMines")
                    /*checks if game status changes and acts accordingly*/
                    updateStatus(id)
                }
                buttonRow.addView(button)
                buttons.add(button)
            }
            board.addView(buttonRow)
        }
    }

    /*populates the "minesAt" Set
    creates cells adjacent to each button
    * first button click will never be a mine*/
    @RequiresApi(Build.VERSION_CODES.O)
    fun setValues(button: Button){
        val first=button.id
        while(minesAt.size<mines){
            val temp:Int=(0 until rows*rows).random()
            if(temp!=first) minesAt.add(temp)
        }

        for(i in 0 until rows){
            val cellRow = ArrayList<Cell>()
            for(j in 0 until rows){
                var currValue=0
                if(minesAt.contains(getId(i,j))) {
                    currValue=MINE
                }
                val cell:Cell= Cell(currValue,false,false)
                cellRow.add(cell)
            }
            cells.add(cellRow)
        }

        cells.forEachIndexed { row, arrayList ->
            arrayList.forEachIndexed { col, _ ->
                if(cells[row][col].value==MINE){
                    setValuesHelper(row,col)
                }
            }
        }

//        buttons.forEach{
//            it.setAutoSizeTextTypeUniformWithConfiguration(20,100,1,1)
//        }

    }

    /*an extension of setValues function
    sets value of each cell surrounding mines*/
    private fun setValuesHelper(row :Int ,col :Int){
        val xMove= arrayOf(-1,0,1)
        val yMove= arrayOf(-1,0,1)
        xMove.forEach {
            val x=it
            yMove.forEach {
                val y=it
                if((row+x in 0 until rows) && (col+y in 0 until rows) && cells[row+x][col+y].value!=MINE){
                    cells[row+x][col+y].value = cells[row+x][col+y].value+1
                } } }
    }

    /*checks if game is won or lost
    * updates status, stops timer, makes a toast and makes all tiles visible in either case*/
    fun updateStatus(value:Int) {
        println("TILESREVEALED "+tilesRevealed)
        if((rows*rows)-tilesRevealed==mines || actualFlagsOnMines==0 || tilesRevealed+flagsOnMines==(rows*rows)){
            status=Status.WON
            makeAllVisible()
            Toast.makeText(this,"Congratulations You WON!!",Toast.LENGTH_LONG).show()
            stopTimer()
            updateScore()
        } else if(value==MINE){
            makeAllVisible()
            status=Status.LOST
            Toast.makeText(this,"You Lose Try Again",Toast.LENGTH_SHORT).show()
            stopTimer()
        }
    }

    /*makes appropriate move according to the button clicked
    * disables the clicked button*/
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun move(button : Button) {
        val id=button.id
        val row=getRowCol(id).row
        val col=getRowCol(id).col
        val curr=cells[row][col]

        if(curr.value==MINE){
            updateStatus(MINE)
        } else if(curr.value==0){
            cells[row][col].visible=true
            tilesRevealed++
            button.setBackgroundColor(grey)
            unlockNeighbours(row,col)
        } else {
            button.text = cells[row][col].value.toString()
            cells[row][col].visible=true
            tilesRevealed++
        }
        button.isEnabled=false
    }

    /*reveals all connected tiles not touching any mines and their neighbouring tiles
    * disables all revealed tiles*/
    fun unlockNeighbours(row:Int, col:Int){
        val q : Queue<Pair<Int,Int>> =LinkedList()
        q.add(Pair(row,col))
        while(!q.isEmpty()){
            val curr = q.poll()
            val currX=curr.first
            val currY=curr.second
            val move= listOf<Pair<Int,Int>>( Pair(1,0), Pair(0,1), Pair(-1,0), Pair(0,-1),Pair(1,-1),Pair(1,1),Pair(-1,1),Pair(-1,-1) )

            move.forEach {
                val i = it.first
                val j = it.second
                if((currX+i in 0 until rows) && (currY+j in 0 until rows)){
                    val temp = cells[currX+i][currY+j]
                    val id=getId(currX+i,currY+j)
                    val button = buttons.get(id)
                    if(!temp.visible){
                        temp.visible=true
                        button.isEnabled=false
                        tilesRevealed++
//                        println("TILESREVEALED------------Row ${currX+i} Col ${currY+j} $tilesRevealed ")
                        if(temp.value==0){
                            q.add(Pair(currX+i,currY+j))
                            button.setBackgroundColor(grey)
                        } else {
                            button.text=temp.value.toString()
                        }
                    }
                }
            }
        }
    }

    /*makes all tiles visible*/
    fun makeAllVisible(){
//        val board=findViewById<LinearLayout>(R.id.game_board)
        buttons.forEach() {
            val button = it
            val id = button.id
            val currTile = cells[getRowCol(id).row][getRowCol(id).col]
            if (!currTile.flag) {
                if (currTile.value == MINE) {
                    button.setBackgroundResource(R.drawable.bomb)
                } else if (currTile.value == 0) {
                    button.setBackgroundColor(grey)
                } else {
                    button.text = currTile.value.toString()
                }
            } else {
                if (currTile.value == 0) {
                    button.setBackgroundColor(grey)
                } else if(currTile.value!= MINE) {
                    button.setBackground(originalButtonBackground)
                    button.text = currTile.value.toString()
                }
            }
            button.isEnabled = false
        }
    }

    /*starts timer
    increments "time" by 1 every second*/
    private fun startTimer(onBoardTimer:TextView) {
        timer.scheduleAtFixedRate(object : TimerTask(){
            override fun run(){
                time++
                runOnUiThread {
                    onBoardTimer.text=getTimeFormat()
                }
            }
        },0,1000)
    }

    /*stops timer and set time to 0*/
    private fun resetTimer(){
        stopTimer()
        time = 0
        onBoardTime.text="00:00"
    }

    /*stops timer*/
    private fun stopTimer(){
        if(timer!=null) timer.cancel()
    }

    /*returns time in "mm:ss" format*/
    fun getTimeFormat() : String {
        val minutes = time/60
        val seconds = time%60
        var sec = "$seconds"
        var min = "$minutes"
        if(seconds<10) sec="0"+sec
        if(minutes<10) min="0"+min
        return "$min:$sec"
    }

    /*updates the bestTime and prevTime if game is won and stores the values permanently*/
    fun updateScore(){
        val sharedPref = getSharedPreferences("MineSweeper", MODE_PRIVATE)
        val hs = sharedPref.getInt(BestTime,Int.MAX_VALUE)
        bestTime = Math.min( bestTime , Math.min(hs,time) )
        prevTime = time
        with((sharedPref.edit())){
            putInt(BestTime , bestTime)
            putInt(PreviousTime , prevTime)
            commit()
        }
    }

    /*returns a pair of row and column corrosponding to the button's id*/
    fun getRowCol(id : Int) : pair {
        val row : Int = id/rows
        val col : Int = id%rows
        return pair(row,col)
    }

    /*returns button id corrosponding to row and column*/
    fun getId(row : Int, col :Int) :Int {
        return (row*rows + col)
    }

}

/*properties each tile will hold*/
data class Cell(var value:Int , var visible:Boolean , var flag:Boolean)

enum class Status{
    WON,
    LOST,
    ONGOING
}

data class pair(val row :Int ,val col :Int)