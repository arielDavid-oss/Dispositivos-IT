package com.davidoss.dispositivos_it.ui.gallery;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.davidoss.dispositivos_it.R;
import com.davidoss.dispositivos_it.SocketManager;
import com.davidoss.dispositivos_it.databinding.FragmentGalleryBinding;
import com.davidoss.dispositivos_it.ui.home.HomeFragment;

import io.socket.client.Socket;

public class GalleryFragment extends Fragment {

    private static final String CHANNEL_ID = "TimbreChannel";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private ImageView image;
    private ImageView photoView;
    private FragmentGalleryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar las vistas
        image = binding.ivLed;
       // photoView = binding.ivPhoto;

        // Crear canal de notificación
        createNotificationChannel();

        // Solicitar permisos de notificación si es necesario
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        try {
            // Crear instancia de nuestro Socket Manager
            Socket socket = SocketManager.getInstance();

            // Escuchar un evento predefinido
            socket.on(Socket.EVENT_CONNECT, args -> {
                Log.d("SOCKET", "Conectado");
            });

            // Escuchar eventos personalizados
            socket.on("LED_SEND", args -> {
                Boolean estado = args[0].toString().equals("true");
                if (estado) {
                    image.setImageResource(R.drawable.linterna2);
                } else {
                    image.setImageResource(R.drawable.linterna);
                }
            });

            socket.on("timbre_push", args -> {
                // Mostrar notificación cuando se presione el timbre
                showNotification("Tocan El timbre", "Se ha presionado el timbre.");
            });

            socket.on("foto_timbre", args -> {
                String base64Image = args[0].toString();
                // Decodificar la imagen en Base64
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                // Mostrar la imagen en el ImageView
                requireActivity().runOnUiThread(() -> photoView.setImageBitmap(bitmap));
            });

            // Emitir un evento junto con su carga útil o mensaje
            socket.emit("LED_GET", "");

            binding.btnLed.setOnClickListener(v -> {
                socket.emit("LED_SET", "");
            });

            socket.connect();
        } catch (Exception e) {
            Log.d("Error", e.getMessage());
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Timbre Channel";
            String description = "Channel for Timbre notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String text) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Si no se ha concedido el permiso, solicita el permiso
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        try {
            // Crear y mostrar la notificación
            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.timbre)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(getPendingIntentForFragment())  // Añade la intención para abrir el fragmento
                    .setAutoCancel(true); // Cierra la notificación al tocarla

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            Log.e("Notification", "Permission denied: " + e.getMessage());
        }
    }


    private PendingIntent getPendingIntentForFragment() {
        Intent intent = new Intent(requireContext(), HomeFragment.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Añade un extra para indicar qué fragmento debe mostrarse
        intent.putExtra("FRAGMENT_KEY", "GALLERY_FRAGMENT"); // Cambia esto según el fragmento que deseas abrir

        return PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
