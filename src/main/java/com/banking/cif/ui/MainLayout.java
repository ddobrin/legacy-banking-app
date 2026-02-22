package com.banking.cif.ui;

import com.banking.cif.ui.view.AccountListView;
import com.banking.cif.ui.view.CustomerListView;
import com.banking.cif.ui.view.TransactionListView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

public class MainLayout extends AppLayout {

    public MainLayout() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Legacy Banking App");
        title.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0");

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Customers", CustomerListView.class));
        nav.addItem(new SideNavItem("Accounts", AccountListView.class));
        nav.addItem(new SideNavItem("Transactions", TransactionListView.class));

        Scroller scroller = new Scroller(nav);

        addToDrawer(scroller);
        addToNavbar(toggle, title);
    }
}