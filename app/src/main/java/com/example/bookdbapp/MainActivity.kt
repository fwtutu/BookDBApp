package com.example.bookdbapp

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookdbapp.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivityMainBinding

    private lateinit var bookList: ArrayList<Book>
    private lateinit var bookRecyclerViewAdapter: BookRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        bookList = arrayListOf()
        bookRecyclerViewAdapter = BookRecyclerViewAdapter(bookList)
        dataBinding.rvBookList.adapter = bookRecyclerViewAdapter
        dataBinding.rvBookList.layoutManager = LinearLayoutManager(this)

        BookDbHelper.init(this)
        val dbHelper = BookDbHelper.getInstance()

//        dbHelper?.addAuthor("aaa")
//        dbHelper?.addAuthor("bbb")
//        dbHelper?.addPublisher("111")
//        dbHelper?.addPublisher("222")

        updateAuthorList()
        updatePublisherList()

        dataBinding.btAdd.setOnClickListener {
            val selectedAuther = (dataBinding.spAuthor.selectedView as TextView).text.toString()
            val selectedPublisher =
                (dataBinding.spPublisher.selectedView as TextView).text.toString()
            dbHelper?.addBook(
                dataBinding.edTitle.text.toString(),
                selectedAuther,
                selectedPublisher
            )
        }

        dataBinding.btShow.setOnClickListener {
            bookList.clear()
            val books = dbHelper?.getAllBooks()
            books?.let {
                bookList.addAll(books)
                bookRecyclerViewAdapter.notifyDataSetChanged()
            }
        }

        dataBinding.btQuery.setOnClickListener {
            bookList.clear()

            val book = Book(
                -1,
                dataBinding.edTitle.text.toString(),"",""
//                dataBinding.edAuthor.text.toString(),
//                dataBinding.edPublisher.text.toString()
            )
            val books = dbHelper?.queryBooks(book)
            books?.let {
                bookList.addAll(books)
                bookRecyclerViewAdapter.notifyDataSetChanged()
            }
        }
    }

    private class BookRecyclerViewAdapter(private val books: ArrayList<Book>) : RecyclerView.Adapter<BookRecyclerViewAdapter.ViewHolder>() {
        data class ViewHolder(val bookView: View) : RecyclerView.ViewHolder(bookView) {
            val tvTitle = bookView.findViewById<TextView>(R.id.tvTitle)
            val tvAuthor = bookView.findViewById<TextView>(R.id.tvAuthor)
            val tvPublisher = bookView.findViewById<TextView>(R.id.tvPublisher)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
            return ViewHolder(v)
        }

        override fun getItemCount(): Int {
            return books.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val book = books[position]
            holder.tvTitle.text = book.title
            holder.tvAuthor.text = book.author
            holder.tvPublisher.text = book.publisher

            holder.bookView.setOnLongClickListener {
                val dlg = BottomSheetDialog(it.context, R.style.BottomSheetDialog)
                dlg.setContentView(R.layout.dlg_del_update)
                val btDel = dlg.findViewById<Button>(R.id.btDel)
                val btUpdate = dlg.findViewById<Button>(R.id.btUpdate)

                btDel?.setOnClickListener {
                    BookDbHelper.getInstance()?.delBook(book.id)
                    books.removeAt(position)
                    notifyDataSetChanged()
                    dlg.dismiss()
                }

                btUpdate?.setOnClickListener {
                    val updateDlg = Dialog(it.context)
                    updateDlg.setContentView(R.layout.dlg_update)
                    updateDlg.setCancelable(false)
                    updateDlg.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val edDlgTitle = updateDlg.findViewById<EditText>(R.id.edDlgTitle)
                    val edDlgAuthor = updateDlg.findViewById<EditText>(R.id.edDlgAuthor)
                    val edDlgPublisher = updateDlg.findViewById<EditText>(R.id.edDlgPublisher)
                    val btOk = updateDlg.findViewById<Button>(R.id.btOk)
                    val btCancel = updateDlg.findViewById<Button>(R.id.btCancel)

                    edDlgTitle.setText(book.title)
                    edDlgAuthor.setText(book.author)
                    edDlgPublisher.setText(book.publisher)

                    btOk?.setOnClickListener {
                        val newData = Book(
                            book.id,
                            edDlgTitle.text.toString(),
                            edDlgAuthor.text.toString(),
                            edDlgPublisher.text.toString())

                        BookDbHelper.getInstance()?.updateBook(newData)
                        books.removeAt(position)
                        books.add(position, newData)
                        notifyDataSetChanged()
                        updateDlg.dismiss()
                        dlg.dismiss()
                    }

                    btCancel?.setOnClickListener {
                        updateDlg.dismiss()
                        dlg.dismiss()
                    }
                    
                    updateDlg.show()
                }

                dlg.show()
                true
            }
        }
    }
    fun updateAuthorList(){
        val spAuthorAdapter = BookDbHelper.getInstance()?.getALLAuthors().let {
            ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item, it as List<String>)
        }
        dataBinding.spAuthor.adapter = spAuthorAdapter
    }

    fun updatePublisherList(){
        val spPublisherAdapter = BookDbHelper.getInstance()?.getALLPublishers().let {
            ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item, it as List<String>)
        }
        dataBinding.spPublisher.adapter = spPublisherAdapter
    }
}