package com.itismob.s16.group4mco.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.itismob.s16.group4mco.R

// Adapter responsible for displaying onboarding pages inside a RecyclerView
class OnboardingAdapter(
    private val pages: List<OnboardingPage>      // List of onboarding page models
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    // Inflates the layout for each onboarding page item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding_page, parent, false)
        return OnboardingViewHolder(view)
    }

    // Binds data to the ViewHolder based on the position
    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    // Returns how many pages will be shown
    override fun getItemCount(): Int = pages.size

    // ViewHolder class representing a single onboarding screen
    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Views inside the onboarding item layout
        private val title: TextView = itemView.findViewById(R.id.title)
        private val subtitle: TextView = itemView.findViewById(R.id.subtitle)
        private val chipContainer: LinearLayout = itemView.findViewById(R.id.chipContainer)
        private val formContainer: LinearLayout = itemView.findViewById(R.id.formContainer)
        private val emailHint: TextInputEditText = itemView.findViewById(R.id.emailHint)

        // Binds the data of an OnboardingPage to the UI elements
        fun bind(page: OnboardingPage) {

            // Set title and subtitle text
            title.text = page.title
            subtitle.text = page.subtitle

            // Handle the chip selection options
            chipContainer.removeAllViews()   // Clear old chips before adding new ones
            if (page.chips.isEmpty()) {
                chipContainer.visibility = View.GONE       // Hide if no chips for this page
            } else {
                chipContainer.visibility = View.VISIBLE    // Show chip container
                val inflater = LayoutInflater.from(itemView.context)

                // Dynamically add chips for each option
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

            // Show or hide the email form section
            formContainer.visibility = if (page.showForm) View.VISIBLE else View.GONE

            // If the form is visible, set a default email hint
            if (page.showForm) {
                emailHint.setText("mertkahveci@icloud.com")
            }
        }
    }
}
