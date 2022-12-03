package android.dev.phoneauthentication.ui

import android.dev.phoneauthentication.R
import android.dev.phoneauthentication.core.viewBinding
import android.dev.phoneauthentication.databinding.FragmentAuthNumberBinding
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class AuthNumberFragment : Fragment(R.layout.fragment_auth_number) {

    private val binding by viewBinding(FragmentAuthNumberBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.registrationBtn.setOnClickListener {
            if (binding.numberEdit.text?.isNotEmpty() == true) {
                val bundle = Bundle()
                bundle.putString("number", binding.numberEdit.text.toString())
                findNavController().navigate(
                    R.id.smsCodeFragment,
                    bundle
                )
            } else {
                Toast.makeText(requireContext(), "Raqamni kiriting!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}