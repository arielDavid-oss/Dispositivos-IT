package com.davidoss.dispositivos_it.ui.home;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.davidoss.dispositivos_it.NotificationService;
import com.davidoss.dispositivos_it.R;
import com.davidoss.dispositivos_it.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {
    private static final String CHANNEL_ID = "TimbreChannel";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Solicitar permisos de notificación si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        // Iniciar el servicio en primer plano
        Intent serviceIntent = new Intent(requireContext(), NotificationService.class);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
