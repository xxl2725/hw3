package com.example.myapplication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // shared preferences that contain user's save feeds
    private var saveFeeds: SharedPreferences? = null
    private var checkUpdate: Boolean = false
    private var oldTag: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        saveFeeds = this.getSharedPreferences("feeds", Context.MODE_PRIVATE)


        //register listener for save and clear tag buttons
        saveButton.setOnClickListener{handleSaveButtonClick()}
        clearTextButton.setOnClickListener{handleClearTagButtonClick()}

        refreshButtons(null)


    }

    //recreate search tag and edit buttons for all saved feeds
    // pass null to create all of them
    private fun refreshButtons(newTag: String?){
        val tags: Array<String> = saveFeeds!!.all.keys.toTypedArray()
        tags.sortWith(String.CASE_INSENSITIVE_ORDER) //sort by tag


        //if a new tag is being added (newTag !=null), insert into the gui at the approprtate locateion
        if(newTag!=null){
            var index = tags.binarySearch(newTag!!)
            if(index<0) index = -index - 1
            makeTagGUI(newTag!!, index)
        }else{
            // display all the feeds
            for (index in tags.indices){
                makeTagGUI(tags[index], index);
            }
        }
    }//refreshButtons


    // add new search to the save file and then refresh all buttons
    private fun makeTag(query : String, tag : String){
        //origanlquery = "" if a new search
        val originalQuery = saveFeeds!!.getString(tag,"")


        //get a startedpreferences edior to store the new tag/query
        //or add a existing one

        val editor = saveFeeds!!.edit()
        editor.putString(tag,query) //store the current search
        editor.apply() // update the file

        //or all in one line
        //saveFeeds!!.edit().putString(tag,query).apply()

        //if a new query add its gui

        if(checkUpdate){
            if(oldTag != tag ){
                editor.remove(oldTag).apply()
            }
            clearButtons()
            refreshButtons(null)

            checkUpdate = false
        }

        else if(originalQuery ==""){
            refreshButtons(tag)
        }

    }

    //add a new tag button and corresponding edit button to GUI
    private fun makeTagGUI(tag: String, index: Int){

        // get a referance to lAYOUTiNFLATER SERVICE
        val inflater: LayoutInflater = LayoutInflater.from(applicationContext)

        //inflate the new_tag_
        val newTagView: View =  inflater.inflate(R.layout.new_tag_view, null , false)

        val newTagButton = newTagView.findViewById<Button>(R.id.newTagButton)
        newTagButton.text = tag
        newTagButton.setOnClickListener(object : View.OnClickListener{

            override fun onClick(v:View?){
                handleQueryButtonClicked(v!! as Button)
            }
        })


        val newEditButton = newTagView.findViewById<Button>(R.id.newEditButton)
        newEditButton.setText(R.string.edit)
        newEditButton.setOnClickListener(object : View.OnClickListener{

            override fun onClick(v:View?){
                handleEditButtonClicked(v!! as Button)
            }
        })


        // add them to the GUI
        queryLinerLayout.addView(newTagView, index)

    }

    //clear buttons from the GI
    private fun clearButtons(){
        queryLinerLayout.removeAllViews()

    }

    private fun handleSaveButtonClick(){
        //create tag if both queryEditText and TagEditdText are not empty
        if(queryEditText.text.length > 0 && tagEditText.text.length > 0){
            makeTag(queryEditText.text.toString(),tagEditText.text.toString())

            //clear the fields
            queryEditText.setText("")
            tagEditText.setText("")


            (getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                tagEditText.windowToken, 0
            )
        }else{
            //display an error message
            //create a new AlertDialog Builder

            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle(R.string.missingTitle)

            //ok button that just dismiss the dialog
            builder.setPositiveButton(R.string.OK, null)

            builder.setMessage(R.string.missingMessage)

            val errorDialog = builder.create()
            errorDialog.show()


        }


    }

    private fun handleClearTagButtonClick(){
        val builder = AlertDialog.Builder(this@MainActivity)

        builder.setTitle(R.string.confirmTitle)

        //ok button
        builder.setPositiveButton(R.string.erase){ dialog, which ->
            clearButtons()
            saveFeeds!!.edit().clear().apply()
        }


        //clart button
        builder.setCancelable(true)
        builder.setNegativeButton(R.string.cancel, null)

        builder.setMessage(R.string.confirmMessage)

        builder.create().show()
    }

    private fun handleEditButtonClicked(v:Button){

        //get all the needed GUI components
        val buttonRow = v.parent as ConstraintLayout
        val seartchButton = buttonRow.findViewById<Button>(R.id.newTagButton)
        val tag = seartchButton.text.toString()
        tagEditText.setText(tag)
        queryEditText.setText(saveFeeds!!.getString(tag,""))
        checkUpdate = true
        oldTag = tag



    }

    // load the seleted search in a web browser
    private fun handleQueryButtonClicked(v:Button){
        //get the query
        val buttonText = v.text.toString()
        val query = saveFeeds!!.getString(buttonText,"")



        //create the URL needed
        val urlString = getString(R.string.searchURL) + query

        //create implicit intent
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))

        //execute the intent
        startActivity(webIntent)
    }

}
