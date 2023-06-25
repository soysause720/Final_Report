package com.example.finalproject

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity2 : AppCompatActivity() {
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val dbHelper = DatabaseHelper(this)  // 替換為你的 DatabaseHelper 的建立方式
        val receivedData = intent.getStringExtra("data_key")
        if (receivedData != null) {
            // 在這裡處理接收到的資料
            dbHelper.insertItem(receivedData)
        }
        val db = dbHelper.readableDatabase
        val projection = arrayOf(DatabaseHelper.COLUMN_ITEM)
        val cursor = db.query(DatabaseHelper.TABLE_NAME, projection, null, null, null, null, null)
        val itemList = mutableListOf<String>()
        itemList.removeAll(dbHelper.getDeletedItems())
        while (cursor.moveToNext()) {
            val item = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ITEM))
            itemList.add(item)
        }

        val Layout = findViewById<ConstraintLayout>(R.id.TicketLayout1)
            Layout.visibility = View.GONE
        val TicketListView = findViewById<ListView>(R.id.TicketListView)  // 替換為你的 ListView 的 ID
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, itemList)
        TicketListView.adapter = adapter
        TicketListView.setOnItemClickListener { parent, view, position, id ->
            // 在這裡處理選項選擇事件
            Layout.visibility = View.VISIBLE
            TicketListView.visibility = View.INVISIBLE
            val selectedItem = adapter.getItem(position) as String
            val textView = findViewById<TextView>(R.id.textView4)
            textView.text = "確定要刪除這筆資料嗎"
            val ConfirmButton = findViewById<Button>(R.id.button)
            val CancelButton = findViewById<Button>(R.id.button2)

            ConfirmButton.setOnClickListener {
                val selectedID = itemList.indexOf(selectedItem).toLong()
                dbHelper.deleteItem(selectedID)
                TicketListView.visibility = View.VISIBLE
                Layout.visibility = View.GONE
                itemList.remove(selectedItem)
                adapter.notifyDataSetChanged()
            }

            CancelButton.setOnClickListener {
                TicketListView.visibility = View.VISIBLE
                Layout.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }
        }


        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fab.setOnClickListener {
            val intent = Intent()
            intent.setClass(this@MainActivity2, MainActivity::class.java)
            startActivity(intent)
        }

    }


}
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "your_database.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "your_table"
        private const val COLUMN_ID = "id"
        const val COLUMN_ITEM = "item"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_ITEM TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle database upgrade if needed
    }

    fun insertItem(item: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_ITEM, item)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }
    fun deleteItem(id: Long) {
        val db = writableDatabase
        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())
        db.delete(TABLE_NAME, whereClause, whereArgs)
        db.close()
    }
    fun getDeletedItems(): List<String> {
        val db = readableDatabase
        val deletedItems = mutableListOf<String>()
        val projection = arrayOf(COLUMN_ITEM)
        val selection = "$COLUMN_ID NOT IN (SELECT DISTINCT $COLUMN_ID FROM $TABLE_NAME)"
        val cursor = db.query(TABLE_NAME, projection, selection, null, null, null, null)

        while (cursor.moveToNext()) {
            val item = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ITEM))
            deletedItems.add(item)
        }

        cursor.close()
        return deletedItems
    }
}



