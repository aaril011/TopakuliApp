package com.project.topakuli.ui.kontribusi

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.topakuli.R
import com.project.topakuli.TopakuliApplication
import com.project.topakuli.data.local.entity.ChatEntity
import com.project.topakuli.data.local.entity.ChatMessage
import com.project.topakuli.databinding.FragmentKontribusiBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class KontribusiFragment : Fragment() {

    private var _binding: FragmentKontribusiBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: KontribusiAdapter
    private val auth = FirebaseAuth.getInstance()
    
    private val viewModel: KontribusiViewModel by viewModels {
        KontribusiViewModel.Factory((requireActivity().application as TopakuliApplication).repository)
    }
    
    private var userName: String = "Warga Desa Pakuli"
    private var userEmail: String = ""
    private var userPhone: String = ""
    private var contributionId: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadAndSendImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKontribusiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val prefs = requireContext().getSharedPreferences("TopakuliPrefs", android.content.Context.MODE_PRIVATE)
        userName = prefs.getString("userName", "Warga Desa Pakuli") ?: "Warga Desa Pakuli"
        userEmail = prefs.getString("userEmail", "") ?: ""
        userPhone = prefs.getString("userPhone", "") ?: ""
        
        setupRecyclerView()
        setupActions()
        observeViewModel()
        
        if (auth.currentUser == null) {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    if (isAdded) {
                        ensureContributionSession()
                        setupChatListener()
                    }
                }
                .addOnFailureListener { e ->
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Koneksi Gagal: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            ensureContributionSession()
            setupChatListener()
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val density = resources.displayMetrics.density
            val defaultBottomPadding = (24 * density).toInt()
            
            binding.layoutBottomBar.setPadding(
                binding.layoutBottomBar.paddingLeft,
                binding.layoutBottomBar.paddingTop,
                binding.layoutBottomBar.paddingRight,
                if (imeHeight > 0) imeHeight else defaultBottomPadding
            )
            
            if (imeHeight > 0) {
                binding.rvChat.postDelayed({
                    if (adapter.itemCount > 0) {
                        binding.rvChat.smoothScrollToPosition(adapter.itemCount - 1)
                    }
                }, 100)
            }
            insets
        }
    }

    private fun ensureContributionSession() {
        val user = auth.currentUser ?: return
        val firestore = FirebaseFirestore.getInstance()
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("KontribusiFragment", "Checking session for UID: ${user.uid}")
                val snapshot = firestore.collection("contributions")
                    .whereEqualTo("userId", user.uid)
                    .get().await()
                
                if (snapshot.isEmpty) {
                    android.util.Log.d("KontribusiFragment", "No session found on server.")
                } else {
                    val doc = snapshot.documents.first()
                    contributionId = doc.id
                    android.util.Log.d("KontribusiFragment", "Existing session found: $contributionId")
                    
                    userName = doc.getString("userName") ?: userName
                    userEmail = doc.getString("userEmail") ?: userEmail
                    userPhone = doc.getString("userPhone") ?: userPhone
                }
            } catch (e: Exception) {
                android.util.Log.e("KontribusiFragment", "Error ensuring session", e)
            }
        }
    }

    private fun setupChatListener() {
        val user = auth.currentUser ?: return
        val firestore = FirebaseFirestore.getInstance()
        
        firestore.collection("contributions")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                val latestDoc = snapshot.documents.sortedByDescending { 
                    it.getLong("timestamp") ?: 0L 
                }.firstOrNull()

                latestDoc?.let { doc ->
                    contributionId = doc.id
                    listenToMessages(doc.id)
                }
            }
    }

    private fun listenToMessages(id: String) {
        FirebaseFirestore.getInstance().collection("contributions").document(id)
            .collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                snapshot.documentChanges.forEach { diff ->
                    if (diff.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val senderType = diff.document.getString("senderType")
                        if (senderType == "admin") {
                            val msg = diff.document.getString("message") ?: ""
                            viewLifecycleOwner.lifecycleScope.launch {
                                addSystemMessage(msg)
                            }
                        }
                    }
                }
            }
    }

    private fun setupRecyclerView() {
        adapter = KontribusiAdapter(
            onOptionClick = { optionId -> handleOptionClick(optionId) },
            onInputSubmit = { input, type -> handleInputSubmit(input, type) },
            onContactSubmit = { name, email, phone -> handleContactSubmit(name, email, phone) },
            onSearchQuery = { query, callback -> performDataSearch(query, callback) },
            onItemSelected = { item -> handleDataSelection(item) }
        )
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@KontribusiFragment.adapter
        }
    }

    private fun performDataSearch(query: String, callback: (List<KontribusiAdapter.SearchItem>) -> Unit) {
        lifecycleScope.launch {
            val repo = (requireActivity().application as TopakuliApplication).repository
            val plants = repo.allTanaman.first().filter { it.nama.contains(query, true) }
                .map { KontribusiAdapter.SearchItem(it.id, it.nama, it.namaLatin, "tanaman") }
            
            val recipes = repo.allResep.first().filter { it.judul.contains(query, true) }
                .map { KontribusiAdapter.SearchItem(it.id, it.judul, it.kategori, "resep") }
            
            callback(plants + recipes)
        }
    }

    private fun handleDataSelection(item: KontribusiAdapter.SearchItem) {
        lifecycleScope.launch {
            viewModel.deleteChatByType("correction_search")
            addUserMessage("Saya ingin mengoreksi data ${item.type}: ${item.name}")
            sendToFirestore("Koreksi untuk ${item.type} ID: ${item.id} (${item.name})")
            delay(500)
            addSystemMessage("Baik, bagian mana dari data '${item.name}' yang menurut Anda perlu dikoreksi? Mohon jelaskan secara detail di bawah ini.")
            binding.layoutBottomBar.visibility = View.VISIBLE
            scrollToBottom()
        }
    }

    private fun handleContactSubmit(name: String, email: String, phone: String) {
        userName = name
        userEmail = email
        userPhone = phone
        
        requireContext().getSharedPreferences("TopakuliPrefs", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("userName", name)
            .putString("userEmail", email)
            .putString("userPhone", phone)
            .apply()

        addUserMessage("Data Kontak:\nNama: $name\nEmail: $email\nWA: $phone")
        
        lifecycleScope.launch {
            viewModel.deleteChatByType("contact_info")
            delay(500)
            createContributionSession(name, email, phone)
            addSystemMessage("Terima kasih, $name! Data Anda telah tersimpan. Admin akan menghubungi Anda via Email/WhatsApp jika diperlukan. Apa yang ingin Anda ajukan hari ini?")
            delay(300)
            saveChatEntity(ChatEntity(type = "options"))
        }
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        
        binding.btnClearChat.setOnClickListener {
            showClearChatConfirmation()
        }

        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                addUserMessage(message)
                binding.etMessage.setText("")
                sendToFirestore(message)
                
                lifecycleScope.launch {
                    delay(1000)
                    val response = "Terima kasih atas pesan Anda. Tim pengelola Topakuli telah menerima masukan Anda dan akan segera meninjaunya secara saksama.\n\nTanggapan atau informasi lebih lanjut akan kami kirimkan melalui Email atau WhatsApp yang telah Anda daftarkan. Mohon tetap pantau notifikasi Anda."
                    addSystemMessage(response)
                }
            }
        }

        binding.btnAttachImage.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun uploadAndSendImage(uri: Uri) {
        Toast.makeText(requireContext(), "Sedang mengirim gambar...", Toast.LENGTH_SHORT).show()
        viewLifecycleOwner.lifecycleScope.launch {
            val app = (requireActivity().application as TopakuliApplication)
            val url = app.firebaseService.uploadImage(uri, "contributions")
            if (url != null) {
                addUserMessage("[Lampiran Gambar]")
                sendToFirestore(url)
            } else {
                Toast.makeText(requireContext(), "Gagal mengirim gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showClearChatConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Riwayat Chat?")
            .setMessage("Apakah Anda yakin ingin menghapus seluruh riwayat percakapan ini? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { dialog, _ ->
                viewModel.clearHistory()
                Toast.makeText(requireContext(), "Riwayat chat dibersihkan", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allChats.collect { chatList ->
                    if (chatList.isEmpty()) {
                        startConversation()
                    } else {
                        val chatItems = chatList.map { entity ->
                            when (entity.type) {
                                "system" -> ChatItem.SystemMessage(entity.message ?: "", entity.timestamp)
                                "user" -> ChatItem.UserMessage(entity.message ?: "", entity.timestamp)
                                "options" -> ChatItem.OptionsMenu
                                "contribution_menu" -> ChatItem.ContributionTypeMenu
                                "tips" -> ChatItem.QuestionTipsCard
                                "suggestion" -> ChatItem.SuggestionMenu
                                "contact_info" -> ChatItem.ContactInfoCard
                                "correction_search" -> ChatItem.CorrectionSearchCard
                                "input_name" -> ChatItem.InputCard("Nama Lengkap*", "Masukkan nama Anda...", ChatItem.InputCard.InputType.NAME)
                                "input_plant" -> ChatItem.InputCard("Masukkan Nama Tanaman", "Contoh: Kumis Kucing, Jahe Merah...", ChatItem.InputCard.InputType.PLANT_NAME)
                                "input_recipe" -> ChatItem.InputCard("Masukkan Nama Resep", "Contoh: Ramuan Batuk, Teh Herbal...", ChatItem.InputCard.InputType.RECIPE_NAME)
                                else -> ChatItem.SystemMessage("Error loading chat")
                            }
                        }
                        adapter.setItems(chatItems)
                        updateBottomBarVisibility(chatItems.lastOrNull())
                        scrollToBottom()
                    }
                }
            }
        }
    }

    private fun updateBottomBarVisibility(lastItem: ChatItem?) {
        binding.layoutBottomBar.visibility = when (lastItem) {
            is ChatItem.SystemMessage -> View.VISIBLE
            is ChatItem.UserMessage -> View.VISIBLE
            is ChatItem.QuestionTipsCard -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun startConversation() {
        lifecycleScope.launch {
            val prefs = requireContext().getSharedPreferences("TopakuliPrefs", android.content.Context.MODE_PRIVATE)
            val savedName = prefs.getString("userName", "")
            
            if (savedName.isNullOrEmpty()) {
                addSystemMessage("Halo! Selamat datang di layanan kontribusi Topakuli. Untuk mempermudah interaksi dengan tim pengelola, silakan lengkapi data diri Anda:")
                delay(300)
                saveChatEntity(ChatEntity(type = "contact_info"))
            } else {
                addSystemMessage("Halo kembali, $savedName! Apa yang bisa kami bantu hari ini?")
                delay(300)
                saveChatEntity(ChatEntity(type = "options"))
            }
        }
    }

    private fun handleInputSubmit(input: String, type: ChatItem.InputCard.InputType) {
        addUserMessage(input)
        
        lifecycleScope.launch {
            delay(500)
            when (type) {
                ChatItem.InputCard.InputType.NAME -> {
                    viewModel.deleteChatByType("input_name")
                    userName = input
                    createContributionSession(input, userEmail, userPhone)
                    addSystemMessage("Salam kenal! Apa yang ingin Anda lakukan hari ini? Silakan pilih opsi di bawah:")
                    delay(300)
                    saveChatEntity(ChatEntity(type = "options"))
                }
                ChatItem.InputCard.InputType.PLANT_NAME -> {
                    viewModel.deleteChatByType("input_plant")
                    val confirmation = "Terima kasih atas kontribusi Anda untuk tanaman '$input'. Pengetahuan Anda sangat berharga bagi pelestarian warisan pengobatan tradisional Desa Pakuli.\n\nMohon kirimkan detail lengkap (khasiat, cara tanam, deskripsi) di bawah ini. Tim ahli kami akan memvalidasi data tersebut dan memberikan tanggapan melalui kontak Email/WhatsApp Anda segera."
                    addSystemMessage(confirmation)
                    sendToFirestore("Pengajuan data tanaman baru: $input")
                    binding.layoutBottomBar.visibility = View.VISIBLE
                    scrollToBottom()
                }
                ChatItem.InputCard.InputType.RECIPE_NAME -> {
                    viewModel.deleteChatByType("input_recipe")
                    val confirmation = "Resep tradisional '$input' telah kami catat dalam antrean tinjauan. Terima kasih telah berbagi warisan budaya yang berharga ini.\n\nSilakan lengkapi detail bahan dan cara pembuatan di bawah ini. Tim pengelola akan memproses pengajuan Anda dan menghubungi Anda kembali melalui kontak yang terdaftar."
                    addSystemMessage(confirmation)
                    sendToFirestore("Pengajuan resep herbal baru: $input")
                    binding.layoutBottomBar.visibility = View.VISIBLE
                    scrollToBottom()
                }
            }
        }
    }

    private suspend fun createContributionSession(name: String, email: String = "", phone: String = "") {
        val user = auth.currentUser ?: return
        val firestore = FirebaseFirestore.getInstance()
        
        val contribution = mapOf(
            "userId" to user.uid,
            "userName" to name,
            "userEmail" to email,
            "userPhone" to phone,
            "lastMessage" to "Aktif",
            "timestamp" to System.currentTimeMillis(),
            "isReadByAdmin" to false
        )
        
        try {
            val ref = firestore.collection("contributions").add(contribution).await()
            contributionId = ref.id
            android.util.Log.d("KontribusiFragment", "Created new session: $contributionId")
        } catch (e: Exception) {
            android.util.Log.e("KontribusiFragment", "Error creating session", e)
            if (isAdded) {
                Toast.makeText(requireContext(), "Gagal membuat sesi: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleOptionClick(optionId: Int) {
        val optionText = when (optionId) {
            1 -> "Ajukan Pertanyaan"
            2 -> "Berikan Saran/Koreksi"
            3 -> "Ajukan Data Tanaman/Resep Baru"
            4 -> "Data Tanaman Baru"
            5 -> "Resep Obat Baru"
            else -> ""
        }

        if (optionText.isNotEmpty()) {
            addUserMessage(optionText)
            viewLifecycleOwner.lifecycleScope.launch {
                var retry = 0
                while (contributionId == null && retry < 10) {
                    delay(500)
                    retry++
                }
                sendToFirestore(optionText)
            }
        }

        when (optionId) {
            1 -> { // Ajukan Pertanyaan
                lifecycleScope.launch {
                    delay(800)
                    addSystemMessage("Kami siap membantu menjawab pertanyaan Anda terkait tanaman obat atau penggunaan aplikasi.\n\nSilakan tuliskan pertanyaan Anda secara detail di bawah ini. Tim ahli kami akan memberikan tanggapan resmi melalui Email atau WhatsApp Anda segera.")
                    delay(300)
                    saveChatEntity(ChatEntity(type = "tips"))
                }
            }
            2 -> { // Berikan Saran/Koreksi
                lifecycleScope.launch {
                    delay(800)
                    addSystemMessage("Saran dan koreksi Anda sangat penting untuk akurasi data aplikasi Topakuli.\n\nSilakan cari dan pilih data tanaman atau resep yang ingin Anda koreksi:")
                    delay(300)
                    saveChatEntity(ChatEntity(type = "correction_search"))
                }
            }
            3 -> { // Ajukan Data Tanaman/Resep Baru
                lifecycleScope.launch {
                    delay(800)
                    addSystemMessage("Kontribusi Anda membantu kami melestarikan pengetahuan herbal Desa Pakuli untuk generasi mendatang.\n\nData baru apa yang ingin Anda ajukan hari ini?")
                    delay(300)
                    saveChatEntity(ChatEntity(type = "contribution_menu"))
                }
            }
            4 -> { // Data Tanaman Baru
                lifecycleScope.launch {
                    delay(800)
                    addSystemMessage("Bagus! Silakan kirimkan detail Tanaman Baru Anda (Nama, Khasiat, Deskripsi). Jika ada foto, silakan lampirkan dengan menekan ikon galeri.")
                    binding.layoutBottomBar.visibility = View.VISIBLE
                    scrollToBottom()
                }
            }
            5 -> { // Resep Obat Baru
                lifecycleScope.launch {
                    delay(800)
                    addSystemMessage("Bagus! Silakan kirimkan detail Resep Baru Anda (Judul, Bahan, Cara Pembuatan) dan cantumkan juga Referensi/Sumber resepnya.")
                    binding.layoutBottomBar.visibility = View.VISIBLE
                    scrollToBottom()
                }
            }
        }
    }

    private fun sendToFirestore(text: String) {
        val user = auth.currentUser ?: return
        
        lifecycleScope.launch {
            var retry = 0
            while (contributionId == null && retry < 15) {
                delay(500)
                retry++
            }
            
            val id = contributionId
            if (id == null) {
                android.util.Log.e("KontribusiFragment", "Failed to send message: No contribution session found after retries")
                return@launch
            }

            val msg = ChatMessage(
                id = UUID.randomUUID().toString(),
                senderId = user.uid,
                senderName = userName,
                message = text,
                timestamp = System.currentTimeMillis(),
                senderType = "user"
            )
            try {
                (requireActivity().application as TopakuliApplication).repository.sendChatMessage(id, msg)
                android.util.Log.d("KontribusiFragment", "Message sent to Firestore successfully: $text")
            } catch (e: Exception) {
                android.util.Log.e("KontribusiFragment", "Error sending message to Firestore", e)
            }
        }
    }

    private fun addSystemMessage(message: String) {
        saveChatEntity(ChatEntity(type = "system", message = message))
    }

    private fun addUserMessage(message: String) {
        saveChatEntity(ChatEntity(type = "user", message = message))
    }

    private fun saveChatEntity(chat: ChatEntity) {
        viewModel.saveChat(chat)
    }

    private fun scrollToBottom() {
        binding.rvChat.post {
            if (isAdded && adapter.itemCount > 0) {
                binding.rvChat.smoothScrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
