package com.ifam.amigopetfixo.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ifam.amigopetfixo.R
import com.ifam.amigopetfixo.databinding.ApresentacaoBinding
import kotlin.math.abs

class Apresentacao : AppCompatActivity() {

    private lateinit var binding: ApresentacaoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ApresentacaoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        })

        val tips = listOf(
            Tip("Bem-vindo ao Amigo Pet", "A maior comunidade dedicada a conectar apaixonados por animais na sua região.", R.drawable.logo_png, R.color.cor_slide_boas_vindas),
            Tip("Interação e Eventos", "Adicione postagens, compartilhe momentos e acompanhe os eventos pets da sua cidade.", R.drawable.ic_jornal_dobrado, R.color.cor_slide_comunidade),
            Tip("Tudo Perto de Você", "Encontre Pet Shops, Veterinários e ONGs rapidamente através do nosso mapa.", R.drawable.ic_location_on_map, R.color.cor_slide_mapa),
            Tip("Adoção e Doação", "Ajude um amiguinho a encontrar um lar ou encontre produtos essenciais para o seu pet.", R.drawable.ic_animais_de_estimacao, R.color.cor_slide_adocao)
        )

        configurarViewPager(tips)
        configurarBotoes(tips)
    }

    private fun configurarViewPager(tips: List<Tip>) {
        binding.viewPage.adapter = OnboardingAdapter(tips)
        addDots(tips.size, 0)

        binding.viewPage.setPageTransformer { page, position ->
            page.alpha = 1f - abs(position)
            page.translationX = -position * page.width / 2
        }

        binding.viewPage.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                addDots(tips.size, position)

                if (position == tips.size - 1) {
                    binding.next.text = "COMEÇAR"
                } else {
                    binding.next.text = "PRÓXIMO"
                }
            }
        })
    }

    private fun configurarBotoes(tips: List<Tip>) {
        binding.next.setOnClickListener {
            if (binding.viewPage.currentItem >= tips.size - 1) {
                irParaSetupPerfil()
            } else {
                binding.viewPage.currentItem += 1
            }
        }

        binding.back.setOnClickListener {
            irParaSetupPerfil()
        }
    }

    private fun irParaSetupPerfil() {
        startActivity(Intent(this, SetupPerfilActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun addDots(size: Int, position: Int) {
        binding.dots.removeAllViews()
        for (i in 0 until size) {
            val textView = TextView(this).apply {
                text = "•"
                textSize = 35f
                setTextColor(
                    if (position == i) Color.parseColor("#212121")
                    else Color.parseColor("#9E9E9E")
                )
            }
            binding.dots.addView(textView)
        }
    }

    private inner class OnboardingAdapter(private val tips: List<Tip>) : RecyclerView.Adapter<OnboardingAdapter.TipViewHolder>() {

        inner class TipViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
            val txtTitulo: TextView = view.findViewById(R.id.txtTituloSlide)
            val txtDescricao: TextView = view.findViewById(R.id.txtDescricaoSlide)
            val imgIcone: ImageView = view.findViewById(R.id.imgIconeSlide)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.slide1, parent, false)
            return TipViewHolder(view)
        }

        override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
            val tip = tips[position]
            holder.txtTitulo.text = tip.title
            holder.txtDescricao.text = tip.description
            holder.imgIcone.setImageResource(tip.icon)

            val corOficial = ContextCompat.getColor(this@Apresentacao, tip.backgroundColorRes)
            holder.view.setBackgroundColor(corOficial)
        }

        override fun getItemCount(): Int = tips.size
    }
}