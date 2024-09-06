package com.zeal.paymentassignment.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zeal.paymentassignment.R
import com.zeal.paymentassignment.core.DialogHelper
import com.zeal.paymentassignment.core.FlowDataObject
import com.zeal.paymentassignment.data.PaymentModel
import com.zeal.paymentassignment.databinding.FragmentSwipeFragment2Binding
import java.util.UUID

const val REQUEST_CODE_LOYALTY = 1001

class SwipeCardFragment : Fragment() {
    private val binding by lazy {
        FragmentSwipeFragment2Binding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        DialogHelper.showPanDialog(requireContext(), {
            startLoyaltyApp(
                PaymentModel(id =  UUID.randomUUID().toString(), amount = FlowDataObject.getInstance().amount, cardNumber = it))

        }) {
            findNavController().popBackStack()
        }


        return binding.root
    }

    private fun callTheBank(amount:String) {
        Thread {

            DialogHelper.showLoadingDialog(requireActivity(), "Sending ${amount} Transaction to The Bank")
            Thread.sleep(2000)
            DialogHelper.showLoadingDialog(requireActivity(), "Receiving Bank Response")
            Thread.sleep(1000)
            DialogHelper.hideLoading(requireActivity())

            requireActivity().runOnUiThread {
                //send nav argument to print receipt fragment
                val bundle = Bundle()
                bundle.putString("amount", amount)
                findNavController().navigate(R.id.action_swipeCardFragment_to_printReceiptFragment, bundle)
            }
        }.start()
    }

    fun startLoyaltyApp(transaction: PaymentModel) {
        val intent = Intent().apply {
            action = "com.example.loyaltyapp.SHOW_DIALOG"
            putExtra("transactionId", transaction.id)
            putExtra("amount", FlowDataObject.getInstance().amount)
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_LOYALTY)
        }catch (ex:Exception){
            Toast.makeText(requireContext(), "Loyalty App Not Installed", Toast.LENGTH_SHORT).show()
            // if loyalty app not installed return to enter amount fragment
            findNavController().navigate(R.id.enterAmountDataFragment)

        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_LOYALTY && resultCode == Activity.RESULT_OK) {
            val finalAmount = data?.getFloatExtra("finalAmount", 0.0f)
            if (finalAmount != null && finalAmount > 0) {
                // Proceed with bank transaction using finalAmount
                callTheBank(finalAmount.toString())
            } else {
                // Complete transaction without contacting the bank
                findNavController().navigate(R.id.action_swipeCardFragment_to_printReceiptFragment)
            }
        }
    }


}