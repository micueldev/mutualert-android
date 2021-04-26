package com.mcuevapps.mutualert.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mcuevapps.mutualert.R;
import com.mcuevapps.mutualert.common.Constantes;
import com.mcuevapps.mutualert.common.DesignService;
import com.mcuevapps.mutualert.common.MyApp;
import com.mcuevapps.mutualert.common.SharedPreferencesManager;
import com.mcuevapps.mutualert.common.ToastService;
import com.mcuevapps.mutualert.retrofit.MutuAlertClient;
import com.mcuevapps.mutualert.retrofit.MutuAlertService;
import com.mcuevapps.mutualert.retrofit.request.RequestUserAuthNewpassword;
import com.mcuevapps.mutualert.retrofit.request.RequestUserAuthSignup;
import com.mcuevapps.mutualert.retrofit.response.ResponseSuccess;
import com.mcuevapps.mutualert.retrofit.response.ResponseUserAuthSuccess;
import com.mcuevapps.mutualert.ui.HomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterInfoFragment extends Fragment implements View.OnClickListener, TextWatcher {

    private static final String TAG = "RegisterInfoFragment";

    private View view;

    private boolean isNewUser = true;
    private String phone;
    private String code;

    private Button buttonRegister;
    private Button buttonLogin;

    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutApellidoPaterno;
    private TextInputLayout textInputLayoutApellidoMaterno;
    private TextInputLayout textInputLayoutNombres;

    private TextInputEditText editTextPassword;
    private TextInputEditText editTextApellidoPaterno;
    private TextInputEditText editTextApellidoMaterno;
    private TextInputEditText editTextNombres;

    private DesignService designService;
    private MutuAlertClient mutuAlertClient;
    private MutuAlertService mutuAlertService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register_info, container, false);

        Bundle arguments = getArguments();
        if(arguments!=null){
            isNewUser = arguments.getBoolean("isNewUser");
            phone = arguments.getString("phone");
            code = arguments.getString("code");
        }

        retrofitInit();
        initUI();
        return view;
    }

    private void retrofitInit() {
        mutuAlertClient = MutuAlertClient.getInstance();
        mutuAlertService = mutuAlertClient.getMutuAlertService();
    }

    private void initUI() {
        designService = new DesignService(MyApp.getContext());

        buttonRegister = (Button) view.findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(this);
        designService.ButtonSecondaryDisable(buttonRegister);

        buttonLogin = (Button) view.findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(this);

        editTextPassword = (TextInputEditText) view.findViewById(R.id.editTextPassword);
        editTextPassword.addTextChangedListener(this);
        editTextApellidoPaterno = (TextInputEditText) view.findViewById(R.id.editTextApellidoPaterno);
        editTextApellidoPaterno.addTextChangedListener(this);
        editTextApellidoMaterno = (TextInputEditText) view.findViewById(R.id.editTextApellidoMaterno);
        editTextApellidoMaterno.addTextChangedListener(this);
        editTextNombres = (TextInputEditText) view.findViewById(R.id.editTextNombres);
        editTextNombres.addTextChangedListener(this);

        if(!isNewUser){
            textInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.textInputLayoutPassword);
            textInputLayoutPassword.setHint(getString(R.string.new_password));

            textInputLayoutApellidoPaterno = (TextInputLayout) view.findViewById(R.id.textInputLayoutApellidoPaterno);
            textInputLayoutApellidoPaterno.setVisibility(View.GONE);

            textInputLayoutApellidoMaterno = (TextInputLayout) view.findViewById(R.id.textInputLayoutApellidoMaterno);
            textInputLayoutApellidoMaterno.setVisibility(View.GONE);

            textInputLayoutNombres = (TextInputLayout) view.findViewById(R.id.textInputLayoutNombres);
            textInputLayoutNombres.setVisibility(View.GONE);

            buttonRegister.setText(getString(R.string.confirm));
            buttonLogin.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.buttonRegister:
                clickButtonRegister();
                break;
            case R.id.buttonLogin:
                goToLogin();
                break;
        }
    }

    private void clickButtonRegister() {
        if(isNewUser){
            signUp();
        } else {
            newPassword();
        }
    }

    private void signUp(){
        RequestUserAuthSignup requestUserAuthSignup = new RequestUserAuthSignup(
                phone, code, editTextPassword.getText().toString(), editTextApellidoPaterno.getText().toString(),
                editTextApellidoMaterno.getText().toString(), editTextNombres.getText().toString() );
        Call<ResponseUserAuthSuccess> call = mutuAlertService.signUp(requestUserAuthSignup);
        call.enqueue(new Callback<ResponseUserAuthSuccess>() {
            @Override
            public void onResponse(Call<ResponseUserAuthSuccess> call, Response<ResponseUserAuthSuccess> response) {
                if( response.isSuccessful() ){
                    SharedPreferencesManager.setSomeStringValue(Constantes.PREF_USERNAME, phone);
                    SharedPreferencesManager.setSomeStringValue(Constantes.PREF_TOKEN, response.body().getData().getToken());
                    SharedPreferencesManager.setSomeStringValue(Constantes.PREF_APELLIDOPAT, response.body().getData().getProfile().getApepat());
                    SharedPreferencesManager.setSomeStringValue(Constantes.PREF_APELLIDOMAT, response.body().getData().getProfile().getApemat());
                    SharedPreferencesManager.setSomeStringValue(Constantes.PREF_NOMBRES, response.body().getData().getProfile().getNombres());
                    SharedPreferencesManager.setSomeStringValue(Constantes.PREF_EMAIL, response.body().getData().getProfile().getEmail());
                    SharedPreferencesManager.setSomeStringValue(Constantes.PREF_AVATAR, response.body().getData().getProfile().getAvatar());
                    goToDashboard();
                } else {
                    ToastService.showErrorResponse(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseUserAuthSuccess> call, Throwable t) {
                Toast.makeText(MyApp.getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToDashboard() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void newPassword(){
        RequestUserAuthNewpassword requestUserAuthNewpassword = new RequestUserAuthNewpassword(
                code, phone, editTextPassword.getText().toString() );
        Call<ResponseSuccess> call = mutuAlertService.newPassword(requestUserAuthNewpassword);
        call.enqueue(new Callback<ResponseSuccess>() {
            @Override
            public void onResponse(Call<ResponseSuccess> call, Response<ResponseSuccess> response) {
                if( response.isSuccessful() ){
                    SharedPreferencesManager.setSomeStringValue(Constantes.PREF_USERNAME, phone);
                    goToLogin();
                } else {
                    ToastService.showErrorResponse(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseSuccess> call, Throwable t) {
                Toast.makeText(MyApp.getContext(), getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if( formValid() ){
            designService.ButtonSecondaryEnable(buttonRegister);
        } else {
            designService.ButtonSecondaryDisable(buttonRegister);
        }
    }

    @Override
    public void afterTextChanged(Editable s) { }

    public boolean formValid(){
        if(isNewUser){
            if( editTextApellidoPaterno.getText().toString().length() < Constantes.PERSON_NAME_LENGTH)
                return false;

            if( editTextApellidoMaterno.getText().toString().length() <Constantes.PERSON_NAME_LENGTH )
                return false;

            if( editTextNombres.getText().toString().length() < Constantes.PERSON_NAME_LENGTH )
                return false;
        }

        if( editTextPassword.getText().toString().length() < Constantes.PASSWORD_LENGTH )
            return false;

        return true;
    }
}