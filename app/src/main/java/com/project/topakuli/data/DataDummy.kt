package com.project.topakuli.data

import com.project.topakuli.data.local.entity.ResepEntity
import com.project.topakuli.data.local.entity.TanamanObatEntity

object DataDummy {

    fun generateDummyTanaman(): List<TanamanObatEntity> {
        return listOf(
            TanamanObatEntity(
                id = "1",
                nama = "Mayana",
                namaDaerah = "Ati-Ati",
                namaLatin = "Coleus scutellarioides",
                deskripsi = "Mayana adalah salah satu tanaman obat asli Indonesia yang memiliki khasiat kesehatan yang sangat beragam. Tanaman ini sangat mudah dikenali dari corak dan warna daunnya yang mencolok yang biasanya berwarna merah hati, ungu pekat, atau kombinasi warna-warni dengan bentuk daun yang menyerupai hati dan tepian yang bergerigi unik, menjadikannya sangat kontras di antara tanaman hijau lainnya.",
                khasiat = "Luka-Luka\nDemam\nPeradangan Pada Telinga\nAmbeien\nGangguan pencernaan\nSakit perut\nCacing gelang\nMenstruasi tidak teratur\nKeputihan\nDemam\nTipes\nBatuk\nDiabetes Melitus",
                imageUrl = "https://img.freepik.com/free-photo/coleus-scutellarioides-plant-pot_23-2148905218.jpg"
            ),
            TanamanObatEntity(
                id = "2",
                nama = "Selasih",
                namaDaerah = "Nggulasih",
                namaLatin = "Ocimum",
                deskripsi = "Selasih adalah tanaman terna kecil yang berasal dari bagian tropis Afrika hingga Asia Tenggara. Tanaman ini dikenal karena bijinya yang dapat mengembang di dalam air dan sering digunakan dalam minuman penyegar.",
                khasiat = "Menghilangkan stres\nMencegah kanker\nMengontrol gula darah\nMenyembuhkan luka\nMenurunkan kolesterol",
                imageUrl = "https://img.freepik.com/free-photo/fresh-basil-leaves_23-2148154575.jpg"
            ),
            TanamanObatEntity(
                id = "3",
                nama = "Tempuyung",
                namaDaerah = "Tempu",
                namaLatin = "Sonchus Arvensis",
                deskripsi = "Tempuyung adalah tanaman liar yang sering ditemukan di pinggir jalan atau sela-sela parit. Tanaman ini memiliki khasiat luar biasa terutama untuk kesehatan ginjal.",
                khasiat = "Meluruhkan batu ginjal\nMenurunkan asam urat\nAnti-peradangan\nMenurunkan tekanan darah tinggi",
                imageUrl = "https://img.freepik.com/free-photo/green-plant-nature_23-2148111664.jpg"
            ),
            TanamanObatEntity(
                id = "4",
                nama = "Sambung Nyawa",
                namaDaerah = "Siumpu Nyosa",
                namaLatin = "Gynura Procumbens",
                deskripsi = "Sambung Nyawa adalah tanaman obat yang telah lama digunakan di Asia Tenggara. Daunnya bisa dikonsumsi mentah sebagai lalapan atau diolah menjadi jamu.",
                khasiat = "Menurunkan tekanan darah\nMencegah kanker\nMengobati ginjal\nMengobati diabetes",
                imageUrl = "https://img.freepik.com/free-photo/green-leaves-background_23-2148111663.jpg"
            ),
            TanamanObatEntity(
                id = "5",
                nama = "Sambiloto",
                namaDaerah = "Daun Kena",
                namaLatin = "Andrographis Paniculata",
                deskripsi = "Sambiloto dikenal sebagai 'Raja Pahit' karena rasanya yang sangat pahit namun memiliki manfaat medis yang sangat kuat, terutama untuk sistem imun.",
                khasiat = "Meredakan demam\nMeningkatkan daya tahan tubuh\nMencegah diabetes\nMengobati infeksi",
                imageUrl = "https://img.freepik.com/free-photo/herbal-medicine_23-2148154574.jpg"
            ),
            TanamanObatEntity(
                id = "6",
                nama = "Keji Beling",
                namaDaerah = "Enyoh Kilo",
                namaLatin = "Strobilanthes Crispus",
                deskripsi = "Keji Beling adalah tanaman yang sering digunakan sebagai obat herbal untuk membantu meluruhkan kencing batu dan memperlancar buang air kecil.",
                khasiat = "Mengobati kencing batu\nDiabetes melitus\nWasir\nSembelit",
                imageUrl = "https://img.freepik.com/free-photo/green-leaf-texture_23-2148111662.jpg"
            ),
            TanamanObatEntity(
                id = "7",
                nama = "Lidah Buaya",
                namaDaerah = "Timposu",
                namaLatin = "Aloe Vera",
                deskripsi = "Lidah buaya adalah spesies tumbuhan dengan daun berdaging tebal dari genus Aloe. Tumbuhan ini bersifat menahun, berasal dari Jazirah Arab, dan tanaman liarnya telah menyebar ke kawasan tropis.",
                khasiat = "Penyembuhan luka\nKesehatan kulit\nKesehatan rambut\nAntioksidan",
                imageUrl = "https://img.freepik.com/free-photo/aloe-vera-plant_23-2148154573.jpg"
            )
        )
    }

    fun generateDummyResep(): List<ResepEntity> {
        return listOf(
            ResepEntity(
                id = "101",
                judul = "Gel Pereda Luka Bakar",
                deskripsi = "Gel alami dari lidah buaya yang membantu mendinginkan dan mempercepat penyembuhan luka bakar ringan pada kulit.",
                bahan = "Lidah Buaya (2 Lembar)\nMadu Murni (1 Sdm)",
                caraPembuatan = "1. Cuci bersih daun lidah buaya hingga bersih dari kotoran\n2. Kupas kulit luar dan ambil gel bening di dalamnya\n3. Campurkan gel dengan madu, aduk hingga rata\n4. Oleskan tipis-tipis pada area kulit yang terkena luka bakar",
                kategori = "Kesehatan Kulit",
                manfaat = "Luka bakar ringan, Iritasi kulit, Kulit terbakar matahari, Gatal pada kulit, Peradangan kulit, Bakteri luka",
                waktuPersiapan = "5 menit",
                caraPakai = "Dioleskan",
                frekuensi = "2 - 3X Sehari",
                masaSimpan = "3 hari (kulkas)",
                tingkatKesulitan = "Mudah",
                tanamanIds = "7",
                imageUrl = "https://img.freepik.com/free-photo/aloe-vera-gel-glass-jar_23-2148905217.jpg",
                catatan = "Hanya untuk luka bakar ringan. Segera konsultasikan ke dokter jika luka semakin parah atau meluas."
            ),
            ResepEntity(
                id = "102",
                judul = "Minuman Jahe Pereda Batuk",
                deskripsi = "Minuman hangat untuk membantu meredakan batuk dan tenggorokan.",
                bahan = "Jahe (2 ruas)\nLemon (1/2 buah)\nMadu (2 sdm)",
                caraPembuatan = "1. Geprek jahe dan rebus dengan air\n2. Tuangkan air rebusan ke gelas\n3. Tambahkan perasan lemon dan madu\n4. Diminum selagi hangat",
                kategori = "Penyakit Dalam",
                manfaat = "Batuk, Tenggorokan gatal, Flu",
                waktuPersiapan = "10 menit",
                caraPakai = "Diminum",
                frekuensi = "1 - 2X Sehari",
                masaSimpan = "Langsung habis",
                tingkatKesulitan = "Mudah",
                tanamanIds = null,
                imageUrl = "https://img.freepik.com/free-photo/hot-ginger-tea-with-lemon-honey_23-2148293754.jpg"
            ),
            ResepEntity(
                id = "103",
                judul = "Rebusan Pereda Pegal Linu",
                deskripsi = "Ramuan tradisional untuk membantu meredakan nyeri sendi tubuh.",
                bahan = "Kunyit (2 ruas)\nTemulawak (1 ruas)\nGula Merah (secukupnya)",
                caraPembuatan = "1. Iris tipis kunyit dan temulawak\n2. Rebus dengan 2 gelas air\n3. Tambahkan gula merah\n4. Rebus hingga sisa 1 gelas",
                kategori = "Tulang & Sendi",
                manfaat = "Pegal linu, Nyeri sendi, Capek-capek",
                waktuPersiapan = "15 menit",
                caraPakai = "Diminum",
                frekuensi = "1X Sehari",
                masaSimpan = "1 hari",
                tingkatKesulitan = "Sedang",
                tanamanIds = null,
                imageUrl = "https://img.freepik.com/free-photo/turmeric-tea-with-cinnamon-sticks_23-2148293753.jpg"
            ),
            ResepEntity(
                id = "104",
                judul = "Ramuan Obat Batuk Berdahak dan Asma",
                deskripsi = "Ramuan herbal dari daun mayana untuk membantu melegakan pernapasan.",
                bahan = "Daun Mayana (7 lembar)\nAir (2 gelas)",
                caraPembuatan = "1. Cuci bersih daun mayana\n2. Rebus dengan 2 gelas air hingga mendidih\n3. Biarkan hingga air tersisa 1 gelas\n4. Saring dan minum secara rutin",
                kategori = "Penyakit Dalam",
                manfaat = "Batuk berdahak, Asma, Sesak napas",
                waktuPersiapan = "20 menit",
                caraPakai = "Diminum",
                frekuensi = "2X Sehari",
                masaSimpan = "1 hari",
                tingkatKesulitan = "Mudah",
                tanamanIds = "1",
                imageUrl = "https://img.freepik.com/free-photo/green-leaves-nature_23-2148111661.jpg"
            )
        )
    }
}
