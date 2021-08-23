package com.aashdit.olmoffline.models;

/**
 * Created by Manabendu on 07/01/21
 */
public class NavMenu {
    public String menuName;
    public int navIcon;
    public int menuIcon;
    public boolean hasItem;

    public NavMenu(String menuName, int navIcon, int menuIcon, boolean hasItem) {
        this.menuName = menuName;
        this.navIcon = navIcon;
        this.menuIcon = menuIcon;
        this.hasItem = hasItem;
    }
}
