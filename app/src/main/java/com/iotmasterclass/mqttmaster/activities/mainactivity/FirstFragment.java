package com.iotmasterclass.mqttmaster.activities.mainactivity;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.Gson;
import com.iotmasterclass.mqttmaster.R;
import com.iotmasterclass.mqttmaster.databinding.FragmentFirstBinding;
import com.iotmasterclass.mqttmaster.interfaces.ViewSetupInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FirstFragment extends Fragment implements ViewSetupInterface {

    private FragmentFirstBinding binding;
    private MainActivityViewModel mViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewSetupInterfaceMethods();    //initViewModels() -> initViews() -> initListeners() -> initViewLoaded()
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void initViewModels() {
        mViewModel = ((MainActivity) getActivity()).mViewModel;
        mViewModel.getTopicMessage().observe(getViewLifecycleOwner(), new Observer<HashMap<String, HashMap<String, Object>>>() {
            @Override
            public void onChanged(HashMap<String, HashMap<String, Object>> topicMessage) {
                // {"wokwi001/iot/telemetry/sensor":{"temp":16.3,"device_id":"wokwi001","humidity":55.0,"type":"sensor"}}
                System.out.println("Message Changed: " + (new Gson()).toJson(topicMessage));
                // Update Sensor Data
                HashMap<String, Object> sensorData = topicMessage.get(binding.menuTextView.getText() + "/iot/telemetry/sensor");
                if (sensorData != null){
                    //Double humidity = Double.valueOf(sensorData.get("humidity"));
                    //Double temperature = Double.valueOf(sensorData.get("temp"));
                    binding.humidityTextview.setText(sensorData.get("humidity").toString());
                    binding.temperatureTextview.setText(sensorData.get("temp").toString());
                }

                // Update Lamp Data
                HashMap<String, Object> lampData = topicMessage.get(binding.menuTextView.getText() + "/iot/telemetry/lamp");
                if (lampData != null){
                    if(lampData.get("red_led").toString().equals("OFF")){
                        //turn off red lamp
                        binding.redLampBtn.setText(R.string.switch_on);
                        binding.redLampImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.lightbulb_off));
                    }else{
                        binding.redLampBtn.setText(R.string.switch_off);
                        binding.redLampImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.lightbulb_on));
                    }

                    if(lampData.get("blue_led").toString().equals("OFF")){
                        //turn off red lamp
                        binding.blueLampBtn.setText(getString(R.string.switch_on));
                        binding.blueLampImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.lightbulb_off));
                    }else{
                        binding.blueLampBtn.setText(getString(R.string.switch_off));
                        binding.blueLampImageView.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.lightbulb_on));
                    }

                }

            }
        });
    }

    @Override
    public void initViews() {
        List devicesList = new ArrayList<>(Arrays.asList("wokwi001", "wokwi002", "ESP32-TableLamp-001"));
        ArrayAdapter deviceAdapter = new ArrayAdapter(requireContext(), R.layout.list_item, devicesList);
        binding.menuTextView.setAdapter(deviceAdapter);
        binding.menuTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ((MainActivity) getActivity()).getIoTDeviceStatus(editable.toString());
            }
        });
        binding.menuTextView.setText(devicesList.get(0).toString(), false);

    }

    @Override
    public void initListeners() {
        //NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_FirstFragment_to_SecondFragment);
        binding.blueLampBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newState = "on";
                String btnTxt = binding.blueLampBtn.getText().toString();
                if (btnTxt.equals(getString(R.string.switch_off))){
                    newState = "off";
                }
                String controlString = binding.menuTextView.getText().toString() + "/lamp/blue/" + newState;
                ((MainActivity) getActivity()).publishMessage(mViewModel.MQTT_CONTROL_TOPIC, controlString);
            }
        });

        binding.redLampBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newState = "on";
                if (binding.redLampBtn.getText().toString().equals(getString(R.string.switch_off))){
                    newState = "off";
                }
                String controlString = binding.menuTextView.getText().toString() + "/lamp/red/" + newState;
                ((MainActivity) getActivity()).publishMessage(mViewModel.MQTT_CONTROL_TOPIC, controlString);
            }
        });

    }

    @Override
    public void initViewLoaded() {
        ((MainActivity) getActivity()).startMqttConnection();

    }

}