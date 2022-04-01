package com.example.pillnotifier.fragments

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.pillnotifier.R
import com.example.pillnotifier.adapters.ProfileAdapter
import com.example.pillnotifier.adapters.ProfileAdapterCreator
import com.example.pillnotifier.adapters.ProfilesListAdapter
import com.example.pillnotifier.adapters.holders.AbstractProfileViewHolder
import com.example.pillnotifier.adapters.holders.RemovableProfileHolder
import com.example.pillnotifier.adapters.holders.SimpleProfileHolder
import com.example.pillnotifier.model.Profile
import com.example.pillnotifier.model.ProfilesList
import kotlinx.android.synthetic.main.fragment_explore.*

class ExploreFragment : Fragment() {
    // TODO in the future change SimpleProfileHolder to other implementations of AbstractProfileViewHolder
    private val profilesListWithAdaptCreators =
        mutableListOf<Pair<ProfilesList, ProfileAdapterCreator<AbstractProfileViewHolder>>>(
            Pair(ProfilesList("Dependents", listOf(
                    Profile("Sarah Gallagher", "sgallagher"),
                ))){c, pl -> ProfileAdapter(c, pl, R.layout.removable_user_list_item){ v -> SimpleProfileHolder(v) } },

            Pair(ProfilesList("Observers", listOf(
                    Profile("Sarah Gallagher", "sgallagher"), Profile("Anna Smith", "asmith")
                ))){c, pl -> ProfileAdapter(c, pl, R.layout.removable_user_list_item){ v -> SimpleProfileHolder(v) } },

            Pair(ProfilesList("Incoming requests", listOf(
                    Profile("Kimberly White", "kwhite")
            ))){c, pl -> ProfileAdapter(c, pl, R.layout.incoming_request_item){ v -> SimpleProfileHolder(v) } },

            Pair(ProfilesList("Outgoing requests", listOf(
                Profile("Jane Thompson", "jthompson")
            ))){c, pl -> ProfileAdapter(c, pl, R.layout.outgoing_request_item){ v -> SimpleProfileHolder(v) } },
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.profiles_lists_rv)
        recyclerView.adapter = ProfilesListAdapter(requireContext(), profilesListWithAdaptCreators)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )

        val dependentInput: EditText = view.findViewById(R.id.dependent_input)
        dependentInput.setOnEditorActionListener{v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    val imm: InputMethodManager = requireContext()
                        .getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    search_iv.performClick()
                }
            }
            false
        }
        val searchIV: ImageView = view.findViewById(R.id.search_iv)
        searchIV.setOnClickListener{ v ->
            Toast.makeText(context, "Not found user @${dependentInput.text}", Toast.LENGTH_LONG).show()
        }

        return view
    }

}