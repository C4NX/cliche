package com.cliche.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cliche.app.databinding.FragmentHomeBinding
import com.cliche.app.models.Post
import com.cliche.app.modules.supabaseClient
import com.cliche.app.services.auth.AuthManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Set click listener on the button instead of using android:onClick in XML
        binding.button.setOnClickListener {
            lifecycleScope.launch {
                val result = supabaseClient.from("posts").select().decodeList<Post>();
                Toast.makeText(requireContext(), result.toString(), Toast.LENGTH_LONG).show()
            }
        }

        // Handle disconnect button click
        binding.button2.setOnClickListener {
            lifecycleScope.launch {
                AuthManager.signOut(requireContext())
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}