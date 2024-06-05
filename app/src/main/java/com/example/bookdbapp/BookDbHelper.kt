package com.example.bookdbapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

class BookDbHelper private constructor(val context: Context) :
    SQLiteOpenHelper(context, DB_FILE, null, 1) {

        companion object {
            private const val DB_FILE = "books.db"
            private const val BOOK_TABLE = "books"
            private const val AUTHOR_TABLE = "authors"
            private const val PUBLISHER_TABLE = "publishers"
            private const val ID = "id"
            private const val TITLE = "title"
            private const val AUTHOR_ID = "author_id"
            private const val PUBLISHER_ID = "publisher_id"
            private const val AUTHOR_NAME = "author_name"
            private const val PUBLISHER_NAME = "publisher_name"

            private var bookDbHelper: BookDbHelper? = null

            fun init(context: Context) {
                if (bookDbHelper == null)
                    bookDbHelper = BookDbHelper(context)
            }

            fun getInstance(): BookDbHelper? {
                return bookDbHelper
            }
        }
    override fun onCreate(db: SQLiteDatabase?) {
        val sqlAuthorTable = "create table $AUTHOR_TABLE ($ID integer primary key, " +
                "$AUTHOR_NAME nvarchar(20));"
        val sqlPublisherTable = "create table $PUBLISHER_TABLE ($ID integer primary key, " +
                "$PUBLISHER_NAME nvarchar(50));"
        val sqlBookTable = "create table $BOOK_TABLE ($ID integer primary key, " +
                "$TITLE nvarchar(30), $AUTHOR_ID integer, $PUBLISHER_ID integer, " +
                "foreign key ($AUTHOR_ID) references $AUTHOR_TABLE($ID), " +
                "foreign key ($PUBLISHER_ID) references $PUBLISHER_TABLE($ID));"

        db?.execSQL(sqlAuthorTable)
        db?.execSQL(sqlPublisherTable)
        db?.execSQL(sqlBookTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion != newVersion) {
            db?.execSQL("drop table if exists $BOOK_TABLE")
            onCreate(db)
        }
    }

    fun addBook(title: String, author: String, publisher: String) {
        var c = readableDatabase.query(AUTHOR_TABLE, arrayOf(ID), "$AUTHOR_NAME = ?",
            arrayOf(author), null, null, null)

        if (c.count == 0) return
        c.moveToFirst()
        val authorId = c.getInt(0);

        c = readableDatabase.query(
            PUBLISHER_TABLE, arrayOf(ID), "$PUBLISHER_NAME = ?",
            arrayOf(publisher), null, null, null)

        if (c.count == 0) return
        c.moveToFirst()
        val publisherId = c.getInt(0);

        val cv = ContentValues()
        cv.put(TITLE, title)
        cv.put(AUTHOR_ID, authorId)
        cv.put(PUBLISHER_ID, publisherId)

        writableDatabase.insert(BOOK_TABLE, null, cv)
        writableDatabase.close()
    }

    fun printAllBooks() {
        val c = readableDatabase.query(BOOK_TABLE, arrayOf(TITLE, AUTHOR_ID, PUBLISHER_ID),
            null, null, null, null, null)

        if (c.count != 0) {
            c.moveToFirst()
            do {
                Toast.makeText(context, "Book: ${c.getString(0)}, " +
                        "${c.getString(1)}, ${c.getString(2)}", Toast.LENGTH_LONG).show()
            } while (c.moveToNext())
        }
    }

    fun getAllBooks(): ArrayList<Book> {
        val c = readableDatabase.rawQuery("select $BOOK_TABLE.$ID, $BOOK_TABLE.$TITLE, $AUTHOR_TABLE.$AUTHOR_NAME, $PUBLISHER_TABLE.$PUBLISHER_NAME " +
        "from $BOOK_TABLE inner join $AUTHOR_TABLE on $BOOK_TABLE.$AUTHOR_ID=$AUTHOR_TABLE.$ID " +
        "inner join $PUBLISHER_TABLE on $BOOK_TABLE.$PUBLISHER_ID=$PUBLISHER_TABLE.$ID", null)

        val books = arrayListOf<Book>()

        if (c.count != 0) {
            c.moveToFirst()
            do {
                val book = Book(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3)
                    )
                books.add(book)
            } while (c.moveToNext())
        }

        return books
    }

    fun delBook(id: Int) {
        writableDatabase.delete(BOOK_TABLE, "$ID = ?", arrayOf(id.toString()))
        writableDatabase.close()
    }

    fun updateBook(book: Book) {
        val cv = ContentValues()
        cv.put(TITLE, book.title)
        cv.put(AUTHOR_ID, book.author)
        cv.put(PUBLISHER_ID, book.publisher)

        writableDatabase.update(BOOK_TABLE, cv, "$ID = ?", arrayOf(book.id.toString()))
        writableDatabase.close()
    }

    fun queryBooks(book: Book): ArrayList<Book> {
        var conditionExists = false
        var selection = ""

        if (book.title.isNotEmpty()) {
            selection = "$TITLE = '${book.title}'"
            conditionExists = true
        }

        if (book.author.isNotEmpty()) {
            if (!conditionExists) {
                selection = "$AUTHOR_ID = '${book.author}'"
                conditionExists = true
            } else {
                selection = "$selection and $AUTHOR_ID = '${book.author}'"
            }
        }

        if (book.publisher.isNotEmpty()) {
            if (!conditionExists) {
                selection = "$PUBLISHER_ID = '${book.publisher}'"
                conditionExists = true
            } else {
                selection = "$selection and $PUBLISHER_ID = '${book.publisher}'"
            }
        }

        val books = arrayListOf<Book>()

        if (!conditionExists) return books

        val c = readableDatabase.query(BOOK_TABLE, arrayOf(ID, TITLE, AUTHOR_ID, PUBLISHER_ID),
            selection, null, null, null, null)

        if (c.count != 0) {
            c.moveToFirst()
            do {
                val book = Book(
                    c.getInt(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3)
                )
                books.add(book)
            } while (c.moveToNext())
        }

        return books
    }

    fun addAuthor(name: String) {
        val cv = ContentValues()
        cv.put(AUTHOR_NAME, name)

        writableDatabase.insert(AUTHOR_TABLE, null, cv)
        writableDatabase.close()
    }

    fun addPublisher(name: String) {
        val cv = ContentValues()
        cv.put(PUBLISHER_NAME, name)

        writableDatabase.insert(PUBLISHER_TABLE, null, cv)
        writableDatabase.close()
    }

    fun getALLAuthors(): List<String>{
        val c = readableDatabase.query(AUTHOR_TABLE, arrayOf(AUTHOR_NAME),
            null,null,null,null,null)
        val authors = arrayListOf<String>()

        if (c.count !=0) {
            c.moveToFirst()
            do {
                authors.add(c.getString(0))
            } while (c.moveToNext())
        }
            return authors
    }

    fun getALLPublishers(): List<String>{
        val c = readableDatabase.query(PUBLISHER_TABLE, arrayOf(PUBLISHER_NAME),
            null,null,null,null,null)
        val publishers = arrayListOf<String>()

        if (c.count !=0) {
            c.moveToFirst()
            do {
                publishers.add(c.getString(0))
            } while (c.moveToNext())
        }
        return publishers
    }
}