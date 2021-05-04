package com.example.minesweeper

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity() : AppCompatActivity() {

    var rows:Int=-1
    var mines:Int=-1
    var selected=false /*used for checking if the predefined diffculties are selected or not*/
    var bestTime:Int=Integer.MAX_VALUE /*best time in seconds a game is won*/
    var prevTime:Int=-1 /*time taken in seconds to complete previous game*/
    val BestTime = "BestTime"
    val PreviousTime="PrevTime"
    lateinit var highScoreText :TextView
    lateinit var prevTimeText:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val pre_difficulties = findViewById<RadioGroup>(R.id.pre_difficulties)
        val start =findViewById<Button>(R.id.start) /*start button*/
        val custom_rows = findViewById<EditText>(R.id.custom_rows) /*EditText to input custom rows*/
        val custom_mines = findViewById<EditText>(R.id.custom_mines) /*EditText to take custom mines*/
        val difficulty_selector = findViewById<RadioGroup>(R.id.pre_difficulties) /*group of radio buttons for selecting pre-defined difficulties*/
        val custom = findViewById<Button>(R.id.custom)
        val custom_board = findViewById<LinearLayout>(R.id.linearLayout) /*layout with EditTexts to input custom rows & mines*/
        val deselector = findViewById<RadioButton>(R.id.deselector) /*an invisible button to deselect any previously selected pre-defined difficulty*/
        custom_board.isInvisible=true

        highScoreText=findViewById(R.id.HighScoreText)
        prevTimeText=findViewById(R.id.PrevTimeText)
        updateScores() /*gets previous scores from sharedPreferences and updates them on board*/

        /*sets different rows and mines for each button pressed
        * makes custom board invisible*/
        difficulty_selector.forEachIndexed { index, view ->
            view.setOnClickListener {
//                val current=it as RadioButton
                rows = if (index == 0) 9 else if(index == 1) 12 else 16
                mines = if (index == 0) 10 else if (index == 1) 25 else 40
                selected = true
                custom_board.isInvisible=true
            }
        }

        /*resets rows and mines
        deselects any predefined difficulty
        toggles custom boards visibility*/
        custom.setOnClickListener {
            custom_board.isInvisible=!custom_board.isInvisible
            reset()
            deselector.isChecked=true
        }

        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            shareHighScore()
        }

        /*if custom values are set rows and mines are taken from there
        * starts the game if rows & mines are set and have proper values
        * 2<=rows<=20 1<=mines<=(rows*rows)/4
        * else makes a toast suggesting to enter proper values or select predefined difficulty*/
        start.setOnClickListener {

            if(!selected){
                try {
                    rows = custom_rows.text.toString().toInt()
                    mines = custom_mines.text.toString().toInt()
                } catch (ex: Exception){
                    reset()
                }
            }

            if(((rows in 2..20) && (mines in 1..(rows*rows)/4)) || selected){

                val intent:Intent=Intent(this, GameBoard::class.java)
                intent.putExtra("rows", rows)
                intent.putExtra("mines",mines)
                startActivityForResult(intent,1)

                reset()
                val handler=Handler()
                handler.postDelayed({
                    custom_board.forEach {
                        (it as EditText).setText(R.string.empty)
                    }
                    deselector.isChecked = true
                }, 500)

            } else {
                val toast:Toast=Toast.makeText(this, "Select a difficulty or\nEnter 2<=rows<=20 and mines<=(1/4)*rows", Toast.LENGTH_LONG)
                toast.show()
            }
        }
    }

    /*used when input is to taken from user
    * rows and mines set out of range*/
    private fun reset(){
        rows=-1
        mines=-1
        selected=false
    }

    /*takes time in seconds an return string in "mm:ss" format*/
    fun toTimeFormat(time:Int) : String {
        val minutes = time/60
        val seconds = time%60
        var sec = "$seconds"
        var min = "$minutes"
        if(seconds<10) sec="0"+sec
        if(minutes<10) min="0"+min
        return "$min:$sec"
    }

    /*updates best time and previous game time in sharedPreferences and sets values on title screen*/
    fun updateScores(){
        val sharedPref = getSharedPreferences("MineSweeper",MODE_PRIVATE)
        if(sharedPref!=null){
            with(sharedPref){
                val hs = getInt(BestTime , Int.MAX_VALUE)
                val ps = getInt(PreviousTime ,-1)
                bestTime = Math.min(bestTime,hs)
                if(ps!=-1) prevTime = ps
                if(hs!= Int.MAX_VALUE){
                    highScoreText.text="High Score : ${toTimeFormat(hs)}"
                }
                if(ps!=-1){
                    prevTimeText.text="Previous Time : ${toTimeFormat(ps)}"
                }
            }
        }
    }

    private fun shareHighScore() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "This is my Highscore in Minesweeper : ${toTimeFormat(bestTime)}")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    /*when user exits game and returns to title screen , scores are updated*/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        updateScores()
    }

}
