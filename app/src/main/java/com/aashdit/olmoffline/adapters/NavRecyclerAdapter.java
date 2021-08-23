package com.aashdit.olmoffline.adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aashdit.olmoffline.R;
import com.aashdit.olmoffline.models.NavMenu;
import com.aashdit.olmoffline.utils.SharedPrefManager;

import java.util.List;

/**
 * Created by Manabendu on 07/01/21
 */
public class NavRecyclerAdapter extends RecyclerView.Adapter<NavRecyclerAdapter.NavRecyclerViewHolder> {

    private static final String TAG = NavRecyclerAdapter.class.getSimpleName();
    MenuItemClickListener menuItemClickListener;
    private final Context mContext;
    private final List<NavMenu> menuList;
    private boolean isOpen = false;
    private final SharedPrefManager sp;


    public NavRecyclerAdapter(Context context, List<NavMenu> data) {
        this.mContext = context;
        this.menuList = data;
        sp = SharedPrefManager.getInstance(context);
    }

    @NonNull
    @Override
    public NavRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.cell_expansion_panel, viewGroup, false);

        return new NavRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NavRecyclerViewHolder holder, int position) {
        // include binding logic here
        NavMenu menu = menuList.get(position);
        holder.mTvMenuName.setText(menu.menuName);
        isOpen = false;
        holder.mTvMenuName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuItemClickListener.onMenuClick(position, "");
                if (position == 1) {
                    holder.mIvExpansion.setVisibility(View.VISIBLE);
                    holder.mLlExpandLayout.setVisibility(View.GONE);

                    if (isOpen) {
//                        holder.mIvExpansion.setRotation(0);
                        rotateAntiClockwise(holder.mIvExpansion);
                        holder.mLlExpandLayout.setVisibility(View.GONE);
                        isOpen = false;
                    } else {
//                        holder.mIvExpansion.setRotation(90);
                        rotateClockwise(holder.mIvExpansion);
                        isOpen = true;
                        holder.mLlExpandLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    holder.mIvExpansion.setVisibility(View.GONE);
                    holder.mLlExpandLayout.setVisibility(View.GONE);
                }
            }
        });
        if (position == 1) {
            holder.mIvExpansion.setVisibility(View.VISIBLE);
            holder.mLlExpandLayout.setVisibility(View.GONE);
            isOpen = false;
            holder.mIvExpansion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isOpen) {
//                        holder.mIvExpansion.setRotation(0);
                        rotateAntiClockwise(holder.mIvExpansion);
                        holder.mLlExpandLayout.setVisibility(View.GONE);
                        isOpen = false;
                    } else {
//                        holder.mIvExpansion.setRotation(90);
                        rotateClockwise(holder.mIvExpansion);
                        isOpen = true;
                        holder.mLlExpandLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
        } else {
            holder.mIvExpansion.setVisibility(View.GONE);
            holder.mLlExpandLayout.setVisibility(View.GONE);
        }

        holder.mIvMenuLogo.setImageResource(menu.menuIcon);
//        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_PMITRA")) {
//            holder.mTvSubMenuOne.setVisibility(View.GONE);
//        } else {
            holder.mTvSubMenuOne.setVisibility(View.VISIBLE);
            holder.mTvSubMenuOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menuItemClickListener.onMenuClick(position, "one");
                }
            });
//        }
        holder.mTvSubMenuTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menuItemClickListener.onMenuClick(position, "two");
            }
        });
//        if (sp.getStringData(Constant.USER_ROLE).equals("ROLE_CRPCM")) {
//            holder.mTvSubMenuThree.setVisibility(View.GONE);
//        } else {
            holder.mTvSubMenuThree.setVisibility(View.VISIBLE);
            holder.mTvSubMenuThree.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menuItemClickListener.onMenuClick(position, "three");
                }
            });
//        }
    }

    public void rotateClockwise(View view) {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", 0f, 90f);
//        rotate.setRepeatCount(10);
        rotate.setDuration(500);
        rotate.start();
    }

    public void rotateAntiClockwise(View view) {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(view, "rotation", 90f, 0f);
//        rotate.setRepeatCount(10);
        rotate.setDuration(500);
        rotate.start();
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public void setMenuItemClickListener(MenuItemClickListener menuItemClickListener) {
        this.menuItemClickListener = menuItemClickListener;
    }

    public interface MenuItemClickListener {
        void onMenuClick(int pos, String type);
    }

    public static class NavRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView mTvMenuName;
        TextView mTvSubMenuOne;
        TextView mTvSubMenuTwo;
        TextView mTvSubMenuThree;
        ImageView mIvExpansion;
        ImageView mIvMenuLogo;
        LinearLayout mLlExpandLayout;

        public NavRecyclerViewHolder(View itemView) {
            super(itemView);

            mTvMenuName = itemView.findViewById(R.id.tv_menu_title);
            mIvExpansion = itemView.findViewById(R.id.headerIndicator);
            mIvMenuLogo = itemView.findViewById(R.id.iv_menu_logo);
            mLlExpandLayout = itemView.findViewById(R.id.ll_expand);
            mTvSubMenuOne = itemView.findViewById(R.id.tv_sub_menu_one);
            mTvSubMenuTwo = itemView.findViewById(R.id.tv_sub_menu_two);
            mTvSubMenuThree = itemView.findViewById(R.id.tv_sub_menu_three);
        }
    }
}