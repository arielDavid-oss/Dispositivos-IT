package com.davidoss.dispositivos_it;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class FragmentAlarmaBebe extends Fragment {

    private static final String TAG = "FragmentAlarmaBebe";

    private TextView tvAlarma;
    private View vista;
    private Socket socket;

    // Argumentos para la creación del fragmento
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public FragmentAlarmaBebe() {
        // Constructor vacío requerido
    }

    // Método de fábrica para crear una nueva instancia del fragmento con argumentos
    public static FragmentAlarmaBebe newInstance(String param1, String param2) {
        FragmentAlarmaBebe fragment = new FragmentAlarmaBebe();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Inicializa el socket
        socket = SocketManager.getInstance();
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on("LED_SEND_INFO_ALARMABEBE", onLedSendEstadoAlarmaBebe);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        vista = inflater.inflate(R.layout.fragment_alarma_bebe, container, false);
        tvAlarma = vista.findViewById(R.id.tvAlarmaBebe);

        // Emite el evento para solicitar el estado del LED
        if (socket.connected()) {
            emitLedGetEstadoAlarmaBebe();
        }

        return vista;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (socket != null && !socket.connected()) {
            socket.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (socket != null) {
            socket.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.off(Socket.EVENT_CONNECT, onConnect);
            socket.off("LED_SEND_INFO_ALARMABEBE", onLedSendEstadoAlarmaBebe);
        }
    }

    // Emitir evento para obtener el estado del LED
    private void emitLedGetEstadoAlarmaBebe() {
        socket.emit("LED_GET_INFO_ALARMABEBE");
        Log.d(TAG, "Emitido evento: LED_GET_INFO_ALARMABEBE");
    }

    // Maneja el evento de conexión del socket
    private final Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d(TAG, "Conectado al servidor de sockets");
            emitLedGetEstadoAlarmaBebe();
        }
    };

    // Maneja el evento de recepción de datos del socket
    private final Emitter.Listener onLedSendEstadoAlarmaBebe = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (args.length > 0 && args[0] != null) {
                try {
                    Log.e("ARGS",args[0].toString());
                    JSONObject jsonObject = new JSONObject(args[0].toString());
                    String estado = jsonObject.getString("estado");
                    Log.d(TAG, "Estado recuperado: "+estado);

                    // Actualiza la UI en el hilo principal
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> tvAlarma.setText(estado));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error al parsear el JSON: " + e.getMessage());
                }
            } else {
                Log.d(TAG, "No se recibió un estado válido");
            }
        }
    };
}
