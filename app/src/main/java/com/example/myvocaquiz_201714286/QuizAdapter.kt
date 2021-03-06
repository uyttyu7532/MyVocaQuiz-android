package com.example.myvocaquiz_201714286

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// RecyclerView에 표시될 View 생성
class QuizAdapter(val data:ArrayList<String>, val correct:Data, val choice_count:Int, val choice_quiz:Int): RecyclerView.Adapter<QuizAdapter.MyViewHolder>() {

    interface onItemClickListener{
        fun onItemClick( holder:MyViewHolder, view: View, data:String, position: Int)
    }

    var itemClickListener:onItemClickListener?=null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // row에 해당하는 객체에 inflation되서 View로 전달. View에서는 textView에 해당하는 것을 참고
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row,parent, false)
        return MyViewHolder(v)

    }
    // 아이템의 데이터 갯수
    override fun getItemCount(): Int {
        return choice_count
    }

    // 뷰홀더에 해당하는 것이 전달됨.(내용만 교체할때 호출됨)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if(choice_quiz == 0)
            holder.textView.text=data[position]
        else
            holder.textView.text=data[position]
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var textView:TextView = itemView.findViewById(R.id.textView)
        init{
            itemView.setOnClickListener {
                if(choice_quiz == 0){ // 영어문제 한글보기
                    if(data[adapterPosition] == correct.meaning){
                        itemClickListener?.onItemClick(this, it, "true", adapterPosition)
                    }
                    else{
                        itemClickListener?.onItemClick(this, it, "false",adapterPosition)
                    }
                }


                else{ // 한글문제 영어보기
                    if(data[adapterPosition]== correct.word){
                        itemClickListener?.onItemClick(this, it, "true", adapterPosition)
                    }
                    else{
                        itemClickListener?.onItemClick(this, it, "false",adapterPosition)
                    }
                }

            }
        }
    }

}