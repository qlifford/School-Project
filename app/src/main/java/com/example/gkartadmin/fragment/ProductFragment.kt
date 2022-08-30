package com.example.gkartadmin.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.gkartadmin.R
import com.example.gkartadmin.databinding.FragmentProductBinding


class ProductFragment : Fragment() {
    private lateinit var binding: FragmentProductBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductBinding.inflate(layoutInflater)

        binding.fab.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductFragment::class.java))

        }

        return binding.root

    }
}