package com.banking.cif.ui.view;

import com.banking.cif.dto.TransactionDTO;
import com.banking.cif.service.TransactionService;
import com.banking.cif.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.Collections;
import java.util.List;

@Route(value = "transactions", layout = MainLayout.class)
public class TransactionListView extends VerticalLayout {

    private final TransactionService transactionService;
    private final Grid<TransactionDTO> grid = new Grid<>(TransactionDTO.class, false);
    private final TextField searchField = new TextField();

    public TransactionListView(TransactionService transactionService) {
        this.transactionService = transactionService;
        setSizeFull();
        configureGrid();
        
        add(getToolbar(), grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addColumn(TransactionDTO::getTransactionId).setHeader("Transaction ID");
        grid.addColumn(TransactionDTO::getAccountId).setHeader("Account ID");
        grid.addColumn(TransactionDTO::getTransactionType).setHeader("Type");
        grid.addColumn(TransactionDTO::getAmount).setHeader("Amount");
        grid.addColumn(TransactionDTO::getBalanceAfter).setHeader("Balance After");
        grid.addColumn(TransactionDTO::getDescription).setHeader("Description");
        grid.addColumn(TransactionDTO::getTransactionDate).setHeader("Date");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private HorizontalLayout getToolbar() {
        searchField.setPlaceholder("Search by Account ID");
        searchField.setClearButtonVisible(true);

        Button searchButton = new Button("Search");
        searchButton.addClickListener(click -> searchTransactions());

        Button newTransactionButton = new Button("New Transaction");
        newTransactionButton.addClickListener(click -> getUI().ifPresent(ui -> ui.navigate(TransactionFormView.class)));

        HorizontalLayout toolbar = new HorizontalLayout(searchField, searchButton, newTransactionButton);
        return toolbar;
    }

    private void searchTransactions() {
        String value = searchField.getValue();
        if (value != null && !value.isEmpty()) {
            try {
                Integer id = Integer.parseInt(value);
                List<TransactionDTO> history = transactionService.getTransactionHistory(id);
                grid.setItems(history);
                if (history.isEmpty()) {
                    Notification.show("No transactions found for this account.");
                }
            } catch (NumberFormatException e) {
                Notification.show("Please enter a valid numeric Account ID.");
            } catch (Exception e) {
                Notification.show("Error retrieving transactions: " + e.getMessage());
                grid.setItems(Collections.emptyList());
            }
        } else {
            grid.setItems(Collections.emptyList());
        }
    }
}