package com.example.mtgcollection.ui.slideshow

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlin.collections.ArrayList

class NotificationDBHelper (context: Context) : SQLiteOpenHelper(context, "Notifications", null, 1)  {

    private var sqLiteDatabase: SQLiteDatabase = this.writableDatabase

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableStatement = "CREATE TABLE $NOTIFICATIONS_TABLE (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_DATE TEXT, " +
                "$COLUMN_TITLE TEXT, " +
                "$COLUMN_TEXT TEXT, " +
                "$COLUMN_UNREAD INTEGER)"
        db?.execSQL(createTableStatement)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun close() {
        sqLiteDatabase.close()
        super.close()
    }

    fun addOne(notification : Notifications) : Boolean {
        val contentValues = ContentValues()

        contentValues.put(COLUMN_DATE, notification.date)
        contentValues.put(COLUMN_TITLE, notification.title)
        contentValues.put(COLUMN_TEXT, notification.text)
        contentValues.put(COLUMN_UNREAD, true)

        val insert = sqLiteDatabase.insert(NOTIFICATIONS_TABLE, null, contentValues)
        return -1L == insert
    }

    fun getAll() : ArrayList<Notifications> {
        val returnList = ArrayList<Notifications>()

        val queryString = "SELECT * FROM $NOTIFICATIONS_TABLE ORDER BY $COLUMN_UNREAD DESC, $COLUMN_DATE ASC;"

        val cursor = sqLiteDatabase.rawQuery(queryString, null)

        if (cursor.moveToFirst()) {
            var notification : Notifications
            do {
                notification = cursorToNotification(cursor)!!
                returnList.add(notification)
            }while (cursor.moveToNext())
        }
        cursor.close()
        return returnList
    }

    fun markRead(notification: Notifications) : Boolean {
        val contentValues = ContentValues()

        contentValues.put(COLUMN_UNREAD, false)
        val update = sqLiteDatabase.update(
            NOTIFICATIONS_TABLE, contentValues, "$COLUMN_ID=?",
            arrayOf(notification.id.toString()))

        return update == 1
    }

    fun markAll() : Boolean {
        val contentValues = ContentValues()

        contentValues.put(COLUMN_UNREAD, false)
        val update = sqLiteDatabase.update(
            NOTIFICATIONS_TABLE, contentValues, "$COLUMN_UNREAD=1",
            null)

        return update == 1
    }

    fun removeAll() {
       sqLiteDatabase.execSQL("delete from $NOTIFICATIONS_TABLE")
    }

    companion object {
        const val NOTIFICATIONS_TABLE = "NOTIFICATIONS"
        const val COLUMN_ID = "ID"
        const val COLUMN_DATE = "DATE"
        const val COLUMN_TITLE = "TITLE"
        const val COLUMN_TEXT = "TEXT"
        const val COLUMN_UNREAD = "UNREAD"

        fun cursorToNotification(cursor: Cursor): Notifications? {
            if (cursor.count == 0) return null

            return Notifications(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getInt(4) == 1
            )
        }
    }
}