package com.example.myvocaquiz_201714286

import VocListAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_voc_list.*
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller
import java.util.*
import kotlin.collections.ArrayList


class VocListFragment : Fragment() {

    var words = mutableMapOf<String, String>()
    var array = ArrayList<String>()
    lateinit var adapter: VocListAdapter
    lateinit var tts: TextToSpeech
    var isTtsReady = false
    var switchOn = false


//    lateinit var basicPref: SharedPreferences
    lateinit var myPref: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_voc_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        val fastScroller = view.findViewById(R.id.fast_scroller) as VerticalRecyclerViewFastScroller
        fastScroller.setRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(fastScroller.getOnScrollListener())


        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = VocListAdapter(array, words, switchOn)
        recyclerView.adapter = adapter


        init(recyclerView)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        // 전체보이기/숨기기
        meaningSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                switchOn = true
                makeList(recyclerView, array)
            } else {
                switchOn = false
                makeList(recyclerView, array)
            }
        }

        sortByABC.setOnClickListener {
            sortByABC.setTextColor(Color.BLACK)
            sortByRecent.setTextColor(Color.GRAY)
            sortArray()
        }
        sortByRecent.setOnClickListener {
            sortByRecent.setTextColor(Color.BLACK)
            sortByABC.setTextColor(Color.GRAY)
            makeList(recyclerView, array)
        }

        addFab.setOnClickListener {
            addDialog()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })


    }

    override fun onStop() {
        super.onStop()
        tts.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }

    @SuppressLint("WrongConstant")
    private fun init(recyclerView: RecyclerView) {

//        basicPref = context!!.getSharedPreferences("basicPref", Context.MODE_PRIVATE)
        myPref = context!!.getSharedPreferences("myPref", Context.MODE_PRIVATE)

        tts = TextToSpeech(context, TextToSpeech.OnInitListener {
            isTtsReady = true
            tts.language = Locale.US
        })

        loadAllData(myPref)
//        basicPref.edit().clear().apply()
//        readBasicFile(recyclerView)
        makeList(recyclerView, array)


        val simpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP and ItemTouchHelper.ACTION_STATE_IDLE,
            ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val delWord = adapter.removeItem(viewHolder.adapterPosition)
                delData(myPref, delWord)
                Toasty.error(
                    context!!,
                    "\"" + delWord + "\" 단어가 삭제되었습니다.",
                    Toast.LENGTH_SHORT,
                    true
                ).show();
            }

        }

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

    }


    private fun makeList(recyclerView: RecyclerView, array: ArrayList<String>) {
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = VocListAdapter(array, words, switchOn)
        adapter.itemClickListener = object : VocListAdapter.onItemClickListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onItemClick(
                holder: VocListAdapter.MyViewHolder,
                view: View,
                data: String,
                position: Int
            ) {
                if (isTtsReady) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak(data, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
                if (holder.meaningView.visibility == View.GONE) {
                    holder.meaningView.visibility = View.VISIBLE
                } else {
                    holder.meaningView.visibility = View.GONE
                }
            }
        }
        recyclerView.adapter = adapter

        try {
            if (searchView.query != null) {
                adapter.filter.filter(searchView.query.toString())
            }
        } catch (_: Exception) {

        }


    }

    private fun saveData(pref: SharedPreferences, word: String, meaning: String) {
        val editor = pref.edit()
        editor.putString(word, meaning)
            .apply()

        words[word] = meaning
        array.add(word)
    }


    private fun loadAllData(pref: SharedPreferences) {
        val prefKeys: MutableSet<String> = pref.all.keys
        for (pref_key in prefKeys) {
            words[pref_key] = pref.getString(pref_key, "null")
            array.add(pref_key)
        }
    }

    private fun delData(pref: SharedPreferences, word: String) {

        val editor = pref.edit()
        editor.remove(word).commit()

        words.remove(word)
        array.remove(word)
        //https://www.it-swarm.dev/ko/java/%ED%8C%8C%EC%9D%BC%EC%97%90%EC%84%9C-%EC%A4%84%EC%9D%84-%EC%B0%BE%EC%95%84%EC%84%9C-%EC%A0%9C%EA%B1%B0%ED%95%98%EC%8B%AD%EC%8B%9C%EC%98%A4/967010671/
        //https://stackoverrun.com/ko/q/11565281
        //https://mantdu.tistory.com/731

    }


    fun addDialog() {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.fragment_add, null)

        var editWord = mDialogView.findViewById<EditText>(R.id.editWord)
        var editMeaning = mDialogView.findViewById<EditText>(R.id.editMeaning)

        var clearEditWord = mDialogView.findViewById<ImageView>(R.id.clearEditWord)
        var clearEditMeaning = mDialogView.findViewById<ImageView>(R.id.clearEditMeaning)

        editWord.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                editMeaning?.setText(words[editWord.text.toString()])
            }
        }


        clearEditWord.setOnClickListener {
            editWord.setText("")
        }

        clearEditMeaning.setOnClickListener {
            editMeaning.setText("")
        }

        val builder = AlertDialog.Builder(context!!)
            .setView(mDialogView)
            .setTitle("단어 추가하기")
            .setPositiveButton("추가") { _, _ ->
                var newWord = editWord.text.toString()
                var newMeaning = editMeaning.text.toString()


                if (words.containsKey(newWord)) {
                    delData(myPref, newWord)
                    saveData(myPref, newWord, newMeaning)
                    Toasty.warning(
                        view!!.context.applicationContext,
                        "\"" + newWord + "\" 단어가 수정되었습니다.",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                } else {
                    saveData(myPref, newWord, newMeaning)
                    Toasty.success(
                        view!!.context.applicationContext,
                        "\"" + newWord + "\" 단어가 추가되었습니다.",
                        Toast.LENGTH_SHORT,
                        true
                    ).show()
                }
                makeList(recyclerView, array)

            }
        builder.setNegativeButton("취소") { _, _ ->
        }


        val dig = builder.create()
        dig.setCanceledOnTouchOutside(false)
        dig.show()

    }

    fun sortArray() {
        var sortedArray: ArrayList<String> = ArrayList(array)
        Collections.sort(sortedArray)
        makeList(recyclerView, sortedArray)
    }


//    fun readBasicFileScan(scan: Scanner) {
//        while (scan.hasNextLine()) {
//            val word = scan.nextLine()
//            val meaning = scan.nextLine()
//
//            saveData(basicPref, word, meaning)
//        }
//    }

//    // 기본 단어장을 sharedpreference에 저장하기
//    fun readBasicFile(recyclerView: RecyclerView) {
//        val scan = Scanner(resources.openRawResource(R.raw.words))
//        readBasicFileScan(scan)
//
//        makeList(recyclerView, array)
//
//    }


}
