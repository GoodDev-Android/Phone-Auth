package android.dev.phoneauthentication.ui

import android.dev.phoneauthentication.R
import android.dev.phoneauthentication.core.viewBinding
import android.dev.phoneauthentication.databinding.FragmentViewNumberBinding
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class ViewNumberFragment : Fragment(R.layout.fragment_view_number) {

    private val binding by viewBinding(FragmentViewNumberBinding::bind)

    private lateinit var auth: FirebaseAuth


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        auth = FirebaseAuth.getInstance()
        val number = arguments?.getString("number")
        if (number?.isNotBlank() == true) binding.number.text = number

        if (auth.currentUser == null) {
            findNavController().navigate(R.id.authNumberFragment)
        } else {
            binding.number.text = auth.currentUser?.phoneNumber
        }

        binding.signOut.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_viewNumberFragment_to_authNumberFragment)
        }
    }

}