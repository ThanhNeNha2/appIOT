package com.iotmasterclass.mqttmaster.activities.mainactivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.iotmasterclass.mqttmaster.R;
import com.iotmasterclass.mqttmaster.databinding.FragmentSecondBinding;
import com.iotmasterclass.mqttmaster.interfaces.ViewSetupInterface;

public class SecondFragment extends Fragment implements ViewSetupInterface {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
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

    }

    @Override
    public void initViews() {
        //Hide Back Button
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void initListeners() {

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }

    @Override
    public void initViewLoaded() {

    }
}