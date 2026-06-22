package com.project.topakuli.ui.intro

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.project.topakuli.R
import com.project.topakuli.databinding.ActivityIntroBinding
import com.project.topakuli.ui.MainActivity

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var introAdapter: IntroAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSlides()
        setupIndicators()
        setCurrentIndicator(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
                updateButtons(position)
            }
        })

        binding.btnNext.setOnClickListener {
            binding.viewPager.currentItem += 1
        }

        binding.btnSkip.setOnClickListener {
            finishIntro()
        }

        binding.btnStart.setOnClickListener {
            finishIntro()
        }
    }

    private fun setupSlides() {
        val slides = listOf(
            IntroSlide(
                "Kenali Tanaman Obat di Desa Pakuli",
                "Jelajahi 40+ spesies tanaman obat Desa Pakuli lengkap dengan manfaat, kandungan, dan cara penggunaannya secara aman.",
                R.drawable.intro_1
            ),
            IntroSlide(
                "Identifikasi Tanaman Obat Sekitarmu Seketika",
                "arahkan kamera ke tanaman obat mana saja dan biarkan Topakuli mengenalinya untukmu menggunakan teknologi AI yang terlatih dengan data lokal.",
                R.drawable.intro_2
            ),
            IntroSlide(
                "Lestarikan Kearifan Lokal Bersama Warga",
                "Ajukan resep, koreksi data, dan tanya langsung ke kader desa. Setiap kontribusi mempererat warisan herbal Desa Pakuli untuk generasi mendatang.",
                R.drawable.intro_3
            )
        )

        introAdapter = IntroAdapter(slides)
        binding.viewPager.adapter = introAdapter
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(introAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }
            binding.indicatorContainer.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val childCount = binding.indicatorContainer.childCount
        for (i in 0 until childCount) {
            val imageView = binding.indicatorContainer.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }

    private fun updateButtons(position: Int) {
        if (position == introAdapter.itemCount - 1) {
            binding.btnSkip.visibility = View.GONE
            binding.btnNext.visibility = View.GONE
            binding.btnStart.visibility = View.VISIBLE
        } else {
            binding.btnSkip.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE
            binding.btnStart.visibility = View.GONE
        }
    }

    private fun finishIntro() {
        // Mark intro as completed
        val sharedPref = getSharedPreferences("topakuli_pref", MODE_PRIVATE)
        sharedPref.edit().putBoolean("is_first_install", false).apply()

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}