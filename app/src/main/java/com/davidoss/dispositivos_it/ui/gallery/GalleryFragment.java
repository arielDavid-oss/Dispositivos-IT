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

import com.davidoss.dispositivos_it.MainActivity; // Asegúrate de importar tu MainActivity u otra actividad relevante
import com.davidoss.dispositivos_it.R;
import com.davidoss.dispositivos_it.SocketManager;
import com.davidoss.dispositivos_it.databinding.FragmentGalleryBinding;

import io.socket.client.Socket;

public class GalleryFragment extends Fragment {

    private static final String CHANNEL_ID = "TimbreChannel";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private ImageView photoView;
    private FragmentGalleryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializar las vistas
        photoView = binding.ivPhoto;
        if (photoView == null) {
            Log.e("GalleryFragment", "photoView is null");
        }

        // Crear canal de notificación
        createNotificationChannel();

        // Solicitar permisos de notificación si es necesario
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        // Configurar el Socket
        setupSocket();

        return root;
    }

    private void setupSocket() {
        try {
            Socket socket = SocketManager.getInstance();
            if (socket == null) {
                Log.e("GalleryFragment", "SocketManager.getInstance() returned null");
                return;
            }

            // Escuchar eventos del socket
            socket.on(Socket.EVENT_CONNECT, args -> Log.d("SOCKET", "Conectado"));

            socket.on("LED_SEND", args -> {
                Boolean estado = args[0].toString().equals("true");
                // Maneja el estado del LED si es necesario
            });

            socket.on("timbre_push", args -> showNotification("Tocan El timbre", "Se ha presionado el timbre."));

            socket.on("foto_timbre", args -> {
                String base64Image = args[0].toString();
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                requireActivity().runOnUiThread(() -> {
                    if (photoView != null) {
                        photoView.setImageBitmap(bitmap);
                    } else {
                        Log.e("GalleryFragment", "photoView is null");
                    }
                });
            });

            binding.btnTakePhoto.setOnClickListener(v -> socket.emit("take_photo", "capture"));

            socket.connect();
        } catch (Exception e) {
            Log.e("GalleryFragment", "Exception in setupSocket: " + e.getMessage(), e);
        }
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

            NotificationManager notificationManager = requireActivity().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String text) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.timbre)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(getPendingIntentForActivity())
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
            notificationManager.notify(1, builder.build());
        } catch (Exception e) {
            Log.e("Notification", "Error showing notification: " + e.getMessage());
        }
    }

    private PendingIntent getPendingIntentForActivity() {
        Intent intent = new Intent(requireContext(), MainActivity.class); // Asegúrate de que MainActivity es la actividad correcta
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("FRAGMENT_KEY", "GALLERY_FRAGMENT");

        return PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
