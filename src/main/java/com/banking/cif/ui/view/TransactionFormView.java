package com.banking.cif.ui.view;

import com.banking.cif.dto.TransactionRequest;
import com.banking.cif.service.TransactionService;
import com.banking.cif.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;

@Route(value = "transaction-form", layout = MainLayout.class)
public class TransactionFormView extends VerticalLayout {

    private final TransactionService transactionService;

    private IntegerField accountId = new IntegerField("Account ID");
    private ComboBox<String> type = new ComboBox<>("Transaction Type");
    private TextField amount = new TextField("Amount");
    private TextField description = new TextField("Description");

    private Button save = new Button("Submit");
    private Button cancel = new Button("Cancel");

    public TransactionFormView(TransactionService transactionService) {
        this.transactionService = transactionService;

        accountId.setRequiredIndicatorVisible(true);
        type.setItems("DEPOSIT", "WITHDRAWAL");
        type.setRequiredIndicatorVisible(true);
        amount.setRequiredIndicatorVisible(true);
        amount.setPlaceholder("0.00");

        add(createFormLayout(), createButtonLayout());
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.add(accountId, type, amount, description);
        return formLayout;
    }

    private HorizontalLayout createButtonLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(event -> validateAndSave());
        cancel.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate(TransactionListView.class)));
        return new HorizontalLayout(save, cancel);
    }

    private void validateAndSave() {
        try {
            if (accountId.getValue() == null) {
                Notification.show("Account ID is required.");
                return;
            }
            if (type.getValue() == null) {
                Notification.show("Transaction type is required.");
                return;
            }
            String amountText = amount.getValue();
            if (amountText == null || amountText.isBlank()) {
                Notification.show("Amount is required.");
                return;
            }

            TransactionRequest request = new TransactionRequest();
            request.setAccountId(accountId.getValue());
            request.setType(type.getValue());
            request.setAmount(new BigDecimal(amountText));
            request.setDescription(description.getValue());

            transactionService.createTransaction(request);
            Notification.show("Transaction completed successfully.");
            getUI().ifPresent(ui -> ui.navigate(TransactionListView.class));
        } catch (NumberFormatException e) {
            Notification.show("Invalid amount format. Please enter a valid number.");
        } catch (Exception e) {
            Notification.show("Error processing transaction: " + e.getMessage());
        }
    }
}
