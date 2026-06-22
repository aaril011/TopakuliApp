package com.project.topakuli.ui.kontribusi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.topakuli.R
import com.project.topakuli.databinding.*
import java.text.SimpleDateFormat
import java.util.*

class KontribusiAdapter(
    private val onOptionClick: (Int) -> Unit,
    private val onInputSubmit: (String, ChatItem.InputCard.InputType) -> Unit,
    private val onContactSubmit: (String, String, String) -> Unit = { _, _, _ -> },
    private val onSearchQuery: (String, (List<SearchItem>) -> Unit) -> Unit = { _, _ -> },
    private val onItemSelected: (SearchItem) -> Unit = { }
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    data class SearchItem(val id: String, val name: String, val sub: String, val type: String)

    private val items = mutableListOf<ChatItem>()

    fun setItems(newItems: List<ChatItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: ChatItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ChatItem.SystemMessage -> 0
        is ChatItem.UserMessage -> 1
        is ChatItem.OptionsMenu -> 2
        is ChatItem.InputCard -> 3
        is ChatItem.ContributionTypeMenu -> 4
        is ChatItem.QuestionTipsCard -> 5
        is ChatItem.SuggestionMenu -> 6
        is ChatItem.ContactInfoCard -> 7
        is ChatItem.CorrectionSearchCard -> 8
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> SystemViewHolder(ItemChatSystemBinding.inflate(inflater, parent, false))
            1 -> UserViewHolder(ItemChatUserBinding.inflate(inflater, parent, false))
            2 -> OptionsViewHolder(ItemChatOptionsBinding.inflate(inflater, parent, false))
            3 -> InputViewHolder(ItemChatInputCardBinding.inflate(inflater, parent, false))
            4 -> ContributionTypeViewHolder(ItemChatOptionsBinding.inflate(inflater, parent, false))
            5 -> TipsViewHolder(ItemChatQuestionTipsBinding.inflate(inflater, parent, false))
            6 -> SuggestionViewHolder(ItemChatSuggestionMenuBinding.inflate(inflater, parent, false))
            7 -> ContactViewHolder(ItemChatContactCardBinding.inflate(inflater, parent, false))
            8 -> CorrectionSearchViewHolder(ItemChatCorrectionSearchBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SystemViewHolder -> if (item is ChatItem.SystemMessage) holder.bind(item)
            is UserViewHolder -> if (item is ChatItem.UserMessage) holder.bind(item)
            is OptionsViewHolder -> holder.bind()
            is InputViewHolder -> if (item is ChatItem.InputCard) holder.bind(item)
            is ContributionTypeViewHolder -> holder.bind()
            is SuggestionViewHolder -> holder.bind()
            is ContactViewHolder -> holder.bind()
            is CorrectionSearchViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int = items.size

    inner class CorrectionSearchViewHolder(private val binding: ItemChatCorrectionSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        private val innerAdapter = SearchResultAdapter { selected ->
            onItemSelected(selected)
        }

        fun bind() {
            binding.rvSearchResults.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = innerAdapter
            }

            binding.etSearchData.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val query = s.toString().trim()
                    if (query.length >= 2) {
                        onSearchQuery(query) { results ->
                            innerAdapter.submitList(results)
                            binding.rvSearchResults.visibility = if (results.isNotEmpty()) View.VISIBLE else View.GONE
                            binding.tvNoResults.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
                        }
                    } else {
                        innerAdapter.submitList(emptyList())
                        binding.rvSearchResults.visibility = View.GONE
                        binding.tvNoResults.visibility = View.GONE
                    }
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
    }

    inner class SearchResultAdapter(private val onClick: (SearchItem) -> Unit) : ListAdapter<SearchItem, SearchResultAdapter.VH>(
        object : DiffUtil.ItemCallback<SearchItem>() {
            override fun areItemsTheSame(old: SearchItem, new: SearchItem) = old.id == new.id
            override fun areContentsTheSame(old: SearchItem, new: SearchItem) = old == new
        }
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(ItemResepTerkaitSmallBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = getItem(position)
            holder.binding.tvJudul.text = item.name
            holder.binding.tvKategori.text = item.sub
            holder.binding.ivResep.setImageResource(if (item.type == "tanaman") R.drawable.herbal else R.drawable.icoon_book)
            holder.itemView.setOnClickListener { onClick(item) }
        }

        inner class VH(val binding: ItemResepTerkaitSmallBinding) : RecyclerView.ViewHolder(binding.root)
    }

    inner class ContactViewHolder(private val binding: ItemChatContactCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.btnSubmitContact.setOnClickListener {
                val name = binding.etName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val phone = binding.etPhone.text.toString().trim()
                
                if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty()) {
                    onContactSubmit(name, email, phone)
                }
            }
        }
    }

    inner class SystemViewHolder(private val binding: ItemChatSystemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem.SystemMessage) {
            binding.tvMessage.text = item.message
            binding.tvTime.text = formatTime(item.timestamp)
        }
    }

    class UserViewHolder(private val binding: ItemChatUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem.UserMessage) {
            binding.tvMessage.text = item.message
        }
    }

    inner class OptionsViewHolder(private val binding: ItemChatOptionsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.btnOption1.setOnClickListener { onOptionClick(1) }
            binding.btnOption2.setOnClickListener { onOptionClick(2) }
            binding.btnOption3.setOnClickListener { onOptionClick(3) }
        }
    }

    inner class ContributionTypeViewHolder(private val binding: ItemChatOptionsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.tvTitle1.text = "Data Tanaman Baru"
            binding.ivIcon1.setImageResource(R.drawable.herbal)
            
            binding.tvTitle2.text = "Resep Obat Baru"
            binding.ivIcon2.setImageResource(R.drawable.icoon_book)
            
            binding.btnOption3.visibility = View.GONE
            
            binding.btnOption1.setOnClickListener { onOptionClick(4) }
            binding.btnOption2.setOnClickListener { onOptionClick(5) }
        }
    }

    inner class InputViewHolder(private val binding: ItemChatInputCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatItem.InputCard) {
            binding.tvInputLabel.text = item.label
            binding.etChatInput.hint = item.hint
            
            // Bersihkan teks lama agar tidak muncul di kartu baru (Recycling Fix)
            binding.etChatInput.setText("")

            binding.btnSubmitInput.setOnClickListener {
                val input = binding.etChatInput.text.toString().trim()
                if (input.isNotEmpty()) onInputSubmit(input, item.type)
            }
        }
    }

    class TipsViewHolder(binding: ItemChatQuestionTipsBinding) : RecyclerView.ViewHolder(binding.root)
    
    inner class SuggestionViewHolder(private val binding: ItemChatSuggestionMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.suggestedResep.tvJudul.text = "Temulawak"
            binding.suggestedResep.tvKategori.text = "Curcuma zanthorrhiza"
            
            binding.suggestedTanaman.tvNama.text = "Daun Kelor"
            binding.suggestedTanaman.tvLatin.text = "Moringa oleifera"
        }
    }

    private fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}
