package com.mobdeve.s16.group4mco.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.mobdeve.s16.group4mco.R

class OnboardingAdapter(
    private val pages: List<OnboardingPage>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_page, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount(): Int = pages.size

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val subtitle: TextView = itemView.findViewById(R.id.subtitle)
        private val chipContainer: LinearLayout = itemView.findViewById(R.id.chipContainer)
        private val formContainer: LinearLayout = itemView.findViewById(R.id.formContainer)
        private val emailHint: TextInputEditText = itemView.findViewById(R.id.emailHint)

        fun bind(page: OnboardingPage) {
            title.text = page.title
            subtitle.text = page.subtitle

            chipContainer.removeAllViews()
            if (page.chips.isEmpty()) {
                chipContainer.visibility = View.GONE
            } else {
                chipContainer.visibility = View.VISIBLE
                val inflater = LayoutInflater.from(itemView.context)
                page.chips.forEach {
                    val chip = inflater.inflate(
                        R.layout.view_onboarding_chip,
                        chipContainer,
                        false
                    ) as TextView
                    chip.text = it
                    chipContainer.addView(chip)
                }
            }

            formContainer.visibility = if (page.showForm) View.VISIBLE else View.GONE
            if (page.showForm) {
                emailHint.setText("mertkahveci@icloud.com")
            }
        }
    }
}


