package com.banking.cif.ui.view;

import com.banking.cif.dto.AccountDTO;
import com.banking.cif.service.AccountService;
import com.banking.cif.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.Collections;

@Route(value = "accounts", layout = MainLayout.class)
public class AccountListView extends VerticalLayout {

    private final AccountService accountService;
    private final Grid<AccountDTO> grid = new Grid<>(AccountDTO.class, false);
    private final TextField searchField = new TextField();

    public AccountListView(AccountService accountService) {
        this.accountService = accountService;
        setSizeFull();
        configureGrid();
        
        add(getToolbar(), grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(AccountDTO::getAccountId).setHeader("Account ID");
        grid.addColumn(AccountDTO::getCustomerId).setHeader("Customer ID");
        grid.addColumn(AccountDTO::getAccountNumber).setHeader("Account Number");
        grid.addColumn(AccountDTO::getProductCode).setHeader("Product Code");
        grid.addColumn(AccountDTO::getBalance).setHeader("Balance");
        grid.addColumn(AccountDTO::getStatus).setHeader("Status");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private HorizontalLayout getToolbar() {
        searchField.setPlaceholder("Search by Account ID or Number");
        searchField.setClearButtonVisible(true);

        Button searchButton = new Button("Search");
        searchButton.addClickListener(click -> searchAccount());

        Button addAccountButton = new Button("Add Account");
        addAccountButton.addClickListener(click -> getUI().ifPresent(ui -> ui.navigate(AccountFormView.class)));

        HorizontalLayout toolbar = new HorizontalLayout(searchField, searchButton, addAccountButton);
        return toolbar;
    }

    private void searchAccount() {
        String value = searchField.getValue();
        if (value != null && !value.isEmpty()) {
            try {
                Integer id = Integer.parseInt(value);
                AccountDTO account = accountService.getAccountById(id);
                grid.setItems(Collections.singletonList(account));
            } catch (NumberFormatException e) {
                Notification.show("Please enter a valid numeric ID.");
            } catch (Exception e) {
                Notification.show("Account not found.");
                grid.setItems(Collections.emptyList());
            }
        } else {
            grid.setItems(Collections.emptyList());
        }
    }
}