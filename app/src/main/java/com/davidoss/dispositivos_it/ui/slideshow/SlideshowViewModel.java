package com.davidoss.dispositivos_it.ui.slideshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SlideshowViewModel extends ViewModel {

    private final MutableLiveData<String> temperature = new MutableLiveData<>();
    private final MutableLiveData<String> threshold = new MutableLiveData<>(); // Para la temperatura predeterminada

    public SlideshowViewModel() {
        // Inicializa la temperatura y el umbral por defecto
        temperature.setValue("0.0°C");
        threshold.setValue("25.0"); // Valor inicial para la temperatura predeterminada
    }

    public LiveData<String> getTemperature() {
        return temperature;
    }

    public LiveData<String> getThreshold() {
        return threshold;
    }

    // Método para actualizar la temperatura
    public void setTemperature(String temp) {
        temperature.setValue(temp);
    }

    // Método para actualizar la temperatura predeterminada
    public void setThreshold(String temp) {
        threshold.setValue(temp);
        // Aquí deberías implementar la lógica para enviar el nuevo umbral al ESP8266
    }
}
