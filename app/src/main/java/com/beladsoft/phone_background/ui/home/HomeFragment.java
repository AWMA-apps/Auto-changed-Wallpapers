package com.beladsoft.phone_background.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beladsoft.phone_background.MODEL.Constants;
import com.beladsoft.phone_background.Objects.HomeObjects;
import com.beladsoft.phone_background.FRAMWORK.Provider;
import com.beladsoft.phone_background.R;
import com.beladsoft.phone_background.databinding.FragmentHomeBinding;
import com.beladsoft.phone_background.ui.AdapterHome;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private StorageReference sr;
    private Context context;
    private AdapterHome adapterHome;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        context = getContext();
        sr = FirebaseStorage.getInstance().getReference();
        recyclerView = root.findViewById(R.id.homeRV);
        //recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setLayoutManager(new GridLayoutManager(context,
                Provider.calculateNoOfColumns(context,202)));
        recyclerView.hasFixedSize();
        progressBar = root.findViewById(R.id.pbLoading);

        adapterHome = new AdapterHome(context, sr);
        recyclerView.setAdapter(adapterHome);
        LoadFolders();
        return root;
    }

    public HomeFragment() {
    }

    private void LoadFolders() {
        progressBar.setVisibility(View.VISIBLE);
        StorageReference listRef = sr;
        StringBuilder sb = new StringBuilder();
        listRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference prefix : listResult.getPrefixes()) {
                        // prefix.getName() = folders
                        try {
                            HomeObjects object = new HomeObjects(
                                    prefix.getName(),
                                    prefix.getName(),
                                    0
                            );
                            adapterHome.addItem(object);
                            sb.append(prefix.getName()).append(",");
                        } catch (Exception e) {
                            Snackbar.make(recyclerView,R.string.errorGettingCategories,Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Try Again",view -> LoadFolders()).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                    context.getSharedPreferences(Constants.SETTINGS_PREF, Context.MODE_PRIVATE)
                            .edit().putString(Constants.CLASSES, sb.toString()).apply();
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(recyclerView,R.string.errorGettingCategories,Snackbar.LENGTH_INDEFINITE)
                            .setAction("Try Again",view -> LoadFolders())
                            .setTextColor(ContextCompat.getColor(context,R.color.white)).show();
                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}