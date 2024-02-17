package com.gdsc.bingo.ui.pinpoint.front

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gdsc.bingo.R
import com.gdsc.bingo.databinding.FragmentPintPointFrontBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textview.MaterialTextView
import java.util.*

class PintPointFrontFragment : Fragment() {

    private lateinit var locationTextView: MaterialTextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val binding by lazy {
        FragmentPintPointFrontBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationTextView = view.findViewById(R.id.pin_point_front_text_view_current_user_location)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (isLocationEnabled()) {
            requestLocation()
        } else {
            locationTextView.text = "Izinkan lokasi Anda untuk menemukan TPU terdekat"
        }

        setupCardSearch()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val address = getAddressFromLocation(location)
                locationTextView.text = address ?: "Lokasi tidak ditemukan"
            } else {
                locationTextView.text = "Lokasi tidak ditemukan"
            }
        }
    }

    private fun getAddressFromLocation(location: Location): String? {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            val subLocality = address.subLocality
            return subLocality ?: "Lokasi terkini: ${location.latitude}, ${location.longitude}"
        }
        return null
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun setupCardSearch() {
        binding.pinPointFrontCardSearch.setOnClickListener {
            val destination = PintPointFrontFragmentDirections.actionPintPointFrontFragmentToSearchMapsFragment()
            findNavController().navigate(destination)
        }
    }
}