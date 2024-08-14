package com.davidoss.dispositivos_it.ui.slideshow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.davidoss.dispositivos_it.R;
import com.davidoss.dispositivos_it.SocketManager;
import com.davidoss.dispositivos_it.databinding.FragmentSlideshowBinding;

import io.socket.client.Socket;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private Socket socket;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView temperatureTextView = binding.textSlideshow;
        final EditText thresholdInput = binding.thresholdInput;
        Button requestTempButton = binding.requestTempButton;
        Button setThresholdButton = binding.setThresholdButton;

        // Inicializar el socket
        socket = SocketManager.getInstance();

        // Configurar eventos de socket
        socket.on(Socket.EVENT_CONNECT, args -> Log.d("SOCKET", "Conectado"));

        // Escuchar el evento de actualización de temperatura
        socket.on("temperature", args -> {
            if (args.length > 0) {
                final String temperature = (String) args[0];
                requireActivity().runOnUiThread(() -> temperatureTextView.setText("Temperatura: " + temperature + "°C"));
            }
        });

        // Escuchar eventos de error (opcional)
       // socket.on(Socket.EVENT_ERROR, args -> Log.d("SOCKET", "Error: " + args[0]));

        // Conectar el socket
        socket.connect();

        // Botón para solicitar la temperatura
        requestTempButton.setOnClickListener(v -> {
            socket.emit("REQUEST_TEMP");
        });

        // Botón para ajustar el umbral de temperatura
        setThresholdButton.setOnClickListener(v -> {
            String threshold = thresholdInput.getText().toString();
            socket.emit("SET_TEMP_THRESHOLD", threshold);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Desconectar el socket cuando la vista se destruya
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
        // Limpiar la referencia al binding
        binding = null;
    }
}
