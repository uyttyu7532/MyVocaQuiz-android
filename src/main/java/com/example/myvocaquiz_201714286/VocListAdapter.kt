
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myvocaquiz_201714286.R

// RecyclerView에 표시될 View 생성
class VocListAdapter(val items: ArrayList<String>, val words:Map<String, String>): RecyclerView.Adapter<VocListAdapter.MyViewHolder>() {

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
        return items.size
    }

    // 뷰홀더에 해당하는 것이 전달됨.(내용만 교체할때 호출됨)
    @SuppressLint("WrongConstant")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView.text=items[position]
        holder.meaningView.text=words.getValue(items[position])

//        if(switchOn) {
//            holder.meaningView.visibility = VISIBLE
//        }
//        else{
//            holder.meaningView.visibility = GONE
//        }


    }



    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        var textView:TextView = itemView.findViewById(R.id.textView)
        var meaningView:TextView = itemView.findViewById(R.id.meaningView)
        init{
            itemView.setOnClickListener {
                itemClickListener?.onItemClick(this, it, items[adapterPosition],adapterPosition)
            }
        }
    }

    fun moveItem(oldPos:Int, newPos:Int){
        val item = items.get(oldPos)
        items.removeAt(oldPos)
        items.add(newPos,item)
        notifyItemMoved(oldPos,newPos)
    }
    fun removeItem(pos:Int){
        items.removeAt(pos)
        notifyItemRemoved(pos)
    }
}