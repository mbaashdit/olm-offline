package com.aashdit.olmoffline.ui.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.adapters.NavRecyclerAdapter;
import com.aashdit.olmoffline.databinding.FragmentNavigationBinding;
import com.aashdit.olmoffline.models.NavMenu;
import com.aashdit.olmoffline.ui.MainActivity;
import com.aashdit.olmoffline.utils.Constant;
import com.aashdit.olmoffline.utils.SharedPrefManager;

import java.util.ArrayList;

public class FragmentNavigation extends Fragment implements NavRecyclerAdapter.MenuItemClickListener {

    private static final String TAG = "FragmentNavigation";
    public DrawerLayout mDrawerLayout;
    public MenuClickListener listener;
    private FragmentNavigationBinding binding;

    private SharedPrefManager sp;

    private String userDesignation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNavigationBinding.inflate(inflater, container, false);
//        resetTitles();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sp = SharedPrefManager.getInstance(getActivity());
        binding.rvMenu.setLayoutManager(new LinearLayoutManager(getActivity()));
        userDesignation = sp.getStringData(Constant.USER_ROLE);
        binding.tvAgentDesignation.setText(userDesignation.replace("ROLE_", ""));

        ArrayList<NavMenu> navMenuArrayList = new ArrayList<>();
        navMenuArrayList.add(new NavMenu(getResources().getString(R.string.home), R.drawable.ic_olm_home, R.drawable.ic_olm_home, false));
        navMenuArrayList.add(new NavMenu(getResources().getString(R.string.activity_management), R.drawable.ic_olm_act_mgnt, R.drawable.ic_olm_act_mgnt, false));
        navMenuArrayList.add(new NavMenu(getResources().getString(R.string.training), R.drawable.ic_olm_training, R.drawable.ic_olm_training, false));
        navMenuArrayList.add(new NavMenu(getResources().getString(R.string.financial_management), R.drawable.ic_olm_fin_mgnt, R.drawable.ic_olm_fin_mgnt, false));
        navMenuArrayList.add(new NavMenu(getResources().getString(R.string.attendance), R.drawable.ic_olm_attendance, R.drawable.ic_olm_attendance, false));
        navMenuArrayList.add(new NavMenu(getResources().getString(R.string.profile), R.drawable.ic_profiling, R.drawable.ic_profiling, false));
        NavRecyclerAdapter menuAdapter = new NavRecyclerAdapter(getActivity(), navMenuArrayList);
        menuAdapter.setMenuItemClickListener(this);
        binding.rvMenu.setAdapter(menuAdapter);

        binding.rlLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });

        binding.tvAgentName.setText(sp.getStringData(Constant.USER_NAME));

    }

    public void setUp(DrawerLayout drawerLayout, MenuClickListener listener) {/*, MenuClickListener listener*/
        mDrawerLayout = drawerLayout;
        this.listener = listener;
    }

    @Override
    public void onDestroy() {
        mDrawerLayout = null;
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onMenuClick(int pos, String type) {
        listener.onClickMenuItem(pos, type);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Logout ?")
                .setMessage("Are you sure you want to logout ?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        sp.clear();
                        Intent intent = new Intent(getActivity(), MainActivity.class); //mContext.getIntent();
                        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        getActivity().finish();

                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public interface MenuClickListener {
        void onClickMenuItem(int position, String type);
    }

}
