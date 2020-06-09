package com.example.licenta;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    private CodeScanner mCodeScanner;
    private Fragment listFragment;
    private Button startBtn;
    private Button goBackBtn;
    private Fragment scanFragment;
    private Fragment acctFragment;
    private TextView txtResult;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;


    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.accountfragment, container, false);
        startBtn = root.findViewById(R.id.startQR);
        scanFragment = new ScanFragment();
        acctFragment = new AccountFragment();
        txtResult = root.findViewById(R.id.textResult);
        firebaseFirestore = FirebaseFirestore.getInstance();
        ScanFragment scfrag = new ScanFragment();
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();
        //txtResult.setText("");

        firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String text = task.getResult().getString("links");
                        if(text==null){
                            txtResult.setText("Your current QRea theme is waiting to be discovered!");
                        } else {
                            txtResult.setText("Your current QRea theme is " + text + ". Now, please add a new text under the QRea category explaining how the surroundings made you feel. Do not forget to add a suitable picture!");
                        }
                    }
                }
            }
        });


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startBtn.getText().equals("Initiate QRea Scan")){
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.actLayout, scanFragment);
                fragmentTransaction.commit();
                startBtn.setText("View scan results");} else {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.actLayout, acctFragment);
                    fragmentTransaction.commit();
                    startBtn.setText("Initiate QRea Scan");
                }
            }
        });
        return root;
    }


}
