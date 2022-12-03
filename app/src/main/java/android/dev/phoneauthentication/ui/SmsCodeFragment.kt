package android.dev.phoneauthentication.ui

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.dev.phoneauthentication.R
import android.dev.phoneauthentication.core.NetworkHelper
import android.dev.phoneauthentication.core.viewBinding
import android.dev.phoneauthentication.databinding.FragmentSmsCodeBinding
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class SmsCodeFragment : Fragment(R.layout.fragment_sms_code) {

    private val binding by viewBinding(FragmentSmsCodeBinding::bind)
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var progressDialog: ProgressDialog
    private lateinit var countDownTimer: CountDownTimer
    private var num: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        auth = FirebaseAuth.getInstance()

        if (NetworkHelper().isNetworkConnected(requireContext())) {
            val number = arguments?.getString("number")
            num = number
            binding.numberText.text =
                "Bir martalik kod +998${number?.substring(0, 5)}-**-**\nraqamiga yuborildi"
            progressDialog()

            sendVerificationCode("+998$number")

            binding.getCode.addTextChangedListener {

                val code = it.toString()
                if (code.length == 6) {
                    val cred =
                        storedVerificationId?.let { it1 -> PhoneAuthProvider.getCredential(it1, code) }
                    if (cred != null) {
                        signInWithPhoneAuthCredential(cred)
                    }
                }
                if (number != null) {
                    resendCode(number)
                }

            }
        } else Snackbar.make(requireContext(), binding.root, "No internet", Snackbar.LENGTH_SHORT).show()
    }

    private fun resendCode(number: String) {
        binding.refreshCode.setOnClickListener {
            progressDialog.show()
            val options = resendToken?.let {
                PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(number)       // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(requireActivity())                 // Activity (for callback binding)
                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                    .setForceResendingToken(it)
                    .build()

            }
            if (options != null) {
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
            binding.refreshCode.visibility = View.GONE
        }
    }

    private fun sendVerificationCode(number: String) {
        progressDialog.setMessage("Itlimos kuting...")
        progressDialog.show()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            progressDialog.dismiss()
            countDownTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (millisUntilFinished / 1000 < 10) {
                        binding.timer.text = "00:0${millisUntilFinished / 1000}"
                    } else {
                        binding.timer.text = "00:${millisUntilFinished / 1000}"
                    }
                }

                override fun onFinish() {
                    binding.refreshCode.visibility = View.VISIBLE
                    num?.let { resendCode(it) }
                }
            }.start()
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)

        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)
            progressDialog.dismiss()
            countDownTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (millisUntilFinished / 1000 < 10) {
                        binding.timer.text = "00:0${millisUntilFinished / 1000}"
                    } else {
                        binding.timer.text = "00:${millisUntilFinished / 1000}"
                    }
                }

                override fun onFinish() {
                    binding.refreshCode.visibility = View.VISIBLE
                    num?.let { resendCode(it) }
                }
            }.start()
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d(TAG, "onVerificationFailed: Invalid request")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d(TAG, "onVerificationFailed: Too many request exception")
            }
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")
            progressDialog.dismiss()
            countDownTimer = object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (millisUntilFinished / 1000 < 10) {
                        binding.timer.text = "00:0${millisUntilFinished / 1000}"
                    } else {
                        binding.timer.text = "00:${millisUntilFinished / 1000}"
                    }
                }

                override fun onFinish() {
                    binding.refreshCode.visibility = View.VISIBLE
                    num?.let { resendCode(it) }
                }
            }.start()
            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                progressDialog.dismiss()
                val bundle = Bundle()
                bundle.putString("number", num)
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    findNavController().navigate(
                        R.id.viewNumberFragment,
                        bundle
                    )
                    binding.getCode.text?.clear()
                    countDownTimer.onFinish()
                    countDownTimer.cancel()
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(requireContext(), "Tasdiqlash kodi xato", Toast.LENGTH_SHORT)
                            .show()
                    }
                    // Update UI
                }
            }
    }

    private fun progressDialog() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setCancelable(false)
    }

}